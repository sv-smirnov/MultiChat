import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerLog {
    public static Logger LOGGER = LogManager.getLogger(ServerLog.class);
    public static void main(String[] args) {
        LOGGER.debug("Debug");
        LOGGER.info("Info");
        LOGGER.warn("Warn");
        LOGGER.error("Error");
        LOGGER.fatal("Fatal");
    }
}
