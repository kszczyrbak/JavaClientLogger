package logger.tests;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;

import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;

import org.junit.Test;

import logger.client.FileHandler;

public class TestFileHandler {

	@Test
	public void testSaveFailedLog() throws IOException {
		File testFile = new File("failed/test.txt");
		if (testFile.exists())
			assertTrue(testFile.delete());
		FileHandler.saveFailedLog("test", "test");
		assertTrue(testFile.exists());
		BufferedReader br = null;
		String test = "";

		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(testFile), "UTF-8"));
			test = br.readLine();
		} finally {
			if (br != null)
				br.close();
		}

		assertEquals("test", test);
		br.close();
		if (!testFile.delete())
			System.out.print("");

	}

	@Test
	public void testLoadFailedLogs() throws IOException {
		File testFile = new File("failed/test.txt");
		FileHandler.saveFailedLog("test", "test1");
		ArrayList<String> test = new ArrayList<String>();
		test.add("test1");
		FileHandler.saveFailedLog("test", "test2");
		test.add("test2");
		ArrayList<String> test2 = FileHandler.loadFailedLogs("test");
		assertEquals(test, test2);
		if (!testFile.delete()) {
			System.out.print("");
		}
	}
}
