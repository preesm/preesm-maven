package org.preesm.maven.sftptransfert.test;

import java.text.MessageFormat;
import org.apache.maven.plugin.MojoFailureException;
import org.preesm.maven.sftptransfert.SftpConnection;
import org.preesm.maven.sftptransfert.jsch.sessioninfos.PasswordSessionInfos;
import org.preesm.maven.sftptransfert.jsch.sessioninfos.PrivateKeySessionInfos;
import org.preesm.maven.sftptransfert.jsch.sessioninfos.SessionInfos;

public class AbstractTransfertTestSettings implements Settings {

  static final SessionInfos passwdInfos = new PasswordSessionInfos(Settings.sftpHost, Settings.sftpPort, Settings.sftpUser, Settings.strictHostKeyChecking,
      Settings.sftpPassword);

  static final SessionInfos keyInfos = new PrivateKeySessionInfos(Settings.sftpHost, Settings.sftpPort, Settings.sftpUser, Settings.strictHostKeyChecking,
      Settings.sftpPrivateKey);

  static SftpConnection sftpTransfert;

  public static void connect() {
    AbstractTransfertTestSettings.sftpTransfert = new SftpConnection(AbstractTransfertTestSettings.passwdInfos, 1);
  }

  static final void connect(final int transferThreadCount) {
    AbstractTransfertTestSettings.sftpTransfert = new SftpConnection(Settings.log, AbstractTransfertTestSettings.keyInfos, 4);
  }

  static final void disconnect() throws MojoFailureException {
    AbstractTransfertTestSettings.sftpTransfert.disconnect();
  }

  static final void transfer(final String mode, final String remotePath, final String localPath) throws MojoFailureException {

    final boolean receivingMode;

    if ("receive".equals(mode)) {
      receivingMode = true;
    } else {
      if ("send".equals(mode)) {
        receivingMode = false;
      } else {
        final String message = MessageFormat.format("Unsupported mode {0}. Supported modes are receive (default) and send.", mode);
        Settings.log.error(message);
        throw new MojoFailureException(message);
      }
    }
    if (receivingMode) {
      AbstractTransfertTestSettings.sftpTransfert.receive(remotePath, localPath);
    } else {
      AbstractTransfertTestSettings.sftpTransfert.send(localPath, remotePath);
    }
  }

}
