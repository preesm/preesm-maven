package org.ietr.maven.sftptransfert.jsch.sessioninfos;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public interface SessionInfos {

  public Session openSession(final JSch jsch);
}
