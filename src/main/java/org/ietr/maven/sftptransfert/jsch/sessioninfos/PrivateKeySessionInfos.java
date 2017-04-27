package org.ietr.maven.sftptransfert.jsch.sessioninfos;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.FileNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.ietr.maven.sftptransfert.TransfertException;

public class PrivateKeySessionInfos extends AbstractSessionInfos {

  private final String keyPath;
  private final String keyPassPhrase;

  public PrivateKeySessionInfos(final String host, final int port, final String user, final boolean strictHostKeyChecking, final String keyPath,
      final String keyPassPhrase) {
    super(host, port, user, strictHostKeyChecking);
    this.keyPath = keyPath;
    this.keyPassPhrase = keyPassPhrase;
  }

  public PrivateKeySessionInfos(final String host, final int port, final String user, final boolean strictHostKeyChecking, final String keyPath) {
    this(host, port, user, strictHostKeyChecking, keyPath, null);
  }

  @Override
  protected Session initSession(final JSch jsch) {

    Session session;
    try {
      final Path keyFilePath = FileSystems.getDefault().getPath(this.keyPath);
      final boolean exists = keyFilePath.toFile().exists();
      if (!exists) {
        throw new FileNotFoundException("Key file " + this.keyPath + "not found in classpath");
      }
      if (this.keyPassPhrase == null) {
        jsch.addIdentity(keyFilePath.toAbsolutePath().toString());
      } else {
        jsch.addIdentity(keyFilePath.toAbsolutePath().toString(), this.keyPassPhrase);
      }
      session = jsch.getSession(this.user, this.host, this.port);
    } catch (JSchException | FileNotFoundException e) {
      throw new TransfertException("Could not connect: " + e.getMessage(), e);
    }
    return session;
  }

}
