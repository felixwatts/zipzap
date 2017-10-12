package com.monkeysonnet.zipzap.achievements;

import com.badlogic.gdx.graphics.Color;

public class TreatUnlockSector extends Treat
{
	private int _sector;

	public TreatUnlockSector(int sector)
	{
		super("sector-" + sector);
		_sector = sector;
	}

	@Override
	public String title()
	{
		return "Unlock Sector";
	}

	@Override
	public String description()
	{
		if(isUnlocked())
			return "You unlocked Sector 0" + _sector + "!"; // todo proper formatting
		else
			return "Unlocks Sector 0" + _sector;
	}
	
	@Override
	public boolean isUnlocked()
	{
		if(_sector == 1)
			return true;
		else return super.isUnlocked();
	}

	@Override
	public Color color()
	{
		return Color.GREEN;
	}
}
