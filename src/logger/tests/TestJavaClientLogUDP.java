package logger.tests;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import logger.client.JavaClientLog;
import logger.client.JavaClientLog.Protocol;
import logger.client.Logger;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ JavaClientLog.class })
public class TestJavaClientLogUDP {
	private JavaClientLog testLogger;
	private OutputStreamWriter writer;
	private ByteArrayOutputStream os;

	@Before
	public void initializeTestLogger() throws Exception {
		Socket testSocket = Mockito.mock(Socket.class);
		PowerMockito.whenNew(Socket.class).withAnyArguments().thenReturn(testSocket);

		writer = new OutputStreamWriter(os, "UTF-8");
		PowerMockito.whenNew(OutputStreamWriter.class).withAnyArguments().thenReturn(writer);

		BufferedReader br = Mockito.mock(BufferedReader.class);
		PowerMockito.whenNew(BufferedReader.class).withAnyArguments().thenReturn(br);

		testLogger = new JavaClientLog("localhost", 8000, Logger.Log.INFO, "test", Protocol.UDP);
	}

	@Test
	public void testCloseOutput() {
		testLogger.setDone();
		assertTrue(testLogger.closeOutput());
	}

	@Test
	public void testReconnect() {
		assertTrue(testLogger.reconnect());
	}

}
