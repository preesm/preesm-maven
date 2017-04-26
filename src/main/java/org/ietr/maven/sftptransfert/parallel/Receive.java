package org.ietr.maven.sftptransfert.parallel;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import org.ietr.maven.sftptransfert.TransfertException;

public class Receive extends Transfer {

  protected Receive(final String localPath, final String remotePath) {
    super(localPath, remotePath);
  }

  @Override
  public void process(final ChannelSftp sftpChannel) {
    try {
      sftpChannel.get(this.remotePath, this.localPath);
    } catch (final SftpException e) {
      throw new TransfertException("Could not receive " + this.remotePath + ": " + e.getMessage() + e);
    }
  }

}
