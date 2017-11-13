package logger.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

/**
 * Implementacja interfejsu logger z wykorzystaniem socketow aby wysylal logi do
 * serwera przez siec.
 * 
 * @author Krzysztof Szczyrbak
 *
 */

public class JavaClientLog implements Logger {
	private Properties config;
	private Socket outputSocket;
	private DatagramSocket udpSocket;
	private String name;
	private byte[] sendData;

	private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	private Log logLevel;

	private OutputStreamWriter output;
	private String hostname;
	private int port;
	private SimpleListener listener;
	private boolean isDone = false;

	/**
	 * Reprezentuje typ protokolu internetowego. TCP lub UDP
	 * 
	 * @author Chris
	 *
	 */
	public enum Protocol {
		TCP, UDP;
	}

	private Protocol protocol;

	/**
	 * Metoda zwracajaca dzisiejsza date w wybranym formacie.
	 * 
	 * @return String terazniejsza data
	 */
	private String getDate() {
		Date date = new Date();
		return dateFormat.format(date);
	}

	public JavaClientLog(String hostname, int port, Log logLevel, String name, Protocol protocol) {
		this.hostname = hostname;
		this.port = port;
		this.logLevel = logLevel;
		this.name = name;

		this.protocol = protocol;

		if (protocol == Protocol.UDP)
			initiateUDPConnection();
		else
			initiateTCPConnection();

		sendFailedLogs();
	}

	public JavaClientLog(File propertiesFile, String name) {

		this.name = name;

		try {
			config = getConfigurationFromFile(propertiesFile);
		} catch (FileNotFoundException e) {
			System.out.println("Configuration file not found. Loading default configuration");
			config = loadDefaultConfiguration();
		} catch (IOException e) {
			e.printStackTrace();
		}

		logLevel = getLogLevelFromConfig();

		if (config.getProperty("protocol").equals("UDP"))
			protocol = Protocol.UDP;
		else if (config.getProperty("protocol").equals("TCP"))
			protocol = Protocol.TCP;

		hostname = config.getProperty("host", "localhost");
		port = Integer.parseInt(config.getProperty("port", "8000"));

		if (protocol == Protocol.UDP)
			initiateUDPConnection();
		else
			initiateTCPConnection();

		sendFailedLogs();
	}

	private void initiateTCPConnection() {
		System.out.println("TCP");
		try {
			outputSocket = new Socket(hostname, port);
		} catch (IOException e1) {
			reconnect();
		}

		try {
			if (outputSocket != null)
				output = new OutputStreamWriter(outputSocket.getOutputStream(), "UTF-8");
		} catch (IOException e) {
			reconnect();
		}

		listener = null;

	}

	private void initiateUDPConnection() {
		System.out.println("UDP");
		try {
			udpSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		initiateListener();
		if (config != null) {
			String dataCap = config.getProperty("sendDataLength", "1024");
			sendData = new byte[Integer.parseInt(dataCap)];
		}
	}

	private Log getLogLevelFromConfig() {
		String level = config.getProperty("level");
		if (level.equals("INFO"))
			return Log.INFO;
		else if (level.equals("WARNING"))
			return Log.WARNING;
		else if (level.equals("ERROR"))
			return Log.ERROR;
		else {
			System.out.println("LogLevel not specified correctly. Using default level INFO");
			return Log.INFO;
		}
	}

	private Properties loadDefaultConfiguration() {
		Properties defaultConfig = new Properties();
		defaultConfig.setProperty("hostname", "localhost");
		defaultConfig.setProperty("port", "8000");
		defaultConfig.setProperty("protocol", "TCP");
		defaultConfig.setProperty("level", "INFO");
		File defaultConfigFile = new File("client_config");

		FileOutputStream fis = null;

		try {
			fis = new FileOutputStream(defaultConfigFile);
			defaultConfig.store(fis, null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fis != null)
					fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return defaultConfig;
	}

	private Properties getConfigurationFromFile(File file) throws IOException {
		Properties tmp = new Properties();
		FileInputStream fis = new FileInputStream(file);
		try {
			tmp.load(fis);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			fis.close();
		}

		Set<Object> keySet = tmp.keySet();
		Properties def = loadDefaultConfiguration();
		Set<Object> defaultKeySet = def.keySet();

		if (defaultKeySet.equals(keySet)) {
			return tmp;
		} else {
			for (Object s : defaultKeySet) {
				String key = (String) s;
				if (tmp.getProperty(key) == null)
					tmp.setProperty(key, def.getProperty(key));
			}
		}

		return tmp;
	}

	/**
	 * Metoda inicjalizujaca klase czekajaca na odpowiedzi serwera.
	 */
	private void initiateListener() {
		listener = new ServerResponseListener();
		new Thread(listener).start();

	}

	/**
	 * @see Logger#debug(String)
	 */
	@Override
	public void debug(String logString) {
		String message = name + " " + getDate() + " DEBUG " + Thread.currentThread().getId() + " " + logString;

		sendMessage(message);
	}

	/**
	 * @see Logger#crt(String)
	 */
	@Override
	public void crt(String logString) {

		String message = name + " " + getDate() + " CRITICAL " + Thread.currentThread().getId() + " " + logString;

		sendMessage(message);
	}

	/**
	 * @see Logger#info(String)
	 */
	public void info(String logString) {
		String message = name + " " + getDate() + " INFO " + Thread.currentThread().getId() + " " + logString;

		sendMessage(message);

	}

	/**
	 * @see Logger#warn(String)
	 */
	public void warn(String logString) {
		String message = name + " " + getDate() + " WARNING " + Thread.currentThread().getId() + " " + logString;

		sendMessage(message);

	}

	/**
	 * @see Logger#err(String)
	 */
	public void err(String logString) {
		String message = name + " " + getDate() + " ERROR " + Thread.currentThread().getId() + " " + logString;

		sendMessage(message);
	}

	private void sendMessage(String message) {

		if (protocol == Protocol.TCP) {
			try {
				output.write(message + "\n");
				output.flush();
			} catch (SocketException se) {
				FileHandler.saveFailedLog(name, message);
				reconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				udpSend(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * @see Logger#log(String)
	 */
	public void log(Log level, String logString) {
		if (logLevel.compareTo(level) <= 0 && Log.DEBUG.compareTo(level) == 0) {
			debug(logString);

		} else if (logLevel.compareTo(level) <= 0 && Log.INFO.compareTo(level) == 0) {
			info(logString);

		} else if (logLevel.compareTo(level) <= 0 && Log.WARNING.compareTo(level) == 0) {
			warn(logString);

		} else if (logLevel.compareTo(level) <= 0 && Log.ERROR.compareTo(level) == 0) {
			err(logString);

		} else if (logLevel.compareTo(level) <= 0 && Log.CRITICAL.compareTo(level) == 0) {
			crt(logString);
		}
	}

	@Override
	public void log(Log level, String logString, Object... objects) {

		String[] leftbracket = logString.split("\\{");

		int i = 1;
		for (Object o : objects) {
			try {
				leftbracket[i] = (String) o;
				i++;
			} catch (IndexOutOfBoundsException e) {
				break;
			}

		}

		StringBuilder buf = new StringBuilder();

		for (String s : leftbracket) {
			buf.append(s + " ");
		}

		logString = buf.toString();

		log(level, logString);

	}

	private void udpSend(String log) throws IOException {
		sendData = log.getBytes("UTF-8");
		DatagramPacket packet = null;
		try {
			packet = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(hostname), port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		if (packet != null)
			udpSocket.send(packet);

		// przesyla informacje listenerowi o logu ktory wlasnie zostal wyslany
		if (!log.equals("QUIT"))
			listener.addResponseLog(log);

	}

	/**
	 * Metoda zamykajaca wyjsciowy socket.
	 */
	public boolean closeOutput() {
		if (!isDone)
			return false;
		// gdyby listener nie dostal komunikatu o ostatnim logu
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		isDone = true;
		// czeka az listener otrzyma wszystkie odpowiedzi
		if (listener != null) {
			int loops = 0;
			System.out.println("waiting for the listener to stop");
			fullbreak: while (!listener.isDone()) {
				if (listener == null)
					break;
				try {
					Thread.sleep(1000);
					loops++;
					if (loops == 20) {
						listener.terminate();
						break fullbreak;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		if (System.console() != null)
			System.console().writer().println("CLIENT CLOSED.");

		try {
			if (output != null)
				output.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Metoda laczaca sie na nowo z serwerem.
	 */
	public boolean reconnect() {
		int tries = 0;
		while (true) {

			try {

				if (System.console() != null)
					System.console().writer().println(name + " reconnecting...");
				System.out.println(name + " reconnecting...");
				outputSocket = new Socket(hostname, port);
				if (System.console() != null)
					System.console().writer().println(name + " reconnected!");
				System.out.println(name + " reconnected!");
				break; // jesli polaczenie sie udalo, wyjdz z petli i
						// przejdz do
						// normalnego dzialania
			} catch (IOException e) {

				if (System.console() != null)
					System.console().writer().println(name + " reconnecting failed. Trying again..");
				System.out.println(name + " reconnecting failed. Trying again..");

				try {
					if (tries == 10)
						return false;
					tries++;
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}

		}

		try {
			output = new OutputStreamWriter(outputSocket.getOutputStream(), "UTF-8");
			System.out.println("output set");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		if (!sendFailedLogs())
			System.out.println("failed: sending awaiting logs");

		return true;
	}

	private boolean sendFailedLogs() {
		ArrayList<String> failedLogs = FileHandler.loadFailedLogs(this.name);
		if (failedLogs == null)
			return false;
		else
			for (String failedLog : failedLogs)
				sendMessage(failedLog);

		return true;

	}

	@Override
	public void stop() {
		setDone();
		closeOutput();
	}

	/**
	 * Metoda sygnalizujaca loggerowi, ze wszystkie zostaly wyslane i powinien
	 * konczyc prace.
	 */
	public void setDone() {
		sendMessage("QUIT");

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {

			e.printStackTrace();
		}

		isDone = true;
	}

	/**
	 * Klasa wewnetrzna odpowiadajaca za nasluchiwanie odpowiedzi serwera.
	 *
	 * @author Krzysztof Szczyrbak
	 *
	 */
	private class ServerResponseListener implements SimpleListener {
		private ArrayList<String> logsToCheckList;

		ServerResponseListener() {
			logsToCheckList = new ArrayList<String>();
		}

		/**
		 * Glowna metoda run, ktora w petli nasluchuje na odpowiedzi, dopoki
		 * klient nie zasygnalizuje ze skonczyl wysylanie logow.
		 */
		@Override
		public void run() {
			while (!JavaClientLog.this.isDone) {
				while (logsToCheckList.size() > 0) {
					try {
						getResponse();
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (isDone)
						break;
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					System.out.println("interrupted");
				}
			}
			System.out.println("listener ended");
			listener = null;
			return;
		}

		/**
		 * Metoda dodajaca wyslany log do kolejki oczekiwanych.
		 * 
		 * @param log
		 *            log wyslany
		 */
		public void addResponseLog(String log) {
			logsToCheckList.add(log);
		}

		/**
		 * Metoda nasluchujaca w petli na odpowiedz serwera. Jesli odpowiedz
		 * jest bledna, zapisz nieudany log w pliku.
		 * 
		 * @throws IOException
		 */
		public void getResponse() throws IOException {
			byte[] receiveData = new byte[1024];
			DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
			String log = logsToCheckList.get(0);
			logsToCheckList.remove(0);
			udpSocket.receive(receivedPacket);
			String receivedData = new String(receivedPacket.getData(), 0, receivedPacket.getLength(), "UTF-8");

			if (receivedData.equals("END"))
				for (String s : logsToCheckList)
					FileHandler.saveFailedLog(name, s);
			if (log.equals(receivedData)) {
				return;
			} else {
				logsToCheckList.remove(receivedData);
				FileHandler.saveFailedLog(name, log);
			}
		}

		/**
		 * Metoda sygnalizuje, ze listener jest gotowy, jesli lista jego
		 * oczekiwanych odpowiedzi jest pusta. Nie zaleca sie wywolywania przed
		 * koncem programu.
		 */
		@Override
		public boolean isDone() {
			return logsToCheckList.isEmpty();
		}

		/**
		 * Metoda zatrzymuj�ca prac� listenera
		 */
		public void terminate() {
			isDone = true;
			try {
				sendData = "END".getBytes("UTF-8");
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			DatagramPacket packet = null;
			try {
				packet = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("localhost"),
						udpSocket.getLocalPort());
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			if (packet != null)
				try {
					udpSocket.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}

		}
	}

}
