package org.preesm.maven.sftptransfert.test;

import com.jcraft.jsch.ChannelSftp;

import org.junit.Assert;
import org.junit.Test;
import org.preesm.maven.sftptransfert.TransfertException;
import org.preesm.maven.sftptransfert.jsch.JschSftpTransfertLayer;

public class NotConnectedTest extends AbstractTransfertTestSettings {

  @Test
  public void testNotConnectedSendLink() {
    final JschSftpTransfertLayer build = JschSftpTransfertLayer.build(AbstractTransfertTestSettings.keyInfos, 1);
    build.connect();
    final ChannelSftp mainSftpChannel = build.getMainSftpChannel();
    mainSftpChannel.exit();
    try {
      build.writeSymlink("/tmp/unsued", ".");
      Assert.fail();
    } catch (final TransfertException e) {
      // success
    } finally {
      build.disconnect();
    }
  }

  @Test
  public void testNotConnectedReceiveFile() {
    final JschSftpTransfertLayer build = JschSftpTransfertLayer.build(AbstractTransfertTestSettings.keyInfos, 1);
    build.connect();
    final ChannelSftp mainSftpChannel = build.getMainSftpChannel();
    mainSftpChannel.exit();
    try {
      build.receive("/tmp/unsued", "/tmp/unused");
      Assert.fail();
    } catch (final TransfertException e) {
      // success
    } finally {
      build.disconnect();
    }
  }

  @Test
  public void testNotConnectedSendFile() {
    final JschSftpTransfertLayer build = JschSftpTransfertLayer.build(AbstractTransfertTestSettings.keyInfos, 1);
    build.connect();
    final ChannelSftp mainSftpChannel = build.getMainSftpChannel();
    mainSftpChannel.exit();
    try {
      build.send("/tmp/unsued", "/tmp/unused");
      Assert.fail();
    } catch (final TransfertException e) {
      // success
    } finally {
      build.disconnect();
    }
  }
}
