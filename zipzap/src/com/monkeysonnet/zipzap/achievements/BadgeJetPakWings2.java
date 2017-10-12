package com.monkeysonnet.zipzap.achievements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.monkeysonnet.lander.Script;
import com.monkeysonnet.zipzap.Z;

public class BadgeJetPakWings2 extends Badge
{
	private final Color color = new Color(1f, 0f, 32f/255f, 1f);
	
	private static BadgeJetPakWings2 _instance;
	
	public static BadgeJetPakWings2 instance()
	{
		if(_instance == null)
			_instance = new BadgeJetPakWings2();
		return _instance;
	}

	protected BadgeJetPakWings2()
	{
		super("jet-pak-wings-2");
	}

	@Override
	public Color color()
	{
		return color;
	}

	@Override
	public String title()
	{
		return "VETERAN WINGS";
	}

	@Override
	public String description()
	{
		return isEarned() ? "You completed Advanced Jet-Pak Training!" : "Complete Advanced Jet-Pak Training";
	}
	
	@Override
	public TextureRegion icon()
	{
		return Z.texture("badge-wings");
	}
	
	@Override
	protected void onEarn()
	{
		super.onEarn();
		
		Z.prefs.putBoolean("jet-pak-hard-mode", false);
		
		Z.prefs.putInteger("jetpak-level", -1);
		Z.prefs.flush();	
		
		((Script)Z.script).nextLevel(-1);
	}
	
	@Override
	public boolean canShare()
	{
		return true;
	}
	
	@Override
	public String shareText()
	{
		return "I completed advanced jet-pak training! " + Z.uriDemo + " #astronomo";
	}
}
