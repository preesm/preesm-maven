package org.ietr.maven.sftptransfert.parallel;

import java.util.concurrent.ArrayBlockingQueue;
import org.ietr.maven.sftptransfert.JschSftpTransfertLayer;
import org.ietr.maven.sftptransfert.TransfertException;
import org.ietr.maven.sftptransfert.sessioninfos.SessionInfos;

public class ParallelJschSftpTransfertLayer extends JschSftpTransfertLayer {

  private final ArrayBlockingQueue<Transfer> transfers = new ArrayBlockingQueue<>(8 * 100);

  @Override
  public final void connect(final SessionInfos infos) {
    super.connect(infos);

  }

  @Override
  public void disconnect() {
    super.disconnect();
  }

  @Override
  public void receive(final String remoteFilePath, final String localFilePath) {
    try {
      this.transfers.put(new Receive(localFilePath, remoteFilePath));
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new TransfertException("Receive failed : " + e.getMessage(), e);
    }
  }

  @Override
  public void send(final String localFilePath, final String remoteFilePath) {
    try {
      this.transfers.put(new Send(localFilePath, remoteFilePath));
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new TransfertException("Receive failed : " + e.getMessage(), e);
    }
  }
}
