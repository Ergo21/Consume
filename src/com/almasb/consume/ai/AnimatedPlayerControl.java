package com.almasb.consume.ai;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import com.almasb.consume.ConsumeApp;
import com.almasb.fxgl.GameApplication;
import com.almasb.fxgl.asset.Texture;
import com.almasb.fxgl.entity.Control;
import com.almasb.fxgl.entity.Entity;

public class AnimatedPlayerControl implements Control {
    
    private Texture spritesheet;
    
    private enum CurAnim{IDLER, IDLEL, MOVER, MOVEL};
    
    private CurAnim current;

    public AnimatedPlayerControl(Texture t) {
    	spritesheet = t;
    }

    @Override
    public void onUpdate(Entity entity, long now) {
        PhysicsControl pc = entity.getControl(PhysicsControl.class);
        if(pc != null){
        	//System.out.println("Update X: " + pc.getVelocity().getX() + ", Y: " + pc.getVelocity().getY());
        	if(pc.getVelocity().getX() == 0){
        		if(current != CurAnim.IDLER && entity.<Boolean>getProperty("facingRight")){
        			System.out.println("Update");
        			ImageView t = subTexture(new Rectangle2D(150,0,90,30), spritesheet);
        			LocalAnimatedTexture sAT = new LocalAnimatedTexture(t.getImage(), 3, ConsumeApp.SECOND);
        			entity.setGraphics(sAT);
        			current = CurAnim.IDLER;
        		}
        		else if (current != CurAnim.IDLEL && !entity.<Boolean>getProperty("facingRight")){
        			ImageView t = subTexture(new Rectangle2D(60,0,90,30), spritesheet);
        			LocalAnimatedTexture sAT = new LocalAnimatedTexture(t.getImage(), 3, ConsumeApp.SECOND);
        			entity.setGraphics(sAT);
        			current = CurAnim.IDLEL;
        		}
        	}
        	else if (pc.getVelocity().getX() != 0){
        		if(current != CurAnim.MOVER && entity.<Boolean>getProperty("facingRight")){
        			ImageView t = subTexture(new Rectangle2D(180,30,120,30), spritesheet);
        			LocalAnimatedTexture sAT = new LocalAnimatedTexture(t.getImage(), 4, ConsumeApp.SECOND);
        			entity.setGraphics(sAT);
        			current = CurAnim.MOVER;
        		}
        		else if (current != CurAnim.MOVEL && !entity.<Boolean>getProperty("facingRight")){
        			ImageView t = subTexture(new Rectangle2D(0,30,120,30), spritesheet);
        			LocalAnimatedTexture sAT = new LocalAnimatedTexture(t.getImage(), 4, ConsumeApp.SECOND);
        			entity.setGraphics(sAT);
        			current = CurAnim.MOVEL;
        		}
        	}
        }
        
    }
    
    public ImageView subTexture(Rectangle2D area, Texture ss) {
        int minX = (int) area.getMinX();
        int minY = (int) area.getMinY();
        int maxX = (int) area.getMaxX();
        int maxY = (int) area.getMaxY();

        if (minX < 0)
            throw new IllegalArgumentException("minX value of sub-texture cannot be negative");
        if (minY < 0)
            throw new IllegalArgumentException("minY value of sub-texture cannot be negative");
        if (maxX > ss.getImage().getWidth())
            throw new IllegalArgumentException("maxX value of sub-texture cannot be greater than image width");
        if (maxY > ss.getImage().getHeight())
            throw new IllegalArgumentException("maxY value of sub-texture cannot be greater than image height");

        PixelReader pixelReader = ss.getImage().getPixelReader();
        WritableImage image = new WritableImage(maxX - minX, maxY - minY);
        PixelWriter pixelWriter = image.getPixelWriter();

        for (int y = minY; y < maxY; y++) {
            for (int x = minX; x < maxX; x++) {
                Color color = pixelReader.getColor(x, y);
                pixelWriter.setColor(x - minX, y - minY, color);
            }
        }

        return new ImageView(image);
    }
    
    public final class LocalAnimatedTexture extends ImageView {

        private Timeline timeline;

        /**
         *
         * @param image     actual image
         * @param frames    number of frames in spritesheet
         * @param duration duration of the animation
         *      use GameApplication.SECOND * n for convenience
         */
        private LocalAnimatedTexture(Image image, int frames, double duration) {
            super(image);

            final double frameW = image.getWidth() / frames;

            this.setFitWidth(frameW);
            this.setFitHeight(image.getHeight());

            this.setViewport(new Rectangle2D(0, 0, frameW, image.getHeight()));

            SimpleIntegerProperty frameProperty = new SimpleIntegerProperty();
            frameProperty.addListener((obs, old, newValue) -> {
                this.setViewport(new Rectangle2D(newValue.intValue() * frameW, 0, frameW, image.getHeight()));
            });

            timeline = new Timeline(new KeyFrame(Duration.seconds(duration / GameApplication.SECOND), new KeyValue(frameProperty, frames - 1)));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
        }
    }
}
