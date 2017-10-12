package com.monkeysonnet.zipzap.entities;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.FixtureTag;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.engine.editor.Shape;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.NumberedFixtureTag;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.DieOnHitBehaviour;
import com.monkeysonnet.zipzap.behaviours.FaceDirectionOfTravelBehaviour;
import com.monkeysonnet.zipzap.behaviours.KillOnContactBehaviour;
import com.monkeysonnet.zipzap.behaviours.DieOnRangeBehaviour;
import com.monkeysonnet.zipzap.behaviours.OrbitBehaviour;

public class Tank extends Enemy implements IRenderableMultiPolygon
{
	private static final float LASER_ENERGY = 3f;
	private static final float MAX_ENERGY = 5f;
	private static final float ENERGY_DRAIN_RATE = 4f;
	private static final float SPEED = 8f;
	protected static final float FIREBALL_SPEED = 18f;
	private static final float RELOAD_TIME_MS = 2000;
	private static final float FIRE_DELAY = 500;
	
	private static final ArmadilloPool pool = new ArmadilloPool(); 
	private static final Map verts = new Map("tank.v");
	private static final Color color = new Color();	
	
	private final FixtureTag[] _fixtureTags = new FixtureTag[verts.numShapes()];
	private Fixture _cockpitFixture;
	private float[] _temps = new float[verts.numShapes()];	
	private int _gunPolyNum;
	private Timeline _fireTimeline;
	
	private static class ArmadilloPool extends Pool<Tank>
	{
		@Override
		protected Tank newObject()
		{
			return new Tank();
		}
	}
	
	private Tank()
	{
		for(int n = 0; n < verts.numShapes(); n++)
			_fixtureTags[n] = new NumberedFixtureTag(this, this, n);
		
		_behaviours.add(DieOnRangeBehaviour.instance());
		_behaviours.add(new OrbitBehaviour(20, 30, 60));
		_behaviours.add(FaceDirectionOfTravelBehaviour.instance());
		_behaviours.add(KillOnContactBehaviour.alsoDie());
	}
	
	private final TweenCallback fireCallback = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			if(type == TweenCallback.START)
			{
				Z.v1()
						.set(Z.ship().origin())
						.sub(_body.getWorldCenter())
						.nor()
						.mul(FIREBALL_SPEED);
				
				Fireball.spawn(_body.getWorldCenter(), Z.v1());
			}
		}
	};
	
	public static void spawn(float x, float y)
	{
		Tank a = pool.obtain();
		
		float angleToShip = Z.v1()
				.set(Z.ship().origin())
				.sub(x, y)
				.angle();
		
		a._body = B2d.kinematicBody()
				.at(x, y)
				.rotated(angleToShip)
				.linearVelocity(Z.v1().set(SPEED, 0).rotate(angleToShip))
				.create(Z.sim().world());
		
		for(int n = 0; n < verts.numShapes(); n++)
		{
			if(verts.shape(n).label != null && verts.shape(n).label.equals("gun"))
				a._gunPolyNum = n;
			
			if(verts.shape(n).label != null)
				continue;
			
			Fixture f = B2d
					.loop(verts.shape(n).shape)
					.category(ZipZapSim.COL_CAT_METEORITE)
					.mask(ZipZapSim.COL_CAT_SHIP)
					.userData(a._fixtureTags[n])
					.create(a._body);
			
			if(n == 0)
				a._cockpitFixture = f;
			
			a._temps[n] = 0;			
		}
		
		Z.sim().entities().add(a);
		
		a._fireTimeline = Timeline.createSequence()
			.push(Tween.call(a.fireCallback))
			.pushPause(FIRE_DELAY)
			.push(Tween.call(a.fireCallback))
			.pushPause(FIRE_DELAY)
			.push(Tween.call(a.fireCallback))
			.pushPause(FIRE_DELAY)
			.repeat(Tween.INFINITY, RELOAD_TIME_MS)
			.start(Z.sim().tweens());
		
		a.onSpawn();
	}
	
	@Override
	public boolean hit(Fixture f, boolean mega, Vector2 loc, Vector2 norm)
	{
		if(mega || f == _cockpitFixture)
		{
			DieOnHitBehaviour.basic().hit(this, f, mega, loc, norm);			
		}
		else
		{
			int num = ((NumberedFixtureTag)f.getUserData()).num();
			
			_temps[num] += LASER_ENERGY;
			if(_temps[num] > MAX_ENERGY)
			{
				_body.destroyFixture(f);				
				Z.sim().spawnDebris(this, num, _body.getLinearVelocity());				
				_temps[num] = -1;
			}					
		}
		
		return false;
	}
	
	@Override
	public void update(float dt)
	{
		super.update(dt);
		
		if(!_dead)
		{
			for(int n = 0; n < _temps.length; n++)
			{
				if(_temps[n] > 0)
				{
					_temps[n] -= dt * ENERGY_DRAIN_RATE;
					if(_temps[n] < 0)
						_temps[n] = 0;
				}
				
			}
		}
	}

	@Override
	public int getNumPolys()
	{
		return verts.numShapes();
	}

	@Override
	public float angle(int poly)
	{
		if(poly ==_gunPolyNum)
		{
			return Z.v1()
				.set(Z.ship().origin())
				.sub(_body.getWorldCenter())
				.angle();
		}
		else return (float)Math.toDegrees(_body.getAngle());
	}

	@Override
	public Vector2 origin(int poly)
	{
		return _body.getWorldCenter();
	}

	@Override
	public Vector2[] verts(int poly)
	{
		return verts.shape(poly).shape;
	}

	@Override
	public Color color(int poly)
	{
		if(poly == 0)
			return Color.RED;
		else
		{
			if(_temps[poly] < 0)
				return null;
			
			float tr1 = _temps[poly] / MAX_ENERGY;
			float tr2 = _temps[poly] < (MAX_ENERGY / 2f) ? 0 : ((_temps[poly] - (MAX_ENERGY/2f)*2)/MAX_ENERGY);
			
			color.set(0.5f + 0.5f * tr1, 0.5f + 0.5f * tr1, 0.5f+0.5f*tr2, 1f);
			return color;
		}
	}
	
	@Override
	public float lineWidth(int poly)
	{
		return 1f;
	}
	
	@Override
	public void onFree()
	{
		killFireTileline();
		Z.sim().tweens().killTarget(this);
		pool.free(this);
	}
	
	private void killFireTileline()
	{
		if(_fireTimeline != null)
		{
			_fireTimeline.kill();
			_fireTimeline = null;
		}
	}
	
	@Override
	public boolean isLoop(int poly)
	{
		return verts.shape(poly).type == Shape.TYPE_LOOP;
	}
	
	@Override
	public float clipRadius()
	{
		return 4f;
	}
}
