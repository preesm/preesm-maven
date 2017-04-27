package org.ietr.maven.sftptransfert.sessioninfos;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.util.Properties;
import org.ietr.maven.sftptransfert.TransfertException;

public abstract class AbstractSessionInfos implements SessionInfos {

  final String  host;
  final int     port;
  final String  user;
  final boolean strictHostKeyChecking;

  public AbstractSessionInfos(final String host, final int port, final String user, final boolean strictHostKeyChecking) {
    this.host = host;
    this.port = port;
    this.user = user;
    this.strictHostKeyChecking = strictHostKeyChecking;
  }

  protected abstract Session initSession(JSch jsch);

  @Override
  public Session openSession(final JSch jsch) {

    final Session session = initSession(jsch);

    final Properties config = new Properties();
    if (!this.strictHostKeyChecking) {
      config.put("StrictHostKeyChecking", "no");
    }
    session.setConfig(config);
    try {
      session.connect();
    } catch (final JSchException e) {
      throw new TransfertException("Could not connect: " + e.getMessage(), e);
    }

    return session;
  }

}
