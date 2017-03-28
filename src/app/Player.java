package app;

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import structure.Track;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class Player {
    private Pane playerPane;
    private LinkedList<Track> tracks;

    private ListIterator<Track> iterator;
    private Track currentTrack;
    private boolean playingNow;

    public Player(Pane playerPane, LinkedList<Track> tracks) {
        this.playerPane = playerPane;
        this.tracks = tracks;
        iterator = tracks.listIterator(0);
        if (iterator.hasNext()) currentTrack = iterator.next();
        playingNow = false;

        BorderPane prevTrackButton = new BorderPane();
        BorderPane startTrackButton = new BorderPane();
        BorderPane nextTrackButton = new BorderPane();

        List<BorderPane> buttons = new LinkedList<>();
        Collections.addAll(buttons, prevTrackButton, startTrackButton, nextTrackButton);
        BackgroundFill fill = new BackgroundFill(Color.valueOf("#cdcdcd"), null, null);

        for (int i = 0; i < buttons.size(); i++) {
            BorderPane button = buttons.get(i);
            button.setBackground(new Background(fill));

            int width = 45;
            button.setMinWidth(width);
            button.setMaxWidth(width);

            int space = 12;
            button.setLayoutX(i * (width + space));

            int height = 36;
            button.setMaxHeight(height);
            button.setMinHeight(height);

            String imagePath;
            switch (i) {
                case 0:
                    imagePath = "img/back_shift.png";
                    break;
                case 1:
                    imagePath = "img/start_shift.png";
                    break;
                case 2:
                    imagePath = "img/next_shift.png";
                    break;
                default:
                    imagePath = "img/loading.gif";
            }
            ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream(imagePath)));
            button.setCenter(imageView);
            BackgroundFill hoverFill = new BackgroundFill(Color.LIGHTCYAN, null, null);
            button.setOnMouseEntered(event -> button.setBackground(new Background(hoverFill)));
            button.setOnMouseExited(event -> button.setBackground(new Background(fill)));
        }

        startTrackButton.setOnMouseClicked(event -> {
            if (currentTrack != null)
                start();
        });
        prevTrackButton.setOnMouseClicked(event -> prevTrack());
        nextTrackButton.setOnMouseClicked(event -> nextTrack());

        Pane buttonBar = new Pane(prevTrackButton, startTrackButton, nextTrackButton);
        buttonBar.setLayoutX(31);
        buttonBar.setLayoutY(12);
        playerPane.getChildren().add(buttonBar);

        Pane infoBarContainer = new Pane();
        int space = 30;
        infoBarContainer.setLayoutX(220);
        infoBarContainer.setMinHeight(playerPane.getMinHeight());
        infoBarContainer.setMaxHeight(playerPane.getMinHeight());
        infoBarContainer.prefWidthProperty().bind(playerPane.widthProperty().subtract(infoBarContainer.getLayoutX() + 15));
        playerPane.getChildren().add(infoBarContainer);

        infoBar = new InfoBar(infoBarContainer);
        if (currentTrack == null || currentTrack.getTitle() == null) infoBar.setTitle("NaN");
        else
            infoBar.setTitle(currentTrack.getTitle());
    }

    private InfoBar infoBar;
    private MediaPlayer currentMediaPlayer;

    private void start() {
        if (currentMediaPlayer == null) {
            currentMediaPlayer = new MediaPlayer(currentTrack.getMusic());
        }
        if (!playingNow) {
            playingNow = true;
            if (currentMediaPlayer.getStatus() != MediaPlayer.Status.PAUSED) {
                currentMediaPlayer = new MediaPlayer(currentTrack.getMusic());
                currentMediaPlayer.setOnReady(() -> {
                    infoBar.bindSlider(currentMediaPlayer);
                    infoBar.setTitle(currentTrack.getTitle());
                    currentMediaPlayer.play();
                    currentMediaPlayer.setOnEndOfMedia(() -> {
                        if (nextPlay)
                            nextTrack();
                        playingNow = false;
                    });
                });
            } else {
                currentMediaPlayer.play();
            }
        } else {
            currentMediaPlayer.pause();
            playingNow = false;
        }

    }

    private boolean nextPlay = true;

    private void nextTrack() {
        nextPlay = true;
        if (iterator.hasNext()) {
            currentTrack = iterator.next();
            start();
        }
    }

    private void prevTrack() {
        nextPlay = false;
        if (iterator.hasPrevious()) {
            currentTrack = iterator.previous();
            start();
        }
    }

    static class InfoBar {
        Label titleLabel = new Label();
        Slider durationSlider = new Slider();

        public InfoBar(Pane container) {
            titleLabel.setFont(Font.font("Verdana", 14));
            durationSlider.setLayoutY(35);
            durationSlider.prefWidthProperty().bind(container.widthProperty().subtract(65));
            container.getChildren().addAll(titleLabel, durationSlider);
        }

        public void bindSlider(MediaPlayer player) {
            durationSlider.setMin(0);
            double duration = player.getMedia().getDuration().toSeconds();
            durationSlider.setMax(100);
            player.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                if (!durationSlider.isPressed())
                    durationSlider.setValue(newValue.toSeconds() / duration * 100);
            });
            durationSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (durationSlider.isPressed()) {
                    player.seek(Duration.seconds(newValue.doubleValue() * duration / 100));
                }
            });
        }

        public void setTitle(String title) {
            titleLabel.setText(title);
        }
    }
}
