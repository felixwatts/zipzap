package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.zipzap.IRenderablePolygon;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.DieOnHitBehaviour;
import com.monkeysonnet.zipzap.behaviours.DieOnRangeBehaviour;
import com.monkeysonnet.zipzap.behaviours.ExplosionOnHitBehaviour;
import com.monkeysonnet.zipzap.behaviours.KillOnContactBehaviour;
import com.monkeysonnet.zipzap.script.IActiveCount;

public class Meteor2 extends Enemy implements IRenderablePolygon
{
	private static final int KILL_SCORE = 15;
	private static final int SPLIT_SCORE = 5;
	private static final float MIN_SIZE = 2f;
	private static final float SEPARATE_SPEED = 5f;	
	private static final int NUM_VERTS = 6;
	private Vector2[] _verts = new Vector2[NUM_VERTS];
	private static final float MAX_ANGULAR_VEL = 6;
	private static final float START_SIZE = 4;
	private int _powerup;
	protected static int _activeCount;
	private float _size;
	private boolean _invincible;
	private static int _activeCountMegaLaser;
	private boolean _doSplit;
	
	private static final Meteor2Pool pool = new Meteor2Pool();
	private static class Meteor2Pool extends Pool<Meteor2>
	{
		@Override
		protected Meteor2 newObject()
		{
			return new Meteor2();
		}
	}
	
	public static final IActiveCount activeCount = new IActiveCount()
	{
		public int activeCount() { return _activeCount; }
	};	
	
	public static final IActiveCount activeCountMegaLaser = new IActiveCount()
	{
		public int activeCount() { return _activeCountMegaLaser; }
	};	
	
	public static void spawn(int powerup, float speed, float angle)
	{
		spawn(powerup, speed, angle, false);
	}
	
	public static void spawn(int powerup, float speed, float angle, boolean invincible)
	{
		if(!PowerUp.canSpawn(powerup))
			return;
		
		if(angle == 0)
			angle = Game.Dice.nextFloat()*360;
		Vector2 pos = Z.sim().vector().obtain().set(ZipZapSim.SPAWN_DISTANCE, 0).rotate(angle + ((Game.Dice.nextFloat() -0.5f) * 90)).add(Z.ship().origin());
		Vector2 vel = Z.sim().vector().obtain().set(speed, 0).rotate(angle+180);
		
		Meteor2 o = spawn(pos, START_SIZE *  (1 + (Game.Dice.nextFloat())), vel);
		
		o._doSplit = false;
		
		o._invincible = invincible;

		o._behaviours.removeValue(ExplosionOnHitBehaviour.cyan(), true);
		if(invincible)
			o._behaviours.add(ExplosionOnHitBehaviour.cyan());

		Z.sim().vector().free(vel);
		Z.sim().vector().free(pos);		
		
		o._powerup = powerup;
		
		if(powerup == PowerUp.TYPE_MEGA_LASER)
			_activeCountMegaLaser++;
	}
	
	public static Meteor2 spawn(Vector2 loc, float size, Vector2 vel)
	{
		if(size < MIN_SIZE)
			return null;
		
		Meteor2 o = pool.obtain();
		
		o._invincible = false;
		
		o._behaviours.clear();			
		o._behaviours.add(KillOnContactBehaviour.alsoDie());
		o._behaviours.add(DieOnRangeBehaviour.instance());
		o._behaviours.add(DieOnHitBehaviour.basic());
		o._behaviours.add(ExplosionOnHitBehaviour.green());
		
		_activeCount++;
		
		o._powerup = PowerUp.TYPE_NONE;
		o._size = size;
		o._killScore = size - 1 >= MIN_SIZE ? 0 : KILL_SCORE;

		float minRadius = size * 0.8f;
		float radiusVariance = size * 0.4f;
		float slice = 360f / NUM_VERTS;	
		//float scale = 1 + (Game.Dice.nextFloat());
		for(int n = 0; n < NUM_VERTS; n++)
		{
			float angleOffset = (Game.Dice.nextFloat() - 0.5f) * slice;
			
			o._verts[n] = Z.sim().vector().obtain()
				.set((Game.Dice.nextFloat() * radiusVariance) + minRadius, 0)
		//		.mul(scale)
				.rotate((n * slice) + angleOffset);
		}

		o._body = B2d
			.dynamicBody()
			.at(loc)
			.angularVelocity((Game.Dice.nextFloat() - 0.5f) * MAX_ANGULAR_VEL * 2)
			.linearVelocity(vel)
			.userData(o)			
			.withFixture(B2d
					.polygon(o._verts)
					.density(1f)
					.userData(o._fixtureTag)
					.category(ZipZapSim.COL_CAT_METEORITE)
					.mask(ZipZapSim.COL_CAT_LASER | ZipZapSim.COL_CAT_BUBBLE | ZipZapSim.COL_CAT_SHIP))
			.create(Z.sim().world());

		o.onSpawn();
		
		Z.sim().entities().add(o);
		
		return o;
	}
	
	@Override
	public void onFree()
	{
		if(_killed && (_powerup != PowerUp.TYPE_NONE))
		{
			PowerUp.spawn(origin(), _powerup);			
			Z.sim().flash(color());
			
			if(_powerup == PowerUp.TYPE_MEGA_LASER)
				_activeCountMegaLaser--;
		}
		
		if(_doSplit)
		{
			float nextSize = _size * 0.75f;
			if(nextSize >= MIN_SIZE && _powerup == PowerUp.TYPE_NONE)
			{
				Z.v1().set(nextSize, 0).rotate(Z.ship().angle()+90);
				Z.v2().set(Z.v1()).mul(SEPARATE_SPEED).add(_body.getLinearVelocity());
				Vector2.tmp.set(origin()).add(Z.v1());
				
				spawn(Vector2.tmp, nextSize, Z.v2());
				
				Vector2.tmp.set(origin()).sub(Z.v1());
				Z.v2().set(Z.v1()).mul(-SEPARATE_SPEED).add(_body.getLinearVelocity());
				
				spawn(Vector2.tmp, nextSize, Z.v2());
				
				Z.screen.sim().score(origin(), SPLIT_SCORE, false);
			}
			else
			{
				Z.sim().spawnFlash(origin(), color());
			}
		}
		
		for(int n = 0; n < _verts.length; n++)
		{
			if(_verts[n] != null)
			{
				Z.sim().vector().free(_verts[n]);
				_verts[n] = null;
			}
		}

		pool.free(this);
		_activeCount--;								
	}

	@Override
	public Vector2[] verts()
	{
		return _verts;
	}

	@Override
	public Color color()
	{
		switch(_powerup)
		{
			case PowerUp.TYPE_NONE:	
			case PowerUp.TYPE_ULTRA_CAPACITOR:
				return _invincible ? Color.GRAY : Color.GREEN;
			default:
				return PowerUp.colorForType(_powerup);
		}
	}

	@Override
	public float lineWidth()
	{
		return 1f;
	}
	
	@Override
	public boolean hit(Fixture f, boolean mega, Vector2 loc, Vector2 norm)
	{
		if(_invincible && !mega)
			return true;
		
		if(!_dead)
		{
			Z.sim.fireEvent(ZipZapSim.EV_EXPLOSION_SMALL, null);
			
			if(mega)
			{			
				_killScore = 4 * KILL_SCORE;
				Z.sim().spawnFlash(origin(), Color.MAGENTA);			
			}
			else
			{
				_doSplit = true;
			}
		}
		
		return super.hit(f, mega, loc, norm);
	}	
	
	@Override
	public float clipRadius()
	{
		return 8f;
	}
	
	@Override
	public void onKill()
	{
		_killed = true;
	}
}
