package com.almasb.consume;

import java.io.Serializable;
import java.util.List;
import java.util.Random;
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
import com.almasb.consume.collision.PlayerEnemyHandler;
import com.almasb.consume.collision.PlayerPowerupHandler;
import com.almasb.consume.collision.ProjectileEnemyHandler;
import com.almasb.consume.collision.ProjectilePlayerHandler;
import com.almasb.fxgl.GameApplication;
import com.almasb.fxgl.asset.Assets;
import com.almasb.fxgl.asset.SaveLoadManager;
import com.almasb.fxgl.asset.Texture;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.event.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.PhysicsManager;
import com.almasb.fxgl.settings.GameSettings;
import com.almasb.fxgl.time.TimerManager;
import com.almasb.fxgl.ui.FXGLMenu;
import com.almasb.fxgl.ui.FXGLMenuFactory;
import com.ergo21.consume.ConsumeController;
import com.ergo21.consume.ConsumeGameMenu;
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
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class ConsumeApp extends GameApplication {

	public Assets assets;
	private LevelParser parser;
	public SoundManager soundManager;

	public Entity player;
	public Player playerData;

	public Physics physics = new Physics(this);
	public EntitySpawner eSpawner;
	public IndependentLoop indiLoop;

	private List<Level> levels;

	private PlayerHUD hud;
	private ConsumeGameMenu consGameMenu;
	private ConsumeMainMenu consMainMenu;
	private Text performance = new Text();

	private long regenTime = 0;

	private boolean playerDied = false;

	public GameScene gScene;
	public ConsumeController consController;

	public SavedSettings sSettings;

	private LevelMenu levelMenu;
	
	public DoubleProperty backVol;
	public DoubleProperty sfxVol;

	@Override
	protected void initSettings(GameSettings settings) {
		settings.setTitle("Consume");
		settings.setVersion("dev version");
		settings.setWidth(640);
		settings.setHeight(360);
		settings.setFullScreen(false);
		settings.setIntroEnabled(false);
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
	protected void initAssets() throws Exception {
		assets = getAssetManager().cache();
		assets.logCached();
	}

	@Override
	protected void initGame() {
		playerData = new Player(assets.getText("player.txt"));
		playerData.getPowers().add(Element.NEUTRAL2);
		//playerData.getPowers().add(Element.FIRE);
		playerData.getPowers().add(Element.EARTH);
		playerData.getPowers().add(Element.LIGHTNING);
		playerData.getPowers().add(Element.METAL);
		playerData.getPowers().add(Element.DEATH);
		//playerData.getPowers().add(Element.CONSUME);
		eSpawner = new EntitySpawner(this);

		initLevels();

		loadLevel(playerData.getCurrentLevel());
	}

	@Override
	protected void initPhysics() {
	    PhysicsManager physicsManager = getPhysicsManager();

		physicsManager.addCollisionHandler(new PlayerPowerupHandler(this));
		physicsManager.addCollisionHandler(new PlayerEnemyHandler(this));
		physicsManager.addCollisionHandler(new ProjectileEnemyHandler(this));
		physicsManager.addCollisionHandler(new PlayerBlockHandler((String scName) -> {
			gScene.changeScene(assets.getText(scName));
			gScene.playScene();
		}));
		physicsManager.addCollisionHandler(new ProjectilePlayerHandler(this));

		physicsManager.addCollisionHandler(new CollisionHandler(Type.PLAYER, Type.NEXT_LEVEL_POINT) {
			@Override
			public void onCollisionBegin(Entity a, Entity b) {
				a.setProperty("inDoor", true);
			}
			@Override
            public void onCollisionEnd(Entity a, Entity b) {
				a.setProperty("inDoor", false);
			}
		});
	}

	private Text debug = new Text();

	@Override
	protected void initUI() {
		gScene = new GameScene(assets.getText("dialogue/scene_0.txt"), assets, this);
		gScene.setTranslateX(140);
		gScene.setTranslateY(300);
		gScene.setScaleX(1.75);
		gScene.setScaleY(1.75);
		gScene.playScene();

		hud = new PlayerHUD(player.<Player> getProperty(Property.DATA).getMaxHealth(),
				player.<Player> getProperty(Property.DATA).getMaxMana());

		hud.setTranslateX(10);
		hud.setTranslateY(100);

		performance.setTranslateX(450);
		performance.setTranslateY(50);
		performance.setFill(Color.BLACK);

		debug.setTranslateX(450);
		debug.setTranslateY(100);
		getSceneManager().addUINodes(gScene, hud, performance, debug);

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
        indiLoop.start();
	}

	private void initLevels() {
		List<LevelData> levelData = IntStream.range(0, Config.MAX_LEVELS)
				.mapToObj(i -> new LevelData(assets.getText("levels/level_" + i + ".txt")))
				.collect(Collectors.toList());

		parser = new LevelParser(levelData);
		levels = parser.parseAll();
	}

	@Override
	protected void initInput() {
		consController = new ConsumeController(this);
		consController.initControls();
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
		if (player.<Boolean>getProperty("climb") != null && !player.<Boolean> getProperty("climb")) {
			// here player is no longer touching the ladder
			player.setProperty("climbing", false);
			player.setProperty(Property.ENABLE_GRAVITY, true);
		}

		if (getNow() - regenTime >= TimerManager.toNanos(Config.REGEN_TIME_INTERVAL)) {
			playerData.regenMana();
			regenTime = getNow();
		}

		if(playerData.getCurrentHealth() <= 0){
			playerData.setCurrentHealth(0);
			player.fireFXGLEvent(new FXGLEvent(Event.PLAYER_DEATH));
		}

		for (Entity e : getSceneManager().getEntities(Type.BLOCK)) {
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
		}

		player.setProperty("climb", false);

		debug.setText("Debug text goes here");
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
		soundManager.playBackgroundMusic();
	}

	private void loadLevel(int lev) {
	    getSceneManager().getEntities().forEach(getSceneManager()::removeEntity);
		System.out.println("Level number: " + lev);
		Level level = levels.get(lev);
		Point2D spawnPoint = level.getSpawnPoint();

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

		if(gScene != null){
			gScene.endScene();
		}

		// add player
		initPlayer(spawnPoint);
		playerDied = false;

		// TODO Remove manual spawn
		getInputManager().addAction(new UserAction("Spawn Flyer") {
			@Override
			protected void onActionBegin() {
				getSceneManager().addEntities(eSpawner.spawnEnemy(spawnPoint.add(1000, -90)));
			}
		}, KeyCode.DIGIT1);

		getInputManager().addAction(new UserAction("Spawn Dog") {
			@Override
			protected void onActionBegin() {
				getSceneManager().addEntities(eSpawner.spawnDog(spawnPoint.add(1000, 0)));
			}
		}, KeyCode.DIGIT2);
		getInputManager().addAction(new UserAction("Spawn Scarab") {
			@Override
			protected void onActionBegin() {
				getSceneManager().addEntities(eSpawner.spawnScarab(spawnPoint.add(1000, 0)));
			}
		}, KeyCode.DIGIT3);
		getInputManager().addAction(new UserAction("Spawn Locust") {
			@Override
			protected void onActionBegin() {
				getSceneManager().addEntities(eSpawner.spawnLocust(spawnPoint.add(1000, -90)));
			}
		}, KeyCode.DIGIT4);
		getInputManager().addAction(new UserAction("Spawn Boss") {
			@Override
			protected void onActionBegin() {
				getSceneManager().addEntities(eSpawner.spawnBoss(spawnPoint.add(1000, 0)));
			}
		}, KeyCode.DIGIT5);
		getInputManager().addAction(new UserAction("Spawn Eloko") {
			@Override
			protected void onActionBegin() {
				getSceneManager().addEntities(eSpawner.spawnEloko(spawnPoint.add(1000, 0)));
			}
		}, KeyCode.DIGIT6);
		getInputManager().addAction(new UserAction("Spawn Spear Thrower"){
			@Override
			protected void onActionBegin() {
				getSceneManager().addEntities(eSpawner.spawnSpearEnemy(spawnPoint.add(1000, 0)));
			}
		}, KeyCode.DIGIT7);
		getInputManager().addAction(new UserAction("Spawn Scorpion"){
			@Override
			protected void onActionBegin() {
				getSceneManager().addEntities(eSpawner.spawnScorpion(spawnPoint.add(1000, 0)));
			}
		}, KeyCode.DIGIT8);
		getInputManager().addAction(new UserAction("Play Background Music") {
			@Override
			protected void onActionBegin() {
	            soundManager.stopAll();

				soundManager.setBackgroundMusic(FileNames.PYRAMID_MUSIC);
				soundManager.getBackgroundMusic().setCycleCount(Integer.MAX_VALUE);
				soundManager.playBackgroundMusic();
			}
		}, KeyCode.P);
	}

	public void changeLevel() {
		getInputManager().setProcessActions(false);
		Rectangle bg = new Rectangle(this.getWidth(), this.getHeight());
		bg.setFill(Color.rgb(10, 1, 1));
		bg.setOpacity(0);
		getSceneManager().addUINodes(bg);
		FadeTransition ft = new FadeTransition(Duration.seconds(0.5), bg);
		ft.setFromValue(0);
		ft.setToValue(1);
		FadeTransition ft2 = new FadeTransition(Duration.seconds(0.5), bg);
		ft2.setFromValue(1);
		ft2.setToValue(0);
		FadeTransition ft3 = new FadeTransition(Duration.seconds(1), bg);
		ft3.setFromValue(1);
		ft3.setToValue(1);
		ft.setOnFinished(evt -> {
			if(playerData.getCurrentHealth() <= 0){
				playerData.setCurrentHealth(playerData.getMaxHealth());
				playerData.setCurrentMana(playerData.getMaxMana());
			}
			getSceneManager().getEntities().forEach(getSceneManager()::removeEntity);
			levels.set(playerData.getCurrentLevel(), parser.parse(playerData.getCurrentLevel()));
			getInputManager().setProcessActions(true);
			ft3.play();
		});
		ft2.setOnFinished(evt -> {
			getSceneManager().removeUINode(bg);
		});
		ft3.setOnFinished(evt -> {
			loadLevel(playerData.getCurrentLevel());
			this.getSceneManager().removeUINode(levelMenu);
			ft2.play();
		});
		ft.play();
	}

	public void showLevelScreen(){
		//TODO
		System.out.println("Show level screen");
		consGameMenu.updatePowerMenu(playerData);

		//this.getSceneManager().closeGameMenu();
		this.soundManager.stopAll();
		if(playerData.getPowers().size() > 6){
			levelMenu.setFinalLevelVisible(true);
		}
		else{
			levelMenu.setFinalLevelVisible(false);
		}
		this.getSceneManager().removeUINode(levelMenu);
		this.getSceneManager().addUINodes(levelMenu);
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
			soundManager.setBackgroundMusic(getBackgroundMusic());
			soundManager.getBackgroundMusic().setCycleCount(Integer.MAX_VALUE);

			changeLevel();
		}
		else{
			System.out.println(d.getClass());
		}
	}

	public String getBackgroundMusic(){
		switch(playerData.getCurrentLevel()/3){
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

	Entity powerStatus;

	private void initPlayer(Point2D point) {

		player = new Entity(Type.PLAYER).setPosition(point.getX(), point.getY()).setCollidable(true)
				.setProperty(Property.DATA, playerData).setProperty("climb", false).setProperty("climbing", false)
				.setProperty("facingRight", true).setProperty("stunned", false).setProperty("eating", false).setProperty("eaten",  false)
				.addControl(new PhysicsControl(physics));

		player.addFXGLEventHandler(Event.PLAYER_DEATH, this::playerDied);

		Texture t = getAssetManager().loadTexture("MC Unarmed.png");
		player.addControl(new AnimatedPlayerControl(t));
		t.setViewport(new Rectangle2D(150, 0, 30, 30));
		player.setGraphics(t);


		getSceneManager().bindViewportOrigin(player, 320, 180);

		powerStatus = Entity.noType();
		powerStatus.translateXProperty().bind(player.translateXProperty());
		powerStatus.translateYProperty().bind(player.translateYProperty().subtract(40));
		Text tTex = new Text();
		tTex.textProperty().bind(playerData.ElementProperty().asString());
		powerStatus.setGraphics(tTex);

		getSceneManager().addEntities(powerStatus);

		getSceneManager().addEntities(player);
	}

	private void playerDied(FXGLEvent e){
		if (!playerDied){
			player.setProperty("stunned", true);
			playerDied = true;
			getTimerManager().runOnceAfter(this::changeLevel, Duration.seconds(0.5));
		}
	}

	private void activateBarrier(Entity block) {
		block.setProperty("state", "dying");

		for (Entity b : getSceneManager().getEntitiesInRange(
				new Rectangle2D(block.getTranslateX() - 40, block.getTranslateY() - 40, 120, 120), Type.BLOCK)) {
			if (b.getProperty(Property.SUB_TYPE) == Block.BARRIER && !"dying".equals(b.getProperty("state"))) {
				activateBarrier(b);
			}
		}

		getSceneManager().removeEntity(block);
		Entity e = new Entity(Type.PLATFORM);
		e.setPosition(block.getTranslateX(), block.getTranslateY());
		Rectangle rect = new Rectangle(40, 40);
		rect.setFill(Color.GREY);
		e.setGraphics(rect);

		getSceneManager().addEntities(e);
	}

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

	public static void main(String[] args) {
		launch(args);
	}
}
