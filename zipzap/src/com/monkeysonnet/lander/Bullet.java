package com.monkeysonnet.lander;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.FixtureTag;
import com.monkeysonnet.zipzap.IRenderableTexture;
import com.monkeysonnet.zipzap.Tools;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.behaviours.BlobTrailBehaviour;
import com.monkeysonnet.zipzap.behaviours.MaxAgeBehaviour;
import com.monkeysonnet.zipzap.entities.Enemy;

public class Bullet extends Enemy implements IRenderableTexture
{
	private static final float MAX_AGE = 2f;	
	public static final float RADIUS = 0.5f;
	private static final float DENSITY = 3f;
	private static final float RESTITUTION = 0.4f;
	private static final float FRICTION = 0.1f;
	private static final int SFX_BOUNCE = -1019;
	public static final Color sparksColour = new Color(1f, 254f/255f, 154f/255f, 1f);
	public static final Color trailColour = new Color(1f, 254f/255f, 154f/255f, 0.5f);
	
	private static final BulletPool pool = new BulletPool();
	private static class BulletPool extends Pool<Bullet>
	{
		@Override
		protected Bullet newObject()
		{
			return new Bullet();
		}
	}
	
	private static final TextureRegion texture = Z.texture("bullet");
	
	
	private Bullet()
	{
		_behaviours.add(new MaxAgeBehaviour(MAX_AGE));
		_behaviours.add(new BlobTrailBehaviour(trailColour, 0f, 0.2f, Tools.zeroVector, 2*RADIUS, -10f*RADIUS));
	}
	
	public static void spawn(Vector2 loc, Vector2 vel)
	{
		Bullet b = pool.obtain();
		
		b._body = B2d
				.dynamicBody()
				.at(loc)
				.linearVelocity(vel)
				.bullet(true)
				.linearDamping(0)
				.gravityScale(2f)
				.withFixture(B2d
						.circle()
						.radius(RADIUS)
						.density(DENSITY)
						.restitution(RESTITUTION)
						.friction(FRICTION)
						.category(LanderSim.COL_CAT_GUY_BULLET)
						.mask(LanderSim.COL_CAT_PAD | LanderSim.COL_CAT_WALL | LanderSim.COL_CAT_ENEMY)
						.userData(b._fixtureTag))
				.create(L.sim.world());
		
		b.onSpawn();
		
		L.sim.entities().add(b);
	}

	@Override
	public float radius()
	{
		return RADIUS;
	}

	@Override
	public TextureRegion texture()
	{
		return texture;
	}

	@Override
	public Color color()
	{
		return Color.WHITE;
	}	
	
	@Override
	protected void onFree()
	{
		L.sim.spawnSparks(origin(), _body.getLinearVelocity(), sparksColour);
		pool.free(this);
	}
	
	@Override
	public void onBeginContact(Contact c, Fixture me, Fixture other)
	{
		super.onBeginContact(c, me, other);
		
		if((other.getFilterData().categoryBits & LanderSim.COL_CAT_ENEMY) != 0)
		{
			Enemy e = (Enemy)(((FixtureTag)other.getUserData()).owner);
			if(e.isHitable())
			{
				e.hit(other, false, c.getWorldManifold().getPoints()[0], c.getWorldManifold().getNormal());
				free();
			}
		}
		else
		{
			L.sim.fireEvent(SFX_BOUNCE, origin());
		}
	}
	
	@Override
	public float angle()
	{
		return 0f;
	}
}
