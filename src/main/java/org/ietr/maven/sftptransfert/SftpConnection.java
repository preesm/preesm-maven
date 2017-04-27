package org.ietr.maven.sftptransfert;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.ietr.maven.sftptransfert.jsch.JschSftpTransfertLayer;
import org.ietr.maven.sftptransfert.sessioninfos.SessionInfos;

public class SftpConnection {

  private final Log                 log;
  private final ISftpTransfertLayer connect;
  private boolean                   fastChecks = false;
  private int                       dirLevel   = 0;
  private int                       fastCheckDirLevel;

  public SftpConnection(final SessionInfos infos, final boolean parallel) {
    this(new SystemStreamLog(), infos, parallel);
  }

  public SftpConnection(final Log log, final SessionInfos infos, final boolean parallel) {
    this.log = log;
    this.connect = JschSftpTransfertLayer.build(infos, parallel);
    this.connect.connect();
  }

  public final void disconnect() {
    this.connect.disconnect();
    this.log.debug(MessageFormat.format("Disconnected from {0}", this.connect));
  }

  public final boolean isConnected() {
    return this.connect.isConnected();
  }

  private final void testExists(final String remotePath) throws MojoFailureException {
    final boolean exists = this.connect.exists(remotePath);
    if (!exists) {
      final String message = MessageFormat.format("Remote path {0} does not exist.", remotePath);
      this.log.warn(message);
      throw new MojoFailureException(message);
    }
  }

  public final void remove(final String remotePath) throws MojoFailureException {
    if (remotePath == null) {
      return;
    }
    testConnection();
    try {
      this.log.info("removing remote path " + remotePath);

      testExists(remotePath);

      if (this.connect.isDirectory(remotePath)) {
        removeDir(remotePath);
      } else {
        this.connect.remove(remotePath);
      }
    } catch (final Exception e) {
      final String message = MessageFormat.format("Could not remove {0} : {1}", remotePath, e.getMessage());
      this.log.error(message, e);
    }

  }

  private void removeDir(final String remotePath) throws MojoFailureException {
    final List<String> ls = this.connect.ls(remotePath);
    for (final String subPath : ls) {
      SftpConnection.this.remove(subPath);
    }
    this.connect.removeDir(remotePath);
  }

  public final void receive(final String remotePath, final String localPath) throws MojoFailureException {
    testConnection();
    try {

      this.log.info("receiving " + remotePath);

      testExists(remotePath);

      if (this.connect.isSymlink(remotePath)) {
        final String message = MessageFormat.format("Remote path {0} points to a syminl. Using receiveSymlink().", remotePath);
        this.log.debug(message);
        receiveSymlink(remotePath, localPath);
      } else {
        if (this.connect.isDirectory(remotePath)) {
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

  private void receiveDir(final String remotePath, final String localPath) throws IOException, MojoFailureException {
    try {
      final Path localDirPath = FileSystems.getDefault().getPath(localPath);
      Files.createDirectories(localDirPath);

      final List<String> ls = this.connect.ls(remotePath);
      for (final String subPath : ls) {
        final Path path = Paths.get(subPath);
        final String childFileName = path.getFileName().toString();
        SftpConnection.this.receive(subPath, localPath + "/" + childFileName);
      }
    } catch (final Exception e) {
      final String message = MessageFormat.format("Could not receive {0} : {1}", remotePath, e.getMessage());
      this.log.error(message, e);
      throw new MojoFailureException(e, message, message);
    }
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
    if (localParentDirPath != null) {
      Files.createDirectories(localParentDirPath);
    }

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
      mkdirs(remoteParentPathString);
      this.fastChecks = true;
      this.fastCheckDirLevel = this.dirLevel;
    }

    final String remotePathString = SftpConnection.cleanupPath(remoteDirPath.toString());
    final boolean exists = this.connect.exists(remotePathString);
    if (!exists) {
      this.connect.mkdir(remotePathString);
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
      mkdirs(remoteParentPathString);
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
      mkdirs(remoteParentPathString);
    }
    this.connect.writeSymlink(remotePath, localSymbolicLinkStringValue);
  }

  private final void mkdirs(final String remoteDirPath) {
    final Path remoteDestinationDir = Paths.get(remoteDirPath);
    final Deque<String> parents = new ArrayDeque<>();
    Path parent = remoteDestinationDir;
    while (parent != null) {
      final String parentString = parent.toAbsolutePath().toString();
      final String cleanedPath = SftpConnection.cleanupPath(parentString);
      parents.push(cleanedPath);
      parent = parent.getParent();
    }
    while (!parents.isEmpty()) {
      final String currentParentToTest = parents.pop();
      final boolean existDir = this.connect.exists(currentParentToTest);
      if (!existDir) {
        this.connect.mkdir(currentParentToTest);
      }
    }
  }

  private static final String cleanupPath(final String remoteDirPath) {
    final int prefixLength = FilenameUtils.getPrefixLength(remoteDirPath);
    final String separatorsToUnix = FilenameUtils.separatorsToUnix(remoteDirPath);
    return separatorsToUnix.substring(prefixLength - 1);
  }

  private void testConnection() throws MojoFailureException {
    if (!isConnected()) {
      final String message = MessageFormat.format("Cannot initiate file transfert: {0} is not connected.", this.getClass().getSimpleName());
      this.log.error(message);
      throw new MojoFailureException(message);
    }
  }

}
