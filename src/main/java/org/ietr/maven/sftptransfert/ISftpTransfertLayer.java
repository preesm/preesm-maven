package org.ietr.maven.sftptransfert;

import java.util.List;

public interface ISftpTransfertLayer {
  public void connectTo(final String host, final int port, final String user, final String password, final boolean strict);

  public void disconnect();

  public boolean isConnected();

  /**
   * Returns false if the path points to a symlink
   */
  public boolean isDirectory(final String dirPath);

  public boolean isSymlink(final String remotePath);

  public List<String> ls(final String remoteDirPath);

  public void mkdir(final String remoteDirPath);

  public void mkdirs(final String remoteDirPath);

  public String readSymlink(final String remotePath);

  public void receive(final String remoteFilePath, final String localFilePath);

  public void send(final String localFilePath, final String remoteFilePath);

  public void writeSymlink(final String remotePath, final String linkPath);

}
