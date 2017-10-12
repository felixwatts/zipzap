package com.monkeysonnet.zipzap.sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.monkeysonnet.zipzap.VariSfx;
import com.monkeysonnet.zipzap.Z;

public class Sfx
{
	private static int NUM_SFX = 53;
	
	public static final float DEFAULT_VOLUME = 0.2f;
	
	private Sound[] _sfx;

	private boolean _enabled;
	
	public Sfx()
	{
		_enabled = Z.prefs.getBoolean("sfx-on", true);
		
		_sfx = new Sound[NUM_SFX];
		for(int n = 0; n < NUM_SFX; n++)
		{
			FileHandle f = Gdx.files.internal("sfx/" + n + ".ogg");
			if(f.exists())
				_sfx[n] = Gdx.audio.newSound(f);
		}
	}
	
	public void enabled(boolean enabled)
	{
		_enabled = enabled;
	}
	
	public boolean enabled()
	{
		return _enabled;
	}
	
	public long play(int sound)
	{
		return play(sound, 1f, false);
	}
	
	public long play(int sound, float vol)
	{
		return play(sound, vol, false);
	}
	
	public long play(int sound, float vol, boolean loop)
	{
		if(!_enabled)
			return -1;
		
		if(loop)
			return _sfx[sound].loop(vol * DEFAULT_VOLUME);
		else return _sfx[sound].play(vol * DEFAULT_VOLUME);
	}
	
	public void setVolume(int sound, long instance, float volume)
	{
		if(!_enabled || instance < 0)
			return;
		
		_sfx[sound].setVolume(instance, volume * DEFAULT_VOLUME);
	}
	
	public void stop(int sound)
	{
		if(!_enabled)
			return;
		
		_sfx[sound].stop();
	}
	
	public void stop(int sound, long instance)
	{
		if(!_enabled || instance < 0)
			return;
		
		_sfx[sound].stop(instance);
	}
	
	public void setPitch(int sound, long instance, float pitch)
	{
		if(!_enabled || instance < 0)
			return;
		
		_sfx[sound].setPitch(instance, pitch);
	}
	
	public final VariSfx explosionSmall = new VariSfx(-1002);
	
	public final VariSfx explosionMedium = new VariSfx(-1014);
	
	public final VariSfx explosionLarge = new VariSfx(-1040);
	
	public final VariSfx laserSmall = new VariSfx(-1000, -1001);
	
	public final VariSfx ouch = new VariSfx(-1046, -1047, -1048, -1049);
	
	public void dispose()
	{
		for(int n = 0; n < _sfx.length; n++)
			if(_sfx[n] != null)
				_sfx[n].dispose();
	}
	
	public void stopAll()
	{
		for(int n = 0; n < _sfx.length; n++)
			if(_sfx[n] != null)
				_sfx[n].stop();
	}
	
	public void pause()
	{
		for(int n = 0; n < _sfx.length; n++)
		{
			if(_sfx[n] != null)
				_sfx[n].stop();
		}
	}
	
	public void resume()
	{		
	}
}
