package com.monkeysonnet.zipzap.achievements;

import com.badlogic.gdx.graphics.Color;
import com.monkeysonnet.zipzap.Z;

public class BadgeOverkill extends Badge
{
	private static BadgeOverkill _instance;

	public static BadgeOverkill instance()
	{
		if(_instance == null)
			_instance = new BadgeOverkill();
		return _instance;
	}
	
	protected BadgeOverkill()
	{
		super("overkill");
		
		_treat = new TreatJetpak();
	}

	@Override
	public Color color()
	{
		return Color.MAGENTA;
	}

	@Override
	public String title()
	{
		return "OVERKILL";
	}

	@Override
	public String description()
	{
		return isEarned() ? "You destroyed 9 enemies in 9 seconds or less!" : "Destroy 9 enemies in 9 seconds or less.";
	}

	@Override
	public boolean canShare()
	{
		return true;
	}
	
	@Override
	public String shareText()
	{
		return "I'm pwning Astronomo - just got a 9x combo and unlocked a minigame! " + Z.uriDemo + " #astronomo";
	}
}
