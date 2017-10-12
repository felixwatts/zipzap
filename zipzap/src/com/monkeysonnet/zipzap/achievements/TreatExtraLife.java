package com.monkeysonnet.zipzap.achievements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.monkeysonnet.zipzap.Z;

public class TreatExtraLife extends Treat
{
	private int _num;
	
	public TreatExtraLife(int num)
	{
		super("extra-life-" + num);
		_num = num;
	}

	@Override
	public String title()
	{
		return "Extra Life";
	}

	@Override
	public String description()
	{
		if(isUnlocked())
		{
			return "You earned an extra life!";
		}
		else
		{
			return "Unlocks an extra life!";
		}
	}

	@Override
	public Color color()
	{
		return Color.RED;
	}

	@Override
	protected void onUnlock()
	{
		Z.prefs.putInteger("num-lives", _num);
		Z.prefs.flush();
	}
	
	@Override
	public TextureRegion icon()
	{
		return Z.texture("treat-extra-life");
	}
	
	public static int currentNumLives()
	{
		return Z.prefs.getInteger("num-lives", 0);
	}
}
