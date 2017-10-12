package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.zipzap.IRenderablePolygon;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.DieOnHitBehaviour;
import com.monkeysonnet.zipzap.behaviours.KillOnContactBehaviour;
import com.monkeysonnet.zipzap.script.IActiveCount;

public class ShootingSeeker extends Enemy implements IRenderablePolygon
{
	private static final ShootingSeekerPool pool = new ShootingSeekerPool();
	private static Vector2[] vertsArr;
	private static int _activeCount;
	
	public static final IActiveCount activeCount = new IActiveCount()
	{		
		@Override
		public int activeCount()
		{
			return _activeCount;
		}
	};
	
	private static final int BH_APPROACH = 0;
	private static final int BH_FLEE = 2;
	private static final float SPEED = 10;
	private static final float PROJECTILE_SPEED = 20;
	private static final int ENERGY = 2;
	private static final float FLEE_DST_2 = 225;
	private static final float APPROACH_DST_2 = 2500f;
	private static final float FIRE_PERIOD = 3f;
		
	private int _behaviour;
	private float _fireTime;
	
	static
	{
		Map m = new Map("shooting-seeker.v");
		vertsArr = m.shape(0).shape;
	}
	
	private static class ShootingSeekerPool extends Pool<ShootingSeeker>
	{
		@Override
		protected ShootingSeeker newObject()
		{
			return new ShootingSeeker();
		}
	}
	
	private ShootingSeeker()
	{
		_behaviours.add(KillOnContactBehaviour.alsoDie());
		_behaviours.add(new DieOnHitBehaviour(Color.YELLOW, 16, true, Color.ORANGE, ENERGY));
		
		_killScore = 50;
	}
	
	public static void spawn()
	{
		float angle = Game.Dice.nextFloat() * 360;
		Game.workingVector2a.set(ZipZapSim.SPAWN_DISTANCE, 0).rotate(angle).add(Z.ship().origin());
		spawn(Game.workingVector2a);
	}
	
	public static void spawn(Vector2 loc)
	{
		ShootingSeeker s = pool.obtain();

		Game.workingVector2b.set(Z.ship().origin()).sub(loc).nor().mul(SPEED);
		
		s._body = B2d
				.kinematicBody()
				.at(loc)
				.withFixture(B2d
						.loop(vertsArr)
						.category(ZipZapSim.COL_CAT_METEORITE)
						.mask(ZipZapSim.COL_CAT_SHIP | ZipZapSim.COL_CAT_BUBBLE)
						.userData(s._fixtureTag))
				.create(Z.sim().world());
		
		s._behaviour = BH_APPROACH;
		
		s.onSpawn();
		
		Z.sim().entities().add(s);
		
		_activeCount++;
	}

	@Override
	public float angle()
	{
		return (float)(Math.toDegrees(_body.getAngle()) % 360f);
	}

	@Override
	public Vector2 origin()
	{
		return _body.getPosition();
	}

	@Override
	public Vector2[] verts()
	{
		return vertsArr;
	}

	@Override
	public Color color()
	{
		return Color.RED;
	}

	@Override
	public float lineWidth()
	{
		return 1f;
	}

	@Override
	public void update(float dt)
	{
		super.update(dt);
		if(!_dead)
		{
			if(!Z.ship().dead())
			{					
				Game.workingVector2a.set(Z.ship().origin()).sub(origin());	
				if(_behaviour == BH_APPROACH && Game.workingVector2a.len2() < FLEE_DST_2)
				{
					flee();
				}
				else if(_behaviour == BH_FLEE && Game.workingVector2a.len2() > APPROACH_DST_2)
					_behaviour = BH_APPROACH;
				
				Game.workingVector2a.set(Z.ship().origin()).sub(origin());	
				if(_behaviour == BH_APPROACH)
				{	
					_body.setLinearVelocity(Game.workingVector2a.nor().mul(SPEED));
					_body.setTransform(origin(), (float)Math.toRadians(Game.workingVector2a.angle()));
				}
				
				if(_behaviour == BH_APPROACH)
				{
					if(_fireTime > 0)
						_fireTime -= dt;
					if(_fireTime <= 0)
					{
						Projectile.spawn(origin(), Game.workingVector2a.set(_body.getLinearVelocity()).nor().mul(PROJECTILE_SPEED));
						_fireTime = FIRE_PERIOD;
						
						Z.sim.fireEvent(ZipZapSim.EV_LASER_SMALL, null);
					}
				}
			}				
		}			
	}

	private void flee()
	{
		_behaviour = BH_FLEE;
		Game.workingVector2a.set(Z.ship().origin()).sub(origin());	
		Game.workingVector2b.set(SPEED, 0).rotate(Game.workingVector2a.angle()+180);
		_body.setLinearVelocity(Game.workingVector2b);
		_body.setTransform(origin(), (float)Math.toRadians(Game.workingVector2b.angle()));
	}

	@Override
	public void onFree()
	{
		pool.free(this);
		_activeCount--;
	}

	@Override
	public int layer()
	{
		return 0;
	}

	@Override
	public boolean hit(Fixture f, boolean mega, Vector2 loc, Vector2 norm)
	{
		super.hit(f, mega, loc, norm);
		if(!_dead)
		{
			Z.sim().spawnExlosion(origin());
			flee();
			Z.sim.fireEvent(-1013, null);
		}
		
		return false;
	}	
	
	@Override
	public float clipRadius()
	{
		return 4f;
	}
	
	@Override
	public void onKill()
	{
		Z.sim.fireEvent(ZipZapSim.EV_EXPLOSION_MEDIUM, false);
	}
}
