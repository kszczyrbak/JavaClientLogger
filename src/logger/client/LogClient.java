package logger.client;

import java.io.*;

import logger.client.JavaClientLog.Protocol;
import logger.client.Logger.Log;

/**
 * Prosta klasa testujaca sieciowa implementacje loggera na wielu watkach.
 * 
 * @author Krzysztof Szczyrbak
 *
 */
public class LogClient implements Runnable {
	private Logger logger;
	private Logger.Log logLevel;
	private String hostname;
	private int port;
	private String name;
	private String filePath;
	boolean file = false;

	// private int test;

	LogClient(String hostname, int port, Logger.Log logLevel, String name, int test) {
		this.hostname = hostname;
		this.port = port;
		this.logLevel = logLevel;
		this.name = name;
		// this.test = test;

	}

	LogClient(String filePath, String name) {
		this.name = name;
		this.filePath = filePath;
		file = true;
	}

	public void run() {

		if (file) {
			try {
				String dirpath = (new File(".")).getAbsolutePath();
				logger = new JavaClientLog(new File(dirpath + "/" + filePath), name);
				logger.log(Log.DEBUG, "Hello, {}{} {} {}", "World", "World", "World", "World");
				logger.log(Logger.Log.WARNING, "Hello, World");
				logger.log(Logger.Log.INFO, "Welcome Home");
				logger.log(Logger.Log.WARNING, "Hello, World");
				Thread.sleep(1000);
				logger.log(Logger.Log.INFO, "Welcome Home");
				logger.log(Logger.Log.WARNING, "Hello, World");
				logger.log(Logger.Log.INFO, "Welcome Home");
				Thread.sleep(1000);
				Thread.sleep(1000);
				Thread.sleep(1000);
				Thread.sleep(1000);
				logger.log(Logger.Log.WARNING, "Hello, World");
				logger.log(Logger.Log.INFO, "Welcome Home");
				logger.log(Logger.Log.WARNING, "Hello, World");
				logger.log(Logger.Log.INFO, "Welcome Home");
				logger.log(Logger.Log.WARNING, "Hello, World");
				logger.log(Logger.Log.INFO, "Welcome Home");
				logger.log(Logger.Log.WARNING, "Hello, World");
				Thread.sleep(1000);
				logger.log(Logger.Log.INFO, "Welcome Home");
				logger.log(Logger.Log.WARNING, "Hello, World");
				logger.log(Logger.Log.INFO, "Welcome Home");
				Thread.sleep(1000);
				logger.log(Logger.Log.WARNING, "Hello, World");
				logger.log(Logger.Log.INFO, "Welcome Home");
				Thread.sleep(1000);
				logger.log(Logger.Log.WARNING, "Hello, World");
				logger.log(Logger.Log.INFO, "Welcome Home");
				Thread.sleep(1000);
				logger.log(Logger.Log.ERROR, "SLEEP");

				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			logger.log(Logger.Log.WARNING, "END");
			logger.stop();
		} else {
			logger = new JavaClientLog(hostname, port, Log.DEBUG, name, Protocol.UDP);

			try {
				logger.log(Log.DEBUG, "Hello, {}{} {} {}", "World", "World", "World", "World");
				logger.log(Logger.Log.WARNING, "Hello, World");
				logger.log(Logger.Log.INFO, "Welcome Home");
				Thread.sleep(1000);
				logger.log(Logger.Log.ERROR, "SLEEP");
				Thread.sleep(10000);
				logger.log(Logger.Log.WARNING, "END");

				logger.stop();

				logger = new JavaClientLog(hostname, port, logLevel, name, Protocol.UDP);
				logger.log(Logger.Log.WARNING, "Hello, World");
				logger.log(Logger.Log.INFO, "Welcome Home");
				Thread.sleep(1000);
				logger.log(Logger.Log.ERROR, "SLEEP");
				Thread.sleep(10000);
				logger.log(Logger.Log.WARNING, "END");

				logger.stop();
				return;
			} catch (InterruptedException e) {
			}
		}
	}

	public static void main(String args[]) throws IOException, InterruptedException {
		for (int i = 0; i < 3; i++) {
			if (args.length == 1) {
				new Thread(new LogClient(args[0], "client")).start();
			} else if (args.length == 2)
				new Thread(new LogClient(args[0], Integer.parseInt(args[1]), Logger.Log.INFO, "client", i)).start();
			else {
				System.out.println("Zle argumenty. Domyslne ustawienia: localhost/8000");
				new Thread(new LogClient("localhost", 8000, Logger.Log.INFO, "client" + i, i)).start();
			}

			Thread.sleep(100);
		}
	}
}