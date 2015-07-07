package com.almasb.consume.collision;

import java.util.List;

import javafx.animation.FadeTransition;
import javafx.scene.text.Text;
import javafx.util.Duration;

import com.almasb.consume.Config;
import com.almasb.consume.Types.Element;
import com.almasb.consume.Types.Property;
import com.almasb.fxgl.GameApplication;
import com.almasb.fxgl.entity.CollisionHandler;
import com.almasb.fxgl.entity.Entity;
import com.ergo21.consume.Enemy;

public class ProjectilePlayerHandler implements CollisionHandler {

    private GameApplication app;

    public ProjectilePlayerHandler(GameApplication app) {
        this.app = app;
    }

    @Override
    public void onCollision(Entity player, Entity projectile) {
        Element element = projectile.getProperty(Property.SUB_TYPE);
        Enemy playerData = player.getProperty(Property.DATA);
        if(playerData == null){
        	System.out.println("playerData == null");
        }

        List<Element> resists = playerData.getResistances();
        List<Element> weaknesses = playerData.getWeaknesses();

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
                    .setPosition(player.getTranslateX(), player.getTranslateY())
                    .setGraphics(new Text(damage + "!  " + modifier));

        app.addEntities(e);

        FadeTransition ft = new FadeTransition(Duration.seconds(1.5), e);
        ft.setToValue(0);
        ft.setOnFinished(event -> {
            app.removeEntity(e);
        });
        ft.play();

        playerData.takeDamage(damage);

        app.removeEntity(projectile);

        if (playerData.getCurrentHealth() <= 0) {
            //app.removeEntity(player);
        }
    }
}
