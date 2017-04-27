package org.ietr.maven.sftptransfert.jsch.parallel;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.ietr.maven.sftptransfert.TransfertException;
import org.ietr.maven.sftptransfert.jsch.JschSftpTransfertLayer;
import org.ietr.maven.sftptransfert.sessioninfos.SessionInfos;
import org.ietr.maven.sftptransfert.transfer.Receive;
import org.ietr.maven.sftptransfert.transfer.Send;
import org.ietr.maven.sftptransfert.transfer.Transfer;

public class ParallelJschSftpTransfertLayer extends JschSftpTransfertLayer {

  private static final int THREAD_POOL_SIZE = 4;

  private BlockingQueue<Transfer> transfers;
  private CountDownLatch          latch;
  private boolean                 addingTransfers;

  public ParallelJschSftpTransfertLayer(final SessionInfos infos) {
    super(infos);
  }

  @Override
  public final void connect() {
    super.connect();
    launchThreads();
  }

  private void launchThreads() {
    this.latch = new CountDownLatch(ParallelJschSftpTransfertLayer.THREAD_POOL_SIZE);
    this.transfers = new ArrayBlockingQueue<>(ParallelJschSftpTransfertLayer.THREAD_POOL_SIZE * 10);
    this.addingTransfers = true;
    final ExecutorService threadPool = Executors.newFixedThreadPool(ParallelJschSftpTransfertLayer.THREAD_POOL_SIZE);
    for (int i = 0; i < ParallelJschSftpTransfertLayer.THREAD_POOL_SIZE; i++) {
      threadPool.execute(new TransferExecutor(i, this));
    }
  }

  public synchronized void latchCountDown() {
    this.latch.countDown();
  }

  @Override
  public void disconnect() {
    this.addingTransfers = false;
    try {
      this.latch.await();
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new TransfertException("Awaiting threads failed: " + e.getMessage(), e);
    }
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

  public boolean isAddingTransfers() {
    return this.addingTransfers;
  }

  public BlockingQueue<Transfer> getTransfers() {
    return this.transfers;
  }

  public CountDownLatch getLatch() {
    return this.latch;
  }

}
