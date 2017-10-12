package com.monkeysonnet.zipzap.screens;

import com.badlogic.gdx.Preferences;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.ICallback;
import com.monkeysonnet.engine.IScreen;
import com.monkeysonnet.zipzap.Z;

public class AchievementsScreen implements IScreen
{
	private ICallback _callback;
	
	public void doAchievements(ICallback callback)
	{
		if(Z.achievments.pending())
		{
			_callback = callback;
			Game.ScreenManager.push(this);
		}
		else if(callback != null)
			callback.callback(null);
	}

	@Override
	public void show()
	{
	}
	
	@Override
	public void pause()
	{
	}

	@Override
	public void focus()
	{
		if(!Z.achievments.process())
		{
			Game.ScreenManager.pop();
			if(_callback != null)
				_callback.callback(null);
		}
	}

	@Override
	public void render()
	{
	}

	@Override
	public void blur()
	{
	}

	@Override
	public void hide()
	{
		
	}

	@Override
	public boolean isFullScreen()
	{
		return false;
	}

	@Override
	public void serialize(Preferences dict)
	{
	}

	@Override
	public void deserialize(Preferences dict)
	{
	}

}
