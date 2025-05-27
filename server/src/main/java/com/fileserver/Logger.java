package com.fileserver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final String LOG_FILE = "server.log";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static PrintWriter writer;

    static {
        try {
            writer = new PrintWriter(new FileWriter(LOG_FILE, true));
        } catch(IOException e) {
            System.out.println("Failed to create log file: " + LOG_FILE);
        }
    }

    public static void log(String message){
        String timestamp = LocalDateTime.now().format(formatter);
        String logMessage = String.format("[%s] %s", timestamp, message);

        System.out.println(logMessage);
        writer.println(logMessage);
        writer.flush();
    }

    public static void close(){
        if (writer != null) { writer.close(); }
    }
}
