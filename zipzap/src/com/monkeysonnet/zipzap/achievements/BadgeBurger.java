package com.monkeysonnet.zipzap.achievements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.monkeysonnet.zipzap.Z;

public class BadgeBurger extends Badge
{
	private static final BadgeBurger[] _instances = new BadgeBurger[4];
	
	public static final Color color = new Color(1f, 204f/255f, 0f, 1f);
	
	public static BadgeBurger instance(int n)
	{
		if(_instances[n] == null)
			_instances[n] = new BadgeBurger(n);
		
		return _instances[n];
	}

	protected BadgeBurger(int n)
	{
		super("burger-" + n);
	}

	@Override
	public Color color()
	{
		return color;
	}

	@Override
	public String title()
	{
		return "ASTRO-NOMS";
	}

	@Override
	public String description()
	{
		return isEarned() ? "You found a hidden hamburger!" : "Find a hidden hamburger";
	}
	
	@Override
	public TextureRegion icon()
	{
		return Z.texture("badge-burger");
	}
	
	@Override
	public boolean canShare()
	{
		return true;
	}
	
	@Override
	public String shareText()
	{
		return "I found a hidden hamburger! " + Z.uriDemo + " #astronomo";
	}
}
