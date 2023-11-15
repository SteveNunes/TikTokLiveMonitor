package application;

import gui.MainWindowController;
import gui.util.Alerts;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import util.IniFile;
import util.TextToSpeech;

public class Main extends Application {
	
	private static IniFile iniFile = IniFile.getNewIniFileInstance("config.ini");
	private static boolean isClosed = false;
	private static Stage mainStage;
	
	public static void main(String[] args) {
		TextToSpeech.setOutputTempFile("Z:\\");
		launch(args);
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/MainWindowView.fxml"));
			VBox vBox = loader.load();
			mainStage = stage;
			stage.setScene(new Scene(vBox));
			stage.setTitle("TikTok Live Monitor");
			MainWindowController controller = loader.getController();
			stage.setOnCloseRequest(e ->
				{ controller.close(); isClosed = true; });
			stage.show();
		}
		catch (Exception e) {
			e.printStackTrace();
			Alerts.exception("Erro", "Erro ao iniciar o programa", e.getMessage(), e);
		}
	}

	public static boolean isClosed()
		{ return isClosed; }
	
	public static Window getWindow()
		{ return mainStage.getScene().getWindow(); }
	
	public static IniFile getIni()
		{ return iniFile; }

}
