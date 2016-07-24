package com.ergo21.consume;

import com.almasb.consume.ConsumeApp;
import com.almasb.fxgl.asset.Music;
import com.almasb.fxgl.ui.Intro;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

public final class ConsumeIntro extends Intro {
    private double w, h;

    private ConsumeApp consApp;
    private Timeline blinker;
    private Timeline timeline;

    public ConsumeIntro(ConsumeApp cA, double w, double h) {
        this.w = w;
        this.h = h;
        consApp = cA;
        
        //HBox ergoText = makeText("ErgoScrit" + "\u25AE");
        //ergoText.setPadding(new Insets(-1));
        //ergoText.setAlignment(Pos.CENTER);
        
        String company = "ErgoScrit";
        
        Text typed = new Text("ErgoScrit");
        typed.setFont(Font.font("Lucida Console", FontWeight.NORMAL, 48));
        typed.setFill(Color.LIGHTGRAY);
        
        Text cursor = new Text("" + "\u25AE");
        cursor.setFont(Font.font("Lucida Console", FontWeight.NORMAL, 48));
        cursor.setFill(Color.LIGHTGRAY);
        
        blinker = createBlinker(cursor);

        HBox ergoText = new HBox(typed, cursor);
        getChildren().addAll(new Rectangle(this.w, this.h), ergoText);
        ergoText.setTranslateX(w/2 - typed.getLayoutBounds().getWidth()/2);
        ergoText.setTranslateY(h/2 - typed.getLayoutBounds().getHeight()/2);
        typed.setText("");
        
        timeline = new Timeline();
        for(int i = 0; i < company.length(); i++){        	
        	timeline.getKeyFrames().add(
        			new KeyFrame(
        					Duration.seconds(0.25*(i+1)),
        					new KeyValue(typed.textProperty(),
        								 company.substring(0, i+1))));
        }
    }
    
    private Timeline createBlinker(Node cursor){
    	Timeline blink = new Timeline(
    			new KeyFrame(
    					Duration.seconds(0.5),
    					new KeyValue(cursor.visibleProperty(),
    								false)),
    			new KeyFrame(
    					Duration.seconds(1),
    					new KeyValue(cursor.visibleProperty(),
    								true)));
    	blink.setCycleCount(4);
    	blink.setOnFinished(e2 -> {
    		finishIntro(); 
    		consApp.introPlaying = false;
    		Music m = consApp.getAssetManager().loadMusic(FileNames.THEME_MUSIC);
            m.setCycleCount(Integer.MAX_VALUE);
            consApp.getAudioManager().playMusic(m);
    	});
    	return blink;
    }

    @Override
    public void startIntro() {
    	consApp.getAudioManager().playSound(consApp.getAssetManager().loadSound(FileNames.KEYBOARD_TYPE));
    	blinker.play();
        timeline.play();
    }
}
