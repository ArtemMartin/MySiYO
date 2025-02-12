package org.example.server;

import java.io.*;
import static java.lang.Thread.sleep;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatServer {

    static final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private static final int PORT = 59867;
    private static final List<PrintWriter> clientWriters = new ArrayList<>();
    private final Crypto crypto = new Crypto();
    private static Map<String, Socket> client = new HashMap<>();
    private static ServerFrame serverFrame;
    private String messageOnServer;

    private static void myLog(IOException e) {
        serverFrame.getPoleStatus().append("\nShlapa: " + e.getMessage());
        serverFrame.getPoleStatus().setCaretPosition(
                serverFrame.getPoleStatus().getDocument().getLength());
        Logger.getLogger(ChatServer.class.getName()).info("Шляпа: " + e.getMessage());
    }

    public ChatServer(ServerFrame serverFrame) {
        this.serverFrame = serverFrame;
    }

    //получить время
    public String getTime() {
        return dateFormat.format(new Date()).toString();
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

    //вывод списка подключенных клиентов
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
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

                name = in.readLine();
                name = crypto.getDeCryptoMessage(name);

                synchronized (clientWriters) {
                    clientWriters.add(out);
                    client.put(name, socket);
                }
                String message;
                while ((message = in.readLine()) != null) {
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
                    serverFrame.getPoleStatus().append("\n" + getTime()
                            + " " + name + ": Disconect...");
                    serverFrame.getPoleStatus().setCaretPosition(
                            serverFrame.getPoleStatus().getDocument().getLength());
                    client.remove(name);
                }
            }
        }

        private void broadcast(String message) {
            synchronized (clientWriters) {
                messageOnServer = crypto.getDeCryptoMessage(message);
                //раскодировать сообщение перед выводом 
                serverFrame.getPoleStatus().append("\n" + messageOnServer);
                serverFrame.getPoleStatus().setCaretPosition(
                        serverFrame.getPoleStatus().getDocument().getLength());
                for (PrintWriter writer : clientWriters) {
                    writer.println(message);
                }
            }
        }
    }
}
