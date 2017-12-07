package org.preesm.maven.sftptransfert.jsch.sessioninfos;

import org.preesm.maven.sftptransfert.TransfertException;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class PasswordSessionInfos extends AbstractSessionInfos {

  private final String password;

  public PasswordSessionInfos(final String host, final int port, final String user, final boolean strictHostKeyChecking, final String password) {
    super(host, port, user, strictHostKeyChecking);
    this.password = password;
  }

  @Override
  protected Session initSession(final JSch jsch) {

    Session session;
    try {
      session = jsch.getSession(this.user, this.host, this.port);
    } catch (final JSchException e) {
      throw new TransfertException("Could not connect: " + e.getMessage(), e);
    }
    session.setPassword(this.password);
    return session;
  }

}
