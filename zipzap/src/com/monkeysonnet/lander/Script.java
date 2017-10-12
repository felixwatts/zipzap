package com.monkeysonnet.lander;

import com.monkeysonnet.zipzap.IGameController;
import com.monkeysonnet.zipzap.IScript;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.achievements.BadgeJetPakWings2;

public class Script implements IScript
{
	private static final int NUM_MAPS = 8;
	private int _level = Z.prefs.getInteger("jetpak-level", 0);
	private int _nextLevel;
	
	@Override
	public IGameController current()
	{
		switch(_level)
		{
			case 0:
				return new Map1Controller();
			default:
				return new LanderGameController(_level);
		}
	}

	@Override
	public IGameController next()
	{
		if(BadgeJetPakWings2.instance().isEarned())
		{
			_level = _nextLevel;
			_nextLevel = -1;
			return current();
		}
		else
		{
			_level++;

			if(_level >= NUM_MAPS)
			{
				return null;
			}
			else
			{
				Z.prefs.putInteger("jetpak-level", _level);
				Z.prefs.flush();		
						
				return current();
			}
		}
	}

	@Override
	public IGameController prev()
	{
		return new LanderGameController(_level-1);
	}

	@Override
	public int level()
	{
		return _level;
	}
	
	@Override
	public void dispose()
	{
	}
	
	@Override
	public void reset()
	{
		_level = BadgeJetPakWings2.instance().isEarned() ? -1 : 0;
		Z.prefs.putInteger("jetpak-level", _level);
		Z.prefs.flush();	
	}

	public void nextLevel(int l)
	{
		_nextLevel = l;
	}
}
