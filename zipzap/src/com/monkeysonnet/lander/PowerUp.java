package com.monkeysonnet.lander;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.FixtureTag;
import com.monkeysonnet.engine.IContactHandler;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.IRenderableTexture;
import com.monkeysonnet.zipzap.Sim;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.achievements.BadgeBurger;
import com.monkeysonnet.zipzap.achievements.Notification;

public class PowerUp implements IRenderableTexture, IEntity, IContactHandler
{
	//private static final TextureRegion texFuelCan = Z.texture("jetpak-fuel-can");
	private static final TextureRegion texBurger = Z.texture("jetpak-burger");
	public static final float SIZE = 4;
	public static final int SFX_PICKUP = -1005;
	private static final PowerUpPool pool = new PowerUpPool();
	private static final Notification _notification = new Notification();
	
	private static class PowerUpPool extends Pool<PowerUp>
	{
		@Override
		protected PowerUp newObject()
		{
			return new PowerUp();
		}
	}
	
	private Body _body;
	private boolean _dead;
	private int _type;
	
	static
	{
		_notification.color.set(BadgeBurger.color);
		_notification.icon = Z.texture("notification-burger");
	}
	
	public static void spawn(Vector2 loc, int type)
	{
		if(BadgeBurger.instance(type).isEarned())
			return;
		
		PowerUp f = pool.obtain();

		f._dead = false;
		
		f._type = type;
		
		f._body = B2d.staticBody()
				.at(loc)				
				.withFixture(B2d
						.box(SIZE/2f, SIZE/2f)
						.sensor(true)
						.category(LanderSim.COL_CAT_ENEMY)
						.mask(LanderSim.COL_CAT_GUY)
						.userData(new FixtureTag(f, f)))
				.create(Z.sim.world());
		
		Z.sim.environment().put(f, loc.x - (SIZE/2f), loc.y - (SIZE/2f), 2, SIZE, SIZE);
	}
	
	private PowerUp()
	{		
	}
	
	public void collect()
	{
		Z.sim.fireEvent(SFX_PICKUP, null);
		if(BadgeBurger.instance(_type).queue())
		{
			_notification.worldLoc.set(origin());
			L.sim.fireEvent(Sim.EV_ENQUEUE_NOTIFICATION, _notification);
		}
		free();
	}

	@Override
	public void update(float dt)
	{
		if(_dead)
			free();
	}

	@Override
	public void free()
	{
		if(Z.sim.inPhysicalUpdate())
			_dead = true;
		else
		{
			Z.sim.world().destroyBody(_body);
			Z.sim.environment().remove(this);
		}
	}

	@Override
	public int layer()
	{
		return 2;
	}

	@Override
	public float radius()
	{
		return SIZE/2f;
	}

	@Override
	public Vector2 origin()
	{
		return _body.getPosition();
	}

	@Override
	public TextureRegion texture()
	{
		return texBurger;
	}

	@Override
	public Color color()
	{
		return Color.WHITE;
	}

	@Override
	public float angle()
	{
		return 0;
	}

	@Override
	public void onBeginContact(Contact c, Fixture me, Fixture other)
	{
		collect();
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
}
