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
public class TestJavaClientLogTCP {
	private JavaClientLog testLogger;
	private OutputStreamWriter writer;
	private ByteArrayOutputStream os;
	private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	private String getDate() {
		Date date = new Date();
		return dateFormat.format(date);
	}

	@Before
	public void initializeTestLogger() throws Exception {
		Socket testSocket = Mockito.mock(Socket.class);
		PowerMockito.whenNew(Socket.class).withAnyArguments().thenReturn(testSocket);

		os = new ByteArrayOutputStream();
		Mockito.when(testSocket.getOutputStream()).thenReturn(os);

		writer = new OutputStreamWriter(os, "UTF-8");
		PowerMockito.whenNew(OutputStreamWriter.class).withAnyArguments().thenReturn(writer);

		InputStream is = Mockito.mock(InputStream.class);
		Mockito.when(testSocket.getInputStream()).thenReturn(is);

		InputStreamReader reader = Mockito.mock(InputStreamReader.class);
		PowerMockito.whenNew(InputStreamReader.class).withAnyArguments().thenReturn(reader);

		BufferedReader br = Mockito.mock(BufferedReader.class);
		PowerMockito.whenNew(BufferedReader.class).withAnyArguments().thenReturn(br);

		testLogger = new JavaClientLog("localhost", 8000, Logger.Log.INFO, "test", Protocol.TCP);
	}

	@Test
	public void testInfo() throws IOException {
		testLogger.info("test");
		writer.flush();
		
		String message = "test" + " " + getDate() + " INFO " + Thread.currentThread().getId() + " " + "test" + "\n";;
		byte[] b = os.toByteArray();

		String str2 = new String(b, "UTF-8");
		assertEquals(message, str2);
	}

	@Test
	public void testWarning() throws IOException {
		testLogger.warn("test");
		writer.flush();
		String message = "test" + " " + getDate() + " WARNING " + Thread.currentThread().getId() + " " + "test" + "\n";;
		byte[] b = os.toByteArray();

		String str2 = new String(b, "UTF-8");
		assertEquals(message, str2);

	}

	@Test
	public void testError() throws IOException {
		testLogger.err("test");

		writer.flush();
		String message = "test" + " " + getDate() + " ERROR " + Thread.currentThread().getId() + " " + "test" + "\n";;
		byte[] b = os.toByteArray();

		String str2 = new String(b, "UTF-8");
		assertEquals(message, str2);
	}

	@Test
	public void testLog() throws IOException {
		testLogger.log(Logger.Log.INFO, "test");
		String message = "test" + " " + getDate() + " INFO " + Thread.currentThread().getId() + " " + "test" + "\n";
		writer.flush();
		assertEquals(message, new String(os.toByteArray(), "UTF-8"));
		File testFailedFile = new File("failed/test.txt");
		if (testFailedFile.delete())
			return;

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
