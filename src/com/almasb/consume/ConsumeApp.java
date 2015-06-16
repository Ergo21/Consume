package com.almasb.consume;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.animation.FadeTransition;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import com.almasb.consume.Config.Speed;
import com.almasb.consume.LevelParser.Level;
import com.almasb.consume.LevelParser.LevelData;
import com.almasb.consume.Types.Block;
import com.almasb.consume.Types.Element;
import com.almasb.consume.Types.Powerup;
import com.almasb.consume.Types.Property;
import com.almasb.consume.Types.Type;
import com.almasb.consume.ai.ChargeControl;
import com.almasb.consume.ai.PhysicsControl;
import com.almasb.consume.ai.ProjectileControl;
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

    private Entity player = new Entity(Type.PLAYER);
    private Player playerData;
    private boolean facingRight = true;

    private Physics physics = new Physics();

    private List<Level> levels;
    private int currentLevel = 0;

    private PlayerHUD hud;
    private Text performance = new Text();

    private Random random = new Random();

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

        initPlayer();
        initLevels();
        initCollisions();
        bindViewportOrigin(player, 320, 180);

        loadNextLevel();
    }

    private Text debug = new Text();

    @Override
    protected void initUI(Pane uiRoot) {
        GameScene scene = new GameScene(assets.getText("dialogue/scene_0.txt"), assets);
        uiRoot.getChildren().add(scene);

        addKeyTypedBinding(KeyCode.ENTER, scene::updateScript);

        hud = new PlayerHUD(player.<Player>getProperty(Property.DATA).getMaxHealth(),
                player.<Player>getProperty(Property.DATA).getMaxMana());

        hud.setTranslateX(10);
        hud.setTranslateY(100);

        uiRoot.getChildren().add(hud);


        performance.setTranslateX(450);
        performance.setTranslateY(50);
        performance.setFill(Color.BLACK);

        uiRoot.getChildren().add(performance);

        debug.setTranslateX(450);
        debug.setTranslateY(100);
        uiRoot.getChildren().add(debug);
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
        addCollisionHandler(Type.PLAYER, Type.POWERUP, (playerEntity, powerup) -> {
            removeEntity(powerup);

            Powerup type = powerup.getProperty(Property.SUB_TYPE);
            Player playerData = playerEntity.getProperty(Property.DATA);
            switch (type) {
                case INC_MANA_REGEN:
                    playerData.increaseManaRegen(Config.MANA_REGEN_INC);
                    break;
                case INC_MAX_HEALTH:
                    playerData.increaseMaxHealth(Config.MAX_HEALTH_INC);
                    break;
                case INC_MAX_MANA:
                    playerData.increaseMaxMana(Config.MAX_MANA_INC);
                    break;
                case RESTORE_HEALTH_12:
                    playerData.restoreHealth(0.125);
                    break;
                case RESTORE_HEALTH_25:
                    playerData.restoreHealth(0.25);
                    break;
                case RESTORE_HEALTH_50:
                    playerData.restoreHealth(0.5);
                    break;
                case RESTORE_MANA_12:
                    playerData.restoreMana(0.125);
                    break;
                case RESTORE_MANA_25:
                    playerData.restoreMana(0.25);
                    break;
                case RESTORE_MANA_50:
                    playerData.restoreMana(0.5);
                    break;
                default:
                    System.out.println("Picked up an unknown powerup: " + type);
                    break;
            }
        });

        addCollisionHandler(Type.PLAYER, Type.ENEMY, (player, enemy) -> {
            if (enemy.getControl(ChargeControl.class) != null) {
                int velocityX = enemy.getControl(ChargeControl.class).getVelocity();
                player.getControl(PhysicsControl.class).moveX(velocityX * 5);

                enemy.fireFXGLEvent(new FXGLEvent(Event.ENEMY_HIT_PLAYER));

                player.setUsePhysics(false);
                Entity e = Entity.noType().setGraphics(new Text("INVINCIBLE"));
                e.translateXProperty().bind(player.translateXProperty());
                e.translateYProperty().bind(player.translateYProperty().subtract(20));

                addEntities(e);

                runOnceAfter(() -> {
                    removeEntity(e);
                    player.setUsePhysics(true);
                }, 2 * SECOND);
            }
        });

        addCollisionHandler(Type.PLAYER, Type.BLOCK, (player, block) -> {
            if (block.getProperty(Property.SUB_TYPE) == Block.BARRIER) {
                block.setProperty("state", "passing");

                if ("none".equals(block.getProperty("start"))) {
                    if (player.getTranslateX() <= block.getTranslateX()) {
                        block.setProperty("start", "left");
                    }
                    else {
                        block.setProperty("start", "right");
                    }
                }

            }
        });

        addCollisionHandler(Type.PROJECTILE, Type.ENEMY, (proj, enemy) -> {
            Element element = proj.getProperty(Property.SUB_TYPE);
            Enemy enemyData = enemy.getProperty(Property.DATA);

            List<Element> resists = enemyData.getResistances();
            List<Element> weaknesses = enemyData.getWeaknesses();

            int damage = Config.POWER_DAMAGE;
            String modifier = "x1";

            if (resists.contains(element)) {
                damage = (int)(damage * 0.5);
                modifier = "x0.5";
            }
            else if (weaknesses.contains(element)) {
                damage *= 2;
                modifier = "x2";
            }

            Entity e = Entity.noType()
                        .setPosition(enemy.getTranslateX(), enemy.getTranslateY())
                        .setGraphics(new Text(damage + "!  " + modifier));

            addEntities(e);

            FadeTransition ft = new FadeTransition(Duration.seconds(1.5), e);
            ft.setToValue(0);
            ft.setOnFinished(event -> {
                removeEntity(e);
            });
            ft.play();

            enemyData.takeDamage(damage);

            removeEntity(proj);

            if (enemyData.getCurrentHealth() <= 0) {
                removeEntity(enemy);
            }
        });

        addCollisionHandler(Type.PLAYER, Type.NEXT_LEVEL_POINT, (player, point) -> {
            loadNextLevel();
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
            player.getControl(PhysicsControl.class).jump();
        });

        addKeyTypedBinding(KeyCode.Q, () -> {
            Element element = playerData.getCurrentPower();

            Entity e = new Entity(Type.PROJECTILE);
            e.setProperty(Property.SUB_TYPE, element);
            e.setPosition(player.getTranslateX(), player.getTranslateY());
            e.setUsePhysics(true);
            e.setGraphics(new Rectangle(10, 1));
            e.addControl(new PhysicsControl(physics));
            e.addControl(new ProjectileControl(facingRight, player));
            e.addFXGLEventHandler(Event.DEATH, event -> {
                removeEntity(event.getTarget());
            });
            e.addFXGLEventHandler(Event.COLLIDED_PLATFORM, event -> {
                removeEntity(event.getTarget());
            });

            e.setProperty(Property.ENABLE_GRAVITY, false);

            addEntities(e);
        });

        // debug
        addKeyTypedBinding(KeyCode.L, () -> {
            getEntitiesInRange(new Rectangle2D(player.getTranslateX() - 50, player.getTranslateY() - 50, 200, 200), Type.PLATFORM.getUniqueType())
                .forEach(e -> System.out.println(e.getType()));
        });

        addKeyTypedBinding(KeyCode.I, () -> {
            Player p = player.getProperty(Property.DATA);
            System.out.println(p.toString());
        });
    }

    @Override
    protected void onUpdate(long now) {
        Player p = player.getProperty(Property.DATA);
        if (p != null) {

            if (now - regenTime >= Config.REGEN_TIME_INTERVAL) {
                p.regenMana();
                regenTime = now;
            }

            hud.setCurHealth(p.getCurrentHealth());
            hud.setCurMana(p.getCurrentMana());
            hud.setMaxHealth(p.getMaxHealth());
            hud.setMaxMana(p.getMaxMana());
        }

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
        }

        for (Entity e : getEntities(Type.BLOCK)) {
            if (e.getProperty(Property.SUB_TYPE) == Block.BARRIER)
                e.setProperty("state", "idle");
        }

        performance.setText("FPS: " + fps + " Performance: " + fpsPerformance);
        debug.setText("Debug text goes here");
    }

    private void loadNextLevel() {
        getAllEntities().stream().filter(e -> !e.isType(Type.PLAYER)).forEach(this::removeEntity);

        Level level = levels.get(currentLevel++);

        addEntities(level.getEntitiesAsArray());

        Point2D spawnPoint = level.getSpawnPoint();
        player.setPosition(spawnPoint.getX(), spawnPoint.getY());

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
    }

    public class Physics {
        /**
         * Returns true iff entity has moved value units
         *
         * @param e
         * @param value
         * @return
         */
        public boolean moveX(Entity e, int value) {
            boolean movingRight = value > 0;

            for (int i = 0; i < Math.abs(value); i++) {
                for (Entity platform : getEntities(Type.PLATFORM)) {
                    if (e.getBoundsInParent().intersects(platform.getBoundsInParent())) {
                        if (movingRight) {
                            if (e.getTranslateX() + e.getWidth() == platform.getTranslateX()) {
                                e.fireFXGLEvent(new FXGLEvent(Event.COLLIDED_PLATFORM, platform));
                                e.translate(-1, 0);
                                return false;
                            }
                        }
                        else {
                            if (e.getTranslateX() == platform.getTranslateX() + platform.getWidth()) {
                                e.fireFXGLEvent(new FXGLEvent(Event.COLLIDED_PLATFORM, platform));
                                e.translate(1, 0);
                                return false;
                            }
                        }
                    }
                }
                e.setTranslateX(e.getTranslateX() + (movingRight ? 1 : -1));
            }

            return true;
        }

        public void moveY(Entity e, int value) {
            boolean movingDown = value > 0;

            for (int i = 0; i < Math.abs(value); i++) {
                for (Entity platform : getEntities(Type.PLATFORM)) {
                    if (e.getBoundsInParent().intersects(platform.getBoundsInParent())) {
                        if (movingDown) {
                            if (e.getTranslateY() + e.getHeight() == platform.getTranslateY()) {
                                e.fireFXGLEvent(new FXGLEvent(Event.COLLIDED_PLATFORM, platform));
                                e.setTranslateY(e.getTranslateY() - 1);
                                e.setProperty("jumping", false);
                                return;
                            }
                        }
                        else {
                            if (e.getTranslateY() == platform.getTranslateY() + platform.getHeight()) {
                                e.fireFXGLEvent(new FXGLEvent(Event.COLLIDED_PLATFORM, platform));
                                return;
                            }
                        }
                    }
                }
                e.setTranslateY(e.getTranslateY() + (movingDown ? 1 : -1));
                e.setProperty("jumping", true);
            }
        }
    }

    private void initPlayer() {
        Rectangle graphics = new Rectangle(15, 30);
        graphics.setFill(Color.YELLOW);

        player.setUsePhysics(true)
            .setGraphics(graphics)
            .setProperty(Property.DATA, playerData);

        player.addControl(new PhysicsControl(physics));
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

    public static void main(String[] args) {
        launch(args);
    }
}
