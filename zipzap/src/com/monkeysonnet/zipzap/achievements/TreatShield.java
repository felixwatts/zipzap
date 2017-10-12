package com.monkeysonnet.zipzap.achievements;

import com.badlogic.gdx.graphics.Color;

public class TreatShield extends Treat
{
	public TreatShield()
	{
		super("shield");
	}

	@Override
	public String title()
	{
		return "Ship Upgrade: Shield";
	}

	@Override
	public String description()
	{
		return isUnlocked() ? "You unlocked a ship upgrade: Start each life with a shield." : "Unlocks ship upgrade: Start each life with a shield.";
	}

	@Override
	public Color color()
	{
		return Color.RED;
	}
}
