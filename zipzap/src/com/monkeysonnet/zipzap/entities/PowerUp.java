package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.FixtureTag;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.IRenderableTexture;
import com.monkeysonnet.zipzap.PowerUpContactHandler;
import com.monkeysonnet.zipzap.Sim;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.achievements.BadgeRearCannon;
import com.monkeysonnet.zipzap.achievements.BadgeShield;
import com.monkeysonnet.zipzap.achievements.BadgeUltraCapacitor;
import com.monkeysonnet.zipzap.achievements.Notification;

public class PowerUp implements IEntity, IRenderableTexture
{
	public static final PowerUpPool pool = new PowerUpPool();
	public static final float RADIUS = 2f;
	public static final int TYPE_NONE = -1;
	public static final int TYPE_MEGA_LASER = 0;
	public static final int TYPE_SHIELD = 1;	
	public static final int TYPE_BOMB = 2;
	public static final int TYPE_DRAGON = 3;
	public static final int TYPE_ULTRA_CAPACITOR = 4;
	public static final int TYPE_SHIELD_UPGRADE = 5;
	public static final int TYPE_REAR_CANNON = 6;
	private static final float MAX_DST_TO_SHIP_2 = 4225;	
	public static final int SFX_PICKUP = -1007;
	private static final Notification notification = new Notification();
	
	private Body _body;
	private int _type;
	private TextureRegion _tex;
	private boolean _pickedUp;
	
	private static class PowerUpPool extends Pool<PowerUp>
	{
		@Override
		protected PowerUp newObject()
		{
			return new PowerUp();
		}
	}
	
	private PowerUp(){}
	
	public void clearForBoss()
	{
		switch(_type)
		{
			case PowerUp.TYPE_BOMB:
			case PowerUp.TYPE_DRAGON:
			case PowerUp.TYPE_MEGA_LASER:
			case PowerUp.TYPE_SHIELD:
				free();
		}
	}
	
	public static boolean canSpawn(int type)
	{
		switch(type)
		{
			case TYPE_REAR_CANNON:
				return !BadgeRearCannon.instance().isEarned();
			case TYPE_ULTRA_CAPACITOR:
				return !BadgeUltraCapacitor.instance().isEarned();
			case TYPE_SHIELD_UPGRADE:
				return !BadgeShield.instance().isEarned();
			default:
				return true;
		}
	}
	
	public static PowerUp spawn(Vector2 origin, int type)
	{
		if(type == TYPE_NONE)
			return null;
		
		if(type == TYPE_REAR_CANNON && BadgeRearCannon.instance().isEarned())
			return null;
		
		if(type == TYPE_ULTRA_CAPACITOR && BadgeUltraCapacitor.instance().isEarned())
			return null;
		
		if(type == TYPE_SHIELD_UPGRADE && BadgeShield.instance().isEarned())
			return null;
		
		PowerUp p = pool.obtain();
		p.init(origin, type);
		Z.sim().entities().add(p);
		return p;
	}

	@Override
	public float radius()
	{
		return RADIUS;
	}

	@Override
	public Vector2 origin()
	{
		return _body.getPosition();
	}

	@Override
	public TextureRegion texture()
	{
		return _tex;
	}

	@Override
	public void update(float dt)
	{
		if(_pickedUp || origin().dst2(Z.ship().origin()) > MAX_DST_TO_SHIP_2)
			free();
	}

	@Override
	public void free()
	{		
		Z.sim().spawnExlosion(origin(), 8, color());
		Z.sim().world().destroyBody(_body);
		Z.sim().entities().removeValue(this, true);
		pool.free(this);
	}
	
	public void pickup()
	{
		if(_pickedUp)
			return;
		
		_pickedUp = true;
		
		switch(_type)
		{
			case PowerUp.TYPE_ULTRA_CAPACITOR:
				if(BadgeUltraCapacitor.instance().queue())
					doNotification();
				break;
			case PowerUp.TYPE_SHIELD_UPGRADE:
				if(BadgeShield.instance().queue())
					doNotification();
				break;
			case PowerUp.TYPE_REAR_CANNON:
				if(BadgeRearCannon.instance().queue())
					doNotification();
				break;
		}
		
		Z.sim.fireEvent(SFX_PICKUP, null);
	}
	
	private void doNotification()
	{
		notification.icon = _tex;
		notification.worldLoc.set(origin());
		notification.color.set(color());
		Z.sim.fireEvent(Sim.EV_ENQUEUE_NOTIFICATION, notification);		
	}

	@Override
	public int layer()
	{
		// TODO Auto-generated method stub
		return 2;
	}

	private void init(Vector2 origin, int type)
	{
		_pickedUp = false;
		_type = type;
		_body = B2d
				.staticBody()
				.at(origin)
				.withFixture(B2d
						.box(RADIUS, RADIUS)
						.category(ZipZapSim.COL_CAT_POWERUP)
						.mask(ZipZapSim.COL_CAT_SHIP)
						.userData(new FixtureTag(this, PowerUpContactHandler.instance())))
				.create(Z.sim().world());
		
		switch(_type)
		{
			case TYPE_BOMB:
				_tex = Z.texture("zipzap-powerup-bomb"); 
				break;
			case TYPE_MEGA_LASER:
				_tex = Z.texture("zipzap-powerup-megalaser"); 
				break;
			case TYPE_SHIELD:
			case TYPE_SHIELD_UPGRADE:
				_tex = Z.texture("zipzap-powerup-shield"); 
				break;
			case TYPE_DRAGON:
				_tex = Z.texture("zipzap-powerup-dragon"); 
				break;
			case TYPE_ULTRA_CAPACITOR:
				_tex = Z.texture("zipzap-powerup-ultra-capacitor"); 
				break;
			case TYPE_REAR_CANNON:
				_tex = Z.texture("zipzap-powerup-rear-cannon"); 
				break;
		}
	}
	
	public int type()
	{
		return _type;
	}

	@Override
	public Color color()
	{
		return colorForType(_type);
	}
	
	public static Color colorForType(int type)
	{
		switch(type)
		{
			case TYPE_BOMB:
				return Color.RED;
			case TYPE_MEGA_LASER:			
				return Color.MAGENTA;
			case TYPE_SHIELD:
				return Color.CYAN;
			case TYPE_DRAGON:
				return Color.YELLOW;
			case TYPE_ULTRA_CAPACITOR:
				return BadgeUltraCapacitor.instance().color();
			case TYPE_REAR_CANNON:
				return BadgeRearCannon.instance().color();
			case TYPE_SHIELD_UPGRADE:
				return BadgeShield.instance().color();
			default:
				return Color.GREEN;
		}
	}
	
	@Override
	public float angle()
	{
		return 0;
	}
}
