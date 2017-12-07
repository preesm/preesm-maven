package org.preesm.maven.sftptransfert.jsch.transfer;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.preesm.maven.sftptransfert.TransfertException;

public class WriteSymLink extends Transfer {

  public WriteSymLink(final String linkPath, final String remotePath) {
    super(linkPath, remotePath);
  }

  @Override
  public void process(final ChannelSftp sftpChannel) {
    final String linkPath = this.localPath;
    try {
      final Path path = Paths.get(this.remotePath);
      final Path parent = path.getParent();
      final String linkParentDirPath = parent.toString();
      // Jsch implementation actually requires to CD first.
      sftpChannel.cd(linkParentDirPath);
      final String actualLinkName = path.getFileName().toString();

      if (exists(sftpChannel)) {
        sftpChannel.rm(actualLinkName);
      }
      sftpChannel.symlink(linkPath, actualLinkName);

    } catch (final SftpException e) {
      throw new TransfertException("Could not write remote link " + this.remotePath + ": " + e.getMessage(), e);
    }
  }

  private final boolean exists(final ChannelSftp sftpChannel) {
    boolean exists = false;
    SftpATTRS lstat = null;
    try {
      lstat = sftpChannel.lstat(this.remotePath);
      exists = true;
    } catch (final SftpException e) {
      if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
        exists = false;
      } else {
        throw new TransfertException("Could not test if remote file " + this.remotePath + " exists:" + e.getMessage(), e);
      }
    }
    return exists && lstat.isLink();
  }
}
