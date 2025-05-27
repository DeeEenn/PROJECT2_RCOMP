package protocol;

public class Protocol {
    // This class serves as a protocol for the file server and client to communicate with each other.
    
    // Request types
    public static final String AUTH = "AUTH";
    public static final String LIST = "LIST";
    public static final String UPLOAD = "UPLOAD";
    public static final String DOWNLOAD = "DOWNLOAD";
    public static final String DELETE = "DELETE";

    // Response types
    public static final String OK = "OK";
    public static final String ERROR = "ERROR";
    public static final String AUTH_OK = "AUTH_OK";
    public static final String AUTH_FAIL = "AUTH_FAIL";

    // Delimiter
    public static final String DELIMITER = "|";
}
