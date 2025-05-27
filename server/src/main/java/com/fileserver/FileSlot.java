package com.fileserver;

public class FileSlot {
    private final int slotNumber;
    private String fileName;
    private byte[] fileContent;
    private boolean isEmpty;
    private static final int MAX_SIZE = 100 * 1024 * 1024; // Maximum size of 100 MB.

    public FileSlot(int slotNumber) {
        this.slotNumber = slotNumber;
        this.isEmpty = true;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public void storeFile(String fileName, byte[] content) {
        if (content.length > MAX_SIZE) {
            throw new IllegalArgumentException("File content is too large to be stored maximum is: " + MAX_SIZE);
        }
        this.fileName = fileName;
        this.fileContent = content;
        this.isEmpty = false;
    }

    public boolean renameFile(String newName) {
        if (isEmpty || newName == null || newName.trim().isEmpty()) {
            return false;
        }

        this.fileName = newName;
        return true;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public void clear() {
        this.fileName = null;
        this.fileContent = null;
        this.isEmpty = true;
    }

    public int getSlotNumber() {
        return slotNumber;
    }
} 