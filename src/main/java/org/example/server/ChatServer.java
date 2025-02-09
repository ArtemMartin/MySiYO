package org.example.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Logger;

public class ChatServer {
    private static final int PORT = 5252;
    private static final List<PrintWriter> clientWriters = new ArrayList<>();

    private static void myLog(IOException e) {
        Logger.getLogger(ChatServer.class.getName()).info("Шляпа: " + e.getMessage());
    }

    public void runServer() {
        System.out.println("Chat server started...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) new ClientHandler(serverSocket.accept()).start();
        } catch (IOException e) {
            myLog(e);
        }
    }

    class ClientHandler extends Thread {
        private final Socket socket;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received: " + message);
                    broadcast(message);
                }
            } catch (IOException e) {
                myLog(e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    myLog(e);
                }
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                }
            }
        }

        private void broadcast(String message) {
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.println(message);
                }
            }
        }
    }
}
