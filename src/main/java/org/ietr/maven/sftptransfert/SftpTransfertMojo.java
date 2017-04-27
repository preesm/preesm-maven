package org.ietr.maven.sftptransfert;

import java.io.File;
import java.text.MessageFormat;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.ietr.maven.sftptransfert.jsch.sessioninfos.PasswordSessionInfos;
import org.ietr.maven.sftptransfert.jsch.sessioninfos.PrivateKeySessionInfos;
import org.ietr.maven.sftptransfert.jsch.sessioninfos.SessionInfos;

@Mojo(name = "sftp-transfert", defaultPhase = LifecyclePhase.NONE)
public final class SftpTransfertMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  public MavenProject project;

  @Parameter(property = "serverId", required = true)
  public String serverId;

  @Parameter(property = "serverHost", required = true)
  public String serverHost;

  @Parameter(property = "mode", defaultValue = "receive", required = true)
  public String mode;

  @Parameter(defaultValue = "22", property = "serverPort", required = true)
  public int serverPort;

  @Parameter(defaultValue = "true", property = "strictHostKeyChecking", required = true)
  public boolean strictHostKeyChecking;

  @Parameter(property = "localPath", required = true)
  public String localPath;
  @Parameter(property = "remotePath", required = true)
  public String remotePath;

  @Parameter(defaultValue = "${settings}", readonly = true)
  public Settings settings;

  @Parameter(defaultValue = "${project.basedir}", readonly = true)
  public File basedir;

  @Parameter(defaultValue = "${project.build.directory}", readonly = true)
  public File target;

  private Log log;

  @Override
  public final void execute() throws MojoExecutionException, MojoFailureException {
    this.log = getLog();

    final SessionInfos sessionInfos = resolveSessionInfos();
    final boolean receivingMode = resolveMode();

    final SftpConnection sftpTransfert = new SftpConnection(this.log, sessionInfos, true);
    try {
      if (receivingMode) {
        sftpTransfert.receive(this.remotePath, this.localPath);
      } else {
        sftpTransfert.send(this.localPath, this.remotePath);
      }
    } finally {
      sftpTransfert.disconnect();
    }
  }

  private SessionInfos resolveSessionInfos() throws MojoFailureException {
    final String sftpHost = this.serverHost;
    final int sftpPort = this.serverPort;

    final Server server = this.settings.getServer(this.serverId);
    if (server == null) {
      final String message = MessageFormat
          .format("Error: Could not find server with id \"{0}\". Make sure you have a <servers>...</servers> section with proper <server> "
              + "configuration in your maven settings. See https://maven.apache.org/settings.html#Servers.", this.serverId);
      this.log.error(message);
      throw new MojoFailureException(message);
    }

    final String sftpUser = server.getUsername();
    final String sftpPassword = server.getPassword();
    final String sftpPrivateKey = server.getPrivateKey();
    final String sftpPrivateKeyPassphrase = server.getPassphrase();

    final SessionInfos sessionInfos;
    if (sftpPassword != null) {
      sessionInfos = new PasswordSessionInfos(sftpHost, sftpPort, sftpUser, this.strictHostKeyChecking, sftpPassword);
    } else {
      sessionInfos = new PrivateKeySessionInfos(sftpHost, sftpPort, sftpUser, this.strictHostKeyChecking, sftpPrivateKey, sftpPrivateKeyPassphrase);
    }
    return sessionInfos;
  }

  private boolean resolveMode() throws MojoFailureException {
    boolean receivingMode;
    if ("receive".equals(this.mode)) {
      receivingMode = true;
    } else {
      if ("send".equals(this.mode)) {
        receivingMode = false;
      } else {
        final String message = MessageFormat.format("Unsupported mode {0}. Supported modes are receive (default) and send.", this.mode);
        this.log.error(message);
        throw new MojoFailureException(message);
      }
    }
    return receivingMode;
  }
}
