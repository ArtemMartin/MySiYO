package org.example.server;

import java.io.*;

import static java.lang.Thread.sleep;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatServer {

    static final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private static final int PORT = 59867;
    private static final List<PrintWriter> clientWriters = new ArrayList<>();
    private static final Map<String, Socket> client = new HashMap<>();
    private static ServerFrame serverFrame;
    private static WriteDataToFile writeDataToFile;

    public ChatServer(ServerFrame serverFrame) {
        ChatServer.serverFrame = serverFrame;
        writeDataToFile = new WriteDataToFile();
    }

    private static void myLog(IOException e) {
        serverFrame.getPoleStatus().append("\nShlapa: " + e.getMessage());
        serverFrame.getPoleStatus().setCaretPosition(
                serverFrame.getPoleStatus().getDocument().getLength());
        Logger.getLogger(ChatServer.class.getName()).info("Шляпа: " + e.getMessage());
    }

    public static int getPORT() {
        return PORT;
    }

    //получить время
    public String getTime() {
        return dateFormat.format(new Date());
    }

    public void runServer() {
        serverFrame.getPoleStatus().setText("Server started...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true)
                new ClientHandler(serverSocket.accept()).start();
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
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

                name = in.readLine();
                name = Crypto.getDeCryptoMessage(name);
                //отправить содержимое файла
                out.println(Arrays.toString(Files.readAllBytes(Paths.get(writeDataToFile.getFile()))));

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
            String check = "->";
            synchronized (clientWriters) {
                //раскодировать сообщение перед выводом
                String messageOnServer = Crypto.getDeCryptoMessage(message);
                serverFrame.getPoleStatus().append("\n" + messageOnServer);
                serverFrame.getPoleStatus().setCaretPosition(
                        serverFrame.getPoleStatus().getDocument().getLength());
                //проверка ненужных сообщений
                if (messageOnServer.contains(check)) {
                    writeDataToFile.appendDataToFile(message);
                    for (PrintWriter writer : clientWriters) {
                        writer.println(message);
                    }
                }
            }
        }
    }
}
