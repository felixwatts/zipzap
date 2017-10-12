package com.monkeysonnet.zipzap;

import com.badlogic.gdx.audio.Music;

import aurelienribon.tweenengine.TweenAccessor;

public class MusicTweener implements TweenAccessor<Music>
{
	public static final int VAL_VOL = 0;

	@Override
	public int getValues(Music target, int tweenType, float[] returnValues)
	{
		returnValues[0] = 0.5f;
		return 1;
	}

	@Override
	public void setValues(Music target, int tweenType, float[] newValues)
	{
		target.setVolume(newValues[0]);
	}	
}
