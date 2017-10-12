package com.monkeysonnet.zipzap.entities;

import aurelienribon.tweenengine.Tween;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.engine.editor.Shape;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.Tools;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.DieOnHitBehaviour;
import com.monkeysonnet.zipzap.behaviours.DieOnRangeBehaviour;
import com.monkeysonnet.zipzap.behaviours.ExplosionOnHitBehaviour;
import com.monkeysonnet.zipzap.behaviours.FaceDirectionOfTravelBehaviour;
import com.monkeysonnet.zipzap.behaviours.FlyingSoundBehaviour;
import com.monkeysonnet.zipzap.behaviours.HomingBehaviour;
import com.monkeysonnet.zipzap.behaviours.IBehaviour;
import com.monkeysonnet.zipzap.behaviours.KillOnContactBehaviour;
import com.monkeysonnet.zipzap.behaviours.OrbitBehaviour;
import com.monkeysonnet.zipzap.behaviours.RandomSwitchingBehaviour;
import com.monkeysonnet.zipzap.script.IActiveCount;

public class Ghoster extends Enemy implements IRenderableMultiPolygon
{
	private static int _activeCount;
	private static final int KILL_SCORE = 15;
	private static final float SPEED = 15f;
	
	private static final Map map = new Map("ghoster.v", 2f, -90);
	
	private static final GhosterPool pool = new GhosterPool();
	
	private static final IBehaviour[] behaviours = new IBehaviour[] 
	{
		new HomingBehaviour(360, 30, false),
		new HomingBehaviour(360, 90, false),
		new HomingBehaviour(360, 90, true),
		new OrbitBehaviour(15, 20, 40)
	};	

	private static class GhosterPool extends Pool<Ghoster>
	{
		@Override
		protected Ghoster newObject()
		{
			return new Ghoster();
		}
	}
	
	//private Tween _trailTween;
	private float _ghostTime;
	private final Color _color = new Color();
	
//	private final TweenCallback trailCallback = new TweenCallback()
//	{
//		
//		@Override
//		public void onEvent(int type, BaseTween<?> source)
//		{
//			Ghost.spawn(Ghoster.this, Color.WHITE, Color.WHITE, 0.5f, 0.9f);
//		}
//	};
	
	private Ghoster()
	{
		_behaviours.add(FaceDirectionOfTravelBehaviour.instance());
		_behaviours.add(KillOnContactBehaviour.alsoDie());
		_behaviours.add(ExplosionOnHitBehaviour.white());
		_behaviours.add(DieOnHitBehaviour.basic());
		_behaviours.add(DieOnRangeBehaviour.instance());		
		_behaviours.add(new RandomSwitchingBehaviour(0.2f, behaviours));
		_behaviours.add(new FlyingSoundBehaviour(36));
		
		_color.set(Color.CLEAR);
		
		_killScore = KILL_SCORE;
	}
	
	public static final IActiveCount activeCount = new IActiveCount()
	{
		@Override
		public int activeCount()
		{
			return _activeCount;
		}
	};

	private static final float GHOST_THRESHOLD_2 = 400;
	private static final int SFX_DIE = -1018;
	private static final int SFX_DIE2 = -1024;
	
	public static void spawn()
	{
		Ghoster g = pool.obtain();
		
		g.onSpawn();
		
		g._body = B2d.kinematicBody()
				.at(Tools.randomSpawnLoc())
				.linearVelocity(Vector2.tmp.set(SPEED, 0))
				.withFixture(B2d
						.loop(map.shape("bbox").shape)
						.category(ZipZapSim.COL_CAT_METEORITE)
						.mask(ZipZapSim.COL_CAT_SHIP)
						.userData(g._fixtureTag))
				.create(Z.sim().world());				
		
		//g._trailTween = Tween.call(g.trailCallback).delay(TRAIL_PERIOD).repeat(Tween.INFINITY, TRAIL_PERIOD).start(Z.sim().tweens());
		
		_activeCount++;
		
		Z.sim().entities().add(g);
	}
	
	@Override
	protected void onFree()
	{
//		_trailTween.kill();
//		_trailTween = null;
		Z.sim().tweens().killTarget(_color);
		pool.free(this);
		_activeCount--;
	}
	
	@Override
	public void update(float dt)
	{
		super.update(dt);
		
		if(!_dead)
		{
			_ghostTime += dt;
			
			float targetGhostTime = 0.1f + (Math.min(origin().dst2(Z.ship().origin()), GHOST_THRESHOLD_2)/GHOST_THRESHOLD_2);
			
			if(_ghostTime > targetGhostTime)
			{
				_ghostTime = 0;
				Ghost.spawn(Ghoster.this, Color.WHITE, Color.WHITE, 0.5f, 0.9f);
				
				Z.sim().tweens().killTarget(_color);
				_color.set(Color.WHITE);
				Tween.to(_color,0, targetGhostTime*900f).target(1, 1, 1, 0).start(Z.sim().tweens());
			}
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
	public Color color(int poly)
	{
		return _color;
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
		return 8f;
	}
	
	@Override
	public void onKill()
	{
		_killed = true;
		Z.sim.fireEvent(SFX_DIE, null);
		Z.sim.fireEvent(SFX_DIE2, null);
	}
}
