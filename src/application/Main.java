package application;

import gui.MainWindowController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import util.Alerts;

public class Main extends Application {
	
	private static boolean isClosed = false;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/MainWindowView.fxml"));
			VBox vBox = loader.load();
			stage.setScene(new Scene(vBox));
			stage.setResizable(false);
			stage.setTitle("TikTok Live Monitor");
			MainWindowController controller = loader.getController();
			stage.setOnCloseRequest(e -> controller.close());
			stage.show();
		}
		catch (Exception e) {
			e.printStackTrace();
			Alerts.exception("Erro", "Erro ao iniciar o programa", e.getMessage(), e);
		}
	}

	public static boolean isClosed()
		{ return isClosed; }

}
