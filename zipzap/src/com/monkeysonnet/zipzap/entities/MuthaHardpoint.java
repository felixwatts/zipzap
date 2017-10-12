package com.monkeysonnet.zipzap.entities;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.Z;

public class MuthaHardpoint implements IEntity
{
	public static final int TYPE_HEAT_SEEKER = 0;
	public static final int TYPE_LASER = 1;
	public static final int TYPE_MINIGUN = 2;
	public static final int TYPE_GNAT = 3;
	private static final float BULLET_SPEED = 20f;
	private static final float ACTIVATION_RANGE_2 = 900;	
	private static final int SFX_FIRE_MINIGUN = -1015;
	private static final int SFX_FIRE_HEAT_SEEKER = -1034;
		
	private Entity _host;
	private Vector2 _offset;
	private float _reloadTime, _time, _angleOffset;	
	private boolean _isActive;
	private int _type;
	private LaserBeam _laserBeam;
	private Timeline _timeline;
	
	private final Vector2 _root = new Vector2();
	
	private final TweenCallback spawnGnatCallback = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			if(type == TweenCallback.START)
			{
				boolean commander = (Boolean)source.getUserData();
				Gnat.spawn(root(), _angleOffset + _host.angle(), Gnat.SQUAD_YELLOW, commander);
			}
		}
	};
	private boolean _dead;
	private TweenCallback timelineCompleteCallback = new TweenCallback()
	{
		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			if(_timeline != null)
			{
				_timeline.kill();
				_timeline = null;
			}
		}
	};
	
	public MuthaHardpoint(Entity host, Vector2 offset, float angleOffset, int type)
	{
		_type = type;
		_host = host;
		_offset = offset;
		_angleOffset = angleOffset;
		_dead = false;
		
		if(_type == TYPE_LASER)
			_laserBeam = LaserBeam.spawn(Color.GREEN);
		
		if(type == TYPE_HEAT_SEEKER)
			_reloadTime = 1f;
		else if(type == TYPE_MINIGUN)
			_reloadTime = 0.1f;
		else if(type == TYPE_GNAT)
		{
			_time = 16f;
			_reloadTime = 12f;
		}
		
		Z.sim().entities().add(this);
		
		active(false);
	}
	
	public void active(boolean a)
	{
		_isActive = a;
		
		if(_type == TYPE_LASER)
		{
			if(_timeline != null)
			{
				_timeline.kill();
				_timeline = null;
			}
			
			if(_isActive)
			{
				_timeline = Timeline.createSequence()
					.push(Tween.set(_laserBeam, 0).target(LaserBeam.MODE_WARM_UP))
					.pushPause(500)
					.push(Tween.set(_laserBeam, 0).target(LaserBeam.MODE_ON))
					.pushPause(100)
					.push(Tween.set(_laserBeam, 0).target(LaserBeam.MODE_WARM_UP))
					.pushPause(500)
					.push(Tween.set(_laserBeam, 0).target(LaserBeam.MODE_OFF))
					.pushPause(3000)
					.repeat(Tween.INFINITY, 0)
					.start(Z.sim().tweens());
			}
			else
			{
				_laserBeam.mode(LaserBeam.MODE_OFF);
			}
		}
	}

	@Override
	public void update(float dt)
	{
		if(!_dead)
		{
			float dst2 = root().dst2(Z.ship().origin());
			if(!_isActive && dst2 < ACTIVATION_RANGE_2)
				active(true);
			else if(_isActive && dst2 > ACTIVATION_RANGE_2)
				active(false);
			
			switch(_type)
			{
				case TYPE_LASER:
					if(_isActive)
						_laserBeam.set(root(), _host.angle() + _angleOffset);
					break;
				case TYPE_HEAT_SEEKER:
				case TYPE_MINIGUN:
					if(_isActive)
					{
						_time -= dt;
						if(_time < 0)
						{
							if(_type == TYPE_HEAT_SEEKER)
							{
								Z.sim.fireEvent(SFX_FIRE_HEAT_SEEKER, null);
								HeatSeeker.spawn(root(), _host.angle() + _angleOffset);
							}
							else
							{
								Z.sim.fireEvent(SFX_FIRE_MINIGUN, null);
								Projectile.spawn(root(), Z.v2().set(Z.ship().origin().sub(root())).nor().mul(BULLET_SPEED), 0, Color.YELLOW, false);
							}
							_time = _reloadTime;
						}
					}
					break;
				case TYPE_GNAT:
					_time -= dt;
					if(_time < 0)
					{
						if(_timeline != null)
						{
							_timeline.kill();
							_timeline = null;
						}
						
						_timeline = Timeline.createSequence()
								.push(Tween.call(spawnGnatCallback).setUserData(true).delay(1))
								.push(Tween.call(spawnGnatCallback).setUserData(false).delay(300))
								.push(Tween.call(spawnGnatCallback).setUserData(false).delay(300))
								.push(Tween.call(timelineCompleteCallback ))
								.start(Z.sim().tweens());
					
						_time = _reloadTime;
					}
			}
		}
	}
	
	private Vector2 root()
	{
		return _root.set(_offset).rotate(_host.angle()).add(_host.origin());
	}
	
	public int type()
	{
		return _type;
	}

	@Override
	public void free()
	{
		_dead = true;
		
		if(_timeline != null)
		{
			_timeline.kill();
			_timeline = null;
		}
		
		if(_laserBeam != null)
		{
			_laserBeam.free();
			_laserBeam = null;
		}
		
		Z.sim().entities().removeValue(this, true);
	}

	@Override
	public int layer()
	{
		return 0;
	}
}
