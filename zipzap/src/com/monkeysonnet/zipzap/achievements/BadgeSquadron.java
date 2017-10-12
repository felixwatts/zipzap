package com.monkeysonnet.zipzap.achievements;

import com.badlogic.gdx.graphics.Color;
import com.monkeysonnet.zipzap.Z;

public class BadgeSquadron extends Badge
{
	private int _num;
	
	private static BadgeSquadron[] _instances = new BadgeSquadron[5];
	
	public static BadgeSquadron instance(int n)
	{
		if(_instances[n] == null)
			_instances[n] = new BadgeSquadron(n);
		return _instances[n];
	}
	
	private BadgeSquadron(int num)
	{
		super("squadron-" + num);
		_num = num;
	}	

	@Override
	public Color color()
	{
		switch(_num)
		{
			case 1:
			default:
				return Color.CYAN;
			case 2:
				return Color.YELLOW;
			case 3:
				return Color.MAGENTA;
			case 4:
				return Color.RED;
		}
	}

	@Override
	public String title()
	{
		switch(_num)
		{
			case 1:
			default:
				return "Quarter Squadron";
			case 2:
				return "Half Squadron";
			case 3:
				return "Three-Quarter Squadron";
			case 4:
				return "Full Squadron";
		}
	}

	@Override
	public String description()
	{
		if(isEarned())
		{
			switch(_num)
			{
				case 1:
				default:
					return "You added a friend to your squadron.";
				case 2:
					return "You added two friends to your squadron.";
				case 3:
					return "You added three friends to your squadron.";
				case 4:
					return "You added four friends to your squadron. Your squadron is complete!";
			}
		}
		else
		{
			switch(_num)
			{
				case 1:
				default:
					return "Add a friend to your squadron.";
				case 2:
					return "Add two friends to your squadron.";
				case 3:
					return "Add three friends to your squadron.";
				case 4:
					return "Add four friends to your squadron.";
			}
		}
	}
	
	@Override
	public ITreat treat()
	{
		if(_treat == null)
			_treat = new TreatExtraLife(_num);
		return _treat;
	}
	
	@Override
	public boolean canShare()
	{
		return true;
	}
	
	@Override
	public String shareText()
	{
		switch(_num)
		{
			case 1:
			default:
				return "I added a friend to my squadron! " + Z.uriDemo + " #astronomo";
			case 2:
				return "I added two friends to my squadron! " + Z.uriDemo + " #astronomo";
			case 3:
				return "I added three friends to my squadron! " + Z.uriDemo + " #astronomo";
			case 4:
				return "I added four friends to my squadron! FULL SQUADRON! " + Z.uriDemo + " #astronomo";
		}
	}
}
