package sftp.maven.plugin.test;

import java.text.MessageFormat;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.ietr.maven.sftptransfert.SftpConnection;
import org.ietr.maven.sftptransfert.sessioninfos.PasswordSessionInfos;
import org.ietr.maven.sftptransfert.sessioninfos.SessionInfos;

public class AbstractTransfertTestSettings {

  static final Log     log                      = new SystemStreamLog();
  static final String  sftpHost                 = "";
  static final String  sftpUser                 = "";
  static final String  sftpPassword             = "";
  static final int     sftpPort                 = 22;
  static final boolean strictHostKeyChecking    = false;
  static final String  sftpPrivateKey           = null;
  static final String  sftpPrivateKeyPassphrase = null;

  static final SessionInfos infos = new PasswordSessionInfos(AbstractTransfertTestSettings.sftpHost, AbstractTransfertTestSettings.sftpPort,
      AbstractTransfertTestSettings.sftpUser, AbstractTransfertTestSettings.strictHostKeyChecking, AbstractTransfertTestSettings.sftpPassword);

  static SftpConnection sftpTransfert;

  static final void connect() {
    AbstractTransfertTestSettings.sftpTransfert = new SftpConnection(AbstractTransfertTestSettings.log, infos);
  }

  static final void disconnect() {
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
        AbstractTransfertTestSettings.log.error(message);
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
