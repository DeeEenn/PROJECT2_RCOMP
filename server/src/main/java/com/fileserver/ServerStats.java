package com.fileserver;

import java.util.concurrent.atomic.AtomicLong;

public class ServerStats {
    private static final AtomicLong totalUploads = new AtomicLong(0);
    private static final AtomicLong totalDownloads = new AtomicLong(0);
    private static final AtomicLong totalBytesUploaded = new AtomicLong(0);
    private static final AtomicLong totalBytesDownloaded = new AtomicLong(0);

    public static void recordUpload(long bytes) {
        totalUploads.incrementAndGet();
        totalBytesUploaded.addAndGet(bytes);
    }

    public static void recordDownload(long bytes) {
        totalDownloads.incrementAndGet();
        totalBytesDownloaded.addAndGet(bytes);
    }

    public static String getStats(){
        return String.format(
                "Server statistics:\n" +
                        "Overall uploaded: %d files (%d bytes)\n" +
                        "Overall downloaded: %d files (%d bytes)",
                totalUploads.get(), totalBytesUploaded.get(),
                totalDownloads.get(), totalBytesDownloaded.get()
        );
    }
}
