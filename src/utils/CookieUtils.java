package utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpCookie;
import java.net.URL;
import java.util.*;

public class CookieUtils {
    static public void writeCookies(List<HttpCookie> httpCookies, String url, PrintStream dbg) {
        DebugMessagePrinter messagePrinter = new DebugMessagePrinter(dbg);

        try {
            final String pathToFileCookies = "cache/cookies.xml";
            final File cookiesFile = new File(pathToFileCookies);
            if (cookiesFile.getParentFile().mkdirs() || cookiesFile.createNewFile())
                messagePrinter.println("Файл " + pathToFileCookies + " был создан");

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            messagePrinter.println("Главный контейнер " + document.hashCode() + " был создан");

            Element root = document.createElement("Cookies");
            XMLCookieParamCreater paramCreater = new XMLCookieParamCreater(document);
            for (HttpCookie httpCookie : httpCookies) {
                String shortUrl = url.contains("?") ? url.substring(0, url.indexOf("?")) : url;
                long maxAge = httpCookie.getMaxAge();
                String name = httpCookie.getName();
                String value = httpCookie.getValue();
                String urlHash = String.valueOf((new URL(shortUrl).getHost()).hashCode());
                String cookieHash = String.valueOf((maxAge + name + value).hashCode());
                long currentTime = System.currentTimeMillis();

                messagePrinter.println("Cookie " + cookieHash + " = {");
                messagePrinter.println("\tShortUrl\t= " + shortUrl + ",");
                messagePrinter.println("\tMaxAge\t\t= " + maxAge + ",");
                messagePrinter.println("\tName\t\t= " + name + ",");
                messagePrinter.println("\tValue\t\t= " + value + ",");
                messagePrinter.println("\tCurrentTime\t= " + currentTime + ",");
                messagePrinter.println("\tUrlHash\t\t= " + urlHash);
                messagePrinter.println("}");

                Element container = document.createElement("cookie");
                paramCreater.addParametr(container, "name", name);
                paramCreater.addParametr(container, "value", value);
                paramCreater.addParametr(container, "maxAge", maxAge);
                paramCreater.addParametr(container, "createTime", currentTime);
                paramCreater.addParametr(container, "urlHash", urlHash);
                paramCreater.addParametr(container, "cookieHash", cookieHash);
                root.appendChild(container);
                messagePrinter.println("Cookie " + cookieHash + " укомплектованно");
            }
            document.appendChild(root);
            messagePrinter.println("Корень cookies " + root.hashCode() + " добавлен в главный контейнер " + document.hashCode());

            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty(OutputKeys.METHOD, "xml");
            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            tr.transform(new DOMSource(document), new StreamResult(new FileOutputStream(cookiesFile)));
            messagePrinter.println("Файл " + pathToFileCookies + " записан");
        } catch (IOException | ParserConfigurationException | TransformerException e) {
            messagePrinter.println("Произошла неисправимая ошибка");
            messagePrinter.println(Arrays.toString(e.getStackTrace()));
        } finally {
            messagePrinter.fire();
        }
    }

    static public void writeCookies(List<HttpCookie> httpCookies, String url) {
        writeCookies(httpCookies, url, null);
    }

    public static List<HttpCookie> readCookies(String url, PrintStream dbg) {
        DebugMessagePrinter messagePrinter = new DebugMessagePrinter(dbg);
        try {
            String shortUrl = url.contains("?") ? url.substring(0, url.indexOf("?")) : url;
            String urlHash = String.valueOf((new URL(shortUrl).getHost()).hashCode());

            messagePrinter.println("ShortUrl = " + shortUrl);
            messagePrinter.println("UrlHash = " + urlHash);

            final String pathToFileCookies = "cache/cookies.xml";
            final File cookiesFile = new File(pathToFileCookies);
            if (!cookiesFile.exists()) {
                messagePrinter.println("Файла " + pathToFileCookies + " не существует. Возврат нулевого значения");
                return null;
            }

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(cookiesFile);
            messagePrinter.println("Главный контейнер " + document.hashCode() + " был загружен");

            NodeList cookieContainerNodeList = document.getElementsByTagName("cookie");
            List<HttpCookie> cookieList = new LinkedList<>();

            for (int i = 0; i < cookieContainerNodeList.getLength(); i++) {
                Node containerNode = cookieContainerNodeList.item(i);
                if (containerNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element container = (Element) containerNode;
                    Map<String, String> cookieParams = new HashMap<>();
                    NodeList cookieParamsNodeList = container.getChildNodes();
                    for (int j = 0; j < cookieParamsNodeList.getLength(); j++) {
                        Node parameterNode = cookieParamsNodeList.item(j);
                        if (parameterNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element parameter = (Element) parameterNode;
                            cookieParams.put(parameter.getTagName(), parameter.getTextContent());
                        } /*else messagePrinter.println(parameterNode.hashCode() + " не является параметром Cookie");*/
                    }
                    if (!cookieParams.containsKey("urlHash") || !cookieParams.get("urlHash").equals(urlHash)) {
                        //messagePrinter.println("Исключение: urlHash не соответствует. Переход к следующему контейнеру");
                        continue;
                    }
                    if (!cookieParams.containsKey("name") || !cookieParams.containsKey("value")) {
                        //messagePrinter.println("Ошибка: отсутвуют обязательные параметры! Переход к следующему контейнеру");
                        continue;
                    }
                    HttpCookie httpCookie = new HttpCookie(cookieParams.get("name"), cookieParams.get("value"));
                    long maxAge = Long.parseLong(cookieParams.get("maxAge"));
                    if (maxAge != -1) {
                        long creatingCookieTime = Long.parseLong(cookieParams.get("createTime"));
                        long currentTime = System.currentTimeMillis();
                        long diff = currentTime - creatingCookieTime;
                        long totalMaxAge = maxAge - diff / 1000;
                        if (totalMaxAge > 0)
                            httpCookie.setMaxAge(totalMaxAge);
                    }
                    messagePrinter.println("Cookie " + httpCookie.hashCode() + " = {");
                    messagePrinter.println("\tName\t= " + httpCookie.getName() + ",");
                    messagePrinter.println("\tValue\t= " + httpCookie.getValue() + ",");
                    messagePrinter.println("\tMaxAge\t= " + httpCookie.getMaxAge());
                    messagePrinter.println("}");
                    cookieList.add(httpCookie);
                    messagePrinter.println("Cookie " + httpCookie.hashCode() + " было добавлено в лист " + cookieList.hashCode());
                } else messagePrinter.println(containerNode.hashCode() + " не является Cookie-контейнером");
            }
            return cookieList;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            messagePrinter.println("Произошла неисправимая ошибка");
            messagePrinter.println(Arrays.toString(e.getStackTrace()));
        } finally {
            messagePrinter.fire();
        }
        return null;
    }

    public static List<HttpCookie> readCookies(String url) {
        return readCookies(url, null);
    }

    public static LinkedList<HttpCookie> joinCookies(List<HttpCookie> httpCookies_a, List<HttpCookie> httpCookies_b, String url, PrintStream dbg) {
        DebugMessagePrinter messagePrinter = new DebugMessagePrinter(dbg);
        try {
            List<HttpCookie> joinedCookies = new LinkedList<>();
            if (httpCookies_a != null) joinedCookies.addAll(httpCookies_a);
            if (httpCookies_b != null) joinedCookies.addAll(httpCookies_b);
            if (joinedCookies.size() == 0) return null;

            String shortUrl = url.contains("?") ? url.substring(0, url.indexOf("?")) : url;
            String urlHash = String.valueOf(shortUrl.hashCode());

            messagePrinter.println("ShortUrl = " + shortUrl);
            messagePrinter.println("UrlHash = " + urlHash);

            LinkedList<Integer> hashes = new LinkedList<>();
            LinkedList<Boolean> trully = new LinkedList<>();
            for (HttpCookie cookie : joinedCookies) {
                String value = cookie.getValue();
                String name = cookie.getName();
                int hashCookie = (urlHash + name + value).hashCode();

                messagePrinter.println("Cookie " + cookie.hashCode() + " Hash = " + hashCookie);
                hashes.add(hashCookie);
                trully.add(true);

                if (hashes.size() >= 2)
                    for (int i = 0; i < hashes.size() - 1; i++) {
                        if (hashes.get(i) == hashCookie || joinedCookies.get(i).getName().equals(name)) {
                            if (cookie.getMaxAge() > joinedCookies.get(i).getMaxAge()) {
                                trully.set(hashes.size() - 1, true);
                                trully.set(i, false);
                            } else {
                                trully.set(i, true);
                                trully.set(hashes.size() - 1, false);
                            }
                        }
                    }
            }
            messagePrinter.println("Таблица истинности: " + trully);
            LinkedList<HttpCookie> clearList = new LinkedList<>();
            for (int i = 0; i < trully.size(); i++) {
                if (trully.get(i))
                    clearList.add(joinedCookies.get(i));
            }
            return clearList;
        } finally {
            messagePrinter.fire();
        }
    }

    public static LinkedList<HttpCookie> joinCookies(List<HttpCookie> httpCookies, String url, PrintStream dbg) {
        DebugMessagePrinter messagePrinter = new DebugMessagePrinter(dbg);
        try {
            List<HttpCookie> readedCookies = readCookies(url, dbg);
            return joinCookies(httpCookies, readedCookies, url, dbg);
        } finally {
            messagePrinter.fire();
        }
    }

    public static LinkedList<HttpCookie> joinCookies(List<HttpCookie> httpCookies, String url) {
        return joinCookies(httpCookies, url, null);
    }

    static class XMLCookieParamCreater {
        private Document document;

        public XMLCookieParamCreater(Document document) {
            this.document = document;
        }

        public void addParametr(Element container, String name, Object value) {
            if (value != null && !value.equals("")) {
                Element element = document.createElement(name);
                element.appendChild(document.createTextNode(value.toString()));
                container.appendChild(element);
            }
        }
    }
}

