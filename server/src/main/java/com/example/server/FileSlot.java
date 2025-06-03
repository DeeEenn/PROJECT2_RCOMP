package com.example.server;

import java.io.Serializable;

public class FileSlot implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String filename;
    private String content;
    private boolean isOccupied;

    public FileSlot() {
        this.isOccupied = false;
    }

    public void storeFile(String filename, String content) {
        this.filename = filename;
        this.content = content;
        this.isOccupied = true;
    }

    public void clear() {
        this.filename = null;
        this.content = null;
        this.isOccupied = false;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public String getFilename() {
        return filename;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        if (!isOccupied) {
            return "Empty";
        }
        return filename;
    }
} 