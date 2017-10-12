package com.monkeysonnet.zipzap.achievements;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.monkeysonnet.zipzap.Z;

public abstract class Treat implements ITreat
{
	private String _key;
	
	protected Treat(String key)
	{
		_key = key;
	}

	@Override
	public boolean isUnlocked()
	{
		return Z.prefs.getBoolean("treat-" + _key, false);
	}

	@Override
	public void unlock()
	{
		Z.prefs.putBoolean("treat-" + _key, true);
		Z.prefs.flush();
		onUnlock();
	}
	
	@Override
	public TextureRegion icon()
	{
		return Z.texture(_key);
	}
	
	protected void onUnlock(){}
}
