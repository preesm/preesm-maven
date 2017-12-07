package org.preesm.maven.sftptransfert.test;

import com.jcraft.jsch.JSchException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.preesm.maven.sftptransfert.SftpConnection;
import org.preesm.maven.sftptransfert.TransfertException;
import org.preesm.maven.sftptransfert.jsch.JschSftpTransfertLayer;
import org.preesm.maven.sftptransfert.jsch.sessioninfos.PrivateKeySessionInfos;

@Ignore
public class FailTest extends AbstractTransfertTestSettings {

  @Test
  public void testAlreadyConnected() throws MojoFailureException {
    final JschSftpTransfertLayer build = JschSftpTransfertLayer.build(AbstractTransfertTestSettings.keyInfos, 1);
    build.connect();
    try {
      build.connect();
      Assert.fail();
    } catch (final TransfertException e) {
      // success
    } finally {
      build.disconnect();
    }
  }

  @Test
  public void testAlreadyConnectedParallel() throws MojoFailureException {
    final JschSftpTransfertLayer build = JschSftpTransfertLayer.build(AbstractTransfertTestSettings.keyInfos, 20);
    build.connect();
    try {
      build.connect();
      Assert.fail();
    } catch (final TransfertException e) {
      // success
    } finally {
      build.disconnect();
    }
  }

  @Test
  public void testReceiveFail() throws IOException, MojoFailureException {
    final Path createTempFile = Files.createTempFile("sftpplugin", "file");
    AbstractTransfertTestSettings.connect();
    try {
      AbstractTransfertTestSettings.transfer("receive", "/nonexistingdir/nonexistingfile", "/tmp/unused");
      Assert.fail();
    } catch (final MojoFailureException e) {
      // success
    } finally {
      AbstractTransfertTestSettings.disconnect();
      Files.delete(createTempFile);
    }
  }

  @Test
  public void testSendFail() throws IOException, MojoFailureException {
    final Path createTempFile = Files.createTempFile("sftpplugin", "file");
    AbstractTransfertTestSettings.connect();
    try {
      AbstractTransfertTestSettings.transfer("send", "/tmp/unused", "/nonexistingdir/nonexistingfile");
      Assert.fail();
    } catch (final MojoFailureException e) {
      // success
    } finally {
      AbstractTransfertTestSettings.disconnect();
      Files.delete(createTempFile);
    }
  }

  @Test
  public void testReceiveFailParallel() throws IOException, MojoFailureException {
    final Path createTempFile = Files.createTempFile("sftpplugin", "file");
    AbstractTransfertTestSettings.connect(20);
    try {
      AbstractTransfertTestSettings.transfer("receive", "/nonexistingdir/nonexistingfile", "/tmp/unused");
      Assert.fail();
    } catch (final MojoFailureException e) {
      // success
    } finally {
      AbstractTransfertTestSettings.disconnect();
      Files.delete(createTempFile);
    }
  }

  @Test
  public void testSendFailParallel() throws IOException, MojoFailureException {
    final Path createTempFile = Files.createTempFile("sftpplugin", "file");
    AbstractTransfertTestSettings.connect(20);
    try {
      AbstractTransfertTestSettings.transfer("send", "/tmp/unused", "/nonexistingdir/nonexistingfile");
      AbstractTransfertTestSettings.disconnect();
      Assert.fail();
    } catch (final MojoFailureException e) {
      // success
    } finally {
      Files.delete(createTempFile);
    }
  }

  @Test
  public void testRemoveFail() throws MojoFailureException, IOException {
    AbstractTransfertTestSettings.connect();
    AbstractTransfertTestSettings.sftpTransfert.remove("/nonexistingdir/nonexistingfile");
    AbstractTransfertTestSettings.disconnect();
  }

  @Test
  public void testNotKeyFile() {
    final PrivateKeySessionInfos t = new PrivateKeySessionInfos(Settings.sftpHost, Settings.sftpPort, Settings.sftpUser, Settings.strictHostKeyChecking,
        "/tmp/notExisting");
    try {
      new SftpConnection(t, 1);
      Assert.fail();
    } catch (final TransfertException e) {
      Assert.assertTrue(e.getCause() instanceof FileNotFoundException);
    }
  }

  @Test
  public void testWrongPassPhrase() throws IOException {
    final Path createTempFile = Files.createTempFile("sftpplugin", ".rsa");
    final PrivateKeySessionInfos t = new PrivateKeySessionInfos(Settings.sftpHost, Settings.sftpPort, Settings.sftpUser, Settings.strictHostKeyChecking,
        createTempFile.toAbsolutePath().toString(), "alsowrongpassphrase");
    try {
      new SftpConnection(t, 1);
      Assert.fail();
    } catch (final TransfertException e) {
      Assert.assertTrue(e.getCause() instanceof JSchException);
    }
    Files.delete(createTempFile);
  }
}
