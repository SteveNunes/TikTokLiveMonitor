package gui;

import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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

	private static List<CheckBox> getCheckBoxes;
	private static String[] getCheckBoxesStr = {
		"checkBoxShowComments", "checkBoxShowFollows", "checkBoxShowJoins",
		"checkBoxShowLikes", "checkBoxShowQuestions", "checkBoxShowGifts", "checkBoxShowEmotes",
		"checkBoxShowShares", "checkBoxShowSubscribes", "checkBoxShowTimeStamp"};

	private static Map<String, Map<String, Boolean>> checkBoxesStates;
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		getCheckBoxes = new LinkedList<>(Arrays.asList(
				checkBoxShowComments, checkBoxShowFollows, checkBoxShowJoins, checkBoxShowLikes,
				checkBoxShowQuestions, checkBoxShowGifts, checkBoxShowEmotes,
				checkBoxShowShares, checkBoxShowSubscribes, checkBoxShowTimeStamp));
	}
	
	public static void openChatConfig(String liveUser) {
		loadFromDisk(liveUser);
		NewWindow newWindow = new NewWindow(Main.getWindow(), Modality.APPLICATION_MODAL, "/gui/ChatConfigView.fxml");
		newWindow.getStage().setTitle("Configurações do chat");
		newWindow.getStage().setResizable(false);
		for (CheckBox checkBox : getCheckBoxes)
			checkBox.setSelected(checkBoxesStates.get(liveUser).get(checkBox.getId()));
		newWindow.show();
	}

	public static void close() {
		for(String liveUser : checkBoxesStates.keySet()) {
			StringBuilder sb = new StringBuilder();
			for(String checkBox : checkBoxesStates.get(liveUser).keySet()) {
				if (!sb.isEmpty())
					sb.append(" ");
				sb.append(checkBoxesStates.get(liveUser).get(checkBox));
			}
			Main.getIni().write("CHECKBOXES", liveUser, sb.toString());
		}
	}
	
  public static boolean getShowComments(String liveUser)
  	{ return getCheckBoxesStates(liveUser, "checkBoxShowComments"); }

  public static boolean getShowFollows(String liveUser)
  	{ return getCheckBoxesStates(liveUser, "checkBoxShowFollows"); }
  
  public static boolean getShowJoins(String liveUser)
  	{ return getCheckBoxesStates(liveUser, "checkBoxShowJoins"); }
  
  public static boolean getShowGifts(String liveUser)	
  	{ return getCheckBoxesStates(liveUser, "checkBoxShowGifts"); }
  
  public static boolean getShowLikes(String liveUser)	
  	{ return getCheckBoxesStates(liveUser, "checkBoxShowLikes"); }

  public static boolean getShowEmotes(String liveUser)
  	{ return getCheckBoxesStates(liveUser, "checkBoxShowEmotes"); }
  
  public static boolean getShowQuestions(String liveUser)
  	{ return getCheckBoxesStates(liveUser, "checkBoxShowQuestions"); }

	public static boolean getShowShares(String liveUser)
  	{ return getCheckBoxesStates(liveUser, "checkBoxShowShares"); }
  
  public static boolean getShowSubscribes(String liveUser)
  	{ return getCheckBoxesStates(liveUser, "checkBoxShowSubscribes"); }
  
  public static boolean getShowTimeStamp(String liveUser)
  	{ return getCheckBoxesStates(liveUser, "checkBoxShowTimeStamp"); }

  private static Boolean getCheckBoxesStates(String liveUser, String checkBoxName) {
 		loadFromDisk(liveUser);
		return checkBoxesStates.get(liveUser).get(checkBoxName);
	}

	private static void loadFromDisk(String liveUser) {
  	if (checkBoxesStates == null)
			checkBoxesStates = new LinkedHashMap<>();
  	if (!checkBoxesStates.containsKey(liveUser)) {
			checkBoxesStates.put(liveUser, new LinkedHashMap<>());
			try {
				String[] split = Main.getIni().read("CHECKBOXES", liveUser).split(" ");
				for(int n = 0; n < split.length; n++) {
					String checkBox = getCheckBoxesStr[n];
					checkBoxesStates.get(liveUser).put(checkBox, Boolean.parseBoolean(split[n]));
				}
			}
			catch (Exception e) {
				for(int n = 0; n < getCheckBoxesStr.length; n++)
					checkBoxesStates.get(liveUser).put(getCheckBoxesStr[n], true);
			}
		}
	}
	
}
