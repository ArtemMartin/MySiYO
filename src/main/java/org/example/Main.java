package org.example;

import org.example.server.ChatServer;
import org.example.server.ServerFrame;
import org.example.server.ServerUtils;

public class Main {

    public static void main(String[] args) {
        ServerFrame serverFrame = new ServerFrame();
        serverFrame.setVisible(true);

        ChatServer chatServer = new ChatServer(serverFrame);
        serverFrame.getPoleAdrServer().setText(ServerUtils.getServerIPv4Address());
        serverFrame.getPolePort().setText(String.valueOf(ChatServer.getPORT()));
        Thread thread2 = new Thread(chatServer::runServer);
        thread2.start();

        Thread thread = new Thread(chatServer::outListClient);
        thread.start();
    }
}
