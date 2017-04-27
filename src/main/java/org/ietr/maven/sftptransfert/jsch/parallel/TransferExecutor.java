package org.ietr.maven.sftptransfert.jsch.parallel;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.ietr.maven.sftptransfert.TransfertException;
import org.ietr.maven.sftptransfert.jsch.JschSftpTransfertLayer;
import org.ietr.maven.sftptransfert.transfer.Transfer;

public final class TransferExecutor implements Runnable {

  private final int                            id;
  private final ParallelJschSftpTransfertLayer transferLayer;

  public TransferExecutor(final int i, final ParallelJschSftpTransfertLayer transferLayer) {
    this.id = i;
    this.transferLayer = transferLayer;
  }

  @Override
  public void run() {
    final BlockingQueue<Transfer> transfers = this.transferLayer.getTransfers();
    final Session session = this.transferLayer.getInfos().openSession(JschSftpTransfertLayer.getDefaultJsch());
    ChannelSftp channel = null;
    try {
      channel = (ChannelSftp) session.openChannel("sftp");
      channel.connect();

      while (this.transferLayer.isAddingTransfers() || !transfers.isEmpty()) {
        final Transfer transfer = transfers.poll(50, TimeUnit.MILLISECONDS);
        if (transfer != null) {
          transfer.process(channel);
        }
      }
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new TransfertException("Transfer thread " + this.id + " interrupted: " + e.getMessage(), e);
    } catch (final JSchException e) {
      throw new TransfertException("Could not open new sftp channel: " + e.getMessage(), e);
    } finally {
      if (channel != null) {
        channel.exit();
        channel.disconnect();
      }
      session.disconnect();
    }
    this.transferLayer.latchCountDown();
  }

}
