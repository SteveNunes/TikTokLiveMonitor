package entity;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import application.Main;
import enums.TTSNameSpeechStyle;
import gui.TTSConfigView;
import io.github.jwdeveloper.tiktok.data.models.users.User;
import util.Misc;
import util.MyString;
import util.TextToSpeech;

public abstract class TTS {
	
	private static List<String> ttsBuffer = new LinkedList<>();
	
	public static void startTTS() {
		new Thread(() -> {
			while (!Main.isClosed()) {
				if (!ttsBuffer.isEmpty()) {
  				TextToSpeech.speech(ttsBuffer.get(0), 1.5f);
  				if (!ttsBuffer.isEmpty())
  					ttsBuffer.remove(0);
				}
				else
					Misc.sleep(100);
			}
		}).start();
	}

	public static void addText(User user, String text) {
		String name = fixNameForTTS(user.getProfileName().trim());
		if (TTSConfigView.getTtsNameSpeechStyle() == TTSNameSpeechStyle.FULL_NAME)
			name = fixNameForTTS(user.getProfileName());
		else if (TTSConfigView.getTtsNameSpeechStyle() == TTSNameSpeechStyle.FIRST_NAME) {
			String[] split = name.trim().split(" ");
			for (int n = 0; n < split.length; n++)
				if (!split[n].isEmpty()) {
					name = split[n];
					break;
				}					
		}
		else if (TTSConfigView.getTtsNameSpeechStyle() == TTSNameSpeechStyle.USER_NAME)
			name = fixNameForTTS(user.getName());
		else
			name = "";
		name = name.trim();
		text = fixMessageForTTS(text.trim());
		if (!text.isEmpty())
			ttsBuffer.add((name.isEmpty() ? "" : name + " falou: ") + text);
	}
	
	public static String removeNonAlphanumeric(String input) {
		String regex = "[^\sa-z0-9àáâãçèéêìíîñòóôõùúû]";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input.toLowerCase());
		String stringWithoutNonAlphanumeric = matcher.replaceAll("");
		return stringWithoutNonAlphanumeric;
	}

  public static String removeSpecialCharacters(String input) {
    String regex = "[^\\p{L}\\p{N}\\p{P}\\p{Z}]";
    Pattern pattern = Pattern.compile(regex, Pattern.UNICODE_CHARACTER_CLASS);
    Matcher matcher = pattern.matcher(input);
    String stringWithoutSpecialChars = matcher.replaceAll("");
    return stringWithoutSpecialChars;
  }
	
  private static String fixNameForTTS(String profileName) {
		profileName = MyString.removeEmojis(profileName);
		profileName = MyString.removeRepeatedChar(profileName);
		profileName = removeSpecialCharacters(profileName);
		profileName = removeNonAlphanumeric(profileName);
		String str = profileName.replaceAll("\\d", "").trim();
		if (!str.isEmpty())
			profileName = str;
		return profileName;
  }

	private static String fixMessageForTTS(String text) {
		text = MyString.removeEmojis(text);
		text = MyString.removeRepeatedChar(text);
		text = removeSpecialCharacters(text);
		return text;
	}

	public static void clearBuffer()
		{ ttsBuffer.clear(); }

}
