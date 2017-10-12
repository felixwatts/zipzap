package com.monkeysonnet.zipzap;

import aurelienribon.tweenengine.TweenAccessor;

public class SimRendererTweener implements TweenAccessor<SimRenderer>
{

	@Override
	public int getValues(SimRenderer target, int tweenType, float[] returnValues)
	{
		returnValues[0] = target.getBlackAndWhite();
		return 1;
	}

	@Override
	public void setValues(SimRenderer target, int tweenType, float[] newValues)
	{
		target.setBlackAndWhite(newValues[0]);
	}

}
