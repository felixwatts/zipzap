package com.monkeysonnet.zipzap;

import com.monkeysonnet.engine.FixtureTag;
import com.monkeysonnet.engine.IContactHandler;

public class NumberedFixtureTag extends FixtureTag
{
	private int num;

	public NumberedFixtureTag(Object owner, IContactHandler ch, int num)
	{
		super(owner, ch);
		this.num = num; 
	}
	
	public int num()
	{
		return num;
	}
}
