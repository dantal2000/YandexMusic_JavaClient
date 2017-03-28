package app;

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
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import structure.Track;
import structure.TrackLoader;

import java.util.LinkedList;

public class Launcher extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        int version = 7;
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
        PostMonitor closeAnimationMonitor = getAnimationMonitor(root, scene);

        NotifyThread thread = new NotifyThread(closeAnimationMonitor, () -> {
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
            upSide.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
            scene.getStylesheets().add(getClass().getResource("css/scroll_pane.css").toExternalForm());


            LinkedList<Track> trackList = TrackLoader.loadTracks();

            Pane scrollListContent = new Pane();
            scrollListContent.setBackground(upSideBackground);
            scrollListContent.prefWidthProperty().bind(scene.widthProperty());
            TrackAdder trackAdder = new TrackAdder(scrollListContent);
            trackList.forEach(trackAdder::add);

            upSide.setContent(scrollListContent);

            Player player = new Player(downSide, trackList);

            Platform.runLater(() -> root.getChildren().add(container));
        });
        thread.runThread();
    }

    private PostMonitor getAnimationMonitor(Pane root, Scene scene) {
        final BorderPane animationBorderPane = getLoadingAnimationBorderPane(scene);
        root.getChildren().add(animationBorderPane);
        String threadName = "Loading_Animation";
        PostMonitor loading_animation = new PostMonitor(() -> Platform.runLater(() -> {
            root.getChildren().remove(animationBorderPane);
        }), threadName);
        loading_animation.runMonitor();
        return loading_animation;
    }

    private BorderPane getLoadingAnimationBorderPane(Scene scene) {
        String gifPath = "img/loading.gif";

        Image loadingGif = new Image(getClass().getResourceAsStream(gifPath));
        ImageView animationView = new ImageView(loadingGif);

        BorderPane borderPane = new BorderPane(animationView);
        borderPane.prefWidthProperty().bind(scene.widthProperty());
        borderPane.prefHeightProperty().bind(scene.heightProperty());

        return borderPane;
    }
}
