package structure;

import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import utils.Logger;
import utils.MusicLoader;

public class Track {
    private int id, durationMs;
    private String title, coverUri, artistName;
    private Media music;
    private Pane trackPane;

    public Track(int id) {
        this.id = id;
    }

    public Track(int id, int durationMs, String title, String coverUri, String artistName) {
        this.id = id;
        this.durationMs = durationMs;
        this.title = title;
        this.coverUri = coverUri;
        this.artistName = artistName;
    }

    public void loadMusic() {
        music = MusicLoader.loadMusic(id);
    }

    public Media getMusic() {
        if (music == null) loadMusic();
        return music;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMusic(Media music) {
        this.music = music;
    }

    public int getId() {
        return id;
    }

    public int getDurationMs() {
        return durationMs;
    }

    public String getTitle() {
        return title;
    }

    public String getCoverUri() {
        return coverUri;
    }

    public String getArtistName() {
        return artistName;
    }

    public Pane getTrackPane() {
        return trackPane;
    }

    public void setTrackPane(Pane trackPane) {
        this.trackPane = trackPane;
    }

    @Override
    public String toString() {
        return "Track{" +
                "id=" + id +
                ", durationMs=" + durationMs +
                ", title='" + title + '\'' +
                ", coverUri='" + coverUri + '\'' +
                ", artistName='" + artistName + '\'' +
                ", music=" + music +
                '}';
    }
}