package com.ergo21.consume;

import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.transform.Rotate;

public class PlayerHUD extends Group{
	private int maxHealth;
	private int maxMana;
	private int curHealth;
	private int curMana;
	private ProgressBar healthBar;
	private ProgressBar manaBar;

	private Label healthLab;
	private Label manaLab;

	public PlayerHUD(int mHel, int mMan){
		super();
		maxHealth = mHel;
		maxMana = mMan;
		curHealth = mHel;
		curMana = mMan;
		healthBar = new ProgressBar();
		healthBar.getTransforms().add(new Rotate(270));
		manaBar = new ProgressBar();
		manaBar.getTransforms().add(new Rotate(270));
		healthLab = new Label();
		manaLab = new Label();

		GridPane grid = new GridPane();
		grid.setVgap(2);
		grid.setHgap(2);
		grid.setPadding(new Insets(5,5,5,5));
		grid.add(healthBar, 0, 0);
		grid.add(manaBar, 1, 0);
		grid.add(healthLab, 0, 1);
		grid.add(manaLab, 1, 1);

		getChildren().add(grid);

		updateValues();
	}

	public int getMaxHealth(){
		return maxHealth;
	}

	public void setMaxHealth(int newMHealth){
		maxHealth = newMHealth;
		updateValues();
	}

	public int getCurHealth(){
		return curHealth;
	}

	public void setCurHealth(int newCHealth){
		curHealth = newCHealth;
		updateValues();
	}

	public int getMaxMana(){
		return maxMana;
	}

	public void setMaxMana(int newMMana){
		maxMana = newMMana;
		updateValues();
	}

	public int getCurMana(){
		return curMana;
	}

	public void setCurMana(int newCMana){
		curMana = newCMana;
		updateValues();
	}

	private void updateValues(){
		healthBar.setProgress(curHealth * 1.0 /maxHealth);
		if (maxMana != 0)
		    manaBar.setProgress(curMana * 1.0 /maxMana);

		healthLab.setText(curHealth + "/" + maxHealth);
		manaLab.setText(curMana + "/" + maxMana);
	}
}