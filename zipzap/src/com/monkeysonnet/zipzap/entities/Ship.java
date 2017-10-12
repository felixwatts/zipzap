package com.monkeysonnet.zipzap.entities;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.ColorTools;
import com.monkeysonnet.engine.FixtureTag;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IContactHandler;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.Cannon;
import com.monkeysonnet.zipzap.IAttractor;
import com.monkeysonnet.zipzap.IOrigin;
import com.monkeysonnet.zipzap.IRenderablePolygon;
import com.monkeysonnet.zipzap.Sim;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.achievements.TreatExtraLife;
import com.monkeysonnet.zipzap.achievements.TreatRearCannon;
import com.monkeysonnet.zipzap.achievements.TreatShield;
import com.monkeysonnet.zipzap.achievements.TreatUltraCapacitor;

public class Ship implements IRenderablePolygon, IEntity, //RayCastCallback,
		IContactHandler, IOrigin, IAttractor
{
	public static final float MEGALASER_TIME = 3f;
	private static final float DRAGON_TIME = 8f;
	private static final float LINEAR_DAMPING = 0.1f;
	public static final Vector2[] verts = new Vector2[4];
	public static final float SCALE = 1f;
	public static final float SPEED_NORMAL = 20;
	private static final float SPEED_DRAGON = SPEED_NORMAL * 3f;
	private static final Vector2 _lastLivePosition = new Vector2();
	private static final float JET_TIME = 0.20f;
	private static final Filter filter = new Filter();
	private static final float RELOAD_TIME = 0.1f;
	private static final float CHARGE_TIME = 1.5f;
	private static final float CHARGE_ATTRACTICLE_START_TIME = 0.1f;
	private static final Color ghostStartColor = new Color(1f, 0f, 0f, 0.4f);
	private static final int SCORE_SECOND_POWERUP = 100;
	private static final float INVINSIBLE_TIME_MS = 2000;	
	
	private Body _body;
	private boolean _hasMegaLaser;
	private boolean _hasUltraCapacitor;
	private boolean _hasAutoCannon;
	private boolean _hasRearCannon;
	private float _megaLaserTimer;
	private Bubble _bubble;
	private int _justPickedUp = PowerUp.TYPE_NONE;
	private boolean _dead;
	private boolean _cannonEnabled = true;
	private float _jetTimer;
	private boolean _ghostMode;
	private boolean _lockControls;
	private boolean _touchDown;
	private float _fireTime;
	private float _chargeTime;
	private boolean _dragonMode;
	private float _dragonTimer;
	private int _lives;
	
	private final Cannon _cannon = new Cannon();

	static
	{
		verts[0] = new Vector2(-2f * SCALE, 2f * SCALE);
		verts[1] = new Vector2(-1, 0f * SCALE);
		verts[2] = new Vector2(-2f * SCALE, -2f * SCALE);
		verts[3] = new Vector2(3, 0f * SCALE);
		
		filter.categoryBits = ZipZapSim.COL_CAT_SHIP;
		filter.maskBits = ZipZapSim.COL_CAT_METEORITE | ZipZapSim.COL_CAT_POWERUP;
	}

	public Ship(World w)
	{
		_body = B2d
				.dynamicBody()
				.linearDamping(LINEAR_DAMPING)				
				.gravityScale(0)
				.userData(this)
				.fixedRotation(true)
				.rotated((float)Math.toRadians(90))
				.withFixture(
						B2d.polygon(
								new Vector2[] { verts[0], verts[1], verts[3] })
								.sensor(true)
								.category(ZipZapSim.COL_CAT_SHIP)
								.mask(ZipZapSim.COL_CAT_POWERUP
										| ZipZapSim.COL_CAT_METEORITE)
								.userData(new FixtureTag(this, this)))
				.withFixture(
						B2d.polygon(
								new Vector2[] { verts[1], verts[2], verts[3] })
								.sensor(true)
								.category(ZipZapSim.COL_CAT_SHIP)
								.mask(ZipZapSim.COL_CAT_POWERUP
										| ZipZapSim.COL_CAT_METEORITE)
								.userData(new FixtureTag(this, this)))
				.create(w);

		_bubble = new Bubble(w);
		
		_hasRearCannon = (new TreatRearCannon()).isUnlocked();
		
		_lives = Z.prefs.getInteger("zipzap-state-lives", TreatExtraLife.currentNumLives());
		
		_hasUltraCapacitor = new TreatUltraCapacitor().isUnlocked();
		
		onStartLife();
	}
	
	public Vector2 gunLoc(boolean rear)
	{
		return rear ? origin() : Vector2.tmp.set(verts[3]).rotate(angle()).add(origin());
	}
	
	public void reset()
	{
		ghostMode(false);
		bubble().reinit();
		lockControls(false);
		_body.setLinearVelocity(0, 0);
		setPosition(0, 0, 90);
	}

	public void lockControls(boolean lock)
	{
		_lockControls = lock;
	}

	public void ghostMode(boolean ghost)
	{
		_ghostMode = ghost;
		
		Z.sim.target(null);
			
		for(Fixture f : _body.getFixtureList())
			f.setFilterData(ghost ? Sim.nullFilter : filter);
	}
	
	public void beginCruise()
	{
		beginCruise(Vector2.tmp.set(0, Ship.SPEED_NORMAL/8f));
	}

	public void beginCruise(Vector2 v)
	{
		_body.setLinearDamping(0);
		_body.setLinearVelocity(v);
		_body.setTransform(origin(), (float)Math.toRadians(v.angle()));
	}

	public void endCruise()
	{
		_body.setLinearDamping(LINEAR_DAMPING);
	}
	
//	private boolean trySpawnLaser(float angleOffset)
//	{
//		if(_hasMegaLaser)
//			return false;
//		
//		Game.workingVector2a.set(_body.getLinearVelocity());
//		if(angleOffset != 0)
//			Game.workingVector2a.rotate(angleOffset);
//		Game.workingVector2a.nor().mul(LASER_LENGTH).add(origin());
//		_rayCastIntersection.set(Game.workingVector2a);
//
//		Z.sim().world()
//				.rayCast(this, origin(), Game.workingVector2a);
//		
//		return _rayCastCache != null;
//	}
	
	public void slide(float x, float y)
	{
		if (_dead)
			return;

		if (_lockControls)
			return;

		float thrust = _dragonMode ? SPEED_DRAGON : SPEED_NORMAL;
		Game.workingVector2a.set(origin()).sub(x, y).nor().mul(-thrust);
		_body.setLinearVelocity(Game.workingVector2a);
		_body.setTransform(origin(), (float) Math.toRadians(_body.getLinearVelocity().angle()));
		
		_jetTimer = JET_TIME;
	}

	public void tap(float x, float y)
	{
		_touchDown = true;
		_fireTime = RELOAD_TIME;
		
		if (_dead)
			return;

		if (_lockControls)
			return;

		float thrust = _dragonMode ? SPEED_DRAGON : SPEED_NORMAL;
		Game.workingVector2a.set(origin()).sub(x, y).nor().mul(-thrust);
		_body.setLinearVelocity(Game.workingVector2a);
		_body.setTransform(origin(), (float) Math.toRadians(_body.getLinearVelocity().angle()));
		
		tryFireCannon();
		
		_jetTimer = JET_TIME;		
		
		if(_hasUltraCapacitor)
			Z.sim.fireEvent(ZipZapSim.EV_BEGIN_UC_CHARGE, null);
	}
	
	private void dragonMode(boolean on)
	{
		if(on == _dragonMode)
			return;
		
		_dragonMode = on;		
		_body.setLinearVelocity(Z.v1().set(_body.getLinearVelocity()).nor().mul(_dragonMode ? SPEED_DRAGON : SPEED_NORMAL));
		
		if(_dragonMode)
			_dragonTimer = DRAGON_TIME;
	}
	
	public boolean dragonMode()
	{
		return _dragonMode;
	}
	
	private void tryFireCannon()
	{
		if (_cannonEnabled && !_dead)
		{
			_cannon.tryFire(isMega(), false);
			if(_hasRearCannon)
				_cannon.tryFire(isMega(), true);
		}
	}
	
	public void release()
	{
		_touchDown = false;
		
		if(_hasUltraCapacitor)
			Z.sim.fireEvent(ZipZapSim.EV_END_UC_CHARGE, null);
		
		if(_chargeTime >= CHARGE_TIME)
		{
			tryFireCannon();
		}
		
		_chargeTime = 0;
	}

	public boolean ghostMode()
	{
		return _ghostMode;
	}

	public void strike()
	{
		if (_dead)
			return;

		if (_ghostMode)
			return;
		
		if(_dragonMode)
			return;

		if (_bubble.active())
		{
			_bubble.strike();
			temporaryInvincibility();
		}
		else
			kill();
	}
	
	private void temporaryInvincibility()
	{
		ghostMode(true);
		Tween.call(callbackEndInvincible).delay(INVINSIBLE_TIME_MS).start(Z.sim.tweens());
	}
	
	public void loseBubble()
	{
		if (_bubble.active())
			_bubble.free();
	}
	
	private boolean isMega()
	{
		return _hasMegaLaser || _chargeTime >= CHARGE_TIME;
	}

	public void givePowerUp(int type)
	{
		switch (type)
		{
			case PowerUp.TYPE_MEGA_LASER:
				_hasMegaLaser = true;
				_megaLaserTimer = 0;
				Z.sim().fireEvent(ZipZapSim.EV_POWERUP_MEGALASER, null);
				break;
			case PowerUp.TYPE_DRAGON:
				dragonMode(true);
				break;
			default:
				_justPickedUp = type;
				break;
		}
	}

	@Override
	public float angle()
	{
		return (float) Math.toDegrees(_body.getAngle());
	}

	@Override
	public Vector2 origin()
	{
		return _body.getWorldCenter();// .getPosition();
	}

	@Override
	public Vector2[] verts()
	{
		return verts;
	}

	@Override
	public Color color()
	{
		if(_dead)
			return null;
		else
		{
			Color result = isMega() ? Color.MAGENTA : Color.RED;
			if(_ghostMode)
				result = ColorTools.combineAlpha(result, 0.5f);
			return result;
		}
	}
	
	public void setPosition(Vector2 v, float angle)
	{
		setPosition(v.x, v.y, angle);
	}

	public void setPosition(float x, float y, float angle)
	{
		_body.setTransform(x, y, (float) (Math.toRadians(angle)));
	}
	
	public Bubble bubble()
	{
		return _bubble;
	}
	
	public void comeAlive()
	{
		_dead = false;		
		lockControls(false);
		Z.sim.entities().add(this);				
		onStartLife();		
		Tween.call(callbackEndInvincible).delay(1000).start(Z.sim.tweens());
	}
	
	public void onStartLife()
	{
		if(new TreatShield().isUnlocked())
			_justPickedUp = PowerUp.TYPE_SHIELD;
	}
	
	private final TweenCallback callbackEndInvincible = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			ghostMode(false);
		}
	};

	public int lives()
	{
		return _lives;
	}
	
	public void restoreState(int numLives, int bubbleType)
	{
		_lives = numLives;
		Z.sim.fireEvent(ZipZapSim.EV_NUM_LIVES_CHANGED, null);		
		_bubble.init(bubbleType);
	}
	
	@Override
	public void update(float dt)
	{
		if (_dead)
		{
			//Z.sim().spawnExlosion(origin());
			
			Z.sim().spawnFlash(origin(), Color.ORANGE);
			Z.sim.spawnCloud(origin(), 4, Color.ORANGE, 12f);
			Z.sim.spawnCloud(origin(), 4, Color.YELLOW, 6f);
			Z.sim.spawnCloud(origin(), 4, Color.WHITE, 3f);
			
			Z.sim.spawnExlosion(origin(), 4, Color.ORANGE, 12f);
			Z.sim.spawnExlosion(origin(), 4, Color.YELLOW, 6f);
			Z.sim.spawnExlosion(origin(), 4, Color.WHITE, 3f);
			
			Z.sim.fireEvent(ZipZapSim.EV_EXPLOSION_MEDIUM, null);
			
			Z.sim().spawnDebris(this, _body.getLinearVelocity());
			
			if(_lives > 0)
			{
				_lives--;
				_body.setLinearVelocity(0, 0);
				Z.sim.entities().removeValue(this, true);
				ghostMode(true);
				lockControls(true);
				Z.screen.sim().clearEnemies();
				Z.sim().fireEvent(ZipZapSim.EV_NUM_LIVES_CHANGED, null);
				new Reinforcer();
			}
			else
			{
				_lastLivePosition.set(origin());
				Z.sim().focalPoint(_lastLivePosition);
				Z.sim().fireEvent(ZipZapSim.EV_GAME_OVER, null);				
				free();
			}
		} 
		else
		{
			if(_hasUltraCapacitor && _touchDown && _chargeTime < CHARGE_TIME && !_dragonMode)
			{
				_chargeTime += dt;
				
				if(_chargeTime >= CHARGE_TIME)
					Z.sim.spawnFlash(origin(), Color.MAGENTA);
				
				if(_chargeTime > CHARGE_ATTRACTICLE_START_TIME && _chargeTime + 1f < CHARGE_TIME)
					Attracticle.spawn(this, Color.MAGENTA);
			}
			
			if (_hasMegaLaser)
			{
				_megaLaserTimer += dt;
				if (_megaLaserTimer > MEGALASER_TIME)
				{
					_hasMegaLaser = false;
				}
			}
			
			if (_dragonMode)
			{
				_dragonTimer -= dt;
				if (_dragonTimer < 0)
				{
					dragonMode(false);
				}
				else
				{
//					if(_ghostTime >= 0)
//					{
//						_ghostTime -= dt;
//						if(_ghostTime < 0)
//						{
//							_ghostTime = RELOAD_GHOST_TIME;
							Ghost.spawn(this, ghostStartColor, Color.YELLOW, Math.min(1.5f, _dragonTimer/3f), 0.7f);
//						}
//					}
				}
			}
			
			if(_hasAutoCannon && _touchDown)
			{
				_fireTime -= dt;
				if(_fireTime < 0)
				{
					tryFireCannon();
					//ShipProjectile.spawn(_haveMegaLaser);
					_fireTime = RELOAD_TIME;
				}
			}

			if (_justPickedUp != PowerUp.TYPE_NONE)
			{
				if(_justPickedUp <= _bubble.type())
				{
					Z.screen.sim().score(origin(), SCORE_SECOND_POWERUP, false);
				}
				else
				{
					switch (_justPickedUp)
					{
						case PowerUp.TYPE_BOMB:
							_bubble.init(PowerUp.TYPE_BOMB);
							Z.sim().fireEvent(ZipZapSim.EV_POWERUP_BOMB, null);
							break;
						case PowerUp.TYPE_SHIELD:
							_bubble.init(PowerUp.TYPE_SHIELD);
							Z.sim().fireEvent(ZipZapSim.EV_POWERUP_SHIELD, null);
							break;
					}
				}

				_justPickedUp = PowerUp.TYPE_NONE;
			}
			
			if(_jetTimer > 0)
			{
				_jetTimer -= dt;
				Particle.spawn(Z.v2().set(verts[1]).rotate(angle()).add(_body.getPosition()), Z.v1().set(0, 0), Color.ORANGE, 0.5f, 0, 0.2f);
			}
		}
	}

	public void enableCannon(boolean enabled)
	{
		_cannonEnabled = enabled;
	}
	
	public void enableRearCannon(boolean enabled)
	{
		_hasRearCannon = enabled;
	}

	public boolean dead()
	{
		return _dead;
	}

	@Override
	public void free()
	{
		ghostMode(true);
		Z.sim().entities().removeValue(this, true);
	}

	public Vector2 velocity()
	{
		return _body.getLinearVelocity();
	}

	@Override
	public int layer()
	{
		return 1;
	}

	@Override
	public float lineWidth()
	{
		return 1f;
	}

	@Override
	public void onBeginContact(Contact c, Fixture me, Fixture other)
	{
	}

	@Override
	public void onEndContact(Contact c, Fixture me, Fixture other)
	{
	}

	@Override
	public void postSolve(Contact c, ContactImpulse impulse, Fixture me,
			Fixture other)
	{
	}

	public boolean inBubble()
	{
		return _bubble.active();
	}

	public void kill()
	{
		_dead = true;
	}
	
	@Override
	public float clipRadius()
	{
		return 4f;
	}
}
