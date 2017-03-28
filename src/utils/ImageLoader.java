package utils;

import javafx.scene.image.Image;
import utils.Connector;
import utils.DebugMessagePrinter;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;

public class ImageLoader {
    public static Image retrieveImage(String uri, String size, int id, PrintStream dbg) {
        DebugMessagePrinter messagePrinter = new DebugMessagePrinter(dbg);
        try {
            String path = "cache/" + id + ".jpeg";
            File imageCache = new File(path);
            if (imageCache.exists()) {
                messagePrinter.println("Обнаружен кэш изображения " + id);
                return new Image(new FileInputStream(imageCache));
            }
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

    public static Image findInCache(int id) {
        String path = "cache/" + id + ".jpeg";
        File imageCache = new File(path);
        if (imageCache.exists()) {
            try {
                return new Image(new FileInputStream(imageCache));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
