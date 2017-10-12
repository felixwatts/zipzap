package com.monkeysonnet.zipzap.entities;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Quad;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.engine.BodyTweener;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.zipzap.IAttractor;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.FaceDirectionOfTravelBehaviour;
import com.monkeysonnet.zipzap.script.IActiveCount;

public class Warpey extends EnemyBasic implements IAttractor
{
	private static final float INNER_DISTANCE = 30;
	private static final float APPROACH_TIME = 1;
	private static final float CHARGE_TIME = 0.5f;
	private static final float FIRE_TIME = 0.1f;	
	private static final float MAX_CHARGE_GLOW_RADIUS = 8f; 
	private static final int KILL_SCORE = 50;
	private static final Map map = new Map("enterprise.v", 1f, 180);	
	private static final WarpeyPool pool = new WarpeyPool();
	private static final Color glowColor = new Color(0f, 1f, 1f, 0.2f);

	private static int _activeCount;
	public static final IActiveCount activeCount = new IActiveCount()
	{
		@Override
		public int activeCount()
		{
			return _activeCount;
		}
	};
	
	private static class WarpeyPool extends Pool<Warpey>
	{
		@Override
		protected Warpey newObject()
		{
			return new Warpey();
		}
	}
	
	private LaserBeam _beam;
	private Timeline _timeline;
	private boolean _charging;
	private float _chargeLevel;
	
	private Warpey()
	{
		_behaviours.removeValue(FaceDirectionOfTravelBehaviour.instance(), true);
		_killScore = KILL_SCORE;
	}
	
	public static void spawn()
	{
		Warpey w = pool.obtain();
		
		w.setup(map, 0);
		w._charging = false;
		
		boolean horz = Game.Dice.nextBoolean();
		boolean low = Game.Dice.nextBoolean();
		
		float x, y, angle, tx, ty, fx, fy;
		if(horz)
		{
			if(low)
			{
				x =  -ZipZapSim.SPAWN_DISTANCE;
				y = (float)Game.Dice.nextGaussian() * ZipZapSim.SPAWN_DISTANCE;
				angle = 0;
				
				tx = -INNER_DISTANCE;
				ty = y;
				
				fx = ZipZapSim.SPAWN_DISTANCE;
				fy = y;
			}
			else
			{
				x =  ZipZapSim.SPAWN_DISTANCE;
				y = (float)Game.Dice.nextGaussian() * ZipZapSim.SPAWN_DISTANCE;
				angle = 180;
				
				tx = INNER_DISTANCE;
				ty = y;
				
				fx = -ZipZapSim.SPAWN_DISTANCE;
				fy = y;
			}
		}
		else
		{
			if(low)
			{
				x = (float)Game.Dice.nextGaussian() * ZipZapSim.SPAWN_DISTANCE;
				y =  -ZipZapSim.SPAWN_DISTANCE;
				angle = 90;
				
				tx = x;
				ty = -INNER_DISTANCE;
				
				fx = x;
				fy = ZipZapSim.SPAWN_DISTANCE;
			}
			else
			{
				x = (float)Game.Dice.nextGaussian() * ZipZapSim.SPAWN_DISTANCE;
				y =  ZipZapSim.SPAWN_DISTANCE;
				angle = 270;
				
				tx = x;
				ty = INNER_DISTANCE;
				
				fx = x;
				fy = -ZipZapSim.SPAWN_DISTANCE;
			}
		}
		
		Vector2 p = Z.ship().origin();
		
		x+= p.x;
		y += p.y;
		tx += p.x;
		ty += p.y;
		fx += p.x;
		fy += p.y;

		w._body.setTransform(x, y, (float)Math.toRadians(angle));
		
		w._timeline = Timeline.createSequence()
				.push(Tween.to(w._body, BodyTweener.VAL_POS_XY, APPROACH_TIME * 1000f).target(tx, ty).ease(Quad.OUT))
				.push(Tween.call(w.callbackCharge))
				.pushPause(CHARGE_TIME * 1000f)
				.push(Tween.call(w.callbackStopAttracticles))
				.pushPause(250)
				.push(Tween.call(w.callbackFire))
				.pushPause(FIRE_TIME * 1000f)
				.push(Tween.call(w.callbackEscape))
				.push(Tween.to(w._body, BodyTweener.VAL_POS_XY, APPROACH_TIME * 1000f).target(fx, fy).ease(Quad.IN))
				.push(Tween.call(w.callbackDisappear))
				.start(Z.sim.tweens());

		w._beam = LaserBeam.spawn(Color.CYAN);
		
		_activeCount++;
	}
	
	@Override
	public void update(float dt)
	{
		super.update(dt);
		
		if(!_dead)
		{
			if(_charging)
			{
				Attracticle.spawn(null, this, Color.CYAN, 250f, 6f);				
			}
			
			if(_beam.mode() == LaserBeam.MODE_WARM_UP)	
				_chargeLevel += dt * (1f/CHARGE_TIME);
			
			if(_beam.mode() != LaserBeam.MODE_OFF)				
			{
				_beam.set(origin(), angle());
			}
		}
	}
	
	private final TweenCallback callbackCharge = new TweenCallback()
	{
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			_beam.set(origin(), angle());
			_beam.mode(LaserBeam.MODE_WARM_UP);
			_charging = true;
			_chargeLevel = 0;
		}
	};
	
	private final TweenCallback callbackFire = new TweenCallback()
	{
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			_beam.mode(LaserBeam.MODE_ON);
			_chargeLevel = 0;
		}
	};
	
	private final TweenCallback callbackStopAttracticles = new TweenCallback()
	{
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			_charging = false;
		}
	};
	
	private final TweenCallback callbackEscape = new TweenCallback()
	{
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			_beam.mode(LaserBeam.MODE_OFF);
		}
	};
	
	private final TweenCallback callbackDisappear = new TweenCallback()
	{
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			Z.sim.spawnFlash(origin(), Color.CYAN);
			_timeline = null;
			free();
		}
	};	
	
	protected void onFree() 
	{
		if(_timeline != null)
		{
			_timeline.kill();
			_timeline = null;
		}
		
		_beam.free();
		
		pool.free(this);
		
		_activeCount--;
	};
	
	@Override
	public Color color(int poly)
	{
		if(poly < map.numShapes())
			return Color.CYAN;
		else return _chargeLevel == 0 ? null : glowColor;
	}
	
	@Override
	public int getNumPolys()
	{
		return super.getNumPolys() + 1;
	}
	
	@Override
	public float lineWidth(int poly)
	{
		if(poly < map.numShapes())
			return super.lineWidth(poly);
		else return _chargeLevel * MAX_CHARGE_GLOW_RADIUS;
	}
	
	@Override
	public Vector2[] verts(int poly)
	{
		if(poly < map.numShapes())
			return super.verts(poly);
		else return null;
	}
	
	@Override
	public boolean isLoop(int poly)
	{
		if(poly < map.numShapes())
			return super.isLoop(poly);
		else return true;
	}
}
