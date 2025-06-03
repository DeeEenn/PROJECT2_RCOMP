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
    
    private String filename;
    private String fileId;
    private boolean isOccupied;

    public FileSlot() {
        this.isOccupied = false;
        createStorageDir();
    }

    private void createStorageDir() {
        try {
            Files.createDirectories(Paths.get(STORAGE_DIR));
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