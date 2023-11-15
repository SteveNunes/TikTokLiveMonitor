package gui;

import java.net.URL;
import java.util.ResourceBundle;

import enums.TTSNameSpeechStyle;
import javafx.fxml.Initializable;

public class TTSConfigView implements Initializable {
	
	private static TTSNameSpeechStyle ttsNameSpeechStyle = TTSNameSpeechStyle.FIRST_NAME;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		
	}

	public static TTSNameSpeechStyle getTtsNameSpeechStyle()
		{ return ttsNameSpeechStyle; }

	public static void setTtsNameSpeechStyle(TTSNameSpeechStyle ttsNameSpeechStyle)
		{ TTSConfigView.ttsNameSpeechStyle = ttsNameSpeechStyle; }
	
	

}
