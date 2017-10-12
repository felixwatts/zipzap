package com.monkeysonnet.zipzap.entities;

import aurelienribon.tweenengine.TweenAccessor;

public class LaserBeamTweener implements TweenAccessor<LaserBeam>
{
	@Override
	public int getValues(LaserBeam target, int tweenType, float[] returnValues)
	{
		returnValues[0] = (float)target.mode();
		return 1;
	}

	@Override
	public void setValues(LaserBeam target, int tweenType, float[] newValues)
	{
		target.mode((int)newValues[0]);
	}
}
