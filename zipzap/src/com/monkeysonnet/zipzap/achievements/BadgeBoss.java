package com.monkeysonnet.zipzap.achievements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.monkeysonnet.zipzap.Z;

public class BadgeBoss extends Badge
{
	private Color _color;
	private String _title;
	private String _descriptionPre;
	private String _descriptionPost;
	private String _shareText;
	
	protected BadgeBoss(Color color, String name, int unlockSector)
	{
		super(name);
		_color = color;
		_title = name.toUpperCase();
		_descriptionPost = "you defeated " + name.toUpperCase() + "!";
		_descriptionPre = "Awarded for defeating " + name.toUpperCase() + ".";
		_shareText = "I heroically defeated " + name.toUpperCase() + "! " + Z.uriDemo + " #astronomo";
		
		if(unlockSector > 0)		
			_treat = new TreatUnlockSector(unlockSector);		
	}

	@Override
	public Color color()
	{
		return _color;
	}

	@Override
	public String title()
	{
		return _title;
	}

	@Override
	public String description()
	{
		return isEarned() ? _descriptionPost : _descriptionPre;
	}
	
	@Override
	public TextureRegion icon()
	{
		return Z.texture("badge-icon-invader");
	}
	
	@Override
	public boolean canShare()
	{
		return true;
	}
	
	@Override
	public String shareText()
	{
		return _shareText;
	}
}
