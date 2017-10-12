package com.monkeysonnet.zipzap.achievements;

import com.badlogic.gdx.graphics.Color;

public class BadgePrismolon extends BadgeBoss
{
	private static BadgePrismolon _instance;

	public static BadgePrismolon instance()
	{
		if(_instance == null)
			_instance = new BadgePrismolon();
		return _instance;
	}
	
	private BadgePrismolon()
	{
		super(Color.CYAN, "prismolon", 4);
	}
}
