package org.ietr.maven.sftptransfert.jsch.transfer;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import org.ietr.maven.sftptransfert.TransfertException;

public class Send extends Transfer {

  public Send(final String localPath, final String remotePath) {
    super(localPath, remotePath);
  }

  @Override
  public void process(final ChannelSftp sftpChannel) {
    try {
      sftpChannel.put(this.localPath, this.remotePath);
    } catch (final SftpException e) {
      throw new TransfertException("Could not send " + this.localPath + ": " + e.getMessage(), e);
    }
  }
}
