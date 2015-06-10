package com.almasb.consume;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import com.almasb.consume.Config.Speed;
import com.almasb.consume.LevelParser.Level;
import com.almasb.consume.LevelParser.LevelData;
import com.almasb.consume.Types.Powerup;
import com.almasb.consume.Types.Property;
import com.almasb.consume.Types.Type;
import com.almasb.consume.ai.PatrolControl;
import com.almasb.consume.ai.SeekControl;
import com.almasb.fxgl.GameApplication;
import com.almasb.fxgl.GameSettings;
import com.almasb.fxgl.asset.Assets;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.ergo21.consume.GameScene;
import com.ergo21.consume.Player;
import com.ergo21.consume.PlayerHUD;

public class ConsumeApp extends GameApplication {

    private Assets assets;

    private Entity player = new Entity(Type.PLAYER);

    private Physics physics = new Physics();

    private int currentLevel = 0;

    private PlayerHUD hud;

    private Random random = new Random();

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
    protected void initMainMenu(Pane mainMenuRoot) {
//        Rectangle bg = new Rectangle(1280, 720);
//
//        Font font = Font.font(48);
//
//        Button btnContinue = new Button("CONTINUE");
//        btnContinue.setDisable(true);
//        btnContinue.setFont(font);
//
//        Button btnStart = new Button("START");
//        btnStart.setFont(font);
//        btnStart.setOnAction(event -> startGame());
//
//        Button btnLoad = new Button("LOAD");
//        btnLoad.setFont(font);
//        btnLoad.setOnAction(event-> {mainMenuRoot.setLayoutX(mainMenuRoot.getLayoutX()-mainMenuRoot.getWidth()/4);
//        mainMenuRoot.setLayoutY(mainMenuRoot.getLayoutY()-mainMenuRoot.getHeight()/4);});
//
//        Button btnExit = new Button("EXIT");
//        btnExit.setFont(font);
//        btnExit.setOnAction(event -> exit());
//
//        VBox vbox = new VBox(20, btnContinue, btnStart, btnLoad, btnExit);
//        vbox.setAlignment(Pos.CENTER);
//        vbox.setTranslateX(500);
//        vbox.setTranslateY(150);
//
//        mainMenuRoot.setScaleX(0.5);
//        mainMenuRoot.setScaleY(0.5);
//
//        mainMenuRoot.getChildren().addAll(bg, vbox);
    }

    @Override
    protected void initGame(Pane gameRoot) {
        List<LevelData> levelData = new ArrayList<>();
        for (int i = 0; i < Config.MAX_LEVELS; i++) {
            LevelData data = new LevelData(assets.getText("levels/level_" + i + ".txt"));
            levelData.add(data);
        }

        LevelParser parser = new LevelParser(levelData);

        Level level = parser.parse(0);

        List<Entity> entities = level.getEntities();
        addEntities(entities.toArray(new Entity[0]));

        Entity spawnPoint = entities.stream().filter(e -> e.isType(Type.SPAWN_POINT)).findAny().get();
        spawnPlayer(spawnPoint.getPosition());

        bindViewportOrigin(player, 320, 180);

        ////////////////////////////////////////////////////////////////////////////////////////////

        Entity testEnemy = new Entity(Type.ENEMY);

        Rectangle rect = new Rectangle(30, 30);
        rect.setFill(Color.RED);

        testEnemy.setGraphics(rect);
        testEnemy.setUsePhysics(true);
        testEnemy.setProperty("jumping", false);
        testEnemy.setProperty("velocity", new Point2D(0, 0));
        testEnemy.setProperty("physics", physics);
        testEnemy.setPosition(spawnPoint.getTranslateX() + 80, spawnPoint.getTranslateY() + 10);
        testEnemy.addControl(new PatrolControl(player.getPosition()));
        testEnemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);

        addEntities(testEnemy);

        testEnemy = new Entity(Type.ENEMY);

        rect = new Rectangle(30, 30);
        rect.setFill(Color.RED);

        testEnemy.setGraphics(rect);
        testEnemy.setUsePhysics(true);
        testEnemy.setProperty("jumping", false);
        testEnemy.setProperty("velocity", new Point2D(0, 0));
        testEnemy.setProperty("physics", physics);
        testEnemy.setPosition(spawnPoint.getTranslateX() + 240, spawnPoint.getTranslateY() + 10);
        testEnemy.addControl(new SeekControl(player));
        testEnemy.addFXGLEventHandler(Event.DEATH, this::onEnemyDeath);

        addEntities(testEnemy);

        Entity nextPoint = entities.stream().filter(e -> e.isType(Type.NEXT_LEVEL_POINT)).findAny().get();
        nextPoint.setUsePhysics(true);

        addCollisionHandler(Type.PLAYER, Type.NEXT_LEVEL_POINT, (player, point) -> {
            getAllEntities().forEach(this::removeEntity);
            player.removeControls();

            runOnceAfter(() -> {
                Level l = parser.parse(++currentLevel);

                List<Entity> ent = l.getEntities();
                addEntities(ent.toArray(new Entity[0]));

                Entity s = ent.stream().filter(e -> e.isType(Type.SPAWN_POINT)).findAny().get();
                spawnPlayer(s.getPosition());
            }, 1 * SECOND);
        });

        initCollisions();
    }

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

                    // TODO: add missing cases

                default:
                    System.out.println("Picked up a powerup: " + type);
                    break;
            }
        });

        // just a test
        addCollisionHandler(Type.PLAYER, Type.ENEMY, (player, enemy) -> {
            enemy.fireFXGLEvent(new FXGLEvent(Event.DEATH));
        });
    }

    @Override
    protected void initInput() {
        addKeyPressBinding(KeyCode.A, () -> {
            movePlayerX(-Speed.PLAYER_MOVE);
        });
        addKeyPressBinding(KeyCode.D, () -> {
            movePlayerX(Speed.PLAYER_MOVE);
        });
        addKeyPressBinding(KeyCode.W, () -> {
            if (player.<Boolean>getProperty("jumping"))
                return;

            player.setProperty("jumping", true);

            Point2D velocity = player.getProperty("velocity");

            player.setProperty("velocity", velocity.add(0, -Speed.PLAYER_JUMP));
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
            hud.setCurHealth(p.getCurrentHealth());
            hud.setCurMana(p.getCurrentMana());
            hud.setMaxHealth(p.getMaxHealth());
            hud.setMaxMana(p.getMaxMana());
        }
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
                                return false;
                            }
                        }
                        else {
                            if (e.getTranslateX() == platform.getTranslateX() + platform.getWidth()) {
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
                                e.setTranslateY(e.getTranslateY() - 1);
                                e.setProperty("jumping", false);
                                return;
                            }
                        }
                        else {
                            if (e.getTranslateY() == platform.getTranslateY() + platform.getHeight()) {
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

    private void movePlayerX(int value) {
        boolean movingRight = value > 0;

        for (int i = 0; i < Math.abs(value); i++) {
            for (Entity platform : getEntities(Type.PLATFORM)) {
                if (player.getBoundsInParent().intersects(platform.getBoundsInParent())) {
                    if (movingRight) {
                        if (player.getTranslateX() + player.getWidth() == platform.getTranslateX()) {
                            return;
                        }
                    }
                    else {
                        if (player.getTranslateX() == platform.getTranslateX() + platform.getWidth()) {
                            return;
                        }
                    }
                }
            }
            player.setTranslateX(player.getTranslateX() + (movingRight ? 1 : -1));
        }
    }

    private void movePlayerY(int value) {
        boolean movingDown = value > 0;

        for (int i = 0; i < Math.abs(value); i++) {
            for (Entity platform : getEntities(Type.PLATFORM)) {
                if (player.getBoundsInParent().intersects(platform.getBoundsInParent())) {
                    if (movingDown) {
                        if (player.getTranslateY() + player.getHeight() == platform.getTranslateY()) {
                            player.setTranslateY(player.getTranslateY() - 1);
                            player.setProperty("jumping", false);
                            return;
                        }
                    }
                    else {
                        if (player.getTranslateY() == platform.getTranslateY() + platform.getHeight()) {
                            return;
                        }
                    }
                }
            }
            player.setTranslateY(player.getTranslateY() + (movingDown ? 1 : -1));
            player.setProperty("jumping", true);
        }
    }

    private void spawnPlayer(Point2D p) {
        Rectangle graphics = new Rectangle(15, 30);
        graphics.setFill(Color.YELLOW);

        player.setPosition(p.getX(), p.getY())
            .setUsePhysics(true)
            .setGraphics(graphics)
            .setProperty("jumping", false)
            .setProperty("velocity", new Point2D(0, 0))
            .setProperty(Property.DATA, new Player(assets.getText("enemies/enemy_Mook.txt")));

        player.addControl((entity, now) -> {
            Point2D velocity = entity.getProperty("velocity");
            velocity = velocity.add(0, Speed.GRAVITY_ACCEL);
            if (velocity.getY() > Speed.GRAVITY_MAX)
                velocity = new Point2D(velocity.getX(), Speed.GRAVITY_MAX);

            entity.setProperty("velocity", velocity);

            movePlayerY((int)velocity.getY());
        });
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

    public static void main(String[] args) {
        launch(args);
    }
}
