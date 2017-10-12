package com.monkeysonnet.zipzap.achievements;

import com.badlogic.gdx.graphics.Color;

public class BadgeNebulon extends BadgeBoss
{
	private static BadgeNebulon _instance;

	public static BadgeNebulon instance()
	{
		if(_instance == null)
			_instance = new BadgeNebulon();
		return _instance;
	}

	private BadgeNebulon()
	{
		super(Color.GREEN, "nebulon", 3);
	}
}
