package com.fileserver.protocol;

public class Protocol {
    // Příkazy pro komunikaci
    public static final String AUTH = "AUTH";           // Autentizace
    public static final String LIST = "LIST";           // Seznam slotů
    public static final String UPLOAD = "UPLOAD";       // Nahrání souboru
    public static final String DOWNLOAD = "DOWNLOAD";   // Stažení souboru
    public static final String DELETE = "DELETE";       // Smazání souboru
    
    // Odpovědi serveru
    public static final String OK = "OK";               // Úspěšná operace
    public static final String ERROR = "ERROR";         // Chyba
    public static final String AUTH_OK = "AUTH_OK";     // Úspěšná autentizace
    public static final String AUTH_FAIL = "AUTH_FAIL"; // Neúspěšná autentizace
    
    // Oddělovače v zprávách
    public static final String DELIMITER = "|";
} 