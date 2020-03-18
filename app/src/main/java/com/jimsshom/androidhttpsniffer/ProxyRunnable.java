package com.jimsshom.androidhttpsniffer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxyRunnable implements Runnable {
    private ServerSocket serverSocket;

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(36994);
            System.out.println(serverSocket.getInetAddress().toString());
            System.out.println(serverSocket.getLocalPort());
            System.out.flush();

            while (true) {
                Socket socket = serverSocket.accept();

                SocketTask socketTask = new SocketTask(socket);
                Thread thread = new Thread(socketTask);
                thread.setName(socket.getRemoteSocketAddress().toString());
                thread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
