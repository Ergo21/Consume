package com.ergo21.consume;

import java.util.ArrayList;
import java.util.List;

import com.almasb.consume.Config;
import com.almasb.consume.ConsumeApp;
import com.almasb.consume.Types.Type;
import com.almasb.consume.ai.PhysicsControl;
import com.almasb.fxgl.asset.Assets;
import com.almasb.fxgl.asset.Texture;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
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

public class GameScene extends Group {

	private ArrayList<SceneLine> script;
	private int currentLine;
	private Text name;
	private Text line;
	private ImageView icon;
	private Assets assets;
	private ConsumeApp app;

	public GameScene(List<String> values, Assets as, ConsumeApp a) {
		super();
		assets = as;
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

			script.add(new SceneLine(nam, getIconFile(icoNam), lin));
		}

		setValues(script.get(currentLine));
	}

	private Texture getIconFile(String icoNam) {
		if(!Config.RELEASE){
			return assets.getTexture(FileNames.EMPTY);
		}
		switch(icoNam){
			case "PLAYER":
				return assets.getTexture(FileNames.PLAYER_ICON);
			case "GENTLEMAN":
				return assets.getTexture(FileNames.GENTLEMAN_ICON);
			case "ANUBIS":
				return assets.getTexture(FileNames.ANUBIS_ICON);
			case "ESHU":
				return assets.getTexture(FileNames.ESHU_ICON);
			case "KIBO":
				return assets.getTexture(FileNames.KIBO_ICON);
			case "SHAKA":
				return assets.getTexture(FileNames.SHAKA_ICON);
			case "SHANGO":
				return assets.getTexture(FileNames.SHANGO_ICON);
		}
		return assets.getTexture(FileNames.EMPTY);
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

			script.add(new SceneLine(nam, getIconFile(icoNam), lin));
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
	}

	public Assets getAssets() {
		return assets;
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