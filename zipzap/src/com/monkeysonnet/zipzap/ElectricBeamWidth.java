package com.monkeysonnet.zipzap;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenAccessor;
import aurelienribon.tweenengine.equations.Sine;

public class ElectricBeamWidth implements TweenAccessor<ElectricBeamWidth>
{
	private static final int TW_BEAM_WIDTH = 4;
	
	private static final float BEAM_WIDTH_MIN = 3f;
	private static final float BEAM_WIDTH_MAX = 4f;
	private Tween _beamGlowTween;
	private float _beamWidth;
	
	public ElectricBeamWidth()
	{
		_beamWidth = BEAM_WIDTH_MIN;
		
		_beamGlowTween = Tween
				.to(this, TW_BEAM_WIDTH, 50)
				.target(BEAM_WIDTH_MAX)
				.ease(Sine.INOUT)
				.repeatYoyo(Tween.INFINITY, 0)
				.start(Z.sim().tweens());
	}
	
	public void free()
	{
		_beamGlowTween.kill();
	}
	
	public float beamWidth()
	{
		return _beamWidth;
	}
	
	@Override
	public int getValues(ElectricBeamWidth target, int tweenType, float[] returnValues)
	{
		switch(tweenType)
		{
			case TW_BEAM_WIDTH:
				returnValues[0] = _beamWidth;
				return 1;
			default: return -1;
		}
	}

	@Override
	public void setValues(ElectricBeamWidth target, int tweenType, float[] newValues)
	{
		switch(tweenType)
		{
			case TW_BEAM_WIDTH:
				_beamWidth = newValues[0];
				break;
		}
	}
}
