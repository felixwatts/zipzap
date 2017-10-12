package com.monkeysonnet.zipzap.achievements;

import com.badlogic.gdx.graphics.Color;

public class TreatJetpak extends Treat
{

	public TreatJetpak()
	{
		super("minigame-jetpak");
	}
	
	@Override
	protected void onUnlock()
	{
	}

	@Override
	public String description()
	{
		return isUnlocked() ? "You unlocked the Jet-Pak minigame!" : "Unlocks minigame: Jet-Pak";
	}
	
	@Override
	public String title()
	{
		return "";
	}
	
	@Override
	public Color color()
	{
		return Color.RED;
	}
}
