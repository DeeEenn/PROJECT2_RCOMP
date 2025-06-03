package com.example.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final int MAX_CLIENTS = 10;
    private static final int SOCKET_TIMEOUT = 300000; // 5 minutes
    
    private final int port;
    private final int maxSlots;
    private final String username;
    private final String password;
    private final int maxFileSize;
    
    private final ExecutorService threadPool;
    private volatile boolean running = true;

    public Server() {
        Properties config = loadConfig();
        this.port = Integer.parseInt(config.getProperty("server.port", "8080"));
        this.maxSlots = Integer.parseInt(config.getProperty("server.max_slots", "10"));
        this.username = config.getProperty("auth.username", "admin");
        this.password = config.getProperty("auth.password", "admin");
        
        // Parse max file size and remove any comments
        String maxFileSizeStr = config.getProperty("file.max_size", "1048576");
        if (maxFileSizeStr.contains("#")) {
            maxFileSizeStr = maxFileSizeStr.substring(0, maxFileSizeStr.indexOf("#")).trim();
        }
        this.maxFileSize = Integer.parseInt(maxFileSizeStr);
        
        this.threadPool = Executors.newFixedThreadPool(MAX_CLIENTS);
        
        // Setting static values in ClientHandler
        ClientHandler.setMaxSlots(maxSlots);
        ClientHandler.setCredentials(username, password);
        ClientHandler.setMaxFileSize(maxFileSize);
    }

    private Properties loadConfig() {
        Properties config = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                config.load(input);
                logger.info("Configuration loaded successfully");
            } else {
                logger.warn("Configuration file not found, using default values");
            }
        } catch (IOException e) {
            logger.error("Error loading configuration", e);
        }
        return config;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Server started on port {}", port);
            logger.info("Max slots: {}", maxSlots);
            logger.info("Max file size: {} bytes", maxFileSize);

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    clientSocket.setSoTimeout(SOCKET_TIMEOUT);
                    threadPool.execute(new ClientHandler(clientSocket));
                } catch (IOException e) {
                    if (running) {
                        logger.error("Error accepting client connection", e);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Server error", e);
        } finally {
            shutdown();
        }
    }

    public void shutdown() {
        running = false;
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
} 