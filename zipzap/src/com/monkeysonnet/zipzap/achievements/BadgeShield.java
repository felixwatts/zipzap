package com.monkeysonnet.zipzap.achievements;

import com.badlogic.gdx.graphics.Color;
import com.monkeysonnet.zipzap.Z;

public class BadgeShield extends Badge
{
	private static BadgeShield _instance;

	public static BadgeShield instance()
	{
		if(_instance == null)
			_instance = new BadgeShield();
		return _instance;
	}

	protected BadgeShield()
	{
		super("shield");
		_treat = new TreatShield();
	}

	private final Color _color = new Color(1f, 0f, 102f/255f, 1f);

	@Override
	public Color color()
	{
		return _color;
	}

	@Override
	public String title()
	{
		return "SHIELD UPGRADE";
	}

	@Override
	public String description()
	{
		return isEarned() ? "You found the Shield upgrade!" : "Find the hidden Shield Upgrade.";
	}
	
	@Override
	public boolean canShare()
	{
		return true;
	}
	
	@Override
	public String shareText()
	{
		return "I found the hidden Shield upgrade! " + Z.uriDemo + " #astronomo";
	}
}
