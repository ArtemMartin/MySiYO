package org.example;

import org.example.server.ChatServer;
import org.example.server.ServerFrame;

public class Main {

    public static void main(String[] args) {
        ServerFrame serverFrame = new ServerFrame();
        serverFrame.setVisible(true);
        
        ChatServer chatServer = new ChatServer(serverFrame);
        chatServer.runServer();
        
        
    }
}
