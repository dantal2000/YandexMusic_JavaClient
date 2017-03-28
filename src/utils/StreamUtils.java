package utils;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;

import java.io.*;

public class StreamUtils {
    public static void transferToFile(String filePath, InputStream stream, PrintStream dbg) {
        DebugMessagePrinter messagePrinter = new DebugMessagePrinter(dbg);
        try {
            File file = new File(filePath);
            if ((file.getParentFile() != null && file.getParentFile().mkdirs()) | file.createNewFile())
                messagePrinter.println("Файл " + filePath + " был успешно создан");
            OutputStream outputStream = new FileOutputStream(file);
            int c;
            while ((c = stream.read()) != -1)
                outputStream.write(c);
            outputStream.flush();
            outputStream.close();
            messagePrinter.println("Файл " + filePath + " успешно записан");
        } catch (IOException e) {
            e.printStackTrace();
            messagePrinter.println("Неиспарвимая ошибка");
            messagePrinter.println(e.getMessage());
        } finally {
            messagePrinter.fire();
        }
    }

    public static void transferToFile(String filePath, InputStream stream) {
        transferToFile(filePath, stream, null);
    }

    public static void writeToFile(String filePath, String content, PrintStream dbg) {
        DebugMessagePrinter messagePrinter = new DebugMessagePrinter(dbg);
        try {
            ByteInputStream inputStream = new ByteInputStream(content.getBytes(), content.length());
            transferToFile(filePath, inputStream, dbg);
        } finally {
            messagePrinter.fire();
        }
    }

    public static void writeToFile(String filePath, String content) {
        writeToFile(filePath, content, null);
    }
}
