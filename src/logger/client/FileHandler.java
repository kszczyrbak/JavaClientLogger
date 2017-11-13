package logger.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Klasa z narzedziami do obslugi plikow dla klienta.
 * 
 * @author Krzysztof Szczyrbak
 *
 */
public class FileHandler {

	private final static String DIRPATH = "failed";

	/**
	 * Metoda tworzaca nowy plik dla klienta jesli on go jeszcze nie posiada i
	 * zapisuje w nim log, ktory nie zostal prawidlowo wyslany do serwera.
	 * 
	 * @param failedLoggerName
	 *            nazwa loggera, ktorego logi nalezy zapisac
	 * @param messageToSave
	 *            nieudany komunikat loggera do zapisania
	 * 
	 */
	public static void saveFailedLog(String failedLoggerName, String messageToSave) {
		if (!new File(DIRPATH).exists()) {
			if (!new File(DIRPATH).mkdirs()) {
				try {
					throw new Exception("Blad w tworzeniu katalogu failed");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		File failedFile = new File(DIRPATH + "/" + failedLoggerName + ".txt");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(failedFile, true), "UTF-8"));

			writer.append(messageToSave + "\r\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			if (writer != null)
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		writer = null;
	}

	/**
	 * Metoda wczytujaca nieudane logi z pliku i zwracajaca je w array liscie.
	 * 
	 * @param failedLoggerName
	 *            nazwa loggera ktorego polaczenie nie udalo sie
	 * @return ArrayList<String> nieudanych logow
	 */
	public static ArrayList<String> loadFailedLogs(String failedLoggerName) {
		File failedFile = new File(DIRPATH + "/" + failedLoggerName + ".txt");
		if (!failedFile.exists())
			return null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(failedFile), "UTF-8"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		ArrayList<String> list = new ArrayList<String>();
		if (br != null) {
			String line = "";
			try {
				while ((line = br.readLine()) != null) {
					list.add(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		PrintWriter clear = null;
		try {
			clear = new PrintWriter(failedFile, "UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (clear != null) {
			clear.write("");
			clear.close();
		}
		if (!failedFile.delete())
			System.out.println("Failed deleting file : " + failedFile);
		return list;
	}

}
