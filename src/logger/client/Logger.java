package logger.client;

/**
 * Prosty interfejs loggera.
 * 
 * @author Krzysztof Szczyrbak
 *
 */
public interface Logger {

	/**
	 * Rozne poziomy logow. Reprezentuja waznosc danego komunikatu.
	 */
	enum Log {
		DEBUG, INFO, WARNING, ERROR, CRITICAL
	}

	/**
	 * Logowanie komunikatow z informacjami dla dewelopera.
	 * 
	 * @param logString
	 *            logowana wiadomosc
	 */
	void debug(String logString);

	/**
	 * Logowanie interesujacych komunikatow.
	 * 
	 * @param LogString
	 *            logowana wiadomosc
	 */
	void info(String LogString);

	/**
	 * Logowanie ostrzezen, na ktore warto zwrocic uwage.
	 *
	 * @param LogString
	 *            logowana wiadomosc
	 */
	void warn(String LogString);

	/**
	 * Logowanie bledow, ktore wymagaja podjecia dzialania.
	 *
	 * @param LogString
	 *            logowana wiadomosc
	 */
	void err(String LogString);

	/**
	 * Logowanie informacji krytycznych.
	 * 
	 * @param logString
	 *            logowana wiadomosc
	 */
	void crt(String logString);

	/**
	 * Loguje podana wiadomosc o wskazanym priorytecie.
	 *
	 * @param level
	 *            waznosc komunikatu
	 * @param LogString
	 *            logowana wiadomosc
	 */
	void log(Log level, String LogString);

	/**
	 * Loguje podana wiadomosc o wskazanym priorytecie oraz podanymi
	 * parametrami.
	 * 
	 * @param level
	 *            waznosc komunikatu
	 * @param logString
	 *            logowana wiadomosc
	 * @param objects
	 *            parametry do wpisania
	 */
	void log(Log level, String logString, Object... objects);

	/**
	 * Metoda zatrzymujaaca prace loggera. Przydatna w przypadku gdy trzeba
	 * zmienic jego implementacje lub konfiguracje.
	 */
	void stop();

}
