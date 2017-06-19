package sftp.maven.plugin.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({ ReceiveTest.class, SendTest.class, FailTest.class, RemoveTest.class, NotConnectedTest.class })
public class TestSuite {

}
