package gui;

import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

import enums.Icons;
import gui.util.ControllerUtils;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import util.MyCalendar;

public class ChatController implements Initializable {
	
	private Map<TextArea, Integer> maxCaretPos = new LinkedHashMap<>();
	private Map<TextArea, Boolean> isCaretMaxScrolled = new LinkedHashMap<>();

	@FXML
  TextArea textAreaLiveInfos;
  @FXML
  TextArea textAreaLiveChat;
  @FXML
  TextArea textAreaJoins;
  @FXML
  Button buttonChatConfig;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		maxCaretPos.put(textAreaLiveChat, 0);
		maxCaretPos.put(textAreaJoins, 0);
		isCaretMaxScrolled.put(textAreaLiveChat, true);
		isCaretMaxScrolled.put(textAreaJoins, true);
		buttonChatConfig.setOnAction(e -> ChatConfigController.openChatConfig(mainWindowController().getCurrentSelectedLiveUser()));
		ControllerUtils.addIconToButton(buttonChatConfig, Icons.CONFIG.getValue(), 24, 24, 200);
		for (TextArea textArea : Arrays.asList(textAreaLiveChat, textAreaJoins))
			textArea.scrollTopProperty().addListener((obs, oldVal, newVal) -> {
		    try {
		    	double oldV = (Double)oldVal, newV = (Double)newVal;
		    	isCaretMaxScrolled.put(textArea, !(newV < oldV || newV < maxCaretPos.get(textArea)));
		    	if (newV > maxCaretPos.get(textArea))
		    		maxCaretPos.put(textArea, (int)newV);
		    } catch (Exception e) {}
			});
	}
	
	void clearChatTextArea()
		{ textAreaLiveChat.clear(); }
	
	void clearJoinsTextArea()
		{ textAreaJoins.clear(); }

	void setButtonChatConfigDisable(boolean state)
		{ buttonChatConfig.setDisable(state); }
	
	void addDirectlyToChatTextArea(String text)
		{ textAreaLiveChat.appendText(text); }

	void sendToJoinTextArea(Date date, String text) {
		Platform.runLater(() -> {
			if (!textAreaJoins.getText().isEmpty())
				textAreaJoins.appendText("\n");
  		textAreaJoins.appendText(MyCalendar.dateToString(date, "'['HH:mm:ss'] '") + text);
		});
	}

	void sendInfoToChat(LiveClient liveClient, Date date, String text, boolean forceChat)
		{ sendToChat(liveClient, date, "* " + text, forceChat); }
	
	void sendInfoToChat(LiveClient liveClient, String text, boolean forceChat)
		{ sendInfoToChat(liveClient, null, text, forceChat); }
	
	void sendInfoToChat(LiveClient liveClient, Date date, String text)
		{ sendToChat(liveClient, date, "* " + text, false); }
	
	void sendInfoToChat(LiveClient liveClient, String text)
		{ sendInfoToChat(liveClient, null, text, false); }
	
	void sendToChat(LiveClient liveClient, Date date, String text, boolean forceChat) {
		Platform.runLater(() -> {
			synchronized (textAreaLiveChat) {
				String str = MyCalendar.dateToString(date == null ? new Date() : date, "'['HH:mm:ss'] '") + text;
	  		if (forceChat || mainWindowController().isCurrentSelectedLive(liveClient)) {
	  			int caret = isCaretMaxScrolled.get(textAreaLiveChat) ? -1 : textAreaLiveChat.getCaretPosition();
	  			textAreaLiveChat.appendText(textAreaLiveChat.getText().isBlank() ? str : "\n\n" + str);
	  			if (caret > -1)
	  				textAreaLiveChat.selectPositionCaret(caret);
	  		}
	  		if (!forceChat)
	  			mainWindowController().getCurrentLiveClient().addChat(str);
			}
	 	});
	}
	
	private MainWindowController mainWindowController()
		{ return MainWindowController.getMainWindowController(); }

	void sendToChat(LiveClient liveClient, Date date, String text)
		{ sendToChat(liveClient, date, text, false); }

	public void setLiveChatMaxCaretPos(int value)
		{ maxCaretPos.put(textAreaLiveChat, value); }

}
