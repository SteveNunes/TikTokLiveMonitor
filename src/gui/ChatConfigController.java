package gui;

import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import application.Main;
import gui.util.NewWindow;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.stage.Modality;

public class ChatConfigController implements Initializable {
	
	private static Map<String, Boolean> checkBoxesStates = null;
	private static ChatConfigController controller;
	
  @FXML
  private CheckBox checkBoxShowComments;
  @FXML
  private CheckBox checkBoxShowFollows;
  @FXML
  private CheckBox checkBoxShowJoins;
  @FXML
  private CheckBox checkBoxShowEmotes;
  @FXML
  private CheckBox checkBoxShowGifts;
  @FXML
  private CheckBox checkBoxShowLikes;
  @FXML
  private CheckBox checkBoxShowQuestions;
  @FXML
  private CheckBox checkBoxShowShares;
  @FXML
  private CheckBox checkBoxShowSubscribes;
  @FXML
  private CheckBox checkBoxShowTimeStamp;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		if (checkBoxesStates == null) {
			checkBoxesStates = new LinkedHashMap<>();
			for(CheckBox checkBox : getCheckBoxes())
				try
					{ checkBoxesStates.put(checkBox.getId(), Main.getIni().readAsBoolean("CHECKBOXES", checkBox.getId())); }
				catch (Exception e)
					{ checkBoxesStates.put(checkBox.getId(), true); }
		}
	}
	
	private void defineCheckBoxes() {
		for(CheckBox checkBox : getCheckBoxes()) {
			checkBox.selectedProperty().addListener((o, wasSelected, isSelected) ->
				{ checkBoxesStates.put(checkBox.getId(), isSelected); });
			checkBox.setSelected(checkBoxesStates.get(checkBox.getId()));
		}
	}
	
	public static void openChatConfig() {
		NewWindow newWindow = new NewWindow(Main.getWindow(), Modality.APPLICATION_MODAL, "/gui/ChatConfigView.fxml");
		controller = newWindow.getController();
		controller.defineCheckBoxes();
		newWindow.getStage().setTitle("Configurações do chat");
		newWindow.getStage().setResizable(false);
		newWindow.showAndWait();
	}

	private List<CheckBox> getCheckBoxes() {
		return Arrays.asList(checkBoxShowComments, checkBoxShowFollows,
				checkBoxShowJoins, checkBoxShowLikes, checkBoxShowQuestions,
				checkBoxShowGifts, checkBoxShowEmotes, checkBoxShowShares,
				checkBoxShowSubscribes, checkBoxShowTimeStamp);
	}
	
	public static void close()
		{ controller.closeConfig(); }
	
	public void closeConfig() {
		for(CheckBox checkBox : getCheckBoxes())
			Main.getIni().write("CHECKBOXES", checkBox.getId(), getCheckBoxesStates(checkBox.getId()).toString());
	}
	
  public static boolean getShowComments()
  	{ return getCheckBoxesStates("checkBoxShowComments"); }

  public static boolean getShowFollows()
  	{ return getCheckBoxesStates("checkBoxShowFollows"); }
  
  public static boolean getShowJoins()
  	{ return getCheckBoxesStates("checkBoxShowJoins"); }
  
  public static boolean getShowGifts()	
  	{ return getCheckBoxesStates("checkBoxShowGifts"); }
  
  public static boolean getShowLikes()	
  	{ return getCheckBoxesStates("checkBoxShowLikes"); }

  public static boolean getShowEmotes()
  	{ return getCheckBoxesStates("checkBoxShowEmotes"); }
  
  public static boolean getShowQuestions()
  	{ return getCheckBoxesStates("checkBoxShowQuestions"); }

	public static boolean getShowShares()
  	{ return getCheckBoxesStates("checkBoxShowShares"); }
  
  public static boolean getShowSubscribes()
  	{ return getCheckBoxesStates("checkBoxShowSubscribes"); }
  
  public static boolean getShowTimeStamp()
  	{ return getCheckBoxesStates("checkBoxShowTimeStamp"); }

  private static Boolean getCheckBoxesStates(String checkBoxName) {
		return checkBoxesStates == null ? true : checkBoxesStates.get(checkBoxName);
	}

}
