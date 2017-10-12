package com.monkeysonnet.zipzap.achievements;

import com.badlogic.gdx.graphics.Color;

public class BadgeTerralon extends BadgeBoss
{
	private static BadgeTerralon _instance;

	public static BadgeTerralon instance()
	{
		if(_instance == null)
			_instance = new BadgeTerralon();
		return _instance;
	}

	private BadgeTerralon()
	{
		super(Color.YELLOW, "terralon", -1);
	}
}
