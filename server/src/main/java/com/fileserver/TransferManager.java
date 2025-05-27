package com.fileserver;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TransferManager{
    private static final ConcurrentHashMap<String, AtomicInteger> activeTransfers = new ConcurrentHashMap<>();
    private static final int MAX_CONCURRENT_TRANSFERS = 3;

    public static boolean canStartTransfer(String clientId) {
        AtomicInteger count = activeTransfers.computeIfAbsent(clientId, k -> new AtomicInteger(0));
        return count.get() < MAX_CONCURRENT_TRANSFERS;
    }

    public static void startTransfer(String clientId) {
        activeTransfers.computeIfAbsent(clientId, k -> new AtomicInteger(0)).incrementAndGet();
    }

    public static void endTransfer(String clientId){
        AtomicInteger count = activeTransfers.get(clientId);
        if(count != null){
            count.decrementAndGet();
        }
    }
}
