package com.monkeysonnet.zipzap.achievements;

import com.badlogic.gdx.graphics.Color;
import com.monkeysonnet.zipzap.Z;

public class BadgeUltraCapacitor extends Badge
{
	private static BadgeUltraCapacitor _instance;

	public static BadgeUltraCapacitor instance()
	{
		if(_instance == null)
			_instance = new BadgeUltraCapacitor();
		return _instance;
	}
	
	protected BadgeUltraCapacitor()
	{
		super("ultra-capacitor");
		
		_treat = new TreatUltraCapacitor();
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
		return "ULTRA CAPACITOR";
	}

	@Override
	public String description()
	{
		return isEarned() ? "You found the Ultra-Capacitor upgrade!" : "Find the hidden Ultra-Capacitor Upgrade.";
	}
	
	@Override
	public boolean canShare()
	{
		return true;
	}
	
	@Override
	public String shareText()
	{
		return "I found the hidden Ultra-Capacitor upgrade! " + Z.uriDemo + " #astronomo";
	}
}
