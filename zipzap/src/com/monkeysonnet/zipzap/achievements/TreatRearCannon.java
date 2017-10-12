package com.monkeysonnet.zipzap.achievements;
import com.badlogic.gdx.graphics.Color;
import com.monkeysonnet.zipzap.Z;

public class TreatRearCannon extends Treat
{
	public TreatRearCannon()
	{
		super("rear-cannon");
	}
	
	@Override
	protected void onUnlock()
	{
		Z.ship().enableRearCannon(true);
	}

	@Override
	public String description()
	{
		return isUnlocked() ? "You unlocked the Rear Cannon ship upgrade!" : "Unlocks ship upgrade: Rear Cannon";
	}
	
	@Override
	public String title()
	{
		return "Ship Upgrade: Rear Cannon";
	}
	
	@Override
	public Color color()
	{
		return Color.RED;
	}
}
