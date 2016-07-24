package com.almasb.consume;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;

import com.almasb.consume.Types.Block;
import com.almasb.consume.Types.Element;
import com.almasb.consume.Types.Platform;
import com.almasb.consume.Types.Powerup;
import com.almasb.consume.Types.Property;
import com.almasb.consume.Types.Type;
import com.almasb.consume.ai.CollideSpawnerControl;
import com.almasb.consume.ai.ESpawnerControl;
import com.almasb.fxgl.asset.Texture;
import com.almasb.fxgl.entity.Entity;
import com.ergo21.consume.FileNames;

public class LevelParser {

	private ConsumeApp consApp;
	private List<LevelData> levels;

	public LevelParser(ConsumeApp cA, List<LevelData> levels) {
		consApp = cA;
		this.levels = levels;
	}
	
	public List<Level> parseAll() {
		return IntStream.range(0, levels.size()).mapToObj(this::parse).collect(Collectors.toList());
	}

	public Level parse(int levelNumber) {
		if(levels.get(levelNumber) == null){
			return null;
		}
		List<String> data = levels.get(levelNumber).data;
		Point2D backY = new Point2D(0,0);

		// TODO: range checks
		
		Level level = new Level();
		
		level.width = data.get(0).length() * Config.BLOCK_SIZE;
		level.height = data.size() * Config.BLOCK_SIZE;

		for (int i = 0; i < data.size(); i++) {
			String line = data.get(i);
			for (int j = 0; j < line.length(); j++) {
				Rectangle rect = new Rectangle(Config.BLOCK_SIZE, Config.BLOCK_SIZE);
				Entity e = null;
				Texture t = null;

				switch (line.charAt(j)) {
				case ' ':
					break;
				case '0':
					break;
				case '1':
					level.spawnPoint = new Point2D(j * Config.BLOCK_SIZE, i * Config.BLOCK_SIZE);
					break;
				case '2':
					if(level.nextLevelEntity == null){
						e = new Entity(Types.Type.NEXT_LEVEL_POINT);
						e.setCollidable(true);
						e.setProperty("autoDoor", true);
						level.nextLevelEntity = e;
						rect.setFill(Color.BLACK);
					}
					break;
				case '3':
					e = new Entity(Types.Type.LIMIT);
					e.setCollidable(true);
					e.setVisible(false);
					rect.setFill(Color.CYAN);
					
					if(level.uLLim == null){
						level.uLLim = new Point2D(j * Config.BLOCK_SIZE, i * Config.BLOCK_SIZE);
					}
					else if(level.lRLim == null){
						level.lRLim = new Point2D(j * Config.BLOCK_SIZE, i * Config.BLOCK_SIZE);
					}
					break;
				case '4':
					backY = backY.add(0, i*Config.BLOCK_SIZE);
					break;
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					e = new Entity(Types.Type.BLOCK);
					e.setProperty(Property.SUB_TYPE, Block.SCENE);
					e.setProperty("played", false);
					e.setCollidable(true);
					e.setVisible(false);
					e.setPosition(0, -Config.BLOCK_SIZE*2);
					rect.setHeight(Config.BLOCK_SIZE*3);
					String chTe = "" + line.charAt(j);
					int scNo = Integer.parseInt(chTe);
					scNo -= 4;
					String scTe = "dialogue/scene_" + levelNumber + "_" + scNo + ".txt";
					e.setProperty("sceneName", scTe);
					rect.setFill(Color.YELLOW);
					
					int tLNo = levelNumber;
					
					if(tLNo%3 == 0){
						tLNo += 2;
					}
					else if(tLNo%3 == 1){
						tLNo += 1;
					}
					
					if(consApp.playerData.getLevsComp().contains(tLNo)){
						e.setCollidable(false);
					}
					break;
				case 'a':
				case 'A':
				case 'b':
				case 'g':
				case 'G':
				case 'h':
				case 'H':
					e = new Entity(Types.Type.BLOCK);
					e.setProperty(Property.SUB_TYPE, Block.BARRIER);
					e.setProperty("state", "idle");
					e.setProperty("start", "none");
					e.setCollidable(false);
					e.setProperty("blockImg", line.charAt(j)); //See activateBarrier() in ConsumeApp for pic choice
					e.setVisible(false);
					if(i == 0 || data.get(i-1).charAt(j) != line.charAt(j)){ 
						int hei = 1;
						for(hei = 1; hei + i < data.size(); hei++){
							if(data.get(i+hei).charAt(j) != line.charAt(j)){
								break;
							}
						}
						Entity e2 = new Entity(Types.Type.BLOCK);
						e2.setProperty(Property.SUB_TYPE, Block.BARRIER);
						e2.setProperty("state", "idle");
						e2.setProperty("start", "none");
						e2.setProperty("blockImg", line.charAt(j));
						e2.setCollidable(true);
						e2.setVisible(false);
						e2.setPosition(j*Config.BLOCK_SIZE, i*Config.BLOCK_SIZE);
						Rectangle rect2 = new Rectangle(Config.BLOCK_SIZE, Config.BLOCK_SIZE*hei);
						rect2.setFill(Color.BLACK);
						e2.setGraphics(rect2);
						level.entities.add(e2);
					}
					break;
				case 'B':{
					e = new Entity(Type.BOSS_SPAWNER);
					rect.setFill(Color.RED);
					e.setVisible(false);
					e.setCollidable(false);
					break;
				}
				case 'c':
				case 'C':{
					if(line.charAt(j) == 'C' && consApp.levelMenu.getLevelsComplete().contains(levelNumber/3)){
						break;
					}
					
					Entity en = new Entity(Type.BLOCK);
					en.setProperty(Property.SUB_TYPE, Type.ENEMY_SPAWNER);
					Rectangle rect2 = new Rectangle(Config.BLOCK_SIZE/2, Config.BLOCK_SIZE*3);
					rect2.setFill(Color.RED);
					en.setGraphics(rect2);
					en.setVisible(false);
					en.setCollidable(true);
					if(line.charAt(j) == 'c'){
						en.setPosition(en.getPosition().add(j * Config.BLOCK_SIZE, ((i-1) * Config.BLOCK_SIZE)));
						en.addControl(new CollideSpawnerControl(
								(Function<Point2D, Pair<Entity, Entity>>) (tS) -> consApp.eSpawner.spawnMummy(tS), 
								2, en.getPosition().add(-Config.BLOCK_SIZE*3, Config.BLOCK_SIZE*2), 
								en.getPosition().add(Config.BLOCK_SIZE*3, Config.BLOCK_SIZE*2)));
						en.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
							if(en != null && en.getControl(CollideSpawnerControl.class) != null){
								ArrayList<Pair<Entity,Entity>> ens = en.getControl(CollideSpawnerControl.class).spawnEnemies();
								for(Pair<Entity,Entity> enV : ens){
									consApp.getSceneManager().addEntities(enV.getKey(), enV.getValue());
								}
								
							}
						});
					}
					else{
						en.setPosition(en.getPosition().add(j * Config.BLOCK_SIZE, ((i-1) * Config.BLOCK_SIZE)));
						en.addControl(new CollideSpawnerControl(
								(Function<Point2D, Pair<Entity,Entity>>) (tS) -> consApp.eSpawner.spawnSandBoss(tS), 
								1, en.getPosition().add(Config.BLOCK_SIZE*11.5, Config.BLOCK_SIZE*2)));
						en.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
							if(en != null && en.getControl(CollideSpawnerControl.class) != null){
								ArrayList<Pair<Entity,Entity>> ens = en.getControl(CollideSpawnerControl.class).spawnEnemies();
								consApp.gScene.setupBoss(ens.get(0), true);
							}
						});
					}
					
					level.entities.add(en);
					
					break;
				}
				case 'd':
				case 'D':
				case 'e':
				case 'E':
				case 'f':
					e = new Entity(Types.Type.PLATFORM);
					e.setProperty(Property.SUB_TYPE, Platform.DESTRUCTIBLE);
					e.setProperty("state", "none");
					rect.setFill(Color.DARKGREEN);
					switch(line.charAt(j)){
						case 'd': e.setProperty("desElement", Element.EARTH); break;
						case 'D': e.setProperty("desElement", Element.FIRE); break;
						case 'e': e.setProperty("desElement", Element.METAL); break;
						case 'E': e.setProperty("desElement", Element.NEUTRAL2); break;
						case 'f': e.setProperty("desElement", Element.LIGHTNING); break;
					}
					
					if(Config.RELEASE){
						switch(line.charAt(j)){
							case 'd': t = consApp.getTexture(FileNames.DES_THATCH_BLOCK); break;
							case 'D': t = consApp.getTexture(FileNames.DES_CRATE_BLOCK); break;
							case 'e': t = consApp.getTexture(FileNames.DES_SANDSTONE_BLOCK); break;
							case 'E': t = consApp.getTexture(FileNames.DES_LIMESTONE_BLOCK); break;
							case 'f': t = consApp.getTexture(FileNames.DES_ICE_BLOCK); break;
						}
						t.setPreserveRatio(true);
						t.setFitHeight(40);
						
						
					}
					break;
				case 'i':
				case 'I':
				case 'j':
				case 'k':
				case 'K':
				case 'l':
				case 'L':
				case 'm':
				case 'M':
				case 'n':
					if(i >= 1 && j + 1 < line.length() && isSameZone(line.charAt(j), line.charAt(j+1)) && 
								isPlatform(data.get(i-1).charAt(j))){
						int len = 1;
						for(int b = 1; b + j < line.length(); b++){
							if(len < 40 && i >= 1 && data.get(i-1) != null && 
									isSameZone(line.charAt(j), line.charAt(j + b)) &&
									isPlatform(data.get(i-1).charAt(j + b))){
								len++;
							}
							else{
								break;
							}
						}
						
						Entity en = new Entity(Types.Type.PLATFORM);
						en.setProperty(Property.SUB_TYPE, Platform.INDESTRUCTIBLE);
						Texture tex = consApp.getTexture(getZoneLine(line.charAt(j)));
						tex = tex.subTexture(new Rectangle2D(0,0,200*len, 200));
						tex.setPreserveRatio(true);
						tex.setFitHeight(Config.BLOCK_SIZE);
						en.setGraphics(tex);
						en.setPosition(en.getPosition().add(j * Config.BLOCK_SIZE, i * Config.BLOCK_SIZE));
						j = j + len - 1;
						level.entities.add(en);
					}
					else{
						e = new Entity(Types.Type.PLATFORM);
						e.setProperty(Property.SUB_TYPE, Platform.INDESTRUCTIBLE);
						rect.setFill(Color.BROWN);
						if(Config.RELEASE){
							if(i >= 1 && data.get(i-1) != null && 
									!(isPlatform(data.get(i-1).charAt(j)))){
								t = consApp.getTexture(getSurfaceTexture(line.charAt(j)));
							}
							else{
								t = consApp.getTexture(getUnderTexture(line.charAt(j)));
							}
						
							if(j >= 1 && line.charAt(j-1) == line.charAt(j) && level.entities.get(level.entities.size() - 1).getScaleX() == 1){
								e.setScaleX(-1);
							}
							t.setPreserveRatio(true);
							t.setFitHeight(40);
						}
					}
					break;
				case 'p':
					if((i >= 1 && data.get(i-1) != null && 
								data.get(i-1).charAt(j) != 'p')){
						e = new Entity(Types.Type.BLOCK);
						e.setProperty(Property.SUB_TYPE, Block.LADDER);
						e.setProperty("top", false);
						e.setCollidable(true);
						e.setPosition(0, -1);
						rect.setFill(Color.GREY);
						if(Config.RELEASE){
							t = consApp.getTexture(FileNames.LADDER_BLOCK);
							t.setPreserveRatio(true);
							t.setFitHeight(40);
						}
						Entity en = new Entity(Types.Type.BLOCK);
						en.setProperty(Property.SUB_TYPE, Block.LADDER);
						en.setProperty("top", true);
						en.setCollidable(true);
						en.setGraphics(new Rectangle(Config.BLOCK_SIZE, 2));
						en.setVisible(false);
						en.setPosition(j * Config.BLOCK_SIZE, i * Config.BLOCK_SIZE - 1);
						level.entities.add(en);
					}
					else{
						e = new Entity(Types.Type.BLOCK);
						e.setProperty(Property.SUB_TYPE, Block.LADDER);
						e.setProperty("top", false);
						e.setCollidable(true);
						e.setPosition(0, -1);
						rect.setFill(Color.GREY);
						if(Config.RELEASE){
							t = consApp.getTexture(FileNames.LADDER_BLOCK);
							t.setPreserveRatio(true);
							t.setFitHeight(40);
						}
					}
					break;
				case 'q':
				case 'Q':
				case 'r':
					if(level.nextLevelEntity == null && 
							(i+1 >= data.size() || data.get(i+1) == null || 
								data.get(i+1).charAt(j) != '1')){
						e = new Entity(Types.Type.NEXT_LEVEL_POINT);
						e.setCollidable(true);
						e.setProperty("autoDoor", false);
						level.nextLevelEntity = e;
						rect.setFill(Color.BLACK);
						if(Config.RELEASE){
							if(line.charAt(j) == 'q'){
								t = consApp.getTexture(FileNames.W_DOOR);
								t.setFitHeight(60);
								e.setPosition(0, -18);
							}
							else if(line.charAt(j) == 'Q'){
								t = consApp.getTexture(FileNames.S_DOOR);
								t.setFitHeight(60);
								e.setPosition(0, -18);
							}
							else{
								t = consApp.getTexture(FileNames.CAVE);	
								t.setFitWidth(80);
								e.setPosition(0, -24);
							}
						
							t.setPreserveRatio(true);
						}
					}
					else if(i+1 < data.size() && data.get(i+1) != null || 
							data.get(i+1).charAt(j) == '1'){
						e = new Entity(Types.Type.PROP);
						e.setCollidable(false);
						rect.setFill(Color.VIOLET);
						if(Config.RELEASE){
							if(line.charAt(j) == 'q'){
								t = consApp.getTexture(FileNames.W_DOOR);
								t.setFitHeight(60);
								e.setPosition(0, 22);
								t.setPreserveRatio(true);
							}
							else if(line.charAt(j) == 'Q'){
								t = consApp.getTexture(FileNames.S_DOOR);
								t.setFitHeight(60);
								e.setPosition(0, 22);
								t.setPreserveRatio(true);
							}
							else{
								t = consApp.getTexture(FileNames.CAVE);
								t.setPreserveRatio(false);
								t.setFitWidth(80);
								t.setFitHeight(80);
								e.setPosition(0, 0);
								
							}
						}
					}
					break;
				case 's':
				case 'S':
				case 't':
				case 'T':{
					Entity en = new Entity(Type.ENEMY_SPAWNER);
					rect.setFill(Color.RED);
					en.setPosition(en.getPosition().add(j * Config.BLOCK_SIZE, i * Config.BLOCK_SIZE));
					en.setGraphics(rect);
					
					en.setVisible(false);
					en.setCollidable(false);
					en.addControl(getSpawner(line.charAt(j), levelNumber, en));
					en.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
						if(en != null && en.getControl(ESpawnerControl.class) != null){
							Pair<Entity,Entity> tEn = en.getControl(ESpawnerControl.class).spawnEnemy();
							if(tEn != null){
								consApp.getSceneManager().addEntities(tEn.getKey(), tEn.getValue());
							}
						}
					});
					en.addFXGLEventHandler(Event.DEATH, event -> {
						consApp.getSceneManager().removeEntity(en);
					});
					
					level.entities.add(en);
					
					break;
				}
				case 'u':
				case 'U':
				case 'v':
					e = new Entity(Types.Type.POWERUP);
					if(line.charAt(j) == 'u'){
						e.setProperty(Property.SUB_TYPE, Powerup.INC_MAX_HEALTH);
					}
					else if(line.charAt(j) == 'U'){
						e.setProperty(Property.SUB_TYPE, Powerup.INC_MAX_MANA);
					}
					else{
						e.setProperty(Property.SUB_TYPE, Powerup.INC_MANA_REGEN);
					}
					
					e.setCollidable(true);
					rect.setFill(Color.PURPLE);
					if(Config.RELEASE){
						t = consApp.getTexture(FileNames.POWERUP_BLOCK);
						t.setPreserveRatio(true);
						t.setFitHeight(30);
						e.setPosition(0, 10);
					}
					break;
				case 'W':
					e = new Entity(Types.Type.PLATFORM);
					e.setCollidable(true);
					rect.setFill(Color.ORANGERED);
					t = consApp.getTexture(FileNames.FIREBALL_PROJ);
					t.setScaleY(-1);
					t.setPreserveRatio(true);
					t.setFitWidth(Config.BLOCK_SIZE);
					t.setTranslateY(-Config.BLOCK_SIZE/2);
					break;
				case 'x':
					e = new Entity(Types.Type.PROP);
					e.setCollidable(false);
					rect.setFill(Color.BLUEVIOLET);
					if(Config.RELEASE){
						t = consApp.getTexture(FileNames.CORPSE_BLOCK);
						t.setPreserveRatio(true);
						t.setFitWidth(60);
						e.setPosition(0, 24);
					}
					break;
				case 'X':
					e = new Entity(Types.Type.PROP);
					e.setCollidable(false);
					rect.setFill(Color.VIOLET);
					if(Config.RELEASE){
						t = consApp.getTexture(FileNames.COFFIN_BLOCK);
						t.setPreserveRatio(true);
						t.setFitHeight(80);
						e.setPosition(0, -40);
					}
					break;
					
				case 'y':		
					e = new Entity(Types.Type.PROP);
					e.setCollidable(false);
					rect.setFill(Color.BLUEVIOLET);
					if(Config.RELEASE){
						t = consApp.getTexture(FileNames.MOTHER_LYING);
						t.setPreserveRatio(true);
						t.setFitWidth(35);
						t.setScaleX(-1);
						e.setPosition(5, 9-Config.BLOCK_SIZE*5);
					}
					break;
				case 'Y':		
					e = new Entity(Types.Type.PROP);
					e.setCollidable(false);
					
					rect.setFill(Color.VIOLET);
					if(Config.RELEASE){
						t = consApp.getTexture(FileNames.MOTHER_STAND);
						t.setPreserveRatio(true);
						t.setFitHeight(40);
						//e.setPosition(0, -40);
					}
					break;
				}

				if (e != null) {
					e.setPosition(e.getPosition().add(j * Config.BLOCK_SIZE, i * Config.BLOCK_SIZE));
					if(Config.RELEASE && t != null){
						e.setGraphics(t);
					}
					else{
						e.setGraphics(rect);
					}
					level.entities.add(e);
				}
			}
		}
		
		if(level.uLLim == null){
			level.uLLim = new Point2D(0, 0);
		}
		if(level.lRLim == null){
			level.lRLim = new Point2D(level.width, level.height);
		}
		
		level.entities.addAll(0, getBackgroundEntities(levelNumber, level, backY));

		if (!level.isValid())
			throw new IllegalArgumentException("Level: " + levelNumber + " was not parsed as valid");

		return level;
	}

	private String getSurfaceTexture(char p) {
		switch(p){
			case 'i':
				return FileNames.S_DIRT_BLOCK;
			case 'I':
				return FileNames.G_DIRT_BLOCK;
			case 'j':
				return FileNames.ST_DIRT_BLOCK;
			case 'k':
				return FileNames.S_SAND_BLOCK;
			case 'K':
				return FileNames.G_SAND_BLOCK;
			case 'l':
				return FileNames.S_N_SAND_BLOCK;
			case 'L':
				return FileNames.G_N_SAND_BLOCK;
			case 'm':
				return FileNames.S_STONE_BLOCK;
			case 'M':
				return FileNames.SN_STONE_BLOCK;
			case 'n':
				return FileNames.S_SANDSTONE_BLOCK;
		}

		return FileNames.S_DIRT_BLOCK;
	}
	
	private String getUnderTexture(char p){
		switch(p){
		case 'i':
		case 'I':
		case 'j':
			return FileNames.U_DIRT_BLOCK;
		case 'k':
		case 'K':
			return FileNames.U_SAND_BLOCK;
		case 'l':
		case 'L':
			return FileNames.U_N_SAND_BLOCK;
		case 'm':
		case 'M':
			return FileNames.U_STONE_BLOCK;
		case 'n':
			return FileNames.U_SANDSTONE_BLOCK;
	}

	return FileNames.U_DIRT_BLOCK;
	}

	private ArrayList<Entity> getBackgroundEntities(int levelNumber, Level level, Point2D backY) {
		ArrayList<Entity> backEn = new ArrayList<Entity>();
		
		int backWidth = 640;
		int backHeight = 360;
		
		if(levelNumber == 7 || levelNumber == 8){
			for(int j = -1; j * backHeight <= level.height; j++) {
				for(int i = 0; i * backWidth <= level.width; i++) {
					Entity bEn = new Entity(Types.Type.BACKGROUND);
					bEn.setCollidable(false);
					bEn.setVisible(true);
					bEn.setPosition(i * backWidth, j * backHeight);
					bEn.setGraphics(getBackground(levelNumber));
								
					backEn.add(bEn);
				}
			}
		}
		else{
			Entity bUEn = new Entity(Types.Type.BACKGROUND);
			bUEn.setCollidable(false);
			bUEn.setVisible(true);
			bUEn.setPosition(0, 0);
			
			Rectangle backUCol = new Rectangle(0,0,level.width,backY.getY() + 1);
			
			PixelReader pRead = getBackground(levelNumber).getImage().getPixelReader();
			double red = 0;
			double green = 0;
			double blue = 0;
			int count = 0;
			
			for(int i = 0; i <= backWidth; i += 5){
				if(i > 0){
					red += pRead.getColor(i-1, 0).getRed();
					green += pRead.getColor(i-1, 0).getGreen();
					blue += pRead.getColor(i-1, 0).getBlue();
				}
				else{
					red += pRead.getColor(0, 0).getRed();
					green += pRead.getColor(0, 0).getGreen();
					blue += pRead.getColor(0, 0).getBlue();
				}
				count++;
			}
			
			backUCol.setFill(new Color((double)red/count, (double)green/count, (double)blue/count, 1));
			red = 0;
			green = 0;
			blue = 0;
			count = 0;
			bUEn.setGraphics(backUCol);
			backEn.add(bUEn);
			
			Entity bDEn = new Entity(Types.Type.BACKGROUND);
			bDEn.setCollidable(false);
			bDEn.setVisible(true);
			bDEn.setPosition(0, (backY.getY() + backHeight - 1));
			
			Rectangle backDCol = new Rectangle(0,0,level.width, (level.height - (backY.getY() + backHeight)) + 1);
			
			for(int i = 0; i <= backWidth; i += 5){
				if(i > 0){
					red += pRead.getColor(i-1, backHeight-1).getRed();
					green += pRead.getColor(i-1, backHeight-1).getGreen();
					blue += pRead.getColor(i-1, backHeight-1).getBlue();
				}
				else{
					red += pRead.getColor(0, backHeight-1).getRed();
					green += pRead.getColor(0, backHeight-1).getGreen();
					blue += pRead.getColor(0, backHeight-1).getBlue();
				}
				count++;
			}
			
			backDCol.setFill(new Color((double)red/count, (double)green/count, (double)blue/count, 1));
			bDEn.setGraphics(backDCol);
						
			backEn.add(bDEn);
			
			for(int i = 0; i * backWidth <= level.width; i++) {
				Entity bEn = new Entity(Types.Type.BACKGROUND);
				bEn.setCollidable(false);
				bEn.setVisible(true);
				bEn.setPosition(i * backWidth, backY.getY());
				bEn.setGraphics(getBackground(levelNumber));
								
				backEn.add(bEn);
			}
			
			
		}

		return backEn;
	}
	
	private boolean isSameZone(char p, char p2){
		if(p == 'i' || p == 'I' || p == 'j'){
			if(p2 == 'i' || p2 == 'I' || p2 == 'j'){
				return true;
			}
		}
		else if(p == 'k' || p == 'K'){
			if(p2 == 'k' || p2 == 'K'){
				return true;
			}
		}
		else if(p == 'l' || p == 'L'){
			if(p2 == 'l' || p2 == 'L'){
				return true;
			}
		}
		else if(p == 'm' || p == 'M'){
			if(p2 == 'm' || p2 == 'M'){
				return true;
			}
		}
		else if(p == 'n'){
			if(p2 == 'n'){
				return true;
			}
		}
		
		return false;
	}
	private boolean isPlatform(char p){
		if(p == 'i' || p == 'I' || p == 'j' || p == 'k' || p == 'K' ||
				p == 'l' || p == 'L' || p == 'm' || p == 'M' || p == 'n'){
			return true;
		}
		return false;
	}
	
	private String getZoneLine(char p){
		if(p == 'i' || p == 'I' || p == 'j'){
			return FileNames.U_DIRT_LINE;
		}
		else if(p == 'k' || p == 'K'){
			return FileNames.U_SAND_LINE;
		}
		else if(p == 'l' || p == 'L'){
			return FileNames.U_N_SAND_LINE;
		}
		else if (p == 'm' || p == 'M'){
			return FileNames.U_STONE_LINE;
		}
		else{
			return FileNames.U_SANDSTONE_LINE;
		}
	}

	private Texture getBackground(int levelNumber) {

		if(!Config.RELEASE){
			return consApp.getTexture("forest1.jpg");
		}
		
		switch(levelNumber){
			case 0: {
				return consApp.getTexture(FileNames.FOREST1_BACK_1);
			}
			case 1: {
				return consApp.getTexture(FileNames.FOREST1_BACK_3);
			}
			case 2: {
				return consApp.getTexture(FileNames.EMPIRE_BACK_2);
			}
			case 3:
			case 4:
			case 5: {
				return consApp.getTexture(FileNames.DESERT_BACK_1);
			}
			case 6: {
				return consApp.getTexture(FileNames.PYRAMID_BACK_1);
			}
			case 7:
			case 8: {
				return consApp.getTexture(FileNames.PYRAMID_BACK_2);
			}
			case 9:
			case 10:
			case 11: {
				return consApp.getTexture(FileNames.FESTIVAL_BACK_1);
			}
			case 12: {
				return consApp.getTexture(FileNames.FOREST1_BACK_2);
			}
			case 13: {
				return consApp.getTexture(FileNames.FOREST1_BACK_3);
			}
			case 14: {
				return consApp.getTexture(FileNames.EMPIRE_BACK_2);
			}
			case 15: {
				return consApp.getTexture(FileNames.MOUNTAIN_BACK_1);
			}
			case 16:
			case 17: {
				return consApp.getTexture(FileNames.MOUNTAIN_BACK_2);
			}
			case 18:
			case 19:
			case 20: {
				return consApp.getTexture(FileNames.COLONY_BACK_1);
			}
			case 21:
			case 22: {
				return consApp.getTexture(FileNames.EMPIRE_BACK_1);
			}
			case 23: {
				return consApp.getTexture(FileNames.EMPIRE_BACK_2);
			}
			default: {
				return consApp.getTexture(FileNames.FESTIVAL_BACK_1);
			}
		}
	}
	
	private ESpawnerControl getSpawner(char c, int level, Entity spawner){
		switch(level){
			case 0:
			case 1:
			case 2:{
				if(c == 's'){
					return new ESpawnerControl(consApp, spawner.getPosition(), (Function<Point2D, Pair<Entity, Entity>>) (t) -> consApp.eSpawner.spawnEloko(t), 
											1);
				}
				else if (c == 'S'){
					return new ESpawnerControl(consApp, spawner.getPosition(),  (Function<Point2D, Pair<Entity, Entity>>) (t) -> consApp.eSpawner.spawnBKnifer(t), 
							1);
				}
				else if (c == 't'){
					return new ESpawnerControl(consApp, spawner.getPosition(), (Function<Point2D, Pair<Entity, Entity>>) (t) -> consApp.eSpawner.spawnBSpearEnemy(t), 
							1);
				}
				else{
					return new ESpawnerControl(consApp, spawner.getPosition(), (Function<Point2D, Pair<Entity, Entity>>) (t) -> consApp.eSpawner.spawnEloko(t), 
							1);
				}
			}
			case 3:
			case 4:
			case 5:{
				if(c == 's'){
					return new ESpawnerControl(consApp, spawner.getPosition(), (Function<Point2D, Pair<Entity, Entity>>) (t) -> consApp.eSpawner.spawnScarab(t), 
											1);
				}
				else if (c == 'S'){
					return new ESpawnerControl(consApp, spawner.getPosition(), (Function<Point2D, Pair<Entity, Entity>>) (t) -> consApp.eSpawner.spawnLocust(t), 
							1);
				}
				else if (c == 't'){
					return new ESpawnerControl(consApp, spawner.getPosition(), (Function<Point2D, Pair<Entity, Entity>>) (t) -> consApp.eSpawner.spawnScorpion(t), 
							1);
				}
				else{
					return new ESpawnerControl(consApp, spawner.getPosition(), (Function<Point2D, Pair<Entity, Entity>>) (t) -> consApp.eSpawner.spawnStoneEnemy(t), 
							1);
				}
			}
			case 6:
			case 7:
			case 8:{
				if(c == 's'){
					return new ESpawnerControl(consApp, spawner.getPosition(), (Function<Point2D, Pair<Entity, Entity>>) (t) -> consApp.eSpawner.spawnScarab(t), 
											1);
				}
				else if (c == 'S'){
					return new ESpawnerControl(consApp, spawner.getPosition(), (Function<Point2D, Pair<Entity, Entity>>) (t) -> consApp.eSpawner.spawnScorpion(t), 
							1);
				}
				else if (c == 't'){
					return new ESpawnerControl(consApp, spawner.getPosition(), (Function<Point2D, Pair<Entity, Entity>>) (t) -> consApp.eSpawner.spawnBurner(t), 
							1, true);
				}
				else{
					return new ESpawnerControl(consApp, spawner.getPosition(), (Function<Point2D, Pair<Entity, Entity>>) (t) -> consApp.eSpawner.spawnIceSpirit(t), 
							1);
				}
			}
			case 9:
			case 10:
			case 11:{
				if(c == 's'){
					return new ESpawnerControl(consApp, spawner.getPosition(), (Function<Point2D, Pair<Entity, Entity>>) (t) -> consApp.eSpawner.spawnMusician(t), 
											1);
				}
				else if (c == 'S'){
					return new ESpawnerControl(consApp, spawner.getPosition(), (Function<Point2D, Pair<Entity, Entity>>) (t) -> consApp.eSpawner.spawnDancer(t), 
							1);
				}
				else if (c == 't'){
					return new ESpawnerControl(consApp, spawner.getPosition(), (Function<Point2D, Pair<Entity, Entity>>) (t) -> consApp.eSpawner.spawnDog(t), 
							1);
				}
				else{
					return new ESpawnerControl(consApp, spawner.getPosition(), (Function<Point2D, Pair<Entity, Entity>>) (t) -> consApp.eSpawner.spawnDog(t), 
							1);
				}
			}
			case 12:
			case 13:
			case 14:{
				if(c == 's'){
					return new ESpawnerControl(consApp, spawner.getPosition(), (Function<Point2D, Pair<Entity, Entity>>) (t) -> consApp.eSpawner.spawnEloko(t), 
											1);
				}
				else if (c == 'S'){
					return new ESpawnerControl(consApp, spawner.getPosition(), (Function<Point2D, Pair<Entity, Entity>>) (t) -> consApp.eSpawner.spawnBKnifer(t), 
							1);
				}
				else if (c == 't'){
					return new ESpawnerControl(consApp, spawner.getPosition(), (Function<Point2D, Pair<Entity, Entity>>) (t) -> consApp.eSpawner.spawnBSpearEnemy(t), 
							1);
				}
				else{
					return new ESpawnerControl(consApp, spawner.getPosition(), (Function<Point2D, Pair<Entity, Entity>>) (t) -> consApp.eSpawner.spawnMagician(t), 
							1);
				}
			}
			case 15:
			case 16:
			case 17:{
				if(c == 's'){
					return new ESpawnerControl(consApp, spawner.getPosition(), (Function<Point2D, Pair<Entity, Entity>>) (t) -> consApp.eSpawner.spawnIceSpirit(t), 
											1);
				}
				else if (c == 'S'){
					return new ESpawnerControl(consApp, spawner.getPosition(), (Function<Point2D, Pair<Entity, Entity>>) (t) -> consApp.eSpawner.spawnStoneEnemy(t), 
							1);
				}
				else if (c == 't'){
					return new ESpawnerControl(consApp, spawner.getPosition(), (Function<Point2D, Pair<Entity, Entity>>) (t) -> consApp.eSpawner.spawnBKnifer(t), 
							1);
				}
				else{
					return new ESpawnerControl(consApp, spawner.getPosition(), (Function<Point2D, Pair<Entity, Entity>>) (t) -> consApp.eSpawner.spawnStoneEnemy(t), 
							1);
				}
			}
			case 18:
			case 19:
			case 20:{
				if(c == 's'){
					return new ESpawnerControl(consApp, spawner.getPosition(), (Function<Point2D, Pair<Entity, Entity>>) (t) -> consApp.eSpawner.spawnCannon(t), 
											1);
				}
				else if (c == 'S'){
					return new ESpawnerControl(consApp, spawner.getPosition(), (Function<Point2D, Pair<Entity, Entity>>) (t) -> consApp.eSpawner.spawnBayoneter(t), 
							1);
				}
				else if (c == 't'){
					return new ESpawnerControl(consApp, spawner.getPosition(), (Function<Point2D, Pair<Entity, Entity>>) (t) -> consApp.eSpawner.spawnRifler(t, false), 
							1);
				}
				else{
					return new ESpawnerControl(consApp, spawner.getPosition(), (Function<Point2D, Pair<Entity, Entity>>) (t) -> consApp.eSpawner.spawnRifler(t, true), 
							1);
				}
			}
			case 21:
			case 22:
			case 23:{
				if(c == 's'){
					return new ESpawnerControl(consApp, spawner.getPosition(), (Function<Point2D, Pair<Entity, Entity>>) (t) -> consApp.eSpawner.spawnZKnifer(t), 
											1);
				}
				else if (c == 'S'){
					return new ESpawnerControl(consApp, spawner.getPosition(), (Function<Point2D, Pair<Entity, Entity>>) (t) -> consApp.eSpawner.spawnZSpearEnemy(t,false), 
							1);
				}
				else if (c == 't'){
					return new ESpawnerControl(consApp, spawner.getPosition(), (Function<Point2D, Pair<Entity, Entity>>) (t) -> consApp.eSpawner.spawnCharger(t), 
							1);
				}
				else{
					return new ESpawnerControl(consApp, spawner.getPosition(), (Function<Point2D, Pair<Entity, Entity>>) (t) -> consApp.eSpawner.spawnZSpearEnemy(t,true), 
							1);
				}
			}
		}
		
		return new ESpawnerControl(consApp, spawner.getPosition(), (Function<Point2D, Pair<Entity, Entity>>) (t) -> consApp.eSpawner.spawnEloko(t), 
				1);
	}

	public static class LevelData {
		private List<String> data;

		public LevelData(List<String> data) {
			this.data = data;
		}

		public List<String> getData() {
			return data;
		}
	}

	public static class Level {
		private int width, height;
		private List<Entity> entities = new ArrayList<>();
		private Point2D spawnPoint;
		private Entity nextLevelEntity;
		private Point2D uLLim, lRLim;

		/* package-private */ Level() {

		}
		
		public void clean(){
			entities.clear();
			nextLevelEntity = null;
		}

		private boolean isValid() {
			return spawnPoint != null && nextLevelEntity != null && width != 0 && height != 0 && 
					uLLim != null && lRLim != null;
		}

		public List<Entity> getEntities() {
			return entities;
		}

		public Entity[] getEntitiesAsArray() {
			return entities.toArray(new Entity[0]);
		}

		public Point2D getSpawnPoint() {
			return spawnPoint;
		}

		public Entity getNextLevelEntity() {
			return nextLevelEntity;
		}
		
		public Point2D getUpperLeftLimit(){
			return uLLim;
		}
		
		public Point2D getLowerRightLimit(){
			return lRLim;
		}
	}
}
