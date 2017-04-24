package org.ietr.maven.sftptransfert;

import java.util.List;

public interface ISftpTransfertLayer {
  public void connectUsingPassword(final String host, final int port, final String user, final String password, final boolean strictHostKeyChecking);

  public void connectUsingKey(final String host, final int port, final String user, final String keyPath, final boolean strictHostKeyChecking);

  public void connectUsingKeyWithPassPhrase(final String host, final int port, final String user, final String keyPath, final String passPhrase,
      final boolean strictHostKeyChecking);

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

}
