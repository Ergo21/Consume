package com.ergo21.consume;

import java.util.ArrayList;
import java.util.List;

import com.almasb.consume.ConsumeApp;
import com.almasb.consume.Types.Type;
import com.almasb.consume.ai.PhysicsControl;
import com.almasb.fxgl.asset.Assets;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
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
		line.setWrappingWidth(300);
		icon = new ImageView();
		grid.setVgap(2);
		grid.setHgap(2);
		grid.setPadding(new Insets(5, 5, 5, 5));
		grid.add(name, 0, 0);
		grid.add(line, 1, 0, 1, 2);
		grid.add(icon, 0, 1);
		grid.setGridLinesVisible(true);
		ColumnConstraints c = new ColumnConstraints();
		c.setMinWidth(50);
		c.setHalignment(HPos.CENTER);
		grid.getColumnConstraints().add(c);

		for (String val : values) {
			if (val.equals("END")) {
				break;
			}
			String nam = val.substring(0, val.indexOf('('));
			String tVal = val.substring(val.indexOf('"') + 1);
			String icoNam = tVal.substring(0, tVal.indexOf('"'));
			tVal = tVal.substring(tVal.indexOf('=') + 1);
			String lin = tVal.trim();

			script.add(new SceneLine(nam, assets.getTexture(icoNam), lin));
		}

		setValues(script.get(currentLine));
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
			String tVal = val.substring(val.indexOf('"') + 1);
			String icoNam = tVal.substring(0, tVal.indexOf('"'));
			tVal = tVal.substring(tVal.indexOf('=') + 1);
			String lin = tVal.trim();

			script.add(new SceneLine(nam, assets.getTexture(icoNam), lin));
		}

		setValues(script.get(currentLine));
	}

	public void playScene() {
		this.setVisible(true);
		app.player.setProperty("stunned", true);
	}

	public void endScene() {
		this.setVisible(false);
		app.player.setProperty("stunned", false);
	}

	private void setValues(SceneLine sceneLine) {
		name.setText(sceneLine.getName());
		line.setText(sceneLine.getSentence());
		// icon = sceneLine.getIcon();
		icon.setImage(sceneLine.getIcon().getImage());
	}

	public Assets getAssets() {
		return assets;
	}
}