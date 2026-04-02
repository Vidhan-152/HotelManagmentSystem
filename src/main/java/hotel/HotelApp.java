package hotel;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HotelApp extends Application {
    private final HotelService service = new HotelService();
    private Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        showAuthScene();
        primaryStage.show();
    }

    void showAuthScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(HotelApp.class.getResource("/hotel/auth.fxml"));
        Scene scene = new Scene(loader.load(), 980, 680);
        scene.getStylesheets().add(HotelApp.class.getResource("/hotel/app.css").toExternalForm());

        AuthController controller = loader.getController();
        controller.initializeApp(this, service);

        primaryStage.setTitle("Grand Vista Hotel Management System");
        primaryStage.setMinWidth(920);
        primaryStage.setMinHeight(640);
        primaryStage.setScene(scene);
    }

    void showDashboardScene(UserAccount account) throws Exception {
        FXMLLoader loader = new FXMLLoader(HotelApp.class.getResource("/hotel/dashboard.fxml"));
        Scene scene = new Scene(loader.load(), 1320, 820);
        scene.getStylesheets().add(HotelApp.class.getResource("/hotel/app.css").toExternalForm());

        DashboardController controller = loader.getController();
        controller.initializeApp(this, service, account);

        primaryStage.setTitle("Grand Vista Hotel Management System");
        primaryStage.setMinWidth(1080);
        primaryStage.setMinHeight(720);
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
