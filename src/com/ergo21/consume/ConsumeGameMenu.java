
package com.ergo21.consume;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import com.almasb.consume.ConsumeApp;
import com.almasb.consume.Types.Actions;
import com.almasb.consume.Types.Element;
import com.almasb.fxgl.asset.SaveLoadManager;
import com.almasb.fxgl.event.MenuEvent;
import com.almasb.fxgl.ui.FXGLMenu;
import com.almasb.fxgl.ui.UIFactory;
import com.almasb.fxgl.util.Version;
import com.sun.javafx.scene.control.skin.LabeledText;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * This is the default FXGL game menu
 *
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 *
 */
public final class ConsumeGameMenu extends FXGLMenu {

	private PowerGroup powerList;
	private AnchorPane contentViewer;
	private ConsumeApp consApp;
	
	public MenuItem levelMenuItem;
	public MenuItem itemExit;

	public ConsumeGameMenu(ConsumeApp app) {
		super(app);
		consApp = app;

		powerList = new PowerGroup();

		contentViewer = new AnchorPane(powerList);
		contentViewer.setTranslateX(10);
		contentViewer.setTranslateY(app.getHeight() / 2);

		MenuBox menu = createMainMenu();
		// menuX = 50;
		// menuY = app.getHeight() / 2 - menu.getLayoutHeight() / 2;

		// just a placeholder
		MenuBox menuContent = new MenuBox((int) app.getWidth() - 300 - 50);
		menuContent.setTranslateX(300);
		menuContent.setTranslateY(menu.getTranslateY());
		menuContent.setVisible(false);

		Rectangle bg = new Rectangle(app.getWidth(), app.getHeight());
		bg.setFill(Color.rgb(10, 1, 1));
		// bg.setOpacity(0.5);

		Title title = new Title(app.getSettings().getTitle());
		title.setTranslateX(app.getWidth() / 2 - title.getLayoutWidth() / 2);
		title.setTranslateY(menu.getTranslateY() / 2 - title.getLayoutHeight() / 2);

		Text version = new Text("v" + app.getSettings().getVersion());
		version.setTranslateY(app.getHeight() - 2);
		version.setFill(Color.WHITE);
		version.setFont(Font.font(18));

		getChildren().setAll(bg, title, version, menu, menuContent, contentViewer);
	}

	private MenuBox createMainMenu() {
		MenuItem itemPowers = new MenuItem("Powers");
		itemPowers.setAction(() -> {
			contentViewer.getChildren().clear();
			contentViewer.getChildren().add(powerList);
		});

		/*MenuItem itemLoad = new MenuItem("Load & Save");
		itemLoad.setAction(() -> {
			contentViewer.getChildren().clear();
			contentViewer.getChildren().add(createContentLoadConsume());
		});*/

		MenuItem itemOptions = new MenuItem("Options");
		itemOptions.setAction(() -> {
			contentViewer.getChildren().clear();
			contentViewer.getChildren().add(createOptionsMenuConsume());
		});

		levelMenuItem = new MenuItem("Level Menu");
		levelMenuItem.setAction(() -> {
			UIFactory.getDialogBox().showConfirmationBox("All progress in this level will be lost. Return to the Level Menu?", (b) -> {
				if(b){
					consApp.showLevelScreen();
					levelMenuItem.fireEvent(new MenuEvent(MenuEvent.RESUME));
					contentViewer.getChildren().clear();
					contentViewer.getChildren().add(powerList);
				}
			});
			
		});

		itemExit = new MenuItem("Main Menu");
		itemExit.setAction(() -> {
			UIFactory.getDialogBox().showConfirmationBox("All progress in this game will be lost. Return to the Main Menu?", (b) -> {
				if(b){
					consApp.indiLoop.stop();
					consApp.soundManager.stopAll();
					consApp.soundManager.setBackgroundMusic(FileNames.THEME_MUSIC);
		        	consApp.soundManager.getBackgroundMusic().setCycleCount(Integer.MAX_VALUE);
		        	consApp.resetWorld();

		        	itemExit.fireEvent(new MenuEvent(MenuEvent.EXIT));
		        	consApp.soundManager.playBackgroundMusic();
					contentViewer.getChildren().clear();
					contentViewer.getChildren().add(powerList);
				}
			});
			
		});

		MenuBox menu = new MenuBox(5, itemPowers, itemOptions, levelMenuItem, itemExit);

		menu.setTranslateX(5);
		menu.setTranslateY(app.getHeight() / 2 - menu.getLayoutHeight() / 2);
		return menu;
	}

	private BorderPane createContentLoadConsume() {
		ListView<String> list = new ListView<String>();
        list.getStylesheets().add("assets/ui/css/list.css");
        list.setMinHeight(consApp.getHeight()/2 - 15);
        list.setMaxHeight(consApp.getHeight()/2 - 15);
		SaveLoadManager.INSTANCE.loadFileNames().ifPresent(names -> list.getItems().setAll(names));
		ArrayList<String> removes = new ArrayList<String>();
		for(String item : list.getItems()){
			if(item.endsWith(".set")){
				removes.add(item);
			}
		}
		list.getItems().removeAll(removes);
		removes.clear();
		list.prefHeightProperty().bind(Bindings.size(list.getItems()).multiply(36));
		
		if (list.getItems().size() > 0) {
			list.getSelectionModel().selectFirst();
		}

		MenuItem btnSave = new MenuItem("Save");
		btnSave.setAction(() -> {
			Serializable data = app.saveState();

			if (data == null)
				return;

			TextInputDialog dialog = new TextInputDialog();
			dialog.setTitle("Naming");
			dialog.setHeaderText("Enter name for save file");
			dialog.showAndWait().ifPresent(fileName -> {
				if(fileName.endsWith(".set")){
					Alert alert = new Alert(AlertType.ERROR);
					alert.setContentText("File extension reserved. File not saved.");
					alert.showAndWait();
					return;
				}
				if(SaveLoadManager.INSTANCE.loadFileNames().get().contains(fileName)){
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.setHeaderText("File already exists. Do you want to overwrite?");
					Optional<ButtonType> result = alert.showAndWait();
					if(result.isPresent() && result.get() == ButtonType.CANCEL){
						return;
					}
				}
				if(consApp.player.getProperty("scenePlaying") != null && (boolean) consApp.player.getProperty("scenePlaying")){
					Alert alert = new Alert(AlertType.ERROR);
					alert.setContentText("Cannot save file in the middle of a scene.");
					alert.showAndWait();
					return;
				}
				try {
					SaveLoadManager.INSTANCE.save(data, fileName);
					SaveLoadManager.INSTANCE.loadFileNames().ifPresent(names -> list.getItems().setAll(names));
					for(String item : list.getItems()){
						if(item.endsWith(".set")){
							removes.add(item);
						}
					}
					list.getItems().removeAll(removes);
					removes.clear();
					consApp.sSettings.setLastSave(fileName);
				} catch (Exception e) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setContentText("Failed to save file: " + fileName + ". Error: " + e.getMessage());
					alert.showAndWait();
				}
			});
		});

		MenuItem btnLoad = new MenuItem("Load");
		btnLoad.setAction(() -> {
			String fileName = list.getSelectionModel().getSelectedItem();
			if (fileName == null)
				return;

			try {
				Serializable data = SaveLoadManager.INSTANCE.load(fileName);
				app.loadState(data);
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Loaded");
				alert.setHeaderText("Save loaded successfully");
				alert.showAndWait();
				consApp.sSettings.setLastSave(fileName);
			} catch (Exception e) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setContentText("Failed to load file: " + fileName + ". Error: " + e.getMessage());
				alert.showAndWait();
			}
		});
		MenuItem btnDelete = new MenuItem("Delete");
		btnDelete.setAction(() -> {
			String fileName = list.getSelectionModel().getSelectedItem();
			if (fileName == null)
				return;

			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Please confirm");
			alert.setHeaderText("Are you sure you want to delete file: " + fileName);
			Optional<ButtonType> result = alert.showAndWait();
			if(result.isPresent() && result.get() == ButtonType.CANCEL){
				return;
			}
			SaveLoadManager.INSTANCE.delete(fileName);
			SaveLoadManager.INSTANCE.loadFileNames().ifPresent(names -> list.getItems().setAll(names));
			for(String item : list.getItems()){
				if(item.endsWith(".set")){
					removes.add(item);
				}
			}
			list.getItems().removeAll(removes);
			removes.clear();
		});

		BorderPane bp = new BorderPane();
		bp.setCenter(list);
		btnLoad.setTranslateY(5);
		btnDelete.setTranslateY(10);
		VBox right = new VBox(btnSave, btnLoad, btnDelete);
		right.setTranslateX(10);
		bp.setRight(right);

		return bp;
	}

	private MenuBox createOptionsMenuConsume() {
		MenuItem itemControls = new MenuItem("Controls");
		itemControls.setAction(() -> {
			contentViewer.getChildren().clear();
			contentViewer.getChildren().add(createContentControls());
		});
		//MenuItem itemVideo = new MenuItem("VIDEO");
		MenuItem itemAudio = new MenuItem("Audio");
		itemAudio.setAction(() ->{
			contentViewer.getChildren().clear();
			contentViewer.getChildren().add(createContentAudio());
		});
		/*MenuItem itemCredits = new MenuItem("CREDITS");
		itemCredits.setAction(() -> {
			contentViewer.getChildren().clear();
			contentViewer.getChildren().add(createContentCredits());
		});*/
		return new MenuBox(5, itemControls, itemAudio);
	}

	public void updatePowerMenu(Player playerData) {
		ArrayList<MenuItem> pItems = new ArrayList<MenuItem>();

		for (Element power : playerData.getPowers()) {
			MenuItem itemPower;
			if(power == Element.NEUTRAL){
				itemPower = new MenuItem("SPEAR");
			}
			else if(power == Element.NEUTRAL2){
				itemPower = new MenuItem("KNIFE");
			}
			else{
				itemPower = new MenuItem("" + power);
			}
			itemPower.setElement(power);
			itemPower.setAction(() -> {
				for (MenuItem item : pItems) {
					item.setHighlighted(false);
				}
				itemPower.setHighlighted(true);
				consApp.consController.changePower(power);
				// playerData.setCurrentPower(power);
			});
			if (playerData.getCurrentPower() == power) {
				itemPower.setHighlighted(true);
			}
			pItems.add(itemPower);
		}

		playerData.ElementProperty().addListener(new ChangeListener<Element>() {
			@Override
			public void changed(ObservableValue<? extends Element> cha, Element old, Element now) {
				for (MenuItem item : pItems) {
					if (item.getElement() == old) {
						item.setHighlighted(false);
					}

					if (item.getElement() == now) {
						item.setHighlighted(true);
					}
				}
			}

		});

		powerList.addItems(pItems);

	}

	private VBox createContentAudio(){
		Text musTex = new Text("Music Volume");
		musTex.setStroke(Color.WHITE);
		musTex.setFill(Color.WHITE);
		musTex.setFont(new Font(20));
		Text musTexVal = new Text();
		musTexVal.setStroke(Color.WHITE);
		musTexVal.setFill(Color.WHITE);
		SoundBar musBar = new SoundBar(200, 15, consApp.backVol, 100, musTexVal, Color.YELLOW);
		musBar.setOnMouseClicked(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent me) {
				consApp.backVol.set((me.getX())/200);
			}

		});
		musBar.setOnMouseDragged(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent me) {
				double val = (me.getX())/200;
				if(val < 0){
					val = 0;
				}
				else if(val > 1){
					val = 1;
				}
				consApp.backVol.set(val);
			}

		});

		HBox musBlock = new HBox(musBar, musTexVal);
		musTexVal.setTranslateX(musTexVal.getTranslateX() + 5);

		Text sfxTex = new Text("Sound Effects Volume");
		sfxTex.setStroke(Color.WHITE);
		sfxTex.setFill(Color.WHITE);
		sfxTex.setFont(new Font(20));
		Text sfxTexVal = new Text();
		sfxTexVal.setStroke(Color.WHITE);
		sfxTexVal.setFill(Color.WHITE);
		SoundBar sfxBar = new SoundBar(200, 15, consApp.sfxVol, 100, sfxTexVal, Color.YELLOW);
		sfxBar.setOnMouseClicked(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent me) {
				consApp.sfxVol.set((me.getX())/200);
			}

		});
		sfxBar.setOnMouseDragged(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent me) {
				double val = (me.getX())/200;
				if(val < 0){
					val = 0;
				}
				else if(val > 1){
					val = 1;
				}
				consApp.sfxVol.set(val);
			}

		});
		HBox sfxBlock = new HBox(sfxBar, sfxTexVal);
		sfxTexVal.setTranslateX(sfxTexVal.getTranslateX() + 5);
		
		MenuItem savAud = new MenuItem("Save");
		savAud.setOnMouseClicked(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent event) {
				consApp.sSettings.setBackMusicVolume(consApp.backVol.doubleValue());
				consApp.sSettings.setSFXVolume(consApp.sfxVol.doubleValue());
				consApp.getAudioManager().setGlobalMusicVolume(consApp.sSettings.getBackMusicVolume());
				consApp.getAudioManager().setGlobalSoundVolume(consApp.sSettings.getSFXVolume());
				UIFactory.getDialogBox().showMessageBox("Audio changes saved.");
			}
		});
		savAud.setMaxWidth(200);

		VBox vb = new VBox(musTex, musBlock, sfxTex, sfxBlock, savAud);
		return vb;
	}

	private BorderPane createContentControls() {
		TableView<TabItem> center = new TableView<TabItem>();
		center.setMaxHeight(consApp.getHeight()/2 - 15);
		center.setPrefWidth(consApp.getWidth()/2);
		center.getStylesheets().add("assets/ui/css/menu.css");
		
		TableColumn<TabItem, String> action = new TableColumn<TabItem, String>("Action");
		action.setResizable(false);
		action.setPrefWidth(consApp.getWidth()/4.2);
		action.setCellValueFactory(new PropertyValueFactory<>("itAction"));
		action.setStyle("-fx-alignment: CENTER");

		TableColumn<TabItem, String> key = new TableColumn<TabItem, String>("Key");
		key.setResizable(false);
		key.setPrefWidth(consApp.getWidth()/4.2);
		key.setCellValueFactory(new PropertyValueFactory<>("itKey"));
		key.setStyle("-fx-alignment: CENTER");

		HashMap<Actions, KeyCode> tKeys = consApp.consController.getCurrentKeys();
		ObservableList<TabItem> items = FXCollections.observableArrayList();
		for(Actions actionItem : Actions.values()){
			items.add(new TabItem(actionItem, tKeys.get(actionItem)));
		}
		center.setItems(items);
		center.addEventFilter(MouseEvent.ANY, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() > 1 && event.getTarget().getClass().getSuperclass() == TableCell.class) {
                	event.consume();
                    center.getItems().get(
                            center.getFocusModel().getFocusedCell().getRow())
                            .setKey(KeyCode.UNDEFINED);
                    
                }
                else if(event.getTarget().getClass() == LabeledText.class || event.getTarget().getClass() == Label.class){
                	event.consume();
                }
            }
        });
		center.setOnKeyPressed(new EventHandler<KeyEvent>(){
			@Override
			public void handle(KeyEvent ke) {
				if(center.getItems().get(center.getFocusModel().getFocusedCell().getRow()).getKey() == KeyCode.UNDEFINED){
					center.getItems().get(center.getFocusModel().getFocusedCell().getRow()).setKey(ke.getCode());
				}

			}
		});
		center.getColumns().add(action);
		center.getColumns().add(key);
		center.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);


		MenuItem itemSave = new MenuItem("Save");
		itemSave.setOnMouseClicked(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent event) {
				HashMap<Actions, KeyCode> newKeyMap = new HashMap<Actions, KeyCode>();
				for (TabItem item : items) {
                    if (newKeyMap.values().contains(item.getKey())
                            && item.getKey() != KeyCode.UNDEFINED) {
                    	UIFactory.getDialogBox().showMessageBox("Duplicate keys found. Changes not saved.");
                        return;
                    }
                    else if(item.getKey() == KeyCode.UNDEFINED){
                    	UIFactory.getDialogBox().showMessageBox(item.getKey() + " key undefined. Changes not saved.");
                        return;
                    }
                    else {
                        newKeyMap.put(item.getAction(), item.getKey());
                    }
                }
                consApp.consController.initControls(newKeyMap);
                UIFactory.getDialogBox().showMessageBox("Key changes saved.");
			}
		});

		MenuItem itemRestore = new MenuItem("Restore to Default");
		itemRestore.setOnMouseClicked(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent event) {
				HashMap<Actions, KeyCode> dKeys = consApp.consController.getDefaultKeys();
				items.clear();
				for(Actions actionItem : Actions.values()){
					items.add(new TabItem(actionItem, dKeys.get(actionItem)));
				}
			}
		});
		itemRestore.setTranslateY(5);
		VBox right = new VBox(itemSave, itemRestore);
		right.setTranslateX(10);

		BorderPane view = new BorderPane();
		view.setCenter(center);
		view.setRight(right);
		return view;
	}

	private VBox createContentCredits() {
		Font font = Font.font(18);

		Text textHead = new Text("FXGL (JavaFX 2D Game Library) " + Version.getAsString());
		textHead.setFont(font);
		textHead.setFill(Color.WHITE);

		Text textJFX = new Text("Graphics and Application Framework: JavaFX 8.0.51");
		textJFX.setFont(font);
		textJFX.setFill(Color.WHITE);

		Text textJBOX = new Text("Physics Engine: JBox2d 2.2.1.1 (jbox2d.org)");
		textJBOX.setFont(font);
		textJBOX.setFill(Color.WHITE);

		Text textAuthor = new Text("Author: Almas Baimagambetov (AlmasB)");
		textAuthor.setFont(font);
		textAuthor.setFill(Color.WHITE);

		Text textDev = new Text("Source code available: https://github.com/AlmasB/FXGL");
		textDev.setFont(font);
		textDev.setFill(Color.WHITE);

		return new VBox(textHead, textJFX, textJBOX, textAuthor, textDev);
	}

	private static class Title extends StackPane {
		private Text text;

		public Title(String name) {
			text = new Text(name);
			text.setFill(Color.WHITE);
			text.setFont(Font.font("", FontWeight.SEMI_BOLD, 50));

			Rectangle bg = new Rectangle(text.getLayoutBounds().getWidth() + 20, 60);
			bg.setStroke(Color.WHITE);
			bg.setStrokeWidth(2);
			bg.setFill(null);

			setAlignment(Pos.CENTER);
			getChildren().addAll(bg, text);
		}

		public double getLayoutWidth() {
			return text.getLayoutBounds().getWidth() + 20;
		}

		public double getLayoutHeight() {
			return text.getLayoutBounds().getHeight() + 20;
		}
	}

	private static class MenuBox extends HBox {
		public MenuBox(int width, MenuItem... items) {
			for (MenuItem item : items) {
				getChildren().addAll(item, createSeparator(width));
			}
		}

		private Line createSeparator(int width) {
			Line sep = new Line();
			sep.setEndX(width);
			sep.setVisible(false);
			return sep;
		}

		// TODO: FIX
		public double getLayoutHeight() {
			return 10 * getChildren().size();
		}
	}

	public class MenuItem extends StackPane {
		private Element thiElement;
		private boolean highlight;

		private Text text;

		private Background defBack;
		private Color defTexFill;

		public MenuItem(String name) {
			LinearGradient gradient = new LinearGradient(0, 1, 0, 0, true, CycleMethod.NO_CYCLE,
					new Stop[] { new Stop(0.5, Color.DARKRED), new Stop(1, Color.RED) });
			
			highlight = false;
			defBack = new Background(new BackgroundFill(Color.BLACK, new CornerRadii(7), new Insets(1)));
			defTexFill = Color.WHITE;
			this.setBackground(defBack);
			this.setBorder(new Border(
					new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, new CornerRadii(5), BorderWidths.DEFAULT)));

			Rectangle bg = new Rectangle(150, 30);
			bg.setVisible(false);
			// this.setOpacity(0.4);

			text = new Text(name);
			text.setFill(defTexFill);
			text.setFont(Font.font("", FontWeight.SEMI_BOLD, 22));

			setAlignment(Pos.CENTER);
			getChildren().addAll(text, bg);

			setOnMouseEntered(event -> {
				this.setBackground(new Background(new BackgroundFill(gradient, new CornerRadii(5), new Insets(1))));
			});

			setOnMouseExited(event -> {
				this.setBackground(defBack);
				text.setFill(defTexFill);
			});

			setOnMousePressed(event -> {
				this.setBackground(new Background(new BackgroundFill(Color.RED, new CornerRadii(5), new Insets(1))));
			});

			setOnMouseReleased(event -> {
				this.setBackground(new Background(new BackgroundFill(gradient, new CornerRadii(5), new Insets(1))));
			});
		}

		public void setAction(Runnable action) {
			this.setOnMouseClicked(event -> {
				action.run();
			});
		}

		/*public void setEnabled(boolean b) {
			this.setDisable(!b);
			this.setOpacity(b ? 1 : 0.33);
		}

		public boolean getHighlighted() {
			return highlight;
		}*/

		public void setHighlighted(boolean b) {
			highlight = b;
			if (highlight) {
				defBack = new Background(new BackgroundFill(Color.YELLOW, new CornerRadii(5), new Insets(1)));
				defTexFill = Color.BLACK;
			} else {
				defBack = new Background(new BackgroundFill(Color.BLACK, new CornerRadii(5), new Insets(1)));
				defTexFill = Color.WHITE;
			}
			this.setBackground(defBack);
			text.setFill(defTexFill);
		}

		public Element getElement() {
			return thiElement;
		}

		public void setElement(Element e) {
			thiElement = e;
		}
	}

	private class PowerGroup extends GridPane {
		public PowerGroup() {
			super();
			this.setVgap(3);
			this.setHgap(3);
		}

		public void addItems(ArrayList<MenuItem> items) {
			this.getChildren().clear();
			int val = 0;
			for (MenuItem item : items) {
				this.add(item, val % 3, val / 3);
				val++;
			}
		}
	}

	public class TabItem{
    	private StringProperty itAction;
    	private StringProperty itKey;
    	private Actions action;
    	private KeyCode itKeyCode;

    	private TabItem(Actions act, KeyCode ke){
    		action = act;
    		itKeyCode = ke;
    		if(act == Actions.CHPOWN){
    			itAction = new SimpleStringProperty("PREVIOUS POWER");
    		}
    		else if(act == Actions.CHPOWP){
    			itAction = new SimpleStringProperty("NEXT POWER");
    		}
    		else if(act == Actions.INTERACT){
    			itAction = new SimpleStringProperty("EAT/ENTER");
    		}
    		else{
    			itAction = new SimpleStringProperty(act.toString());
    		}
    		itKey = new SimpleStringProperty(ke.toString());
    	}

    	public StringProperty itActionProperty() {
            return itAction;
        }

        public void setAction(String action) {
            itAction.set(action);
        }

        public StringProperty itKeyProperty() {
            return itKey;
        }

        public void setKey(KeyCode k) {
            itKey.set(k.toString());
            itKeyCode = k;
        }

        public KeyCode getKey(){
        	return itKeyCode;
        }

        public Actions getAction(){
        	return action;
        }
    }

    @Override
    protected com.almasb.fxgl.ui.FXGLMenu.MenuBox createMenuBody() {
        return new com.almasb.fxgl.ui.FXGLMenu.MenuBox(200);
    }
    
    private class SoundBar extends Group{
		private DoubleProperty curValue;
		private double maxValue;
		private Rectangle backBar;
		private Text textValue;
		public SoundBar(int width, int height, DoubleProperty cV, double mV, Text t, Paint p){
			curValue = cV;
			maxValue = mV;
			backBar = new Rectangle(width, height, Color.BLACK);
			textValue = t;
			this.getChildren().add(backBar);
			
			for(int i = 0; i < maxValue; i++){
				Rectangle r = new Rectangle(i*width/maxValue + 1, 2, (width - maxValue)/maxValue, height - 4);
				r.setFill(p);
				this.getChildren().add(r);
			}
			
			for(int i = 0; i < getChildren().size(); i++){
				getChildren().get(i).setVisible(i <= curValue.doubleValue()*100);
			}
			textValue.setText(Math.round(curValue.doubleValue()*100) + "%");
			
			curValue.addListener(new ChangeListener<Number>(){
				@Override
				public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
					for(int i = 0; i < getChildren().size(); i++){
						getChildren().get(i).setVisible(i <= arg2.doubleValue()*100);
					}
					textValue.setText(Math.round(arg2.doubleValue()*100) + "%");
				}});
		}
	}
}