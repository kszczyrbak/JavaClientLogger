package logger.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ TestFileHandler.class, TestJavaClientLogTCP.class, TestJavaClientLogUDP.class })
public class TestClientSuite {
}