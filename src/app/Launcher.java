package app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import structure.Track;
import structure.TrackLoader;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.LinkedList;

public class Launcher extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            System.setProperty("file.encoding","UTF-8");
            Field charset = Charset.class.getDeclaredField("defaultCharset");
            charset.setAccessible(true);
            charset.set(null,null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        int version = 8;
        String title = "Yandex Music. Version " + version;

        Pane root = new Pane();
        Scene scene = new Scene(root, 527, 555);
        primaryStage.setScene(scene);
        primaryStage.setTitle(title);

        setMinimumRootSize(primaryStage);
        primaryStage.show();

        createUI(scene, root);
    }

    private void setMinimumRootSize(Stage primaryStage) {
        double height = 594;
        double width = 543;

        primaryStage.setMinHeight(height);
        primaryStage.setMinWidth(width);
    }

    private void createUI(Scene scene, Pane root) {
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
            downSide.setBackground(new Background(new BackgroundFill(Color.DARKGREY, CornerRadii.EMPTY, Insets.EMPTY)));

            upSide.prefWidthProperty().bind(scene.widthProperty());
            upSide.prefHeightProperty().bind(scene.heightProperty().subtract(downHeight));
            Background upSideBackground = new Background(new BackgroundFill(Color.valueOf("#e6e6e6"), CornerRadii.EMPTY, Insets.EMPTY));
            upSide.setBackground(upSideBackground);
            upSide.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scene.getStylesheets().add(getClass().getResource("css/scroll_pane.css").toExternalForm());

            Pane scrollListContent = new Pane();
            scrollListContent.setBackground(upSideBackground);
            scrollListContent.prefWidthProperty().bind(scene.widthProperty());
            TrackAdder trackAdder = new TrackAdder(scrollListContent);

            LinkedList<Track> tracks = TrackLoader.loadTracks();
            tracks.forEach(trackAdder::add);

            upSide.setContent(scrollListContent);
            new Player(downSide, tracks);

            Platform.runLater(() -> {
                root.getChildren().remove(animationOfLoadingPane);
                root.getChildren().add(container);
            });
        });
        thread.setDaemon(true);
        thread.start();
    }

    private BorderPane getAnimationOfLoading() {
        BorderPane centeredImageView = new BorderPane();
        Image image = new Image(getClass().getResourceAsStream("img/loading.gif"));
        ImageView imageView = new ImageView(image);
        centeredImageView.setCenter(imageView);
        return centeredImageView;
    }
}
