package com.ergo21.consume;

import com.almasb.consume.Types.Property;
import com.almasb.fxgl.entity.Entity;

import javafx.animation.FadeTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

public class PlayerHUD extends Group {

	protected IntegerProperty maxHealth = new SimpleIntegerProperty();
	protected IntegerProperty maxMana = new SimpleIntegerProperty();
	protected IntegerProperty curHealth = new SimpleIntegerProperty();
	protected IntegerProperty curMana = new SimpleIntegerProperty();
	protected IntegerProperty bossHealth = new SimpleIntegerProperty();
	private ValueBar healthBar;
	private ValueBar manaBar;
	private ValueBar bossHealthBar;

	private Label healthLab;
	private Label manaLab;

	public PlayerHUD(int mHel, int mMan) {
		super();
		maxHealth.set(mHel);
		maxHealth.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				updateValues();
			}
		});
		maxMana.set(mMan);
		maxMana.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				updateValues();
			}
		});
		curHealth.set(mHel);
		curHealth.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				updateValues();
			}
		});
		curMana.set(mMan);
		curMana.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				updateValues();
			}
		});

		healthBar = new ValueBar(95, 20, curHealth, maxHealth, Color.RED);
		healthBar.getTransforms().add(new Rotate(270));
		healthBar.getTransforms().add(new Translate(-10, 15, 0));
		manaBar = new ValueBar(95, 20, curMana, maxMana, Color.LIMEGREEN);
		manaBar.getTransforms().add(new Rotate(270));
		manaBar.getTransforms().add(new Translate(-10, 40, 0));
		healthLab = new Label();
		manaLab = new Label();
		healthLab.setVisible(false);
		manaLab.setVisible(false);

		GridPane grid = new GridPane();
		grid.setVgap(2);
		grid.setHgap(2);
		grid.setPadding(new Insets(5, 5, 5, 5));
		grid.add(healthLab, 0, 1);
		grid.add(manaLab, 1, 1);

		getChildren().addAll(grid, healthBar, manaBar);

		updateValues();
	}
	
	public void setBossBar(Entity boss){
		getChildren().remove(bossHealthBar);
		
		bossHealthBar = new ValueBar(95, 20, boss.<Enemy>getProperty(Property.DATA).curHealth, boss.<Enemy>getProperty(Property.DATA).maxHealth, Color.YELLOW);
		bossHealthBar.getTransforms().add(new Rotate(270));
		bossHealthBar.getTransforms().add(new Translate(-10, 580, 0));
		boss.<Enemy>getProperty(Property.DATA).curHealth.addListener(new ChangeListener<Number>(){
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				if(arg2.intValue() <= 0){
					FadeTransition ft = new FadeTransition(Duration.seconds(1), bossHealthBar);
					ft.setFromValue(1);
					ft.setToValue(0);
					ft.setOnFinished((event)-> bossHealthBar.setVisible(false));
					ft.play();
				}
			}});
		getChildren().add(bossHealthBar);
	}

	public IntegerProperty MaxHealthProperty() {
		return maxHealth;
	}

	public int getMaxHealth() {
		return maxHealth.get();
	}

	public void setMaxHealth(int newMHealth) {
		maxHealth.set(newMHealth);
		updateValues();
	}

	public IntegerProperty CurHealthProperty() {
		return curHealth;
	}

	public int getCurHealth() {
		return curHealth.get();
	}

	public void setCurHealth(int newCHealth) {
		curHealth.set(newCHealth);
		updateValues();
	}

	public IntegerProperty MaxManaProperty() {
		return maxMana;
	}

	public int getMaxMana() {
		return maxMana.get();
	}

	public void setMaxMana(int newMMana) {
		maxMana.set(newMMana);
		updateValues();
	}

	public IntegerProperty CurManaProperty() {
		return curMana;
	}

	public int getCurMana() {
		return curMana.get();
	}

	public void setCurMana(int newCMana) {
		curMana.set(newCMana);
		updateValues();
	}

	private void updateValues() {
		healthLab.setText(curHealth.get() + "/" + maxHealth.get());
		manaLab.setText(curMana.get() + "/" + maxMana.get());
	}
	
	private class ValueBar extends Group{
		private IntegerProperty curValue;
		private IntegerProperty maxValue;
		private Rectangle backBar;
		public ValueBar(int width, int height, IntegerProperty cV, IntegerProperty mV, Paint p){
			curValue = cV;
			maxValue = mV;
			backBar = new Rectangle(width, height, Color.BLACK);
			this.getChildren().add(backBar);
			
			for(int i = 0; i < maxValue.intValue(); i++){
				Rectangle r = new Rectangle(i*width/maxValue.intValue() + 1, 2, (width - maxValue.intValue())/maxValue.intValue(), height - 4);
				r.setFill(p);
				this.getChildren().add(r);
			}
			
			curValue.addListener(new ChangeListener<Number>(){
				@Override
				public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
					for(int i = 0; i < getChildren().size(); i++){
						getChildren().get(i).setVisible(i <= arg2.intValue());
					}
				}});
			
			maxValue.addListener(new ChangeListener<Number>(){
				@Override
				public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
					getChildren().clear();
					getChildren().add(backBar);
					for(int i = 0; i < arg2.intValue(); i++){
						Rectangle r = new Rectangle(i*width/arg2.intValue() + 1, 2, (width - arg2.intValue())/arg2.intValue(), height - 4);
						r.setFill(p);
						getChildren().add(r);
					}
				}});
		}
	}
}