package org.ietr.maven.sftptransfert;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import java.io.FileNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class JschSftpTransfertLayer implements ISftpTransfertLayer {

  private boolean     connected       = false;
  private Session     session         = null;
  private ChannelSftp mainSftpChannel = null;

  private final Map<String, SftpATTRS>     remotePathToAttributesCache = new HashMap<>();
  private final Map<String, List<LsEntry>> remotePathToLsEntryCache    = new HashMap<>();

  private JschSftpTransfertLayer() {
  }

  public static final JschSftpTransfertLayer build() {
    return new JschSftpTransfertLayer();
  }

  @Override
  public final void connectUsingKey(final String host, final int port, final String user, final String keyPath, final boolean strictHostKeyChecking) {
    connectTo(host, port, user, null, keyPath, null, strictHostKeyChecking);
  }

  @Override
  public void connectUsingKeyWithPassPhrase(final String host, final int port, final String user, final String keyPath, final String passPhrase,
      final boolean strictHostKeyChecking) {
    connectTo(host, port, user, null, keyPath, passPhrase, strictHostKeyChecking);
  }

  @Override
  public final void connectUsingPassword(final String host, final int port, final String user, final String password, final boolean strictHostKeyChecking) {
    connectTo(host, port, user, password, null, null, strictHostKeyChecking);
  }

  private final void connectTo(final String host, final int port, final String user, final String password, final String keyPath, final String keyPassphrase,
      final boolean strictHostKeyChecking) {
    if (this.connected) {
      throw new TransfertException("Already connected");
    }
    try {
      final JSch jsch = new JSch();

      if ((keyPath != null) && (keyPath != "")) {
        final Path keyFilePath = FileSystems.getDefault().getPath(keyPath);
        final boolean exists = keyFilePath.toFile().exists();
        if (!exists) {
          throw new FileNotFoundException("Key file " + keyPath + "not found in classpath");
        }
        if (keyPassphrase == null) {
          jsch.addIdentity(keyFilePath.toAbsolutePath().toString());
        } else {
          jsch.addIdentity(keyPath, keyPassphrase);
        }
        this.session = jsch.getSession(user, host, port);
      } else {
        this.session = jsch.getSession(user, host, port);
        this.session.setPassword(password);
      }

      final Properties config = new Properties();
      if (!strictHostKeyChecking) {
        // do not check for key checking
        config.put("StrictHostKeyChecking", "no");
      }
      this.session.setConfig(config);
      this.session.connect();
      this.connected = true;

      this.mainSftpChannel = (ChannelSftp) this.session.openChannel("sftp");
      if (this.mainSftpChannel == null) {
        throw new JSchException("Could not create channel", new NullPointerException());
      }
      this.mainSftpChannel.connect();
    } catch (final JSchException | FileNotFoundException e) {
      throw new TransfertException("Could not connect", e);
    }
  }

  @Override
  public final void disconnect() {
    this.session.disconnect();
    this.connected = false;
  }

  @Override
  public final boolean isConnected() {
    return this.connected;
  }

  @Override
  public final boolean exists(final String remotePath) {
    try {
      this.mainSftpChannel.lstat(remotePath);
      return true;
    } catch (final SftpException e) {
      if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
        return false;
      } else {
        throw new TransfertException("Could not test if file exists", e);
      }
    }
  }

  @Override
  public final boolean isDirectory(final String remoteDirPath) {
    try {
      if (!exists(remoteDirPath)) {
        return false;
      }
      final SftpATTRS lstat = lsAttrsCache(remoteDirPath);
      return lstat.isDir();
    } catch (final SftpException e) {
      throw new TransfertException("Could not get attributes", e);
    }
  }

  @Override
  public final boolean isSymlink(final String remotePath) {
    try {
      if (!exists(remotePath)) {
        return false;
      }
      final SftpATTRS lstat = lsAttrsCache(remotePath);
      return lstat.isLink();
    } catch (final SftpException e) {
      throw new TransfertException("Could not get attributes", e);
    }
  }

  private SftpATTRS lsAttrsCache(final String remotePath) throws SftpException {
    final String replace = remotePath.replace("//", "/");
    final boolean containsKey = this.remotePathToAttributesCache.containsKey(replace);
    SftpATTRS res;
    if (containsKey) {
      res = this.remotePathToAttributesCache.get(replace);
    } else {
      res = this.mainSftpChannel.lstat(replace);
      this.remotePathToAttributesCache.put(replace, res);
    }
    return res;
  }

  @Override
  public final List<String> ls(final String remoteDirPath) {
    final List<String> res = new ArrayList<>();

    try {
      final List<LsEntry> ls = lsCache(remoteDirPath);
      for (final LsEntry fileEntry : ls) {
        final String filename = fileEntry.getFilename();
        if (".".equals(filename) || "..".equals(filename) || filename.startsWith(".")) {
          continue;
        }
        populateAttributesCache(remoteDirPath, fileEntry);
        res.add(remoteDirPath + "/" + filename);
      }
    } catch (final SftpException e) {
      throw new TransfertException("Could not make dir", e);
    }

    return res;
  }

  private List<LsEntry> lsCache(final String remotePath) throws SftpException {
    final String replace = remotePath.replace("//", "/");
    final boolean containsKey = this.remotePathToLsEntryCache.containsKey(replace);
    List<LsEntry> res;
    if (containsKey) {
      res = this.remotePathToLsEntryCache.get(replace);
    } else {
      @SuppressWarnings("unchecked")
      final ArrayList<LsEntry> arrayList = new ArrayList<>(this.mainSftpChannel.ls(remotePath));
      res = arrayList;
      this.remotePathToLsEntryCache.put(replace, res);
    }
    return res;
  }

  private void populateAttributesCache(final String dirPath, final LsEntry fileEntry) {
    final String filename = fileEntry.getFilename();
    final String fullRemotePath = (dirPath + "/" + filename).replace("//", "/");
    final SftpATTRS attrs = fileEntry.getAttrs();
    this.remotePathToAttributesCache.put(fullRemotePath, attrs);
  }

  @Override
  public final void mkdir(final String remoteDirPath) {
    try {
      this.mainSftpChannel.mkdir(remoteDirPath);
    } catch (final SftpException e) {
      throw new TransfertException("Could not make dir", e);
    }
  }

  @Override
  public final void mkdirs(final String remoteDirPath) {
    final Path remoteDestinationDir = Paths.get(remoteDirPath);
    final Deque<String> parents = new ArrayDeque<>();
    Path parent = remoteDestinationDir;
    while (parent != null) {
      parents.push(parent.toAbsolutePath().toString());
      parent = parent.getParent();
    }
    while (!parents.isEmpty()) {
      final String currentParentToTest = parents.pop();
      final boolean existDir = isDirectory(currentParentToTest);
      if (!existDir) {
        mkdir(currentParentToTest);
      }
    }
  }

  @Override
  public final String readSymlink(final String remotePath) {
    String readlink;
    try {
      readlink = this.mainSftpChannel.readlink(remotePath);
    } catch (final SftpException e) {
      throw new TransfertException("Could not read link", e);
    }
    return readlink;
  }

  @Override
  public final void receive(final String remoteFilePath, final String localFilePath) {
    try {
      this.mainSftpChannel.get(remoteFilePath, localFilePath);
    } catch (final SftpException e) {
      throw new TransfertException("Receive failed : " + e.getMessage(), e);
    }
  }

  @Override
  public final void send(final String localFilePath, final String remoteFilePath) {
    try {
      this.mainSftpChannel.put(localFilePath, remoteFilePath);
    } catch (final SftpException e) {
      throw new TransfertException("Send failed : " + e.getMessage(), e);
    }
  }

  @Override
  public String toString() {
    if (isConnected()) {
      return "SftpConnection (" + this.session.getUserName() + "@" + this.session.getHost() + ":" + this.session.getPort() + ")";
    } else {
      return "SftpConnection (disconneced)";
    }
  }

  @Override
  public final void writeSymlink(final String remotePath, final String linkPath) {
    try {
      final Path path = Paths.get(remotePath);
      final Path parent = path.getParent();
      final String linkParentDirPath = parent.toString();
      // Jsch implementation actually requires to CD first.
      this.mainSftpChannel.cd(linkParentDirPath);
      final String actualLinkName = path.getFileName().toString();

      if (isSymlink(remotePath)) {
        this.mainSftpChannel.rm(actualLinkName);
      }
      this.mainSftpChannel.symlink(linkPath, actualLinkName);

    } catch (final SftpException e) {
      throw new TransfertException("Could not write link", e);
    }
  }

}
