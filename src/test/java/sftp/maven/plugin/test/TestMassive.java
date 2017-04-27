package sftp.maven.plugin.test;

import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;

public class TestMassive extends AbstractTransfertTestSettings {

  @Test
  public void testSendMassive() throws MojoFailureException {
    AbstractTransfertTestSettings.connect(true);

    AbstractTransfertTestSettings.transfer("send", "/root/testsend/", "/home/koubi/test/complete/");

    AbstractTransfertTestSettings.disconnect();
  }
}
