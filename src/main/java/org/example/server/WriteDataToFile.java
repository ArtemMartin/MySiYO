package org.example.server;

import java.io.*;
import java.util.logging.Logger;

public class WriteDataToFile {
    File file = new File("DataChat.txt");
    BufferedWriter writer;

    public WriteDataToFile() {
        try {
            file.createNewFile();
            this.writer = new BufferedWriter((new FileWriter(file, true)));
        } catch (IOException e) {
            myLog(e, 15);
        }
    }

    private static void myLog(IOException e, int line) {
        Logger.getLogger(WriteDataToFile.class.getName()).info("Line " + line + ": " + e.getMessage());
    }


    public void appendDataToFile(String str) {
        try {
            writer.append(str).append("\n");
            writer.flush();
        } catch (IOException e) {
            myLog(e, 33);
        }
    }

}
