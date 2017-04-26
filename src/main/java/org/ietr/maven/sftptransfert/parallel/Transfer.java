package org.ietr.maven.sftptransfert.parallel;

import com.jcraft.jsch.ChannelSftp;

public abstract class Transfer {

  protected final String localPath;
  protected final String remotePath;

  protected Transfer(final String localPath, final String remotePath) {
    this.localPath = localPath;
    this.remotePath = remotePath;
  }

  public String getLocalPath() {
    return this.localPath;
  }

  public String getRemotePath() {
    return this.remotePath;
  }

  public abstract void process(ChannelSftp sftpChannel);

}
