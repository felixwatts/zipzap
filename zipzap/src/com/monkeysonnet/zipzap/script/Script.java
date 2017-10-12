package com.monkeysonnet.zipzap.script;

import com.monkeysonnet.zipzap.IGameController;
import com.monkeysonnet.zipzap.IScript;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.achievements.BadgeRearCannon;
import com.monkeysonnet.zipzap.achievements.BadgeShield;
import com.monkeysonnet.zipzap.achievements.BadgeUltraCapacitor;

public abstract class Script implements IScript
{
	private int _level = -1;
	
	public static IScript create(int sector)
	{
		return create(sector, 0);
	}

	public static IScript create(int sector, int wave)
	{
		switch(sector)
		{
			case 1:
			default:
				return new Sector1Script(wave);
			case 2:
				return new Sector2Script(wave);
			case 3:
				return new Sector3Script(wave);
			case 4:
				return new Sector4Script(wave);
		}
	}
	
	public Script(int level)
	{
		_level = level;
	}
	
	@Override
	public IGameController current()
	{
		return get(_level);
	}
	
	@Override
	public void reset()
	{
		_level = -1;
	}

	@Override
	public IGameController next()
	{
		if(_level == -1)
		{
			IGameController t = getTutorial();
			if(t != null)
				return t;
		}		
		
		_level++;
		return current();
	}
	
	@Override
	public IGameController prev()
	{
		return get(--_level);
	}
	
	public int level()
	{
		return _level;
	}

	protected abstract IGameController get(int level);
	
	public void dispose(){}
	
	private IGameController getTutorial()
	{
		if(BadgeUltraCapacitor.instance().isEarned() && !Z.prefs.getBoolean("tutorial-ultra-capacitor", false))
		{
			Z.prefs.putBoolean("tutorial-ultra-capacitor", true);
			Z.prefs.flush();
			
			Z.console().clearNow()
				.setAvatar(Z.texture("miner"), Z.colorTutorial)
				.setColour(Z.colorTutorial, Z.colorTutorialBg)
				.write("Captain, the Ultra-Capacitor upgrade has been fitted to your ship.")
				.touch()
				.clear()
				.write("Tap-and-hold to charge up your cannon.")
				.touch()
				.clear()
				.write("Once the cannon is fully charged, release to fire a powerful laser beam that will cut through even the toughest stains.")
				.touch()
				.clear()
				.write("... enemies, the toughest enemies.")
				.touch()
				.clear()
				.write("Good luck!");
			
			return new ConsoleCutsceneController();
		}
		
		if(BadgeShield.instance().isEarned() && !Z.prefs.getBoolean("tutorial-shield", false))
		{
			Z.prefs.putBoolean("tutorial-shield", true);
			Z.prefs.flush();
			
			Z.console().clearNow()
				.setAvatar(Z.texture("miner"), Z.colorTutorial)
				.setColour(Z.colorTutorial, Z.colorTutorialBg)
				.write("Captain, the Shield upgrade has been fitted to your ship.")
				.touch()
				.clear()
				.write("Now you will start each life with a shield!")
				.touch()
				.clear()
				.write("But don't get complacent, these shields tend to smash to pieces if you even so much as look at them wrong.")
				.touch()
				.clear()
				.write("Go get 'em!");
			
			return new ConsoleCutsceneController();
		}
		
		if(BadgeRearCannon.instance().isEarned() && !Z.prefs.getBoolean("tutorial-rear-cannon", false))
		{
			Z.prefs.putBoolean("tutorial-rear-cannon", true);
			Z.prefs.flush();
			
			Z.console().clearNow()
				.setAvatar(Z.texture("miner"), Z.colorTutorial)
				.setColour(Z.colorTutorial, Z.colorTutorialBg)
				.write("Captain, the Rear-Cannon upgrade has been fitted to your ship.")
				.touch()
				.clear()
				.write("Now you can shoot enemies as you flee!")
				.touch()
				.clear()
				.write("... as you bravely, bravely flee.")
				.touch()
				.clear()
				.write("Go get 'em!");
			
			return new ConsoleCutsceneController();
		}
		
		return null;
	}
}
