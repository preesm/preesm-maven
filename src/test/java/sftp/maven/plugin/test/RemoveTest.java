package sftp.maven.plugin.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RemoveTest extends AbstractTransfertTestSettings {

  private String remoteFolder;

  @Before
  public void init() throws Exception {

    AbstractTransfertTestSettings.connect();
    final Path createTempDir = Files.createTempDirectory("sftpplugin");
    Files.createFile(Paths.get(createTempDir.toString() + "/subfile1"));
    Files.createSymbolicLink(Paths.get(createTempDir.toString() + "/sublink"), Paths.get("subfile1"));
    final Path subDir = Files.createTempDirectory(createTempDir, "subdir");
    Files.createDirectory(Paths.get(createTempDir.toString() + "/subdir2"));

    Files.createSymbolicLink(Paths.get(createTempDir.toString() + "/sublinkDir"), subDir.getFileName());

    Files.createFile(Paths.get(subDir.toString() + "/subfile2"));
    Files.createSymbolicLink(Paths.get(subDir.toString() + "/sublink"), Paths.get("subfile2"));

    this.remoteFolder = "/tmp/tmpDir" + createTempDir.getFileName().toString();

    AbstractTransfertTestSettings.transfer("send", this.remoteFolder, createTempDir.toAbsolutePath().toString());

    AbstractTransfertTestSettings.disconnect();

    final boolean exists = Files.exists(createTempDir);
    System.out.println(exists);
    FileUtils.deleteDirectory(createTempDir.toFile());
  }

  @After
  public void tearDown() throws Exception {
    AbstractTransfertTestSettings.connect();
    AbstractTransfertTestSettings.sftpTransfert.remove(this.remoteFolder);
    AbstractTransfertTestSettings.disconnect();
  }

  @Test
  public void testRemoveLink() throws MojoFailureException, IOException {
    final Path createTempLink = Files.createTempFile("sftpplugin", "link");
    AbstractTransfertTestSettings.connect();
    AbstractTransfertTestSettings.sftpTransfert.remove(this.remoteFolder + "/sublink");
    AbstractTransfertTestSettings.disconnect();
    Files.delete(createTempLink);
  }

  @Test
  public void testRemoveLinkParallel() throws MojoFailureException, IOException {
    final Path createTempLink = Files.createTempFile("sftpplugin", "link");
    AbstractTransfertTestSettings.connect(true);
    AbstractTransfertTestSettings.sftpTransfert.remove(this.remoteFolder + "/sublink");
    AbstractTransfertTestSettings.disconnect();
    Files.delete(createTempLink);
  }

  // @Test
  // public void testReceiveDirLink() throws MojoFailureException, IOException {
  // final Path createTempDirLink = Files.createTempFile("sftpplugin", "dirLink");
  // AbstractTransfertTestSettings.connect();
  // AbstractTransfertTestSettings.transfer("receive", this.remoteFolder + "/sublinkDir", createTempDirLink.toAbsolutePath().toString());
  // AbstractTransfertTestSettings.disconnect();
  // Files.delete(createTempDirLink);
  // }
  //
  // @Test
  // public void testReceiveFile() throws MojoFailureException, IOException {
  // final Path createTempFile = Files.createTempFile("sftpplugin", "file");
  // AbstractTransfertTestSettings.connect();
  // AbstractTransfertTestSettings.transfer("receive", this.remoteFolder + "/subfile1", createTempFile.toAbsolutePath().toString());
  // AbstractTransfertTestSettings.disconnect();
  // Files.delete(createTempFile);
  // }
  //
  // @Test
  // public void testReceiveEmptyDir() throws MojoFailureException, IOException {
  // final Path createTempDir = Files.createTempDirectory("sftpplugin");
  // AbstractTransfertTestSettings.connect();
  // AbstractTransfertTestSettings.transfer("receive", this.remoteFolder + "/subdir2", createTempDir.toAbsolutePath().toString());
  // AbstractTransfertTestSettings.disconnect();
  // Files.delete(createTempDir);
  // }
  //
  // @Test
  // public void testReceiveFilledDir() throws MojoFailureException, IOException {
  // final Path createTempDir = Files.createTempDirectory("sftpplugin");
  // AbstractTransfertTestSettings.connect();
  // AbstractTransfertTestSettings.transfer("receive", this.remoteFolder, createTempDir.toAbsolutePath().toString());
  // AbstractTransfertTestSettings.disconnect();
  // FileUtils.deleteDirectory(createTempDir.toFile());
  // }

}
