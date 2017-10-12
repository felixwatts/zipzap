package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.ICallback;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.engine.editor.Shape;
import com.monkeysonnet.zipzap.IAttractor;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.Sim;
import com.monkeysonnet.zipzap.Tools;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.DieOnHitBehaviour;
import com.monkeysonnet.zipzap.behaviours.ExplosionOnHitBehaviour;
import com.monkeysonnet.zipzap.behaviours.FaceDirectionOfTravelBehaviour;
import com.monkeysonnet.zipzap.behaviours.FireWhenFacingBehaviour;
import com.monkeysonnet.zipzap.behaviours.HomingBehaviour;
import com.monkeysonnet.zipzap.behaviours.IBehaviour;
import com.monkeysonnet.zipzap.behaviours.KillOnContactBehaviour;
import com.monkeysonnet.zipzap.behaviours.OrbitBehaviour;
import com.monkeysonnet.zipzap.behaviours.RandomSwitchingBehaviour;
import com.monkeysonnet.zipzap.script.IActiveCount;

public class Fighter extends Enemy implements IRenderableMultiPolygon, IAttractor
{
	private static final float SPEED = 20f;
	
	private static int _activeCount;

	private static Fixture _cockpitFixture;
	
	private static final IBehaviour[] behaviours = new IBehaviour[]
	{
		new HomingBehaviour(45, 90, false),
		//new HomingBehaviour(45, 12, false),
		//new HomingBehaviour(360, 90, true),
		new OrbitBehaviour(30, 35, 25)
	};
	
//	private Timeline _timeline;
//	private LaserBeam _beam;
	
	public static final IActiveCount activeCount = new IActiveCount()
	{
		@Override
		public int activeCount()
		{
			return _activeCount;
		}
	};
	
	private static final FighterPool pool = new FighterPool();
	private static class FighterPool extends Pool<Fighter>
	{
		@Override
		protected Fighter newObject()
		{
			return new Fighter();
		}
	}
	
	private static final Map map = new Map("armadillo2.v", 0.5f, 0);
	private static final Color[] colors = Tools.mapColours(map);
	protected static final float PROJECTILE_SPEED = 30;
	
	private Fighter()
	{
		_behaviours.add(new FireWhenFacingBehaviour(20, 0.25f, fireProjectileCallback));
		_behaviours.add(new RandomSwitchingBehaviour(1, behaviours));
		_behaviours.add(ExplosionOnHitBehaviour.cyan());
		_behaviours.add(new DieOnHitBehaviour(Color.CYAN, 16, true, Color.CYAN, 8));
		_behaviours.add(FaceDirectionOfTravelBehaviour.instance());
		_behaviours.add(KillOnContactBehaviour.alsoDie());
		_killScore = 500;
	}
	
	private final ICallback fireProjectileCallback = new ICallback()
	{		
		@Override
		public void callback(Object arg)
		{
			Projectile.spawn(origin(), Vector2.tmp.set(PROJECTILE_SPEED, 0).rotate(angle()), 2, Color.CYAN, true);
		}
	}; 

	public static void spawn()
	{
		Fighter l = pool.obtain();	
		
		l._body = B2d.kinematicBody()
				.at(Tools.randomSpawnLoc())
				.linearVelocity(Vector2.tmp.set(SPEED, 0))
				.create(Z.sim().world());	
		
		for(int n = 0; n < map.numShapes(); n++)
		{
			Shape s = map.shape(n);
			if(s.type == Shape.TYPE_LOOP)
			{
				Fixture f = B2d
					.loop(s.shape)
					.category(ZipZapSim.COL_CAT_METEORITE)
					.mask(ZipZapSim.COL_CAT_SHIP)
					.userData(l._fixtureTag)
					.create(l._body);
				
				if(s.hasProperty("cockpit"))
					_cockpitFixture = f;
			}
		}
		
//		l._beam = LaserBeam.spawn(Color.CYAN);
//		
//		l._behaviours.add(orbitBehaviour);
//		
//		l._timeline = Timeline.createSequence()
//				.pushPause((ORBIT_TIME + (Game.Dice.nextFloat() * MAX_TIMELINE_DELAY)) * 1000)
//				.push(Tween.call(l.lineupCallback))
//				.pushPause(LINEUP_TIME * 1000)
//				.push(Tween.call(l.chargeCallback))
//				.pushPause(CHARGE_TIME * 1000)
//				.push(Tween.call(l.fireCallback))
//				.pushPause(FIRE_TIME * 1000)
//				.push(Tween.call(l.fleeCallback))
//				.pushPause(FLEE_TIME * 1000)
//				.push(Tween.call(l.orbitCallback))
//				.repeat(Tween.INFINITY, 1)
//				.start(Z.sim.tweens());
				
		Z.sim().entities().add(l);
		
		l.onSpawn();
	
		_activeCount++;
	}
	
//	private final TweenCallback lineupCallback = new TweenCallback()
//	{
//		@Override
//		public void onEvent(int type, BaseTween<?> source)
//		{
//			_behaviours.removeValue(orbitBehaviour, true);
//			_behaviours.add(homingBehaviour);
//		}
//	};
//	
//	private final TweenCallback chargeCallback = new TweenCallback()
//	{
//		@Override
//		public void onEvent(int type, BaseTween<?> source)
//		{
//			_behaviours.removeValue(homingBehaviour, true);
//			_beam.mode(LaserBeam.MODE_WARM_UP);
//			_beam.set(origin(), angle());
//		}
//	};
//	
//	private final TweenCallback fireCallback = new TweenCallback()
//	{
//		@Override
//		public void onEvent(int type, BaseTween<?> source)
//		{
//			_beam.mode(LaserBeam.MODE_ON);
//		}
//	};
//	
//	private final TweenCallback fleeCallback = new TweenCallback()
//	{
//		@Override
//		public void onEvent(int type, BaseTween<?> source)
//		{
//			_beam.mode(LaserBeam.MODE_OFF);			
//			_behaviours.add(fleeingBehaviour);
//		}
//	};
//	
//	private final TweenCallback orbitCallback = new TweenCallback()
//	{
//		@Override
//		public void onEvent(int type, BaseTween<?> source)
//		{
//			_behaviours.removeValue(fleeingBehaviour, true);
//			_behaviours.add(orbitBehaviour);
//		}
//	};
	
	@Override
	public Color color(int poly)
	{
		return colors[poly];
	}
	
	@Override
	protected void onFree()
	{
//		_timeline.kill();
//		_timeline = null;		
//		_beam.free();
//		_beam = null;
		_activeCount--;
		pool.free(this);
	}	
	
	@Override
	public boolean hit(Fixture f, boolean mega, Vector2 loc, Vector2 norm)
	{
		if(f != _cockpitFixture)
		{
			_behaviours.removeValue(behaviours[1], true);
			_behaviours.add(behaviours[0]);
			
			return super.hit(f, mega, loc, norm);
		}
		else
		{
			Z.sim.spawnDebris(this, _body.getLinearVelocity());
			Z.sim.fireEvent(Sim.EV_RUMBLE, 0.2f);
			Z.sim.spawnFlash(origin(), Color.CYAN);
			Z.sim.spawnCloud(origin(), 1, Color.BLUE, 16);
			Z.sim.spawnCloud(origin(), 1, Color.CYAN, 8);
			Z.sim.spawnCloud(origin(), 1, Color.WHITE, 4);
			
			PowerUp.spawn(origin(), PowerUp.TYPE_SHIELD);
			
			free();
			
			return false;
		}
	}

	@Override
	public int getNumPolys()
	{
		return map.numShapes();
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
		return map.shape(poly).shape;
	}

	@Override
	public float lineWidth(int poly)
	{
		return 1f;
	}

	@Override
	public boolean isLoop(int poly)
	{
		return map.shape(poly).type == Shape.TYPE_LOOP;
	}
	
	@Override
	public float clipRadius()
	{
		return 4f;
	}
}
