package com.ergo21.consume;

import java.util.ArrayList;
import java.util.List;

import com.almasb.consume.Config;
import com.almasb.consume.ConsumeApp;
import com.almasb.consume.Types;
import com.almasb.consume.Config.Speed;
import com.almasb.consume.Types.Type;
import com.almasb.consume.ai.CameraControl;
import com.almasb.consume.ai.PhysicsControl;
import com.almasb.fxgl.asset.Texture;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.event.MenuEvent;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Text;
import javafx.util.Pair;

public class GameScene extends Group {

	private ArrayList<SceneLine> script;
	private int currentLine;
	private Text name;
	private Text line;
	private ImageView icon;
	private ConsumeApp app;

	public GameScene(List<String> values, ConsumeApp a) {
		super();
		app = a;
		script = new ArrayList<SceneLine>();
		currentLine = 0;
		GridPane grid = new GridPane();
		super.getChildren().add(grid);
		name = new Text();
		line = new Text();
		line.setWrappingWidth(275);
		icon = new ImageView();
		icon.setPreserveRatio(true);
		icon.setFitWidth(60);
		
		LinearGradient gradient = new LinearGradient(0, 1, 0, 0, true, CycleMethod.NO_CYCLE, new Stop[] { new Stop(0.25, Color.DARKRED), new Stop(1, Color.RED) });	
		grid.setBackground(new Background(new BackgroundFill(gradient, new CornerRadii(5), new Insets(0))));
		grid.setBorder(new Border(
				new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(4), BorderWidths.DEFAULT)));
		
		Region rNam = new Region(); 
		rNam.setMinSize(64, 20);
		rNam.setMaxSize(64, 20);
		rNam.setBorder(new Border(
				new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(4,0,0,0, false), new BorderWidths(0, 2, 1, 0), new Insets(0,0,-2,0))));
	
		Region rLin = new Region();
		rLin.setMinSize(280, 85);
		rLin.setMaxSize(280, 85);
		
		Region rIco = new Region();
		rIco.setMinSize(64, 65);
		rIco.setMaxSize(64, 65);
		rIco.setBorder(new Border(
				new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(0,0,0,4, false), new BorderWidths(1, 2, 0, 0), new Insets(0,0,2,0))));
	
		icon.setTranslateX(1);
		icon.imageProperty().addListener(new ChangeListener<Image>(){
			@Override
			public void changed(ObservableValue<? extends Image> observable, Image oldValue, Image newValue) {
				double ratio = 60/newValue.getWidth();
				icon.setTranslateY((65 - newValue.getHeight()*ratio)/2);
			}
		});
		
		Group icoGro = new Group(rIco,icon);
		
		grid.add(new TextBox(rNam,name, true), 0, 0);
		grid.add(new TextBox(rLin,line, false), 1, 0, 1, 2);
		grid.add(icoGro, 0, 1);
		//grid.setGridLinesVisible(true);
		grid.setMinSize(344, 85);
		grid.setMaxSize(344, 85);
		ColumnConstraints c = new ColumnConstraints();
		c.setMinWidth(64);
		c.setHalignment(HPos.CENTER);
		grid.getColumnConstraints().add(c);

		for (String val : values) {
			if (val.equals("END")) {
				break;
			}
			String nam = val.substring(0, val.indexOf('('));
			String tVal = val.substring(val.indexOf('(') + 1);
			String icoNam = tVal.substring(0, tVal.indexOf(')'));
			tVal = tVal.substring(tVal.indexOf('"') + 1, tVal.lastIndexOf('"'));
			String lin = tVal.trim();

			if(val.indexOf("*") != -1 && val.indexOf("*", val.indexOf("*")+1) != -1) { 
				String command = val.substring(val.indexOf("*")+1, val.indexOf("*", val.indexOf("*")+1)).toLowerCase();
				script.add(new SceneLine(nam, getIconFile(icoNam), lin, getRunnable(command)));
			}
			else{
				script.add(new SceneLine(nam, getIconFile(icoNam), lin, ()->{}));
			}
		}

		setValues(script.get(currentLine));
	}

	private Texture getIconFile(String icoNam) {
		if(!Config.RELEASE){
			return app.getTexture(FileNames.EMPTY);
		}
		switch(icoNam){
			case "PLAYER":
				return app.getTexture(FileNames.PLAYER_ICON);
			case "GENTLEMAN":
				return app.getTexture(FileNames.GENTLEMAN_ICON);
			case "ANUBIS":
				return app.getTexture(FileNames.ANUBIS_ICON);
			case "ESHU":
				return app.getTexture(FileNames.ESHU_ICON);
			case "KIBO":
				return app.getTexture(FileNames.KIBO_ICON);
			case "SHAKA":
				return app.getTexture(FileNames.SHAKA_ICON);
			case "SHANGO":
				return app.getTexture(FileNames.SHANGO_ICON);
		}
		return app.getTexture(FileNames.EMPTY);
	}

	public boolean updateScript() {
		if (currentLine + 1 >= script.size()) {
			endScene();
			return false;
		} else {
			currentLine += 1;
			setValues(script.get(currentLine));
		}

		return true;
	}

	public void changeScene(List<String> values) {
		currentLine = 0;
		script.clear();
		app.getSceneManager().getEntities(Type.PLAYER).get(0).getControl(PhysicsControl.class).moveX(0);
		for (String val : values) {
			if (val.equals("END")) {
				break;
			}
			String nam = val.substring(0, val.indexOf('('));
			String tVal = val.substring(val.indexOf('(') + 1);
			String icoNam = tVal.substring(0, tVal.indexOf(')'));
			tVal = tVal.substring(tVal.indexOf('"') + 1, tVal.lastIndexOf('"'));
			String lin = tVal.trim();
			
			if(val.indexOf("*") != -1 && val.indexOf("*", val.indexOf("*")+1) != -1) { 
				String command = val.substring(val.indexOf("*")+1, val.indexOf("*", val.indexOf("*")+1)).toLowerCase();
				script.add(new SceneLine(nam, getIconFile(icoNam), lin, getRunnable(command)));
			}
			else{
				script.add(new SceneLine(nam, getIconFile(icoNam), lin, ()->{}));
			}
		}

		setValues(script.get(currentLine));
	}

	public void playScene() {
		this.setVisible(true);
		app.player.setProperty("scenePlaying", true);
	}

	public void endScene() {
		this.setVisible(false);
		app.player.setProperty("scenePlaying", false);
	}

	private void setValues(SceneLine sceneLine) {
		name.setText(sceneLine.getName());
		line.setText(sceneLine.getSentence());
		icon.setImage(sceneLine.getIcon().getImage());
		sceneLine.getRunnable().run();
	}
	
	private Runnable getRunnable(String method){
		String value = method.substring(method.indexOf('(') + 1, method.indexOf(')'));
		
		if(method.contains("spawnboss")){
			Point2D p;
			if(!app.getSceneManager().getEntities(Types.Type.BOSS_SPAWNER).isEmpty()){
				p = app.getSceneManager().getEntities(Types.Type.BOSS_SPAWNER).get(0).getPosition();
			}
			else{
				p = new Point2D(0,0);
			}
			
			switch(Integer.parseInt(value)){
			case 0: return ()-> setupBoss(app.eSpawner.spawnEshuIBoss(p), false);
			case 1: return ()-> setupBoss(app.eSpawner.spawnSandBoss(p), true);
			case 2: return ()-> setupBoss(app.eSpawner.spawnAnubisBoss(p), true);
			case 3: return ()-> setupBoss(app.eSpawner.spawnShangoBoss(p), true);
			case 4: return ()-> setupBoss(app.eSpawner.spawnKiboBoss(p), true);
			case 5: return ()-> setupBoss(app.eSpawner.spawnGentlemanBoss(p), true);
			case 6: return ()-> setupBoss(app.eSpawner.spawnEshuBoss(p), true);
			
			}
		}
		else if(method.contains("bossturn")){
			return ()-> {
				List<Entity> bosses = app.getSceneManager().getEntities(Type.BOSS);
				for(Entity boss : bosses){
					if(boss != null && boss.getControl(PhysicsControl.class) != null){
						boss.setProperty("facingRight", value.contains("right"));
						break;
					}
				}
			};
					
		}
		else if(method.contains("turn")){
			return ()-> app.player.setProperty("facingRight", value.contains("right"));
			
		}
		else if(method.contains("bossmove")){
			return ()-> {
				List<Entity> bosses = app.getSceneManager().getEntities(Type.BOSS);
				for(Entity boss : bosses){
					if(boss != null && boss.getControl(PhysicsControl.class) != null){
						boss.getControl(PhysicsControl.class).moveX
							(value.contains("right") ? Speed.PLAYER_MOVE*3/4 : -Speed.PLAYER_MOVE*3/4);
						boss.setProperty("facingRight", value.contains("right"));
						break;
					}
				}
				
			};
					
		}
		else if(method.contains("move")){
			return ()-> {
				app.player.getControl(PhysicsControl.class).moveX
					(value.contains("right") ? Speed.PLAYER_MOVE*3/4 : -Speed.PLAYER_MOVE*3/4);
				app.player.setProperty("facingRight", value.contains("right"));
					};
		}
		else if(method.contains("bossstop")){
			return ()-> {
				List<Entity> bosses = app.getSceneManager().getEntities(Type.BOSS);
				for(Entity boss : bosses){
					if(boss != null && boss.getControl(PhysicsControl.class) != null){
						boss.getControl(PhysicsControl.class).moveX(0);
						break;
					}
				}	
			};
					
		}
		else if(method.contains("stop")){
			return ()-> app.player.getControl(PhysicsControl.class).moveX(0);
		}
		else if(method.contains("level")){
			if(value.contains("next")){
				return ()->{
					app.playerData.setCurrentLevel(app.playerData.getCurrentLevel() + 1);
					app.changeLevel();
				};
			}
			else if(value.contains("screen")){
				return ()->{
					app.showLevelScreen();
					this.fireEvent(new MenuEvent(MenuEvent.RESUME));
				};
			}
		}
		
		return ()->{};
		
	}
	
	public void setupBoss(Pair<Entity,Entity> p, boolean bossBarVisible) {
		if(bossBarVisible){
			app.hud.setBossBar(p.getKey());
		}
		Entity e2 = Entity.noType();
		e2.setPosition(app.player.getPosition());
		e2.addControl(new CameraControl(app.player.getPosition().add(Config.BLOCK_SIZE*5,0)));
		e2.setVisible(false);
		app.getSceneManager().bindViewportOrigin(e2, 320, 180);
		app.getSceneManager().addEntities(p.getKey(), p.getValue(), e2);
	}

	private class TextBox extends AnchorPane{
		private Region background;
		private Text text;
		
		private TextBox(Region back, Text tex, boolean centerX){
			super();
			background = back;
			text = tex;
			
			HBox hBox = new HBox(text);
			//hBox.setStyle("-fx-border-color: red;");
			if(centerX){
				hBox.widthProperty().addListener(new ChangeListener<Number>(){
					@Override
					public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
						hBox.setTranslateX((background.getWidth() - arg2.doubleValue())/2);
					}
				});
			}
			else{
				hBox.setTranslateX(2);
			}
			hBox.heightProperty().addListener(new ChangeListener<Number>(){
				@Override
				public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
					hBox.setTranslateY((background.getHeight() - arg2.doubleValue())/2);
				}
			});
			
			this.getChildren().addAll(background, hBox);
		}
	}
}