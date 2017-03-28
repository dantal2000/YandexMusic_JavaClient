package app;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import structure.Track;
import utils.ImageLoader;

public class TrackAdder {
    private Pane controlPane;
    private int count = 0;

    private final int paneHeight = 128;
    private final String loadImageSize = "200x200";
    private final int titleLabelLayoutX = 128 + 33;
    private final int infoLabelLayoutY = paneHeight - 18 - 14;

    public TrackAdder(Pane controlPane) {
        this.controlPane = controlPane;
    }

    public TrackAdder(Pane controlPane, int count) {
        this.controlPane = controlPane;
        this.count = count;
    }

    public void add(Track track) {
        Pane trackPane = new Pane();

        trackPane.setMinHeight(paneHeight);
        trackPane.setMaxHeight(paneHeight);
        trackPane.prefWidthProperty().bind(controlPane.widthProperty());

        trackPane.setLayoutY(count * paneHeight);

        trackPane.getChildren().add(getImage(track.getCoverUri(), track.getId()));
        trackPane.getChildren().add(getLine());
        trackPane.getChildren().add(getTitleLabel(track.getTitle()));
        trackPane.getChildren().add(getInfoLabel(track.getDurationMs(), track.getArtistName()));

        track.setTrackPane(trackPane);
        controlPane.getChildren().add(trackPane);
        count++;
    }

    private BorderPane getImage(String coverUri, int trackId) {
        BorderPane centeredImageView = new BorderPane();
        Image image;
        ImageView imageView;
        if (coverUri == null) {
            image = ImageLoader.findInCache(trackId);
            if (image == null)
                image = new Image(getClass().getResourceAsStream("img/loading.gif"));
            imageView = new ImageView(image);
        } else {
            image = ImageLoader.retrieveImage(coverUri, loadImageSize, trackId);
            imageView = new ImageView(image);
        }

        imageView.setFitHeight(paneHeight);
        imageView.setFitWidth(paneHeight);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        centeredImageView.setCenter(imageView);
        return centeredImageView;
    }

    private Line getLine() {
        int layoutY = paneHeight - 1;
        Line line = new Line(0, layoutY, 0, layoutY);
        line.endXProperty().bind(controlPane.widthProperty());
        return line;
    }

    private Label getTitleLabel(String title) {
        Label titleLabel = new Label();
        titleLabel.setFont(Font.font("Verdana", 20));
        titleLabel.setLayoutY(18);
        titleLabel.setLayoutX(titleLabelLayoutX);
        if (title == null) {
            titleLabel.setText("NaN");
        } else {
            titleLabel.setText(title);
        }
        return titleLabel;
    }

    private Label getInfoLabel(long durationMs, String artistName) {
        long durationSeconds = durationMs / 1000;
        long mins = durationSeconds / 60;
        long secs = durationSeconds % 60;
        String durationString = mins + ":" + (secs > 9 ? secs : "0" + secs);

        Label infoLabel = new Label();
        infoLabel.setFont(Font.font("Verdana", 14));
        infoLabel.setLayoutY(infoLabelLayoutY);
        infoLabel.setLayoutX(titleLabelLayoutX);

        if (artistName == null) {
            infoLabel.setText(durationString + " | NaN");
        } else {
            infoLabel.setText(durationString + " | " + artistName);
        }
        return infoLabel;
    }
}
