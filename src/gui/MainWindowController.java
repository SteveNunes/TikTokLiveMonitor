package gui;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;

import io.github.jwdeveloper.tiktok.TikTokLive;
import io.github.jwdeveloper.tiktok.data.models.gifts.Gift;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.models.ConnectionState;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import util.MyCalendar;

public class MainWindowController implements Initializable {
	
	/**
	 * Ativar os botões de ADICIONAR e REMOVER LIVE
	 * 
	 * Ativar o text area com infos da live
	 * 
	 * Fazer o método que retorna o valor do presente, pegar esse
	 * valor de uma fonte secundária criada por mim, caso o presente
	 * retorne null
	 * 
	 * Criar a classe de LiveClient que extende a classe original,
	 * e adicionar métodos e atributos extras
	 * 
	 * Fazer salvar todo o buffer do chat em txt, e limitar a quantidade
	 * máxima de linhas. Se entrar mais linhas, as mais antigas são
	 * excluidas.
	 * 
	 * Verificar se tem alguma forma de identificar batalha e se tá
	 * em double e talz
	 */
	
	private float coinValue = 0.025f;
	
  private static Map<LiveClient, List<String>> chatBuffers = new HashMap<>();
  private static Map<LiveClient, Map<Gift, Integer>> gifts = new HashMap<>();
	
	private List<String> livesUsername = new ArrayList<>(Arrays.asList(
			"anyzinha11",
	  	"flaviosphgamer",
			"valzinha5498",
			"samdraculax",
			"bruna_piasse",
			"kryticalmind",
			"karacomparetto",
			"patrickfernando69",
			"eajaymello",
			"anatoly_pranks",
			"maiconmask",
			"meninododronefpv",
			"toor.manpreet",
			"batra_naina",
			"lttqzd2002"
	 ));
	
  @FXML
  private TableView<LiveClient> tableViewLives;
  @FXML
  private TableColumn<LiveClient, LiveClient> tableColumnLive;
  @FXML
  private TableColumn<LiveClient, LiveClient> tableColumnLiveViewers;
  @FXML
  private TableView<Gift> tableViewGifts;
  @FXML
  private TableColumn<Gift, Gift> tableColumnGiftNome;
  @FXML
  private TableColumn<Gift, Gift> tableColumnGiftQuant;
  @FXML
  private TableColumn<Gift, Gift> tableColumnGiftVal;
  @FXML
  private TextArea textAreaLiveInfos;
  @FXML
  private TextArea textAreaLiveChat;
  @FXML
  private TextField textFieldJoins;
  @FXML
  private TextField textFieldGiftsTotalValue;
  @FXML
  private Button buttonAddLive;
  @FXML
  private Button buttonRemoveLive;
  
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		setLivesTableViewFactories();
		setGiftsTableViewFactories();
		for (Node node : Arrays.asList(textFieldJoins, textAreaLiveChat, textFieldGiftsTotalValue, textAreaLiveInfos))
			node.setStyle("-fx-font-size: 14px; -fx-font-family: \"Lucida Console\";");
		textAreaLiveChat.setWrapText(true);
		textAreaLiveInfos.setWrapText(true);
		textAreaLiveChat.textProperty().addListener((obs, oldText, newText) ->
			{ textAreaLiveChat.setScrollTop(Double.MAX_VALUE); });
		for (int n = 0; n < 4; n++)
			addLive(livesUsername.get(n));
	}
	
	private void connectToLive(LiveClient client) {
		try {
			Platform.runLater(() -> textAreaLiveChat.clear());
			sendInfoToChat(client, "Conectando á live de " + client.getRoomInfo().getHostName());
			client.connectAsync();
		}
		catch (Exception e)
			{ sendInfoToChat(client, "Não foi possível conectar á live de " + client.getRoomInfo().getHostName()); }
	}

	private void setLivesTableViewFactories() {
		tableViewLives.getSelectionModel().selectedItemProperty().addListener((o, oldValue, newValue) -> {
			if (newValue.getRoomInfo().getConnectionState() == ConnectionState.DISCONNECTED)
				connectToLive(newValue);
			else {
				if (chatBuffers.containsKey(newValue)) {
					StringBuilder sb = new StringBuilder();
					for (String s : chatBuffers.get(newValue))
						sb.append(sb.isEmpty() ? s : "\n\n" + s);
					Platform.runLater(() -> { 
						textAreaLiveChat.clear();
						textAreaLiveChat.appendText(sb.toString());
						tableViewGifts.getItems().clear();
						if (gifts.containsKey(getCurrentLiveClient())) {
							for (Gift gift : gifts.get(getCurrentLiveClient()).keySet())
								tableViewGifts.getItems().add(gift);
						}
						tableViewGifts.refresh();
	    			textFieldGiftsTotalValue.setText(totalCurrentGiftsValue());
					});
				}
			}
		});
		for (TableColumn<LiveClient, LiveClient> col : Arrays.asList(tableColumnLive, tableColumnLiveViewers)) {
			col.setStyle("-fx-font-size: 14px; -fx-font-family: \"Lucida Console\";");
			col.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
			col.setCellFactory(lv -> {
				TableCell<LiveClient, LiveClient> cell = new TableCell<>() {
					@Override
					protected void updateItem(LiveClient client, boolean empty) {
						super.updateItem(client, empty);
						if (client != null) {
							if (col.getId().equals("tableColumnLive"))
								setText(client.getRoomInfo().getHostName());
							else if (col.getId().equals("tableColumnLiveViewers"))
								setText("" + client.getRoomInfo().getViewersCount());
						}
						else
							setGraphic(null);
					}
				};
				return cell;
			});
		}
	}
	
	private void setGiftsTableViewFactories() {
		tableViewGifts.getSelectionModel().selectedItemProperty().addListener((o, oldValue, newValue) -> {
			// TODO
		});
		for (TableColumn<Gift, Gift> col : Arrays.asList(tableColumnGiftNome, tableColumnGiftQuant, tableColumnGiftVal)) {
			col.setStyle("-fx-font-size: 14px; -fx-font-family: \"Lucida Console\";");
			col.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
			col.setCellFactory(lv -> {
				TableCell<Gift, Gift> cell = new TableCell<>() {
					@Override
					protected void updateItem(Gift gift, boolean empty) {
						super.updateItem(gift, empty);
						if (gift != null) {
							if (col.getId().equals("tableColumnGiftNome"))
								setText(gift.getName());
							else if (col.getId().equals("tableColumnGiftQuant"))
								setText("" + gifts.get(getCurrentLiveClient()).get(gift));
							else if (col.getId().equals("tableColumnGiftVal"))
								setText(textGiftValue(gift));
						}
						else
							setGraphic(null);
					}
				};
				return cell;
			});
		}
	}
	
	private LiveClient getCurrentLiveClient()
		{ return tableViewLives.getSelectionModel().getSelectedItem(); }

	private float giftValue(Gift gift)
		{ return gifts.get(getCurrentLiveClient()).get(gift) * gift.getDiamondCost() * coinValue; }
	
	private String textGiftValue(Gift gift)
		{ return String.format("%.2f", giftValue(gift)); } 

	public LiveClient addLive(String liveID) {
		try {
			LiveClient client = TikTokLive.newClient(liveID)
				.configure((settings) -> {
	        settings.setClientLanguage("pt");
	        settings.setLogLevel(Level.OFF);
	        settings.setPrintToConsole(false);
	        settings.setHandleExistingEvents(true);
	        settings.setRetryOnConnectionFailure(true);
	        settings.setRetryConnectionTimeout(Duration.ofSeconds(30));
		    })
	    	.onConnected((liveClient, event) -> {
    			if (!isCurrentSelectedLive(liveClient))
	    			sendInfoToChat(liveClient, "[NOTIFY] " + liveID + " está ao vivo agora mesmo.", true);
	    		else
	    			sendInfoToChat(liveClient, "Conectado com sucesso á live de " + liveID);
	    		tableViewLives.refresh();
	    	})
	    	.onError((liveClient, event) -> {
	    	})
	    	.onDisconnected((liveClient, event) -> {
    			if (isCurrentSelectedLive(liveClient))
	    			sendInfoToChat(liveClient, liveID + " encerrou a live", true);
	    		else
	    			sendInfoToChat(liveClient, "[NOTIFY] " + liveID + " encerrou a live", true);
	    	})
	    	.onLivePaused((liveClient, event) -> {
    			sendInfoToChat(liveClient, liveID + " pausou a live");
	    	})
	    	.onComment((liveClient, event) -> {
    			sendToChat(liveClient, new Date(event.getTimeStamp()), "<" + event.getUser().getName() + "> " + event.getText());
	    	})
	    	.onGift((liveClient, event) -> {
	    		sendInfoToChat(liveClient, new Date(event.getTimeStamp()), event.getUser().getName() + " enviou presente (" + event.getGift().getName() + " x" + event.getCombo() + ")");
	    		Platform.runLater(() -> {
	    			if (!gifts.containsKey(liveClient))
	    				gifts.put(liveClient, new HashMap<>());
		    		if (!gifts.get(liveClient).containsKey(event.getGift())) {
		    			gifts.get(liveClient).put(event.getGift(), 0);
		    			if (isCurrentSelectedLive(liveClient))
		    				tableViewGifts.getItems().add(event.getGift());
		    		}
		    		gifts.get(liveClient).put(event.getGift(), gifts.get(liveClient).get(event.getGift()) + event.getCombo());
	    			tableViewGifts.refresh();
	    			textFieldGiftsTotalValue.setText(totalCurrentGiftsValue());
	    		});
	    	})
	    	.onJoin((liveClient, event) -> {
	    		if (isCurrentSelectedLive(liveClient))
		    		synchronized (tableViewLives) {
			    		synchronized (textFieldJoins) {
				    		tableViewLives.refresh();
			    			Platform.runLater(() -> textFieldJoins.setText(MyCalendar.dateToString(new Date(event.getTimeStamp()), "'['HH:mm:ss'] '") + event.getUser().getName() + " acessou a live"));
			    		}
		    		}
	    	})
	    	.build();
			tableViewLives.getItems().add(client);
			return client;
		}
		catch (Exception e)
			{ return null; }
	}
	
	private String totalCurrentGiftsValue() {
		float value = 0;
		if (gifts.containsKey(getCurrentLiveClient()))
			for (Gift gift : gifts.get(getCurrentLiveClient()).keySet())
				value += giftValue(gift);
		return String.format("R$ %.2f", value);
	}

	private void sendInfoToChat(LiveClient liveClient, Date date, String text, boolean forceChat)
		{ sendToChat(liveClient, date, "* " + text, forceChat); }
	
	private void sendInfoToChat(LiveClient liveClient, String text, boolean forceChat)
		{ sendInfoToChat(liveClient, null, text, forceChat); }
	
	private void sendInfoToChat(LiveClient liveClient, Date date, String text)
		{ sendToChat(liveClient, date, "* " + text, false); }

	private void sendInfoToChat(LiveClient liveClient, String text)
		{ sendInfoToChat(liveClient, null, text, false); }
	
	private void sendToChat(LiveClient liveClient, Date date, String text, boolean forceChat) {
		Platform.runLater(() -> {
			synchronized (textAreaLiveChat) {
				String str = MyCalendar.dateToString(date == null ? new Date() : date, "'['HH:mm:ss'] '") + text;
    		if (forceChat || isCurrentSelectedLive(liveClient))
    			textAreaLiveChat.appendText(textAreaLiveChat.getText().isBlank() ? str : "\n\n" + str);
    		if (!forceChat) {
					if (!chatBuffers.containsKey(liveClient))
						chatBuffers.put(liveClient, new ArrayList<>());
					chatBuffers.get(liveClient).add(str);
    		}
			}
		});
	}

	private void sendToChat(LiveClient liveClient, Date date, String text)
		{ sendToChat(liveClient, date, text, false); }
	
	private boolean isCurrentSelectedLive(LiveClient liveClient) {
		LiveClient client = tableViewLives.getSelectionModel().getSelectedItem();
		return liveClient.equals(client);
	}

	public void close() {
		for (LiveClient client : tableViewLives.getItems())
			client.disconnect();
	}

}
