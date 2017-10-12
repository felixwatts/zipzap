package com.monkeysonnet.zipzap;

import com.badlogic.gdx.graphics.Color;

import aurelienribon.tweenengine.TweenAccessor;

public class ColourableTweener implements TweenAccessor<IColourable>
{

	@Override
	public int getValues(IColourable target, int tweenType, float[] returnValues)
	{
		Color c = target.color();
		returnValues[0] = c.r;
		returnValues[1] = c.g;
		returnValues[2] = c.b;
		returnValues[3] = c.a;
		return 4;
	}

	@Override
	public void setValues(IColourable target, int tweenType, float[] newValues)
	{
		target.setColor(newValues[0], newValues[1], newValues[2], newValues[3]);
	}
	
}
