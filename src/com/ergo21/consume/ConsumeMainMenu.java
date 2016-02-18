package com.ergo21.consume;

import java.util.ArrayList;
import java.util.HashMap;

import com.almasb.consume.ConsumeApp;
import com.almasb.consume.Types.Actions;
import com.almasb.fxgl.asset.AssetManager;
import com.almasb.fxgl.asset.Music;
import com.almasb.fxgl.asset.SaveLoadManager;
import com.almasb.fxgl.event.MenuEvent;
import com.almasb.fxgl.ui.FXGLMenu;
import com.almasb.fxgl.util.Version;
import com.sun.javafx.scene.control.skin.LabeledText;

import javafx.animation.FadeTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
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
import javafx.util.Duration;

/**
 * This is the default FXGL menu used if the users don't provide their own
 *
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 *
 */
public final class ConsumeMainMenu extends FXGLMenu {

    private double menuX, menuY;
    private ConsumeApp consApp;
    private int mainWidth = 200;
    private int subWidth = 100;
    private ListView<String> saveList;
    private MenuItem itemContinue;
    private MenuContent emptyMenu;

    public ConsumeMainMenu(ConsumeApp app) {
        super(app);
        consApp = app;
        saveList = new ListView<String>();
        emptyMenu = new MenuContent();
        consApp.getAudioManager().setGlobalMusicVolume(consApp.sSettings.getBackMusicVolume());
		consApp.getAudioManager().setGlobalSoundVolume(consApp.sSettings.getSFXVolume());
        MenuBox menu = createMainMenu();
        menuX = 50;
        menuY = consApp.getHeight() / 2 - menu.getLayoutHeight() / 2;

        // just a placeholder
        MenuBox menuContent = new MenuBox((int) consApp.getWidth() - 300 - 50);
        menuContent.setTranslateX(300);
        menuContent.setTranslateY(menu.getTranslateY());
        menuContent.setVisible(false);

        Rectangle bg = new Rectangle(consApp.getWidth(), consApp.getHeight());
        bg.setFill(Color.rgb(10, 1, 1));

        Title title = new Title(consApp.getSettings().getTitle());
        title.setTranslateX(
                consApp.getWidth() / 2 - title.getLayoutWidth() / 2);
        title.setTranslateY(
                menu.getTranslateY() / 2 - title.getLayoutHeight() / 2);

        Text version = new Text("v" + consApp.getSettings().getVersion());
        version.setTranslateY(consApp.getHeight() - 2);
        version.setFill(Color.WHITE);
        version.setFont(Font.font(18));

        getChildren().setAll(bg, title, version, menu, menuContent);

        Music m = consApp.getAssetManager().loadMusic(FileNames.THEME_MUSIC);
        m.setCycleCount(Integer.MAX_VALUE);
        consApp.getAudioManager().playMusic(m);
        
    }

    private MenuBox createMainMenu() {
        itemContinue = new MenuItem(mainWidth, "Continue");
        itemContinue.setEnabled(
                SaveLoadManager.INSTANCE.loadFileNames().isPresent()
                        && SaveLoadManager.INSTANCE.loadFileNames().get()
                                .contains(consApp.sSettings.getLastSave()));
        itemContinue.setAction(() -> {
            try {
                if (!SaveLoadManager.INSTANCE.loadFileNames().get()
                        .contains(consApp.sSettings.getLastSave())) {
                    itemContinue.setEnabled(
                            SaveLoadManager.INSTANCE.loadFileNames().isPresent()
                                    && SaveLoadManager.INSTANCE.loadFileNames()
                                            .get().contains(
                                                    consApp.sSettings
                                                            .getLastSave()));
                    return;
                }
                GameSave data = SaveLoadManager.INSTANCE
                        .load(consApp.sSettings.getLastSave());
                Rectangle bg = new Rectangle(consApp.getWidth(),
                        consApp.getHeight());
                bg.setFill(Color.rgb(10, 1, 1));
                bg.setOpacity(1);
                FadeTransition ft = new FadeTransition(Duration.seconds(1), bg);
                ft.setFromValue(1);
                ft.setToValue(1);
                ft.setOnFinished(evt -> {
                    consApp.getSceneManager().removeUINode(bg);
                });

                itemContinue.fireEvent(
                        new MenuEvent(getScene(), itemContinue, MenuEvent.LOAD,
                                data));

                ft.play();
                consApp.getSceneManager().addUINodes(bg);

                consApp.soundManager.getBackgroundMusic()
                        .setCycleCount(Integer.MAX_VALUE);
                consApp.soundManager.playBackgroundMusic();

                switchMenuContentTo(emptyMenu);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });

        MenuItem itemNewGame = new MenuItem(mainWidth, "New Game");
        itemNewGame.setAction(() -> {
            switchMenuContentTo(emptyMenu);
            Rectangle bg = new Rectangle(consApp.getWidth(),
                    consApp.getHeight());
            bg.setFill(Color.rgb(10, 1, 1));
            bg.setOpacity(1);
            FadeTransition ft = new FadeTransition(Duration.seconds(1), bg);
            ft.setFromValue(1);
            ft.setToValue(0);
            ft.setOnFinished(evt -> consApp.getSceneManager().removeUINode(bg));

            itemNewGame.fireEvent(new MenuEvent(MenuEvent.NEW_GAME));

            FadeTransition ft2 = new FadeTransition(Duration.seconds(1), bg);
            ft2.setFromValue(1);
            ft2.setToValue(1);
            ft2.setOnFinished(evt -> ft.play());
            ft2.play();
            consApp.getSceneManager().addUINodes(bg);
        });

        MenuItem itemLoad = new MenuItem(mainWidth, "Load");
        itemLoad.setMenuContent(createContentLoadConsume());
        itemLoad.setOnMouseClicked(event -> {
            refreshSaveList();
            switchMenuContentTo(itemLoad.getMenuContent());
        });

        MenuItem itemOptions = new MenuItem(mainWidth, "OPTIONS");
        itemOptions.setChild(createOptionsMenuConsume());

        MenuItem itemExtra = new MenuItem(mainWidth, "EXTRA");
        itemExtra.setChild(createExtraMenuConsume());

        MenuItem itemExit = new MenuItem(mainWidth, "Exit");
        itemExit.setAction(
                () -> itemExit.fireEvent(new MenuEvent(MenuEvent.EXIT)));

        MenuBox menu = new MenuBox(mainWidth, itemContinue, itemNewGame,
                itemLoad, itemOptions, itemExtra, itemExit);
        menu.setTranslateX(50);
        menu.setTranslateY(
                consApp.getHeight() / 2 - menu.getLayoutHeight() / 2);
        return menu;
    }

    private MenuContent createContentLoadConsume() {
        refreshSaveList();
        saveList.prefHeightProperty()
                .bind(Bindings.size(saveList.getItems()).multiply(36));

        if (saveList.getItems().size() > 0) {
            saveList.getSelectionModel().selectFirst();
        }

        MenuItem btnLoad = new MenuItem(subWidth, "Load");
        btnLoad.setAction(() -> {
            String fileName = saveList.getSelectionModel().getSelectedItem();
            if (fileName == null)
                return;

            try {
                GameSave data = SaveLoadManager.INSTANCE.load(fileName);
                Rectangle bg = new Rectangle(consApp.getWidth(),
                        consApp.getHeight());
                bg.setFill(Color.rgb(10, 1, 1));
                bg.setOpacity(1);
                FadeTransition ft = new FadeTransition(Duration.seconds(1), bg);
                ft.setFromValue(1);
                ft.setToValue(1);
                ft.setOnFinished(evt -> {
                    consApp.getSceneManager().removeUINode(bg);
                });

                btnLoad.fireEvent(
                        new MenuEvent(getScene(), btnLoad, MenuEvent.LOAD,
                                data));

                ft.play();
                consApp.getSceneManager().addUINodes(bg);

                consApp.soundManager.getBackgroundMusic()
                        .setCycleCount(Integer.MAX_VALUE);
                consApp.soundManager.playBackgroundMusic();

                consApp.sSettings.setLastSave(fileName);
                switchMenuContentTo(emptyMenu);
            }
            catch (Exception e) {
                // TODO: use custom stages, as alerts will kick users from the
                // fullscreen
                Alert alert = new Alert(AlertType.ERROR);
                alert.setContentText(
                        "Failed to load file: " + fileName + ". Error: "
                                + e.getMessage());
                alert.showAndWait();
            }
        });
        MenuItem btnDelete = new MenuItem(subWidth, "Delete");
        btnDelete.setAction(() -> {
            String fileName = saveList.getSelectionModel().getSelectedItem();
            if (fileName == null)
                return;

            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setContentText(
                    SaveLoadManager.INSTANCE.delete(fileName)
                            ? "File was deleted" : "File couldn't be deleted");
            alert.showAndWait();

            refreshSaveList();
        });

        HBox hbox = new HBox(50, btnLoad, btnDelete);
        hbox.setAlignment(Pos.CENTER);

        return new MenuContent(saveList, hbox);
    }

    private MenuBox createOptionsMenuConsume() {
        MenuItem itemControls = new MenuItem(mainWidth, "Controls");
        MenuContent mcc = createContentControls();
        mcc.getStylesheets().add("assets/ui/css/menu.css");
        itemControls.setMenuContent(mcc);

        MenuItem itemAudio = new MenuItem(mainWidth, "Audio");
        MenuContent menuContent = createContentAudio();
        itemAudio.setMenuContent(menuContent);
        itemAudio.setOnMouseClicked(event -> {
            switchMenuContentTo(menuContent);
            consApp.backVol.set(consApp.sSettings.getBackMusicVolume());
            consApp.sfxVol.set(consApp.sSettings.getSFXVolume());
            musTexVal.setText(Math.round(consApp.backVol.get() * 100) + "%");
            sfxTexVal.setText(Math.round(consApp.sfxVol.get() * 100) + "%");
        });

        return new MenuBox(mainWidth, itemControls, itemAudio);
    }

    private MenuBox createExtraMenuConsume() {
        MenuItem itemCredits = new MenuItem(mainWidth, "CREDITS");
        itemCredits.setMenuContent(createContentCredits());

        return new MenuBox(mainWidth, itemCredits);
    }

    private MenuContent createContentCredits() {
        Font font = Font.font(18);

        Text textHead = new Text(
                "FXGL (JavaFX 2D Game Library) " + Version.getAsString());
        textHead.setFont(font);
        textHead.setFill(Color.WHITE);

        Text textJFX = new Text(
                "Graphics and Application Framework: JavaFX 8.0.51");
        textJFX.setFont(font);
        textJFX.setFill(Color.WHITE);

        Text textJBOX = new Text("Physics Engine: JBox2d 2.2.1.1 (jbox2d.org)");
        textJBOX.setFont(font);
        textJBOX.setFill(Color.WHITE);

        Text textAuthor = new Text("Author: Almas Baimagambetov (AlmasB)");
        textAuthor.setFont(font);
        textAuthor.setFill(Color.WHITE);

        Text textDev = new Text(
                "Source code available: https://github.com/AlmasB/FXGL");
        textDev.setFont(font);
        textDev.setFill(Color.WHITE);

        return new MenuContent(textHead, textJFX, textJBOX, textAuthor,
                textDev);
    }
    
    private Text musTexVal;
    private Text sfxTexVal;

    private MenuContent createContentAudio() {
        Text musTex = new Text("Music Volume");
        musTex.setStroke(Color.WHITE);
        musTex.setFill(Color.WHITE);
        musTex.setFont(new Font(20));
        musTexVal = new Text();
        musTexVal.setStroke(Color.WHITE);
        musTexVal.setFill(Color.WHITE);
        SoundBar musBar = new SoundBar(200, 15, consApp.backVol, 100, musTexVal, Color.YELLOW);
        musBar.setOnMouseClicked(event -> {
        	consApp.backVol.set((event.getX()) / 200);
        });
        musBar.setOnMouseDragged(event -> {
            double val = (event.getX()) / 200;
            if (val < 0) {
                val = 0;
            }
            else if (val > 1) {
                val = 1;
            }
            consApp.backVol.set(val);
        });

        HBox musBlock = new HBox(musBar, musTexVal);
        musTexVal.setTranslateX(musTexVal.getTranslateX() + 5);

        Text sfxTex = new Text("Sound Effects Volume");
        sfxTex.setStroke(Color.WHITE);
        sfxTex.setFill(Color.WHITE);
        sfxTex.setFont(new Font(20));
        sfxTexVal = new Text();
        sfxTexVal.setStroke(Color.WHITE);
        sfxTexVal.setFill(Color.WHITE);
        SoundBar sfxBar = new SoundBar(200, 15, consApp.sfxVol, 100, sfxTexVal, Color.YELLOW);
        sfxBar.setOnMouseClicked(event -> {
        	consApp.sfxVol.set((event.getX()) / 200);
        });
        sfxBar.setOnMouseDragged(event -> {
            double val = (event.getX()) / 200;
            if (val < 0) {
                val = 0;
            }
            else if (val > 1) {
                val = 1;
            }
            consApp.sfxVol.set(val);
        });
        HBox sfxBlock = new HBox(sfxBar, sfxTexVal);
        sfxTexVal.setTranslateX(sfxTexVal.getTranslateX() + 5);

        MenuItem savAud = new MenuItem(subWidth, "Save");
        savAud.setOnMouseClicked(event -> {
            consApp.sSettings.setBackMusicVolume(consApp.backVol.doubleValue());
            consApp.sSettings.setSFXVolume(consApp.sfxVol.doubleValue());
            consApp.getAudioManager().setGlobalMusicVolume(consApp.sSettings.getBackMusicVolume());
			consApp.getAudioManager().setGlobalSoundVolume(consApp.sSettings.getSFXVolume());
        });

        MenuContent mc = new MenuContent(musTex, musBlock, sfxTex, sfxBlock,
                savAud);
        return mc;
    }

    private MenuContent createContentControls() {
        TableView<TabItem> center = new TableView<TabItem>();
        center.setMaxHeight(consApp.getHeight() / 2);
        center.setPrefWidth(consApp.getWidth() / 2);

        TableColumn<TabItem, String> action = new TableColumn<TabItem, String>(
                "Action");
        action.setResizable(false);
        action.setMinWidth(consApp.getWidth() / 4.2);
        action.setCellValueFactory(new PropertyValueFactory<>("itAction"));
        action.setStyle("-fx-alignment: CENTER");

        TableColumn<TabItem, String> key = new TableColumn<TabItem, String>(
                "Key");
        key.setResizable(false);
        key.setMinWidth(consApp.getWidth() / 4.2);
        key.setCellValueFactory(new PropertyValueFactory<>("itKey"));
        key.setStyle("-fx-alignment: CENTER");

        HashMap<Actions, KeyCode> tKeys = consApp.consController
                .getCurrentKeys();
        ObservableList<TabItem> items = FXCollections.observableArrayList();
        for (Actions actionItem : Actions.values()) {
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
        center.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                if (center.getItems()
                        .get(center.getFocusModel().getFocusedCell().getRow())
                        .getKey() == KeyCode.UNDEFINED) {
                    center.getItems().get(
                            center.getFocusModel().getFocusedCell().getRow())
                            .setKey(ke.getCode());
                }

            }
        });
        center.getColumns().add(action);
        center.getColumns().add(key);
        center.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        MenuItem itemSave = new MenuItem(subWidth, "Save");
        itemSave.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                HashMap<Actions, KeyCode> newKeyMap = new HashMap<Actions, KeyCode>();
                for (TabItem item : items) {
                    if (newKeyMap.values().contains(item.getKey())
                            && item.getKey() != KeyCode.UNDEFINED) {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setContentText(
                                "Duplicate keys found. Changes not saved.");
                        alert.showAndWait();
                        return;
                    }
                    else if(item.getKey() == KeyCode.UNDEFINED){
                    	Alert alert = new Alert(AlertType.ERROR);
                        alert.setContentText(
                                "Key undefined. Changes not saved.");
                        alert.showAndWait();
                        return;
                    }
                    else {
                        newKeyMap.put(item.getAction(), item.getKey());
                    }
                }
                consApp.consController.initControls(newKeyMap);
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setHeaderText("Saved");
                alert.setContentText(
                        "Key changes saved.");
                alert.showAndWait();
            }
        });
        MenuItem itemRestore = new MenuItem(mainWidth, "Restore to Default");
        itemRestore.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                HashMap<Actions, KeyCode> dKeys = consApp.consController
                        .getDefaultKeys();
                items.clear();
                for (Actions actionItem : Actions.values()) {
                    items.add(new TabItem(actionItem, dKeys.get(actionItem)));
                }
            }
        });
        
        return new MenuContent(center, itemSave, itemRestore);
    }

    private void refreshSaveList() {
        itemContinue.setEnabled(
                SaveLoadManager.INSTANCE.loadFileNames().isPresent()
                        && SaveLoadManager.INSTANCE.loadFileNames().get()
                                .contains(consApp.sSettings.getLastSave()));
        SaveLoadManager.INSTANCE.loadFileNames()
                .ifPresent(names -> saveList.getItems().setAll(names));
        ArrayList<String> removes = new ArrayList<String>();
        for (String item : saveList.getItems()) {
            if (item.endsWith(".set")) {
                removes.add(item);
            }
        }
        saveList.getItems().removeAll(removes);
    }

    private void switchMenuTo(MenuBox menu) {
        Node oldMenu = getChildren().get(3);

        FadeTransition ft = new FadeTransition(Duration.seconds(0.33), oldMenu);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            menu.setTranslateX(menuX);
            menu.setTranslateY(menuY);
            menu.setOpacity(0);
            getChildren().set(3, menu);
            oldMenu.setOpacity(1);

            FadeTransition ft2 = new FadeTransition(Duration.seconds(0.33),
                    menu);
            ft2.setToValue(1);
            ft2.play();
        });
        ft.play();
        
        switchMenuContentTo(emptyMenu);
    }

    private void switchMenuContentTo(MenuContent content) {
        content.setTranslateX(menuX * 2 + 200);
        content.setTranslateY(menuY);
        getChildren().set(4, content);
    }

    private static class Title extends StackPane {
        private Text text;

        public Title(String name) {
            text = new Text(name);
            text.setFill(Color.WHITE);
            text.setFont(Font.font("", FontWeight.SEMI_BOLD, 50));

            Rectangle bg = new Rectangle(text.getLayoutBounds().getWidth() + 20,
                    60);
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

    private static class MenuBox extends VBox {
        public MenuBox(int width, MenuItem... items) {
            getChildren().add(createSeparator(width));

            for (MenuItem item : items) {
                item.setParent(this);
                getChildren().addAll(item, createSeparator(width));
            }
        }

        private Line createSeparator(int width) {
            Line sep = new Line();
            sep.setEndX(width);
            sep.setStroke(Color.DARKGREY);
            return sep;
        }

        /*public double getLayoutWidth() {
            return 200;
        }*/

        // TODO: FIX
        public double getLayoutHeight() {
            return 10 * getChildren().size();
        }
    }

    private class MenuItem extends StackPane {
        private MenuBox parent;
        private MenuBox child;
        private MenuContent menuContent;

        private Text text = new Text();

        public MenuItem(int width, String name) {
            LinearGradient gradient = new LinearGradient(0, 1, 0, 0, true, CycleMethod.NO_CYCLE,
					new Stop[] { new Stop(0.5, Color.DARKRED), new Stop(1, Color.RED) });

            Rectangle bg = new Rectangle(width, 30);
            bg.setOpacity(1);

            text.setText(name);
            text.setFill(Color.DARKGREY);
            text.setFont(Font.font("", FontWeight.SEMI_BOLD, 22));

            setAlignment(Pos.CENTER);
            getChildren().addAll(bg, text);

            setOnMouseEntered(event -> {
                bg.setFill(gradient);
                text.setFill(Color.WHITE);
            });

            setOnMouseExited(event -> {
                bg.setFill(Color.BLACK);
                text.setFill(Color.DARKGREY);
            });

            setOnMousePressed(event -> {
                bg.setFill(Color.RED);
            });

            setOnMouseReleased(event -> {
                bg.setFill(gradient);
            });
        }

        public void setParent(MenuBox menu) {
            parent = menu;
        }

        public void setMenuContent(MenuContent content) {
            menuContent = content;
            this.setOnMouseClicked(event -> {
                switchMenuContentTo(menuContent);
            });
        }

        public void setChild(MenuBox menu) {
            child = menu;

            MenuItem back = new MenuItem(mainWidth, "Back");
            menu.getChildren().add(back);

            back.setOnMouseClicked(evt -> {
                switchMenuTo(MenuItem.this.parent);
                switchMenuContentTo(emptyMenu);
            });

            this.setOnMouseClicked(event -> {
                switchMenuTo(menu);
            });
        }

        public void setAction(Runnable action) {
            this.setOnMouseClicked(event -> {
                action.run();
            });
        }

        /*public MenuBox getMenuParent() {
            return parent;
        }*/

        public MenuContent getMenuContent() {
            return menuContent;
        }

        public void setEnabled(boolean b) {
            this.setDisable(!b);
            this.setOpacity(b ? 1 : 0.33);
        }
    }

    private class MenuContent extends VBox {
        public MenuContent(Node... items) {
            if (items.length != 0) {
                getChildren().add(
                        createSeparator((int) consApp.getWidth() - 300 - 50));
                for (Node item : items) {
                    getChildren().addAll(
                            item,
                                createSeparator(
                                        (int) consApp.getWidth() - 300 - 50));
                }
            }
        }

        private Line createSeparator(int width) {
            Line sep = new Line();
            sep.setEndX(width);
            sep.setStroke(Color.DARKGREY);
            return sep;
        }
    }

    public class TabItem {
        private StringProperty itAction;
        private StringProperty itKey;
        private Actions action;
        private KeyCode itKeyCode;

        private TabItem(Actions act, KeyCode ke) {
            action = act;
            itKeyCode = ke;
            if (act == Actions.CHPOWN) {
                itAction = new SimpleStringProperty("PREVIOUS POWER");
            }
            else if (act == Actions.CHPOWP) {
                itAction = new SimpleStringProperty("NEXT POWER");
            }
            else {
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

        public KeyCode getKey() {
            return itKeyCode;
        }

        public Actions getAction() {
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