package com.monkeysonnet.zipzap.achievements;

import com.badlogic.gdx.graphics.Color;

public class BadgeKobolon extends BadgeBoss
{
	private static BadgeKobolon _instance;

	public static BadgeKobolon instance()
	{
		if(_instance == null)
			_instance = new BadgeKobolon();
		return _instance;
	}

	private BadgeKobolon()
	{
		super(Color.RED, "kobolon", 2);
	}
}
