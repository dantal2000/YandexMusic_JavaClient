package app;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import structure.Track;
import utils.WaitingThread;

import java.util.Collections;
import java.util.LinkedList;

public class Player {
    private LinkedList<Track> tracks;

    private Track currentTrack;
    private int currentTrackId;

    private MediaPlayer currentMediaPlayer;
    private InfoBar infoBar;

    private WaitingThread waitingThread;

    public Player(Pane playerPane, LinkedList<Track> tracks) {
        this.tracks = tracks;

        if (tracks.size() == 0) throw new NullPointerException("tracks.size() == 0");
        else currentTrackId = 0;

        Pane controlBar = ControlBar.getBar();
        controlBar.setLayoutX(31);
        controlBar.setLayoutY(12);
        ControlBar.previous.setOnMouseClicked(event -> {
            if (currentTrackId > 0) playPreviousTrack();
        });
        ControlBar.current.setOnMouseClicked(event -> {
            if (currentMediaPlayer != null) {
                switch (currentMediaPlayer.getStatus()) {
                    case PAUSED:
                        resumeCurrentTrack();
                        break;
                    case STOPPED:
                        playCurrentTrack();
                        break;
                    default:
                        pauseCurrentTrack();
                        break;
                }
            } else playCurrentTrack();
        });
        ControlBar.next.setOnMouseClicked(event -> {
            if (currentTrackId < tracks.size() - 1)
                playNextTrack();
        });

        infoBar = new InfoBar();
        Pane infoBarContainer = new Pane();
        int infoBarContainerLayoutX = 159 + 31 + 30;
        infoBarContainer.setLayoutX(infoBarContainerLayoutX);
        infoBarContainer.prefWidthProperty().bind(playerPane.widthProperty().subtract(infoBarContainerLayoutX));

        infoBar.setContainer(infoBarContainer);
        Platform.runLater(() -> playerPane.getChildren().addAll(controlBar, infoBarContainer));
    }

    public void playCurrentTrack() {
        if (currentTrack != null)
            currentTrack.getTrackPane().setBackground(new Background(new BackgroundFill(Color.valueOf("#e6e6e6"), CornerRadii.EMPTY, Insets.EMPTY)));
        currentTrack = tracks.get(currentTrackId);
        if (currentMediaPlayer != null) currentMediaPlayer.stop();
        infoBar.removeTrack();

        Pane trackPane = currentTrack.getTrackPane();
        trackPane.setBackground(new Background(new BackgroundFill(Color.LIMEGREEN, CornerRadii.EMPTY, Insets.EMPTY)));

        if (waitingThread == null) setWaitingThread(new WaitingThread());
        waitingThread.setWorking(() -> {
            Media music = currentTrack.getMusic();
            currentMediaPlayer = new MediaPlayer(music);
            currentMediaPlayer.setOnReady(() -> {
                infoBar.setTrack(currentTrack.getTitle(), currentMediaPlayer);
                currentMediaPlayer.play();
            });
            currentMediaPlayer.setOnEndOfMedia(this::playNextTrack);
        });
        waitingThread.fire();
    }

    public void playNextTrack() {
        if (currentTrackId < tracks.size() - 1) {
            currentTrackId++;
            playCurrentTrack();
        }
    }

    public void playPreviousTrack() {
        if (currentTrackId > 0) {
            currentTrackId--;
            playCurrentTrack();
        }
    }

    public void pauseCurrentTrack() {
        currentMediaPlayer.pause();
    }

    public void resumeCurrentTrack() {
        currentMediaPlayer.play();
    }

    public void setWaitingThread(WaitingThread waitingThread) {
        this.waitingThread = waitingThread;
        final WaitingThread.Action opening = waitingThread.getOpening();
        final WaitingThread.Action closing = waitingThread.getClosing();
        waitingThread.setOpening(() -> {
            if (opening != null) opening.action();
            ControlBar.blockBar();
        });
        waitingThread.setClosing(() -> {
            if (closing != null) closing.action();
            ControlBar.activateBar();
        });
    }

    public void setCurrentTrack(Track track) {
        if (tracks.contains(track)) {
            currentTrackId = tracks.indexOf(track);
            playCurrentTrack();
        }
    }

    private static class InfoBar {
        private Slider playSlider = new Slider(0, 100, 0);
        private Slider volumeSlider = new Slider(0, 1, 1);
        private BorderPane volumePane = new BorderPane(volumeSlider);
        private Label titleLabel = new Label("NaN");

        {
            titleLabel.setFont(Font.font("Verdana", 14));
            playSlider.setLayoutY(35);
            volumeSlider.setOrientation(Orientation.VERTICAL);

            volumePane.setMinSize(30, 30);
            volumePane.setMaxSize(30, 30);
            volumePane.setBackground(new Background(new BackgroundFill(Color.LIGHTCYAN, CornerRadii.EMPTY, Insets.EMPTY)));
            volumePane.setLayoutY(13);
        }

        public void setContainer(Pane container) {
            playSlider.prefWidthProperty().bind(container.widthProperty().subtract(65));
            volumePane.layoutXProperty().bind(playSlider.widthProperty().add(17));

            container.getChildren().addAll(titleLabel, playSlider, volumePane);
        }

        public void setTrack(String trackTitle, MediaPlayer player) {
            if (trackTitle != null) titleLabel.setText(trackTitle);
            double duration = player.getMedia().getDuration().toSeconds();
            ChangeListener<Duration> durationChangeListener = (observable, oldValue, newValue) ->
                    playSlider.setValue(newValue.toSeconds() / duration * 100);
            player.currentTimeProperty().addListener(durationChangeListener);

            playSlider.setOnMousePressed(event ->
                    player.currentTimeProperty().removeListener(durationChangeListener));
            playSlider.setOnMouseReleased(event -> {
                player.seek(Duration.seconds(playSlider.getValue() * duration / 100));
                player.currentTimeProperty().addListener(durationChangeListener);
            });
            player.volumeProperty().bind(volumeSlider.valueProperty());
        }

        public void removeTrack() {
            titleLabel.setText("NaN");
            playSlider.setValue(0);
        }
    }

    private static class ControlBar {
        private static BorderPane previous = new BorderPane(), current = new BorderPane(), next = new BorderPane();
        private static Pane container = new Pane();

        static {
            container.setMinSize(159, 36);
            container.setMaxSize(159, 36);

            LinkedList<BorderPane> buttons = new LinkedList<>();
            Collections.addAll(buttons, previous, current, next);

            BackgroundFill hoverFill = new BackgroundFill(Color.LIGHTCYAN, CornerRadii.EMPTY, Insets.EMPTY);
            BackgroundFill fill = new BackgroundFill(Color.valueOf("#cdcdcd"), CornerRadii.EMPTY, Insets.EMPTY);
            for (int i = 0; i < buttons.size(); i++) {
                BorderPane button = buttons.get(i);
                button.setMaxSize(45, 36);
                button.setMinSize(45, 36);
                button.setLayoutX(i * (45 + 12));
                button.setBackground(new Background(new BackgroundFill(Color.valueOf("#cdcdcd"), null, null)));

                button.setOnMouseEntered(event -> button.setBackground(new Background(hoverFill)));
                button.setOnMouseExited(event -> button.setBackground(new Background(fill)));

                String spritePath = "";
                switch (i) {
                    case 0:
                        spritePath = "img/back_shift.png";
                        break;
                    case 1:
                        spritePath = "img/start_shift.png";
                        break;
                    case 2:
                        spritePath = "img/next_shift.png";
                        break;
                }

                Image buttonImage = new Image(Player.class.getResourceAsStream(spritePath));
                button.setCenter(new ImageView(buttonImage));
            }

            container.getChildren().addAll(previous, current, next);
        }

        public static Pane getBar() {
            return container;
        }

        public static void blockBar() {
            previous.setDisable(true);
            current.setDisable(true);
            next.setDisable(true);
        }

        public static void activateBar() {
            previous.setDisable(false);
            current.setDisable(false);
            next.setDisable(false);
        }
    }
}
