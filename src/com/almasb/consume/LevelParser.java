package com.almasb.consume;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

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
					e.setPosition(0, -Config.BLOCK_SIZE);
					rect.setHeight(Config.BLOCK_SIZE*2);
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
					e = new Entity(Types.Type.BLOCK);
					e.setProperty(Property.SUB_TYPE, Block.BARRIER);
					e.setProperty("state", "idle");
					e.setProperty("start", "none");
					e.setCollidable(true);
					e.setProperty("blockImg", line.charAt(j)); //See activateBarrier() in ConsumeApp for pic choice
					e.setVisible(false);
					break;
				case 'B':{
					//TODO: BOSS SPAWNER, add Boss, add Boss Health Bar, bind viewport to center of arena, start scene
					e = new Entity(Type.BOSS_SPAWNER);
					rect.setFill(Color.RED);
					e.setVisible(false);
					e.setCollidable(false);
					break;
				}
				case 'c':
				case 'C':{
					Entity en = new Entity(Type.BLOCK);
					en.setProperty(Property.SUB_TYPE, Type.ENEMY_SPAWNER);
					rect.setFill(Color.RED);
					en.setPosition(en.getPosition().add(j * Config.BLOCK_SIZE, (i * Config.BLOCK_SIZE) - Config.BLOCK_SIZE/2));
					en.setGraphics(rect);
					
					en.setCollidable(true);
					if(line.charAt(j) == 'c'){
						en.addControl(new CollideSpawnerControl(
								(Function<Point2D, Entity>) (tS) -> consApp.eSpawner.spawnMummy(tS), 
								2, en.getPosition().add(-Config.BLOCK_SIZE*2, Config.BLOCK_SIZE/2), 
								en.getPosition().add(Config.BLOCK_SIZE*2, Config.BLOCK_SIZE/2)));
						en.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
							if(en != null && en.getControl(CollideSpawnerControl.class) != null){
								ArrayList<Entity> ens = en.getControl(CollideSpawnerControl.class).spawnEnemy();
								for(Entity enV : ens){
									consApp.getSceneManager().addEntities(enV);
								}
								
							}
						});
					}
					else{
						en.addControl(new CollideSpawnerControl(
								(Function<Point2D, Entity>) (tS) -> consApp.eSpawner.spawnSandBoss(tS), 
								1, en.getPosition().add(Config.BLOCK_SIZE*8, Config.BLOCK_SIZE/2)));
						en.addFXGLEventHandler(Event.ENEMY_FIRED, event -> {
							if(en != null && en.getControl(CollideSpawnerControl.class) != null){
								ArrayList<Entity> ens = en.getControl(CollideSpawnerControl.class).spawnEnemy();
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
							case 'E': t = consApp.getTexture(FileNames.DES_BRICK_BLOCK); break;
							case 'f': t = consApp.getTexture(FileNames.DES_ICE_BLOCK); break;
						}
						t.setPreserveRatio(true);
						t.setFitHeight(40);
						
						
					}
					break;
				case 'i':
				case 'I':
				case 'j':
					if(j + 1 < line.length() && line.charAt(j) == line.charAt(j+1) && 
							(data.get(i-1).charAt(j) == 'i' || data.get(i-1).charAt(j) == 'I' || data.get(i-1).charAt(j) == 'j')){
						int len = 1;
						for(int b = 1; b + j < line.length(); b++){
							if(len < 40 && i >= 1 && data.get(i-1) != null && 
									(data.get(i-1).charAt(j + b) == 'i' || data.get(i-1).charAt(j + b) == 'I' || data.get(i-1).charAt(j + b) == 'j')){
								len++;
							}
							else{
								break;
							}
						}
						
						Entity en = new Entity(Types.Type.PLATFORM);
						en.setProperty(Property.SUB_TYPE, Platform.INDESTRUCTIBLE);
						Texture tex = consApp.getTexture(FileNames.U_DIRT_LINE);
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
									(data.get(i-1).charAt(j) != 'i' && data.get(i-1).charAt(j) != 'I' && data.get(i-1).charAt(j) != 'j')){
								if(line.charAt(j) == 'I'){
									t = consApp.getTexture(FileNames.G_DIRT_BLOCK);
								}
								else if (line.charAt(j) == 'j'){
									t = consApp.getTexture(FileNames.ST_DIRT_BLOCK);
								}
								else{
									t = consApp.getTexture(FileNames.S_DIRT_BLOCK);
								}
							}
							else{
								t = consApp.getTexture(FileNames.U_DIRT_BLOCK);
							}
						
							if(j >= 1 && line.charAt(j-1) == line.charAt(j) && level.entities.get(level.entities.size() - 1).getScaleX() == 1){
								e.setScaleX(-1);
							}
							t.setPreserveRatio(true);
							t.setFitHeight(40);
						}
					}
					break;
				case 'k':
				case 'K':
					if(j + 1 < line.length() && line.charAt(j) == line.charAt(j+1) && 
						(data.get(i-1).charAt(j) == 'k' || data.get(i-1).charAt(j) == 'K')){
						int len = 1;
						for(int b = 1; b + j < line.length(); b++){
							if(len < 40 && i >= 1 && data.get(i-1) != null && 
									(data.get(i-1).charAt(j + b) == 'k' || data.get(i-1).charAt(j + b) == 'K')){
								len++;
							}
							else{
								break;
							}
						}
				
						Entity en = new Entity(Types.Type.PLATFORM);
						en.setProperty(Property.SUB_TYPE, Platform.INDESTRUCTIBLE);
						Texture tex = consApp.getTexture(FileNames.U_SAND_LINE);
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
							if(i >= 1 && data.get(i-1) != null && (data.get(i-1).charAt(j) != 'k' && data.get(i-1).charAt(j) != 'K')){
								if(line.charAt(j) == 'K'){
									t = consApp.getTexture(FileNames.G_SAND_BLOCK);
								}
								else{
									t = consApp.getTexture(FileNames.S_SAND_BLOCK);
								}
							}
							else{
								t = consApp.getTexture(FileNames.U_SAND_BLOCK);
							}
							if(j >= 1 && line.charAt(j-1) == line.charAt(j) && level.entities.get(level.entities.size() - 1).getScaleX() == 1){
								e.setScaleX(-1);
							}
							t.setPreserveRatio(true);
							t.setFitHeight(40);
						}
					}
					break;
				case 'l':
				case 'L':
					if(j + 1 < line.length() && line.charAt(j) == line.charAt(j+1) && 
						(data.get(i-1).charAt(j) == 'l' || data.get(i-1).charAt(j) == 'L')){
						int len = 1;
						for(int b = 1; b + j < line.length(); b++){
							if(len < 40 && i >= 1 && data.get(i-1) != null && 
								(data.get(i-1).charAt(j + b) == 'l' || data.get(i-1).charAt(j + b) == 'L')){
								len++;
							}
							else{
								break;
							}
						}
			
						Entity en = new Entity(Types.Type.PLATFORM);
						en.setProperty(Property.SUB_TYPE, Platform.INDESTRUCTIBLE);
						Texture tex = consApp.getTexture(FileNames.U_N_SAND_LINE);
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
							if(i >= 1 && data.get(i-1) != null && (data.get(i-1).charAt(j) != 'l' && data.get(i-1).charAt(j) != 'L')){
								if(line.charAt(j) == 'L'){
									t = consApp.getTexture(FileNames.G_N_SAND_BLOCK);
								}
								else{
									t = consApp.getTexture(FileNames.S_N_SAND_BLOCK);
								}
							}
							else{
								t = consApp.getTexture(FileNames.U_N_SAND_BLOCK);
							}
							if(j >= 1 && line.charAt(j-1) == line.charAt(j) && level.entities.get(level.entities.size() - 1).getScaleX() == 1){
								e.setScaleX(-1);
							}
							t.setPreserveRatio(true);
							t.setFitHeight(40);
						}
					}
					break;
				case 'm':
				case 'M':
					if(j + 1 < line.length() && line.charAt(j) == line.charAt(j+1) && 
						(data.get(i-1).charAt(j) == 'm' || data.get(i-1).charAt(j) == 'M')){
						int len = 1;
						for(int b = 1; b + j < line.length(); b++){
							if(len < 40 && i >= 1 && data.get(i-1) != null && 
								(data.get(i-1).charAt(j + b) == 'm' || data.get(i-1).charAt(j + b) == 'M')){
								len++;
							}
							else{
								break;
							}
						}
			
						Entity en = new Entity(Types.Type.PLATFORM);
						en.setProperty(Property.SUB_TYPE, Platform.INDESTRUCTIBLE);
						Texture tex = consApp.getTexture(FileNames.U_STONE_LINE);
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
							if(i >= 1 && data.get(i-1) != null && (data.get(i-1).charAt(j) != 'm' && data.get(i-1).charAt(j) != 'M')){
								if(line.charAt(j) == 'M'){
									t = consApp.getTexture(FileNames.SN_STONE_BLOCK);
								}
								else{
									t = consApp.getTexture(FileNames.S_STONE_BLOCK);
								}
							}
							else{
								t = consApp.getTexture(FileNames.U_STONE_BLOCK);
							}
							if(j >= 1 && line.charAt(j-1) == line.charAt(j) && level.entities.get(level.entities.size() - 1).getScaleX() == 1){
								e.setScaleX(-1);
							}
							t.setPreserveRatio(true);
							t.setFitHeight(40);
						}
					}
					break;
				case 'n':
					if(j + 1 < line.length() && line.charAt(j) == line.charAt(j+1) && 
						(data.get(i-1).charAt(j) == 'n')){
						int len = 1;
						for(int b = 1; b + j < line.length(); b++){
							if(len < 40 && i >= 1 && data.get(i-1) != null && 
								(data.get(i-1).charAt(j + b) == 'n')){
								len++;
							}
							else{
								break;
							}
						}
			
						Entity en = new Entity(Types.Type.PLATFORM);
						en.setProperty(Property.SUB_TYPE, Platform.INDESTRUCTIBLE);
						Texture tex = consApp.getTexture(FileNames.U_STONE_LINE);
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
							if(i >= 1 && data.get(i-1) != null && (data.get(i-1).charAt(j) != 'n')){
								t = consApp.getTexture(FileNames.S_SANDSTONE_BLOCK);
							}
							else{
								t = consApp.getTexture(FileNames.U_SANDSTONE_BLOCK);
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
					e = new Entity(Types.Type.BLOCK);
					e.setProperty(Property.SUB_TYPE, Block.LADDER);
					e.setCollidable(true);
					rect.setFill(Color.GREY);
					if(Config.RELEASE){
						t = consApp.getTexture(FileNames.LADDER_BLOCK);
						t.setPreserveRatio(true);
						t.setFitHeight(40);
					}
					break;
				case 'q':
				case 'Q':
				case 'r':
					if(level.nextLevelEntity == null){
						e = new Entity(Types.Type.NEXT_LEVEL_POINT);
						e.setCollidable(true);
						e.setProperty("autoDoor", false);
						level.nextLevelEntity = e;
						rect.setFill(Color.BLACK);
						if(Config.RELEASE){
							if(line.charAt(j) == 'q'){
								t = consApp.getTexture(FileNames.W_DOOR);
							}
							else if(line.charAt(j) == 'Q'){
								t = consApp.getTexture(FileNames.S_DOOR);
							}
							else{
								t = consApp.getTexture(FileNames.CAVE);				
							}
						
							t.setPreserveRatio(true);
							t.setFitHeight(60);
							e.setPosition(0, -20);
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
							consApp.getSceneManager().addEntities(en.getControl(ESpawnerControl.class).spawnEnemy());
						}
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
					
				case 'y':		//TODO: Add mother
					e = new Entity(Types.Type.PROP);
					e.setCollidable(false);
					rect.setFill(Color.BLUEVIOLET);
					if(Config.RELEASE){
						t = consApp.getTexture(FileNames.MOTHER_LYING);
						t.setPreserveRatio(true);
						t.setFitWidth(60);
						e.setPosition(0, 24);
					}
					break;
				case 'Y':		//TODO: Add mother
					e = new Entity(Types.Type.PROP);
					e.setCollidable(false);
					rect.setFill(Color.VIOLET);
					if(Config.RELEASE){
						t = consApp.getTexture(FileNames.MOTHER_STAND);
						t.setPreserveRatio(true);
						t.setFitHeight(80);
						e.setPosition(0, -40);
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
			
			Rectangle backUCol = new Rectangle(0,0,level.width,backY.getY());
			backUCol.setFill(getBackground(levelNumber).getImage().getPixelReader().getColor(1, 1));
			bUEn.setGraphics(backUCol);
						
			backEn.add(bUEn);
			
			for(int i = 0; i * backWidth <= level.width; i++) {
				Entity bEn = new Entity(Types.Type.BACKGROUND);
				bEn.setCollidable(false);
				bEn.setVisible(true);
				bEn.setPosition(i * backWidth, backY.getY());
				bEn.setGraphics(getBackground(levelNumber));
								
				backEn.add(bEn);
			}
			
			Entity bDEn = new Entity(Types.Type.BACKGROUND);
			bDEn.setCollidable(false);
			bDEn.setVisible(true);
			bDEn.setPosition(0, (backY.getY() + backHeight));
			
			Rectangle backDCol = new Rectangle(0,0,level.width, level.height - (backY.getY() + backHeight));
			backDCol.setFill(getBackground(levelNumber).getImage().getPixelReader().getColor(1, backHeight-1));
			bDEn.setGraphics(backDCol);
						
			backEn.add(bDEn);
		}

		return backEn;
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
					return new ESpawnerControl(consApp, (Function<Point2D, Entity>) (t) -> consApp.eSpawner.spawnEloko(t), 
											1);
				}
				else if (c == 'S'){
					return new ESpawnerControl(consApp, (Function<Point2D, Entity>) (t) -> consApp.eSpawner.spawnBKnifer(t), 
							1);
				}
				else if (c == 't'){
					return new ESpawnerControl(consApp, (Function<Point2D, Entity>) (t) -> consApp.eSpawner.spawnBSpearEnemy(t), 
							1);
				}
				else{
					return new ESpawnerControl(consApp, (Function<Point2D, Entity>) (t) -> consApp.eSpawner.spawnEloko(t), 
							1);
				}
			}
			case 3:
			case 4:
			case 5:{
				if(c == 's'){
					return new ESpawnerControl(consApp, (Function<Point2D, Entity>) (t) -> consApp.eSpawner.spawnScarab(t), 
											1);
				}
				else if (c == 'S'){
					return new ESpawnerControl(consApp, (Function<Point2D, Entity>) (t) -> consApp.eSpawner.spawnLocust(t), 
							1);
				}
				else if (c == 't'){
					return new ESpawnerControl(consApp, (Function<Point2D, Entity>) (t) -> consApp.eSpawner.spawnScorpion(t), 
							1);
				}
				else{
					return new ESpawnerControl(consApp, (Function<Point2D, Entity>) (t) -> consApp.eSpawner.spawnStoneEnemy(t), 
							1);
				}
			}
			case 6:
			case 7:
			case 8:{
				if(c == 's'){
					return new ESpawnerControl(consApp, (Function<Point2D, Entity>) (t) -> consApp.eSpawner.spawnScarab(t), 
											1);
				}
				else if (c == 'S'){
					return new ESpawnerControl(consApp, (Function<Point2D, Entity>) (t) -> consApp.eSpawner.spawnScorpion(t), 
							1);
				}
				else if (c == 't'){
					return new ESpawnerControl(consApp, (Function<Point2D, Entity>) (t) -> consApp.eSpawner.spawnBurner(t), 
							1);
				}
				else{
					return new ESpawnerControl(consApp, (Function<Point2D, Entity>) (t) -> consApp.eSpawner.spawnIceSpirit(t), 
							1);
				}
			}
			case 9:
			case 10:
			case 11:{
				if(c == 's'){
					return new ESpawnerControl(consApp, (Function<Point2D, Entity>) (t) -> consApp.eSpawner.spawnMusician(t), 
											1);
				}
				else if (c == 'S'){
					return new ESpawnerControl(consApp, (Function<Point2D, Entity>) (t) -> consApp.eSpawner.spawnDancer(t), 
							1);
				}
				else if (c == 't'){
					return new ESpawnerControl(consApp, (Function<Point2D, Entity>) (t) -> consApp.eSpawner.spawnDog(t), 
							1);
				}
				else{
					return new ESpawnerControl(consApp, (Function<Point2D, Entity>) (t) -> consApp.eSpawner.spawnDog(t), 
							1);
				}
			}
			case 12:
			case 13:
			case 14:{
				if(c == 's'){
					return new ESpawnerControl(consApp, (Function<Point2D, Entity>) (t) -> consApp.eSpawner.spawnEloko(t), 
											1);
				}
				else if (c == 'S'){
					return new ESpawnerControl(consApp, (Function<Point2D, Entity>) (t) -> consApp.eSpawner.spawnBKnifer(t), 
							1);
				}
				else if (c == 't'){
					return new ESpawnerControl(consApp, (Function<Point2D, Entity>) (t) -> consApp.eSpawner.spawnBSpearEnemy(t), 
							1);
				}
				else{
					return new ESpawnerControl(consApp, (Function<Point2D, Entity>) (t) -> consApp.eSpawner.spawnMagician(t), 
							1);
				}
			}
			case 15:
			case 16:
			case 17:{
				if(c == 's'){
					return new ESpawnerControl(consApp, (Function<Point2D, Entity>) (t) -> consApp.eSpawner.spawnIceSpirit(t), 
											1);
				}
				else if (c == 'S'){
					return new ESpawnerControl(consApp, (Function<Point2D, Entity>) (t) -> consApp.eSpawner.spawnStoneEnemy(t), 
							1);
				}
				else if (c == 't'){
					return new ESpawnerControl(consApp, (Function<Point2D, Entity>) (t) -> consApp.eSpawner.spawnIceSpirit(t), 
							1);
				}
				else{
					return new ESpawnerControl(consApp, (Function<Point2D, Entity>) (t) -> consApp.eSpawner.spawnStoneEnemy(t), 
							1);
				}
			}
			case 18:
			case 19:
			case 20:{
				if(c == 's'){
					return new ESpawnerControl(consApp, (Function<Point2D, Entity>) (t) -> consApp.eSpawner.spawnCannon(t), 
											1);
				}
				else if (c == 'S'){
					return new ESpawnerControl(consApp, (Function<Point2D, Entity>) (t) -> consApp.eSpawner.spawnBayoneter(t), 
							1);
				}
				else if (c == 't'){
					return new ESpawnerControl(consApp, (Function<Point2D, Entity>) (t) -> consApp.eSpawner.spawnRifler(t), 
							1);
				}
				else{
					return new ESpawnerControl(consApp, (Function<Point2D, Entity>) (t) -> consApp.eSpawner.spawnBayoneter(t), 
							1);
				}
			}
			case 21:
			case 22:
			case 23:{
				if(c == 's'){
					return new ESpawnerControl(consApp, (Function<Point2D, Entity>) (t) -> consApp.eSpawner.spawnZKnifer(t), 
											1);
				}
				else if (c == 'S'){
					return new ESpawnerControl(consApp, (Function<Point2D, Entity>) (t) -> consApp.eSpawner.spawnZSpearEnemy(t), 
							1);
				}
				else if (c == 't'){
					return new ESpawnerControl(consApp, (Function<Point2D, Entity>) (t) -> consApp.eSpawner.spawnCharger(t), 
							1);
				}
				else{
					return new ESpawnerControl(consApp, (Function<Point2D, Entity>) (t) -> consApp.eSpawner.spawnZKnifer(t), 
							1);
				}
			}
		}
		
		return new ESpawnerControl(consApp, (Function<Point2D, Entity>) (t) -> consApp.eSpawner.spawnEloko(t), 
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
