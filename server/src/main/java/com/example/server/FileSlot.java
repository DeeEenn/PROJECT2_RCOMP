package com.example.server;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class FileSlot implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String STORAGE_DIR = "server_storage";
    private static final String SLOTS_DIR = "slots";
    
    private String filename;
    private String fileId;
    private boolean isOccupied;
    private int slotNumber;

    public FileSlot(int slotNumber) {
        this.slotNumber = slotNumber;
        this.isOccupied = false;
        createStorageDirs();
        loadFromDisk();
    }

    private void createStorageDirs() {
        try {
            Files.createDirectories(Paths.get(STORAGE_DIR));
            Files.createDirectories(Paths.get(STORAGE_DIR, SLOTS_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFromDisk() {
        Path slotFile = Paths.get(STORAGE_DIR, SLOTS_DIR, "slot_" + slotNumber + ".meta");
        if (Files.exists(slotFile)) {
            try {
                String content = Files.readString(slotFile);
                String[] parts = content.split("\\|");
                if (parts.length >= 2) {
                    this.filename = parts[0];
                    this.fileId = parts[1];
                    this.isOccupied = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveToDisk() {
        Path slotFile = Paths.get(STORAGE_DIR, SLOTS_DIR, "slot_" + slotNumber + ".meta");
        try {
            if (isOccupied) {
                Files.writeString(slotFile, filename + "|" + fileId);
            } else {
                Files.deleteIfExists(slotFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void storeFile(String filename, String content) {
        this.filename = filename;
        this.fileId = UUID.randomUUID().toString();
        this.isOccupied = true;
        
        try {
            Path filePath = Paths.get(STORAGE_DIR, fileId);
            Files.write(filePath, content.getBytes());
            saveToDisk();
        } catch (IOException e) {
            e.printStackTrace();
            this.isOccupied = false;
            this.fileId = null;
        }
    }

    public void clear() {
        if (fileId != null) {
            try {
                Files.deleteIfExists(Paths.get(STORAGE_DIR, fileId));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.filename = null;
        this.fileId = null;
        this.isOccupied = false;
        saveToDisk();
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public String getFilename() {
        return filename;
    }

    public String getContent() {
        if (!isOccupied || fileId == null) {
            return null;
        }
        try {
            return new String(Files.readAllBytes(Paths.get(STORAGE_DIR, fileId)));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        if (!isOccupied) {
            return "Empty";
        }
        return filename;
    }
} 