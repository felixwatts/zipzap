package com.monkeysonnet.zipzap.achievements;

import com.badlogic.gdx.graphics.Color;
import com.monkeysonnet.zipzap.Z;

public class TreatJetPakAdvancedTraining extends Treat
{

	protected TreatJetPakAdvancedTraining()
	{
		super("jet-pak-advanced");
	}

	@Override
	public String title()
	{
		return "";
	}

	@Override
	public String description()
	{
		return isUnlocked() ? "You unlocked Advanced Jet-Pak Training." : "Unlocks Advanced Jet-Pak Training.";
	}

	@Override
	public Color color()
	{
		return null;
	}
	
	@Override
	protected void onUnlock()
	{
		super.onUnlock();		
		Z.prefs.putBoolean("jet-pak-hard-mode", true);
		Z.prefs.flush();
	}
}
