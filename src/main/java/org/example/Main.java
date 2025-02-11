package org.example;

import org.example.server.ChatServer;
import org.example.server.ServerFrame;
import org.example.server.ServerUtils;

public class Main {

    public static void main(String[] args) {
        ServerFrame serverFrame = new ServerFrame();
        serverFrame.setVisible(true);

        ChatServer chatServer = new ChatServer(serverFrame);
        serverFrame.getPoleAdrServer().setText(new ServerUtils().getServerIPv4Address());
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
