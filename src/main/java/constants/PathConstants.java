package constants;

public class PathConstants {
    public static final String BASE_PATH = System.getProperty("user.dir") + "/src/main/resources/portfolio_performance_file/";
    public static final String BASE_TARGET_PATH = System.getProperty("user.dir") + "/target/";
    public static final String INPUT_FILE_NAME = "Portfolio Performance.xml";
    public static final String OUTPUT_FILE_NAME = "Portfolio Performance imported.xml";
    public static final String SAVE_FILE_NAME = "do_not_delete_needed_for_next_import.json";
    public static final String LOGS_PATH = BASE_TARGET_PATH + "logs/";
    public static final String SAVE_FILE = LOGS_PATH + SAVE_FILE_NAME;
    public static final String CACHE_PATH = BASE_TARGET_PATH + "cache/";
}
