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

import application.Main;
import enums.Icons;
import gui.util.Alerts;
import gui.util.ControllerUtils;
import io.github.jwdeveloper.tiktok.TikTokLive;
import io.github.jwdeveloper.tiktok.data.models.Emote;
import io.github.jwdeveloper.tiktok.data.models.RankingUser;
import io.github.jwdeveloper.tiktok.data.models.gifts.Gift;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.models.ConnectionState;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import util.IniFile;
import util.Misc;
import util.MyCalendar;

public class MainWindowController implements Initializable {
	
	/**
	 * Ativar os botões de ADICIONAR e REMOVER LIVE
	 * 
	 * Criar a classe de LiveClient que extende a classe original,
	 * e adicionar métodos e atributos extras
	 */
	
	private float coinValue = 0.025f;
  private Map<LiveClient, List<String>> chatBuffers = new HashMap<>();
  private Map<LiveClient, Map<Gift, Integer>> gifts = new HashMap<>();
  private List<LiveClient> connected;
	private List<LiveClient> disconnected;
  
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
  private TextArea textAreaJoins;
  @FXML
  private TextField textFieldGiftsTotalValue;
  @FXML
  private Button buttonAddLive;
  @FXML
  private Button buttonRemoveLive;
  @FXML
  private Button buttonChatConfig;
  @FXML
  private TableColumn<RankingUser, RankingUser> tableColumnTopFollowersCoins;
  @FXML
  private TableColumn<RankingUser, RankingUser> tableColumnTopFollowersName;
  @FXML
  private TableView<RankingUser> tableViewTopFollowers;
  
	@Override
 	public void initialize(URL url, ResourceBundle rb) {
		connected = new ArrayList<>();
		disconnected = new ArrayList<>();
		loadConfigs();
		setLivesTableViewFactories();
		setGiftsTableViewFactories();
		setRankingUsersTableViewFactories();
		setButtonsActions();
	}
	
	private void setButtonsActions() {
		buttonRemoveLive.setDisable(true);
		buttonAddLive.setOnAction(e -> {
			String user = Alerts.textPrompt("Prompt", "Adicionar nova live", null, "Digite o usuário da live á adicionar");
			if (user != null) {
				if (!TikTokLive.isHostNameValid(user))
					Alerts.error("Erro", user + " - Usuário inválido");
				else
					addLive(user);
			}
		});		
		buttonRemoveLive.setOnAction(e -> {
			String user = tableViewLives.getSelectionModel().getSelectedItem().getRoomInfo().getHostName();
			LiveClient liveClient = tableViewLives.getSelectionModel().getSelectedItem(); 
			if (liveClient != null && Alerts.confirmation("Confirmação", "Deseja mesmo excluir a live \"" + user + "\" da lista?")) {
				if (liveClient.getRoomInfo().getConnectionState() != ConnectionState.DISCONNECTED)
					liveClient.disconnect();
				tableViewLives.getItems().remove(liveClient);
			}
		});
		buttonChatConfig.setOnAction(e -> {
			ChatConfigController.openChatConfig();
		});
		ControllerUtils.addIconToButton(buttonAddLive, Icons.ICON_NEWITEM.getValue(), 24, 24, 200);
		ControllerUtils.addIconToButton(buttonRemoveLive, Icons.ICON_DELETE.getValue(), 24, 24, 200);
		ControllerUtils.addIconToButton(buttonChatConfig, Icons.ICON_CONFIG.getValue(), 24, 24, 200);
	}

	private void connectToLive(LiveClient client) {
		try {
			Platform.runLater(() -> {
				textAreaLiveChat.clear();
				textAreaJoins.clear();
				textAreaLiveInfos.clear();
				tableViewGifts.getItems().clear();
				tableViewTopFollowers.getItems().clear();
			});
			if (client.getRoomInfo().getConnectionState() == ConnectionState.DISCONNECTED) {
				if (connected.size() == 4) {
					if (connected.get(0).getRoomInfo().getConnectionState() != ConnectionState.DISCONNECTED) {
						sendInfoToChat(connected.get(0), "Você foi desconectado da live de " + connected.get(0).getRoomInfo().getHostName() + " por exceder o limite máximo de 4 conexões simultâneas.", true);
						disconnected.add(connected.get(0));
						connected.get(0).disconnect();
					}
					connected.remove(0);
				}
				sendInfoToChat(client, "Conectando á live de " + client.getRoomInfo().getHostName());
				client.connectAsync();
				if (!connected.contains(client))
					connected.add(client);
			}
		}
		catch (Exception e)
			{ sendInfoToChat(client, "Não foi possível conectar á live de " + client.getRoomInfo().getHostName()); }
	}

	private void setLivesTableViewFactories() {
		tableViewLives.getSelectionModel().selectedItemProperty().addListener((o, oldValue, newValue) -> {
			buttonRemoveLive.setDisable(false);
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
							else
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
							else
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
	
	private void setRankingUsersTableViewFactories() {
		for (TableColumn<RankingUser, RankingUser> col : Arrays.asList(tableColumnTopFollowersName, tableColumnTopFollowersCoins)) {
			col.setStyle("-fx-font-size: 14px; -fx-font-family: \"Lucida Console\";");
			col.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
			col.setCellFactory(lv -> {
				TableCell<RankingUser, RankingUser> cell = new TableCell<>() {
					@Override
					protected void updateItem(RankingUser user, boolean empty) {
						super.updateItem(user, empty);
						if (user != null) {
							if (col.getId().equals("tableColumnTopFollowersName"))
								setText(user.getRank() + ". " + (!user.getUser().getProfileName().isEmpty() ? user.getUser().getProfileName() : user.getUser().getName()));
							else
								setText("" + user.getScore());
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
	        settings.setRetryConnectionTimeout(Duration.ofSeconds(60));
		    })
	    	.onConnected((liveClient, event) -> {
    			if (!isCurrentSelectedLive(liveClient))
	    			sendInfoToChat(liveClient, "[NOTIFY] " + liveClient.getRoomInfo().getHostName() + " está ao vivo agora mesmo.", true);
	    		else {
	    			sendInfoToChat(liveClient, "Conectado com sucesso á live de " + liveClient.getRoomInfo().getHostName());
						synchronized (textAreaLiveInfos) {
							textAreaLiveInfos.clear();
							textAreaLiveInfos.appendText("Live de " + liveClient.getRoomInfo().getHostName());
						}
	    		}
	    		tableViewLives.refresh();
	    		if (!connected.contains(liveClient))
	    			connected.add(liveClient);
	    	})
	    	.onDisconnected((liveClient, event) -> {
	    		if (!disconnected.contains(liveClient) && connected.contains(liveClient)) {
	    			sendInfoToChat(liveClient, "Você foi desconectado da live de " + liveClient.getRoomInfo().getHostName(), true);
		    		connected.remove(liveClient);
	    		}
	    		else
	    			disconnected.remove(liveClient);
	    	})
	    	.onLivePaused((liveClient, event) -> {
    			if (isCurrentSelectedLive(liveClient))
    				sendInfoToChat(liveClient, liveClient.getRoomInfo().getHostName() + " pausou a live");
	    	})
	    	.onLiveEnded((liveClient, event) -> {
    			sendInfoToChat(liveClient, (isCurrentSelectedLive(liveClient) ? "" : "[NOTIFY] ") + "A live de " + liveClient.getRoomInfo().getHostName() + " foi encerrada pelo anfitrião.", true);
	    	})
	    	.onJoin((liveClient, event) -> {
	    		if (ChatConfigController.getShowJoins())
	    			Platform.runLater(() -> {
	    				tableViewLives.refresh();
	    				sendToJoinTextArea(new Date(event.getTimeStamp()), event.getUser().getProfileName() + " acessou a live");
		    		});
	    	})
	    	.onComment((liveClient, event) -> {
	    		if (ChatConfigController.getShowComments())
	    			sendToChat(liveClient, new Date(event.getTimeStamp()), event.getUser().getProfileName() + ":\n" + event.getText());
	    	})
	    	.onGift((liveClient, event) -> {
	    		if (ChatConfigController.getShowGifts())
	    			sendInfoToChat(liveClient, new Date(event.getTimeStamp()), event.getUser().getProfileName() + " enviou [" + event.getGift().getName() + " x" + event.getCombo() + "]");
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
	    	.onEmote((liveClient, event) -> {
    			if (isCurrentSelectedLive(liveClient)) {
    				String emotes = "";
    				for (Emote emote : event.getEmotes())
    					emotes += (emotes.isBlank() ? "" : ", ") + emote.toString();
  	    		if (ChatConfigController.getShowEmotes())
  	    			sendInfoToChat(liveClient, event.getUser().getProfileName() + " onEmote(): " + emotes);
    			}
	    	})
	    	.onFollow((liveClient, event) -> {
    			if (isCurrentSelectedLive(liveClient) && ChatConfigController.getShowFollows())
    				sendInfoToChat(liveClient, event.getUser().getProfileName() + " está seguindo o anfitrião da live");
	    	})
	    	.onLike((liveClient, event) -> {
    			if (isCurrentSelectedLive(liveClient) && ChatConfigController.getShowLikes())
    				sendToJoinTextArea(new Date(event.getTimeStamp()), event.getUser().getProfileName() + " curtiu a live");
	    	})
	    	.onShare((liveClient, event) -> {
    			if (isCurrentSelectedLive(liveClient) && ChatConfigController.getShowShares())
    				sendInfoToChat(liveClient, event.getUser().getProfileName() + " compartilhou a live");
	    	})
	    	.onSubscribe((liveClient, event) -> {
    			if (isCurrentSelectedLive(liveClient) && ChatConfigController.getShowSubscribes())
    				sendInfoToChat(liveClient, event.getUser().getProfileName() + " se inscreveu na live");
	    	})
	    	.onQuestion((liveClient, event) -> {
    			if (isCurrentSelectedLive(liveClient) && ChatConfigController.getShowQuestions())
    				sendInfoToChat(liveClient, event.getUser().getProfileName() + " perguntou: " + event.getText());
	    	})
	    	.onError((liveClient, event) -> {
	    		System.out.println("[" + liveClient.getRoomInfo().getHostName() + "] ERROR: " + event.getException());
	    		event.getException().printStackTrace();
	    	})
				.onRoomUserInfo((liveClient, event) -> {
					Platform.runLater(() -> {
						synchronized (tableViewTopFollowers) {
							tableViewTopFollowers.getItems().clear();
							for (RankingUser user : event.getUsersRanking()) {
								tableViewTopFollowers.getItems().add(user);
								if (tableViewTopFollowers.getItems().size() == 5)
									break;
							}
							tableViewTopFollowers.refresh();
						}
					});		
	    	})
	    	.build();
			tableViewLives.getItems().add(client);
			return client;
		}
		catch (Exception e)
			{ return null; }
	}
	
	private void sendToJoinTextArea(Date date, String text) {
		Platform.runLater(() -> {
			if (!textAreaJoins.getText().isEmpty())
				textAreaJoins.appendText("\n");
  		textAreaJoins.appendText(MyCalendar.dateToString(date, "'['HH:mm:ss'] '") + text);
		});
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
		saveConfigs();
		ChatConfigController.close();
	}

	private void loadConfigs() {
		for (String liveUsername : Main.getIni().getItemList("LIVES"))
			if (Misc.alwaysFalse() && !TikTokLive.isHostNameValid(liveUsername))
				System.out.println("Erro ao carregar lives do arquivo ini: " + liveUsername + " (usuário inválido)");
			else
				addLive(liveUsername);
	}

	private void saveConfigs() {
		Main.getIni().clearFile();
		for (LiveClient live : tableViewLives.getItems())
			Main.getIni().write("LIVES", live.getRoomInfo().getHostName(), "1");
	}

}
