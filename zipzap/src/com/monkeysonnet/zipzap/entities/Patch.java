package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.engine.editor.Graphic;
import com.monkeysonnet.zipzap.IRenderableSprite;
import com.monkeysonnet.zipzap.Z;

public class Patch implements IEntity, IRenderableSprite
{
	private static final PatchPool pool = new PatchPool();
	private static class PatchPool extends Pool<Patch>
	{
		@Override
		protected Patch newObject()
		{
			return new Patch();
		}
	}
	
	private Sprite _sprite;
	private boolean _foreground;

	public static Patch spawn(Graphic g, boolean foreground)
	{
		return spawn(g.toSprite(Z.textures), foreground, g.zIndex);
	}
	
	public static Patch spawn(Sprite s, boolean foreground, int zIndex)
	{
		Patch p = pool.obtain();
		p._sprite = s;
		p._foreground = foreground;
		Z.sim.environment().put(p, s.getX(), s.getY(), zIndex, s.getWidth(), s.getHeight());
		return p;
	}
	
	private Patch(){}

	@Override
	public void update(float dt)
	{
	}

	@Override
	public void free()
	{
		pool.free(this);
	}

	@Override
	public int layer()
	{
		return _foreground ? 3 : 0;
	}

	@Override
	public Sprite sprite()
	{
		return _sprite;
	}

}
