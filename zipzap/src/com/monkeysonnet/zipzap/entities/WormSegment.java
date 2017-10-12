package com.monkeysonnet.zipzap.entities;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.zipzap.IRenderablePolygon;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.ExplosionOnHitBehaviour;
import com.monkeysonnet.zipzap.script.IActiveCount;

public class WormSegment extends Enemy implements IRenderablePolygon
{
	public static final int BH_TUBULON = 0;
	public static final int BH_SNAKEY = 1;
	public static final int BH_BABYTUB = 2;
	
	private static final float TUBULON_SEGMENT_RADIUS = 6f;
	private static final int TUBULON_NUM_SEGMENTS = 16;
	private static final float STEP_SIZE_TUBULON = 6;
	private static final float SPEED_TUBULON = 8f/3f;
	
	private static final float BABYTUB_SEGMENT_RADIUS = 2f;
	private static final int BABYTUB_NUM_SEGMENTS = 9;	
	private static final float STEP_SIZE_BABYTUB = 3;
	private static final float SPEED_BABYTUB = 12f/3f;
	
	private static final float SNAKEY_TURN_CHANCE = 4f;
	private static final float SNAKEY_SEGMENT_RADIUS = 1f;
	private static final int SNAKEY_NUM_SEGMENTS = 16;
	private static final float STEP_SIZE_SNAKEY = 2;
	private static final float SPEED_SNAKEY = 16f/3f;	
	private static final int SNAKEY_MIN_LENGTH = 3;
	
	private static int _activeSegmentCountSnakey;
	
	public static final IActiveCount activeSegmentCountSnakey = new IActiveCount()
	{
		@Override
		public int activeCount()
		{
			return _activeSegmentCountSnakey;
		}
	};
	
	private static class WormSegmentPool extends Pool<WormSegment>
	{
		@Override
		protected WormSegment newObject()
		{
			return new WormSegment();
		}
	}
	
	private static final WormSegmentPool pool = new WormSegmentPool();
	private static final Vector2[] vertsTubulon = new Vector2[6];
	private static final Vector2[] vertsBabytub = new Vector2[6];
	private static final Vector2[] vertsSnakey = new Vector2[4];	
	private static final Vector2[] vertsSnakeySegment = new Vector2[4];	
	//private static final DieOnHitBehaviour dieOnHitBehaviour = new DieOnHitBehaviour(Color.GREEN, 4, false, 0); 
	public static final Color tubulonColor = new Color(0.6f, 1f, 0.6f, 1f);
	
	protected WormSegment _prev, _next;
	private final Vector2 _start = new Vector2(), _end = new Vector2();
	private float _dst;
	protected float _speed, _stepSize;
	private int _behaviour;
	
	static
	{
		for(int n = 0; n < vertsTubulon.length; n++)
		{
			vertsTubulon[n] = new Vector2().set(TUBULON_SEGMENT_RADIUS, 0).rotate((360f/vertsTubulon.length) * n);
		}
		
		for(int n = 0; n < vertsBabytub.length; n++)
		{
			vertsBabytub[n] = new Vector2().set(BABYTUB_SEGMENT_RADIUS, 0).rotate((360f/vertsBabytub.length) * n);
		}
		
		for(int n = 0; n < vertsSnakey.length; n++)
		{
			vertsSnakey[n] = new Vector2().set(SNAKEY_SEGMENT_RADIUS*1.5f, 0).rotate((360f/vertsSnakey.length) * n);
		}
		
		vertsSnakeySegment[0] = new Vector2(-1f, -1f);
		vertsSnakeySegment[1] = new Vector2(1f, -1f);
		vertsSnakeySegment[2] = new Vector2(1f, 1f);
		vertsSnakeySegment[3] = new Vector2(-1f, 1f);
	}
	
	private WormSegment()
	{		
	}
	
	public static WormSegment spawnTubulon(float x, float y)
	{
		return spawn(x, y, vertsTubulon, null, TUBULON_NUM_SEGMENTS, BH_TUBULON);
	}
	
	public static WormSegment spawnSnakey()
	{
		float angle = Game.Dice.nextFloat() * 360f;
		return spawnSnakey(angle);
	}
	
	public static WormSegment spawnSnakey(float angle)
	{
		Z.v1().set(ZipZapSim.SPAWN_DISTANCE, 0).rotate(angle);
		return spawn(Z.v1().x, Z.v1().y, vertsSnakey, null, SNAKEY_NUM_SEGMENTS, BH_SNAKEY);
	}
	
	public static WormSegment spawnBabytub(Vector2 loc)
	{
		return spawn(loc.x, loc.y, vertsBabytub, null, BABYTUB_NUM_SEGMENTS, BH_BABYTUB);
	}
	
	public static WormSegment spawn(float x, float y, Vector2[] verts, WormSegment prev, int numSegments, int behaviour)
	{
		if(numSegments == 0)
			return null;
		
		WormSegment s = pool.obtain();
		
		s._dst = 0;
		s._prev = prev;
		s._behaviour = behaviour;
		s._start.set(x, y);
		s._end.set(x, y);
		
//		s._behaviours.clear();
//		
		float radius = 0;
		
		switch(behaviour)
		{
			case BH_TUBULON:
				s._stepSize = STEP_SIZE_TUBULON;
				s._speed = SPEED_TUBULON;
				radius = TUBULON_SEGMENT_RADIUS;
				s._killScore = 0;
				break;
			case BH_BABYTUB:
			//	s._behaviours.add(dieOnHitBehaviour);
				s._behaviours.add(ExplosionOnHitBehaviour.green());
				s._stepSize = STEP_SIZE_BABYTUB;
				s._speed = SPEED_BABYTUB;
				radius = BABYTUB_SEGMENT_RADIUS;
				s._killScore = 10000;
				break;
			case BH_SNAKEY:
				s._stepSize = STEP_SIZE_SNAKEY;
				s._speed = SPEED_SNAKEY;
				s._killScore = 15;
				radius = SNAKEY_SEGMENT_RADIUS;				
				if(prev == null)				
					s
						._end
						.set(Z.ship().origin())
						.sub(s._start)
						.nor()
						.mul(STEP_SIZE_SNAKEY)
						.add(s._start);	
				_activeSegmentCountSnakey++;
				break;
		}
		
		s._body = B2d
				.staticBody()
				.at(x, y)
				.withFixture(B2d
						.circle()
						.radius(radius)
						.sensor(true)
						.category(ZipZapSim.COL_CAT_METEORITE)
						.mask(ZipZapSim.COL_CAT_SHIP)
						.userData(s._fixtureTag))
				.create(Z.sim().world());
		
		s.onSpawn();
		
		Z.sim().entities().add(s);
		
		s._next = WormSegment.spawn(x, y, verts, s, numSegments-1, behaviour);
		
		return s;
	}
	
	@Override
	public void update(float dt)
	{
		super.update(dt);
		
		if(!_dead)
		{
			if(_prev == null)
			{
				// head
				
				_dst += _speed*dt;
				
				if(_dst > 1f)
				{
					_dst -= 1f;					
					steer(dt);
					
					WormSegment s = _next;
					while(s != null)
					{
						s._start.set(s._end);
						s._end.set(s._prev._start);
						s = s._next;
					}
				}
				
				WormSegment s = _next;
				while(s != null)
				{
					s._dst = _dst;
					s = s._next;
				}
			}
			
			Z.v1()
				.set(_end)
				.sub(_start)
				.mul(_dst)
				.add(_start);
			
			_body.setTransform(
					Z.v1(), 
					(float)Math.toRadians(
						Vector2.tmp
						.set(_end)
						.sub(_start)
						.angle()));
		}
	}
	
	public int behaviour()
	{
		return _behaviour;
	}
	
	private void steer(float dt)
	{	
		switch(_behaviour)
		{
			case BH_TUBULON:
			case BH_BABYTUB:
			default:
				if(!Z.ship().dead())
				{
					_start.set(_end);
					
					Vector2.tmp
						.set(Z.ship().origin())
						.sub(_end)
						.nor()
						.mul(_stepSize);
					
					_end.add(Vector2.tmp);
				}
				else
				{
					Z.v1().set(_end).sub(_start).add(_end);
					_start.set(_end);
					_end.set(Z.v1());
				}
				break;
				
			case BH_SNAKEY:
				
				if(!Z.ship().dead() && (Game.Dice.nextFloat() < (SNAKEY_TURN_CHANCE*dt)))
				{
					turn();
				}
				else
				{
					Z.v1().set(_end).sub(_start).add(_end);
					_start.set(_end);
					_end.set(Z.v1());
				}
				
				break;
				
		}		
	}
	
	@Override
	public void onBeginContact(Contact c, Fixture me, Fixture other)
	{
		super.onBeginContact(c, me, other);
		
		if(!_dead)
		{
			if(other.getBody().getUserData() == Z.ship())
			{
				Z.ship().strike();
			}
		}
	}
	
	@Override
	public void onFree()
	{
		switch(_behaviour)
		{
			case BH_BABYTUB:
				
				Z.sim().spawnCloud(origin(), 3, Color.GREEN, BABYTUB_SEGMENT_RADIUS*2f);
				if(_next != null)
				{
					//_next.free();
					_next._prev = null;
					_next = null;
				}
//				
//				if(_prev != null)
//				{
//					_prev.free();
//					_prev = null;
//				}
				
				//Z.sim().spawnCloud(origin(), 1, color(), TUBULON_SEGMENT_RADIUS);
				
				//Z.sim().spawnExlosion(origin(), 4, color(), TUBULON_SEGMENT_RADIUS*2f);
				//Z.sim().spawnExlosion(origin(), 3, Color.RED);
				
//				if(_prev != null)
//				{					
//					Tween.call(killSegmentCallback).setUserData(_prev).delay(20).start(Z.sim().tweens());
//				}
//				
//				if(_next != null)
//				{
//					Tween.call(killSegmentCallback).setUserData(_next).delay(20).start(Z.sim().tweens());
//				}
				
			//	_behaviours.removeValue(dieOnHitBehaviour, true);
				_behaviours.removeValue(ExplosionOnHitBehaviour.green(), true);
				
				break;
			
			case BH_TUBULON:
				
				Z.sim().spawnCloud(origin(), 1, color(), TUBULON_SEGMENT_RADIUS*4f);
				
				//Z.sim().spawnExlosion(origin(), 4, color(), TUBULON_SEGMENT_RADIUS*2f);
				//Z.sim().spawnExlosion(origin(), 3, Color.RED);
				
				if(_prev != null)
				{					
					Tween.call(killSegmentCallback).setUserData(_prev).delay(5).start(Z.sim().tweens());
				}
				
				if(_next != null)
				{
					Tween.call(killSegmentCallback).setUserData(_next).delay(5).start(Z.sim().tweens());
				}

				break;
				
			case BH_SNAKEY:
				
				Z.sim().spawnExlosion(origin(), 4, color());

				if(_prev != null)
				{
					_prev._next = null;
					
					if(_prev.length() < SNAKEY_MIN_LENGTH)
					{
						_prev.free();
						
						//Tween.call(killSegmentCallback).setUserData(_prev).delay(5).start(Z.sim().tweens());
					}
				}
				if(_next != null)
				{
					_next._prev = null;
					
					if(_next.length() < SNAKEY_MIN_LENGTH)
					{
						_next.free();
						//Tween.call(killSegmentCallback).setUserData(_next).delay(5).start(Z.sim().tweens());
					}
					else _next.turn();
				}
				
				_activeSegmentCountSnakey--;
				
				break;
		}
		
		pool.free(this);
	}
	
	private int length()
	{
		int l = 1;
		
		WormSegment seg = _prev;
		while(seg != null)
		{
			l++;
			seg = seg._prev;
		}
		
		seg = _next;
		while(seg != null)
		{
			l++;
			seg = seg._next;
		}
		
		return l;
	}
	
	private static TweenCallback killSegmentCallback = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			WormSegment s = (WormSegment)source.getUserData();			
			s.free();
		}
	};

	@Override
	public float angle()
	{
		return (float)Math.toDegrees(_body.getAngle());
	}

	@Override
	public Vector2[] verts()
	{
		switch(_behaviour)
		{
			case BH_BABYTUB:
				return vertsBabytub;
			case BH_TUBULON:
				default:
				return vertsTubulon;
			case BH_SNAKEY:
				if(_prev == null)
					return vertsSnakey;
				else return vertsSnakeySegment;
		}
	}

	@Override
	public Color color()
	{
		switch(_behaviour)
		{
			case BH_BABYTUB:
			case BH_TUBULON:			
			default:
				return tubulonColor;	
			case BH_SNAKEY:
				if(_prev == null)
					return Color.RED;
				else return Color.WHITE;
		}
	}

	@Override
	public float lineWidth()
	{
		switch(_behaviour)
		{
			case BH_TUBULON:
				return 1.5f;
			default:
				return 1f;
		}
	}
	
	@Override
	public boolean hit(Fixture f, boolean mega, Vector2 loc, Vector2 norm) 
	{
		super.hit(f, mega, loc, norm);
	
		if(!_dead)
		{
			switch(_behaviour)
			{
				case BH_SNAKEY:
					
					Z.screen.sim().score(origin(), _killScore, length() <= SNAKEY_MIN_LENGTH);
					
					free();
					return false;
					
				case BH_BABYTUB:
					
					Z.screen.sim().score(origin(), 100, true);
					
					if(_prev == null)
						free();

					return false;
			}
		}
		
		return false;
	}
	
	private void turn()
	{
		switch(_behaviour)
		{
			case BH_SNAKEY:
				boolean towards = true;//Game.Dice.nextFloat() < SNAKEY_TURN_TOWARDS_CHANCE;
				
				float angleToShip = Z.v1().set(Z.ship().origin()).sub(_end).angle();
				float angleOfTravel =  Z.v1().set(_end).sub(_start).angle();
				float angleToShipRel = angleToShip - angleOfTravel;
				if(angleToShipRel > 180)
					angleToShipRel -= 360;
				if(angleToShip < -180)
					angleToShipRel += 360;
				
				boolean clockwise = towards == (angleToShipRel < 0);
	
				float newAngle = angleOfTravel + (clockwise ? -90 : 90);
				
				_start.set(_end);
				_end.add(Z.v1().set(_stepSize, 0).rotate(newAngle));
				break;
		}
	}
		
	@Override
	public float clipRadius()
	{
		return 8f; // todo
	}
}
