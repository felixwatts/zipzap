package com.monkeysonnet.lander;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.zipzap.IRenderableTexture;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.entities.Enemy;

public class Crate extends Enemy implements IRenderableTexture
{
	private static final Color color = Color.WHITE;
	private static final TextureRegion texture = Z.texture("crate");
	public static final float DEFAULT_RADIUS = 3;
	private static final float DENSITY = 1;
	private static final float FRICTION = 0.8f;
	private static final float RESTITUTION = 0.05f;
	
	private static final CratePool pool = new CratePool();
	private static class CratePool extends Pool<Crate>
	{
		@Override
		protected Crate newObject()
		{
			return new Crate();
		}
	}

	private float _radius;

	private Crate()
	{
		//_behaviours.add(new ShootableBehaviour(ENERGY));
	}
	
	public static void spawn(Vector2 loc, float radius)
	{
		Crate c = pool.obtain();
		
		c._radius = radius;
		
		c._body = B2d.dynamicBody()
				.at(loc.x, loc.y + radius)
				.withFixture(B2d
						.box(radius, radius)
						.density(DENSITY)
						.friction(FRICTION)
						.restitution(RESTITUTION)
						.category(LanderSim.COL_CAT_WALL)
						.mask(LanderSim.COL_CAT_GUY_BULLET | LanderSim.COL_CAT_GUY | LanderSim.COL_CAT_WALL | LanderSim.COL_CAT_ENEMY | LanderSim.COL_CAT_ENEMY_BULLET)
						.userData(c._fixtureTag))
				.create(L.sim.world());
		
		c.onSpawn();
		L.sim.entities().add(c);
	}
	
	public static void spawn(Vector2 loc)
	{
		spawn(loc, DEFAULT_RADIUS);
	}
	
	@Override
	protected void onFree()
	{
		pool.free(this);
	}

	@Override
	public float radius()
	{
		return _radius;
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
}
