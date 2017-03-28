package develApp;

import app.Player;
import app.TrackAdder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import structure.Track;
import utils.MusicLoader;

import java.util.LinkedList;

public class Launcher extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        Scene scene = new Scene(root, 527, 555);
        primaryStage.setScene(scene);
        primaryStage.setMinHeight(594);
        primaryStage.setMinWidth(543);
        primaryStage.setTitle("Player v 0.1 alpha");
        createUI(root, scene);
        primaryStage.show();
    }

    private BorderPane getAnimationOfLoading() {
        BorderPane centeredImageView = new BorderPane();
        Image image = new Image(getClass().getResourceAsStream("img/loading.gif"));
        ImageView imageView = new ImageView(image);
        centeredImageView.setCenter(imageView);
        return centeredImageView;
    }

    private void createUI(Pane root, Scene scene) {
        BorderPane animationOfLoadingPane = getAnimationOfLoading();
        animationOfLoadingPane.prefWidthProperty().bind(scene.widthProperty());
        animationOfLoadingPane.prefHeightProperty().bind(scene.heightProperty());
        root.getChildren().add(animationOfLoadingPane);

        Thread thread = new Thread(() -> {
            Pane container = new Pane();
            ScrollPane upSide = new ScrollPane();
            Pane downSide = new Pane();
            container.getChildren().addAll(upSide, downSide);

            int downHeight = 62;
            downSide.setMaxHeight(downHeight);
            downSide.setMinHeight(downHeight);
            downSide.layoutYProperty().bind(scene.heightProperty().subtract(downHeight));
            downSide.prefWidthProperty().bind(scene.widthProperty());
            downSide.setBackground(new Background(new BackgroundFill(Color.DARKGREY, null, null)));

            upSide.prefWidthProperty().bind(scene.widthProperty());
            upSide.prefHeightProperty().bind(scene.heightProperty().subtract(downHeight));
            Background upSideBackground = new Background(new BackgroundFill(Color.valueOf("#e6e6e6"), null, null));
            upSide.setBackground(upSideBackground);
            upSide.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scene.getStylesheets().add(getClass().getResource("css/scroll_pane.css").toExternalForm());

            Pane scrollListContent = new Pane();
            scrollListContent.setBackground(upSideBackground);
            scrollListContent.prefWidthProperty().bind(scene.widthProperty());
            TrackAdder trackAdder = new TrackAdder(scrollListContent);

            LinkedList<Track> tracks = new LinkedList<>();
            int id = 34402006;
            Track track = new Track(id);
            track.setMusic(MusicLoader.findInCache(id));
            tracks.add(track);
            tracks.forEach(trackAdder::add);

            upSide.setContent(scrollListContent);
            Player player = new Player(downSide, tracks);

            Platform.runLater(() -> {
                root.getChildren().remove(animationOfLoadingPane);
                root.getChildren().add(container);
            });
        });
        thread.setDaemon(true);
        thread.start();
    }
}
