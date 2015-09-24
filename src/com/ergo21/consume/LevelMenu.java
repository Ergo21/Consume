package com.ergo21.consume;

import com.almasb.consume.ConsumeApp;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class LevelMenu extends Group{
	private ConsumeApp consApp;
	private MenuItem finalLevel;
	public LevelMenu(ConsumeApp a){
		super();
		consApp = a;
		
		Rectangle bg = new Rectangle(consApp.getWidth(), consApp.getHeight());
		bg.setFill(Color.OLIVE);
		this.getChildren().add(bg);
		
		GridPane gp = new GridPane();
		
		for(int i = 0; i < 7; i++){
			MenuItem but = new MenuItem(String.valueOf(i));
			int lev = (i+1)*3;
			but.setAction(() -> {
				consApp.playerData.setCurrentLevel(lev);
				consApp.soundManager.setBackgroundMusic(consApp.getBackgroundMusic());
				consApp.soundManager.getBackgroundMusic().setCycleCount(Integer.MAX_VALUE);
				consApp.changeLevel();
				consApp.soundManager.playBackgroundMusic();
			}); 
			
			switch(i){
				case 0:{
					gp.add(but, 1, 0);
					break;
				}
				case 1:{
					gp.add(but, 2, 0);
					break;
				}
				case 2:{
					gp.add(but, 0, 1);
					break;
				}
				case 3:{
					gp.add(but, 1, 1);
					finalLevel = but;
					but.setVisible(false);
					break;
				}
				case 4:{
					gp.add(but, 2, 1);
					break;
				}
				case 5:{
					gp.add(but, 0, 2);
					break;
				}
				case 6:{
					gp.add(but, 1, 2);
					break;
				}
				
			}
		}
		gp.setHgap(40);
		gp.setVgap(20);
		
		gp.setLayoutX((consApp.getWidth() - 360)/2);
		gp.setLayoutY((consApp.getHeight() - 300)/2);
		
		this.getChildren().add(gp);
	}
	
	public void setFinalLevelVisible(boolean v){
		finalLevel.setVisible(v);
	}
	
	private class MenuItem extends StackPane {
		private Text text;

		private Background defBack;
		private Color defTexFill;

		public MenuItem(String name) {
			LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
					new Stop[] { new Stop(0.5, Color.hsb(33, 0.7, 0.7)), new Stop(1, Color.hsb(100, 0.8, 1)) });

			defBack = new Background(new BackgroundFill(new Color(0,0,0,0.4), new CornerRadii(7), new Insets(1)));
			defTexFill = Color.WHITE;
			this.setBackground(defBack);
			this.setBorder(new Border(
					new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, new CornerRadii(5), BorderWidths.DEFAULT)));

			Rectangle bg = new Rectangle(80, 80);
			bg.setVisible(false);
			bg.setOpacity(0.4);

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
	}
}