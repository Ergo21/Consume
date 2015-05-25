package com.almasb.consume;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

import com.almasb.fxgl.GameApplication;
import com.almasb.fxgl.GameSettings;

public class ConsumeApp extends GameApplication {

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Consume");
        settings.setVersion("0.0.1dev");
        settings.setWidth(1280);
        settings.setHeight(720);
    }

    @Override
    protected void initAssets() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    protected void initMainMenu(Pane mainMenuRoot) {
        Rectangle bg = new Rectangle(1280, 720);

        Font font = Font.font(48);

        Button btnContinue = new Button("CONTINUE");
        btnContinue.setDisable(true);
        btnContinue.setFont(font);

        Button btnStart = new Button("START");
        btnStart.setFont(font);
        btnStart.setOnAction(event -> startGame());

        Button btnLoad = new Button("LOAD");
        btnLoad.setFont(font);

        Button btnExit = new Button("EXIT");
        btnExit.setFont(font);
        btnExit.setOnAction(event -> exit());

        VBox vbox = new VBox(20, btnContinue, btnStart, btnLoad, btnExit);
        vbox.setAlignment(Pos.CENTER);
        vbox.setTranslateX(500);
        vbox.setTranslateY(150);

        mainMenuRoot.getChildren().addAll(bg, vbox);
    }

    @Override
    protected void initGame(Pane gameRoot) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void initUI(Pane uiRoot) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void initInput() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onUpdate(long now) {
        // TODO Auto-generated method stub

    }

    public static void main(String[] args) {
        launch(args);
    }
}
