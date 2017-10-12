package com.monkeysonnet.zipzap.entities;

import aurelienribon.tweenengine.Tween;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.Tools;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.DieOnHitBehaviour;
import com.monkeysonnet.zipzap.behaviours.DieOnRangeBehaviour;
import com.monkeysonnet.zipzap.behaviours.ExplosionOnHitBehaviour;
import com.monkeysonnet.zipzap.behaviours.HomingBehaviour;
import com.monkeysonnet.zipzap.behaviours.IBehaviour;
import com.monkeysonnet.zipzap.behaviours.KillOnContactBehaviour;
import com.monkeysonnet.zipzap.behaviours.OrbitBehaviour;
import com.monkeysonnet.zipzap.behaviours.RandomSwitchingBehaviour;
import com.monkeysonnet.zipzap.script.IActiveCount;

public class Bacterium extends Enemy implements IRenderableMultiPolygon
{
	private static final IBehaviour[] behaviours = new IBehaviour[] 
	{
		new HomingBehaviour(360, 30, false),
		new HomingBehaviour(360, 30, false),
		new HomingBehaviour(360, 90, true),
		new OrbitBehaviour(15, 20, 40)
	};

	private static final Color color = new Color(0, 1, 0.5f, 0.4f);

	private static final BacteriumPool pool = new BacteriumPool();
	private static final float RADIUS = 6f;
	private static final int NUM_VERTS = 10;
	private static final float MAX_STRETCH = 1.2f;
	private static final float SPAWN_PERIOD = 3f;	
	private static final Vector2[] unstretchedVerts = new Vector2[NUM_VERTS];
	private static final float SPEED = 10;
	private static final int MAX_ACTIVE = 20;
	//private static final BlobTrailBehaviour blobTrailBehaviour = new BlobTrailBehaviour(color, 0.25f, 3f, RADIUS);
	
	private static int _activeCount;
	
	private static class BacteriumPool extends Pool<Bacterium>
	{
		@Override
		protected Bacterium newObject()
		{
			return new Bacterium();
		}
	}
	
	private Vector2[] _verts = new Vector2[NUM_VERTS];
	private Vector2 _stretch = new Vector2();
	private Tween _stretchTween;
	private final Vector2 _targetDirection = new Vector2();
	private final Vector2 _speedOffset = new Vector2();
	private final DieOnHitBehaviour dieOnHitBehaviour = new DieOnHitBehaviour(color, 6, false, color, 0);
	private float _spawnTimer;

	private Tween _moveTween;
	
	public static IActiveCount activeCount = new IActiveCount()
	{
		@Override
		public int activeCount()
		{
			return _activeCount;
		}
	};
	
	static
	{
		for(int n = 0; n < NUM_VERTS; n++)
		{
			unstretchedVerts[n] = new Vector2().set(RADIUS, 0).rotate((360f/NUM_VERTS)*n);			
		}
	}
	
	private Bacterium()
	{
		_behaviours.add(KillOnContactBehaviour.alsoDie());
		_behaviours.add(ExplosionOnHitBehaviour.white());
		_behaviours.add(DieOnRangeBehaviour.instance());		
		_behaviours.add(new RandomSwitchingBehaviour(0.2f, behaviours));		
		//_behaviours.add(blobTrailBehaviour);
		_behaviours.add(dieOnHitBehaviour);
	}
	
	public static void spawn()
	{
		spawn(Tools.randomSpawnLoc());
	}
	
	public static void spawn(Vector2 loc)
	{
		Bacterium b = pool.obtain();
		
		b._spawnTimer = SPAWN_PERIOD;
		
		b._body = B2d
				.kinematicBody()
				.at(loc)
				.linearVelocity(Vector2.tmp.set(SPEED, 0).rotate(Game.Dice.nextFloat()*360f))
				.withFixture(B2d
						.circle()
						.radius(RADIUS)
						.category(ZipZapSim.COL_CAT_METEORITE)
						.mask(ZipZapSim.COL_CAT_SHIP)
						.userData(b._fixtureTag))
				.create(Z.sim.world());
		
		b._targetDirection.set(b._body.getLinearVelocity());
		
		for(int n = 0; n < NUM_VERTS; n++)
			b._verts[n] = Z.sim.vector().obtain();
		
		b._stretch.set(MAX_STRETCH, 1);
		
		b._stretchTween = Tween
				.to(b._stretch, 0, 200)
				.target(1, MAX_STRETCH)
			//	.ease(Elastic.INOUT)
				.repeatYoyo(Tween.INFINITY, 0)
				.start(Z.sim.tweens());
		
		b._speedOffset.set(0.1f*SPEED, 0);
		b._moveTween = Tween
				.to(b._speedOffset, 0, 500)
				.target(SPEED, 0)
			//	.ease(Quad.INOUT)
				.repeatYoyo(Tween.INFINITY, 0)
				.start(Z.sim.tweens());
		
		b.onSpawn();
		Z.sim.entities().add(b);
		
		_activeCount++;
	}
	
	@Override
	protected void onFree()
	{
		Z.sim.spawnCloud(origin(), 6, color, RADIUS*2);
		
		_stretchTween.kill();
		_moveTween.kill();
		
		for(int n = 0; n < NUM_VERTS; n++)
		{
			Z.sim.vector().free(_verts[n]);
			_verts[n] = null;
		}
		
		pool.free(this);
		
		_activeCount--;
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
		return _body.getPosition();
	}

	@Override
	public Vector2[] verts(int poly)
	{
		switch(poly)
		{
			case 0:
				return null;
			case 1:
				return _verts;
			default:
				return null;
		}
	}

	@Override
	public Color color(int poly)
	{
		return color;
	}

	@Override
	public float lineWidth(int poly)
	{
		switch(poly)
		{
			case 0:
				return RADIUS;
			case 1:
				return 1.5f;
			default:
				return 0;
		}
	}

	@Override
	public boolean isLoop(int poly)
	{
		return true;
	}
	
	@Override
	public void update(float dt)
	{
		super.update(dt);
		
		if(!_dead)
		{
			updateVerts();
			updateVel();
			
			if(_spawnTimer > 0)
			{
				_spawnTimer -= dt;
				
				color.r = 1 - (_spawnTimer / SPAWN_PERIOD);
				
				if(_spawnTimer < 0 && _activeCount < MAX_ACTIVE)
				{
					Bacterium.spawn(origin());
					Z.sim.spawnCloud(origin(), 6, color, RADIUS*2);
					
					_spawnTimer = SPAWN_PERIOD;
				}
			}
		}
	}

	private void updateVel()
	{
		_body.setLinearVelocity(Vector2.tmp.set(_targetDirection).nor().mul(_speedOffset.x));
	}

	private void updateVerts()
	{
		for(int n = 0; n < NUM_VERTS; n++)
		{
			_verts[n].set(unstretchedVerts[n].x * _stretch.x, unstretchedVerts[n].y * _stretch.y);
		}
	}
	
	@Override
	public void targetDirection(Vector2 v)
	{
		_targetDirection.set(v);
	}
	
	@Override
	public Vector2 targetDirection()
	{
		return _targetDirection;
	}
	
	@Override
	public boolean hit(Fixture f, boolean mega, Vector2 loc, Vector2 norm)
	{
		super.hit(f, mega, loc, norm);		
		Z.sim.spawnCloud(loc, 3, color, RADIUS);
		return false;
	}
	
	@Override
	public float clipRadius()
	{
		return 4f;
	}
}
