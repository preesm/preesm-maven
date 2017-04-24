package org.ietr.maven.sftptransfert;

import org.apache.maven.plugin.logging.Log;

public final class PrivateKeySftpConnection extends SftpConnection {

  public PrivateKeySftpConnection(final Log log, final String sftpUser, final String sftpHost, final int sftpPort, final String keyPath,
      final boolean strictHostKeyChecking) {
    super(log, sftpUser, sftpHost, sftpPort, keyPath, null, strictHostKeyChecking);
  }

  public PrivateKeySftpConnection(final String sftpUser, final String sftpHost, final int sftpPort, final String keyPath, final boolean strictHostKeyChecking) {
    super(sftpUser, sftpHost, sftpPort, keyPath, null, strictHostKeyChecking);
  }

  public PrivateKeySftpConnection(final Log log, final String sftpUser, final String sftpHost, final int sftpPort, final String keyPath,
      final String keyPassPhrase, final boolean strictHostKeyChecking) {
    super(log, sftpUser, sftpHost, sftpPort, keyPath, keyPassPhrase, strictHostKeyChecking);
  }

  public PrivateKeySftpConnection(final String sftpUser, final String sftpHost, final int sftpPort, final String keyPath, final String keyPassPhrase,
      final boolean strictHostKeyChecking) {
    super(sftpUser, sftpHost, sftpPort, keyPath, keyPassPhrase, strictHostKeyChecking);
  }

}
