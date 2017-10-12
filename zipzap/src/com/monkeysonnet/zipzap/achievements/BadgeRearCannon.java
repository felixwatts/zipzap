package com.monkeysonnet.zipzap.achievements;

import com.badlogic.gdx.graphics.Color;
import com.monkeysonnet.zipzap.Z;

public class BadgeRearCannon extends Badge
{

	private static BadgeRearCannon _instance;

	public static BadgeRearCannon instance()
	{
		if(_instance == null)
			_instance = new BadgeRearCannon();
		return _instance;
	}
	
	protected BadgeRearCannon()
	{
		super("rear-cannon");
		
		_treat = new TreatRearCannon();
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
		return "REAR CANNON";
	}

	@Override
	public String description()
	{
		return isEarned() ? "You found the Rear-Cannon upgrade!" : "Find the hidden Rear-Cannon Upgrade.";
	}
	
	@Override
	public boolean canShare()
	{
		return true;
	}
	
	@Override
	public String shareText()
	{
		return "I found the hidden Rear-Cannon upgrade! " + Z.uriDemo + " #astronomo";
	}
}
