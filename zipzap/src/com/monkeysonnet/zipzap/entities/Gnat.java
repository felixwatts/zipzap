package com.monkeysonnet.zipzap.entities;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.BlobTrailBehaviour;
import com.monkeysonnet.zipzap.behaviours.DieOnHitBehaviour;
import com.monkeysonnet.zipzap.behaviours.FaceDirectionOfTravelBehaviour;
import com.monkeysonnet.zipzap.behaviours.HomingBehaviour;
import com.monkeysonnet.zipzap.behaviours.DieOnRangeBehaviour;
import com.monkeysonnet.zipzap.behaviours.KillOnContactBehaviour;
import com.monkeysonnet.zipzap.behaviours.OrbitBehaviour;
import com.monkeysonnet.zipzap.script.IActiveCount;

public class Gnat extends Enemy implements IRenderableMultiPolygon
{
	private static final float SPEED = 16f;
	private static final float ANGULAR_VELOCITY = 90f;
	private static final HomingBehaviour _homingBehaviour = new HomingBehaviour(360, ANGULAR_VELOCITY, false);
	private static final HomingBehaviour _fleeingBehaviour = new HomingBehaviour(360, ANGULAR_VELOCITY, true);
	private static final OrbitBehaviour _orbitBehaviour = new OrbitBehaviour(12, 30, ANGULAR_VELOCITY);
	private static final BlobTrailBehaviour _blobTrailBehaviour = new BlobTrailBehaviour(Color.YELLOW);
	
	public static final int SQUAD_YELLOW = 0;
	public static final int SQUAD_CYAN = 1;
	public static final int SQUAD_MAGENTA = 2;
	
	private static final int MODE_CHARGE = 1;
	private static final int MODE_FLEE = 2;
	private static final GnatPool pool = new GnatPool();
	private static final Map verts = new Map("gnat.v");
	protected static final float WING_LENGTH = 2f;
	protected static final float BULLET_SPEED = 25f;
	private static final float FIRE_DELAY = 200;
	private static final float SQUADRON_RADIUS = 45f;
	
	public static final IActiveCount activeCommanderCount = new IActiveCount()
	{
		@Override
		public int activeCount()
		{
			return _activeCommanderCount;
		}
	};
	
	private static int _activeCommanderCount;
	
	private static class GnatPool extends Pool<Gnat>
	{
		@Override
		protected Gnat newObject()
		{
			return new Gnat();
		}
	}
	
	private int _mode;
	private boolean _isCommander;
	private boolean _firing;
	private boolean _hasCommander;
	private int _squad;
	private Timeline _fireTimeline;
	
	private Gnat()
	{		
	}
	
	public static void spawnSquadron(int num)
	{
		spawnSquadron(num, Game.Dice.nextInt(3));
	}
	
	public static void spawnSquadron(int num, int color)
	{
		float angle = Game.Dice.nextFloat() * 360;
		
		spawn(angle, color, true);
		
		for(int n = 0; n < num-1; n++)
		{
			float a = angle + (float)(Game.Dice.nextGaussian() * SQUADRON_RADIUS);
			spawn(a, color, false);
		}
	}
	
	public static void spawn(float angle, int squad, boolean commander)
	{
		Z.v1().set(ZipZapSim.SPAWN_DISTANCE, 0).rotate(angle).add(Z.ship().origin());
		spawn(Z.v1(), angle, squad, commander);
	}
	
	public static void spawn(Vector2 loc, float angle, int squad, boolean commander)
	{
		Gnat g = pool.obtain();
		
		g._squad = squad;
		g._isCommander = commander;
		g._hasCommander = true;
		
		g._killScore = commander ? 50 : 30;
		
		g._body = B2d
				.kinematicBody()
				.at(loc)
				.withFixture(B2d
						.chain(verts.shape(0).shape)
						.sensor(true)
						.category(ZipZapSim.COL_CAT_METEORITE)
						.mask(ZipZapSim.COL_CAT_SHIP)
						.userData(g._fixtureTag))
				.linearVelocity(Z.v2().set(SPEED, 0).rotate(180 + angle))
				.create(Z.sim().world());
		
		g._behaviours.clear();
		g._behaviours.add(_blobTrailBehaviour);
		g._behaviours.add(FaceDirectionOfTravelBehaviour.instance());		
		g._behaviours.add(_homingBehaviour);
		g._behaviours.add(DieOnHitBehaviour.basic());
		g._behaviours.add(DieOnRangeBehaviour.instance());
		g._behaviours.add(KillOnContactBehaviour.basic());
		g._mode = MODE_CHARGE;
		
		Z.sim().entities().add(g);
		
		g.onSpawn();
		
		if(commander)
			_activeCommanderCount++;
	}
	
	private final TweenCallback _fireCallback = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			if(Z.ship().ghostMode())
				return;
			
			boolean left = (Boolean)source.getUserData();
			
			float l = left ? WING_LENGTH : -WING_LENGTH;
			
			Projectile
				.spawn(
					Z
						.v1()
						.set(0, l)
						.rotate((float)Math.toDegrees(_body.getAngle()))
						.add(origin()),
					Z
						.v2()
						.set(_body.getLinearVelocity())
						.nor()
						.mul(BULLET_SPEED));
			
			Z.sim.fireEvent(ZipZapSim.EV_LASER_SMALL, null);
		}
	};
	
	private final TweenCallback _endFireCallback = new TweenCallback()
	{
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			_firing = false;
			_fireTimeline = null;
		}
	};	
	
	private void killTweens()
	{
		if(_fireTimeline != null)
		{
			_fireTimeline.kill();
			_fireTimeline = null;
		}
	}
	
	private void beginFire()
	{
		if(_firing)
			return;
		
		//Z.sim().tweens().killTarget(this);
		killTweens();
		
		_firing = true;
		_fireTimeline = Timeline
			.createSequence()
			.push(Tween.call(_fireCallback).setUserData(true))
			.pushPause(FIRE_DELAY)
			.push(Tween.call(_fireCallback).setUserData(false))
			.pushPause(FIRE_DELAY)
			.push(Tween.call(_fireCallback).setUserData(true))
			.pushPause(FIRE_DELAY)
			.push(Tween.call(_fireCallback).setUserData(false))
			.pushPause(4000)
			.push(Tween.call(_endFireCallback))
			.start(Z.sim().tweens());
		
		//Tween.call(_endFireCallback).delay(5*FIRE_DELAY + 1000).start(Z.sim().tweens());
	}
	
	private void cancelFire()
	{
		killTweens();
		//Z.sim().tweens().killTarget(this);
		_firing = false;
	}
	
	private void flee()
	{
		cancelFire();
		_behaviours.removeValue(_homingBehaviour, true);
		_behaviours.removeValue(_orbitBehaviour, true);
		_behaviours.add(_fleeingBehaviour);
		_mode = MODE_FLEE;
	}
	
	private void charge()
	{
		_behaviours.removeValue(_fleeingBehaviour, true);
		
		_behaviours.add(Z.ship().ghostMode() ? _orbitBehaviour : _homingBehaviour);
		_mode = MODE_CHARGE;
	}
	
	@Override
	public void update(float dt)
	{
		super.update(dt);
		
		if(!_dead)
		{
		
			float dst2 = origin().dst2(Z.ship().origin());
			float chance;
			
			if(_hasCommander)
			{
				switch(_mode)
				{
					case MODE_CHARGE:
						
						if(!_firing && dst2 < 900)
						{
							//if(Game.Dice.nextFloat() < (0.5f * dt))
							float angleToShip = Z.v1().set(Z.ship().origin()).sub(body().getWorldCenter()).angle();
							float angleOfTravel = body().getLinearVelocity().angle();
							if(angleToShip > 180)
								angleToShip -= 360;
							if(angleOfTravel > 180)
								angleOfTravel -= 360;
							if(angleOfTravel > (angleToShip - 15) && angleOfTravel < (angleToShip + 15))
								beginFire();
						}
						
						chance = Z.ship().ghostMode() ? 0.05f : (Math.max(0, 400 - (dst2 + 25f)) / 400f);
						if(Game.Dice.nextFloat() < chance)
						{
							flee();
						}
						break;
						
					case MODE_FLEE:
						chance =  Math.min(1f, (Math.max(0, dst2 - 400f) / 2100f));
						if(Game.Dice.nextFloat() < chance)
						{					
							charge();
						}
						break;
				}
			}
		}
	}
	
	@Override
	public void onFree()
	{
		killTweens();
		
		if(_isCommander)
		{
			for(IEntity e : Z.sim().entities())
			{
				if(e instanceof Gnat)
				{
					Gnat g = (Gnat)e;
					if(g._squad == _squad)
					{
						g._hasCommander = false;
						g.flee();
					}
				}
			}
			
			_activeCommanderCount--;
		}

		_firing = false;
		Z.sim().spawnExlosion(origin(), 8, Color.YELLOW);
		pool.free(this);
	}

	@Override
	public int getNumPolys()
	{
		return verts.numShapes();
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
		return verts.shape(poly).shape;
	}

	@Override
	public Color color(int poly)
	{
		if(_isCommander)
		{
			if(poly == 0)
				return squadColor(_squad);
			else return Color.RED;
		}
		else return squadColor(_squad);
	}
	
	private static Color squadColor(int squad)
	{
		switch (squad)
		{
			case SQUAD_CYAN:
				return Color.CYAN;
			case SQUAD_MAGENTA:
				return Color.WHITE;
			case SQUAD_YELLOW:
				return Color.YELLOW;
			default:
				return null;
		}
	}

	@Override
	public float lineWidth(int poly)
	{
		return 1f;
	}
	
	@Override
	public boolean isLoop(int poly)
	{
		return true;
	}	
	
	@Override
	public float clipRadius()
	{
		return 4f;
	}
}
