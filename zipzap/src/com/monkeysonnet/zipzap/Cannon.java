package com.monkeysonnet.zipzap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.utils.Array;
import com.monkeysonnet.engine.FixtureTag;
import com.monkeysonnet.zipzap.entities.Laser;

public class Cannon
{
	private static final float NEAR_MISS_ANGLE = 15;
	private static final float BEAM_LENGTH_INITIAL = 60f;
	private static final float BEAM_LENGTH_REFLECTION = 120f;
	private static final int MAX_REFLECTION_COUNT = 8;
	private static final int SFX_MEGA = -1039;
	private static final int SFX_MEGA_2 = -1003;

	private final Array<Vector2> _reflectionQueue = new Array<Vector2>();
	private final Array<Fixture> _reflectionFixtureQueue = new Array<Fixture>();
	
	private VariSfx _sfxNormal = new VariSfx(-1000, -1001);
	
	private boolean _hasIntersection;	
	private Fixture _hitFixture;
	private final Vector2 _hitPoint = new Vector2();
	private final Vector2 _hitNormal = new Vector2();
	private boolean _mega;
	private boolean _isReflection;
	private Fixture _ignoreFixture;
	private int _reflectionCount;
	
	public void tryFire(boolean mega, boolean rear)
	{
		_mega = mega;
		
		_reflectionQueue.add(Z.sim().vector().obtain().set(Z.ship().gunLoc(rear)));
		_reflectionQueue.add(Z.sim().vector().obtain().set(BEAM_LENGTH_INITIAL, 0).rotate(Z.ship().angle() + (rear ? 180 : 0)).add(Z.ship().origin()));
		_reflectionFixtureQueue.add(null);
		
		_isReflection = false;
		_reflectionCount = 0;
		
		processQueue();			
	}
	
	private void processQueue()
	{
		while(_reflectionQueue.size > 0)
		{			
			if(_reflectionCount > MAX_REFLECTION_COUNT)
			{
				while(_reflectionQueue.size > 0)
				{
					Z.sim.vector().free(_reflectionQueue.removeIndex(0));
				}
				
				return;			
			}
			_reflectionCount++;
			
			Vector2 start = _reflectionQueue.removeIndex(0);
			Vector2 end = _reflectionQueue.removeIndex(0);
			_ignoreFixture = _reflectionFixtureQueue.removeIndex(0);
						
			Z.sim().rayCast(
					_mega ? megaRayCastCallback : nonMegaRayCastCallback, 
					start, 
					end);
			
			if(!_mega)
			{
				if(_hitFixture != null)
				{
					tryHit(_hitFixture, _hitPoint, _hitNormal, _mega);
					
					Laser.spawn(start,
							_hitPoint,
							Color.RED,
							1.5f);
					
					Laser.spawn(start,
							_hitPoint,
							Color.WHITE,
							0.5f);
					
					//_sfxNormal.play();
				}
				else
				{
					if(_isReflection || hasIntersection(start, Vector2.tmp.set(end).sub(start).rotate(NEAR_MISS_ANGLE).add(start)) 
							|| hasIntersection(start, Vector2.tmp.set(end).sub(start).rotate(-NEAR_MISS_ANGLE).add(start)))
					{
						Laser.spawn(start,
								end,
								Color.RED,
								1.5f);
						
						Laser.spawn(start,
								end,
								Color.WHITE,
								0.5f);
						
						_sfxNormal.play();
					}
				}
			}
			else
			{
				Laser.spawn(start,
						end,
						Color.MAGENTA,
						3f);
				
				Laser.spawn(start,
						end,
						Color.WHITE,
						1f);
				
				Z.sim().flash(Color.MAGENTA);
				
				Z.sim.fireEvent(SFX_MEGA, null);
				Z.sim.fireEvent(SFX_MEGA_2, null);
				//_sfxNormal.play();
			}
							
			Z.sim().vector().free(start);
			Z.sim().vector().free(end);
			
			_isReflection = true;
			
			_hitFixture = null;
		}
	}
	
	private final RayCastCallback nonMegaRayCastCallback = new RayCastCallback()
	{		
		@Override
		public float reportRayFixture(Fixture fixture, Vector2 point,
				Vector2 normal, float fraction)
		{
			if(fixture != _ignoreFixture && canHit(fixture) != null)
			{
				_hitFixture = fixture;
				_hitPoint.set(point);
				_hitNormal.set(normal);
				return fraction;
			}
			else return 1f;
		}
	};
	
	private final RayCastCallback megaRayCastCallback = new RayCastCallback()
	{		
		@Override
		public float reportRayFixture(Fixture fixture, Vector2 point,
				Vector2 normal, float fraction)
		{
			if(fixture != _ignoreFixture)
				tryHit(fixture, point, normal, true);				
			return 1f;
		}
	};
	
	private final RayCastCallback hasIntersectionRayCastCallback = new RayCastCallback()
	{		
		@Override
		public float reportRayFixture(Fixture fixture, Vector2 point,
				Vector2 normal, float fraction)
		{
			if(fixture != null && canHit(fixture) != null)
			{
				_hasIntersection = true;
				return 0;
			}
			else return 1f;
		}
	};
	
	private boolean hasIntersection(Vector2 origin, Vector2 end)
	{
		_hasIntersection = false;
		Z.sim().world().rayCast(hasIntersectionRayCastCallback, origin, end);
		return _hasIntersection;
	}
	
	private IHitable canHit(Fixture f)
	{
		if(f.getUserData() == null)
			return null;
		
		FixtureTag tag = (FixtureTag)f.getUserData();
		
		if(!(tag.owner instanceof IHitable))
			return null;
		
		IHitable h = (IHitable)tag.owner;
		return h.isHitable() ? h : null;
	}
	
	private void tryHit(Fixture fixture, Vector2 point, Vector2 normal, boolean mega)
	{
		IHitable h = canHit(fixture);
		if(h != null)
		{
			boolean reflect = h.hit(fixture, mega, point, normal);
			
			if(reflect)
			{						
				_reflectionQueue.add(Z.sim().vector().obtain().set(point));											
				_reflectionQueue.add(Z.sim().vector().obtain().set(normal).mul(BEAM_LENGTH_REFLECTION).add(point));
				_reflectionFixtureQueue.add(fixture);
			}

		}
	}
}
