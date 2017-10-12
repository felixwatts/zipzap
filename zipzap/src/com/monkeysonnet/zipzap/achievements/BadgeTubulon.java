package com.monkeysonnet.zipzap.achievements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.entities.WormSegment;

public class BadgeTubulon extends Badge
{
	private ITreat _treat;
	
	public BadgeTubulon()
	{
		super("tubulon");
	}

	@Override
	public Color color()
	{
		return WormSegment.tubulonColor;
	}

	@Override
	public String description()
	{
		return isEarned() ? "you defeated TUBULON!" : "Awarded for defeating TUBULON.";
	}

	@Override
	public ITreat treat()
	{
		if(_treat == null)
			_treat = new TreatRearCannon();
		return _treat;
	}
	
	@Override
	public TextureRegion icon()
	{
		return Z.texture("badge-icon-invader");
	}

	@Override
	public String title()
	{
		return "TUBULON";
	}
}
