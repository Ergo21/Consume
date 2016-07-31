package com.almasb.consume;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.almasb.consume.LevelParser.Level;
import com.almasb.consume.LevelParser.LevelData;
import com.almasb.consume.Types.Block;
import com.almasb.consume.Types.Element;
import com.almasb.consume.Types.Platform;
import com.almasb.consume.Types.Powerup;
import com.almasb.consume.Types.Property;
import com.almasb.consume.Types.Type;
import com.almasb.consume.ai.AnimatedPlayerControl;
import com.almasb.consume.ai.PhysicsControl;
import com.almasb.consume.collision.PlayerBlockHandler;
import com.almasb.consume.collision.PlayerBossHandler;
import com.almasb.consume.collision.PlayerEnemyHandler;
import com.almasb.consume.collision.PlayerPowerupHandler;
import com.almasb.consume.collision.ProjectileBossHandler;
import com.almasb.consume.collision.ProjectileEnemyHandler;
import com.almasb.consume.collision.ProjectilePlayerHandler;
import com.almasb.fxgl.GameApplication;
import com.almasb.fxgl.asset.Assets;
import com.almasb.fxgl.asset.SaveLoadManager;
import com.almasb.fxgl.asset.Texture;
import com.almasb.fxgl.asset.AssetManager;
import com.almasb.fxgl.asset.AssetManager.Asset_Types;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.event.MenuEvent;
import com.almasb.fxgl.event.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.PhysicsManager;
import com.almasb.fxgl.settings.GameSettings;
import com.almasb.fxgl.time.TimerManager;
import com.almasb.fxgl.ui.FXGLMenu;
import com.almasb.fxgl.ui.FXGLMenuFactory;
import com.almasb.fxgl.ui.Intro;
import com.almasb.fxgl.ui.UIFactory;
import com.ergo21.consume.ConsumeController;
import com.ergo21.consume.ConsumeGameMenu;
import com.ergo21.consume.ConsumeIntro;
import com.ergo21.consume.ConsumeMainMenu;
import com.ergo21.consume.EntitySpawner;
import com.ergo21.consume.FileNames;
import com.ergo21.consume.GameSave;
import com.ergo21.consume.GameScene;
import com.ergo21.consume.IndependentLoop;
import com.ergo21.consume.LevelMenu;
import com.ergo21.consume.Player;
import com.ergo21.consume.PlayerHUD;
import com.ergo21.consume.SavedSettings;
import com.ergo21.consume.SoundManager;

import javafx.animation.FadeTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;

public class ConsumeApp extends GameApplication {

	public Assets assets;
	private LevelParser parser;
	public SoundManager soundManager;

	public Entity player;
	public Player playerData;
	public Entity camera;

	public Physics physics = new Physics(this);
	public EntitySpawner eSpawner;
	public IndependentLoop indiLoop;

	//private ArrayList<Level> levels;

	public PlayerHUD hud;
	private ConsumeGameMenu consGameMenu;
	private ConsumeMainMenu consMainMenu;
	private Text performance = new Text();

	private long regenTime = 0;

	private boolean playerDied = false;

	private Rectangle fadeScreen;
	private FadeTransition fadeIn;
	private FadeTransition fadeOut;
	private Runnable fInComMet;
	private Runnable fOutComMet;
	private boolean firstPlay;
	
	public GameScene gScene;
	public ConsumeController consController;

	public SavedSettings sSettings;

	public LevelMenu levelMenu;
	public boolean newGamePlusGame = false;
	
	public DoubleProperty backVol;
	public DoubleProperty sfxVol;
	
	private AnimatedPlayerControl playAni;
	private Point2D spawnPoint;
	
	private Pair<Point2D, Point2D> limits; //First is upper left limit, second bottom right limit
	public PlayerBlockHandler plBlHandler;
	
	public boolean introPlaying;

	@Override
	protected void initSettings(GameSettings settings) {
		settings.setTitle("Consume");
		settings.setVersion("Beta");
		settings.setWidth(640);
		settings.setHeight(360);
		settings.setFullScreen(false);
		settings.setIntroEnabled(true);
		settings.setMenuEnabled(true);
		settings.setIconFileName("app_icon.png");
		settings.setShowFPS(false);

		if(SaveLoadManager.INSTANCE.loadFileNames().isPresent() && SaveLoadManager.INSTANCE.loadFileNames().get().contains(FileNames.SETTINGS)){
			try {
				sSettings = (SavedSettings)SaveLoadManager.INSTANCE.load(FileNames.SETTINGS);
			} catch (Exception e) {
				System.out.println("Unable to load settings");
				e.printStackTrace();
				sSettings = new SavedSettings();
			}
		}
		else{
			sSettings = new SavedSettings();
			try {
				SaveLoadManager.INSTANCE.save(sSettings, FileNames.SETTINGS);
			} catch (Exception e) {
				System.out.println("Unable to save settings");
				e.printStackTrace();
			}
		}
		backVol = new SimpleDoubleProperty(sSettings.getBackMusicVolume());
		sfxVol = new SimpleDoubleProperty(sSettings.getSFXVolume()); 
	}
	
	@Override
	protected Intro initIntroVideo() {
		introPlaying = true;
        return new ConsumeIntro(this, getWidth(), getHeight());
    }

	@Override
	protected void initAssets() throws Exception {
		if(assets == null){
			//printMemoryUsage("Assets Before Init");
			assets = getZoneAssets(0);
			//printMemoryUsage("Assets After Init");
			assets.logCached();
		}
		
	}
	
	private long lastMemory = 0;
	public void printMemoryUsage(String label){
		/*Runtime run = Runtime.getRuntime();
		
		long aMem = run.totalMemory();
		long fMem = run.freeMemory();
		long uMem = aMem - fMem;
		int  cMem = (int)uMem - (int)lastMemory;
		lastMemory = uMem;
		
		System.out.println(label + " {");
		System.out.println("Free memory: " + fMem);
		System.out.println("Allocated memory: " + aMem);
		System.out.println("Using memory: " + uMem);
		//System.out.println("Max memory: " + run.maxMemory());
		System.out.println("Used memory changed: " + cMem);
		System.out.println("}");
		System.out.println();*/
	}

	@Override
	protected void initGame() {
		playerData = new Player(assets.getText(FileNames.PLAYER_STATS));
		if(newGamePlusGame){
			playerData.getPowers().add(Element.NEUTRAL2);
			playerData.getPowers().add(Element.FIRE);
			playerData.getPowers().add(Element.EARTH);
			playerData.getPowers().add(Element.LIGHTNING);
			playerData.getPowers().add(Element.METAL);
			playerData.getPowers().add(Element.DEATH);
			playerData.getPowers().add(Element.CONSUME);
			for(int i = 0; i < 24; i++){
				playerData.getUpgrades().add(i);
			}
			playerData.increaseManaRegen(Config.MANA_REGEN_INC*2);
			playerData.increaseMaxHealth(Config.MAX_HEALTH_INC*2);
			playerData.increaseMaxMana(Config.MAX_MANA_INC*2);
			playerData.restoreHealth(1);
			playerData.restoreMana(playerData.getMaxMana());
		}
		
		eSpawner = new EntitySpawner(this);
		
		//printMemoryUsage("Game Init");

		initLevels();
		//printMemoryUsage("Levels Init");
		loadLevel(playerData.getCurrentLevel());
		
		//printMemoryUsage("Levels Load");
	}

	@Override
	protected void initPhysics() {
	    PhysicsManager physicsManager = getPhysicsManager();

		physicsManager.addCollisionHandler(new PlayerPowerupHandler(this));
		physicsManager.addCollisionHandler(new PlayerEnemyHandler(this));
		physicsManager.addCollisionHandler(new ProjectileEnemyHandler(this));
		physicsManager.addCollisionHandler(new PlayerBossHandler(this));
		physicsManager.addCollisionHandler(new ProjectileBossHandler(this));
		plBlHandler = new PlayerBlockHandler(this, (String scName) -> {
			gScene.changeScene(assets.getText(scName));
			gScene.playScene();
		});
		physicsManager.addCollisionHandler(plBlHandler);
		physicsManager.addCollisionHandler(new ProjectilePlayerHandler(this));
		

		physicsManager.addCollisionHandler(new CollisionHandler(Type.PLAYER, Type.NEXT_LEVEL_POINT) {
			@Override
			public void onCollisionBegin(Entity a, Entity b) {
				if(b.getProperty("autoDoor") != null && b.<Boolean>getProperty("autoDoor")){
					playerData.setCurrentLevel(playerData.getCurrentLevel() + 1);
					changeLevel();
				}
				else{
					a.setProperty("inDoor", true);
				}
			}
			@Override
            public void onCollisionEnd(Entity a, Entity b) {
				a.setProperty("inDoor", false);
			}
		});
		
		physicsManager.addCollisionHandler(new CollisionHandler(Type.BLOCK, Type.ENEMY) {
			@Override
			public void onCollision(Entity block, Entity enemy) {
				if (block.getProperty(Property.SUB_TYPE) == Block.LADDER) {
					if(block.<Boolean>getProperty("top")){
						if(enemy.getControl(PhysicsControl.class) != null){
							double movingDown = enemy.getControl(PhysicsControl.class).getVelocity().getY();
							if (movingDown > 0) {
								enemy.setTranslateY(block.getTranslateY() - (enemy.getHeight())); 
								enemy.getControl(PhysicsControl.class).moveY(0);
								enemy.setProperty("jumping", false);
							} 
						}
					}				
				}	
			}
		});
		
		//printMemoryUsage("Physics Init");
	}

	//private Text debug = new Text();

	@Override
	protected void initUI() {
		gScene = new GameScene(assets.getText("dialogue/scene_0.txt"), this);
		gScene.setTranslateX(140);
		gScene.setTranslateY(250);
		gScene.setScaleX(1.4);
		gScene.setScaleY(1.4);
		gScene.playScene();

		hud = new PlayerHUD(player.<Player> getProperty(Property.DATA).getMaxHealth(),
				player.<Player> getProperty(Property.DATA).getMaxMana());

		hud.setTranslateX(10);
		hud.setTranslateY(100);

		performance.setTranslateX(450);
		performance.setTranslateY(50);
		performance.setFill(Color.BLACK);

		///debug.setTranslateX(450);
		//debug.setTranslateY(100);
		fadeScreen = new Rectangle(this.getSettings().getWidth(), this.getSettings().getHeight());
		fadeScreen.setFill(Color.BLACK);
		fadeIn = new FadeTransition(Duration.seconds(1), fadeScreen);
        fadeIn.setFromValue(1);
        fadeIn.setToValue(0);
        fadeIn.setOnFinished((e) -> {
        	if(fInComMet != null){
        		fInComMet.run();
        		fInComMet = null;
        	}
        });
		fadeOut = new FadeTransition(Duration.seconds(1), fadeScreen);
        fadeOut.setFromValue(0);
        fadeOut.setToValue(1);
        fadeOut.setOnFinished((e) -> {
        	if(fOutComMet != null){
        		fOutComMet.run();
        		fOutComMet = null;
        	}
        });
		firstPlay = false;
		Vignette vignette = new Vignette(640, 360, 720);
		getSceneManager().addUINodes(vignette, gScene, hud, performance, levelMenu, fadeScreen);
		levelMenu.setVisible(false);
        hud.CurHealthProperty().bind(playerData.CurrentHealthProperty());
        hud.CurManaProperty().bind(playerData.CurrentManaProperty());
        hud.MaxHealthProperty().bind(playerData.MaxHealthProperty());
        hud.MaxManaProperty().bind(playerData.MaxManaProperty());

        consGameMenu.updatePowerMenu(playerData);
        soundManager = new SoundManager(this);
        soundManager.stopAll();
        soundManager.setBackgroundMusic(FileNames.FOREST1_MUSIC);
        soundManager.getBackgroundMusic().setCycleCount(Integer.MAX_VALUE);
        soundManager.playBackgroundMusic();

        indiLoop = new IndependentLoop(this);
        //indiLoop.start();
		//printMemoryUsage("UI Init");
	}

	private void initLevels() {
		List<LevelData> levelData = IntStream.range(0, Config.MAX_LEVELS)
				.mapToObj(i -> new LevelData(assets.getText("levels/level_" + i + ".txt")))
				.collect(Collectors.toList());

		parser = new LevelParser(this, levelData);
		/*levels = new ArrayList<Level>();
		levels.add(parser.parse(0));*/
		//parser.parseAll();
	}

	@Override
	protected void initInput() {
		consController = new ConsumeController(this);
		consController.initControls();
		/*
		// TODO Remove manual spawn
				getInputManager().addAction(new UserAction("Spawn Bandit Knifer"){
					@Override
					protected void onActionBegin() {
						Pair<Entity, Entity> pEn = eSpawner.spawnBKnifer(spawnPoint.add(500, -100));
						getSceneManager().addEntities(pEn.getKey(), pEn.getValue());
					}
				}, KeyCode.DIGIT1);
				getInputManager().addAction(new UserAction("Spawn Bandit Magician"){
					@Override
					protected void onActionBegin() {
						Pair<Entity, Entity> pEn = eSpawner.spawnMagician(spawnPoint.add(500, -100));
						getSceneManager().addEntities(pEn.getKey(), pEn.getValue());
					}
				}, KeyCode.DIGIT2);
				getInputManager().addAction(new UserAction("Spawn Bandit Spear Thrower"){
					@Override
					protected void onActionBegin() {
						Pair<Entity, Entity> pEn = eSpawner.spawnBSpearEnemy(spawnPoint.add(500, -100));
						getSceneManager().addEntities(pEn.getKey(), pEn.getValue());
					}
				}, KeyCode.DIGIT3);
				getInputManager().addAction(new UserAction("Spawn Rifler"){
					@Override
					protected void onActionBegin() {
						Pair<Entity, Entity> pEn = eSpawner.spawnRifler(spawnPoint.add(500, -100), false);
						getSceneManager().addEntities(pEn.getKey(), pEn.getValue());
					}
				}, KeyCode.DIGIT4);
				getInputManager().addAction(new UserAction("Spawn Bayoneter"){
					@Override
					protected void onActionBegin() {
						Pair<Entity, Entity> pEn = eSpawner.spawnBayoneter(spawnPoint.add(500, -100));
						getSceneManager().addEntities(pEn.getKey(), pEn.getValue());
					}
				}, KeyCode.DIGIT5);
				getInputManager().addAction(new UserAction("Spawn Cannon"){
					@Override
					protected void onActionBegin() {
						Pair<Entity, Entity> pEn = eSpawner.spawnCannon(spawnPoint.add(500, -100));
						getSceneManager().addEntities(pEn.getKey(), pEn.getValue());
					}
				}, KeyCode.DIGIT6);
				getInputManager().addAction(new UserAction("Spawn Dancer"){
					@Override
					protected void onActionBegin() {
						Pair<Entity, Entity> pEn = eSpawner.spawnDancer(spawnPoint.add(500, -100));
						getSceneManager().addEntities(pEn.getKey(), pEn.getValue());
					}
				}, KeyCode.DIGIT7);
				getInputManager().addAction(new UserAction("Spawn Dog"){
					@Override
					protected void onActionBegin() {
						Pair<Entity, Entity> pEn = eSpawner.spawnDog(spawnPoint.add(500, -100));
						getSceneManager().addEntities(pEn.getKey(), pEn.getValue());
					}
				}, KeyCode.DIGIT8);
				getInputManager().addAction(new UserAction("Spawn Eloko") {
					@Override
					protected void onActionBegin() {
						Pair<Entity, Entity> pEn = eSpawner.spawnEloko(spawnPoint.add(500, -100));
						getSceneManager().addEntities(pEn.getKey(), pEn.getValue());
					}
				}, KeyCode.DIGIT9);
				getInputManager().addAction(new UserAction("Spawn Ice Spirit"){
					@Override
					protected void onActionBegin() {
						Pair<Entity, Entity> pEn = eSpawner.spawnIceSpirit(spawnPoint.add(500, -100));
						getSceneManager().addEntities(pEn.getKey(), pEn.getValue());
					}
				}, KeyCode.DIGIT0);
				getInputManager().addAction(new UserAction("Spawn Locust") {
					@Override
					protected void onActionBegin() {
						Pair<Entity, Entity> pEn = eSpawner.spawnLocust(spawnPoint.add(500, -100));
						getSceneManager().addEntities(pEn.getKey(), pEn.getValue());
					}
				}, KeyCode.NUMPAD1);
				getInputManager().addAction(new UserAction("Spawn Mummy"){
					@Override
					protected void onActionBegin() {
						Pair<Entity, Entity> pEn = eSpawner.spawnMummy(spawnPoint.add(500, -100));
						getSceneManager().addEntities(pEn.getKey(), pEn.getValue());
					}
				}, KeyCode.NUMPAD2);
				getInputManager().addAction(new UserAction("Spawn Musician"){
					@Override
					protected void onActionBegin() {
						Pair<Entity, Entity> pEn = eSpawner.spawnMusician(spawnPoint.add(500, -100));
						getSceneManager().addEntities(pEn.getKey(), pEn.getValue());
					}
				}, KeyCode.NUMPAD3);
				getInputManager().addAction(new UserAction("Spawn Scarab") {
					@Override
					protected void onActionBegin() {
						Pair<Entity, Entity> pEn = eSpawner.spawnScarab(spawnPoint.add(500, -100));
						getSceneManager().addEntities(pEn.getKey(), pEn.getValue());
					}
				}, KeyCode.NUMPAD4);
				getInputManager().addAction(new UserAction("Spawn Scorpion"){
					@Override
					protected void onActionBegin() {
						Pair<Entity, Entity> pEn = eSpawner.spawnScorpion(spawnPoint.add(500, -100));
						getSceneManager().addEntities(pEn.getKey(), pEn.getValue());
					}
				}, KeyCode.NUMPAD5);
				getInputManager().addAction(new UserAction("Spawn Stone Spirit"){
					@Override
					protected void onActionBegin() {
						Pair<Entity, Entity> pEn = eSpawner.spawnStoneEnemy(spawnPoint.add(500, -100));
						getSceneManager().addEntities(pEn.getKey(), pEn.getValue());
					}
				}, KeyCode.NUMPAD6);
				getInputManager().addAction(new UserAction("Spawn Wall Burner"){
					@Override
					protected void onActionBegin() {
						//Note that this needs to be spawned in LevelParser
						Pair<Entity, Entity> pEn = eSpawner.spawnBurner(spawnPoint.add(500, -100));
						getSceneManager().addEntities(pEn.getKey(), pEn.getValue());
					}
				}, KeyCode.NUMPAD7);
				getInputManager().addAction(new UserAction("Spawn Zulu Knifer"){
					@Override
					protected void onActionBegin() {
						Pair<Entity, Entity> pEn = eSpawner.spawnZKnifer(spawnPoint.add(500, -100));
						getSceneManager().addEntities(pEn.getKey(), pEn.getValue());
					}
				}, KeyCode.NUMPAD8);
				getInputManager().addAction(new UserAction("Spawn Zulu Charger") {
					@Override
					protected void onActionBegin() {
						Pair<Entity, Entity> pEn = eSpawner.spawnCharger(spawnPoint.add(500, -100));
						getSceneManager().addEntities(pEn.getKey(), pEn.getValue());
					}
				}, KeyCode.NUMPAD9);
				getInputManager().addAction(new UserAction("Spawn Zulu Spear Thrower"){
					@Override
					protected void onActionBegin() {
						Pair<Entity, Entity> pEn = eSpawner.spawnZSpearEnemy(spawnPoint.add(500, -100), false);
						getSceneManager().addEntities(pEn.getKey(), pEn.getValue());
					}
				}, KeyCode.NUMPAD0);
				getInputManager().addAction(new UserAction("Spawn Anubis Boss") {
					@Override
					protected void onActionBegin() {
						Pair<Entity, Entity> pEn = eSpawner.spawnAnubisBoss(spawnPoint.add(500, -100));
						getSceneManager().addEntities(pEn.getKey(), pEn.getValue());
					}
				}, KeyCode.F1);
				getInputManager().addAction(new UserAction("Spawn Eshu Boss") {
					@Override
					protected void onActionBegin() {
						Pair<Entity, Entity> pEn = eSpawner.spawnEshuBoss(spawnPoint.add(500, -100));
						getSceneManager().addEntities(pEn.getKey(), pEn.getValue());
					}
				}, KeyCode.F2);
				getInputManager().addAction(new UserAction("Spawn Gentleman Boss") {
					@Override
					protected void onActionBegin() {
						Pair<Entity, Entity> pEn = eSpawner.spawnGentlemanBoss(spawnPoint.add(500, -100));
						getSceneManager().addEntities(pEn.getKey(), pEn.getValue());
					}
				}, KeyCode.F3);
				getInputManager().addAction(new UserAction("Spawn Kibo Boss") {
					@Override
					protected void onActionBegin() {
						Pair<Entity, Entity> pEn = eSpawner.spawnKiboBoss(spawnPoint.add(500, -100));
						getSceneManager().addEntities(pEn.getKey(), pEn.getValue());
					}
				}, KeyCode.F4);
				getInputManager().addAction(new UserAction("Spawn Sand Elephant Boss") {
					@Override
					protected void onActionBegin() {
						Pair<Entity, Entity> pEn = eSpawner.spawnSandBoss(spawnPoint.add(500, -60));// Config.BLOCK_SIZE/2 -40));
						getSceneManager().addEntities(pEn.getKey(), pEn.getValue());
					}
				}, KeyCode.F5);
				getInputManager().addAction(new UserAction("Spawn Shaka Boss") {
					@Override
					protected void onActionBegin() {
						Pair<Entity, Entity> pEn = eSpawner.spawnShakaBoss(spawnPoint.add(500, -100));
						getSceneManager().addEntities(pEn.getKey(), pEn.getValue());
					}
				}, KeyCode.F6);
				getInputManager().addAction(new UserAction("Spawn Shango Boss") {
					@Override
					protected void onActionBegin() {
						Pair<Entity, Entity> pEn = eSpawner.spawnShangoBoss(spawnPoint.add(500, -100));
						getSceneManager().addEntities(pEn.getKey(), pEn.getValue());
					}
				}, KeyCode.F7);
				
				getInputManager().addAction(new UserAction("Level screen") {
					@Override
					protected void onActionBegin() {
					    showLevelScreen();
					}
				}, KeyCode.L);
				
				getInputManager().addAction(new UserAction("Next Level") {
					@Override
					protected void onActionBegin() {
						playerData.setCurrentLevel(playerData.getCurrentLevel() + 1);
						changeLevel();
					}
				}, KeyCode.P);
				
				getInputManager().addAction(new UserAction("Choose Level") {
					@Override
					protected void onActionBegin() {
						UIFactory.getDialogBox().showInputBox("Choose Level", new Consumer<String>(){
							@Override
							public void accept(String arg0) {
								playerData.setCurrentLevel(Integer.parseInt(arg0));
								changeLevel();
							}							
						});
						
					}
				}, KeyCode.SEMICOLON);
				
				
				getInputManager().addAction(new UserAction("Entity number") {
					@Override
					protected void onActionBegin() {
						System.out.println("Entity #: " + getSceneManager().getEntities().size());
						System.out.println("Platforms #: " + getSceneManager().getEntities(Types.Type.PLATFORM).size());
						//System.out.println("Blocks #: " + getSceneManager().getEntities(Types.Type.).size());
						
					}
				}, KeyCode.O);
				
				getInputManager().addAction(new UserAction("Memory test"){
					@Override
					protected void onActionBegin() {
						printMemoryUsage("Manual memory test");
					}
				}, KeyCode.M);
				
				getInputManager().addAction(new UserAction("Ladder test"){
					@Override
					protected void onActionBegin() {
						System.out.println("Ladder Size: " + plBlHandler.laddersOn.size() );
						System.out.println("Climb: " + player.getProperty("climb"));
						System.out.println("Climbing: " + player.getProperty("climbing"));
					}
				}, KeyCode.I);*/
	}

	@Override
    protected FXGLMenuFactory initMenuFactory() {
	    return new FXGLMenuFactory() {
            @Override
            public FXGLMenu newMainMenu(GameApplication app) {
                consMainMenu = new ConsumeMainMenu(ConsumeApp.this);
                return consMainMenu;
            }

            @Override
            public FXGLMenu newGameMenu(GameApplication app) {
                levelMenu = new LevelMenu(ConsumeApp.this);
                consGameMenu = new ConsumeGameMenu(ConsumeApp.this);
               return consGameMenu;
            }
	    };
	}
	
	@Override
	protected void onUpdate() {
		if(!firstPlay){
			firstPlay = true;
			getTimerManager().runOnceAfter(() -> {
				fadeIn.play();
			}, Duration.seconds(0.5));
		}
		
		/*if(plBlHandler.laddersOn.isEmpty()){
			player.setProperty("climbing", false);
			player.setProperty("climb", false);
			player.setProperty(Property.ENABLE_GRAVITY, true);
		}*/
		
		if(!player.isCollidable()){
			plBlHandler.laddersOn.clear();
		}
		
		if(playerData.getCurrentMana() == playerData.getMaxMana()){
			regenTime = getNow();
		}
		
		if (getNow() - regenTime >= TimerManager.toNanos(Config.REGEN_TIME_INTERVAL)) {
			playerData.regenMana();
			regenTime = getNow();
		}

		if(playerData.getCurrentHealth() <= 0){
			playerData.setCurrentHealth(0);
			player.fireFXGLEvent(new FXGLEvent(Event.PLAYER_DEATH));
		}

		/*for (Entity e : getSceneManager().getEntities(Type.BLOCK)) {
			if (e.getProperty(Property.SUB_TYPE) == Block.BARRIER && "idle".equals(e.getProperty("state"))
					&& !"none".equals(e.getProperty("start"))) {

				if (player.getTranslateX() <= e.getTranslateX()) {
					// check if came from left
					if (!"left".equals(e.getProperty("start"))) {
						activateBarrier(e);
						player.setTranslateX(e.getTranslateX() - 40);
					} else {
						e.setProperty("start", "none");
					}
				} else {
					if (!"right".equals(e.getProperty("start"))) {
						activateBarrier(e);
						player.setTranslateX(e.getTranslateX() + 40);
					} else {
						e.setProperty("start", "none");
					}
				}
			}

			if (e.getProperty(Property.SUB_TYPE) == Block.BARRIER)
				e.setProperty("state", "idle");
		}*/

		//debug.setText("Debug text goes here");
	}

	@Override
	public void onExit(){
		if(indiLoop != null){
			indiLoop.stop();
		}
		if(soundManager != null){
			soundManager.stopAll();
		}
		try {
			SaveLoadManager.INSTANCE.save(sSettings, FileNames.SETTINGS);
		} catch (Exception e) {
			System.out.println("Unable to save settings");
			e.printStackTrace();
		}
	}

	@Override
	public void onMenuOpen(){
		soundManager.pauseBackgroundMusic();
		backVol.set(sSettings.getBackMusicVolume());
        sfxVol.set(sSettings.getSFXVolume());
		if(playerData.getCurrentLevel() < 3){
			consGameMenu.levelMenuItem.setVisible(false);
		}
		else{
			consGameMenu.levelMenuItem.setVisible(true);
		}
	}

	@Override
	public void onMenuClose(){
		if(soundManager != null){
			soundManager.playBackgroundMusic();
		}
	}

	private void loadLevel(int lev) {
		printMemoryUsage("Load Level Begin");
	    getSceneManager().getEntities().forEach(getSceneManager()::removeEntity);
		printMemoryUsage("Load Level After remove");
		Level level = parser.parse(lev);
		printMemoryUsage("Load Level After parse");
		spawnPoint = level.getSpawnPoint();
		limits = new Pair<Point2D, Point2D>(level.getUpperLeftLimit(), level.getLowerRightLimit());

		// add level objects
		for (Entity e : level.getEntitiesAsArray()) {
			// TODO: currently we don't have death animations/handlers for other
			// types
			// when we will, this might move to another class
			if (e.isType(Type.POWERUP)) {
				e.addFXGLEventHandler(Event.DEATH, event -> getSceneManager().removeEntity(e));
			}
			if(playerData.getUpgrades().contains(lev) && (e.getProperty(Property.SUB_TYPE) == Powerup.INC_MANA_REGEN
														|| e.getProperty(Property.SUB_TYPE) == Powerup.INC_MAX_MANA
														|| e.getProperty(Property.SUB_TYPE) == Powerup.INC_MAX_HEALTH)){

			}
			else{
			    getSceneManager().addEntities(e);
			}

		}
		
		printMemoryUsage("Load Level After add");

		if(gScene != null){
			gScene.endScene();
		}

		printMemoryUsage("Load Level Before Player");
		// add player
		initPlayer(spawnPoint);
		playerDied = false;
		
		printMemoryUsage("Load Level End");
		
		level.clean();
	}

	public void changeLevel() {
		getInputManager().setProcessActions(false);
		player.setCollidable(false);
		fOutComMet = () -> {
			if(playerData.getCurrentHealth() <= 0){
				playerData.setCurrentHealth(playerData.getMaxHealth());
				playerData.setCurrentMana(playerData.getMaxMana());
			}
			getSceneManager().getEntities().forEach(getSceneManager()::removeEntity);
			
			hud.setBossHealthBarVisible(false);
			//levels.set(playerData.getCurrentLevel(), parser.parse(playerData.getCurrentLevel()));
			
			loadLevel(playerData.getCurrentLevel());
			plBlHandler.laddersOn.clear();
			
			levelMenu.setVisible(false);
			
			getTimerManager().runOnceAfter(() -> {
				getInputManager().setProcessActions(true);
				player.setCollidable(true);
				fadeIn.play();
			}, Duration.seconds(0.5));
		};
		fadeScreen.setVisible(true);
		fadeOut.play();
		
	}

	public void showLevelScreen(){
	    getInputManager().setProcessActions(false);
		fOutComMet = () -> {
			consGameMenu.updatePowerMenu(playerData);
			getInputManager().setProcessActions(true);
			//this.getInputManager().
			//this.getSceneManager().closeGameMenu();
			this.soundManager.stopAll();
			if(playerData.getLevsComp().size() > 5){
				levelMenu.setFinalLevelVisible(true);
			}
			else{
				levelMenu.setFinalLevelVisible(false);
			}
			levelMenu.setVisible(true);
			getSceneManager().getEntities().forEach(getSceneManager()::removeEntity);
			
			getTimerManager().runOnceAfter(() -> fadeIn.play(), Duration.seconds(0.5));
		};
		fInComMet = () -> {
			this.soundManager.setBackgroundMusic(FileNames.THEME_MUSIC);
			this.soundManager.playBackgroundMusic();
			fadeScreen.setVisible(false);
		};
		fadeOut.play();
	}

	@Override
	public Serializable saveState(){
		return new GameSave(playerData);
	}

	@Override
	public void loadState(Serializable d){
		if(d.getClass() == GameSave.class){

			soundManager.stopAll();
			GameSave g = (GameSave) d;
			playerData.setElement(g.getCurElement());
			playerData.setCurrentHealth(g.getCurHealth());
			playerData.setCurrentLevel(g.getCurLevel());
			playerData.setCurrentMana(g.getCurMana());
			playerData.setManaRegenRate(g.getManaReg());
			playerData.setMaxHealth(g.getMaxHealth());
			playerData.setMaxMana(g.getMaxMana());
			playerData.setName(g.getName());
			playerData.getPowers().clear();
			playerData.getPowers().addAll(g.getPowers());
			playerData.getResistances().clear();
			playerData.getResistances().addAll(g.getResists());
			playerData.setSpritesheet(g.getSSheet());
			playerData.getWeaknesses().clear();
			playerData.getWeaknesses().addAll(g.getWeaks());
			playerData.getUpgrades().clear();
			playerData.getUpgrades().addAll(g.getUpgrades());
			playerData.getLevsComp().clear();
			playerData.getLevsComp().addAll(g.getLevsComp());
			consGameMenu.updatePowerMenu(playerData);
			soundManager.setBackgroundMusic(getBackgroundMusic(playerData.getCurrentLevel()));
			soundManager.getBackgroundMusic().setCycleCount(Integer.MAX_VALUE);

			changeLevel();
		}
		else{
			System.out.println(d.getClass());
		}
	}

	public String getBackgroundMusic(int level){
		switch(level/3){
			case 0:{
				return FileNames.FOREST1_MUSIC;
			}
			case 1:{
				return FileNames.DESERT_MUSIC;
			}
			case 2:{
				return FileNames.PYRAMID_MUSIC;
			}
			case 3:{
				return FileNames.FESTIVAL_MUSIC;
			}
			case 4:{
				return FileNames.FOREST2_MUSIC;
			}
			case 5:{
				return FileNames.MOUNTAIN_MUSIC;
			}
			case 6:{
				return FileNames.COLONY_MUSIC;
			}
			case 7:{
				return FileNames.EMPIRE_MUSIC;
			}
			default:{
				return FileNames.FESTIVAL_MUSIC;
			}
		}
	}

	//Entity powerStatus;

	private void initPlayer(Point2D point) {
		printMemoryUsage("Init player begin");
		player = new Entity(Type.PLAYER);
		
		player.setPosition(point.getX(), point.getY()).setCollidable(true)
				.setProperty(Property.DATA, playerData).setProperty("climb", false).setProperty("climbing", false)
				.setProperty("facingRight", true).setProperty("stunned", false).setProperty("eating", false).setProperty("eaten",  false)
				.setProperty("scenePlaying", false).setProperty("attacking", false).setProperty("consumed", false)
				.addControl(new PhysicsControl(physics));
		
		Rectangle rG = new Rectangle(0, 0, 16, 30);
		rG.setFill(Color.RED);
		player.setGraphics(rG);
		player.setVisible(false);

		player.addFXGLEventHandler(Event.PLAYER_DEATH, this::playerDied);

		printMemoryUsage("Init player 1");
		
		camera = Entity.noType();
		double camX = player.getPosition().getX();
		double camY = player.getPosition().getY();
		if(camX < limits.getKey().getX() + 320){
			camX = limits.getKey().getX() + 320;
		}
		else if(camX > limits.getValue().getX() - 320){
			camX = limits.getValue().getX() - 320;
		}
		if(camY < limits.getKey().getY() + 180){
			camY = limits.getKey().getY() + 180;
		}
		else if(camY > limits.getValue().getY() - 180){
			camY = limits.getValue().getY() - 180;
		}
		camera.setPosition(camX, camY);
		player.translateXProperty().addListener(new ChangeListener<Number>(){
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				if(!camera.translateXProperty().isBound() && 
					(arg2.intValue() >= limits.getKey().getX() + 320 && 
					arg2.intValue() <= limits.getValue().getX() - 320)){
					camera.translateXProperty().bind(player.translateXProperty());
				}
				else if(camera.translateXProperty().isBound() && 
						(arg2.intValue() < limits.getKey().getX() + 320 || 
						arg2.intValue() > limits.getValue().getX() - 320)){
					camera.translateXProperty().unbind();
					if(arg2.intValue() < limits.getKey().getX() + 320){
						camera.setTranslateX(limits.getKey().getX() + 320);
					}
					else{
						camera.setTranslateX(limits.getValue().getX() - 320);
					}
					
				}
			}
		});
		player.translateYProperty().addListener(new ChangeListener<Number>(){
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				if(!camera.translateYProperty().isBound() && 
					(arg2.intValue() >= limits.getKey().getY() + 180 && 
					arg2.intValue() <= limits.getValue().getY() - 180)){
					camera.translateYProperty().bind(player.translateYProperty());
				}
				else if(camera.translateYProperty().isBound() && 
						(arg2.intValue() < limits.getKey().getY() + 180 || 
						arg2.intValue() > limits.getValue().getY() - 180)){
					camera.translateYProperty().unbind();
					if(arg2.intValue() < limits.getKey().getY() + 180){
						camera.setTranslateY(limits.getKey().getY() + 180);
					}
					else{
						camera.setTranslateY(limits.getValue().getY() - 180);
					}
					
				}
			}
		});
		camera.setVisible(false);
		
		getSceneManager().bindViewportOrigin(camera, 320, 180);
		
		printMemoryUsage("Init player 2");
		
		Entity pPicBox = new Entity(Type.PLAYER_PIC_BOX);
		pPicBox.setCollidable(false);
		if(Config.RELEASE){
			if(playAni == null){
				printMemoryUsage("Before Animated player control");
				playAni = new AnimatedPlayerControl(player, playerData, getPlayerImages(),
						assets.getTexture(FileNames.PLAYER_DIR));
				printMemoryUsage("After Animated player control");
			}
			else{
				playAni.setPlayer(player);
			}
			pPicBox.addControl(playAni);
		}
		else{
			Rectangle r = new Rectangle(40, 20);
			r.setFill(Color.GREEN);
			pPicBox.setGraphics(r);
		}
		
		pPicBox.translateXProperty().bind(player.translateXProperty().add(pPicBox.getTranslateX()));
		pPicBox.translateYProperty().bind(player.translateYProperty().add(pPicBox.getTranslateY()));

		printMemoryUsage("Init player 3");
		/*powerStatus = Entity.noType();
		powerStatus.translateXProperty().bind(player.translateXProperty());
		powerStatus.translateYProperty().bind(player.translateYProperty().subtract(40));
		Text tTex = new Text();
		tTex.textProperty().bind(playerData.ElementProperty().asString());
		powerStatus.setGraphics(tTex);

		getSceneManager().addEntities(powerStatus);*/

		getSceneManager().addEntities(player, pPicBox, camera);
		printMemoryUsage("Init player end");
	}

	private HashMap<Element, Integer> getPlayerImages() {
		HashMap<Element, Integer> pTex = new HashMap<Element, Integer>();
		Element[] eles = Types.Element.values();
		for(int i = 0; i < eles.length; i++){
			pTex.put(eles[i], 1500*i);
		}
		
		return pTex;
	}

	private void playerDied(FXGLEvent e){
		if (!playerDied){
			player.setProperty("stunned", true);
			player.setProperty("climb", false);
			player.setProperty("climbing", false);
			player.setProperty(Property.ENABLE_GRAVITY, true);
			playerDied = true;
			getTimerManager().runOnceAfter(this::changeLevel, Duration.seconds(0.5));
		}
	}
	
	public void resetWorld(){
		playAni = null;
		getSceneManager().getEntities().forEach(getSceneManager()::removeEntity);
	}

	public void activateBarrier(Entity block) {
		block.setProperty("state", "dying");
		ArrayList<Entity> barriers = new ArrayList<Entity>(collectBarriersVLine(block));
		barriers.remove(block);
		if((char)block.getProperty("blockImg") == 'a'){ 
			barriers.sort(new Comparator<Entity>(){
				@Override
				public int compare(Entity arg0, Entity arg1) {
					if(arg0.getTranslateY() < arg1.getTranslateY()){
						return 1;
					}
					else if(arg0.getTranslateY() == arg1.getTranslateY()){
						return 0;
					}
					else{
						return -1;
					}
			}});
		}
		else{
			barriers.sort(new Comparator<Entity>(){
				@Override
				public int compare(Entity arg0, Entity arg1) {
					if(arg0.getTranslateY() < arg1.getTranslateY()){
						return -1;
					}
					else if(arg0.getTranslateY() == arg1.getTranslateY()){
						return 0;
					}
					else{
						return 1;
					}
			}});
		}
		
		if((char)block.getProperty("blockImg") == 'a'){
			soundManager.playSFX(FileNames.FIRE_TRAP);
		}
		
		for(int i = 0; i < barriers.size(); i++){
			Entity e = barriers.get(i);
			getSceneManager().removeEntity(e);
			Entity e2 = new Entity(Type.PLATFORM);
			e2.setPosition(e.getTranslateX(), e.getTranslateY());
			e2.setVisible(false);
			
			switch((char)e.getProperty("blockImg")){
				case 'a':{
					Texture t = getTexture(FileNames.FIREBALL_PROJ);
					t.setScaleY(-1);
					t.setPreserveRatio(true);
					t.setFitWidth(Config.BLOCK_SIZE);
					t.setTranslateY(-Config.BLOCK_SIZE/2);
					e2.setGraphics(t);
					break;
				}
				case 'A':{
					Texture t = getTexture(FileNames.U_STONE_BLOCK);
					t.setScaleY(-1);
					t.setPreserveRatio(true);
					t.setFitWidth(Config.BLOCK_SIZE);
					//t.setTranslateY(-Config.BLOCK_SIZE/2);
					e2.setGraphics(t);
					break;
				}
				case 'b':{
					Texture t = getTexture(FileNames.DES_CRATE_BLOCK);
					t.setScaleY(-1);
					t.setPreserveRatio(true);
					t.setFitWidth(Config.BLOCK_SIZE);
					e2.setGraphics(t);
					break;
				}
				case 'g':{
					Texture t = getTexture(FileNames.U_N_SAND_BLOCK);
					t.setScaleY(-1);
					t.setPreserveRatio(true);
					t.setFitWidth(Config.BLOCK_SIZE);
					e2.setGraphics(t);
					break;
				}
				case 'G':{
					Texture t = getTexture(FileNames.U_SANDSTONE_BLOCK);
					t.setScaleY(-1);
					t.setPreserveRatio(true);
					t.setFitWidth(Config.BLOCK_SIZE);
					e2.setGraphics(t);
					break;
				}
				default:{		//h, H symbols potentially usable
					Texture t = getTexture(FileNames.DES_CRATE_BLOCK);
					t.setScaleY(-1);
					t.setPreserveRatio(true);
					t.setFitWidth(Config.BLOCK_SIZE);
					e2.setGraphics(t);
					break;
				}
			}
			

			getSceneManager().addEntities(e2);
			this.getTimerManager().runOnceAfter(new Runnable(){
				@Override
				public void run() {
					e2.setVisible(true);
					
				}
			}, Duration.seconds(0.5).multiply(i));
			
		}
		
	}
	
	public Texture getTexture(String address) {
		return assets.getTexture(address);
	}
	
	public Assets getZoneAssets(int levelNumber) throws Exception {
	    HashMap<Asset_Types, List<String>> toCache = new HashMap<>();
	    toCache.put(Asset_Types.BINARY, new ArrayList<String>());
	    toCache.put(Asset_Types.FONTS, new ArrayList<String>());
	    toCache.put(Asset_Types.MUSIC, new ArrayList<String>());
	    toCache.put(Asset_Types.SOUNDS, this.getAssetManager().loadFileNames(AssetManager.SOUNDS_DIR));
	    toCache.put(Asset_Types.TEXT, this.getAssetManager().loadFileNames(AssetManager.TEXT_DIR));
	    toCache.put(Asset_Types.TEXTURES, new ArrayList<String>());
	    
	    toCache.get(Asset_Types.MUSIC).add(FileNames.THEME_MUSIC);
	    toCache.get(Asset_Types.MUSIC).add(this.getBackgroundMusic(levelNumber));
	    
	    toCache.get(Asset_Types.TEXTURES).add(FileNames.EMPTY);
	    toCache.get(Asset_Types.TEXTURES).add(FileNames.U_DIRT_BLOCK);
	    toCache.get(Asset_Types.TEXTURES).add(FileNames.S_DIRT_BLOCK);
	    toCache.get(Asset_Types.TEXTURES).add(FileNames.PLAYER_DIR);
	    toCache.get(Asset_Types.TEXTURES).add(FileNames.PLAYER_ICON);
	    toCache.get(Asset_Types.TEXTURES).add(FileNames.LADDER_BLOCK);
	    toCache.get(Asset_Types.TEXTURES).add(FileNames.FOOD_BLOCK); 
	    toCache.get(Asset_Types.TEXTURES).add(FileNames.POWERUP_BLOCK);   
	    toCache.get(Asset_Types.TEXTURES).add(FileNames.DEATH_BLOOD);
	    toCache.get(Asset_Types.TEXTURES).add(FileNames.DEATH_COBBLE);
	    
	    toCache.get(Asset_Types.TEXTURES).add(FileNames.SPEAR_PROJECTILE);
	    toCache.get(Asset_Types.TEXTURES).add(FileNames.ANKH_PROJECTILE);
	    toCache.get(Asset_Types.TEXTURES).add(FileNames.COAL_PROJECTILE);
	    toCache.get(Asset_Types.TEXTURES).add(FileNames.SAND_PROJECTILE);
	    toCache.get(Asset_Types.TEXTURES).add(FileNames.FIREBALL_PROJ);
	    toCache.get(Asset_Types.TEXTURES).add(FileNames.STONE_PROJ);

	    switch(levelNumber){
            case 0:
            case 1:
            case 2: {
                toCache.get(Asset_Types.TEXTURES).add(FileNames.FOREST1_BACK_1);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.FOREST1_BACK_3);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.EMPIRE_BACK_2);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.U_DIRT_LINE);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.G_DIRT_BLOCK);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.CORPSE_BLOCK);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.ESHU_ICON);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.MOTHER_STAND);
                String[] enemies = {FileNames.ELOKO_DIR, FileNames.BANDIT_K_DIR, FileNames.BANDIT_S_DIR, FileNames.ESHU_DIR};
                for(String en : enemies){
                    for(String fi : this.getAssetManager().loadFileNames(AssetManager.TEXTURES_DIR + en)){
                        if(en.endsWith("/") || fi.startsWith("/")){
                            toCache.get(Asset_Types.TEXTURES).add(en + fi);
                        }
                        else{
                            toCache.get(Asset_Types.TEXTURES).add(en + "/" + fi);
                        }
                    }
                }
                break;
            }
            case 3:
            case 4:
            case 5: {
                toCache.get(Asset_Types.TEXTURES).add(FileNames.DESERT_BACK_1);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.U_N_SAND_BLOCK);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.U_N_SAND_LINE);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.S_N_SAND_BLOCK);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.G_N_SAND_BLOCK);
                String[] enemies = {FileNames.SCARAB_DIR, FileNames.LOCUST_DIR, FileNames.SCORPION_DIR, FileNames.GOLEM_DIR, FileNames.SAND_ELEPHANT_DIR};
                for(String en : enemies){
                    for(String fi : this.getAssetManager().loadFileNames(AssetManager.TEXTURES_DIR + en)){
                        if(en.endsWith("/") || fi.startsWith("/")){
                            toCache.get(Asset_Types.TEXTURES).add(en + fi);
                        }
                        else{
                            toCache.get(Asset_Types.TEXTURES).add(en + "/" + fi);
                        }
                    }
                }
                break;
            }
            case 6:
            case 7:
            case 8: {
                toCache.get(Asset_Types.TEXTURES).add(FileNames.PYRAMID_BACK_1);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.PYRAMID_BACK_2);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.COFFIN_BLOCK);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.ANUBIS_ICON);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.U_SAND_BLOCK);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.U_SAND_LINE);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.S_SAND_BLOCK);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.G_SAND_BLOCK);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.U_SANDSTONE_BLOCK);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.U_SANDSTONE_LINE);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.S_SANDSTONE_BLOCK);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.DES_SANDSTONE_BLOCK);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.S_DOOR);
                String[] enemies = {FileNames.SCARAB_DIR, FileNames.SCORPION_DIR, FileNames.ICE_SPIRIT_DIR, FileNames.MUMMY_DIR, FileNames.ANUBIS_DIR};
                for(String en : enemies){
                    for(String fi : this.getAssetManager().loadFileNames(AssetManager.TEXTURES_DIR + en)){
                        if(en.endsWith("/") || fi.startsWith("/")){
                            toCache.get(Asset_Types.TEXTURES).add(en + fi);
                        }
                        else{
                            toCache.get(Asset_Types.TEXTURES).add(en + "/" + fi);
                        }
                    }
                }
                break;
            }
            case 9:
            case 10:
            case 11: {
                toCache.get(Asset_Types.TEXTURES).add(FileNames.FESTIVAL_BACK_1);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.U_DIRT_LINE);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.G_DIRT_BLOCK);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.DES_THATCH_BLOCK);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.SHANGO_ICON);
                String[] enemies = {FileNames.MUSICIAN_DIR, FileNames.DANCERS_DIR, FileNames.DOG_DIR, FileNames.SHANGO_DIR};
                for(String en : enemies){
                    for(String fi : this.getAssetManager().loadFileNames(AssetManager.TEXTURES_DIR + en)){
                        if(en.endsWith("/") || fi.startsWith("/")){
                            toCache.get(Asset_Types.TEXTURES).add(en + fi);
                        }
                        else{
                            toCache.get(Asset_Types.TEXTURES).add(en + "/" + fi);
                        }
                    }
                }
                break;
            }
            case 12:
            case 13:
            case 14: {
                toCache.get(Asset_Types.TEXTURES).add(FileNames.FOREST1_BACK_2);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.FOREST1_BACK_3);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.EMPIRE_BACK_2);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.U_DIRT_LINE);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.G_DIRT_BLOCK);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.CORPSE_BLOCK);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.ESHU_ICON);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.MOTHER_DEAD);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.MOTHER_LYING);
                String[] enemies = {FileNames.ELOKO_DIR, FileNames.BANDIT_K_DIR, FileNames.BANDIT_S_DIR, FileNames.BANDIT_M_DIR, FileNames.ESHU_DIR};
                for(String en : enemies){
                    for(String fi : this.getAssetManager().loadFileNames(AssetManager.TEXTURES_DIR + en)){
                        if(en.endsWith("/") || fi.startsWith("/")){
                            toCache.get(Asset_Types.TEXTURES).add(en + fi);
                        }
                        else{
                            toCache.get(Asset_Types.TEXTURES).add(en + "/" + fi);
                        }
                    }
                }
                break;
            }
            case 15:
            case 16:
            case 17: {
                toCache.get(Asset_Types.TEXTURES).add(FileNames.MOUNTAIN_BACK_1);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.MOUNTAIN_BACK_2);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.U_DIRT_LINE);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.U_STONE_BLOCK);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.U_STONE_LINE);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.S_STONE_BLOCK);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.SN_STONE_BLOCK);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.DES_ICE_BLOCK);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.CAVE);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.KIBO_ICON);
                String[] enemies = {FileNames.ICE_SPIRIT_DIR, FileNames.GOLEM_DIR, FileNames.BANDIT_K_DIR, FileNames.KIBO_DIR};
                for(String en : enemies){
                    for(String fi : this.getAssetManager().loadFileNames(AssetManager.TEXTURES_DIR + en)){
                        if(en.endsWith("/") || fi.startsWith("/")){
                            toCache.get(Asset_Types.TEXTURES).add(en + fi);
                        }
                        else{
                            toCache.get(Asset_Types.TEXTURES).add(en + "/" + fi);
                        }
                    }
                }
                break;
            }
            case 18:
            case 19:
            case 20: {
                toCache.get(Asset_Types.TEXTURES).add(FileNames.COLONY_BACK_1);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.U_DIRT_LINE);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.G_DIRT_BLOCK);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.ST_DIRT_BLOCK);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.DES_CRATE_BLOCK);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.GENTLEMAN_ICON);
                String[] enemies = {FileNames.CANNON_DIR, FileNames.BRITISH_DIR, FileNames.GENTLEMAN_DIR};
                for(String en : enemies){
                    for(String fi : this.getAssetManager().loadFileNames(AssetManager.TEXTURES_DIR + en)){
                        if(en.endsWith("/") || fi.startsWith("/")){
                            toCache.get(Asset_Types.TEXTURES).add(en + fi);
                        }
                        else{
                            toCache.get(Asset_Types.TEXTURES).add(en + "/" + fi);
                        }
                    }
                }
                break;
            }
            case 21:
            case 22:
            case 23: {
                toCache.get(Asset_Types.TEXTURES).add(FileNames.EMPIRE_BACK_1);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.EMPIRE_BACK_2);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.G_DIRT_BLOCK);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.U_DIRT_LINE);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.DES_LIMESTONE_BLOCK);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.W_DOOR);
                toCache.get(Asset_Types.TEXTURES).add(FileNames.SHAKA_ICON);
                String[] enemies = {FileNames.ZULU_K_DIR, FileNames.ZULU_SH_DIR, FileNames.ZULU_SP_DIR, FileNames.SHAKA_DIR};
                for(String en : enemies){
                    for(String fi : this.getAssetManager().loadFileNames(AssetManager.TEXTURES_DIR + en)){
                        if(en.endsWith("/") || fi.startsWith("/")){
                            toCache.get(Asset_Types.TEXTURES).add(en + fi);
                        }
                        else{
                            toCache.get(Asset_Types.TEXTURES).add(en + "/" + fi);
                        }
                    }
                }
                break;
            }
	    }
	    
	    return this.getAssetManager().cacheGiven(toCache);
	}

	private ArrayList<Entity> collectBarriersVLine(Entity block){
		block.setProperty("state", "dying");
		ArrayList<Entity> barriers = new ArrayList<Entity>();
		barriers.add(block);
		
		for (Entity b : getSceneManager().getEntitiesInRange(
				new Rectangle2D(block.getTranslateX() + 10, block.getTranslateY() - 40, 20, 120), Type.BLOCK)) {
			if (b.getProperty(Property.SUB_TYPE) == Block.BARRIER && !"dying".equals(b.getProperty("state"))) {
				barriers.addAll(collectBarriersVLine(b));
			}
		}
		
		return barriers;
	}
	
	/*private ArrayList<Entity> collectBarriersHLine(Entity block){
		block.setProperty("state", "dying");
		ArrayList<Entity> barriers = new ArrayList<Entity>();
		barriers.add(block);
		
		for (Entity b : getSceneManager().getEntitiesInRange(
				new Rectangle2D(block.getTranslateX() - 40, block.getTranslateY() + 10, 120, 20), Type.BLOCK)) {
			if (b.getProperty(Property.SUB_TYPE) == Block.BARRIER && !"dying".equals(b.getProperty("state"))) {
				barriers.addAll(collectBarriersHLine(b));
			}
		}
		
		
		return barriers;
	}
	private ArrayList<Entity> collectBarriers(Entity block){
		block.setProperty("state", "dying");
		ArrayList<Entity> barriers = new ArrayList<Entity>();
		
		for (Entity b : getSceneManager().getEntitiesInRange(
				new Rectangle2D(block.getTranslateX() - 40, block.getTranslateY() - 40, 120, 120), Type.BLOCK)) {
			if (b.getProperty(Property.SUB_TYPE) == Block.BARRIER && !"dying".equals(b.getProperty("state"))) {
				barriers.add(b);
				barriers.addAll(collectBarriers(b));
			}
		}
		
		
		return barriers;
	}*/

	public void destroyBlock(Entity block) {
		block.setProperty("state", "dying");

		for (Entity b : getSceneManager().getEntitiesInRange(
				new Rectangle2D(block.getTranslateX() - 40, block.getTranslateY() - 40, 120, 120), Type.PLATFORM)) {
			if (b.getProperty(Property.SUB_TYPE) == Platform.DESTRUCTIBLE && !"dying".equals(b.getProperty("state"))) {
				destroyBlock(b);
			}
		}

		getSceneManager().removeEntity(block);
	}

	public Random getRandom() {
		return new Random();
	}

	public void fillWithFire(Point2D start) {
		ArrayList<Entity> flames = new ArrayList<>();
		for(int i = 0; i < 10; i++){
			for(int j = 0; j < 2; j++){
				Texture t = getTexture(FileNames.FIREBALL_PROJ);
				t.setScaleY(-1);
				t.setPreserveRatio(true);
				t.setFitWidth(Config.BLOCK_SIZE*2);
				t.setTranslateY(-Config.BLOCK_SIZE*1.5);
				Entity e = new Entity(Type.PROP);
				e.setPosition(start.getX() + Config.BLOCK_SIZE*i*2, start.getY() - Config.BLOCK_SIZE*j*3);
				e.setGraphics(t);
				flames.add(e);
			}
		}
		
		SimpleBooleanProperty tRun = new SimpleBooleanProperty(true);
		getTimerManager().runAtIntervalWhile(()->{
			getSceneManager().addEntities(flames.remove(0));
			soundManager.playSFX(FileNames.FIRE_TRAP);
			tRun.set(!flames.isEmpty());
		}, Duration.seconds(0.4), tRun);
		fOutComMet = () -> {
				indiLoop.stop();
				soundManager.stopAll();
				soundManager.setBackgroundMusic(FileNames.THEME_MUSIC);
	        	soundManager.getBackgroundMusic().setCycleCount(Integer.MAX_VALUE);
	        	resetWorld();

	        	consGameMenu.itemExit.fireEvent(new MenuEvent(MenuEvent.EXIT));
	        	soundManager.playBackgroundMusic();
		};
		
		tRun.addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if(!arg2){
					fadeOut.play();
				}
			}
		});
	}
	
	public static void main(String[] args) {
	    for(String s : args){
	        if(s.contains("-Xmx")){
	            launch(args);
	            return;
	        }
	    }
	    
	    String[] args2 = new String[args.length+1];
	    
	    for(int i = 0; i < args.length; i++){
	        args2[i] = args[i];
	    }
	    
	    args2[args.length] = "-Xmx1024M";
	    
		launch(args2);
	}
}
