package com.monkeysonnet.zipzap.achievements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.monkeysonnet.zipzap.Z;

public class BadgeJetPakWings1 extends Badge
{
	private final Color color = new Color(0, 102f/255f, 255f/255f, 1f);
	
	private static BadgeJetPakWings1 _instance;
	
	public static BadgeJetPakWings1 instance()
	{
		if(_instance == null)
			_instance = new BadgeJetPakWings1();
		return _instance;
	}

	protected BadgeJetPakWings1()
	{
		super("jet-pak-wings-1");
	}

	@Override
	public Color color()
	{
		return color;
	}

	@Override
	public String title()
	{
		return "ROOKIE WINGS";
	}

	@Override
	public String description()
	{
		return isEarned() ? "You completed basic Jet-Pak training." : "Complete basic Jet-Pak training.";
	}

	@Override
	public TextureRegion icon()
	{
		return Z.texture("badge-wings");
	}
	
	@Override
	public ITreat treat()
	{
		if(_treat == null)
			_treat = new TreatJetPakAdvancedTraining();
		return _treat;
	}
	
	@Override
	public boolean canShare()
	{
		return true;
	}
	
	@Override
	public String shareText()
	{
		return "I completed basic jet-pak training! " + Z.uriDemo + " #astronomo";
	}
}
