package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.FixtureTag;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.DieOnHitBehaviour;
import com.monkeysonnet.zipzap.behaviours.DieOnRangeBehaviour;
import com.monkeysonnet.zipzap.behaviours.KillOnContactBehaviour;

public class Ufo extends Enemy implements IRenderableMultiPolygon
{
	private static final UfoPool pool = new UfoPool(); 
	private static final Vector2[][] verts = new Vector2[2][];
	private static final float RADIUS = 2;
	private static final float SPEED = 16;
	private static final float RELOAD_TIME = 0.3f;
	private static final float PROJECTILE_SPEED = 20;
	private static final int PROJECTILE_LENGTH = 2;
	private static final float MAX_FIRE_DST_2 = 625;
	
	private static class UfoPool extends Pool<Ufo>
	{
		@Override
		protected Ufo newObject()
		{
			return new Ufo();
		}
	}

	static
	{
		verts[0] = new Vector2[10];
		verts[1] = new Vector2[10];
		for(int n = 0; n < 10; n++)
		{
			verts[0][n] = new Vector2(RADIUS, 0).rotate(n * (360f/10f));
			verts[1][n] = new Vector2(RADIUS/2f, 0).rotate(n * (360f/10f));
		}
	}
	
	private final FixtureTag _fixtureTag = new FixtureTag(this, this);
	
	private float _reloadTime;
	
	public static Ufo spawn(float x, float y, float angle)
	{
		Ufo j = pool.obtain();
		
		j._killScore = 100;
		
		j._body = B2d
				.kinematicBody()
				.at(Z.ship().origin().x + x, Z.ship().origin().y + y)
				.rotated((float)Math.toRadians(angle))
				.linearVelocity(Z.v1().set(SPEED, 0).rotate(angle))
				.withFixture(B2d
						.circle()
						.radius(RADIUS)
						.category(ZipZapSim.COL_CAT_METEORITE)
						.mask(ZipZapSim.COL_CAT_SHIP)
						.userData(j._fixtureTag))
				.create(Z.sim().world());
		
		j.onSpawn();
		Z.sim().entities().add(j);
		
		return j;
	}
	
	public static Ufo spawn(float angle)
	{
		Z.v1().set(ZipZapSim.SPAWN_DISTANCE, 0).rotate((float)(angle + 180 + (Game.Dice.nextGaussian() * 90))).add(Z.ship().origin());
		return spawn(Z.v1().x, Z.v1().y, angle);		
	}
	
	private Ufo()
	{
		_behaviours.add(new DieOnHitBehaviour(Color.YELLOW, 12, true, Color.YELLOW, 1));
		_behaviours.add(KillOnContactBehaviour.alsoDie());
		_behaviours.add(DieOnRangeBehaviour.instance());
		
		_killScore = 300;
	}
	
	@Override
	public void onFree()
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
		return 0;
	}

	@Override
	public Vector2 origin(int poly)
	{
		return _body.getWorldCenter();
	}

	@Override
	public Vector2[] verts(int poly)
	{
		return verts[poly];
	}

	@Override
	public Color color(int poly)
	{
		return Color.CYAN;
	}

	@Override
	public float lineWidth(int poly)
	{
		return 1f;
	}
	
	@Override
	public void update(float dt) 
	{
		super.update(dt);
		
		if(!_dead)
		{
			_reloadTime -= dt;
			if(_reloadTime < 0)
			{
				if(origin().dst(Z.ship().origin()) < MAX_FIRE_DST_2)
					fire();
				_reloadTime = RELOAD_TIME;
			}
		}
	}

	private void fire() 
	{
		Projectile.spawn(origin(), Z.v1().set(Z.ship().origin()).sub(origin()).nor().mul(PROJECTILE_SPEED), PROJECTILE_LENGTH, Color.RED, false);
	}
	
	@Override
	public boolean isLoop(int poly)
	{
		return true; // todo
	}
		
	@Override
	public float clipRadius()
	{
		return 4f;
	}
}
