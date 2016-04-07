package com.almasb.consume.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.almasb.consume.Types;
import com.almasb.consume.Types.AnimationActions;
import com.almasb.fxgl.asset.Assets;
import com.almasb.fxgl.asset.StaticAnimatedTexture;
import com.almasb.fxgl.asset.Texture;
import com.almasb.fxgl.entity.Control;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.time.TimerManager;

import javafx.scene.transform.Translate;
import javafx.util.Duration;

public class AnimatedElephantControl implements Control {
	
	private HashMap<Types.AnimationActions, Texture> animations;
	private Texture idle;
	private ArrayList<Texture> form;
	private ArrayList<Texture> atk;
	private Entity enemy;
	private AnimationActions curAnimation = null; 

	public AnimatedElephantControl(Entity e, HashMap<Types.AnimationActions, Double> hashMap, String sD, Assets as) {
		enemy = e;
		Set<String> kS = as.TextureKeySet();
		animations = new HashMap<>();
		
		AnimationActions defAct;
				
		if(kS.contains(sD + "/IDLE_1.png")){
			defAct = AnimationActions.IDLE;
		}
		else{
			defAct = AnimationActions.MOVE;
		}
		
		for(Types.AnimationActions aA : Types.AnimationActions.values()){
			ArrayList<Texture> texs = new ArrayList<>();
			for(int i = 1; i < 5; i++){
				if(kS.contains(sD + "/" + aA + "_" + i + ".png")){
					texs.add(as.getTexture(sD + "/" + aA + "_" + i + ".png"));
				}
				else if(i == 1){
					texs.add(as.getTexture(sD + "/" + defAct + "_" + i + ".png"));
					break;
				}
				else{
					break;
				}
			}
				
			if(aA == Types.AnimationActions.MOVE){
				form = new ArrayList<Texture>();
				for(Texture t : texs){
					t.setPreserveRatio(true);
			       	t.setFitHeight(120);
			        		
			       	Translate off = new Translate();
			       	off.setX(-(t.getImage().getWidth()/4 - enemy.getWidth())/2);
			       	t.getTransforms().add(off);
			       	form.add(t);
				}
			}
			else if(aA == Types.AnimationActions.ATK){
				atk = new ArrayList<Texture>();
				for(Texture t : texs){
					t.setPreserveRatio(true);
			       	t.setFitHeight(120);
			        		
			       	Translate off = new Translate();
			       	off.setX(-(t.getImage().getWidth()/4 - enemy.getWidth())/2);
			       	t.getTransforms().add(off);
			       	atk.add(t);
				}
			}
			else if(aA == Types.AnimationActions.IDLE){
				Texture t = texs.get(0);
				t.setPreserveRatio(true);
		       	t.setFitHeight(120);
		        		
		       	Translate off = new Translate();
		       	off.setX(-(t.getImage().getWidth()/4 - enemy.getWidth())/2);
		       	t.getTransforms().add(off);
				idle = t;
			}
			else if(texs.size() > 1){
				Texture[] tS = new Texture[texs.size()];
				texs.toArray(tS);
				StaticAnimatedTexture sAT = new StaticAnimatedTexture(enemy.getWidth(), Duration.seconds(hashMap.get(aA)),
						tS);
				animations.put(aA, sAT);
			}
			else{
				Texture t = texs.get(0);
				t.setPreserveRatio(true);
		       	t.setFitHeight(120);
		        		
		       	Translate off = new Translate();
		       	off.setX(-(t.getImage().getWidth()/4 - enemy.getWidth())/2);
		       	t.getTransforms().add(off);
				animations.put(aA, t);
			}
		}
	}

	
	private int frames = 10;
	@Override
	public void onUpdate(Entity entity, long now){
		frames++;
		if(frames >= 2){
			actualUpdate(entity, now);
			frames = 0;
		}
	}
	
	public void actualUpdate(Entity entity, long now) {
		if(enemy != null && 
			enemy.getControl(SandBossControl.class) != null && enemy.getControl(SandBossControl.class).isUnderground()){
			entity.setVisible(false);
		}
		else if(enemy != null && enemy.getProperty("beenHit") != null && enemy.<Boolean>getProperty("beenHit")){
			entity.setVisible(!entity.isVisible());
		}
		else if(!entity.isVisible()){
			entity.setVisible(true);
		}
		
		Texture t = getCurrentImage(now);
		if(t != null){
			t.translateYProperty().bind(enemy.heightProperty().add(-120));
			entity.setGraphics(t);
			
			if(entity != null && enemy != null && enemy.getProperty("facingRight") != null){
				t.setTranslateX(enemy.<Boolean>getProperty("facingRight") ? -5 : -35);
				if(((enemy.<Boolean> getProperty("facingRight") && entity.getScaleX() < 0) ||
					(!enemy.<Boolean> getProperty("facingRight") && entity.getScaleX() > 0))){
					entity.setScaleX(entity.getScaleX()*-1);
				}
			}
		}
		
	}
	
	private int curForm = -1;
	private int frame = 0;
	private long lastUpdate = 0;
	
	private Texture getCurrentImage(long now) {
		Texture t = null;
		
		PhysicsControl ec = enemy.getControl(PhysicsControl.class);
		SandBossControl sc = enemy.getControl(SandBossControl.class);
		if (enemy != null && ec != null) {
			if (sc.getForm() != -1 && form != null) {
				if(sc.getForm() != curForm){
					curForm = sc.getForm();
					t = form.get(curForm);
					t.setPreserveRatio(true);
					t.setFitHeight(120);	
					curAnimation = AnimationActions.MOVE;
				}
			} 
			else if(sc.isUnderground()){
			}
			else if(enemy.<Boolean> getProperty("attacking") && atk != null){
				if(frame < atk.size() && now - lastUpdate >= TimerManager.secondsToNanos(0.5*frame)){
					t = atk.get(frame);
					frame++;
					///lastUpdate = now;
					curAnimation = AnimationActions.ATK;
					t.setPreserveRatio(true);
					t.setFitHeight(120);	
				}
			}
			else if(idle != null) {
				if(curAnimation != AnimationActions.IDLE){
					t = idle;
					curAnimation = AnimationActions.IDLE;
					t.setPreserveRatio(true);
					t.setFitHeight(120);
				}
			}
		}
		
		if(curAnimation != AnimationActions.ATK){
			frame = 0;
			lastUpdate = now;
		}
		
		return t;
	}
}
