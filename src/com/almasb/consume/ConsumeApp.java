package com.almasb.consume;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.almasb.consume.Config.Speed;
import com.almasb.consume.LevelParser.Level;
import com.almasb.consume.LevelParser.LevelData;
import com.almasb.consume.Types.Block;
import com.almasb.consume.Types.Element;
import com.almasb.consume.Types.Platform;
import com.almasb.consume.Types.Powerup;
import com.almasb.consume.Types.Property;
import com.almasb.consume.Types.Type;
import com.almasb.consume.ai.AnimatedPlayerControl;
import com.almasb.consume.ai.ChargeControl;
import com.almasb.consume.ai.PhysicsControl;
import com.almasb.consume.ai.SimpleJumpControl;
import com.almasb.consume.ai.SimpleMoveControl;
import com.almasb.consume.collision.PlayerBlockHandler;
import com.almasb.consume.collision.PlayerEnemyHandler;
import com.almasb.consume.collision.PlayerPowerupHandler;
import com.almasb.consume.collision.ProjectileEnemyHandler;
import com.almasb.consume.collision.ProjectilePlayerHandler;
import com.almasb.fxgl.GameApplication;
import com.almasb.fxgl.GameSettings;
import com.almasb.fxgl.asset.Assets;
import com.almasb.fxgl.asset.Texture;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.physics.CollisionHandler;
import com.ergo21.consume.ConsumeController;
import com.ergo21.consume.ConsumeGameMenu;
import com.ergo21.consume.Enemy;
import com.ergo21.consume.GameScene;
import com.ergo21.consume.Player;
import com.ergo21.consume.PlayerHUD;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class ConsumeApp extends GameApplication {

    private Assets assets;

    public Entity player;
    public Player playerData;

    public Physics physics = new Physics(this);

    private List<Level> levels;
    private int currentLevel = 0;

    private PlayerHUD hud;
    private ConsumeGameMenu consGameMenu;
    private Text performance = new Text();

    private long regenTime = 0;

    public GameScene gScene;
    public ConsumeController consController;

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
        playerData.getPowers().add(Element.FIRE);
        playerData.getPowers().add(Element.EARTH);
        playerData.getPowers().add(Element.LIGHTNING);
        playerData.getPowers().add(Element.METAL);
        playerData.getPowers().add(Element.DEATH);
        playerData.getPowers().add(Element.CONSUME);

        initLevels();

        currentLevel = 0;
        loadLevel(currentLevel++);
    }

    @Override
    protected void initPhysics() {
        physicsManager.addCollisionHandler(new PlayerPowerupHandler());
        physicsManager.addCollisionHandler(new PlayerEnemyHandler(this));
        physicsManager.addCollisionHandler(new ProjectileEnemyHandler(this));
        physicsManager.addCollisionHandler(new PlayerBlockHandler((String scName)->{
        	gScene.changeScene(assets.getText(scName));
        	gScene.playScene();
        }));
        physicsManager.addCollisionHandler(new ProjectilePlayerHandler(this));

        physicsManager.addCollisionHandler(new CollisionHandler(Type.PLAYER, Type.NEXT_LEVEL_POINT) {
            @Override
            public void onCollisionBegin(Entity a, Entity b) {
                loadLevel(currentLevel++);
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

        hud = new PlayerHUD(player.<Player>getProperty(Property.DATA).getMaxHealth(),
                player.<Player>getProperty(Property.DATA).getMaxMana());

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
        List<LevelData> levelData =
                IntStream.range(0, Config.MAX_LEVELS)
                .mapToObj(i -> new LevelData(assets.getText("levels/level_" + i + ".txt")))
                .collect(Collectors.toList());

        LevelParser parser = new LevelParser(levelData);
        levels = parser.parseAll();
    }

    @Override
    protected void initInput() {
    	consController = new ConsumeController(this);
    	consController.initControls();
    }
    
    @Override
    protected ConsumeGameMenu initGameMenu(){
    	consGameMenu = new ConsumeGameMenu(this);
    	
    	return consGameMenu;
    }
    
    @Override
    protected void postInit(){
    	hud.CurHealthProperty().bind(playerData.CurrentHealthProperty());
    	hud.CurManaProperty().bind(playerData.CurrentManaProperty());
    	hud.MaxHealthProperty().bind(playerData.MaxHealthProperty());
    	hud.MaxManaProperty().bind(playerData.MaxManaProperty());
    	
    	consGameMenu.updatePowerMenu(playerData);
    }

    @Override
    protected void onUpdate() {
    	if (!player.<Boolean>getProperty("climb")) {
            // here player is no longer touching the ladder
            player.setProperty("climbing", false);
            player.setProperty(Property.ENABLE_GRAVITY, true);
        }

        if (getNow() - regenTime >= Config.REGEN_TIME_INTERVAL) {
            playerData.regenMana();
            regenTime = getNow();
        }

        for (Entity e : sceneManager.getEntities(Type.BLOCK)) {
            if (e.getProperty(Property.SUB_TYPE) == Block.BARRIER
                    &&"idle".equals(e.getProperty("state"))
                    && !"none".equals(e.getProperty("start"))) {

                if (player.getTranslateX() <= e.getTranslateX()) {
                    // check if came from left
                    if (!"left".equals(e.getProperty("start"))) {
                        activateBarrier(e);
                        player.setTranslateX(e.getTranslateX() - 40);
                    }
                    else {
                        e.setProperty("start", "none");
                    }
                }
                else {
                    if (!"right".equals(e.getProperty("start"))) {
                        activateBarrier(e);
                        player.setTranslateX(e.getTranslateX() + 40);
                    }
                    else {
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

	private void loadLevel(int lev) {
		sceneManager.getEntities().forEach(sceneManager::removeEntity);
		
        Level level = levels.get(lev);
        Point2D spawnPoint = level.getSpawnPoint();

        // add level objects
        for (Entity e : level.getEntitiesAsArray()) {
            // TODO: currently we don't have death animations/handlers for other types
            // when we will, this might move to another class
            if (e.isType(Type.POWERUP)) {
                e.addFXGLEventHandler(Event.DEATH, event -> sceneManager.removeEntity(e));
            }

            sceneManager.addEntities(e);
        }
        
        // add player
        initPlayer(spawnPoint);
        

        // TODO: remove after test
        Entity testEnemy = new Entity(Type.ENEMY);

        Rectangle rect = new Rectangle(30, 30);
        rect.setFill(Color.RED);

        testEnemy.setGraphics(rect);
        testEnemy.setCollidable(true);
        testEnemy.setProperty(Property.DATA, new Enemy(assets.getText("enemies/enemy_FireElemental.txt")));
        testEnemy.setProperty("physics", physics);
        testEnemy.setPosition(spawnPoint.getX() + 640, spawnPoint.getY() + 10);
        testEnemy.addControl(new ChargeControl(player));
        testEnemy.addControl(new PhysicsControl(physics));
        testEnemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
        testEnemy.addFXGLEventHandler(Event.ENEMY_SAW_PLAYER, event -> {
            Entity enemy = event.getTarget();

            Entity e = Entity.noType().setGraphics(new Text("!"));
            e.setPosition(enemy.getTranslateX(), enemy.getTranslateY());
            sceneManager.addEntities(e);
            timerManager.runOnceAfter(() -> {
                sceneManager.removeEntity(e);
            }, Config.ENEMY_CHARGE_DELAY);
        });
        
        sceneManager.addEntities(testEnemy);

        Entity testEnemy2 = new Entity(Type.ENEMY);
        Rectangle rect2 = new Rectangle(30, 30);
        rect2.setFill(Color.RED);

        testEnemy2.setGraphics(rect2);
        testEnemy2.setCollidable(true);
        testEnemy2.setProperty(Property.DATA, new Enemy(assets.getText("enemies/enemy_FireElemental.txt")));
        testEnemy2.setProperty("physics", physics);
        testEnemy2.setPosition(spawnPoint.getX() + 640, spawnPoint.getY() - 90);
        //testEnemy2.addControl(new AimedProjectileControl(player));
        testEnemy2.addControl(new SimpleMoveControl(player));
        testEnemy2.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
        testEnemy2.addFXGLEventHandler(Event.ENEMY_SAW_PLAYER, event -> consController.aimedProjectile(testEnemy2, player));
        sceneManager.addEntities(testEnemy2);
        
        Entity testEnemy3 = new Entity(Type.ENEMY);
        Rectangle rect3 = new Rectangle(30, 30);
        rect3.setFill(Color.RED);
        testEnemy3.setGraphics(rect3);
        testEnemy3.setCollidable(true);
        testEnemy3.setProperty(Property.DATA, new Enemy(assets.getText("enemies/enemy_FireElemental.txt")));
        testEnemy3.setProperty("physics", physics);
        testEnemy3.setPosition(spawnPoint.getX() + 1000, spawnPoint.getY() - 90);
        //testEnemy2.addControl(new AimedProjectileControl(player));
        testEnemy3.addControl(new PhysicsControl(physics));
        testEnemy3.addControl(new SimpleJumpControl(player, Speed.ENEMY_JUMP));
        testEnemy3.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
        sceneManager.addEntities(testEnemy3);
    }

	Entity powerStatus;

    private void initPlayer(Point2D point) {

        player = new Entity(Type.PLAYER)
            .setPosition(point.getX(), point.getY())
            .setCollidable(true)
            .setProperty(Property.DATA, playerData)
            .setProperty("climb", false)
            .setProperty("climbing", false)
            .setProperty("facingRight", true)
            .setProperty("stunned", false)
            .addControl(new PhysicsControl(physics));

        Rectangle graphics = new Rectangle(15, 30);
        graphics.setFill(Color.YELLOW);
		try {
			Texture t = this.assetManager.loadTexture("MC Unarmed.png");
			player.addControl(new AnimatedPlayerControl(t));
			t.setViewport(new Rectangle2D(150,0,30,30));
			player.setGraphics(t);
		} catch (Exception e) {
			// TODO Auto-generated catch block
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

    private void onEnemyDeath(FXGLEvent event) {
        Entity enemy = event.getTarget();
        sceneManager.removeEntity(enemy);

        // chance based drop logic
        if (random.nextInt(100) <= 33) {    // check if dropping
            ArrayList<Powerup> drops = new ArrayList<>();
            drops.add(Powerup.RESTORE_HEALTH_12);
            drops.add(Powerup.RESTORE_MANA_12);

            Player p = player.getProperty(Property.DATA);
            double hpPercent = p.getCurrentHealth() * 1.0 / p.getMaxHealth();
            double manaPercent = p.getCurrentMana() * 1.0 / p.getMaxMana();

            if (hpPercent < 0.75)
                drops.add(Powerup.RESTORE_HEALTH_25);

            if (hpPercent < 0.5)
                drops.add(Powerup.RESTORE_HEALTH_50);

            if (manaPercent < 0.75)
                drops.add(Powerup.RESTORE_MANA_25);

            if (hpPercent < 0.5)
                drops.add(Powerup.RESTORE_MANA_50);

            if (hpPercent > manaPercent) {
                drops.remove(Powerup.RESTORE_HEALTH_12);
                drops.remove(Powerup.RESTORE_HEALTH_25);
                drops.remove(Powerup.RESTORE_HEALTH_50);
            }
            else {
                drops.remove(Powerup.RESTORE_MANA_12);
                drops.remove(Powerup.RESTORE_MANA_25);
                drops.remove(Powerup.RESTORE_MANA_50);
            }

            Collections.shuffle(drops);

            Entity e = new Entity(Type.POWERUP);
            e.setCollidable(true);
            e.setPosition(enemy.getTranslateX(), enemy.getTranslateY());
            e.setProperty(Property.SUB_TYPE, drops.get(0));
            Rectangle r = new Rectangle(30, 30);
            r.setFill(Color.PINK);
            e.setGraphics(r);

            sceneManager.addEntities(e);
        }
    }

    private void activateBarrier(Entity block) {
        block.setProperty("state", "dying");

        for (Entity b : sceneManager.getEntitiesInRange(
                new Rectangle2D(block.getTranslateX() - 40,
                        block.getTranslateY() - 40,
                        120, 120),
                        Type.BLOCK)) {
            if (b.getProperty(Property.SUB_TYPE) == Block.BARRIER
                    && !"dying".equals(b.getProperty("state"))) {
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
                new Rectangle2D(block.getTranslateX() - 40,
                        block.getTranslateY() - 40,
                        120, 120),
                        Type.PLATFORM)) {
            if (b.getProperty(Property.SUB_TYPE) == Platform.DESTRUCTIBLE
                    && !"dying".equals(b.getProperty("state"))) {
                destroyBlock(b);
            }
        }

        sceneManager.removeEntity(block);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
