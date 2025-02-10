package org.example.server;

import java.io.*;
import static java.lang.Thread.sleep;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatServer {

    private static final int PORT = 5252;
    private static final List<PrintWriter> clientWriters = new ArrayList<>();
    private static Map<String, Socket> client = new HashMap<>();
    private static ServerFrame serverFrame;

    private static void myLog(IOException e) {
        serverFrame.getPoleStatus().append("\nШляпа: " + e.getMessage());
        serverFrame.getPoleStatus().setCaretPosition(
                serverFrame.getPoleStatus().getDocument().getLength());
        Logger.getLogger(ChatServer.class.getName()).info("Шляпа: " + e.getMessage());
    }

    public ChatServer(ServerFrame serverFrame) {
        this.serverFrame = serverFrame;
    }

    public void runServer() {
        serverFrame.getPoleStatus().setText("Server started...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            myLog(e);
        }
    }

    public void outListClient() {
        while (true) {
            try {
                serverFrame.getPoleListAbonent().setText("");
                for (Map.Entry<String, Socket> entry : client.entrySet()) {
                    String key = entry.getKey();
                    Socket value = entry.getValue();
                    String ip = value.getInetAddress().toString();
                    serverFrame.getPoleListAbonent().append(key + ip + "\n");
                }
                sleep(5000);
            } catch (InterruptedException ex) {
                Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    class ClientHandler extends Thread {

        private final Socket socket;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            String name = "";
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                name = in.readLine();

                synchronized (clientWriters) {
                    clientWriters.add(out);
                    client.put(name, socket);
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
                    client.remove(name);
                }
            }
        }

        private void broadcast(String message) {
            synchronized (clientWriters) {
                serverFrame.getPoleStatus().append("\n" + message);
                serverFrame.getPoleStatus().setCaretPosition(
                        serverFrame.getPoleStatus().getDocument().getLength());
                for (PrintWriter writer : clientWriters) {
                    writer.println(message);
                }
            }
        }
    }
}
