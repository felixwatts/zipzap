package com.monkeysonnet.zipzap.achievements;

import com.badlogic.gdx.graphics.Color;

public class TreatUltraCapacitor extends Treat
{
	private final Color _color = new Color(1f, 0f, 102f/255f, 1f);
	
	public TreatUltraCapacitor()
	{
		super("ultra-capacitor");
	}

	@Override
	public String title()
	{
		return "ULTRA CAPACITOR";
	}

	@Override
	public String description()
	{
		if(isUnlocked())
		{
			return "You unlocked the Ultra-Capacitor ship upgrade!";
		}
		else
		{
			return "Unlocks ship upgrade: Ultra-Capacitor";
		}
	}

	@Override
	public Color color()
	{
		return _color;
	}
}
