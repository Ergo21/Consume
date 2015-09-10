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

import java.io.Serializable;

import com.almasb.consume.ConsumeApp;
import com.almasb.fxgl.GameApplication;
import com.almasb.fxgl.asset.AssetManager;
import com.almasb.fxgl.asset.SaveLoadManager;
import com.almasb.fxgl.ui.Menu;
import com.almasb.fxgl.util.Version;

import javafx.animation.FadeTransition;
import javafx.beans.binding.Bindings;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.GridPane;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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

   public ConsumeMainMenu(ConsumeApp app) {
       super(app);
       consApp = app;

       MenuBox menu = createMainMenu();
       menuX = 50;
       menuY = app.getHeight() / 2 - menu.getLayoutHeight() / 2;

       // just a placeholder
       MenuBox menuContent = new MenuBox((int)app.getWidth() - 300 - 50);
       menuContent.setTranslateX(300);
       menuContent.setTranslateY(menu.getTranslateY());
       menuContent.setVisible(false);

       Rectangle bg = new Rectangle(app.getWidth(), app.getHeight());
       bg.setFill(Color.rgb(10, 1, 1));

       Title title = new Title(app.getTitle());
       title.setTranslateX(app.getWidth() / 2 - title.getLayoutWidth() / 2);
       title.setTranslateY(menu.getTranslateY() / 2 - title.getLayoutHeight() / 2);

       Text version = new Text("v" + app.getVersion());
       version.setTranslateY(app.getHeight() - 2);
       version.setFill(Color.WHITE);
       version.setFont(Font.font(18));

       root.getChildren().addAll(bg, title, version, menu, menuContent);
   }

   private MenuBox createMainMenu() {
       MenuItem itemContinue = new MenuItem("Continue");
       itemContinue.setEnabled(SaveLoadManager.INSTANCE.loadFileNames().isPresent() && 
    		   SaveLoadManager.INSTANCE.loadFileNames().get().contains(consApp.sSettings.getLastSave()));
       itemContinue.setAction(() -> {
    	   try {
    		   GameSave data = SaveLoadManager.INSTANCE.load(consApp.sSettings.getLastSave());
    		   Rectangle bg = new Rectangle(app.getWidth(), app.getHeight());
    		   bg.setFill(Color.rgb(10, 1, 1));
    		   bg.setOpacity(1);
    		   FadeTransition ft = new FadeTransition(Duration.seconds(1), bg);
    		   ft.setFromValue(1);
    		   ft.setToValue(1);
    		   ft.setOnFinished(evt ->{app.getSceneManager().removeUINode(bg);});   		   
    		   app.startNewGame();
    		   consApp.loadState(data);
    		   ft.play();
    		   app.getSceneManager().addUINodes(bg);
    		   consApp.soundManager.getBackgroundMusic().loop();
    		   
    	   } catch (Exception e) {
    		   e.printStackTrace();
    	   }
       });

       MenuItem itemNewGame = new MenuItem("New Game");
       itemNewGame.setAction(app::startNewGame);

       MenuItem itemLoad = new MenuItem("LOAD");
       itemLoad.setMenuContent(createContentLoad());

       MenuItem itemOptions = new MenuItem("OPTIONS");
       itemOptions.setChild(createOptionsMenu());

       MenuItem itemExtra = new MenuItem("EXTRA");
       itemExtra.setChild(createExtraMenu());

       MenuItem itemExit = new MenuItem("EXIT");
       itemExit.setAction(app::exit);

       MenuBox menu = new MenuBox(200, itemContinue, itemNewGame, itemLoad, itemOptions, itemExtra, itemExit);
       menu.setTranslateX(50);
       menu.setTranslateY(app.getHeight() / 2 - menu.getLayoutHeight() / 2);
       return menu;
   }

   private MenuContent createContentLoad() {
       ListView<String> list = new ListView<>();
       SaveLoadManager.INSTANCE.loadFileNames().ifPresent(names -> list.getItems().setAll(names));
       list.prefHeightProperty().bind(Bindings.size(list.getItems()).multiply(36));

       try {
           String css = AssetManager.INSTANCE.loadCSS("listview.css");
           list.getStylesheets().add(css);
       }
       catch (Exception e) {}

       if (list.getItems().size() > 0) {
           list.getSelectionModel().selectFirst();
       }

       MenuItem btnLoad = new MenuItem("LOAD");
       btnLoad.setAction(() -> {
           String fileName = list.getSelectionModel().getSelectedItem();
           if (fileName == null)
               return;

           try {
               Serializable data = SaveLoadManager.INSTANCE.load(fileName);
               app.loadState(data);
               consApp.sSettings.setLastSave(fileName);
           }
           catch (Exception e) {
               // TODO: use custom stages, as alerts will kick users from the fullscreen
               Alert alert = new Alert(AlertType.ERROR);
               alert.setContentText("Failed to load file: " + fileName + ". Error: " + e.getMessage());
               alert.showAndWait();
           }
       });
       MenuItem btnDelete = new MenuItem("DELETE");
       btnDelete.setAction(() -> {
           String fileName = list.getSelectionModel().getSelectedItem();
           if (fileName == null)
               return;

           Alert alert = new Alert(AlertType.INFORMATION);
           alert.setContentText(SaveLoadManager.INSTANCE.delete(fileName) ? "File was deleted" : "File couldn't be deleted");
           alert.showAndWait();

           list.getItems().remove(fileName);
       });

       HBox hbox = new HBox(50, btnLoad, btnDelete);
       hbox.setAlignment(Pos.CENTER);

       return new MenuContent(list, hbox);
   }

   private MenuBox createOptionsMenu() {
       MenuItem itemControls = new MenuItem("CONTROLS");
       itemControls.setMenuContent(createContentControls());

       MenuItem itemVideo = new MenuItem("VIDEO");
       MenuItem itemAudio = new MenuItem("AUDIO");

       return new MenuBox(200, itemControls, itemVideo, itemAudio);
   }

   private MenuBox createExtraMenu() {
       MenuItem itemCredits = new MenuItem("CREDITS");
       itemCredits.setMenuContent(createContentCredits());

       return new MenuBox(200, itemCredits);
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

   private MenuContent createContentControls() {
       Font font = Font.font(18);

       GridPane grid = new GridPane();
       grid.setAlignment(Pos.CENTER);
       grid.setHgap(50);

       int i = 0;
       /*for (InputBinding binding : app.getInputManager().getBindings()) {
           Text actionName = new Text(binding.getAction().getName());
           actionName.setFont(font);
           actionName.setFill(Color.WHITE);

           MenuItem triggerName = new MenuItem("");
           triggerName.text.textProperty().bind(binding.triggerNameProperty());
           triggerName.setOnMouseClicked(event -> {
               Rectangle rect = new Rectangle(250, 100);
               rect.setStroke(Color.AZURE);

               Text text = new Text("PRESS ANY KEY");
               text.setFill(Color.WHITE);
               text.setFont(Font.font(24));

               Stage stage = new Stage(StageStyle.TRANSPARENT);
               stage.initModality(Modality.WINDOW_MODAL);
               stage.initOwner(root.getScene().getWindow());

               Scene scene = new Scene(new StackPane(rect, text));
               scene.setOnKeyPressed(e -> {
                   app.getInputManager().rebind(binding.getAction(), e.getCode());
                   stage.close();
               });
               scene.setOnMouseClicked(e -> {
                   app.getInputManager().rebind(binding.getAction(), e.getButton());
                   stage.close();
               });

               stage.setScene(scene);
               stage.show();
           });

           grid.addRow(i++, actionName, triggerName);

           GridPane.setHalignment(actionName, HPos.RIGHT);
           GridPane.setHalignment(triggerName, HPos.LEFT);
       }*/

       ScrollPane scroll = new ScrollPane(grid);
       scroll.setVbarPolicy(ScrollBarPolicy.ALWAYS);
       scroll.setMaxHeight(app.getHeight() / 2);
       scroll.setStyle("-fx-background: black;");

       HBox hbox = new HBox(scroll);
       hbox.setAlignment(Pos.CENTER);

       return new MenuContent(hbox);
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

       public MenuItem(String name) {
           LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, new Stop[] {
                   new Stop(0.5, Color.hsb(33, 0.7, 0.7)),
                   new Stop(1, Color.hsb(100, 0.8, 1))
           });

           Rectangle bg = new Rectangle(200, 30);
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

           MenuItem back = new MenuItem("BACK");
           menu.getChildren().add(0, back);

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
           getChildren().add(createSeparator((int)app.getWidth() - 300 - 50));

           for (Node item : items) {
               getChildren().addAll(item, createSeparator((int)app.getWidth() - 300 - 50));
           }
       }

       private Line createSeparator(int width) {
           Line sep = new Line();
           sep.setEndX(width);
           sep.setStroke(Color.DARKGREY);
           return sep;
       }
   }
}