package suprsend;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Initialize HTTP request logs when debug is sent as true
 * @author Suprsend
 */
public class RequestLogs {
	static {
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.ALL);
		Logger log = LogManager.getLogManager().getLogger("");
		log.addHandler(handler);
	    log.setLevel(Level.ALL);
	}
}
