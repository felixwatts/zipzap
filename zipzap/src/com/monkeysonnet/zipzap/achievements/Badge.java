package com.monkeysonnet.zipzap.achievements;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.monkeysonnet.zipzap.Z;

public abstract class Badge implements IBadge
{
	private String _name;
	private boolean _isEarned, _pending;
	protected ITreat _treat;
	
	protected Badge(String name)
	{
		_name = name;
		_isEarned = Z.prefs.getBoolean("badge-" + _name, false);
	}
	
	@Override
	public TextureRegion icon()
	{
		return Z.texture("badge-" + _name);
	}
	
	@Override
	public void earn()
	{
		Z.prefs.putBoolean("badge-" + _name, true);
		Z.prefs.flush();	
		_isEarned = true;
		_pending = false;
		onEarn();
	}

	protected void onEarn()
	{
	}

	@Override
	public boolean isEarned()
	{
		return _isEarned;
	}
	
	@Override
	public ITreat treat()
	{
		return _treat;
	}
	
	@Override
	public void onSimulationEvent(int eventType, Object argument)
	{
	}
	
	public boolean needsSimEvents()
	{
		return false;
	}
	
	@Override
	public boolean isPending()
	{
		return _pending;		
	}
	
	public boolean queue()
	{
		if(isEarned() || isPending())
			return false;
		
		_pending = true;
		Z.achievments.earn(this);
		return true;
	}
	
	@Override
	public boolean canShare()
	{
		return false;
	}
	
	@Override
	public String shareText()
	{
		return null;
	}
	
}
