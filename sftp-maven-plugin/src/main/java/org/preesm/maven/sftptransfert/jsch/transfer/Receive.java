package org.preesm.maven.sftptransfert.jsch.transfer;

import org.preesm.maven.sftptransfert.TransfertException;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

public class Receive extends Transfer {

  public Receive(final String localPath, final String remotePath) {
    super(localPath, remotePath);
  }

  @Override
  public void process(final ChannelSftp sftpChannel) {
    try {
      sftpChannel.get(this.remotePath, this.localPath);
    } catch (final SftpException e) {
      throw new TransfertException("Could not receive " + this.remotePath + ": " + e.getMessage(), e);
    }
  }
}
