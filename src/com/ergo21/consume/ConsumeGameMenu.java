/*
 * The MIT License (MIT)
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
import java.util.ArrayList;

import com.almasb.consume.ConsumeApp;
import com.almasb.consume.Types.Element;
import com.almasb.fxgl.asset.AssetManager;
import com.almasb.fxgl.asset.SaveLoadManager;
import com.almasb.fxgl.ui.Menu;
import com.almasb.fxgl.util.Version;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
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
public final class ConsumeGameMenu extends Menu {

    private PowerGroup powerList;
    private AnchorPane contentViewer;
    private ConsumeApp consApp;
    
    public ConsumeGameMenu(ConsumeApp app) {
        super(app);
        consApp = app;
        
        powerList = new PowerGroup();
        
        contentViewer = new AnchorPane(powerList);
        contentViewer.setTranslateX(10);
        contentViewer.setTranslateY(app.getHeight()/2);

        MenuBox menu = createMainMenu();
        //menuX = 50;
        //menuY = app.getHeight() / 2 - menu.getLayoutHeight() / 2;

        // just a placeholder
        MenuBox menuContent = new MenuBox((int)app.getWidth() - 300 - 50);
        menuContent.setTranslateX(300);
        menuContent.setTranslateY(menu.getTranslateY());
        menuContent.setVisible(false);

        Rectangle bg = new Rectangle(app.getWidth(), app.getHeight());
        bg.setFill(Color.rgb(10, 1, 1));
        //bg.setOpacity(0.5);

        Title title = new Title(app.getTitle());
        title.setTranslateX(app.getWidth() / 2 - title.getLayoutWidth() / 2);
        title.setTranslateY(menu.getTranslateY() / 2 - title.getLayoutHeight() / 2);

        Text version = new Text("v" + app.getVersion());
        version.setTranslateY(app.getHeight() - 2);
        version.setFill(Color.WHITE);
        version.setFont(Font.font(18));

        root.getChildren().addAll(bg, title, version, menu, menuContent, contentViewer);
    }

    private MenuBox createMainMenu() {
        MenuItem itemPowers = new MenuItem("Powers");
        itemPowers.setAction(() -> {
        	contentViewer.getChildren().clear();
        	contentViewer.getChildren().add(powerList);
        });

        MenuItem itemSave = new MenuItem("Save");
        itemSave.setAction(() -> {
            Serializable data = app.saveState();

            if (data == null)
                return;

            TextInputDialog dialog = new TextInputDialog();
            dialog.setContentText("Enter name for save file");
            dialog.showAndWait().ifPresent(fileName -> {
                try {
                    SaveLoadManager.INSTANCE.save(data, fileName);
                }
                catch (Exception e) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setContentText("Failed to save file: " + fileName + ". Error: " + e.getMessage());
                    alert.showAndWait();
                }
            });
        });

        MenuItem itemLoad = new MenuItem("Load");
        itemLoad.setAction(() -> {
        	contentViewer.getChildren().clear();
        	contentViewer.getChildren().add(createContentLoad());
        });

        MenuItem itemOptions = new MenuItem("Options");
        itemOptions.setAction(() -> {
        	contentViewer.getChildren().clear();
    		contentViewer.getChildren().add(createOptionsMenu());
        });

        MenuItem itemExit = new MenuItem("Main Menu");
        itemExit.setAction(app.getSceneManager()::exitToMainMenu);

        MenuBox menu = new MenuBox(5, itemPowers, itemSave, itemLoad, itemOptions, itemExit);
        
        menu.setTranslateX(0);
        menu.setTranslateY(app.getHeight() / 2 - menu.getLayoutHeight() / 2);
        return menu;
    }

    private MenuBox createContentLoad() {
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
            }
            catch (Exception e) {
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

        return new MenuBox(5, btnLoad, btnDelete);
    }

    private MenuBox createOptionsMenu() {
        MenuItem itemControls = new MenuItem("CONTROLS");
        MenuItem itemVideo = new MenuItem("VIDEO");
        MenuItem itemAudio = new MenuItem("AUDIO");
        MenuItem itemCredits = new MenuItem("Credits");
        itemCredits.setAction(() -> {
        	contentViewer.getChildren().clear();
    		contentViewer.getChildren().add(createContentCredits());
        });
        return new MenuBox(5, itemControls, itemVideo, itemAudio, itemCredits);
    }

    public void updatePowerMenu(Player playerData) {
    	ArrayList<MenuItem> pItems = new ArrayList<MenuItem>();
    	
        for(Element power : playerData.getPowers()){
        	MenuItem itemPower = new MenuItem("" + power);
        	itemPower.setElement(power);
            itemPower.setAction(() -> {
            	for(MenuItem item : pItems){
            		item.setHighlighted(false);
            	}
            	itemPower.setHighlighted(true);
            	consApp.changePower(power);
            	//playerData.setCurrentPower(power);
            });
            if(playerData.getCurrentPower() == power){
            	itemPower.setHighlighted(true);
            }
            pItems.add(itemPower);
        }
        
        playerData.ElementProperty().addListener(new ChangeListener<Element>(){
			@Override
			public void changed(ObservableValue<? extends Element> cha,
					Element old, Element now) {
				for(MenuItem item : pItems){
					if(item.getElement() == old){
						item.setHighlighted(false);
					}
					
					if(item.getElement() == now){
						item.setHighlighted(true);
					}
				}
			}
        	
        });
        
        powerList.addItems(pItems);
        
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

    private class MenuItem extends StackPane {
    	private Element thiElement;
    	private boolean highlight;
    	
    	private Text text;
    	
    	private Background defBack;
    	private Color defTexFill;
        public MenuItem(String name) {
            LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, new Stop[] {
                    new Stop(0.5, Color.hsb(33, 0.7, 0.7)),
                    new Stop(1, Color.hsb(100, 0.8, 1))
            });
            
            highlight = false;
            defBack = new Background(new BackgroundFill(Color.BLACK, new CornerRadii(7), new Insets(1)));
            defTexFill = Color.WHITE;
            this.setBackground(defBack);
            this.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, new CornerRadii(5), BorderWidths.DEFAULT)));
            
            Rectangle bg = new Rectangle(120, 30);
            bg.setVisible(false);
            //this.setOpacity(0.4);

            text = new Text(name);
            text.setFill(defTexFill);
            text.setFont(Font.font("", FontWeight.SEMI_BOLD, 22));

            setAlignment(Pos.CENTER);
            getChildren().addAll(text, bg);

            setOnMouseEntered(event -> {
            	this.setBackground(new Background(new BackgroundFill(gradient, new CornerRadii(5), new Insets(1))));
                text.setFill(Color.BLACK);
            });

            setOnMouseExited(event -> {
            	this.setBackground(defBack);
                text.setFill(defTexFill);
            });

            setOnMousePressed(event -> {
            	this.setBackground(new Background(new BackgroundFill(Color.GOLD, new CornerRadii(5), new Insets(1))));
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

        public void setEnabled(boolean b) {
            this.setDisable(!b);
            this.setOpacity(b ? 1 : 0.33);
        }
        
        public void setHighlighted(boolean b){
        	highlight = b;
        	if(highlight){
        		defBack = new Background(new BackgroundFill(Color.YELLOW, new CornerRadii(5), new Insets(1)));
                defTexFill = Color.BLACK;
        	}
        	else{
        		defBack = new Background(new BackgroundFill(Color.BLACK, new CornerRadii(5), new Insets(1)));
                defTexFill = Color.WHITE;
        	}
        	this.setBackground(defBack);
            text.setFill(defTexFill);
        }
        
        public boolean getHighlighted(){
        	return highlight;
        }
        
        public Element getElement(){
        	return thiElement;
        }
        
        public void setElement(Element e){
        	thiElement = e;
        }
    }
    
    private class PowerGroup extends GridPane{
    	public PowerGroup(){
    		super();
    		this.setVgap(3);
    		this.setHgap(3);
    	}
    	
    	public void addItems(ArrayList<MenuItem> items){
    		this.getChildren().clear();
    		int val = 0;
    		for(MenuItem item : items){
    			this.add(item, val%3, val/3);
    			val++;
    		}
    	}
    }
}