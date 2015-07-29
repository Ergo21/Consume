package com.almasb.consume.collision;

import java.util.List;

import com.almasb.consume.Config;
import com.almasb.consume.Types.Element;
import com.almasb.consume.Types.Property;
import com.almasb.consume.Types.Type;
import com.almasb.fxgl.GameApplication;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.CollisionHandler;
import com.ergo21.consume.Enemy;

import javafx.animation.FadeTransition;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class ProjectileEnemyHandler extends CollisionHandler {

    private GameApplication app;

    public ProjectileEnemyHandler(GameApplication app) {
        super(Type.PLAYER_PROJECTILE, Type.ENEMY);
        this.app = app;
    }

    @Override
    public void onCollision(Entity projectile, Entity enemy) {
        Element element = projectile.getProperty(Property.SUB_TYPE);
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

        app.addEntities(e);

        FadeTransition ft = new FadeTransition(Duration.seconds(1.5), e);
        ft.setToValue(0);
        ft.setOnFinished(event -> {
            app.removeEntity(e);
        });
        ft.play();

        enemyData.takeDamage(damage);

        app.removeEntity(projectile);

        if (enemyData.getCurrentHealth() <= 0) {
            app.removeEntity(enemy);
        }
    }

    @Override
    public void onCollisionBegin(Entity a, Entity b) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onCollisionEnd(Entity a, Entity b) {
        // TODO Auto-generated method stub

    }
}
