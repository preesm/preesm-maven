package sftp.maven.plugin.test;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;

public interface Settings {

  static final Log     log                      = new SystemStreamLog();
  static final String  sftpHost                 = "";
  static final String  sftpUser                 = "";
  static final String  sftpPassword             = "";
  static final int     sftpPort                 = 22;
  static final boolean strictHostKeyChecking    = false;
  static final String  sftpPrivateKey           = "";
  static final String  sftpPrivateKeyPassphrase = null;
}
