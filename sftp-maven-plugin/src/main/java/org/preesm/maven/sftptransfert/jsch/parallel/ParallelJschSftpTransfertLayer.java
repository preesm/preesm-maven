package org.preesm.maven.sftptransfert.jsch.parallel;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.preesm.maven.sftptransfert.TransfertException;
import org.preesm.maven.sftptransfert.jsch.JschSftpTransfertLayer;
import org.preesm.maven.sftptransfert.jsch.sessioninfos.SessionInfos;
import org.preesm.maven.sftptransfert.jsch.transfer.Receive;
import org.preesm.maven.sftptransfert.jsch.transfer.Send;
import org.preesm.maven.sftptransfert.jsch.transfer.Transfer;
import org.preesm.maven.sftptransfert.jsch.transfer.WriteSymLink;

public class ParallelJschSftpTransfertLayer extends JschSftpTransfertLayer {

  private BlockingQueue<Transfer> transfers;
  private CountDownLatch          latch;
  private boolean                 addingTransfers;

  private volatile TransfertException caughtThrowable = null;
  private final int                   transferThreadCount;
  private final ThreadFactory         threadFactory   = new ThreadFactoryBuilder().setNameFormat("transfer-thread-%d")
      .setUncaughtExceptionHandler((thread, throwable) -> {
                                                            if (throwable instanceof TransfertException) {
                                                              this.caughtThrowable = (TransfertException) throwable;
                                                            } else {
                                                              this.caughtThrowable = new TransfertException(thread.toString() + " failed", throwable);
                                                            }
                                                            latchCountDown();
                                                          })
      .build();

  public ParallelJschSftpTransfertLayer(final SessionInfos infos, final int transferThreadCount) {
    super(infos);
    this.transferThreadCount = transferThreadCount;
  }

  @Override
  public final void connect() {
    super.connect();
    launchThreads();
  }

  private void launchThreads() {
    this.latch = new CountDownLatch(transferThreadCount);
    this.transfers = new ArrayBlockingQueue<>(100);
    this.addingTransfers = true;
    final ExecutorService threadPool = Executors.newFixedThreadPool(transferThreadCount, this.threadFactory);
    for (int i = 0; i < transferThreadCount; i++) {
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
