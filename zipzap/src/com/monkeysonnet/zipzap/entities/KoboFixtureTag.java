package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.engine.FixtureTag;
import com.monkeysonnet.engine.IContactHandler;
import com.monkeysonnet.engine.IEntity;

public class KoboFixtureTag extends FixtureTag
{
	private static final KoboFixtureTagPool pool = new KoboFixtureTagPool();
	
	public int x, y;
	public IEntity hardpoint;
	
	private static class KoboFixtureTagPool extends Pool<KoboFixtureTag>
	{
		@Override
		protected KoboFixtureTag newObject()
		{
			return new KoboFixtureTag();
		}
	}
	
	private KoboFixtureTag()
	{
		super(null, null);
	}
	
	public static KoboFixtureTag obtain(Object owner, IContactHandler ch, int x, int y, IEntity hardPoint)
	{
		KoboFixtureTag t = pool.obtain();
		t.init(owner, ch, x, y, hardPoint);
		return t;
	}

	private void init(Object owner, IContactHandler ch, int x, int y, IEntity hardpoint)
	{
		this.owner = owner;
		this.contactHandler = ch;
		this.x = x;
		this.y = y;
		this.hardpoint = hardpoint;
	}
		
	public void free()
	{
		this.owner = null;
		this.contactHandler = null;
		pool.free(this);
	}
}
