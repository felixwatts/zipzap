package com.monkeysonnet.zipzap;

import aurelienribon.tweenengine.Tween;

import com.monkeysonnet.engine.Game;
import com.monkeysonnet.zipzap.entities.LaserBeam;
import com.monkeysonnet.zipzap.entities.LaserBeamTweener;
import com.monkeysonnet.zipzap.screens.LoadingScreen;

public class ZipZapGame extends Game
{
	private boolean _firstResize = true;

	@Override
	public void dispose() 
	{
		Z.dispose();
		_firstResize = true;
	}

	@Override
	public void resize(int width, int height) 
	{
		if(_firstResize)
		{
			_firstResize = false;
			ScreenManager.push(new LoadingScreen());
		}
	}
	
	@Override
	protected void registerTweenAccessors()
	{
		super.registerTweenAccessors();
		Tween.registerAccessor(SimRenderer.class, new SimRendererTweener());
		Tween.registerAccessor(IColourable.class, new ColourableTweener());
		Tween.registerAccessor(LaserBeam.class, new LaserBeamTweener());
		Tween.registerAccessor(com.badlogic.gdx.audio.Music.class, new MusicTweener());
	}
}
