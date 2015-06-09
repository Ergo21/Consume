package com.almasb.consume;

import java.util.Arrays;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

import com.almasb.consume.LevelParser.Level;
import com.almasb.consume.LevelParser.LevelData;
import com.almasb.consume.Types.Type;
import com.almasb.consume.ai.SeekControl;
import com.almasb.fxgl.GameApplication;
import com.almasb.fxgl.GameSettings;
import com.almasb.fxgl.asset.Assets;
import com.almasb.fxgl.entity.Entity;

public class ConsumeApp extends GameApplication {

    private Assets assets;

    private Entity player;
    private Entity testEnemy;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Consume");
        settings.setVersion("0.0.1dev");
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
        LevelData levelData = new LevelData(assets.getText("levels/level_0.txt"));
        LevelParser parser = new LevelParser(Arrays.asList(levelData));

        Level level = parser.parse(0);

        List<Entity> entities = level.getEntities();
        addEntities(entities.toArray(new Entity[0]));

        Entity spawnPoint = entities.stream().filter(e -> e.isType(Type.SPAWN_POINT)).findAny().get();
        spawnPlayer(spawnPoint.getPosition());

        bindViewportOrigin(player, 320, 180);

        testEnemy = new Entity(Type.ENEMY);

        Rectangle rect = new Rectangle(30, 30);
        rect.setFill(Color.RED);

        testEnemy.setGraphics(rect);
        testEnemy.setPosition(spawnPoint.getTranslateX() + 80, spawnPoint.getTranslateY() + 10);
        testEnemy.addControl(new SeekControl(player));

        addEntities(testEnemy);
    }

    @Override
    protected void initUI(Pane uiRoot) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void initInput() {
        addKeyPressBinding(KeyCode.A, () -> {
            movePlayerX(-5);
        });
        addKeyPressBinding(KeyCode.D, () -> {
            movePlayerX(5);
        });
        addKeyPressBinding(KeyCode.W, () -> {
            if (player.<Boolean>getProperty("jumping"))
                return;

            player.setProperty("jumping", true);

            Point2D velocity = player.getProperty("velocity");

            player.setProperty("velocity", velocity.add(0, -30));
        });

        // debug
        addKeyTypedBinding(KeyCode.L, () -> {
            getEntitiesInRange(new Rectangle2D(player.getTranslateX() - 50, player.getTranslateY() - 50, 200, 200), Type.PLATFORM.getUniqueType())
                .forEach(e -> System.out.println(e.getType()));
        });
    }

    @Override
    protected void onUpdate(long now) {
        // TODO Auto-generated method stub

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

        player = new Entity(Type.PLAYER)
                    .setPosition(p.getX(), p.getY())
                    .setUsePhysics(true)
                    .setGraphics(graphics)
                    .setProperty("jumping", false)
                    .setProperty("velocity", new Point2D(0, 0));

        player.addControl((entity, now) -> {
            Point2D velocity = entity.getProperty("velocity");
            velocity = velocity.add(0, 1);
            if (velocity.getY() > 10)
                velocity = new Point2D(velocity.getX(), 10);

            entity.setProperty("velocity", velocity);

            movePlayerY((int)velocity.getY());
        });
        addEntities(player);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
