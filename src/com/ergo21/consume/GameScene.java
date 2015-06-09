package com.ergo21.consume;

import java.util.ArrayList;
import java.util.List;

import com.almasb.fxgl.asset.Assets;

import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

public class GameScene extends Group{

	private ArrayList<SceneLine> script;
	private int currentLine;
	private Text name;
	private Text line;
	private ImageView icon;

	public GameScene(List<String> values, Assets ass){
		super();
		script = new ArrayList<SceneLine>();
		currentLine = 0;
		GridPane grid = new GridPane();
		super.getChildren().add(grid);
		name = new Text();
		line = new Text();
		icon = new ImageView();
		grid.setVgap(2);
		grid.setHgap(2);
		grid.setPadding(new Insets(5,5,5,5));
		grid.add(name, 0, 0);
		grid.add(line, 1, 0);
		grid.add(icon, 0, 1);

		for(String val : values){
			if(val.equals("END")){
				break;
			}
			String nam = val.substring(0, val.indexOf('('));
			String tVal = val.substring(val.indexOf('"') + 1);
			String icoNam = tVal.substring(0, tVal.indexOf('"'));
			tVal = tVal.substring(tVal.indexOf('=') + 1);
			String lin = tVal.trim();

			System.out.println(icoNam);

			script.add(new SceneLine(nam, ass.getTexture(icoNam), lin));
		}

		setValues(script.get(currentLine));
	}

	public boolean updateScript(){
		if(currentLine + 1 >= script.size()){
			return false;
		}
		else{
			currentLine += 1;
			setValues(script.get(currentLine));
		}

		return true;
	}

	private void setValues(SceneLine sceneLine) {
		name.setText(sceneLine.getName());
		line.setText(sceneLine.getSentence());
		//icon = sceneLine.getIcon();
		icon.setImage(sceneLine.getIcon().getImage());
	}
}