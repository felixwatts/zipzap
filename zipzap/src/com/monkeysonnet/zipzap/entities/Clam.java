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
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.Tools;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.DieOnHitBehaviour;
import com.monkeysonnet.zipzap.behaviours.DieOnRangeBehaviour;
import com.monkeysonnet.zipzap.behaviours.FlyingSoundBehaviour;
import com.monkeysonnet.zipzap.behaviours.IBehaviour;
import com.monkeysonnet.zipzap.behaviours.KillOnContactBehaviour;
import com.monkeysonnet.zipzap.behaviours.OrbitBehaviour;
import com.monkeysonnet.zipzap.script.IActiveCount;

public class Clam extends Enemy implements IRenderableMultiPolygon
{
	private static final ClamPool pool = new ClamPool();
	private static class ClamPool extends Pool<Clam>
	{
		@Override
		protected Clam newObject()
		{
			return new Clam();
		}
	}
	
	private static Color[] coloursClosed;
	private static Color[] coloursOpen;
	private static final Map mapClosed = new Map("clam-closed.v", 2f, 90, true);
	private static final Map mapOpen = new Map("clam-open.v", 2f, 90, true);
	private static final int SFX_FLY = 21;	
	private static final int SFX_DEFLECT = -1019;
	private static final int SFX_SHOOT = -1020;
	private static final int ST_CLOSED = 0;
	private static final int ST_OPEN = 1;
	private static final float PRE_OPEN_PAUSE_MS = 200;
	private static final float RELOAD_TIME_MS = 400;
	private static final float PRE_CLOSE_PAUSE_MS = 1000;
	private static final float PROJECTILE_SPEED = 24;
	private static final float RADIUS = 2f;
	private static final float HOMING_TIME_MS = 3000;
	private static final float SPEED = 16;	
	
	private static int numActive;
	private int _powerup;
	
	private final IBehaviour behaviour = new OrbitBehaviour(30f, 35f, 90f);// new RandomSwitchingBehaviour(0.5f, new HomingBehaviour(360, 90, false), new OrbitBehaviour(30f, 35f, 90f));
	
	public static final IActiveCount activeCount = new IActiveCount()
	{
		@Override
		public int activeCount()
		{
			return numActive;
		}
	};

	static
	{
		coloursClosed = getColours(mapClosed);
		coloursOpen = getColours(mapOpen);
	}
	
	public static void spawn(int powerup)
	{
		Vector2 v = Tools.randomSpawnLoc();
		spawn(v.x, v.y, powerup);
	}

	public static void spawn(float x, float y, int powerup)
	{
		Clam j = pool.obtain();
		
		j._powerup = powerup;
		
		j._body = B2d
				.kinematicBody()
				.at(x, y)
				.withFixture(B2d
						.circle()
						.radius(RADIUS)
						.sensor(true)
						.category(ZipZapSim.COL_CAT_METEORITE)
						.mask(ZipZapSim.COL_CAT_SHIP)
						.userData(j._fixtureTag))
				.create(Z.sim().world());
		
		j.setState(ST_CLOSED);
		
		j.onSpawn();
		Z.sim().entities().add(j);
		
		j._timeline = Timeline.createSequence()
			.push(Tween.call(j.homingCallback).setUserData(true))
			.pushPause(HOMING_TIME_MS)
			.push(Tween.call(j.homingCallback).setUserData(false))
			.pushPause(PRE_OPEN_PAUSE_MS)
			.push(Tween.call(j.setStateCallback).setUserData(ST_OPEN))
			.pushPause(RELOAD_TIME_MS)
			.push(Tween.call(j.fireCallback))
			.pushPause(RELOAD_TIME_MS)
			.push(Tween.call(j.fireCallback))
			.pushPause(PRE_CLOSE_PAUSE_MS)
			.push(Tween.call(j.setStateCallback).setUserData(ST_CLOSED))
			.repeat(Tween.INFINITY, 0)
			.start(Z.sim().tweens());
		
		numActive++;
	}

	private Timeline _timeline;
	private int _state;
	private TweenCallback setStateCallback = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			setState((Integer)source.getUserData());
		}
	};
	
	private TweenCallback fireCallback = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			fire();
		}
	};
	

	private TweenCallback homingCallback = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			if((Boolean)source.getUserData())
			{
				_behaviours.add(behaviour);
			}
			else
				_behaviours.removeValue(behaviour, true);
		}
	};
	
	private Clam()
	{
		_behaviours.add(KillOnContactBehaviour.alsoDie());
		_behaviours.add(DieOnRangeBehaviour.instance());
		_behaviours.add(new FlyingSoundBehaviour(SFX_FLY, 1f, 20f));
		_behaviours.add(behaviour);
		
		_killScore = 100;
	}
	
	private void setState(int state)
	{
		_state = state;
		
		switch(_state)
		{
			case ST_OPEN:
				_body.setLinearVelocity(Z.v1().set(0, 0));
				_behaviours.add(DieOnHitBehaviour.basic());
				break;
			case ST_CLOSED:
				_body.setLinearVelocity(Z.v1().set(Z.ship().origin()).sub(origin()).nor().mul(SPEED));
				_behaviours.removeValue(DieOnHitBehaviour.basic(), true);
				break;
		}
	}
	
	@Override
	public void onFree()
	{	
		if(_killed)
		{
			Z.sim.fireEvent(ZipZapSim.EV_RUMBLE, 0.5f);
			Z.sim.fireEvent(ZipZapSim.EV_EXPLOSION_MEDIUM, 0.5f);
		}
		
		_timeline.kill();
		pool.free(this);
		numActive--;
	}

	@Override
	public int getNumPolys()
	{
		switch(_state)
		{
			case ST_CLOSED:
			default:
				return mapClosed.numShapes();
			case ST_OPEN:
				return mapOpen.numShapes();
		}
	}

	@Override
	public float angle(int poly)
	{
		return (float)Math.toDegrees(_body.getAngle());
	}

	@Override
	public Vector2 origin(int poly)
	{
		return _body.getWorldCenter();
	}

	@Override
	public Vector2[] verts(int poly)
	{
		switch(_state)
		{
			case ST_CLOSED:
			default:
				return mapClosed.shape(poly).shape;
			case ST_OPEN:
				return mapOpen.shape(poly).shape;
		}
	}

	@Override
	public Color color(int poly)
	{
		switch(_state)
		{
			case ST_CLOSED:
			default:
				return coloursClosed[poly];
			case ST_OPEN:
				return coloursOpen[poly] == Color.RED ? PowerUp.colorForType(_powerup) : coloursOpen[poly];
		}
	}

	@Override
	public float lineWidth(int poly)
	{
		return 1f;
	}
	
	@Override
	public void update(float dt)
	{
		super.update(dt);
		
		if(!_dead)
		{
		}
	}
	
	protected void fire()
	{
		Projectile.spawn(origin(), Z.v1().set(Z.ship().origin()).sub(origin()).nor().mul(PROJECTILE_SPEED), 2, PowerUp.colorForType(_powerup), true);
		Z.sim.fireEvent(SFX_SHOOT, null);
	}
	
	@Override
	public boolean isLoop(int poly)
	{
		return true;
	}
	
	@Override
	public boolean hit(Fixture f, boolean mega, Vector2 loc, Vector2 norm)
	{
		if(mega)
		{
			DieOnHitBehaviour.basic().hit(this, f, mega, loc, norm);
			return false;
		}
		else
		{
			if(_state != ST_CLOSED)
				PowerUp.spawn(origin(), _powerup);
			else 
				Z.sim.fireEvent(SFX_DEFLECT, 0);
			
			super.hit(f, mega, loc, norm);						
			
			return _state == ST_CLOSED;
		}
	}
	
	@Override
	public float clipRadius()
	{
		return 4f;
	}
}
