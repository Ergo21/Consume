package com.almasb.consume;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import com.almasb.consume.Types.Block;
import com.almasb.consume.Types.Powerup;
import com.almasb.consume.Types.Property;
import com.almasb.fxgl.entity.Entity;

public class LevelParser {

    private List<LevelData> levels;

    public LevelParser(List<LevelData> levels) {
        this.levels = levels;
    }

    public Level parse(int level) {
        if (level >= levels.size())
            throw new IllegalArgumentException("There are only " + levels.size()
                    + " levels. Attempted to load level: " + level);

        List<String> data = levels.get(level).data;

        // TODO: range checks

        int width = data.get(0).length() * Config.BLOCK_SIZE;
        int height = data.size() * Config.BLOCK_SIZE;
        List<Entity> entities = new ArrayList<>();

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
                        e = new Entity(Types.Type.SPAWN_POINT);
                        rect.setFill(Color.TRANSPARENT);
                        break;
                    case '2':
                        e = new Entity(Types.Type.NEXT_LEVEL_POINT);
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
                        rect.setFill(Color.DARKGREEN);
                        break;
                    case 'i':
                        e = new Entity(Types.Type.PLATFORM);
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
                    entities.add(e);
                }
            }
        }

        return new Level(width, height, entities);
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
        private List<Entity> entities;

        public Level(int width, int height, List<Entity> entities) {
            this.width = width;
            this.height = height;
            this.entities = entities;
        }

        public List<Entity> getEntities() {
            return entities;
        }
    }
}
