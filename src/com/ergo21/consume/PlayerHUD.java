package com.ergo21.consume;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class PlayerHUD extends Group{
	
	protected IntegerProperty maxHealth = new SimpleIntegerProperty();
	protected IntegerProperty maxMana = new SimpleIntegerProperty();
	protected IntegerProperty curHealth = new SimpleIntegerProperty();
	protected IntegerProperty curMana = new SimpleIntegerProperty();
	private ProgressBar healthBar;
	private ProgressBar manaBar;

	private Label healthLab;
	private Label manaLab;

	public PlayerHUD(int mHel, int mMan){
		super();
		maxHealth.set(mHel);
		maxHealth.addListener(new ChangeListener<Number>(){
			@Override
			public void changed(ObservableValue<? extends Number> arg0,
					Number arg1, Number arg2) {
				updateValues();
			}		
		});
		maxMana.set(mMan);
		maxMana.addListener(new ChangeListener<Number>(){
			@Override
			public void changed(ObservableValue<? extends Number> arg0,
					Number arg1, Number arg2) {
				updateValues();
			}		
		});
		curHealth.set(mHel);
		curHealth.addListener(new ChangeListener<Number>(){
			@Override
			public void changed(ObservableValue<? extends Number> arg0,
					Number arg1, Number arg2) {
				updateValues();
			}		
		});
		curMana.set(mMan);
		curMana.addListener(new ChangeListener<Number>(){
			@Override
			public void changed(ObservableValue<? extends Number> arg0,
					Number arg1, Number arg2) {
				updateValues();
			}		
		});
		healthBar = new ProgressBar();
		healthBar.getTransforms().add(new Rotate(270));
		manaBar = new ProgressBar();
		manaBar.getTransforms().add(new Rotate(270));
		manaBar.getTransforms().add(new Translate(0, 25, 0));
		healthLab = new Label();
		manaLab = new Label();

		GridPane grid = new GridPane();
		grid.setVgap(2);
		grid.setHgap(2);
		grid.setPadding(new Insets(5,5,5,5));
		grid.add(healthLab, 0, 1);
		grid.add(manaLab, 1, 1);

		getChildren().addAll(grid, healthBar, manaBar);

		updateValues();
	}

	public IntegerProperty MaxHealthProperty(){
		return maxHealth;
	}
	public int getMaxHealth(){
		return maxHealth.get();
	}
	public void setMaxHealth(int newMHealth){
		maxHealth.set(newMHealth);
		updateValues();
	}

	public IntegerProperty CurHealthProperty(){
		return curHealth;
	}
	public int getCurHealth(){
		return curHealth.get();
	}
	public void setCurHealth(int newCHealth){
		curHealth.set(newCHealth);
		updateValues();
	}

	public IntegerProperty MaxManaProperty(){
		return maxMana;
	}
	public int getMaxMana(){
		return maxMana.get();
	}
	public void setMaxMana(int newMMana){
		maxMana.set(newMMana);
		updateValues();
	}

	public IntegerProperty CurManaProperty(){
		return curMana;
	}
	public int getCurMana(){
		return curMana.get();
	}
	public void setCurMana(int newCMana){
		curMana.set(newCMana);
		updateValues();
	}

	private void updateValues(){
		healthBar.setProgress(curHealth.get() * 1.0 /maxHealth.get());
		if (maxMana.get() != 0)
		    manaBar.setProgress(curMana.get() * 1.0 /maxMana.get());

		healthLab.setText(curHealth.get() + "/" + maxHealth.get());
		manaLab.setText(curMana.get() + "/" + maxMana.get());
	}
}