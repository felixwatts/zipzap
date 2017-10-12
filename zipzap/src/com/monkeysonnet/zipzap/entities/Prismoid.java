package com.monkeysonnet.zipzap.entities;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenAccessor;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Quad;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.zipzap.ElectricBeamWidth;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.KillOnContactBehaviour;

public class Prismoid extends Entity implements IRenderableMultiPolygon, TweenAccessor<Prismoid>
{
	private static final int TW_RADIUS = 0;
	private static final int TW_POS_XY = 1;
	private static final int TW_ANGLE = 2;
	private static final int TW_OPACITY = 3;
	
	private static final float START_RADIUS = 60f;
	private static final float HOVER_RADIUS = 30f;
	private static final float SHRINK_TO_HOVER_TIME_MS = 1000f;
	private static final float HOVER_TIME_MS = 2000f;
	private static final float SHRINK_TIME_MS = 4000f;	
	
	private final Body[] _edgeBodies = new Body[3];
	private final Vector2[] vertsOn = new Vector2[3];
	private final Vector2[] vertsOff = new Vector2[1];
	private final Vector2 _origin = new Vector2();
	private final Color color = new Color();
	
	private float _angle;	
	private float _radius;
	private boolean _isLaserOn;
	private boolean _trackShip;
	private Rotolon _controller;
	private Timeline _timeline;
	private float _opacity;	
	private ElectricBeamWidth _beamWidth;
	
	public Prismoid(Rotolon controller) 
	{
		_controller = controller;
		
		for(int n = 0; n < 3; n++)
			vertsOn[n] = new Vector2();
		
		_behaviours.add(KillOnContactBehaviour.basic());
		
		_opacity = 0;
		_trackShip = true;
		_isLaserOn = false;
		setRadius(START_RADIUS);
		_angle = 0;
		
		for(int n = 0; n < 3; n++)
		{
			_edgeBodies[n] = B2d
					.staticBody()
					.at(vertsOn[n])
					.active(false)
					.withFixture(B2d
							.edge()
							.between(Z.v1().set(0, 0), Z.v2().set(vertsOn[(n+1)%3]).sub(vertsOn[n]))
							.category(ZipZapSim.COL_CAT_METEORITE)
							.mask(ZipZapSim.COL_CAT_SHIP)
							.userData(_fixtureTag))
					.create(Z.sim().world());
		}		
		
		_beamWidth = new ElectricBeamWidth();
		
		onSpawn();
	}
	
	public void beginSequence(float radius)
	{
		killTweens();
		
		_opacity = 1;
		_trackShip = false;
		_isLaserOn = false;
		setRadius(START_RADIUS);
		_angle = 90;
		
		setRadius(_controller.radius());
		_origin.set(_controller.origin());		

		_timeline = Timeline.createSequence()
			.push(Timeline.createParallel()
					.push(Tween
							.to(this, TW_OPACITY, SHRINK_TO_HOVER_TIME_MS/2f)
							.target(0))
					.push(Tween
							.to(this, TW_RADIUS, SHRINK_TO_HOVER_TIME_MS)
							.target(START_RADIUS)
							.ease(Quad.INOUT)))
			.push(Tween.call(setTrackShipCallback).setUserData(true))
			.push(Timeline.createParallel()
					.push(Tween
							.to(this, TW_OPACITY, SHRINK_TO_HOVER_TIME_MS/2f)
							.target(1))
					.push(Tween
							.to(this, TW_ANGLE, SHRINK_TO_HOVER_TIME_MS)
							.target(360)
							.ease(Quad.INOUT))
					.push(Tween
							.to(this, TW_RADIUS, SHRINK_TO_HOVER_TIME_MS)
							.target(HOVER_RADIUS)
							.ease(Quad.INOUT)))
			.push(Tween
					.to(this, TW_ANGLE, HOVER_TIME_MS)
					.target(0)
					.ease(Quad.INOUT))
			.push(Tween
					.call(setIsLaserOnCallback)
					.setUserData(true))
			.push(Tween
					.call(setTrackShipCallback)
					.setUserData(false))
			.push(Timeline.createParallel()
					.push(Tween
						.to(this, TW_ANGLE, SHRINK_TIME_MS)
						.target(360))
					.push(Tween
						.to(this, TW_RADIUS, SHRINK_TIME_MS)
						.target(radius)))
			.push(Tween
				.call(setIsLaserOnCallback)
				.setUserData(false))
			.push(Timeline.createParallel()
				.push(Tween
						.to(this, TW_OPACITY, SHRINK_TO_HOVER_TIME_MS/2f)
						.target(0))
				.push(Tween
						.to(this, TW_ANGLE, SHRINK_TO_HOVER_TIME_MS)
						.target(0)
						.ease(Quad.INOUT))
				.push(Tween
						.to(this, TW_RADIUS, SHRINK_TO_HOVER_TIME_MS)
						.target(START_RADIUS)
						.ease(Quad.INOUT)))
			.push(Tween.set(this, TW_POS_XY).target(_controller.origin().x, _controller.origin().y))
			.push(Tween.set(this, TW_ANGLE).target(90))
			.push(Timeline.createParallel()					
				.push(Tween
						.to(this, TW_OPACITY, SHRINK_TO_HOVER_TIME_MS/2f)
						.target(1))
				.push(Tween
						.to(this, TW_RADIUS, SHRINK_TO_HOVER_TIME_MS)
						.target(_controller.radius())
						.ease(Quad.INOUT)))
			.push(Tween.set(this, TW_OPACITY).target(0))
			.setCallbackTriggers(TweenCallback.COMPLETE)
			.setCallback(sequenceCompleteCallback )
			.start(Z.sim().tweens());
	}
	
	private final TweenCallback setIsLaserOnCallback = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			setIsLaserOn((Boolean)source.getUserData());
		}
	};
	
	private final TweenCallback setTrackShipCallback = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			_trackShip = (Boolean)source.getUserData();
		}
	};
	
	private final TweenCallback sequenceCompleteCallback = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			_timeline.kill();
			_timeline = null;
			_controller.onPrismoidSequenceComplete();
		}
	};
	
	private void killTweens()
	{
		if(_timeline != null)
		{
			_timeline.kill();
			_timeline = null;
		}
		Z.sim().tweens().killTarget(_body);
		Z.sim().tweens().killTarget(this);
	}

	@Override
	public int getNumPolys()
	{
		return _isLaserOn ? 2 : 6;
	}

	@Override
	public float angle(int poly)
	{
		return _angle;
	}

	@Override
	public Vector2 origin(int poly)
	{
		return _origin;
	}

	@Override
	public Vector2[] verts(int poly)
	{
		if(_isLaserOn)
			return vertsOn;
		else
		{
			if(poly >= 3)
				poly -= 3;
			vertsOff[0] = vertsOn[poly];
			return vertsOff;
		}
	}

	@Override
	public Color color(int poly)
	{
		if(_isLaserOn)
		{
			return poly == 0 ? Color.CYAN : Color.WHITE;
		}
		else
		{
			if(poly > 2)
				color.set(1, 1, 1, _opacity);
			else
				color.set(0, 1, 1, _opacity);
			return color;
		}
	}

	@Override
	public float lineWidth(int poly)
	{
		if(_isLaserOn)
		{
			return poly == 0 ? _beamWidth.beamWidth() : 1;
		}
		else
		{
			return poly > 2 ? 1 : _beamWidth.beamWidth();
		}
	}
	
	private void setRadius(float radius)
	{
		_radius = radius;

		for(int n = 0; n < 3; n++)
			vertsOn[n].set(radius, 0).rotate(n * 120f);
	}
	
	private void setIsLaserOn(boolean on)
	{
		_isLaserOn = on;
		for(int n = 0; n < 3; n++)
			_edgeBodies[n].setActive(on);
	}
	
	private void refreshBodyTransforms()
	{
		for(int n = 0; n < 3; n++)
		{		
			_edgeBodies[n].setTransform(
					Z.v1().set(vertsOn[n]).rotate(_angle).add(_origin), 
					(float)Math.toRadians(_angle));
		}
	}
	
	@Override
	public Vector2 origin()
	{
		return _origin;
	}
	
	@Override
	public void update(float dt)
	{
		super.update(dt);
		
		if(!_dead)
		{
			if(_trackShip)
			{
				_origin.set(Z.ship().origin());
			}
			
			if(_isLaserOn)
			{
				refreshBodyTransforms();
			}
		}
	}

	@Override
	public int getValues(Prismoid target, int tweenType, float[] returnValues)
	{
		switch(tweenType)
		{
			case TW_ANGLE:
				returnValues[0] = _angle;
				return 1;
			case TW_POS_XY:
				returnValues[0] = _origin.x;
				returnValues[1] = _origin.y;
				return 2;
			case TW_RADIUS:
				returnValues[0] = _radius;
				return 1;
			case TW_OPACITY:
				returnValues[0] = _opacity;
				return 1;
			default: return -1;
		}
	}

	@Override
	public void setValues(Prismoid target, int tweenType, float[] newValues)
	{
		switch(tweenType)
		{
			case TW_ANGLE:
				_angle = newValues[0];
				break;
			case TW_POS_XY:
				_origin.x = newValues[0];
				_origin.y = newValues[1];
				break;
			case TW_RADIUS:
				setRadius(newValues[0]);
				break;
			case TW_OPACITY:
				_opacity = newValues[0];
				break;
		}
	}
	
	@Override
	protected void onFree()
	{
		killTweens();
		
		_beamWidth.free();
		
		for(int n = 0; n < 3; n++)
		{
			Z.sim().world().destroyBody(_edgeBodies[n]);
			_edgeBodies[n] = null;
		}
	}
	
	@Override
	public boolean isLoop(int poly)
	{
		return true;
	}	
	
	@Override
	public float clipRadius()
	{
		return 40f;
	}
}
