package com.monkeysonnet.lander;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.FixtureTag;
import com.monkeysonnet.engine.IContactHandler;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.IOrigin;
import com.monkeysonnet.zipzap.IRenderableSprite;
import com.monkeysonnet.zipzap.Z;

public class Guy implements IEntity, IContactHandler, IOrigin, IRenderableSprite
{
	private static final float SCALE = 0.2f;
	
	public static final float WIDTH = 13f * SCALE;
	public static final float HEIGHT = 16f * SCALE;	
	private static final float SPRITE_OFFSET_X = -7.5f * SCALE;
	private static final float SPRITE_OFFSET_Y = -12f * SCALE;
	
	private static final float THRUST_SCALE = 0.25f;
	private static final float THRUST_DROPOFF = 0.5f;
	private static final float THRUST_THRESHOLD = 1;
	private static final float LINEAR_DAMPING = 0.0f;
	private static final float THRUST_SCALE_PAN = 10f;
	private static final float IMPULSE_DAMAGE_SCALE = 5f;
	private static final float START_ENERGY = 100f;
	private static final float LAND_VERIFY_TIME = 1.5f;
	private static final float IMPULSE_DAMAGE_THRESHOLD = 5f;
	private static final float FRICTION = 0.6f;	
	private static final float SMOKE_SPEED = 24f;	
	private static final float BULLET_SPEED = 70f;//35f;
	
	private static final float GUN_OFFSET_X = 7.5f*SCALE;
	private static final float GUN_OFFSET_Y = -5f*SCALE;
	
	private static final int SFX_SHOOT = -1015;
	private static final int SFX_EXPLODE = -1016;
	
	private static final TextureRegion _texJetOn = Z.texture("jetpak-guy-jet-on-gun"), _texJetOff = Z.texture("jetpak-guy-jet-off-gun");
	
	private static final Vector2 lastLivePosition = new Vector2();

	private Body _body;
	private final Vector2 _thrust = new Vector2();
	private float _energy;
	private boolean _dead;
	private float _landedTime = -1;
	private int _numPadContacts;
	private Sprite _sprite;
	private boolean _facingLeft;
	public boolean lockControls;

	private boolean _isThrusting;

	public Guy(Vector2 loc)
	{
		_body = B2d
				.dynamicBody()
				.at(loc.x, loc.y + (HEIGHT/2f))				
				.fixedRotation(true)
				.linearDamping(LINEAR_DAMPING)
				.withFixture(B2d
						.box(WIDTH/2f, HEIGHT/2f)
						.density(0.1f)
						.category(LanderSim.COL_CAT_GUY)
						.mask(
								LanderSim.COL_CAT_WALL 
								| LanderSim.COL_CAT_PAD 
								| LanderSim.COL_CAT_ENEMY_BULLET
								| LanderSim.COL_CAT_ENEMY)
						.userData(new FixtureTag(this, this))
						.friction(FRICTION))
				.create(Z.sim.world());
		
		_hardMode = Z.prefs.getBoolean("jet-pak-hard-mode", false);

		_energy = START_ENERGY;
		
		_sprite = new Sprite(Z.texture("jetpak-guy-jet-off-gun"));
		_sprite.setSize(SCALE*15f, SCALE*20f);
		
		Z.sim.entities().add(this);
	}
	
	public Body body()
	{
		return _body;
	}
	
	public void tap(float x, float y)
	{
		if(!_dead)
		{		
			setSpriteFacing(x - origin().x);
			
			Z.v1().set(origin()).add(_facingLeft ? -GUN_OFFSET_X : GUN_OFFSET_X, GUN_OFFSET_Y);
			
			Z.v2().set(x, y).sub(Z.v1()).nor().mul(BULLET_SPEED).add(_body.getLinearVelocity());
			Bullet.spawn(
					Z.v1(), 
					Z.v2());
			
			L.sim.fireEvent(SFX_SHOOT, null);
		}
	}
	
	public void fling(float vx, float vy)
	{
		if(lockControls)
			return;
		
		if(_energy > 0)
			_thrust.set(vx * THRUST_SCALE, (vy > 0) ? 0 : -vy * THRUST_SCALE);

		setSpriteFacing(vx);
		
		if(!_isThrusting)
		{
			L.sim.fireEvent(LanderSim.EV_BEGIN_THRUST, null);
			_isThrusting = true;
		}
	}
	
	public void pan(float dx, float dy)
	{
		if(lockControls)
			return;
		
		if(_energy > 0)
			_thrust.set(dx * THRUST_SCALE_PAN, (dy > 0) ? 0 : -dy * THRUST_SCALE_PAN);		
		
		setSpriteFacing(dx);
		
		if(!_isThrusting)
		{
			L.sim.fireEvent(LanderSim.EV_BEGIN_THRUST, null);
			_isThrusting = true;
		}
	}
	
	private void setSpriteFacing(float latestXDir)
	{
		boolean dirChanged = (_facingLeft == latestXDir > 0);
		if(dirChanged)
		{
			_sprite.flip(true, false);
			_facingLeft = !_facingLeft;
		}	
	}
	
	public boolean dead()
	{
		return _dead;
	}

	@Override
	public void update(float dt)
	{
		if(_dead || _energy <= 0f)
		{
			_thrust.set(0, 0);
			
			SmokePuff.spawnExplosion(origin());
			Z.sim().spawnFlash(origin(), Color.ORANGE);
			Z.sim.spawnCloud(origin(), 4, Color.ORANGE, 12f);
			Z.sim.spawnCloud(origin(), 4, Color.YELLOW, 6f);
			Z.sim.spawnCloud(origin(), 4, Color.WHITE, 3f);
			
			for(int n = 0; n < 12; n++)
				Gib.spawn(origin(), _body.getLinearVelocity());
			
			Z.renderer.shakeCamera(1, 1f);

			L.sim.fireEvent(LanderSim.EV_EXPLOSION_MEDIUM, null);
			L.sim.fireEvent(SFX_EXPLODE, null);
			Z.sim.fireEvent(LanderSim.EV_PLAYER_DIED, null);
			
			free();
		}
		else
		{
			_sprite.setPosition(origin().x + SPRITE_OFFSET_X, origin().y + SPRITE_OFFSET_Y);
			
			if(_thrust.x != 0 || _thrust.y != 0)
			{
				_sprite.setRegion(_texJetOn);
				SmokePuff.spawn(Vector2.tmp.set(origin()).add(0, -HEIGHT/3f), Z.v1().set(_thrust).nor().mul(-SMOKE_SPEED).add(_body.getLinearVelocity()));
			}
			else _sprite.setRegion(_texJetOff);
			
			if(_facingLeft)
				_sprite.flip(true, false);
			
			if(_energy > 0)
			{
				if(_thrust.x != 0 || _thrust.y != 0)
				{
//					_body.applyForce(
//							Vector2.tmp.set(0, STABILIZATION_LIFT), 
//							Z.v1().set(0, THRUST_OFFSET).rotate(angle()).add(_body.getWorldCenter()));					
					
					_body.applyForceToCenter(Vector2.tmp.set(_thrust).rotate(angle()));												
					
					//_energy -= dt * _thrust.len() * FUEL_USAGE_SCALE;
					
					_thrust.mul(THRUST_DROPOFF);
					if(Math.abs(_thrust.x) < THRUST_THRESHOLD)
						_thrust.x = 0;
					if(Math.abs(_thrust.y) < THRUST_THRESHOLD)
						_thrust.y = 0;
					
					if(_thrust.x == 0 && _thrust.y == 0)
					{
						L.sim.fireEvent(LanderSim.EV_END_THRUST, null);
						_isThrusting = false;
					}
				}
			}	
//			else
//			{
//				_outOfFuelTime += dt * ((_body.getLinearVelocity().len2() < IS_MOVING_VEL_THRESHOLD_2) ? 1 : 0.5f);
//				if(_outOfFuelTime > MAX_OUT_OF_FUEL_TIME)
//				{
//					_dead = true;
//				}
//			}
			
			if(_landedTime > 0)
			{
				_landedTime -= dt;
				if(_landedTime <= 0)
				{
					// win
					L.sim.spawnWarpCurtain();
					L.sim.fireEvent(LanderSim.EV_LEVEL_COMPLETE, null);
					free();
				}
			}
		}
	}
	
	@Override
	public void free()
	{
		lastLivePosition.set(origin());
		L.sim.focalPoint(lastLivePosition);
		L.sim.entities().removeValue(this, true);
		L.sim.world().destroyBody(_body);		
	}
	
//	public float jetVolume()
//	{
//		return (_thrust.x == 0 && _thrust.y == 0) ? 0 : 0.25f;
//	}

	@Override
	public int layer()
	{
		return 3;
	}

	@Override
	public void onBeginContact(Contact c, Fixture me, Fixture other)
	{
		if((other.getFilterData().categoryBits & LanderSim.COL_CAT_PAD) != 0)
		{
			_numPadContacts++;
			
			// todo necessary?
			if(_numPadContacts > 1)
				_numPadContacts = 1;
			
			if(_numPadContacts == 1)
				_landedTime = LAND_VERIFY_TIME;
		}
	}

	@Override
	public void onEndContact(Contact c, Fixture me, Fixture other)
	{
		if((other.getFilterData().categoryBits & LanderSim.COL_CAT_PAD) != 0)
		{
			_numPadContacts--;
			
			// todo necessary
			if(_numPadContacts < 0)
				_numPadContacts = 0;
			
			if(_numPadContacts != 2)
				_landedTime = -1;
		}
	}

	@Override
	public void postSolve(Contact c, ContactImpulse impulse, Fixture me,
			Fixture other)
	{
		if((other.getFilterData().categoryBits & LanderSim.COL_CAT_WALL) != 0
				|| (other.getFilterData().categoryBits & LanderSim.COL_CAT_PAD) != 0)
		{
			// hit wall	
			for(int p = 0; p < /*c.getWorldManifold().getNumberOfContactPoints()*/ 1; p++)
			{
				float damage = Math.max(0, (impulse.getNormalImpulses()[p] - IMPULSE_DAMAGE_THRESHOLD) * IMPULSE_DAMAGE_SCALE);		
				
				if(damage > 0)
				{
					damage(damage);				
					L.sim.spawnSparks(c.getWorldManifold().getPoints()[p], _body.getLinearVelocity(), Gib.color);
				}
			}
		}
	}
	
	public final IBarData energyBar = new IBarData()
	{
		@Override
		public float val()
		{
			return _energy;
		}
		
		@Override
		public float maxVal()
		{
			return START_ENERGY;
		}
	};

	private boolean _hardMode;
	
//	public final IBarData fuelBar = new IBarData()
//	{
//		@Override
//		public float val()
//		{
//			return _fuel;
//		}
//		
//		@Override
//		public float maxVal()
//		{
//			return START_FUEL;
//		}
//	};
	
	public void kill()
	{
		_energy = 0;
		_dead = true;		
	}

	public void damage(float f)
	{
		if(_hardMode)
			_energy = 0;
		else
			_energy -= f;
		
		if(_energy < 0)
		{
			_energy = 0;
			_dead = true;
		}
		
		if(!_dead)
			L.sim.fireEvent(LanderSim.EV_OUCH, null);
	}

	@Override
	public float angle()
	{
		return (float)Math.toDegrees(_body.getAngle());
	}

	@Override
	public Vector2 origin()
	{
		return _body.getPosition();
	}
	
	@Override
	public Sprite sprite()
	{
		return _sprite;
	}
}
