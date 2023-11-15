package gui;

import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;

import application.Main;
import entity.LiveClientEX;
import entity.TTS;
import enums.Icons;
import gui.util.Alerts;
import gui.util.ControllerUtils;
import io.github.jwdeveloper.tiktok.TikTokLive;
import io.github.jwdeveloper.tiktok.data.models.Emote;
import io.github.jwdeveloper.tiktok.data.models.RankingUser;
import io.github.jwdeveloper.tiktok.data.models.gifts.Gift;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveOfflineHostException;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.models.ConnectionState;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import util.Misc;
import util.MyCalendar;
import util.MyFile;

public class MainWindowController implements Initializable {
	
	private float coinValue = 0.025f;
  private List<LiveClient> connected;
	private List<LiveClient> disconnected;
	
	private static ChatController chatController;
	private static MainWindowController mainWindowController;
  
  @FXML
  private TableView<LiveClientEX> tableViewLives;
  @FXML
  private TableColumn<LiveClientEX, LiveClientEX> tableColumnLive;
  @FXML
  private TableColumn<LiveClientEX, LiveClientEX> tableColumnLiveViewers;
  @FXML
  private TableView<Gift> tableViewGifts;
  @FXML
  private TableColumn<Gift, Gift> tableColumnGiftNome;
  @FXML
  private TableColumn<Gift, Gift> tableColumnGiftQuant;
  @FXML
  private TableColumn<Gift, Gift> tableColumnGiftVal;
  @FXML
  private TextField textFieldGiftsTotalValue;
  @FXML
  private Button buttonAddLive;
  @FXML
  private Button buttonRemoveLive;
  @FXML
  private TableColumn<RankingUser, RankingUser> tableColumnTopFollowersCoins;
  @FXML
  private TableColumn<RankingUser, RankingUser> tableColumnTopFollowersName;
  @FXML
  private TableView<RankingUser> tableViewTopFollowers;
  @FXML
  private TabPane tabPane;
  @FXML
  private Tab tabChat;
  @FXML
  private Tab tabConfigEventos;
  @FXML
  private Tab tabConfigOutros;
  @FXML
  private Tab tabConfigTTS;
  
	@Override
 	public void initialize(URL url, ResourceBundle rb) {
		mainWindowController = this;
		File chatBufferDir = new File("./chatBuffers/");
		if (!chatBufferDir.exists())
			chatBufferDir.mkdir();
		connected = new ArrayList<>();
		disconnected = new ArrayList<>();
		initTabs();
		loadConfigs();
		setLivesTableViewFactories();
		setGiftsTableViewFactories();
		setRankingUsersTableViewFactories();
		setButtonsActions();
		TTS.startTTS();
	}
	
	private void initTabs() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/ChatView.fxml"));
			VBox vBoxFiltros = loader.load();
			chatController = loader.getController();
			tabChat.setContent(vBoxFiltros);
		}
		catch (Exception e) {}
	}
	
	private void setButtonsActions() {
		chatController.buttonChatConfig.setDisable(true);
		buttonRemoveLive.setDisable(true);
		buttonAddLive.setOnAction(e -> {
			String user = Alerts.textPrompt("Prompt", "Adicionar nova live", null, "Digite o usuário da live á adicionar");
			if (user != null) {
				if (!TikTokLive.isHostNameValid(user))
					Alerts.error("Erro", user + " - Usuário inválido");
				else {
					for (LiveClientEX client : tableViewLives.getItems())
						if (client.getUserName().equals(user)) {
							Alerts.warning("Advertência", "O usuário " + user + " já está na lista");
							return;
						}
					addLive(user);
				}
			}
		});		
		buttonRemoveLive.setOnAction(e -> {
			String user = tableViewLives.getSelectionModel().getSelectedItem().getRoomInfo().getHostName();
			LiveClientEX liveClient = tableViewLives.getSelectionModel().getSelectedItem(); 
			if (liveClient != null && Alerts.confirmation("Confirmação", "Deseja mesmo excluir a live \"" + user + "\" da lista?")) {
				if (liveClient.getRoomInfo().getConnectionState() != ConnectionState.DISCONNECTED)
					liveClient.disconnect();
				tableViewLives.getItems().remove(liveClient);
				if (tableViewLives.getItems().isEmpty())
					chatController.buttonChatConfig.setDisable(true);
			}
		});
		ControllerUtils.addIconToButton(buttonAddLive, Icons.NEW_ITEM.getValue(), 24, 24, 200);
		ControllerUtils.addIconToButton(buttonRemoveLive, Icons.DELETE.getValue(), 24, 24, 200);
	}

	private void connectToLive(LiveClient client) {
		try {
			chatController.clearChatTextArea();
			Platform.runLater(() -> {
				chatController.buttonChatConfig.setDisable(false);
				chatController.textAreaJoins.clear();
				chatController.clearChatTextArea();
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
				if (!connected.contains(client)) {
					LiveClientEX clientEX = new LiveClientEX(client);
					connected.add(clientEX);
					if (!clientEX.getCharBuffer().isEmpty())
						chatController.addDirectlyToChatTextArea(String.join("\n", clientEX.getCharBuffer()));
				}
				sendInfoToChat(client, MyCalendar.dateToString(new Date(), "'Nova sessão: 'dd' de 'MMMM' de 'yyyy") + 
						"\nConectando á live de " + client.getRoomInfo().getHostName(), true);
				client.connectAsync();
			}
		}
		catch (Exception e)
			{ sendInfoToChat(client, "Não foi possível conectar á live de " + client.getRoomInfo().getHostName()); }
	}

	private void setLivesTableViewFactories() {
		tableViewLives.getSelectionModel().selectedItemProperty().addListener((o, oldValue, newValue) -> {
			chatController.setLiveChatMaxCaretPos(0);
			TTS.clearBuffer();
			buttonRemoveLive.setDisable(false);
			if (newValue.getRoomInfo().getConnectionState() == ConnectionState.DISCONNECTED)
				connectToLive(newValue);
			else {
				Platform.runLater(() -> { 
					if (!newValue.getCharBuffer().isEmpty()) {
						chatController.clearChatTextArea();
						chatController.addDirectlyToChatTextArea(String.join("\n", newValue.getCharBuffer()));
					}
					tableViewGifts.getItems().clear();
					for (Gift gift : getCurrentLiveClient().getGiftsFromDate(new Date()))
						tableViewGifts.getItems().add(gift);
					tableViewGifts.refresh();
    			textFieldGiftsTotalValue.setText(totalCurrentGiftsValue());
				});
			}
		});
		for (TableColumn<LiveClientEX, LiveClientEX> col : Arrays.asList(tableColumnLive, tableColumnLiveViewers)) {
			col.setStyle("-fx-font-size: 14px; -fx-font-family: \"Lucida Console\";");
			col.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
			col.setCellFactory(lv -> {
				TableCell<LiveClientEX, LiveClientEX> cell = new TableCell<>() {
					@Override
					protected void updateItem(LiveClientEX client, boolean empty) {
						super.updateItem(client, empty);
						if (client != null) {
							if (col.getId().equals("tableColumnLive"))
								setText(client.getRoomInfo().getHostName());
							else
								setText("" + client.getRoomInfo().getViewersCount() + "/" + client.getRoomInfo().getTotalViewersCount());
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
								setText("" + getCurrentLiveClient().getGiftQuantity(gift));
							else
								setText(strGiftCumulativeValue(gift));
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
	
	LiveClientEX getCurrentLiveClient()
		{ return tableViewLives.getSelectionModel().getSelectedItem(); }

	private float giftCumulativeValue(Gift gift)
		{ return getCurrentLiveClient().getGiftQuantity(gift) * gift.getDiamondCost() * coinValue; }
	
	private String strGiftCumulativeValue(Gift gift)
		{ return String.format("%.2f", giftCumulativeValue(gift)); } 

	public LiveClientEX addLive(String liveID) {
		try {
			LiveClient client = TikTokLive.newClient(liveID)
				.configure((settings) -> {
	        settings.setClientLanguage("pt");
	        settings.setLogLevel(Level.OFF);
	        settings.setPrintToConsole(false);
	        settings.setRetryOnConnectionFailure(true);
	        settings.setRetryConnectionTimeout(Duration.ofSeconds(60));
		    })
	    	.onConnected((liveClient, event) -> {
    			if (!isCurrentSelectedLive(liveClient))
    				sendInfoToChat(liveClient, "[NOTIFY] " + liveClient.getRoomInfo().getHostName() + " está ao vivo agora mesmo.", true);
	    		else {
	    			sendInfoToChat(liveClient, "Conectado com sucesso á live de " + liveClient.getRoomInfo().getHostName());
						synchronized (chatController.textAreaLiveInfos) {
							chatController.clearChatTextArea();
							chatController.addDirectlyToChatTextArea("Live de " + liveClient.getRoomInfo().getHostName());
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
	      		disconnected.add(liveClient);
	      		if (connected.size() < 4)
	      			Misc.createTimer(0, 10000, () -> liveClient.connect());
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
	    		if (ChatConfigController.getShowJoins(getCurrentSelectedLiveUser()))
	    			Platform.runLater(() -> {
	    				tableViewLives.refresh();
	    				chatController.sendToJoinTextArea(new Date(event.getTimeStamp()), event.getUser().getProfileName() + " acessou a live");
		    		});
	    	})
	    	.onComment((liveClient, event) -> {
	    		if (ChatConfigController.getShowComments(getCurrentSelectedLiveUser())) { 
	    			if (isCurrentSelectedLive(liveClient)) {
	    				sendToChat(liveClient, new Date(event.getTimeStamp()), event.getUser().getProfileName() + ":\n  " + event.getText());
			    		if (connected.contains(liveClient))
			    			TTS.addText(event.getUser(), event.getText());
	    			}
		    		if (connected.contains(liveClient))
		    			getCurrentLiveClient().addChat(event.getText());
	    		}
	    	})
	    	.onGift((liveClient, event) -> {
	    		if (ChatConfigController.getShowGifts(getCurrentSelectedLiveUser()))
	    			sendInfoToChat(liveClient, new Date(event.getTimeStamp()), event.getUser().getProfileName() + " enviou [" + event.getGift().getName() + " x" + event.getCombo() + "]");
	    		Platform.runLater(() -> {
	    			getCurrentLiveClient().addGift(event.getGift(), event.getCombo());
	    			if (!tableViewGifts.getItems().contains(event.getGift()))
	    				tableViewGifts.getItems().add(event.getGift());
	    			tableViewGifts.refresh();
	    			textFieldGiftsTotalValue.setText(totalCurrentGiftsValue());
	    		});
	    	})
	    	.onEmote((liveClient, event) -> {
    			if (isCurrentSelectedLive(liveClient)) {
    				String emotes = "";
    				for (Emote emote : event.getEmotes())
    					emotes += (emotes.isBlank() ? "" : ", ") + emote.toString();
  	    		if (ChatConfigController.getShowEmotes(getCurrentSelectedLiveUser()))
  	    			sendInfoToChat(liveClient, event.getUser().getProfileName() + " onEmote(): " + emotes);
    			}
	    	})
	    	.onFollow((liveClient, event) -> {
    			if (isCurrentSelectedLive(liveClient) && ChatConfigController.getShowFollows(getCurrentSelectedLiveUser()))
    				sendInfoToChat(liveClient, event.getUser().getProfileName() + " está seguindo o anfitrião");
	    	})
	    	.onLike((liveClient, event) -> {
    			if (isCurrentSelectedLive(liveClient) && ChatConfigController.getShowLikes(getCurrentSelectedLiveUser()))
    				chatController.sendToJoinTextArea(new Date(event.getTimeStamp()), event.getUser().getProfileName() + " curtiu a live");
	    	})
	    	.onShare((liveClient, event) -> {
    			if (isCurrentSelectedLive(liveClient) && ChatConfigController.getShowShares(getCurrentSelectedLiveUser()))
    				sendInfoToChat(liveClient, event.getUser().getProfileName() + " compartilhou a live");
	    	})
	    	.onSubscribe((liveClient, event) -> {
    			if (isCurrentSelectedLive(liveClient) && ChatConfigController.getShowSubscribes(getCurrentSelectedLiveUser()))
    				sendInfoToChat(liveClient, event.getUser().getProfileName() + " se inscreveu na live");
	    	})
	    	.onQuestion((liveClient, event) -> {
    			if (isCurrentSelectedLive(liveClient) && ChatConfigController.getShowQuestions(getCurrentSelectedLiveUser()))
    				sendInfoToChat(liveClient, event.getUser().getProfileName() + " perguntou: " + event.getText());
	    	})
	    	.onError((liveClient, event) -> {
	    		if (event.getException() instanceof TikTokLiveOfflineHostException)
	    			sendInfoToChat(liveClient, "Não foi possível conectar á live de " + liveClient.getRoomInfo().getHostName(), true);
	    		else {
		    		System.out.println("[" + liveClient.getRoomInfo().getHostName() + "] ERROR: " + event.getException());
		    		event.getException().printStackTrace();
	    		}
	    	})
				.onRoomInfo((liveClient, event) -> {
					final List<RankingUser> list = new LinkedList<>(event.getRoomInfo().getUsersRanking());
					Platform.runLater(() -> {
						synchronized (tableViewTopFollowers) {
							tableViewTopFollowers.getItems().clear();
							for (RankingUser user : list) {
								tableViewTopFollowers.getItems().add(user);
								if (tableViewTopFollowers.getItems().size() == 5)
									break;
							}
							tableViewTopFollowers.refresh();
						}
					});		
	    	})
	    	.build();
			LiveClientEX newClient = new LiveClientEX(client);
			tableViewLives.getItems().add(newClient);
			return newClient;
		}
		catch (Exception e)
			{ return null; }
	}
	
	private void sendInfoToChat(LiveClient liveClient, Date date, String text)
		{ chatController.sendInfoToChat(liveClient, date, text); }

	private void sendInfoToChat(LiveClient liveClient, String text)
		{ chatController.sendInfoToChat(liveClient, text); }

	private void sendInfoToChat(LiveClient liveClient, String text, boolean forceChat)
		{ chatController.sendToChat(liveClient, null, text, forceChat); }

	private void sendToChat(LiveClient liveClient, Date date, String text)
		{ chatController.sendToChat(liveClient, date, text); }

	private String totalCurrentGiftsValue() {
		float value = 0;
		for (Gift gift : getCurrentLiveClient().getGiftsFromDate(new Date()))
				value += giftCumulativeValue(gift);
		return String.format("R$ %.2f", value);
	}

	String getCurrentSelectedLiveUser()
		{ return tableViewLives.getSelectionModel().getSelectedItem().getUserName(); }
	
	boolean isCurrentSelectedLive(LiveClient liveClient)
		{ return liveClient.getRoomInfo().getHostName().equals(getCurrentSelectedLiveUser()); }

	public void close() {
		for (LiveClientEX client : tableViewLives.getItems()) {
			client.disconnect();
			if (client.getCharBuffer() != null && !client.getCharBuffer().isEmpty())
				MyFile.writeAllLinesOnFile(client.getCharBuffer(), "./chatBuffers/" + client.getUserName() + ".txt"); 
		}
		saveConfigs();
		ChatConfigController.close();
	}

	private void loadConfigs() {
		for (String liveUsername : Main.getIni().getItemList("LIVES"))
			addLive(liveUsername);
	}

	private void saveConfigs() {
		Main.getIni().clearFile();
		for (LiveClient live : tableViewLives.getItems())
			Main.getIni().write("LIVES", live.getRoomInfo().getHostName(), "1");
	}
	
	static MainWindowController getMainWindowController()
		{ return mainWindowController; }

	static ChatController getChatController()
		{ return chatController; }

	/**
	 * - ALTERAR O PROGRAMA PARA NÃO NARRAR EXATAMENTE TODAS AS MENSAGENS SE NAO ATRASA DEMAIS
	 */

}
