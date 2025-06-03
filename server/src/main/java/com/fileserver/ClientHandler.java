package com.fileserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

import com.fileserver.protocol.Protocol;

public class ClientHandler extends Thread {
    private final Socket clientSocket;
    private final FileServer server;
    private boolean authenticated;
    private static final int TIMEOUT = 3000;

    public ClientHandler(Socket socket, FileServer server) {
        this.clientSocket = socket;
        this.server = server;
        this.authenticated = false;

        try {
            clientSocket.setSoTimeout(TIMEOUT);
        } catch (IOException e) {
            System.out.println("Client socket set timeout");
        }
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                String[] parts = inputLine.split("\\" + Protocol.DELIMITER);
                String command = parts[0];

                if (!authenticated && !command.equals(Protocol.AUTH)) {
                    out.println(Protocol.ERROR + Protocol.DELIMITER + "You are not authenticated");
                    continue;
                }

                switch (command) {
                    case Protocol.AUTH:
                        handleAuth(parts, out);
                        break;
                    case Protocol.LIST:
                        handleList(out);
                        break;
                    case Protocol.UPLOAD:
                        handleUpload(parts, out);
                        break;
                    case Protocol.DOWNLOAD:
                        handleDownload(parts, out, in);
                        break;
                    case Protocol.DELETE:
                        handleDelete(parts, out);
                        break;
                    default:
                        out.println(Protocol.ERROR + Protocol.DELIMITER + "Unknown command");
                }
            }
        } catch (IOException e) {
            System.err.println("Error processing client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    private void handleAuth(String[] parts, PrintWriter out) {
        if (parts.length != 3) {
            out.println(Protocol.ERROR + Protocol.DELIMITER + "Wrong command format");
            return;
        }
        String username = parts[1];
        String password = parts[2];
        
        if (server.authenticate(username, password)) {
            authenticated = true;
            out.println(Protocol.AUTH_OK);
        } else {
            out.println(Protocol.AUTH_FAIL);
        }
    }

    private void handleList(PrintWriter out) {
        StringBuilder response = new StringBuilder(Protocol.LIST);
        for (Map.Entry<Integer, FileSlot> entry : server.getSlots().entrySet()) {
            FileSlot slot = entry.getValue();
            if (!slot.isEmpty()) {
                response.append(Protocol.DELIMITER)
                       .append(slot.getSlotNumber())
                       .append(":")
                       .append(slot.getFileName());
            }
        }
        out.println(response.toString());
    }

    private void handleUpload(String[] parts, PrintWriter out) {
        if (parts.length != 4) {
            out.println(Protocol.ERROR + Protocol.DELIMITER + "Wrong command format");
            return;
        }

        try {
            int slotNumber = Integer.parseInt(parts[1]);
            String fileName = parts[2];
            byte[] content = parts[3].getBytes();

            FileSlot slot = server.getSlots().get(slotNumber);
            if (slot == null) {
                out.println(Protocol.ERROR + Protocol.DELIMITER + "Non-existent slot");
                return;
            }

            if (!slot.isEmpty()) {
                out.println(Protocol.ERROR + Protocol.DELIMITER + "Slot is occupied");
                return;
            }

            slot.storeFile(fileName, content);
            out.println(Protocol.OK + Protocol.DELIMITER + "File successfully uploaded");
        } catch (NumberFormatException e) {
            out.println(Protocol.ERROR + Protocol.DELIMITER + "Invalid slot number");
        }
    }

    private void handleDownload(String[] parts, PrintWriter out, BufferedReader in) {
        if (parts.length != 2) {
            out.println(Protocol.ERROR + Protocol.DELIMITER + "Wrong command format");
            return;
        }

        try {
            int slotNumber = Integer.parseInt(parts[1]);
            FileSlot slot = server.getSlots().get(slotNumber);
            
            if (slot == null) {
                out.println(Protocol.ERROR + Protocol.DELIMITER + "Non-existent slot");
                return;
            }

            if (slot.isEmpty()) {
                out.println(Protocol.ERROR + Protocol.DELIMITER + "Slot is empty");
                return;
            }

            out.println(Protocol.OK + Protocol.DELIMITER + slot.getFileName() + 
                       Protocol.DELIMITER + new String(slot.getFileContent()));
        } catch (NumberFormatException e) {
            out.println(Protocol.ERROR + Protocol.DELIMITER + "Invalid slot number");
        }
    }

    private void handleDelete(String[] parts, PrintWriter out) {
        if (parts.length != 2) {
            out.println(Protocol.ERROR + Protocol.DELIMITER + "Wrong command format");
            return;
        }

        try {
            int slotNumber = Integer.parseInt(parts[1]);
            FileSlot slot = server.getSlots().get(slotNumber);
            
            if (slot == null) {
                out.println(Protocol.ERROR + Protocol.DELIMITER + "Non-existent slot");
                return;
            }

            if (slot.isEmpty()) {
                out.println(Protocol.ERROR + Protocol.DELIMITER + "Slot is already empty");
                return;
            }

            slot.clear();
            out.println(Protocol.OK + Protocol.DELIMITER + "File successfully deleted");
        } catch (NumberFormatException e) {
            out.println(Protocol.ERROR + Protocol.DELIMITER + "Invalid slot number");
        }
    }
} 