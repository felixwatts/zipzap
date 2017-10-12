package com.monkeysonnet.zipzap.achievements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public interface ITreat
{
	boolean isUnlocked();
	void unlock();
	String title();
	String description();
	TextureRegion icon();
	Color color();
}
