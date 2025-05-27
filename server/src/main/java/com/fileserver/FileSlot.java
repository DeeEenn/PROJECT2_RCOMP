package com.fileserver;

public class FileSlot {
    private final int slotNumber;
    private String fileName;
    private byte[] fileContent;
    private boolean isEmpty;

    public FileSlot(int slotNumber) {
        this.slotNumber = slotNumber;
        this.isEmpty = true;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public void storeFile(String fileName, byte[] content) {
        this.fileName = fileName;
        this.fileContent = content;
        this.isEmpty = false;
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