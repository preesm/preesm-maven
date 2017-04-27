package org.ietr.maven.sftptransfert.jsch.parallel;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.ietr.maven.sftptransfert.TransfertException;
import org.ietr.maven.sftptransfert.jsch.JschSftpTransfertLayer;
import org.ietr.maven.sftptransfert.sessioninfos.SessionInfos;
import org.ietr.maven.sftptransfert.transfer.Receive;
import org.ietr.maven.sftptransfert.transfer.Send;
import org.ietr.maven.sftptransfert.transfer.Transfer;
import org.ietr.maven.sftptransfert.transfer.WriteSymLink;

public class ParallelJschSftpTransfertLayer extends JschSftpTransfertLayer {

  private static final int THREAD_POOL_SIZE = 4;

  private BlockingQueue<Transfer> transfers;
  private CountDownLatch          latch;
  private boolean                 addingTransfers;

  private volatile TransfertException caughtThrowable = null;
  private final ThreadFactory         threadFactory   = new ThreadFactoryBuilder().setNameFormat("transfer-thread-%d")
      .setUncaughtExceptionHandler((thread, throwable) -> {
                                                            if (throwable instanceof TransfertException) {
                                                              this.caughtThrowable = (TransfertException) throwable;
                                                            } else {
                                                              this.caughtThrowable = new TransfertException(thread.toString() + " failed", throwable);
                                                            }
                                                            this.latchCountDown();
                                                          })
      .build();

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
    final ExecutorService threadPool = Executors.newFixedThreadPool(ParallelJschSftpTransfertLayer.THREAD_POOL_SIZE, this.threadFactory);
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
      getLatch().await();
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new TransfertException("Awaiting threads failed: " + e.getMessage(), e);
    }
    if (this.caughtThrowable != null) {
      throw this.caughtThrowable;
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
  public void writeSymlink(final String remotePath, final String linkPath) {
    try {
      this.transfers.put(new WriteSymLink(linkPath, remotePath));
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new TransfertException("Write symlink failed : " + e.getMessage(), e);
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
