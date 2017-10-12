package com.monkeysonnet.lander;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.entities.Enemy;

public class SentryBullet extends Enemy implements IRenderableMultiPolygon
{
	private static final Color color = Color.RED;// Color.YELLOW;
	private static final SentryBulletPool pool = new SentryBulletPool();
	private static final float LENGTH = 1;
	private static final float DAMAGE = 9;
	private static final Vector2[] verts = new Vector2[]
	{
		new Vector2(-LENGTH/2f, 0),
		new Vector2(LENGTH/2f, 0)
	};
	
	private static final int SFX_HIT_GUY = -1013;
	
	private static class SentryBulletPool extends Pool<SentryBullet>
	{
		@Override
		protected SentryBullet newObject()
		{
			return new SentryBullet();
		}
	}
	
	public static void spawn(Vector2 loc, Vector2 vel)
	{
		SentryBullet b = pool.obtain();
		
		b._body = B2d
				.dynamicBody()
				.at(loc)
				.bullet(true)
				.rotated((float)Math.toRadians(vel.angle()))
				.linearVelocity(vel)
				.gravityScale(0)
				.withFixture(B2d
						.box(LENGTH/2f, LENGTH/8f)
						.sensor(true)
						.category(LanderSim.COL_CAT_ENEMY_BULLET)
						.mask(LanderSim.COL_CAT_GUY | LanderSim.COL_CAT_WALL)
						.userData(b._fixtureTag))
				.create(L.sim.world());
		
		b.onSpawn();		
		L.sim.entities().add(b);						
	}
	
	@Override
	public void onBeginContact(Contact c, Fixture me, Fixture other)
	{
		super.onBeginContact(c, me, other);
		
		if((other.getFilterData().categoryBits & LanderSim.COL_CAT_WALL) != 0)
		{
			free();
			L.sim.spawnSparks(origin(), _body.getLinearVelocity(), color);
		}
		else if((other.getFilterData().categoryBits & LanderSim.COL_CAT_GUY) != 0)
		{
			if(!L.sim.guy().dead())
			{
				L.sim.guy().damage(DAMAGE);
				L.sim.spawnSparks(origin(), _body.getLinearVelocity(), Color.RED);
				L.sim.fireEvent(SFX_HIT_GUY, null);
			}
			free();
		}
	}
	
	@Override
	protected void onFree()
	{
		pool.free(this);
		
	}

	@Override
	public int getNumPolys()
	{
		return 2;
	}

	@Override
	public float angle(int poly)
	{
		return (float)Math.toDegrees(_body.getAngle());
	}

	@Override
	public Vector2 origin(int poly)
	{
		return _body.getPosition();
	}

	@Override
	public Vector2[] verts(int poly)
	{
		return verts;
	}

	@Override
	public Color color(int poly)
	{
		return poly == 1 ? Color.WHITE : color;
	}

	@Override
	public float lineWidth(int poly)
	{
		return poly == 0 ? 1f : 0.5f;
	}

	@Override
	public boolean isLoop(int poly)
	{
		return false;
	}

	@Override
	public float clipRadius()
	{
		return LENGTH;
	}
}
