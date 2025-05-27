package com.fileserver.protocol;

public class Protocol {

    // Request
    public static final String AUTH = "AUTH";      
    public static final String LIST = "LIST";           
    public static final String UPLOAD = "UPLOAD";      
    public static final String DOWNLOAD = "DOWNLOAD";  
    public static final String DELETE = "DELETE";
    public static final String MOVE = "MOVE";
    public static final String RENAME = "RENAME";
    public static final String STATS = "STATS";
    public static final String COMPRESS = "COMPRESS";
    // Response
    public static final String OK = "OK";             
    public static final String ERROR = "ERROR";        
    public static final String AUTH_OK = "AUTH_OK";     
    public static final String AUTH_FAIL = "AUTH_FAIL"; 
    
    public static final String DELIMITER = "|";
} 