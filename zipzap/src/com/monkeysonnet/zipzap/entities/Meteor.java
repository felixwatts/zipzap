package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.zipzap.IRenderablePolygon;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.DieOnRangeBehaviour;
import com.monkeysonnet.zipzap.behaviours.KillOnContactBehaviour;
import com.monkeysonnet.zipzap.script.IActiveCount;

public class Meteor extends Enemy implements IRenderablePolygon
{
	private static final MeteorPool pool = new MeteorPool();
	private static final int MAX_INITIAL_VERTS = 10;//5;
	private static final float RADIUS_VARIANCE = 4;
	private static final float MIN_RADIUS = 4;
	private static final float MAX_ANGULAR_VEL = 6;
	public static final float SPLIT_SPEED = 10;
	private static final int KILL_SCORE = 20;
	private static final int SPLIT_SCORE = 10;
	
	public static final Array<Vector2> workingVectorArray1 = new Array<Vector2>();
	private static final Vector2[] tmpTriangle = new Vector2[3];	
	
	private static int _activeCount;
	
	private Vector2[] _verts = new Vector2[MAX_INITIAL_VERTS + 4];
	private int _powerup;
	
	private static class MeteorPool extends Pool<Meteor>
	{
		@Override
		protected Meteor newObject()
		{
			return new Meteor();
		}
	}
	
	static
	{
		tmpTriangle[0] = new Vector2();
		tmpTriangle[1] = new Vector2();
		tmpTriangle[2] = new Vector2();
	}
	
	public static void spawn(float speed, float angleOfTravel, int powerup)
	{
		float angle = Game.Dice.nextFloat() * 360;
		Game.workingVector2a.set(ZipZapSim.SPAWN_DISTANCE, 0).rotate(angle).add(Z.ship().origin());
		Game.workingVector2b.set(1, 0).rotate(angleOfTravel).mul(speed);
		spawn(Game.workingVector2a, Game.workingVector2b, powerup);
	}
	
	public static void spawn(float speed, int powerup)
	{		
		float angle = Game.Dice.nextFloat() * 360;
		Game.workingVector2a.set(ZipZapSim.SPAWN_DISTANCE, 0).rotate(angle).add(Z.ship().origin());
		Game.workingVector2b.set(1, 0).rotate(angle + 180).mul(speed);
		spawn(Game.workingVector2a, Game.workingVector2b, powerup);
	}
	
	public static void spawn(Vector2 pos, Vector2 vel, int powerup)
	{
		_activeCount++;
		Meteor m = pool.obtain();
		m.init(pos, vel, powerup);
		m.onSpawn();
		Z.sim().entities().add(m);
	}
	
	public static void spawn(Array<Vector2> verts, Vector2 pos, Vector2 vel)
	{
		_activeCount++;
		Meteor m = pool.obtain();
		m.init(verts, pos, vel);
		m.onSpawn();
		Z.sim().entities().add(m);
	}
	
	public static final IActiveCount activeCount = new IActiveCount()
	{
		public int activeCount() { return _activeCount; }
	};
	
	private Meteor()
	{
		_behaviours.add(DieOnRangeBehaviour.instance());
		_behaviours.add(KillOnContactBehaviour.alsoDie());
		
		_killScore = KILL_SCORE;
	}
	
	public void split(boolean destroy)
	{
		if(_dead)
			return;
		
		int numVerts = _verts.length;
		while(numVerts > 0 && _verts[numVerts-1] == null)
			numVerts--;
		
		if(_powerup == PowerUp.TYPE_NONE && !destroy && numVerts > 5)
		{
			Z.screen.sim().score(origin(), SPLIT_SCORE, false);
			
			int splitIndex = numVerts / 2;
			
			workingVectorArray1.clear();
			for(int n = 0; n <= splitIndex; n++)
				workingVectorArray1.add(Z.sim().vector().obtain().set(_verts[n]));
			
			Game.workingVector2a.set(0, 0);
			for(Vector2 v : workingVectorArray1)
				Game.workingVector2a.add(v);
			Game.workingVector2a
				.mul(1f/workingVectorArray1.size)
				.nor()
				.rotate(angle())
				.mul(SPLIT_SPEED);
			
			Meteor.spawn(workingVectorArray1, origin(), Game.workingVector2a);
			
			workingVectorArray1.clear();
			for(int n = splitIndex; n <= numVerts; n++)
				workingVectorArray1.add(Z.sim().vector().obtain().set(_verts[n%numVerts]));
			
			Game.workingVector2a.set(0, 0);
			for(Vector2 v : workingVectorArray1)
				Game.workingVector2a.add(v);
			Game.workingVector2a
				.mul(1f/workingVectorArray1.size)
				.nor()
				.rotate(angle())
				.mul(SPLIT_SPEED);
			
			Meteor.spawn(workingVectorArray1, origin(), Game.workingVector2a);
		}
		else
		{
			Z.screen.sim().score(origin(), _killScore, false);
			
			Z.sim().spawnDebris(this, _body.getLinearVelocity());
			
			if(_powerup != PowerUp.TYPE_NONE)
			{
				PowerUp p = PowerUp.spawn(origin(), _powerup);
				Z.sim().flash(p.color());
			}
		}
		
		free();	
	}

	@Override
	public float angle()
	{
		return (float)Math.toDegrees(_body.getAngle());
	}

	@Override
	public Vector2 origin()
	{
		return _body.getPosition();
	}

	@Override
	public Vector2[] verts()
	{
		return _verts;
	}

	@Override
	public Color color()
	{
		switch (_powerup)
		{
			case PowerUp.TYPE_BOMB:
				return Color.RED;
			case PowerUp.TYPE_SHIELD:
				return Color.CYAN;
			case PowerUp.TYPE_MEGA_LASER:
				return Color.MAGENTA;
			case PowerUp.TYPE_DRAGON:
				return Color.ORANGE;
			default:
				return _dead ? Color.GRAY : Color.GREEN;
		}
	}

//	@Override
//	public void update(float dt)
//	{
//		super.update(dt);
//		if(!_dead)
//		{
//			
//		}
//		if(_destroyed || origin().dst2(ZipZapScreen.instance().sim().ship().origin()) > MAX_DST_TO_SHIP_2)
//		{
//			free();
//		}
//	}

	@Override
	public void onFree()
	{
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
	public int layer()
	{
		return 2;
	}

	@Override
	public float lineWidth()
	{
		return 1f;
	}
	
	private void init(Vector2 pos, Vector2 vel, int powerup)
	{
		int numVerts = Game.Dice.nextInt(MAX_INITIAL_VERTS) + 4;
		
		workingVectorArray1.clear();
		float slice = 360f / numVerts;
		
		for(int n = 0; n < numVerts; n++)
		{
			float angleOffset = (Game.Dice.nextFloat() - 0.5f) * slice;
			
			workingVectorArray1.add(Z.sim().vector().obtain()
				.set((Game.Dice.nextFloat() * RADIUS_VARIANCE) + MIN_RADIUS, 0)
				.rotate((n * (360f / numVerts)) + angleOffset));
		}
		
		init(workingVectorArray1, pos, vel);
		_powerup = powerup;	
	}
	
	private void init(Array<Vector2> verts, Vector2 pos, Vector2 vel)
	{
		_powerup = PowerUp.TYPE_NONE;
		
		Vector2 v = Z.sim().vector().obtain().set(vel);

		Game.workingVector2b.set(0, 0);
		for(int n = 0; n < verts.size; n++)
			Game.workingVector2b.add(verts.get(n));
		Game.workingVector2b.mul(1f / verts.size);
		for(int n = 0; n < verts.size; n++)
			verts.get(n).sub(Game.workingVector2b);
		pos.add(Game.workingVector2b);
		
		for(int n = 0; n < verts.size; n++)
			_verts[n] = verts.get(n);
				
		// todo array allocation
		Vector2[] tmp = new Vector2[verts.size];
		for(int n = 0; n < verts.size; n++)
			tmp[n] = verts.get(n);
				
		_body = B2d
			.dynamicBody()
			.at(pos)
			.angularVelocity(verts.size > 3 ? (Game.Dice.nextFloat() - 0.5f) * MAX_ANGULAR_VEL * 2 : 0f)
			.linearVelocity(v)
			.userData(this)
			.withFixture(B2d
					.loop(tmp)
					.userData(_fixtureTag)
					.category(ZipZapSim.COL_CAT_METEORITE)
					.mask(ZipZapSim.COL_CAT_LASER | ZipZapSim.COL_CAT_BUBBLE | ZipZapSim.COL_CAT_SHIP))
			.create(Z.sim().world());
		
		Z.sim().vector().free(v);
	}

	@Override
	public boolean hit(Fixture f, boolean mega, Vector2 loc, Vector2 norm)
	{
		split(mega);
		
		return false;
	}
	
	
	@Override
	public float clipRadius()
	{
		return 8f;
	}
}
