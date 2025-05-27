package com.fileserver;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyStore;

public class SSLServer {
    private static final String KEYSTORE_PATH = "server.keystore";
    private static final String KEYSTORE_PASSWORD = "password";
    private static final String KEY_PASSWORD = "password";

    private SSLServerSocket serverSocket;
    private final FileServer fileServer;

    public SSLServer(FileServer fileServer) {
        this.fileServer = fileServer;
        initializeSSL();
    }

    public void initializeSSL() {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(KEYSTORE_PATH), KEYSTORE_PASSWORD.toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, KEY_PASSWORD.toCharArray());

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, null);

            SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
            serverSocket.setEnabledProtocols(new String[] {"TLSv1.2", "TLSv1.3"});
        } catch (Exception e) {
            System.out.println("Error initializing SSL context: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void start() {
        try {
            System.out.println("Starting SSL server on port " + serverSocket.getLocalPort());

            while (true) {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                new ClientHandler(clientSocket, fileServer).start();
            }
        } catch (Exception e) {
            System.out.println("Error initializing SSL server: " + e.getMessage());
        }
    }
}
