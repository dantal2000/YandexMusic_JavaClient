package structure;

import org.json.JSONArray;
import org.json.JSONObject;
import utils.Connector;
import utils.DebugMessagePrinter;
import utils.StreamUtils;

import java.io.*;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrackLoader {
    public static LinkedList<Track> loadTracks(PrintStream dbg) {
        DebugMessagePrinter messagePrinter = new DebugMessagePrinter(dbg);
        try {
            InputStream pageStream = Connector.connect("https://music.yandex.ru", dbg);
            if (pageStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(pageStream));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null)
                    builder.append(line);
                String pageContent = builder.toString();
                String writtenPageContentPath = "cache/page.html";
                StreamUtils.writeToFile(writtenPageContentPath, pageContent, dbg);
                messagePrinter.println("Контент страницы отправлен на запись в файл " + writtenPageContentPath);

                Pattern scriptPattern = Pattern.compile("var\\s+Mu\\s+=\\s+(.+?);\\s+</script>");
                Matcher matcher = scriptPattern.matcher(pageContent);
                if (matcher.find()) {
                    String jsonString = matcher.group(1);
                    JSONObject jsonObject = new JSONObject(jsonString);
                    JSONArray tracksToPlay = jsonObject.getJSONObject("pageData").getJSONArray("days").
                            getJSONObject(0).getJSONArray("tracksToPlay");
                    LinkedList<Track> trackList = new LinkedList<>();
                    for (int i = 0; i < tracksToPlay.length(); i++) {
                        JSONObject trackJson = tracksToPlay.getJSONObject(i);

                        int id, durationMs, albumId;
                        String title, coverUri, artistName;

                        id = trackJson.getInt("id");
                        durationMs = trackJson.getInt("durationMs");
                        title = trackJson.getString("title");
                        coverUri = trackJson.getString("coverUri");

                        artistName = trackJson.getJSONArray("artists").getJSONObject(0).getString("name");
                        albumId = trackJson.getJSONArray("albums").getJSONObject(0).getInt("id");

                        Track track = new Track(id, durationMs, albumId, title, coverUri, artistName);
                        messagePrinter.println("Track #" + i + " = " + track.toString());
                        trackList.add(track);
                    }
                    return trackList;
                } else {
                    messagePrinter.println("Скрипт не найден. Возврат нудевого значения");
                }
            } else {
                messagePrinter.println("Соединение не установлено. Возврат нулевого значения");
                return null;
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            messagePrinter.println("Произошла неисправимая ошибка. Возврат нулевого значения");
            messagePrinter.println(e.getMessage());
            return null;
        } finally {
            messagePrinter.fire();
        }
    }

    public static LinkedList<Track> loadTracks() {
        return loadTracks(null);
    }
}
