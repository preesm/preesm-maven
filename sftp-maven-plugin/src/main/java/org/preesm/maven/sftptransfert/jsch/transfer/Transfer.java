package org.preesm.maven.sftptransfert.jsch.transfer;

import com.jcraft.jsch.ChannelSftp;

public abstract class Transfer {

  protected final String localPath;
  protected final String remotePath;

  protected Transfer(final String localPath, final String remotePath) {
    this.localPath = localPath;
    this.remotePath = remotePath;
  }

  public abstract void process(ChannelSftp sftpChannel);

}
