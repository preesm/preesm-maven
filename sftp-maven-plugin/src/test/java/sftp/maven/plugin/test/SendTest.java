package sftp.maven.plugin.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.Test;

public class SendTest extends AbstractTransfertTestSettings {

  @Test
  public void testConnect() throws MojoFailureException {
    AbstractTransfertTestSettings.connect();
    AbstractTransfertTestSettings.disconnect();
    this.remotePath = null;
  }

  private String remotePath;

  @After
  public void tearDown() throws Exception {
    AbstractTransfertTestSettings.connect();

    AbstractTransfertTestSettings.sftpTransfert.remove(this.remotePath);
    AbstractTransfertTestSettings.disconnect();
  }

  @Test
  public void testSendLink() throws MojoFailureException, IOException {
    final Path createTempLink = Files.createTempFile("sftpplugin", "link");
    Files.delete(createTempLink);
    Files.createSymbolicLink(createTempLink, Paths.get("/"));
    this.remotePath = "/tmp/tmpLink" + createTempLink.getFileName().toString();

    AbstractTransfertTestSettings.connect();
    AbstractTransfertTestSettings.transfer("send", this.remotePath, createTempLink.toAbsolutePath().toString());
    AbstractTransfertTestSettings.disconnect();

    Files.delete(createTempLink);
  }

  @Test
  public void testOverwriteLink() throws MojoFailureException, IOException {
    final Path createTempLink = Files.createTempFile("sftpplugin", "link");
    Files.delete(createTempLink);
    Files.createSymbolicLink(createTempLink, Paths.get("/"));
    this.remotePath = "/tmp/tmpLink" + createTempLink.getFileName().toString();

    AbstractTransfertTestSettings.connect();
    AbstractTransfertTestSettings.transfer("send", this.remotePath, createTempLink.toAbsolutePath().toString());
    AbstractTransfertTestSettings.transfer("send", this.remotePath, createTempLink.toAbsolutePath().toString());
    AbstractTransfertTestSettings.disconnect();

    Files.delete(createTempLink);
  }

  @Test

  public void testSendFile() throws MojoFailureException, IOException {
    final Path createTempFile = Files.createTempFile("sftpplugin", "file");
    this.remotePath = "/tmp/tmpFile" + createTempFile.getFileName().toString();

    System.out.println("#####");
    System.out.println("#####");
    AbstractTransfertTestSettings.connect();
    AbstractTransfertTestSettings.transfer("send", this.remotePath, createTempFile.toAbsolutePath().toString());
    AbstractTransfertTestSettings.disconnect();
    System.out.println("#####");
    System.out.println("#####");
    Files.delete(createTempFile);
  }

  @Test
  public void testSendEmptyDir() throws MojoFailureException, IOException {
    final Path createTempDir = Files.createTempDirectory("sftpplugin");
    this.remotePath = "/tmp/tmpDir" + createTempDir.getFileName().toString();

    AbstractTransfertTestSettings.connect();
    AbstractTransfertTestSettings.transfer("send", this.remotePath, createTempDir.toAbsolutePath().toString());
    AbstractTransfertTestSettings.disconnect();

    Files.delete(createTempDir);
  }

  @Test
  public void testSendFilledDir() throws MojoFailureException, IOException {
    final Path createTempDir = Files.createTempDirectory("sftpplugin");
    Files.createFile(Paths.get(createTempDir.toString() + "/subfile1"));
    Files.createSymbolicLink(Paths.get(createTempDir.toString() + "/sublink"), Paths.get("subfile1"));
    final Path subDir = Files.createTempDirectory(createTempDir, "subdir");

    Files.createSymbolicLink(Paths.get(createTempDir.toString() + "/sublinkDir"), subDir.getFileName());

    Files.createFile(Paths.get(subDir.toString() + "/subfile2"));
    Files.createSymbolicLink(Paths.get(subDir.toString() + "/sublink"), Paths.get("subfile2"));

    this.remotePath = "/tmp/tmpDir" + createTempDir.getFileName().toString();

    AbstractTransfertTestSettings.connect();
    AbstractTransfertTestSettings.transfer("send", this.remotePath, createTempDir.toAbsolutePath().toString());
    AbstractTransfertTestSettings.disconnect();

    final boolean exists = Files.exists(createTempDir);
    System.out.println(exists);
    FileUtils.deleteDirectory(createTempDir.toFile());
  }

  @Test
  public void testSendLinkParallel() throws MojoFailureException, IOException {
    final Path createTempLink = Files.createTempFile("sftpplugin", "link");
    Files.delete(createTempLink);
    Files.createSymbolicLink(createTempLink, Paths.get("/"));
    this.remotePath = "/tmp/tmpLink" + createTempLink.getFileName().toString();

    AbstractTransfertTestSettings.connect(20);
    AbstractTransfertTestSettings.transfer("send", this.remotePath, createTempLink.toAbsolutePath().toString());
    AbstractTransfertTestSettings.disconnect();

    Files.delete(createTempLink);
  }

  @Test

  public void testSendFileParallel() throws MojoFailureException, IOException {
    final Path createTempFile = Files.createTempFile("sftpplugin", "file");
    this.remotePath = "/tmp/tmpFile" + createTempFile.getFileName().toString();

    System.out.println("#####");
    System.out.println("#####");
    AbstractTransfertTestSettings.connect(20);
    AbstractTransfertTestSettings.transfer("send", this.remotePath, createTempFile.toAbsolutePath().toString());
    AbstractTransfertTestSettings.disconnect();
    System.out.println("#####");
    System.out.println("#####");
    Files.delete(createTempFile);
  }

  @Test
  public void testSendEmptyDirParallel() throws MojoFailureException, IOException {
    final Path createTempDir = Files.createTempDirectory("sftpplugin");
    this.remotePath = "/tmp/tmpDir" + createTempDir.getFileName().toString();

    AbstractTransfertTestSettings.connect(20);
    AbstractTransfertTestSettings.transfer("send", this.remotePath, createTempDir.toAbsolutePath().toString());
    AbstractTransfertTestSettings.disconnect();

    Files.delete(createTempDir);
  }

  @Test
  public void testSendFilledDirParallel() throws MojoFailureException, IOException {
    final Path createTempDir = Files.createTempDirectory("sftpplugin");
    Files.createFile(Paths.get(createTempDir.toString() + "/subfile1"));
    Files.createSymbolicLink(Paths.get(createTempDir.toString() + "/sublink"), Paths.get("subfile1"));
    final Path subDir = Files.createTempDirectory(createTempDir, "subdir");

    Files.createSymbolicLink(Paths.get(createTempDir.toString() + "/sublinkDir"), subDir.getFileName());

    Files.createFile(Paths.get(subDir.toString() + "/subfile2"));
    Files.createSymbolicLink(Paths.get(subDir.toString() + "/sublink"), Paths.get("subfile2"));

    this.remotePath = "/tmp/tmpDir" + createTempDir.getFileName().toString();

    AbstractTransfertTestSettings.connect(20);
    AbstractTransfertTestSettings.transfer("send", this.remotePath, createTempDir.toAbsolutePath().toString());
    AbstractTransfertTestSettings.disconnect();

    final boolean exists = Files.exists(createTempDir);
    System.out.println(exists);
    FileUtils.deleteDirectory(createTempDir.toFile());
  }

}
