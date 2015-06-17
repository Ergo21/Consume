package com.almasb.consume;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import com.almasb.consume.Types.Block;
import com.almasb.consume.Types.Platform;
import com.almasb.consume.Types.Powerup;
import com.almasb.consume.Types.Property;
import com.almasb.fxgl.entity.Entity;

public class LevelParser {

    private List<LevelData> levels;

    public LevelParser(List<LevelData> levels) {
        this.levels = levels;
    }

    public List<Level> parseAll() {
        return IntStream.range(0, levels.size())
                .mapToObj(this::parse)
                .collect(Collectors.toList());
    }

    private Level parse(int levelNumber) {
        List<String> data = levels.get(levelNumber).data;

        // TODO: range checks

        Level level = new Level();

        level.width = data.get(0).length() * Config.BLOCK_SIZE;
        level.height = data.size() * Config.BLOCK_SIZE;

        for (int i = 0; i < data.size(); i++) {
            String line = data.get(i);
            for (int j = 0; j < line.length(); j++) {
                Rectangle rect = new Rectangle(40, 40);
                Entity e = null;

                switch (line.charAt(j)) {
                    case ' ':
                        break;
                    case '0':
                        break;
                    case '1':
                        level.spawnPoint = new Point2D(j*Config.BLOCK_SIZE, i*Config.BLOCK_SIZE);
                        break;
                    case '2':
                        e = new Entity(Types.Type.NEXT_LEVEL_POINT);
                        e.setUsePhysics(true);
                        level.nextLevelEntity = e;
                        rect.setFill(Color.BLACK);
                        break;
                    case 'b':
                        e = new Entity(Types.Type.BLOCK);
                        e.setProperty(Property.SUB_TYPE, Block.BARRIER);
                        e.setProperty("state", "idle");
                        e.setProperty("start", "none");
                        e.setUsePhysics(true);
                        rect.setFill(Color.BLUE);
                        break;
                    case 'd':
                        e = new Entity(Types.Type.PLATFORM);
                        e.setProperty(Property.SUB_TYPE, Platform.DESTRUCTIBLE);
                        e.setProperty("state", "none");
                        rect.setFill(Color.DARKGREEN);
                        break;
                    case 'i':
                        e = new Entity(Types.Type.PLATFORM);
                        e.setProperty(Property.SUB_TYPE, Platform.INDESTRUCTIBLE);
                        rect.setFill(Color.BROWN);
                        break;
                    case 'p':
                        e = new Entity(Types.Type.BLOCK);
                        e.setProperty(Property.SUB_TYPE, Block.LADDER);
                        rect.setFill(Color.GREY);
                        break;
                    case 'u':
                        e = new Entity(Types.Type.POWERUP);
                        e.setProperty(Property.SUB_TYPE, Powerup.INC_MAX_HEALTH);
                        e.setUsePhysics(true);
                        rect.setFill(Color.PURPLE);
                        break;
                    case 'U':
                        e = new Entity(Types.Type.POWERUP);
                        e.setProperty(Property.SUB_TYPE, Powerup.INC_MAX_MANA);
                        e.setUsePhysics(true);
                        rect.setFill(Color.PURPLE);
                        break;
                    case 'v':
                        e = new Entity(Types.Type.POWERUP);
                        e.setProperty(Property.SUB_TYPE, Powerup.INC_MANA_REGEN);
                        e.setUsePhysics(true);
                        rect.setFill(Color.PURPLE);
                        break;
                }

                if (e != null) {
                    e.setPosition(j*Config.BLOCK_SIZE, i*Config.BLOCK_SIZE);
                    e.setGraphics(rect);
                    level.entities.add(e);
                }
            }
        }

        if (!level.isValid())
            throw new IllegalArgumentException("Level: " + levelNumber + " was not parsed as valid");

        return level;
    }

    public static class LevelData {
        private List<String> data;
        public LevelData(List<String> data) {
            this.data = data;
        }

        public List<String> getData() {
            return data;
        }
    }

    public static class Level {
        private int width, height;
        private List<Entity> entities = new ArrayList<>();
        private Point2D spawnPoint;
        private Entity nextLevelEntity;

        /* package-private */ Level() {

        }

        private boolean isValid() {
            return spawnPoint != null && nextLevelEntity != null
                    && width != 0 && height != 0;
        }

        public List<Entity> getEntities() {
            return entities;
        }

        public Entity[] getEntitiesAsArray() {
            return entities.toArray(new Entity[0]);
        }

        public Point2D getSpawnPoint() {
            return spawnPoint;
        }

        public Entity getNextLevelEntity() {
            return nextLevelEntity;
        }
    }
}
