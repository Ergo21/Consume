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
import com.almasb.fxgl.GameSettings;
import com.almasb.fxgl.asset.Assets;
import com.almasb.fxgl.asset.SaveLoadManager;
import com.almasb.fxgl.asset.Texture;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.event.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.time.TimerManager;
import com.ergo21.consume.ConsumeController;
import com.ergo21.consume.ConsumeGameMenu;
import com.ergo21.consume.ConsumeMainMenu;
import com.ergo21.consume.EntitySpawner;
import com.ergo21.consume.GameSave;
import com.ergo21.consume.GameScene;
import com.ergo21.consume.IndependentLoop;
import com.ergo21.consume.Player;
import com.ergo21.consume.PlayerHUD;
import com.ergo21.consume.SavedSettings;
import com.ergo21.consume.SoundManager;

import javafx.animation.FadeTransition;
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

	@Override
	protected void initSettings(GameSettings settings) {
		settings.setTitle("Consume");
		settings.setVersion("dev version");
		settings.setWidth(640);
		settings.setHeight(360);
		settings.setIntroEnabled(false);
		settings.setMenuEnabled(true);
		settings.setIconFileName("app_icon.png");
		settings.setShowFPS(false);
		
		if(SaveLoadManager.INSTANCE.loadFileNames().isPresent() && SaveLoadManager.INSTANCE.loadFileNames().get().contains("settings.set")){
			try {
				sSettings = (SavedSettings)SaveLoadManager.INSTANCE.load("settings.set");
			} catch (Exception e) {
				System.out.println("Unable to load settings");
				e.printStackTrace();
				sSettings = new SavedSettings();
			}
		}
		else{
			sSettings = new SavedSettings();
			try {
				SaveLoadManager.INSTANCE.save(sSettings, "settings.set");
			} catch (Exception e) {
				System.out.println("Unable to save settings");
				e.printStackTrace();
			}
		}
		
		
		
	}

	@Override
	protected void initAssets() throws Exception {
		assets = assetManager.cache();
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
		playerData.getPowers().add(Element.CONSUME);
		eSpawner = new EntitySpawner(this);

		initLevels();

		loadLevel(playerData.getCurrentLevel());
	}

	@Override
	protected void initPhysics() {
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
		sceneManager.addUINodes(gScene, hud, performance, debug);
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
	protected ConsumeGameMenu initGameMenu() {
		consGameMenu = new ConsumeGameMenu(this);

		return consGameMenu;
	}
	
	@Override
	protected ConsumeMainMenu initMainMenu(){
		consMainMenu = new ConsumeMainMenu(this);
		return consMainMenu;
	}

	@Override
	protected void postInit() {
		hud.CurHealthProperty().bind(playerData.CurrentHealthProperty());
		hud.CurManaProperty().bind(playerData.CurrentManaProperty());
		hud.MaxHealthProperty().bind(playerData.MaxHealthProperty());
		hud.MaxManaProperty().bind(playerData.MaxManaProperty());

		consGameMenu.updatePowerMenu(playerData);
		soundManager = new SoundManager(this);
		soundManager.setBackgroundMusic("07 Festival.mp3");
		soundManager.getBackgroundMusic().loop();
		indiLoop = new IndependentLoop(this);
		indiLoop.start();
	}

	@Override
	protected void onUpdate() {
		if (player.<Boolean>getProperty("climb") != null && !player.<Boolean> getProperty("climb")) {
			// here player is no longer touching the ladder
			player.setProperty("climbing", false);
			player.setProperty(Property.ENABLE_GRAVITY, true);
		}

		if (getNow() - regenTime >= Config.REGEN_TIME_INTERVAL) {
			playerData.regenMana();
			regenTime = getNow();
		}
		
		if(playerData.getCurrentHealth() <= 0){
			playerData.setCurrentHealth(0);
			player.fireFXGLEvent(new FXGLEvent(Event.PLAYER_DEATH));
		}

		for (Entity e : sceneManager.getEntities(Type.BLOCK)) {
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
			SaveLoadManager.INSTANCE.save(sSettings, "settings.set");
		} catch (Exception e) {
			System.out.println("Unable to save settings");
			e.printStackTrace();
		}
	}

	private void loadLevel(int lev) {
		sceneManager.getEntities().forEach(sceneManager::removeEntity);

		Level level = levels.get(lev);
		Point2D spawnPoint = level.getSpawnPoint();
		
		// add level objects
		for (Entity e : level.getEntitiesAsArray()) {
			// TODO: currently we don't have death animations/handlers for other
			// types
			// when we will, this might move to another class
			if (e.isType(Type.POWERUP)) {
				e.addFXGLEventHandler(Event.DEATH, event -> sceneManager.removeEntity(e));
			}
			if(playerData.getUpgrades().contains(lev) && (e.getProperty(Property.SUB_TYPE) == Powerup.INC_MANA_REGEN
														|| e.getProperty(Property.SUB_TYPE) == Powerup.INC_MAX_MANA
														|| e.getProperty(Property.SUB_TYPE) == Powerup.INC_MAX_HEALTH)){
				
			}	
			else{
				sceneManager.addEntities(e);
			}
			
		}
		
		if(gScene != null){
			gScene.endScene();
		}
		
		// add player
		initPlayer(spawnPoint);
		playerDied = false;
		
		// TODO Remove manual spawn
		inputManager.addAction(new UserAction("Spawn Flyer") {
			@Override
			protected void onActionBegin() {
				sceneManager.addEntities(eSpawner.spawnEnemy(spawnPoint.add(1000, -90)));
			}
		}, KeyCode.DIGIT1);

		inputManager.addAction(new UserAction("Spawn Dog") {
			@Override
			protected void onActionBegin() {
				sceneManager.addEntities(eSpawner.spawnDog(spawnPoint.add(1000, 0)));
			}
		}, KeyCode.DIGIT2);
		inputManager.addAction(new UserAction("Spawn Scarab") {
			@Override
			protected void onActionBegin() {
				sceneManager.addEntities(eSpawner.spawnScarab(spawnPoint.add(1000, 0)));
			}
		}, KeyCode.DIGIT3);
		inputManager.addAction(new UserAction("Spawn Locust") {
			@Override
			protected void onActionBegin() {
				sceneManager.addEntities(eSpawner.spawnLocust(spawnPoint.add(1000, -90)));
			}
		}, KeyCode.DIGIT4);
		inputManager.addAction(new UserAction("Spawn Boss") {
			@Override
			protected void onActionBegin() {
				sceneManager.addEntities(eSpawner.spawnBoss(spawnPoint.add(1000, 0)));
			}
		}, KeyCode.DIGIT5);
		inputManager.addAction(new UserAction("Play Background Music") {
			@Override
			protected void onActionBegin() {
				soundManager.getBackgroundMusic().stop();
				soundManager.setBackgroundMusic("06 Pyramid.mp3");
				soundManager.getBackgroundMusic().loop();
			}
		}, KeyCode.P);
		
	}
	
	public void changeLevel() {
		pause();
		Rectangle bg = new Rectangle(this.getWidth(), this.getHeight());
		bg.setFill(Color.rgb(10, 1, 1));
		bg.setOpacity(0);
		sceneManager.addUINodes(bg);
		FadeTransition ft = new FadeTransition(Duration.seconds(0.5), bg);
		ft.setFromValue(0);
		ft.setToValue(1);
		FadeTransition ft2 = new FadeTransition(Duration.seconds(0.5), bg);
		ft2.setFromValue(1);
		ft2.setToValue(0);
		ft.setOnFinished(evt -> {
			if(playerData.getCurrentHealth() <= 0){
				playerData.setCurrentHealth(playerData.getMaxHealth());
				playerData.setCurrentMana(playerData.getMaxMana());
			}
			sceneManager.getEntities().forEach(sceneManager::removeEntity);
			levels.set(playerData.getCurrentLevel(), parser.parse(playerData.getCurrentLevel()));
			resume();
			System.out.println("Change 1 Finished");
			timerManager.runOnceAfter(new Runnable(){
				@Override
				public void run() {
					System.out.println("Change 2 Started");
					loadLevel(playerData.getCurrentLevel());
					ft2.play();
				}}, TimerManager.SECOND);
			
		});	
		ft2.setOnFinished(evt -> {
			sceneManager.removeUINode(bg);
			System.out.println("Change 2 Finished");
		});
		ft.play();
	}
	
	public void showLevelScreen(){
		//TODO
		System.out.println("Show level screen");
		consGameMenu.updatePowerMenu(playerData);
		
	}
	
	@Override
	public Serializable saveState(){
		return new GameSave(playerData);
	}
	
	@Override 
	public void loadState(Serializable d){
		if(d.getClass() == GameSave.class){
			if(playerData == null){
				this.startNewGame();
			}
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
			
			changeLevel();
		}
		else{
			System.out.println(d.getClass());
		}
	}

	Entity powerStatus;

	private void initPlayer(Point2D point) {

		player = new Entity(Type.PLAYER).setPosition(point.getX(), point.getY()).setCollidable(true)
				.setProperty(Property.DATA, playerData).setProperty("climb", false).setProperty("climbing", false)
				.setProperty("facingRight", true).setProperty("stunned", false).addControl(new PhysicsControl(physics));
		
		player.addFXGLEventHandler(Event.PLAYER_DEATH, this::playerDied);
		
		Rectangle graphics = new Rectangle(15, 30);
		graphics.setFill(Color.YELLOW);
		try {
			Texture t = this.assetManager.loadTexture("MC Unarmed.png");
			player.addControl(new AnimatedPlayerControl(t));
			t.setViewport(new Rectangle2D(150, 0, 30, 30));
			player.setGraphics(t);
		} catch (Exception e) {
			e.printStackTrace();
			player.setGraphics(graphics);
		}

		sceneManager.bindViewportOrigin(player, 320, 180);

		powerStatus = Entity.noType();
		powerStatus.translateXProperty().bind(player.translateXProperty());
		powerStatus.translateYProperty().bind(player.translateYProperty().subtract(40));
		Text tTex = new Text();
		tTex.textProperty().bind(playerData.ElementProperty().asString());
		powerStatus.setGraphics(tTex);

		sceneManager.addEntities(powerStatus);

		sceneManager.addEntities(player);
	}
	
	private void playerDied(FXGLEvent e){
		if(!playerDied){
			player.setProperty("stunned", true);
			playerDied = true;
			timerManager.runOnceAfter(new Runnable(){
				@Override
				public void run() {
					changeLevel();
				}}, TimerManager.SECOND/2);	
		}
		
	}

	private void activateBarrier(Entity block) {
		block.setProperty("state", "dying");

		for (Entity b : sceneManager.getEntitiesInRange(
				new Rectangle2D(block.getTranslateX() - 40, block.getTranslateY() - 40, 120, 120), Type.BLOCK)) {
			if (b.getProperty(Property.SUB_TYPE) == Block.BARRIER && !"dying".equals(b.getProperty("state"))) {
				activateBarrier(b);
			}
		}

		sceneManager.removeEntity(block);
		Entity e = new Entity(Type.PLATFORM);
		e.setPosition(block.getTranslateX(), block.getTranslateY());
		Rectangle rect = new Rectangle(40, 40);
		rect.setFill(Color.GREY);
		e.setGraphics(rect);

		sceneManager.addEntities(e);
	}

	public void destroyBlock(Entity block) {
		block.setProperty("state", "dying");

		for (Entity b : sceneManager.getEntitiesInRange(
				new Rectangle2D(block.getTranslateX() - 40, block.getTranslateY() - 40, 120, 120), Type.PLATFORM)) {
			if (b.getProperty(Property.SUB_TYPE) == Platform.DESTRUCTIBLE && !"dying".equals(b.getProperty("state"))) {
				destroyBlock(b);
			}
		}

		sceneManager.removeEntity(block);
	}

	public Random getRandom() {
		return random;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
