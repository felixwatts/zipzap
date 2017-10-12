package com.monkeysonnet.lander;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.zipzap.IRenderableTexture;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.entities.Enemy;

public class PowerBox extends Enemy implements IRenderableTexture
{
	private static final Color color = Color.WHITE;
	private static final TextureRegion texture = Z.texture("power-box");
	private static final int ENERGY = 2;
	
	private static final PowerBoxPool pool = new PowerBoxPool();
	public static final float HEIGHT = 6f;
	public static final float WIDTH = HEIGHT * (14f/16f);
	private static class PowerBoxPool extends Pool<PowerBox>
	{
		@Override
		protected PowerBox newObject()
		{
			return new PowerBox();
		}
	}
	
	private String _targets;

	private PowerBox()
	{
		_behaviours.add(new ShootableBehaviour(ENERGY));
	}
	
	public static void spawn(Vector2 loc, String targets)
	{
		PowerBox c = pool.obtain();

		c._body = B2d.staticBody()
				.at(loc.x, loc.y + (HEIGHT/2f))
				.withFixture(B2d
						.box(HEIGHT/2f, WIDTH/2f)
						.category(LanderSim.COL_CAT_ENEMY)
						.mask(LanderSim.COL_CAT_GUY_BULLET | LanderSim.COL_CAT_GUY | LanderSim.COL_CAT_WALL)
						.userData(c._fixtureTag))
				.create(L.sim.world());
		
		c._targets = targets;
		
		c.onSpawn();
		L.sim.entities().add(c);
	}
	
	@Override
	protected void onFree()
	{
		pool.free(this);
	}

	@Override
	public float radius()
	{
		return HEIGHT/2f;
	}

	@Override
	public TextureRegion texture()
	{
		return texture;
	}

	@Override
	public Color color()
	{
		return color;
	}
	
	@Override
	public void onKill()
	{
		super.onKill();		
	}
	
	@Override
	public void update(float dt)
	{
		super.update(dt);
		
		if(_dead && _killed)
		{
			L.sim.trigger(_targets);
		}
	}
}
