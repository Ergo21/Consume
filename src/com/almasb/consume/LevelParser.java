package com.almasb.consume;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

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

        int width = data.get(0).length() * 40;
        int height = data.size() * 40;
        List<Entity> entities = new ArrayList<>();

        for (int i = 0; i < data.size(); i++) {
            String line = data.get(i);
            for (int j = 0; j < data.get(0).length(); j++) {
                Rectangle rect = new Rectangle(40, 40);
                Entity e = null;

                switch (line.charAt(j)) {
                    case ' ':
                        break;
                    case '0':
                        break;
                    case '1':
                        break;
                    case '2':
                        break;
                    case 'd':
                        e = new Entity(Type.DESTRUCTIBLE_BLOCK);
                        rect.setFill(Color.DARKGREEN);
                        break;
                    case 'i':
                        e = new Entity(Type.INDESTRUCTIBLE_BLOCK);
                        rect.setFill(Color.BROWN);
                        break;
                    case 'p':
                        e = new Entity(Type.LADDER);
                        rect.setFill(Color.GREY);
                        break;
                    case 'u':
                        e = new Entity(Type.INCREASE_MAX_HEALTH);
                        rect.setFill(Color.PINK);
                        break;
                    case 'U':
                        e = new Entity(Type.INCREASE_MAX_MANA);
                        rect.setFill(Color.PINK);
                        break;
                    case 'v':
                        e = new Entity(Type.INCREASE_MANA_REGEN);
                        rect.setFill(Color.PINK);
                        break;
                }

                if (e != null) {
                    e.setPosition(j*40, i*40);
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
