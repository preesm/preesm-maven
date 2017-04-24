package org.ietr.maven.sftptransfert;

import org.apache.maven.plugin.logging.Log;

public class PasswordSftpConnection extends SftpConnection {

  public PasswordSftpConnection(final Log log, final String sftpUser, final String sftpHost, final int sftpPort, final String sftpPassword,
      final boolean strictHostKeyChecking) {
    super(log, sftpUser, sftpHost, sftpPort, sftpPassword, strictHostKeyChecking);
  }

  public PasswordSftpConnection(final String sftpUser, final String sftpHost, final int sftpPort, final String sftpPassword,
      final boolean strictHostKeyChecking) {
    super(sftpUser, sftpHost, sftpPort, sftpPassword, strictHostKeyChecking);
  }

}
