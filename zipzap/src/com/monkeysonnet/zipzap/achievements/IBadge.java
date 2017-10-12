package com.monkeysonnet.zipzap.achievements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.monkeysonnet.engine.ISimulationEventHandler;

public interface IBadge extends ISimulationEventHandler
{
	Color color();
	TextureRegion icon();
	void earn();
	String title();
	String description();
	ITreat treat();
	boolean isEarned();
	boolean needsSimEvents();
	boolean isPending();
	boolean queue();
	boolean canShare();
	String shareText();
}
