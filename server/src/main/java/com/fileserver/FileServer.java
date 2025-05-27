package com.fileserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class FileServer {
    private static final int DEFAULT_PORT = 8080;
    private static final int MAX_SLOTS = 10;
    private static final int THREAD_POOL_SIZE = 10;

    private final Map<Integer, FileSlot> slots;
    private final String username;
    private final String password;
    private final int port;
    private final ExecutorService threadPool;
    private ServerSocket serverSocket;
    private boolean running;

    public FileServer(int port, String username, String password) {
        this.port = port;
        this.username = username;
        this.password = password;
        this.slots = new ConcurrentHashMap<>();
        this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        for (int i = 1; i <= MAX_SLOTS; i++) {
            slots.put(i, new FileSlot(i));
        }
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("Server běží na portu: " + port);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket, this).start();
            }
        } catch (IOException e) {
            System.err.println("Chyba při spuštění serveru: " + e.getMessage());
        }
    }

    public boolean authenticate(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }

    public Map<Integer, FileSlot> getSlots() {
        return slots;
    }

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        String username = "admin";
        String password = "password";

        FileServer server = new FileServer(port, username, password);
        server.start();
    }
} 