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
  public void testConnect() {
    AbstractTransfertTestSettings.connect();
    AbstractTransfertTestSettings.disconnect();
    remotePath = null;
  }

  private String remotePath;

  @After
  public void tearDown() throws Exception {
    AbstractTransfertTestSettings.connect();

    AbstractTransfertTestSettings.sftpTransfert.remove(remotePath);
    AbstractTransfertTestSettings.disconnect();
  }

  @Test
  public void testSendLink() throws MojoFailureException, IOException {
    final Path createTempLink = Files.createTempFile("sftpplugin", "link");
    Files.delete(createTempLink);
    Files.createSymbolicLink(createTempLink, Paths.get("/"));
    remotePath = "/tmp/tmpLink" + createTempLink.getFileName().toString();

    AbstractTransfertTestSettings.connect();
    AbstractTransfertTestSettings.transfer("send", remotePath, createTempLink.toAbsolutePath().toString());
    AbstractTransfertTestSettings.disconnect();

    Files.delete(createTempLink);
  }

  @Test

  public void testSendFile() throws MojoFailureException, IOException {
    final Path createTempFile = Files.createTempFile("sftpplugin", "file");
    remotePath = "/tmp/tmpFile" + createTempFile.getFileName().toString();

    AbstractTransfertTestSettings.connect();
    AbstractTransfertTestSettings.transfer("send", remotePath, createTempFile.toAbsolutePath().toString());
    AbstractTransfertTestSettings.disconnect();

    Files.delete(createTempFile);
  }

  @Test
  public void testSendEmptyDir() throws MojoFailureException, IOException {
    final Path createTempDir = Files.createTempDirectory("sftpplugin");
    remotePath = "/tmp/tmpDir" + createTempDir.getFileName().toString();

    AbstractTransfertTestSettings.connect();
    AbstractTransfertTestSettings.transfer("send", remotePath, createTempDir.toAbsolutePath().toString());
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

    remotePath = "/tmp/tmpDir" + createTempDir.getFileName().toString();

    AbstractTransfertTestSettings.connect();
    AbstractTransfertTestSettings.transfer("send", remotePath, createTempDir.toAbsolutePath().toString());
    AbstractTransfertTestSettings.disconnect();

    final boolean exists = Files.exists(createTempDir);
    System.out.println(exists);
    FileUtils.deleteDirectory(createTempDir.toFile());
  }

}
