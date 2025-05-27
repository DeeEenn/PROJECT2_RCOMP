package com.fileserver;

public class ProgressTracker {
    private final long totalSize;
    private long currentSize;
    private final String fileName;

    public ProgressTracker(String fileName, long totalSize) {
        this.fileName = fileName;
        this.totalSize = totalSize;
        this.currentSize = 0;
    }

    public void updateProgressTracker(long bytesTransferred) {
        currentSize += bytesTransferred;
        int percentage = (int) (currentSize * 100 / totalSize);

        System.out.printf("\r%s: %d%% [%d/%d bytes]", fileName, percentage, currentSize, totalSize);

        if (percentage == 100) {
            System.out.println();
        }
    }
}
