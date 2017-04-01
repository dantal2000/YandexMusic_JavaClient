package utils;

import javafx.scene.image.Image;

import java.io.*;
import java.util.Arrays;

public class ImageLoader {
    public static Image retrieveImage(String uri, String size, int id, PrintStream dbg) {
        DebugMessagePrinter messagePrinter = new DebugMessagePrinter(dbg);
        try {
            Image inCache = findInCache(id, dbg);
            if (inCache != null) return inCache;

            String path = "cache/img/" + id + ".jpeg";
            File imageCache = new File(path);
            if (imageCache.getParentFile() != null && imageCache.getParentFile().mkdirs())
                messagePrinter.println("Поддиректория была успешно создана");
            if (imageCache.createNewFile())
                messagePrinter.println("Файл успешно создан");

            String url = "http://" + uri.replace("%%", size);
            messagePrinter.println("Url = " + url);
            InputStream inputStream = Connector.connect(url, dbg);
            if (inputStream != null) {
                if (imageCache.getParentFile().mkdirs() | imageCache.createNewFile())
                    messagePrinter.println("Файл " + path + " был создан");
                OutputStream outputStream = new FileOutputStream(imageCache);
                int c;
                while ((c = inputStream.read()) != -1)
                    outputStream.write(c);
                outputStream.close();
                inputStream.close();
                return new Image(new FileInputStream(imageCache));
            } else {
                messagePrinter.println("Соединения с сервером отсутствует. Возврат нулевого значения");
                return null;
            }
        } catch (IOException e) {
            messagePrinter.println("Произошла неисправимая ошибка. Возвращение нулевого значения");
            messagePrinter.println(Arrays.toString(e.getStackTrace()));
            return null;
        } finally {
            messagePrinter.fire();
        }
    }

    public static Image retrieveImage(String uri, String size, int id) {
        return retrieveImage(uri, size, id, null);
    }

    public static Image findInCache(int id, PrintStream dbg) {
        DebugMessagePrinter messagePrinter = new DebugMessagePrinter(dbg);
        try {
            String path = "cache/img/" + id + ".jpeg";
            messagePrinter.println("Path = " + path);
            File imageCache = new File(path);
            if (imageCache.exists()) {
                messagePrinter.println("Файл присутствует. Возврат файла");
                return new Image(new FileInputStream(imageCache));
            } else {
                messagePrinter.println("Файл отсутствует. Возврат нулевого значения");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            messagePrinter.println("Выполнение завершилось неисправимой ошибкой. Возврат нулевого значения");
            return null;
        } finally {
            messagePrinter.fire();
        }
    }
}
