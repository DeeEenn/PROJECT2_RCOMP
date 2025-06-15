package com.example.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private static final ConcurrentHashMap<String, String> users = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, FileSlot> slots = new ConcurrentHashMap<>();
    
    private static int MAX_SLOTS = 10;
    private static String AUTH_USERNAME = "admin";
    private static String AUTH_PASSWORD = "admin";
    private static int MAX_FILE_SIZE = 1048576; // 1MB

    private final Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        // Initialize slots only if not already initialized
        for (int i = 0; i < MAX_SLOTS; i++) {
            slots.putIfAbsent(i, new FileSlot(i));
        }
    }

    public static void setMaxSlots(int maxSlots) {
        MAX_SLOTS = maxSlots;
    }

    public static void setCredentials(String username, String password) {
        AUTH_USERNAME = username;
        AUTH_PASSWORD = password;
    }

    public static void setMaxFileSize(int maxFileSize) {
        MAX_FILE_SIZE = maxFileSize;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.trim().isEmpty()) continue;
                
                String[] parts = inputLine.split("\\|");
                String command = parts[0];

                switch (command) {
                    case "AUTH":
                        handleAuth(parts);
                        break;
                    case "LIST":
                        handleList();
                        break;
                    case "UPLOAD":
                        handleUpload(parts);
                        break;
                    case "DOWNLOAD":
                        handleDownload(parts);
                        break;
                    case "DELETE":
                        handleDelete(parts);
                        break;
                    default:
                        out.println("ERROR|Unknown command");
                }
            }
        } catch (IOException e) {
            logger.error("Error handling client", e);
        } finally {
            closeConnection();
        }
    }

    private void handleAuth(String[] parts) {
        if (parts.length != 3) {
            out.println("AUTH_FAIL");
            return;
        }
        String username = parts[1];
        String password = parts[2];

        if (username.equals(AUTH_USERNAME) && password.equals(AUTH_PASSWORD)) {
            users.put(username, password);
            out.println("AUTH_OK");
        } else {
            out.println("AUTH_FAIL");
        }
    }

    private void handleList() {
        StringBuilder response = new StringBuilder("OK|");
        boolean hasFiles = false;
        for (int i = 0; i < MAX_SLOTS; i++) {
            FileSlot slot = slots.get(i);
            if (slot != null && slot.isOccupied()) {
                response.append(i).append(":").append(slot.getFilename()).append(";");
                hasFiles = true;
            }
        }
        if (!hasFiles) {
            response.append("No files");
        }
        out.println(response.toString());
    }

    private void handleUpload(String[] parts) {
        if (parts.length != 4) {
            out.println("ERROR|Invalid upload format");
            return;
        }

        try {
            int slot = Integer.parseInt(parts[1]);
            if (slot < 0 || slot >= MAX_SLOTS) {
                out.println("ERROR|Invalid slot number");
                return;
            }

            String filename = parts[2];
            String content = parts[3];

            if (content.length() > MAX_FILE_SIZE) {
                out.println("ERROR|File too large");
                return;
            }

            FileSlot fileSlot = slots.get(slot);
            if (fileSlot.isOccupied()) {
                out.println("ERROR|Slot is occupied");
                return;
            }

            fileSlot.storeFile(filename, content);
            out.println("OK|File uploaded successfully");
        } catch (NumberFormatException e) {
            out.println("ERROR|Invalid slot number format");
        }
    }

    private void handleDownload(String[] parts) {
        if (parts.length != 2) {
            out.println("ERROR|Invalid download format");
            return;
        }

        try {
            int slot = Integer.parseInt(parts[1]);
            if (slot < 0 || slot >= MAX_SLOTS) {
                out.println("ERROR|Invalid slot number");
                return;
            }

            FileSlot fileSlot = slots.get(slot);
            if (fileSlot == null || !fileSlot.isOccupied()) {
                out.println("ERROR|No file in slot " + slot);
                return;
            }

            out.println("OK|" + fileSlot.getFilename() + "|" + fileSlot.getContent());
        } catch (NumberFormatException e) {
            out.println("ERROR|Invalid slot number format");
        }
    }

    private void handleDelete(String[] parts) {
        if (parts.length != 2) {
            out.println("ERROR|Invalid delete format");
            return;
        }

        try {
            int slot = Integer.parseInt(parts[1]);
            if (slot < 0 || slot >= MAX_SLOTS) {
                out.println("ERROR|Invalid slot number");
                return;
            }

            FileSlot fileSlot = slots.get(slot);
            if (fileSlot == null || !fileSlot.isOccupied()) {
                out.println("ERROR|No file in slot " + slot);
                return;
            }

            fileSlot.clear();
            out.println("OK|File deleted successfully");
        } catch (NumberFormatException e) {
            out.println("ERROR|Invalid slot number format");
        }
    }

    private void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            logger.error("Error closing connection", e);
        }
    }
} 