package utils;

import javafx.scene.media.Media;
import org.json.JSONObject;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class MusicLoader {
    public static Media loadMusic(int trackId, PrintStream dbg, boolean refresh) {
        DebugMessagePrinter messagePrinter = new DebugMessagePrinter(dbg);
        try {
            messagePrinter.println("Переданные параметры = {" + trackId + ", " + dbg + ", " + refresh + "}");
            String pathname = "cache/" + trackId + ".mp3";
            File cachedMedia = new File(pathname);

            if (cachedMedia.getParentFile().mkdirs() | cachedMedia.createNewFile())
                messagePrinter.println("Файл " + pathname + " успешно создан");
            else messagePrinter.println("Файл " + pathname + " будет перезаписан");

            String firstUrl = "https://music.yandex.ru/api/v2.1/handlers/track/" + trackId + "/track/download/m?hq=1";
            List<String[]> firstAdditionalHeaders = new LinkedList<>();
            firstAdditionalHeaders.add(new String[]{"X-Retpath-Y", "https://music.yandex.ru/"});

            Connector.CombinatedData<Map<String, List<String>>, InputStream> combinatedData =
                    Connector.connect(firstUrl, dbg, firstAdditionalHeaders);
            InputStream inputStream = combinatedData != null ? combinatedData.getValue2() : null;

            messagePrinter.println("FirstUrl = " + firstUrl);
            StringBuilder toStringBuilder = new StringBuilder();
            firstAdditionalHeaders.forEach((String[] strings) -> toStringBuilder.append(Arrays.toString(strings)));
            messagePrinter.println("FirstAdditionalHeaders = " + toStringBuilder.toString());

            if (inputStream != null) {
                messagePrinter.println("Соединение успешно");

                String firstJsonString = readStringInputStream(inputStream).toString();
                messagePrinter.println("FIRST_JSON = " + firstJsonString);

                JSONObject firstJSON = new JSONObject(firstJsonString);
                String src = firstJSON.getString("src");
                messagePrinter.println("Src = " + src);

                String secondUrl = src + "&format=json&external-domain=music.yandex.ru&overembed=no&__t=" + System.currentTimeMillis();
                messagePrinter.println("SecondUrl = " + secondUrl);

                InputStream secondInputStream = Connector.connect(secondUrl, dbg);
                if (secondInputStream == null) throw new MyException();

                String secondJsonString = readStringInputStream(secondInputStream).toString();
                JSONObject secondJSON = new JSONObject(secondJsonString);
                messagePrinter.println("SECOND_JSON = " + secondJsonString);

                String s, ts, path, host;

                s = secondJSON.getString("s");
                ts = secondJSON.getString("ts");
                path = secondJSON.getString("path");
                host = secondJSON.getString("host");

                messagePrinter.println("SECOND_JSON_ELEMENTS = {");
                messagePrinter.println("\ts\t\t=\t" + s + ",");
                messagePrinter.println("\tts\t\t=\t" + ts + ",");
                messagePrinter.println("\tpath\t=\t" + path + ",");
                messagePrinter.println("\thost\t=\t" + host);
                messagePrinter.println("}");

                String md5Salt = "XGRlBW9FXlekgbPrRHuSiA";

                String hashingString = md5Salt + path.substring(1, path.length()) + s;
                messagePrinter.println("HashingString = " + hashingString);

                /*MessageDigest messageDigest = MessageDigest.getInstance("org.MD5");
                messageDigest.reset();
                messageDigest.update(hashingString.getBytes());

                byte[] digest = messageDigest.digest();
                BigInteger bigInteger = new BigInteger(1, digest);
                StringBuilder md5Hash = new StringBuilder(bigInteger.toString(16));
                while (md5Hash.length() < 32)
                    md5Hash.insert(0, "0");

                String md5HashString = md5Hash.toString();*/
                //String md5HashString = Hashing.md5().newHasher().putString(hashingString, Charsets.UTF_8).hash().toString();

                //String md5HashString = Hashing.md5().hashString(hashingString, Charsets.UTF_8).toString();

                /*org.MD5 md5 = new org.MD5(hashingString);
                String md5HashString = md5.asHex();*/

                String md5HashString = org.MD5.toHexString(org.MD5.computeMD5(hashingString.getBytes())).toLowerCase();

                messagePrinter.println("Hash = " + md5HashString);
                String thirdUrl = "https://" + host + "/get-mp3/" + md5HashString + "/" + ts + path + "?track-id=" + trackId;
                messagePrinter.println("ThirdUrl = " + thirdUrl);
                LinkedList<String[]> additionalParams = new LinkedList<>();
                additionalParams.add(new String[]{"Accept-Encoding", "gzip, deflate"});
                Connector.CombinatedData<Map<String, List<String>>, InputStream> data = Connector.connect(thirdUrl, dbg, additionalParams);
                InputStream thirdInputStream = data != null ? data.getValue2() : null;
                String encoding;
                encoding = (data != null && data.getValue1().containsKey("Content-Encoding")) ? "gzip" : "deflate";
                messagePrinter.println("Encoding = " + encoding);
                if (thirdInputStream != null) {
                    OutputStream outputStream = new FileOutputStream(cachedMedia);
                    if (encoding.equals("gzip")) {
                        GZIPInputStream gzipInputStream = new GZIPInputStream(thirdInputStream);
                        int c;
                        while ((c = gzipInputStream.read()) != -1)
                            outputStream.write(c);
                        outputStream.close();
                        thirdInputStream.close();
                    } else {
                        int c;
                        while ((c = thirdInputStream.read()) != -1)
                            outputStream.write(c);
                        outputStream.close();
                        thirdInputStream.close();
                    }

                    return new Media(cachedMedia.toURI().toURL().toString());
                } else {
                    throw new MyException();
                }
            } else {
                throw new MyException();
            }

        } catch (MyException e) {
            messagePrinter.println("Произошли неполадки... Возвращаем нулевое значение");
            return null;
        } catch (IOException /*| NoSuchAlgorithmException*/ e) {
            messagePrinter.println("Произошла неисправимая ошибка! Возвращаем нулевое значение");
            messagePrinter.println(Arrays.toString(e.getStackTrace()));
            return null;
        } finally {
            messagePrinter.fire();
        }
    }

    private static StringBuilder readStringInputStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
            stringBuilder.append(line);
        inputStream.close();
        reader.close();
        return stringBuilder;
    }

    public static Media loadMusic(int trackId) {
        return loadMusic(trackId, null, false);
    }

    public static Media findInCache(int trackId) {
        String pathname = "cache/" + trackId + ".mp3";
        File cachedMedia = new File(pathname);
        try {
            if (cachedMedia.exists() && cachedMedia.length() != 0)
                return new Media(cachedMedia.toURI().toURL().toString());
            else {
                Logger logger = new Logger();
                try {
                    return loadMusic(trackId, logger.getPrintStream(), true);
                } finally {
                    logger.closeLog();
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    static class MyException extends Throwable {
        MyException() {
        }
    }
}
