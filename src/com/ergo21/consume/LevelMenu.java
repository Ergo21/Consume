package com.ergo21.consume;

import java.util.ArrayList;

import com.almasb.consume.Config;
import com.almasb.consume.ConsumeApp;
import com.almasb.fxgl.asset.Texture;

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
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

public class LevelMenu extends Group{
	private ConsumeApp consApp;
	private MenuItem finalLevel;
	private ArrayList<MenuItem> mIList;
	
	public LevelMenu(ConsumeApp a){
		super();
		consApp = a;
		
		mIList = new ArrayList<MenuItem>();
		
		Rectangle bg = new Rectangle(consApp.getWidth(), consApp.getHeight());
		bg.setFill(Color.OLIVE);
		this.getChildren().add(bg);
		if(Config.RELEASE){
			this.getChildren().add(consApp.getAssetManager().loadTexture(FileNames.AFRICA));
		}
		
		GridPane gp = new GridPane();
		
		for(int i = 0; i < 7; i++){
			MenuItem but = new MenuItem(getButtonIcon(i));
			int lev = (i+1)*3;
			but.setAction(() -> {
				consApp.soundManager.stopAll();
				consApp.playerData.setCurrentLevel(lev);
				consApp.playerData.setCurrentHealth(consApp.playerData.getMaxHealth());
				consApp.playerData.setCurrentMana(consApp.playerData.getMaxMana());
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
					gp.add(but, 1, 2);
					break;
				}
				case 6:{
					gp.add(but, 2, 2);
					break;
				}
				
			}
			
			mIList.add(but);
		}
		gp.setHgap(40);
		gp.setVgap(20);
		
		gp.setLayoutX((consApp.getWidth() - 360)/2);
		gp.setLayoutY((consApp.getHeight() - 300)/2);
		
		this.getChildren().add(gp);
	}
	
	private Texture getButtonIcon(int lev) {
		Texture t = null;
		switch(lev){
			case 0:{
				//t = consApp.getAssetManager().loadTexture(FileNames.DESERT_BACK_1);
				t = consApp.getAssetManager().loadTexture(FileNames.ELEPHANT_ICON);
				t.setPreserveRatio(false);
				t.setFitWidth(76);
				t.setFitHeight(72);
				break;
			}
			case 1:{
				//t = consApp.getAssetManager().loadTexture(FileNames.PYRAMID_BACK_1);
				t = consApp.getAssetManager().loadTexture(FileNames.ANUBIS_ICON);
				t.setPreserveRatio(true);
				t.setFitWidth(80);
				t.setFitHeight(80);
				break;
			}
			case 2:{
				//t = consApp.getAssetManager().loadTexture(FileNames.FESTIVAL_BACK_1);
				t = consApp.getAssetManager().loadTexture(FileNames.SHANGO_ICON);
				t.setPreserveRatio(true);
				t.setFitWidth(80);
				t.setFitHeight(80);
				break;
			}
			case 3:{
				//t = consApp.getAssetManager().loadTexture(FileNames.FOREST1_BACK_2);
				t = consApp.getAssetManager().loadTexture(FileNames.ESHU_ICON);
				t.setPreserveRatio(true);
				t.setFitWidth(80);
				t.setFitHeight(80);
				break;
			}
			case 4:{
				//t = consApp.getAssetManager().loadTexture(FileNames.MOUNTAIN_BACK_1);
				t = consApp.getAssetManager().loadTexture(FileNames.KIBO_ICON);
				t.setPreserveRatio(true);
				t.setFitWidth(80);
				t.setFitHeight(80);
				break;
			}
			case 5:{
				//t = consApp.getAssetManager().loadTexture(FileNames.COLONY_BACK_1);
				t = consApp.getAssetManager().loadTexture(FileNames.GENTLEMAN_ICON);
				t.setPreserveRatio(true);
				t.setFitWidth(80);
				t.setFitHeight(80);
				break;
			}
			case 6:{
				//t = consApp.getAssetManager().loadTexture(FileNames.EMPIRE_BACK_1);
				t = consApp.getAssetManager().loadTexture(FileNames.SHAKA_ICON);
				t.setPreserveRatio(true);
				t.setFitWidth(80);
				t.setFitHeight(80);
				break;
			}
			default:{
				t = consApp.getAssetManager().loadTexture(FileNames.DESERT_BACK_1);
				t.setPreserveRatio(true);
				t.setFitWidth(80);
				t.setFitHeight(80);
				break;
			}
		}
		return t;
	}

	public void setFinalLevelVisible(boolean v){
		finalLevel.setVisible(v);
	}
	
	public void setLevelComplete(int l){
		if(l > 0 || l <= mIList.size()){
			mIList.get(l-1).setComplete();
		}
	}
	
	public ArrayList<Integer> getLevelsComplete(){
		ArrayList<Integer> com = new ArrayList<>();
		
		for(int i = 0; i < mIList.size(); i++){
			if(mIList.get(i).isComplete()){
				com.add(i+1);
			}
		}
		
		return com;
	}
	
	private class MenuItem extends StackPane {

		private Background defBack;
		private Texture icon;
		private Background hovBack;
		private Background preBack;
		private boolean complete;

		public MenuItem(Texture ico) {
			icon = ico;
			complete = false;
			LinearGradient gradient = new LinearGradient(0, 1, 0, 0, true, CycleMethod.NO_CYCLE,
					new Stop[] { new Stop(0.5, Color.DARKRED), new Stop(1, Color.RED) });

			defBack = new Background(new BackgroundFill(new Color(0,0,0,0.4), new CornerRadii(7), new Insets(1)));
			hovBack = new Background(new BackgroundFill(gradient, new CornerRadii(5), new Insets(1)));
			preBack = new Background(new BackgroundFill(Color.GOLD, new CornerRadii(5), new Insets(1)));
			this.setBackground(defBack);
			this.setBorder(new Border(
					new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, new CornerRadii(5), BorderWidths.DEFAULT)));

			Rectangle bg = new Rectangle(80, 80);
			bg.setVisible(false);
			bg.setOpacity(0.4);

			setAlignment(Pos.CENTER);
			getChildren().addAll(icon, bg);

			setOnMouseEntered(event -> {
				this.setBackground(hovBack);
			});

			setOnMouseExited(event -> {
				this.setBackground(defBack);
			});

			setOnMousePressed(event -> {
				this.setBackground(preBack);
			});

			setOnMouseReleased(event -> {
				this.setBackground(hovBack);
			});
		}

		public void setAction(Runnable action) {
			this.setOnMouseClicked(event -> {
				action.run();
			});
		}
		
		public void setComplete(){
			complete = true;
			double rLen = this.getWidth() - 5; 
			Polygon s1 = new Polygon(0, (rLen-5)/2,
									(rLen-5)/2, (rLen-5)/2,
									(rLen-5)/2, 0,
									(rLen+5)/2, 0,
									(rLen+5)/2, (rLen-5)/2,
									rLen, (rLen-5)/2,
									rLen, (rLen+5)/2,
									(rLen+5)/2, (rLen+5)/2,
									(rLen+5)/2, rLen,
									(rLen-5)/2, rLen,
									(rLen-5)/2, (rLen+5)/2,
									0, (rLen+5)/2);
			s1.setFill(Color.RED);
			s1.setRotate(45);
			s1.setStroke(Color.BLACK);
			s1.setStrokeWidth(3);
			getChildren().addAll(s1);
		}
		
		public boolean isComplete(){
			return complete;
		}
	}
}