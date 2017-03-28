package utils;

import java.io.*;

public class Logger {
    private PrintStream printStream;
    private String path;

    public Logger(String path) {
        init(path);
    }

    private void init(String path) {
        this.path = path;
        File file = new File(path);
        try {
            if (!file.exists()) {
                if (file.getParentFile() != null && file.getParentFile().mkdirs())
                    System.out.println("Поддиректория лог-файла была создана");
                if (file.createNewFile()) System.out.println("Лог-файл " + path + " был создан");
            }
            printStream = new PrintStream(new FileOutputStream(file));
            System.out.println("Перенаправление в файл " + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Logger() {
        init("cache/log");
    }

    public PrintStream getPrintStream() {
        return printStream;
    }

    public void closeLog() {
        System.out.println("Логгирование " + path + " закончено");
        printStream.close();
    }
}
