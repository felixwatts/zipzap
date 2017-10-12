package com.monkeysonnet.lander;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.zipzap.IRenderableTexture;
import com.monkeysonnet.zipzap.ITriggerable;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.entities.Enemy;

public class SentryGun extends Enemy implements IRenderableTexture, ITriggerable
{
	private static final int START_ENERGY = 5;
	private static final float HEIGHT = 8;
	private static final float WIDTH = 8;	
	private static final float RANGE_2 = 2500f;
	private static final float RELOAD_TIME = 0.1f;
	private static final float BULLET_SPEED = 35f;
	private static final float AIM_VARIANCE = 3f;
	public static final float RADIUS = 3;
	private static final int SFX_ACTIVATE = -1020;
	private static final float NOZZLE_OFFSET = RADIUS/2f;	
	private static final TextureRegion texture = Z.texture("sentry-barrel");	
	private static final SentryGunPool pool = new SentryGunPool();	

	private static class SentryGunPool extends Pool<SentryGun>
	{
		@Override
		protected SentryGun newObject()
		{
			return new SentryGun();
		}
	}

	private float _reloadTime;
	private boolean _isOn;
	private String _name;
	
	private SentryGun()
	{
		_behaviours.add(new ShootableBehaviour(START_ENERGY));
	}	
	
	public static void spawn(Vector2 loc, boolean on, String name)
	{
		SentryGun g = pool.obtain();
		
		g._body = B2d
				.staticBody()
				.at(loc)
				.rotated((float)Math.toRadians(-90))
				.withFixture(B2d
						.box(RADIUS, (RADIUS/7f)*2f)
						.category(LanderSim.COL_CAT_ENEMY)
						.mask(LanderSim.COL_CAT_GUY_BULLET | LanderSim.COL_CAT_GUY | LanderSim.COL_CAT_WALL)
						.userData(g._fixtureTag))
				.create(L.sim.world());
		
		g._name = name;
		
		g.activate(on);
		
		if(!on && name != null)
			L.sim.register(g, name);
		
		g.onSpawn();
		
		L.sim.environment().put(g, loc.x - WIDTH, loc.y - WIDTH, 2, WIDTH * 2f, HEIGHT * 2f);		
	}
	
	private void activate(boolean on)
	{
		if(on == _isOn)
			return;
		
		_isOn = on;
		if(!_isOn)
			_body.setTransform(origin(), (float)Math.toRadians(-90));
		else
			L.sim.fireEvent(SFX_ACTIVATE, origin());
	}

	@Override
	public void update(float dt)
	{
		super.update(dt);
		
		if(!_dead && _isOn)
		{		
			_body.setTransform(origin(), (float)Math.toRadians(Vector2.tmp
							.set(L.sim.guy().origin())
							.sub(origin()).angle()));
			
			if(_reloadTime >= 0)
				_reloadTime -= dt;
			if(_reloadTime < 0)
			{
				float dst2 = origin().dst2(L.sim.guy().origin());
				if(dst2 < RANGE_2)
				{
					_reloadTime = RELOAD_TIME;
					
					SentryBullet.spawn(Z.v1().set(NOZZLE_OFFSET, 0).rotate(angle()).add(origin()), 
							Vector2.tmp
							.set(L.sim.guy().origin())
							.sub(origin())
							.rotate((float)Game.Dice.nextGaussian()*AIM_VARIANCE)
							.nor()
							.mul(BULLET_SPEED));
					
					L.sim.fireEvent(LanderSim.EV_LASER_SMALL, origin());						
				}
			}
		}
	}

	@Override
	public float radius()
	{
		return RADIUS;
	}

	@Override
	public TextureRegion texture()
	{
		return texture;
	}

	@Override
	public Color color()
	{
		return Color.WHITE;
	}
	
	@Override
	protected void onFree()
	{
		L.sim.unregister(_name);
		L.sim.environment().remove(this);
		pool.free(this);
	}
	
	@Override
	public int layer()
	{
		return 2;
	}
	
	@Override
	public void onKill()
	{
		super.onKill();		
		Smoker.spawn(origin());
	}

	@Override
	public boolean trigger()
	{
		activate(true);
		return true;
	}
}
