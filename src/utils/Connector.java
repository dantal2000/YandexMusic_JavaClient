package utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpCookie;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class Connector {
    public static InputStream connect(String url) {
        CombinatedData<Map<String, List<String>>, InputStream> combinatedData = connect(url, null, null);
        if (combinatedData != null)
            return combinatedData.getValue2();
        else return null;
    }

    public static InputStream connect(String url, PrintStream dbg) {
        CombinatedData<Map<String, List<String>>, InputStream> combinatedData = connect(url, dbg, null);
        if (combinatedData != null)
            return combinatedData.getValue2();
        else return null;
    }

    public static CombinatedData<Map<String, List<String>>, InputStream> connect(String url, PrintStream dbg, List<String[]> additionalHeaders) {
        DebugMessagePrinter messagePrinter = new DebugMessagePrinter(dbg);

        try {
            URLConnection connection = new URL(url).openConnection();
            messagePrinter.println("Connection = " + connection.hashCode());
            String user_agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:52.0) Gecko/20100101 Firefox/52.0 Waterfox/52.0";

            Map<String, String> myRequestProperties = new HashMap<>();
            myRequestProperties.put("User-Agent", user_agent);
            myRequestProperties.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            myRequestProperties.put("Accept-Language", "ru,en-US;q=0.7,en;q=0.3");
            myRequestProperties.put("Accept-Encoding", "deflate");

            messagePrinter.println("Добавление Cookies");
            List<HttpCookie> httpCookies = CookieUtils.readCookies(url, dbg);
            if (httpCookies != null && httpCookies.size() != 0) {
                StringBuilder stringBuilder = new StringBuilder(httpCookies.size());
                httpCookies.forEach(httpCookie -> stringBuilder.append(httpCookie.toString()).append(";"));
                String message = stringBuilder.toString();
                messagePrinter.println(message);
                if (myRequestProperties.containsKey("Cookie"))
                    myRequestProperties.replace("Cookie", message);
                else
                    myRequestProperties.put("Cookie", message);
            }

            if (additionalHeaders != null)
                additionalHeaders.forEach(strings -> {
                    if (myRequestProperties.containsKey(strings[0]))
                        myRequestProperties.replace(strings[0], strings[1]);
                    else
                        myRequestProperties.put(strings[0], strings[1]);
                });

            myRequestProperties.forEach(connection::addRequestProperty);
            messagePrinter.println("Заголовки добавлены");

            connection.getRequestProperties().forEach((s, strings) -> messagePrinter.println("\t" + s + "\t=\t" + strings));

            messagePrinter.println("Соединение...");
            connection.connect();

            Map<String, List<String>> headerFields = connection.getHeaderFields();
            String headerName = null;
            if (headerFields.containsKey("set-cookie")) {
                headerName = "set-cookie";
            } else if (headerFields.containsKey("Set-Cookie")) {
                headerName = "Set-Cookie";
            }
            if (headerName != null) {
                messagePrinter.println("В заголовках ответа есть cookies");
                messagePrinter.println("HeaderName = " + headerName);
                List<String> cookieStrings = headerFields.get(headerName);
                LinkedList<HttpCookie> poorCookies = new LinkedList<>();
                cookieStrings.forEach(s -> poorCookies.add(HttpCookie.parse(s).get(0)));
                httpCookies = CookieUtils.joinCookies(poorCookies, httpCookies, url, dbg);
                CookieUtils.writeCookies(httpCookies, url, dbg);
                messagePrinter.println("Записанные cookies = " + httpCookies);
            }

            messagePrinter.println("Заголовки ответа");
            headerFields.forEach((s, strings) -> {
                messagePrinter.println("\t" + s + "\t=\t" + strings);
            });

            //return connection.getInputStream();
            return new CombinatedData<>(connection.getHeaderFields(), connection.getInputStream());
        } catch (IOException e) {
            messagePrinter.println("Произошла неисправимая ошибка! Возвращаемое значение равно null");
            messagePrinter.println(Arrays.toString(e.getStackTrace()));
        } finally {
            messagePrinter.fire();
        }
        return null;
    }

    public static class CombinatedData<T, S> {
        private T value1;
        private S value2;

        public CombinatedData(T value1, S value2) {
            this.value1 = value1;
            this.value2 = value2;
        }

        public T getValue1() {
            return value1;
        }

        public S getValue2() {
            return value2;
        }
    }
}