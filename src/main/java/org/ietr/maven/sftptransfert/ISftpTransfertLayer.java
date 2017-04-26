package org.ietr.maven.sftptransfert;

import java.util.List;
import org.ietr.maven.sftptransfert.sessioninfos.SessionInfos;

public interface ISftpTransfertLayer {
  public void connect(final SessionInfos infos);

  public void disconnect();

  public boolean isConnected();

  public boolean exists(final String remotePath);

  /**
   * Returns false if the path points to a symlink
   */
  public boolean isDirectory(final String dirPath);

  public boolean isSymlink(final String remotePath);

  public List<String> ls(final String remoteDirPath);

  public void mkdir(final String remoteDirPath);

  public String readSymlink(final String remotePath);

  public void receive(final String remoteFilePath, final String localFilePath);

  public void send(final String localFilePath, final String remoteFilePath);

  public void writeSymlink(final String remotePath, final String linkPath);

  public void remove(final String remotePath);

  public void removeDir(final String remotePath);

}
