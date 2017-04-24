package org.ietr.maven.sftptransfert;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;

public abstract class SftpConnection {

  private final Log                 log;
  private final ISftpTransfertLayer connect;
  private boolean                   fastChecks = false;
  private int                       dirLevel   = 0;
  private int                       fastCheckDirLevel;

  protected SftpConnection(final String sftpUser, final String sftpHost, final int sftpPort, final String sftpPassword, final boolean strictHostKeyChecking) {
    this(new SystemStreamLog(), sftpUser, sftpHost, sftpPort, sftpPassword, strictHostKeyChecking);
  }

  protected SftpConnection(final Log log, final String sftpUser, final String sftpHost, final int sftpPort, final String sftpPassword,
      final boolean strictHostKeyChecking) {
    this.log = log;
    this.connect = JschSftpTransfertLayer.build();
    this.connect.connectUsingPassword(sftpHost, sftpPort, sftpUser, sftpPassword, strictHostKeyChecking);
  }

  protected SftpConnection(final String sftpUser, final String sftpHost, final int sftpPort, final String keyPath, final String keyPassPhrase,
      final boolean strictHostKeyChecking) {
    this(new SystemStreamLog(), sftpUser, sftpHost, sftpPort, keyPath, keyPassPhrase, strictHostKeyChecking);
  }

  protected SftpConnection(final Log log, final String sftpUser, final String sftpHost, final int sftpPort, final String keyPath, final String keyPassPhrase,
      final boolean strictHostKeyChecking) {
    this.log = log;
    this.connect = JschSftpTransfertLayer.build();
    this.connect.connectUsingKeyWithPassPhrase(sftpHost, sftpPort, sftpUser, keyPath, keyPassPhrase, strictHostKeyChecking);
  }

  public final void disconnect() {
    this.connect.disconnect();
    this.log.debug(MessageFormat.format("Disconnected from {0}", this.connect));
  }

  public final boolean isConnected() {
    return this.connect.isConnected();
  }

  private static final String cleanupPath(final String remoteDirPath) {
    final String separatorsToUnix = FilenameUtils.separatorsToUnix(remoteDirPath);
    final int prefixLength = FilenameUtils.getPrefixLength(separatorsToUnix);
    return separatorsToUnix.substring(prefixLength - 1);
  }

  public final void receive(final String remotePath, final String localPath) throws MojoFailureException {
    testConnection();
    try {

      this.log.info("receiving " + remotePath);

      final boolean isDirectory = this.connect.isDirectory(remotePath);
      final boolean isSymlink = this.connect.isSymlink(remotePath);

      if (isSymlink) {
        final String message = MessageFormat.format("Remote path {0} points to a syminl. Using receiveSymlink().", remotePath);
        this.log.debug(message);
        receiveSymlink(remotePath, localPath);
      } else {
        if (isDirectory) {
          final String message = MessageFormat.format("Remote path {0} points to a directory. Using receiveDir().", remotePath);
          this.log.debug(message);
          receiveDir(remotePath, localPath);
        } else {
          final String message = MessageFormat.format("Remote path {0} points to a file. Using receiveFile().", remotePath);
          this.log.debug(message);
          receiveFile(remotePath, localPath);
        }
      }
    } catch (final Exception e) {
      final String message = MessageFormat.format("Could not receive {0} : {1}", remotePath, e.getMessage());
      this.log.error(message, e);
      throw new MojoFailureException(e, message, message);
    }
  }

  private void receiveDir(final String remotePath, final String localPath) throws IOException {
    final Path localDirPath = FileSystems.getDefault().getPath(localPath);
    final Path localParentDirPath = localDirPath.getParent();
    if (localParentDirPath != null) {
      Files.createDirectories(localParentDirPath);
    }

    final List<String> ls = this.connect.ls(remotePath);
    ls.forEach(s -> {
      try {
        final Path path = Paths.get(s);
        final String childFileName = path.getFileName().toString();
        SftpConnection.this.receive(s, localPath + "/" + childFileName);
      } catch (final Exception e) {
        this.log.error(e);
      }
    });
  }

  private void receiveFile(final String remotePath, final String localPath) throws IOException {
    final Path localFilePath = Paths.get(localPath);
    final Path localParentDirPath = localFilePath.getParent();
    Files.createDirectories(localParentDirPath);

    this.connect.receive(remotePath, localPath);
  }

  private void receiveSymlink(final String remotePath, final String localPath) throws IOException {
    final Path localLinkPath = Paths.get(localPath);
    final Path localParentDirPath = localLinkPath.getParent();
    Files.createDirectories(localParentDirPath);

    final String readSymlink = this.connect.readSymlink(remotePath);
    final Path symLinkPath = Paths.get(readSymlink);

    Files.deleteIfExists(localLinkPath);
    Files.createSymbolicLink(localLinkPath, symLinkPath);
  }

  public final void send(final String localPath, final String remotePath) throws MojoFailureException {
    testConnection();
    try {

      this.log.info("sending " + localPath);

      final Path path = FileSystems.getDefault().getPath(localPath);
      final boolean isDirectory = path.toFile().isDirectory();
      final boolean isSymbolicLink = Files.isSymbolicLink(path);

      if (isSymbolicLink) {
        final String message = MessageFormat.format("Local path {0} points to a symlink. Using sendSymlink().", localPath);
        this.log.debug(message);
        sendSymlink(localPath, remotePath);
      } else {
        if (isDirectory) {
          final String message = MessageFormat.format("Local path {0} points to a directory. Using sendDir().", localPath);
          this.log.debug(message);
          sendDir(localPath, remotePath);
        } else {
          final String message = MessageFormat.format("Local path {0} points to a file. Using sendFile().", localPath);
          this.log.debug(message);
          sendFile(localPath, remotePath);
        }
      }
    } catch (final Exception e) {
      final String message = MessageFormat.format("Could not send {0} : {1}", localPath, e.getMessage());
      this.log.error(message, e);
      throw new MojoFailureException(e, message, message);
    }
  }

  private void sendDir(final String localPath, final String remotePath) throws IOException {
    this.dirLevel++;
    final Path remoteDirPath = Paths.get(remotePath);
    final Path remoteParentPath = remoteDirPath.getParent();
    final String remoteParentPathString = SftpConnection.cleanupPath(remoteParentPath.toString());
    if (!this.fastChecks) {
      this.connect.mkdirs(remoteParentPathString);
      this.fastChecks = true;
      this.fastCheckDirLevel = this.dirLevel;
    }

    final boolean isDirectory = this.connect.isDirectory(remotePath);
    if (!isDirectory) {
      this.connect.mkdir(remotePath);
    }

    final Path path = FileSystems.getDefault().getPath(localPath);
    Files.list(path).forEach(f -> {
      try {
        final String string = f.getFileName().toString();
        SftpConnection.this.send(f.toString(), remotePath + "/" + string);
      } catch (final Exception e) {
        this.log.error(e);
      }
    });

    if (this.fastCheckDirLevel == this.dirLevel) {
      this.fastChecks = false;
    }
    this.dirLevel--;
  }

  private void sendFile(final String localPath, final String remotePath) {
    final Path remoteFilePath = Paths.get(remotePath);
    final Path remoteParentPath = remoteFilePath.getParent();
    final String remoteParentPathString = SftpConnection.cleanupPath(remoteParentPath.toString());

    if (!this.fastChecks) {
      this.connect.mkdirs(remoteParentPathString);
    }
    this.connect.send(localPath, remotePath);
  }

  private void sendSymlink(final String localPath, final String remotePath) throws IOException {
    final Path localLinkPath = Paths.get(localPath);
    final Path localSymbolicLinkDestPath = Files.readSymbolicLink(localLinkPath);
    final String localSymbolicLinkStringValue = localSymbolicLinkDestPath.toString();

    final Path remoteParentPath = localLinkPath.getParent();
    final String remoteParentPathString = SftpConnection.cleanupPath(remoteParentPath.toString());

    if (!this.fastChecks) {
      this.connect.mkdirs(remoteParentPathString);
    }
    this.connect.writeSymlink(remotePath, localSymbolicLinkStringValue);
  }

  private void testConnection() throws MojoFailureException {
    if (!isConnected()) {
      final String message = MessageFormat.format("Cannot initiate file transfert: {0} is not connected.", this.getClass().getSimpleName());
      this.log.error(message);
      throw new MojoFailureException(message);
    }
  }

}
