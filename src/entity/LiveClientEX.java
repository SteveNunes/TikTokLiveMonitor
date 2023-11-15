package entity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Logger;

import io.github.jwdeveloper.tiktok.data.models.gifts.Gift;
import io.github.jwdeveloper.tiktok.listener.ListenersManager;
import io.github.jwdeveloper.tiktok.live.GiftManager;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.live.LiveRoomInfo;
import util.Misc;
import util.MyCalendar;
import util.MyFile;

public class LiveClientEX implements LiveClient {

	private LiveClient liveClient;
	private Map<Date, Map<Gift, Integer>> gifts;
	private List<String> chatBuffer;
	private int chatBufferLimit;
	private String userName;
	
	public LiveClientEX(LiveClient liveClient) {
		this.liveClient = liveClient;
		chatBufferLimit = 500;
		userName = getRoomInfo().getHostName();
		gifts = new LinkedHashMap<>();
		try {
			chatBuffer = new LinkedList<>(MyFile.readAllLinesFromFile("./chatBuffers/" + userName + ".txt"));
			if (chatBuffer == null)
				chatBuffer = new LinkedList<>();
		}
		catch (Exception e)
			{ chatBuffer = new LinkedList<>(); }
	}
	
	public LiveClient getLiveClient()
		{ return liveClient; }
	
	public void addGift(Gift gift, int quantity) {
		Date date = MyCalendar.getDateAtMidnight();
		if (!gifts.containsKey(date))
			gifts.put(date, new LinkedHashMap<>());
		if (!gifts.get(date).containsKey(gift))
			gifts.get(date).put(gift, 0);
		gifts.get(date).put(gift, gifts.get(date).get(gift) + quantity);
	}
	
	public List<Gift> getGiftsFromDate(Date date) {
		List<Gift> list = new LinkedList<>();
		date = MyCalendar.getDateAtMidnight(date);
		if (!gifts.containsKey(date))
			return list;
		for (Gift gift : gifts.get(date).keySet())
			list.add(gift);
		return list;
	}
	
	public List<Gift> getAllGifts() {
		List<Gift> list = new LinkedList<>();
		for (Date date : gifts.keySet())
			for (Gift gift : getGiftsFromDate(date))
				list.add(gift);
			return list;
	}
	
	public int getGiftQuantityFromDate(Gift gift, Date date)
		{ return gifts.get(date).get(gift); }
	
	public int getGiftQuantity(Gift gift) {
		int quantity = 0;
		for (Date date : gifts.keySet())
			quantity += getGiftQuantityFromDate(gift, date);
		return quantity;
	}
	
	public void addChat(String text) {
		chatBuffer.add(text);
		setChatBufferLimit(chatBufferLimit);
	}
	
	public List<String> getCharBuffer()
		{ return chatBuffer; }
	
	public void clearChatBuffer()
		{ chatBuffer.clear(); }
	
	public void setChatBufferLimit(int limit) {
		chatBufferLimit = limit;
		while (chatBuffer.size() > chatBufferLimit)
			chatBuffer.remove(0);
	}

	@Override
	public void connect()
		{ liveClient.connect(); }

	@Override
	public CompletableFuture<LiveClient> connectAsync()
		{ return liveClient.connectAsync(); }

	@Override
	public void connectAsync(Consumer<LiveClient> client)
		{ liveClient.connectAsync(client); }

	@Override
	public void disconnect()
		{ liveClient.disconnect(); }

	@Override
	public GiftManager getGiftManager()
		{ return liveClient.getGiftManager(); }

	@Override
	public ListenersManager getListenersManager()
		{ return liveClient.getListenersManager(); }

	@Override
	public Logger getLogger()
		{ return liveClient.getLogger(); }

	@Override
	public LiveRoomInfo getRoomInfo()
		{ return liveClient.getRoomInfo(); }

	public String getUserName()
		{ return userName; }
	
}
