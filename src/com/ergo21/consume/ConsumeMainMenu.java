/* The MIT License (MIT)
*
* FXGL - JavaFX Game Library
*
* Copyright (c) 2015 AlmasB (almaslvl@gmail.com)
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*/
package com.ergo21.consume;

import java.util.ArrayList;
import java.util.HashMap;

import com.almasb.consume.ConsumeApp;
import com.almasb.consume.Types.Actions;
import com.almasb.fxgl.asset.AssetManager;
import com.almasb.fxgl.asset.SaveLoadManager;
import com.almasb.fxgl.ui.Menu;
import com.almasb.fxgl.util.Version;

import javafx.animation.FadeTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
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
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
* This is the default FXGL menu used if the users
* don't provide their own
*
* @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
*
*/
public final class ConsumeMainMenu extends Menu {

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
       MenuBox menu = createMainMenu();
       menuX = 50;
       menuY = consApp.getHeight() / 2 - menu.getLayoutHeight() / 2;

       // just a placeholder
       MenuBox menuContent = new MenuBox((int)consApp.getWidth() - 300 - 50);
       menuContent.setTranslateX(300);
       menuContent.setTranslateY(menu.getTranslateY());
       menuContent.setVisible(false);

       Rectangle bg = new Rectangle(consApp.getWidth(), consApp.getHeight());
       bg.setFill(Color.rgb(10, 1, 1));

       Title title = new Title(consApp.getSettings().getTitle());
       title.setTranslateX(consApp.getWidth() / 2 - title.getLayoutWidth() / 2);
       title.setTranslateY(menu.getTranslateY() / 2 - title.getLayoutHeight() / 2);

       Text version = new Text("v" + consApp.getSettings().getVersion());
       version.setTranslateY(consApp.getHeight() - 2);
       version.setFill(Color.WHITE);
       version.setFont(Font.font(18));

       root.getChildren().addAll(bg, title, version, menu, menuContent);
   }

   private MenuBox createMainMenu() {
       itemContinue = new MenuItem(mainWidth, "Continue");
       itemContinue.setEnabled(SaveLoadManager.INSTANCE.loadFileNames().isPresent() &&
    		   SaveLoadManager.INSTANCE.loadFileNames().get().contains(consApp.sSettings.getLastSave()));
       itemContinue.setAction(() -> {
    	   try {
    		   if(!SaveLoadManager.INSTANCE.loadFileNames().get().contains(consApp.sSettings.getLastSave())){
    			   itemContinue.setEnabled(SaveLoadManager.INSTANCE.loadFileNames().isPresent() &&
    		    		   SaveLoadManager.INSTANCE.loadFileNames().get().contains(consApp.sSettings.getLastSave()));
    			   return;
    		   }
    		   GameSave data = SaveLoadManager.INSTANCE.load(consApp.sSettings.getLastSave());
    		   Rectangle bg = new Rectangle(consApp.getWidth(), consApp.getHeight());
    		   bg.setFill(Color.rgb(10, 1, 1));
    		   bg.setOpacity(1);
    		   FadeTransition ft = new FadeTransition(Duration.seconds(1), bg);
    		   ft.setFromValue(1);
    		   ft.setToValue(1);
    		   ft.setOnFinished(evt ->{consApp.getSceneManager().removeUINode(bg);});
    		   consApp.startNewGame();
    		   consApp.loadState(data);
    		   ft.play();
    		   consApp.getSceneManager().addUINodes(bg);

    		   consApp.soundManager.getBackgroundMusic().setCycleCount(Integer.MAX_VALUE);
    		   consApp.soundManager.playBackgroundMusic();

    		   switchMenuContentTo(emptyMenu);
    	   } catch (Exception e) {
    		   e.printStackTrace();
    	   }
       });

       MenuItem itemNewGame = new MenuItem(mainWidth, "New Game");
       itemNewGame.setAction(() -> {
    	   switchMenuContentTo(emptyMenu);
    	   Rectangle bg = new Rectangle(consApp.getWidth(), consApp.getHeight());
		   bg.setFill(Color.rgb(10, 1, 1));
		   bg.setOpacity(1);
		   FadeTransition ft = new FadeTransition(Duration.seconds(1), bg);
		   ft.setFromValue(1);
		   ft.setToValue(0);
		   ft.setOnFinished(evt -> consApp.getSceneManager().removeUINode(bg));
		   consApp.startNewGame();
		   FadeTransition ft2 = new FadeTransition(Duration.seconds(1), bg);
		   ft2.setFromValue(1);
		   ft2.setToValue(1);
		   ft2.setOnFinished(evt -> ft.play());
		   ft2.play();
		   consApp.getSceneManager().addUINodes(bg);
       });

       MenuItem itemLoad = new MenuItem(mainWidth, "Load");
       itemLoad.setMenuContent(createContentLoad());
       itemLoad.setOnMouseClicked(event -> {
    	   refreshSaveList();
    	   switchMenuContentTo(itemLoad.getMenuContent());
       });

       MenuItem itemOptions = new MenuItem(mainWidth, "OPTIONS");
       itemOptions.setChild(createOptionsMenu());

       MenuItem itemExtra = new MenuItem(mainWidth, "EXTRA");
       itemExtra.setChild(createExtraMenu());

       MenuItem itemExit = new MenuItem(mainWidth, "Exit");
       itemExit.setAction(consApp::exit);

       MenuBox menu = new MenuBox(mainWidth, itemContinue, itemNewGame, itemLoad, itemOptions, itemExtra, itemExit);
       menu.setTranslateX(50);
       menu.setTranslateY(consApp.getHeight() / 2 - menu.getLayoutHeight() / 2);
       return menu;
   }

   private MenuContent createContentLoad() {
	   refreshSaveList();
       saveList.prefHeightProperty().bind(Bindings.size(saveList.getItems()).multiply(36));

       try {
           String css = AssetManager.INSTANCE.loadCSS("listview.css");
           saveList.getStylesheets().add(css);
       }
       catch (Exception e) {}

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
    		   Rectangle bg = new Rectangle(consApp.getWidth(), consApp.getHeight());
    		   bg.setFill(Color.rgb(10, 1, 1));
    		   bg.setOpacity(1);
    		   FadeTransition ft = new FadeTransition(Duration.seconds(1), bg);
    		   ft.setFromValue(1);
    		   ft.setToValue(1);
    		   ft.setOnFinished(evt ->{consApp.getSceneManager().removeUINode(bg);});
    		   consApp.startNewGame();
    		   consApp.loadState(data);
    		   ft.play();
    		   consApp.getSceneManager().addUINodes(bg);

               consApp.soundManager.getBackgroundMusic().setCycleCount(Integer.MAX_VALUE);
               consApp.soundManager.playBackgroundMusic();

    		   consApp.sSettings.setLastSave(fileName);
    		   switchMenuContentTo(emptyMenu);
           }
           catch (Exception e) {
               // TODO: use custom stages, as alerts will kick users from the fullscreen
               Alert alert = new Alert(AlertType.ERROR);
               alert.setContentText("Failed to load file: " + fileName + ". Error: " + e.getMessage());
               alert.showAndWait();
           }
       });
       MenuItem btnDelete = new MenuItem(subWidth, "Delete");
       btnDelete.setAction(() -> {
           String fileName = saveList.getSelectionModel().getSelectedItem();
           if (fileName == null)
               return;

           Alert alert = new Alert(AlertType.INFORMATION);
           alert.setContentText(SaveLoadManager.INSTANCE.delete(fileName) ? "File was deleted" : "File couldn't be deleted");
           alert.showAndWait();

           refreshSaveList();
       });

       HBox hbox = new HBox(50, btnLoad, btnDelete);
       hbox.setAlignment(Pos.CENTER);

       return new MenuContent(saveList, hbox);
   }

   private MenuBox createOptionsMenu() {
       MenuItem itemControls = new MenuItem(mainWidth, "Controls");
       itemControls.setMenuContent(createContentControls());

       MenuItem itemVideo = new MenuItem(mainWidth, "VIDEO");
       MenuItem itemAudio = new MenuItem(mainWidth, "Audio");
       itemAudio.setMenuContent(createContentAudio());

       return new MenuBox(mainWidth, itemControls, itemVideo, itemAudio);
   }

   private MenuBox createExtraMenu() {
       MenuItem itemCredits = new MenuItem(mainWidth, "CREDITS");
       itemCredits.setMenuContent(createContentCredits());

       return new MenuBox(mainWidth, itemCredits);
   }

   private MenuContent createContentCredits() {
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

       return new MenuContent(textHead, textJFX, textJBOX, textAuthor, textDev);
   }

   private MenuContent createContentAudio(){
		Text musTex = new Text("Music Volume");
		musTex.setStroke(Color.WHITE);
		musTex.setFill(Color.WHITE);
		musTex.setFont(new Font(20));
		Text musTexVal = new Text();
		musTexVal.setStroke(Color.WHITE);
		musTexVal.setFill(Color.WHITE);
		ProgressBar musBar = new ProgressBar();
		musBar.setProgress(consApp.sSettings.getBackMusicVolume());
		musBar.setPadding(new Insets(0,5,20,0));
		musBar.setPrefSize(200, 15);
		musBar.setOnMouseClicked(event -> {
				musBar.setProgress((event.getX()+8)/200);
				musTexVal.setText(Math.round(musBar.getProgress() * 100) + "%");
		});
		musBar.setOnMouseDragged(event -> {
				double val = (event.getX()+8)/200;
				if(val < 0){
					val = 0;
				}
				else if(val > 1){
					val = 1;
				}
				musBar.setProgress(val);
				musTexVal.setText(Math.round(musBar.getProgress() * 100) + "%");
		});
		musTexVal.setText(Math.round(musBar.getProgress() * 100) + "%");

		HBox musBlock = new HBox(musBar, musTexVal);


		Text sfxTex = new Text("Sound Effects Volume");
		sfxTex.setStroke(Color.WHITE);
		sfxTex.setFill(Color.WHITE);
		sfxTex.setFont(new Font(20));
		Text sfxTexVal = new Text();
		sfxTexVal.setStroke(Color.WHITE);
		sfxTexVal.setFill(Color.WHITE);
		ProgressBar sfxBar = new ProgressBar();
		sfxBar.setProgress(consApp.sSettings.getSFXVolume());
		sfxBar.setPadding(new Insets(0,5,20,0));
		sfxBar.setPrefSize(200, 15);
		sfxBar.setOnMouseClicked(event -> {
				sfxBar.setProgress((event.getX()+8)/200);
				sfxTexVal.setText(Math.round(sfxBar.getProgress() * 100) + "%");
		});
		sfxBar.setOnMouseDragged(event -> {
				double val = (event.getX()+8)/200;
				if(val < 0){
					val = 0;
				}
				else if(val > 1){
					val = 1;
				}
				sfxBar.setProgress(val);
				sfxTexVal.setText(Math.round(sfxBar.getProgress() * 100) + "%");
		});
		sfxTexVal.setText(Math.round(sfxBar.getProgress() * 100) + "%");
		HBox sfxBlock = new HBox(sfxBar, sfxTexVal);

		MenuItem savAud = new MenuItem(subWidth, "Save");
		savAud.setOnMouseClicked(event -> {
				consApp.sSettings.setBackMusicVolume(musBar.getProgress());
				consApp.sSettings.setSFXVolume(sfxBar.getProgress());
		});

		MenuContent mc = new MenuContent(musTex, musBlock, sfxTex, sfxBlock, savAud);
		return mc;
	}

   private MenuContent createContentControls() {
	   TableView<TabItem> center = new TableView<TabItem>();
	   center.setMaxHeight(consApp.getHeight()/2);
	   center.setPrefWidth(consApp.getWidth()/2);
		//center.getC
		
	   TableColumn<TabItem, String> action = new TableColumn<TabItem, String>("Action");
	   action.setResizable(false); 
	   action.setPrefWidth(consApp.getWidth()/4);
	   action.setCellValueFactory(new PropertyValueFactory<>("itAction"));

	   TableColumn<TabItem, String> key = new TableColumn<TabItem, String>("Key");
	   key.setResizable(false);
	   key.setPrefWidth(consApp.getWidth()/4);
	   key.setCellValueFactory(new PropertyValueFactory<>("itKey"));

	   HashMap<Actions, KeyCode> tKeys = consApp.consController.getCurrentKeys();
	   ObservableList<TabItem> items = FXCollections.observableArrayList();
	   for(Actions actionItem : Actions.values()){
		   items.add(new TabItem(actionItem, tKeys.get(actionItem)));
	   }
	   center.setItems(items);
	   center.setOnMouseClicked(new EventHandler<MouseEvent>(){
		   @Override
		   public void handle(MouseEvent event) {
			   if(event.getClickCount() > 1){
				   center.getItems().get(center.getFocusModel().getFocusedCell().getRow()).setKey(KeyCode.UNDEFINED);
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


	   MenuItem itemSave = new MenuItem(subWidth, "Save");
	   itemSave.setOnMouseClicked(new EventHandler<MouseEvent>(){
		   @Override
		   public void handle(MouseEvent event) {
			   HashMap<Actions, KeyCode> newKeyMap = new HashMap<Actions, KeyCode>();
			   for(TabItem item : items){
				   if(newKeyMap.values().contains(item.getKey()) && item.getKey() != KeyCode.UNDEFINED){
					   Alert alert = new Alert(AlertType.ERROR);
					   alert.setContentText("Duplicate keys found. Changes not saved.");
					   alert.showAndWait();
					   return;
				   }
				   else{
					   newKeyMap.put(item.getAction(), item.getKey());
				   }
			   }
			   consApp.consController.initControls(newKeyMap);
		   }
	   });
	   MenuItem itemRestore = new MenuItem(mainWidth, "Restore to Default");
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

       return new MenuContent(center, itemSave, itemRestore);
   }

   private void refreshSaveList(){
	   itemContinue.setEnabled(SaveLoadManager.INSTANCE.loadFileNames().isPresent() &&
	    		   SaveLoadManager.INSTANCE.loadFileNames().get().contains(consApp.sSettings.getLastSave()));
	   SaveLoadManager.INSTANCE.loadFileNames().ifPresent(names -> saveList.getItems().setAll(names));
       ArrayList<String> removes = new ArrayList<String>();
       for(String item : saveList.getItems()){
    	   if(item.endsWith(".set")){
    		   removes.add(item);
    	   }
       }
       saveList.getItems().removeAll(removes);
   }

   private void switchMenuTo(MenuBox menu) {
       Node oldMenu = root.getChildren().get(3);

       FadeTransition ft = new FadeTransition(Duration.seconds(0.33), oldMenu);
       ft.setToValue(0);
       ft.setOnFinished(e -> {
           menu.setTranslateX(menuX);
           menu.setTranslateY(menuY);
           menu.setOpacity(0);
           root.getChildren().set(3, menu);
           oldMenu.setOpacity(1);

           FadeTransition ft2 = new FadeTransition(Duration.seconds(0.33), menu);
           ft2.setToValue(1);
           ft2.play();
       });
       ft.play();
   }

   private void switchMenuContentTo(MenuContent content) {
       content.setTranslateX(menuX * 2 + 200);
       content.setTranslateY(menuY);
       root.getChildren().set(4, content);
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

       public double getLayoutWidth() {
           return 200;
       }

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
           LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, new Stop[] {
                   new Stop(0.5, Color.hsb(33, 0.7, 0.7)),
                   new Stop(1, Color.hsb(100, 0.8, 1))
           });

           Rectangle bg = new Rectangle(width, 30);
           bg.setOpacity(0.4);

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
               bg.setFill(Color.GOLD);
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

           MenuItem back = new MenuItem(mainWidth, "BACK");
           menu.getChildren().add(back);

           back.setOnMouseClicked(evt -> {
               switchMenuTo(MenuItem.this.parent);
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

       public MenuBox getMenuParent() {
           return parent;
       }

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
           if(items.length != 0){
        	   getChildren().add(createSeparator((int)consApp.getWidth() - 300 - 50));
        	   for (Node item : items) {
                   getChildren().addAll(item, createSeparator((int)consApp.getWidth() - 300 - 50));
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
}