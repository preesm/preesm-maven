package org.ietr.maven.sftptransfert.jsch;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ietr.maven.sftptransfert.ISftpTransfertLayer;
import org.ietr.maven.sftptransfert.TransfertException;
import org.ietr.maven.sftptransfert.jsch.parallel.ParallelJschSftpTransfertLayer;
import org.ietr.maven.sftptransfert.jsch.sessioninfos.SessionInfos;
import org.ietr.maven.sftptransfert.jsch.transfer.Receive;
import org.ietr.maven.sftptransfert.jsch.transfer.Send;
import org.ietr.maven.sftptransfert.jsch.transfer.WriteSymLink;

public class JschSftpTransfertLayer implements ISftpTransfertLayer {

  private static final JSch DEFAULT_JSCH = new JSch();

  protected final SessionInfos infos;

  private boolean       connected       = false;
  private Session       session         = null;
  protected ChannelSftp mainSftpChannel = null;

  private final Map<String, SftpATTRS>     remotePathToAttributesCache = new HashMap<>();
  private final Map<String, List<LsEntry>> remotePathToLsEntryCache    = new HashMap<>();

  protected JschSftpTransfertLayer(final SessionInfos infos) {
    this.infos = infos;
  }

  public static final JSch getDefaultJsch() {
    return JschSftpTransfertLayer.DEFAULT_JSCH;
  }

  public static final JschSftpTransfertLayer build(final SessionInfos infos, final boolean parallel) {
    if (parallel) {
      return new ParallelJschSftpTransfertLayer(infos);
    } else {
      return new JschSftpTransfertLayer(infos);
    }
  }

  public final ChannelSftp getMainSftpChannel() {
    return this.mainSftpChannel;
  }

  public final SessionInfos getInfos() {
    return this.infos;
  }

  @Override
  public void connect() {
    if (this.connected) {
      throw new TransfertException("Already connected");
    }
    try {

      this.session = this.infos.openSession(JschSftpTransfertLayer.DEFAULT_JSCH);
      this.mainSftpChannel = (ChannelSftp) this.session.openChannel("sftp");
      if (this.mainSftpChannel == null) {
        throw new JSchException("Could not create channel", new NullPointerException());
      }
      this.mainSftpChannel.connect();
    } catch (final JSchException e) {
      throw new TransfertException("Could not connect: " + e.getMessage(), e);
    }
    this.connected = true;
  }

  @Override
  public void disconnect() {
    this.mainSftpChannel.exit();
    this.mainSftpChannel.disconnect();
    this.mainSftpChannel = null;
    this.session.disconnect();
    this.session = null;
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
        throw new TransfertException("Could not test if file " + remotePath + " exists:" + e.getMessage(), e);
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
      throw new TransfertException("Could not get attributes of " + remoteDirPath + ": " + e.getMessage(), e);
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
      throw new TransfertException("Could not get attributes of " + remotePath + ": " + e.getMessage(), e);
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
      throw new TransfertException("Could not list remote dir " + remoteDirPath + ":" + e.getMessage(), e);
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
      throw new TransfertException("Could not make remote dir " + remoteDirPath + ": " + e.getMessage(), e);
    }
  }

  @Override
  public final String readSymlink(final String remotePath) {
    String readlink;
    try {
      readlink = this.mainSftpChannel.readlink(remotePath);
    } catch (final SftpException e) {
      throw new TransfertException("Could not read remote link " + remotePath + ": " + e.getMessage(), e);
    }
    return readlink;
  }

  @Override
  public void receive(final String remoteFilePath, final String localFilePath) {
    new Receive(localFilePath, remoteFilePath).process(this.mainSftpChannel);
  }

  @Override
  public void send(final String localFilePath, final String remoteFilePath) {
    new Send(localFilePath, remoteFilePath).process(this.mainSftpChannel);
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
  public void writeSymlink(final String remotePath, final String linkPath) {
    new WriteSymLink(linkPath, remotePath).process(this.mainSftpChannel);
  }

  @Override
  public final void remove(final String remotePath) {
    try {
      this.mainSftpChannel.rm(remotePath);
    } catch (final SftpException e) {
      throw new TransfertException("Could not remove remote file " + remotePath + ": " + e.getMessage(), e);
    }
  }

  @Override
  public final void removeDir(final String remotePath) {
    try {
      this.mainSftpChannel.rmdir(remotePath);
    } catch (final SftpException e) {
      throw new TransfertException("Could not remove dir " + remotePath + ": " + e.getMessage(), e);
    }
  }

}
