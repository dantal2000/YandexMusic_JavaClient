package structure;

import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import utils.MusicLoader;

public class Track {
    private int id, durationMs, albumId;
    private String title, coverUri, artistName;
    private Media music;
    private Pane trackPane;

    public Track(int id) {
        this.id = id;
    }

    public Track(int id, int durationMs, int albumId, String title, String coverUri, String artistName) {
        this.id = id;
        this.durationMs = durationMs;
        this.albumId = albumId;
        this.title = title;
        this.coverUri = coverUri;
        this.artistName = artistName;
    }

    public void loadMusic() {
        Media sMusic = MusicLoader.findInCache(id);
        if (sMusic == null)
            music = MusicLoader.loadMusic(id);
        else music = sMusic;
    }

    public Media getMusic() {
        if (music == null) loadMusic();
        return music;
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

    public void setTitle(String title) {
        this.title = title;
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
                ", albumId=" + albumId +
                '}';
    }

    public int getAlbumId() {
        return albumId;
    }
}