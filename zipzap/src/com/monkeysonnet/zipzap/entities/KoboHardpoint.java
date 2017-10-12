package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;

public class KoboHardpoint implements IEntity
{
	private static final float SHOOT_DELAY = 1f;
	private static final float LAUNCH_DELAY = 5f;
	private static final KoboHardpointPool pool = new KoboHardpointPool();
	private static final float SHOOT_DST_2 = 1600f;
	private static final float PROJECTILE_SPEED = 20f;
	private Vector2 _loc, _origin;
	private boolean _isShipLauncher;
	private boolean _freed;
	
	private static class KoboHardpointPool extends Pool<KoboHardpoint>
	{
		@Override
		protected KoboHardpoint newObject()
		{
			return new KoboHardpoint();
		}
	}
	
	private float _timer;
	
	public static KoboHardpoint spawn(Vector2 loc, Vector2 origin, boolean isShipauncher)
	{
		KoboHardpoint h = pool.obtain();
		h._freed = false;
		h._origin = origin;
		h._loc = Z.sim().vector().obtain().set(loc);
		h._isShipLauncher = isShipauncher;
		Z.sim().entities().add(h);
		return h;
	}

	@Override
	public void update(float dt)
	{
		_timer -= dt;
		if(_timer < 0 && !Z.ship().ghostMode())
		{
			Vector2.tmp.set(_origin).add(_loc);
			
			if(Z.ship().origin().dst2(Vector2.tmp) < SHOOT_DST_2)
			{
				_timer = _isShipLauncher ? LAUNCH_DELAY : SHOOT_DELAY;

				if(_isShipLauncher)
				{
					ShootingSeeker.spawn(Vector2.tmp);
				}
				else
				{
					Z.sim.fireEvent(ZipZapSim.EV_LASER_SMALL, null);
					
					Projectile
						.spawn(Vector2.tmp, 
								Z
								.v1()
								.set(Z.ship().origin())
								.sub(Vector2.tmp)
								.nor()
								.mul(PROJECTILE_SPEED));
				}
			}
		}
	}

	@Override
	public void free()
	{
		if(!_freed)
		{
			Z.sim().vector().free(_loc);
			Z.sim().entities().removeValue(this, true);
			pool.free(this);
			_freed = true;
		}
	}

	@Override
	public int layer()
	{
		return 0;
	}
}
