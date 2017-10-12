package com.monkeysonnet.zipzap.sound;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.zipzap.MusicTweener;
import com.monkeysonnet.zipzap.Z;

public class Music
{
	private static final float FADE_OUT_TIME = 1.5f;
	private static final float VOLUME = 0.75f;
	
	private com.badlogic.gdx.audio.Music _playing;
	private com.badlogic.gdx.audio.Music _fadingOut;
	private boolean _enabled;	
	
	public Music()
	{
		_enabled = Z.prefs.getBoolean("music-on", true);
	}
	
	public void play(FileHandle file)
	{
		if(!_enabled)
			return;
		
		fadeOut();
		
		if(file != null)
		{
			_playing = Gdx.audio.newMusic(file);
			_playing.setVolume(VOLUME);
			_playing.setLooping(true);
			_playing.play();
		}
	}
	
	public void pause()
	{
		if(!_enabled)
			return;
		
		if(_playing != null)
			_playing.pause();
	}
	
	public void resume()
	{
		if(!_enabled)
			return;
		
		if(_playing != null)
			_playing.play();
	}
	
	public void dispose()
	{
		if(_playing != null)
		{
			_playing.stop();
			_playing.dispose();
			_playing = null;
		}
		
		if(_fadingOut != null)
		{
			Game.TweenManager.killTarget(_fadingOut);
			_fadingOut.stop();
			_fadingOut.dispose();
			_fadingOut = null;
		}
	}
	
	public void enabled(boolean enabled)
	{
		_enabled = enabled;
		Z.prefs.putBoolean("music-on", enabled);
		
		if(_playing != null)
		{
			if(_enabled)
				_playing.play();
			else _playing.stop();
		}		
	}
	
	public boolean enabled()
	{
		return _enabled;
	}
	
	private void fadeOut()
	{
		if(_fadingOut != null)
		{
			Game.TweenManager.killTarget(_fadingOut);
			_fadingOut.stop();
			_fadingOut.dispose();
			_fadingOut = null;
		}
		
		if(_playing != null)
		{
			_fadingOut = _playing;
			_playing = null;
			
			Game.TweenManager.killTarget(_fadingOut);
			Tween
				.to(_fadingOut, MusicTweener.VAL_VOL, FADE_OUT_TIME*1000)
				.target(0f)
				.cast(com.badlogic.gdx.audio.Music.class)
				.setCallback(callbackFadoutComplete)
				.start(Game.TweenManager);
		}
	}
	
	private final TweenCallback callbackFadoutComplete = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			_fadingOut.stop();
			_fadingOut.dispose();
			_fadingOut = null;
		}
	};
}
