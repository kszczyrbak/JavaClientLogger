package logger.client;

import java.io.IOException;

 /**
		 * Interfejs prostego listenera, ktory oczekuje na odpowiedzi serwera.
		 * 
		 * @author Krzysztof Szczyrbak
		 *
		 */
public interface SimpleListener extends Runnable {

	/**
	 * Dodaje spodziewana odpowiedz do kolejki listenera.
	 * 
	 * @param response
	 *            spodziewana odpowiedz
	 */
	void addResponseLog(String response);

	/**
	 * Metoda boolean zwracajaca informacje o tym czy listener otrzymal wszystkie oczekiwane
	 * odpowiedzi.
	 * 
	 * @return true/false
	 */
	boolean isDone();

	/**
	 * Metoda oczekujaca na odpowiedz serwera.
	 * 
	 * @throws IOException
	 */
	void getResponse() throws IOException;

	/**
	 * Metoda rozkazujaca listenerowi zakonczyc prace.
	 */
	void terminate();

}
