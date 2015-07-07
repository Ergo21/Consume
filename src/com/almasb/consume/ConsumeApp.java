package com.almasb.consume;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import com.almasb.consume.Config.Speed;
import com.almasb.consume.LevelParser.Level;
import com.almasb.consume.LevelParser.LevelData;
import com.almasb.consume.Types.Block;
import com.almasb.consume.Types.Element;
import com.almasb.consume.Types.Platform;
import com.almasb.consume.Types.Powerup;
import com.almasb.consume.Types.Property;
import com.almasb.consume.Types.Type;
import com.almasb.consume.ai.AimedProjectileControl;
import com.almasb.consume.ai.ChargeControl;
import com.almasb.consume.ai.PhysicsControl;
import com.almasb.consume.ai.ProjectileControl;
import com.almasb.consume.ai.SimpleMoveControl;
import com.almasb.consume.collision.PlayerBlockHandler;
import com.almasb.consume.collision.PlayerEnemyHandler;
import com.almasb.consume.collision.PlayerPowerupHandler;
import com.almasb.consume.collision.ProjectileEnemyHandler;
import com.almasb.consume.collision.ProjectilePlayerHandler;
import com.almasb.fxgl.GameApplication;
import com.almasb.fxgl.GameSettings;
import com.almasb.fxgl.asset.Assets;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.ergo21.consume.Enemy;
import com.ergo21.consume.GameScene;
import com.ergo21.consume.Player;
import com.ergo21.consume.PlayerHUD;

public class ConsumeApp extends GameApplication {

    private Assets assets;

    private Entity player;
    private Player playerData;
    private boolean facingRight = true;

    private Physics physics = new Physics(this);

    private List<Level> levels;
    private int currentLevel = 0;

    private PlayerHUD hud;
    private Text performance = new Text();

    private long regenTime = 0;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Consume");
        settings.setVersion("dev version");
        settings.setWidth(640);
        settings.setHeight(360);
    }

    @Override
    protected void initAssets() throws Exception {
        assets = assetManager.cache();
        assets.logCached();
    }

    @Override
    protected void initMainMenu(Pane mainMenuRoot) {}

    @Override
    protected void initGame(Pane gameRoot) {
        playerData = new Player(assets.getText("player.txt"));

        initLevels();
        initCollisions();

        loadNextLevel();
    }

    private Text debug = new Text();

    @Override
    protected void initUI(Pane uiRoot) {
        GameScene scene = new GameScene(assets.getText("dialogue/scene_0.txt"), assets);
        scene.setTranslateX(140);
        scene.setTranslateY(300);
        scene.setScaleX(1.75);
        scene.setScaleY(1.75);

        addKeyTypedBinding(KeyCode.ENTER, scene::updateScript);

        hud = new PlayerHUD(player.<Player>getProperty(Property.DATA).getMaxHealth(),
                player.<Player>getProperty(Property.DATA).getMaxMana());

        hud.setTranslateX(10);
        hud.setTranslateY(100);

        performance.setTranslateX(450);
        performance.setTranslateY(50);
        performance.setFill(Color.BLACK);

        debug.setTranslateX(450);
        debug.setTranslateY(100);
        uiRoot.getChildren().addAll(scene, hud, performance, debug);
    }

    private void initLevels() {
        List<LevelData> levelData =
                IntStream.range(0, Config.MAX_LEVELS)
                .mapToObj(i -> new LevelData(assets.getText("levels/level_" + i + ".txt")))
                .collect(Collectors.toList());

        LevelParser parser = new LevelParser(levelData);
        levels = parser.parseAll();
    }

    private void initCollisions() {
        // order matters, must match class name
        addCollisionHandler(Type.PLAYER, Type.POWERUP, new PlayerPowerupHandler());
        addCollisionHandler(Type.PLAYER, Type.ENEMY, new PlayerEnemyHandler(this));
        addCollisionHandler(Type.PLAYER_PROJECTILE, Type.ENEMY, new ProjectileEnemyHandler(this));
        addCollisionHandler(Type.PLAYER, Type.BLOCK, new PlayerBlockHandler());
        addCollisionHandler(Type.PLAYER, Type.ENEMY_PROJECTILE, new ProjectilePlayerHandler(this));

        addCollisionHandler(Type.PLAYER, Type.NEXT_LEVEL_POINT, (player, point) -> {
            loadNextLevel();
        });

        addCollisionHandler(Type.PLAYER_PROJECTILE, Type.PLATFORM, (proj, platform) -> {
            removeEntity(proj);

            if (platform.getProperty(Property.SUB_TYPE) == Platform.DESTRUCTIBLE) {
                destroyBlock(platform);
            }
        });
    }

    @Override
    protected void initInput() {
        addKeyPressBinding(KeyCode.A, () -> {
            player.getControl(PhysicsControl.class).moveX(-Speed.PLAYER_MOVE);
            facingRight = false;
        });
        addKeyPressBinding(KeyCode.D, () -> {
            player.getControl(PhysicsControl.class).moveX(Speed.PLAYER_MOVE);
            facingRight = true;
        });
        addKeyPressBinding(KeyCode.W, () -> {
            if (player.<Boolean>getProperty("climbing")) {
                player.getControl(PhysicsControl.class).moveY(-5);
            }
            else
                player.getControl(PhysicsControl.class).jump();
        });
        addKeyPressBinding(KeyCode.S, () -> {
            if (player.<Boolean>getProperty("climbing")) {
                player.getControl(PhysicsControl.class).moveY(5);
            }
        });
        addKeyPressBinding(KeyCode.SPACE, () -> {
        });

        addKeyTypedBinding(KeyCode.Q, () -> {
            shootProjectile();
        });

        // debug
        addKeyTypedBinding(KeyCode.I, () -> {
            log.info(playerData::toString);
        });
    }

    @Override
    protected void onUpdate(long now) {
    	if (!player.<Boolean>getProperty("climb")) {
            // here player is no longer touching the ladder
            player.setProperty("climbing", false);
            player.setProperty(Property.ENABLE_GRAVITY, true);
        }

        if (now - regenTime >= Config.REGEN_TIME_INTERVAL) {
            playerData.regenMana();
            regenTime = now;
        }

        // leave manual update for now
        hud.setCurHealth(playerData.getCurrentHealth());
        hud.setCurMana(playerData.getCurrentMana());
        hud.setMaxHealth(playerData.getMaxHealth());
        hud.setMaxMana(playerData.getMaxMana());

        for (Entity e : getEntities(Type.BLOCK)) {
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

        performance.setText("FPS: " + fps + " Performance: " + fpsPerformance);
        debug.setText("Debug text goes here");
    }

    @Override
    protected void postInit() {
	}

	private void loadNextLevel() {
        getAllEntities().forEach(this::removeEntity);

        Level level = levels.get(currentLevel++);
        Point2D spawnPoint = level.getSpawnPoint();

        // add level objects
        for (Entity e : level.getEntitiesAsArray()) {
            // TODO: currently we don't have death animations/handlers for other types
            // when we will, this might move to another class
            if (e.isType(Type.POWERUP)) {
                e.addFXGLEventHandler(Event.DEATH, event -> removeEntity(e));
            }

            addEntities(e);
        }
        // add player
        initPlayer(spawnPoint);

        // TODO: remove after test
        Entity testEnemy = new Entity(Type.ENEMY);

        Rectangle rect = new Rectangle(30, 30);
        rect.setFill(Color.RED);

        testEnemy.setGraphics(rect);
        testEnemy.setUsePhysics(true);
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
            addEntities(e);
            runOnceAfter(() -> {
                removeEntity(e);
            }, Config.ENEMY_CHARGE_DELAY);
        });

        addEntities(testEnemy);
        
        Entity testEnemy2 = new Entity(Type.ENEMY);
        Rectangle rect2 = new Rectangle(30, 30);
        rect2.setFill(Color.RED);

        testEnemy2.setGraphics(rect2);
        testEnemy2.setUsePhysics(true);
        testEnemy2.setProperty(Property.DATA, new Enemy(assets.getText("enemies/enemy_FireElemental.txt")));
        testEnemy2.setProperty("physics", physics);
        testEnemy2.setPosition(spawnPoint.getX() + 640, spawnPoint.getY() - 90);
        //testEnemy2.addControl(new AimedProjectileControl(player));
        testEnemy2.addControl(new SimpleMoveControl(player));
        testEnemy2.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);
        testEnemy2.addFXGLEventHandler(Event.ENEMY_SAW_PLAYER, event -> aimedProjectile(testEnemy2, player));
        addEntities(testEnemy2);
    }

    private void initPlayer(Point2D point) {
        Rectangle graphics = new Rectangle(15, 30);
        graphics.setFill(Color.YELLOW);

        player = new Entity(Type.PLAYER)
            .setPosition(point.getX(), point.getY())
            .setUsePhysics(true)
            .setGraphics(graphics)
            .setProperty(Property.DATA, playerData)
            .setProperty("climb", false)
            .setProperty("climbing", false)
            .addControl(new PhysicsControl(physics));

        bindViewportOrigin(player, 320, 180);
        addEntities(player);
    }

    private void onEnemyDeath(FXGLEvent event) {
        Entity enemy = event.getTarget();
        removeEntity(enemy);

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
            e.setUsePhysics(true);
            e.setPosition(enemy.getTranslateX(), enemy.getTranslateY());
            e.setProperty(Property.SUB_TYPE, drops.get(0));
            Rectangle r = new Rectangle(30, 30);
            r.setFill(Color.PINK);
            e.setGraphics(r);

            addEntities(e);
        }
    }

    private void activateBarrier(Entity block) {
        block.setProperty("state", "dying");

        for (Entity b : getEntitiesInRange(
                new Rectangle2D(block.getTranslateX() - 40,
                        block.getTranslateY() - 40,
                        120, 120),
                        Type.BLOCK.getUniqueType())) {
            if (b.getProperty(Property.SUB_TYPE) == Block.BARRIER
                    && !"dying".equals(b.getProperty("state"))) {
                activateBarrier(b);
            }
        }

        removeEntity(block);
        Entity e = new Entity(Type.PLATFORM);
        e.setPosition(block.getTranslateX(), block.getTranslateY());
        Rectangle rect = new Rectangle(40, 40);
        rect.setFill(Color.GREY);
        e.setGraphics(rect);

        addEntities(e);
    }

    private void destroyBlock(Entity block) {
        block.setProperty("state", "dying");

        for (Entity b : getEntitiesInRange(
                new Rectangle2D(block.getTranslateX() - 40,
                        block.getTranslateY() - 40,
                        120, 120),
                        Type.PLATFORM.getUniqueType())) {
            if (b.getProperty(Property.SUB_TYPE) == Platform.DESTRUCTIBLE
                    && !"dying".equals(b.getProperty("state"))) {
                destroyBlock(b);
            }
        }

        removeEntity(block);
    }

    private void shootProjectile() {
        Element element = playerData.getCurrentPower();

        Entity e = new Entity(Type.PLAYER_PROJECTILE);
        e.setProperty(Property.SUB_TYPE, element);
        e.setPosition(player.getPosition());
        e.setUsePhysics(true);
        e.setGraphics(new Rectangle(10, 1));
        e.addControl(new PhysicsControl(physics));
        e.addControl(new ProjectileControl(facingRight, player));
        e.addFXGLEventHandler(Event.DEATH, event -> {
            removeEntity(event.getTarget());
        });

        e.setProperty(Property.ENABLE_GRAVITY, false);

        addEntities(e);
    }
    
    private void aimedProjectile(Entity source, Entity target){
    	Enemy sourceData = source.getProperty(Property.DATA);
    	Type t = Type.ENEMY_PROJECTILE;
    	if(source == player){
    		t = Type.PLAYER_PROJECTILE;
    	}
    	
    	Entity e = new Entity(t);
    	e.setProperty(Property.SUB_TYPE, sourceData.getElement());
    	e.setPosition(source.getPosition().add(0, source.getHeight()/2));
    	e.setUsePhysics(true);
        e.setGraphics(new Rectangle(10, 1));
        e.addControl(new PhysicsControl(physics));
        e.addControl(new AimedProjectileControl(source, target));
        e.addFXGLEventHandler(Event.DEATH, event -> {
            removeEntity(event.getTarget());
        });

        e.setProperty(Property.ENABLE_GRAVITY, false);

        addEntities(e);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
