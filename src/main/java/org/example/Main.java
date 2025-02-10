package org.example;

import org.example.server.ChatServer;
import org.example.server.ServerFrame;

public class Main {

    public static void main(String[] args) {
        ServerFrame serverFrame = new ServerFrame();
        serverFrame.setVisible(true);

        ChatServer chatServer = new ChatServer(serverFrame);
        Thread thread2 = new Thread(() -> {
            chatServer.runServer();
        });
        thread2.start();

        Thread thread = new Thread(() -> {
            chatServer.outListClient();
        });
        thread.start();
    }
}
