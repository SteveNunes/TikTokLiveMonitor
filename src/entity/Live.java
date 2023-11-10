package entity;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.github.jwdeveloper.tiktok.data.models.gifts.Gift;
import io.github.jwdeveloper.tiktok.live.LiveClient;

public class Live {

	private static LiveClient liveClient;
	private static String username;
	private static boolean isOnline;
	private static Map<Date, Map<Gift, Integer>> gifts;
	private static List<String> chatBuffer;
	private static Date today;
	
}
