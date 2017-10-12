package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.zipzap.IBossBody;
import com.monkeysonnet.zipzap.IRenderableTexture;
import com.monkeysonnet.zipzap.Sim;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.achievements.Notification;
import com.monkeysonnet.zipzap.behaviours.KillOnContactBehaviour;

public class BossEye extends Entity implements IRenderableTexture // IRenderableMultiPolygon
{
	private static final float RADIUS = 2;
	public static final int EV_BOSS_KILLED = 0;
	private static BossEye _instance;
	
	private final Color _color = new Color();
	private IBossBody _handler;
	private final Notification badgeNotification = new Notification();
	//private Map _map;
	private static final TextureRegion _tex = Z.texture("boss-eye");
	
	private BossEye()
	{	
	}
	
	public static BossEye instance()
	{
		if(_instance == null)
			_instance = new BossEye();
		
		return _instance;
	}
	
	public BossEye init(Color color, float scale, IBossBody handler)
	{
		_handler = handler;
		_color.set(color);
		_body = B2d
				.staticBody()
				.withFixture(B2d
						.circle()
						.radius(RADIUS * scale)
						.category(ZipZapSim.COL_CAT_METEORITE)
						.mask(ZipZapSim.COL_CAT_SHIP)
						.userData(_fixtureTag))
						.create(Z.sim().world());
		
		_behaviours.add(KillOnContactBehaviour.basic());
		
		Z.sim().entities().add(this);
		
		Z.screen.sim().target(this);
		
		onSpawn();
		
		return this;
	}
	
	public void setLoc(Vector2 loc, float angle)
	{
		_body.setTransform(loc, (float)Math.toRadians(angle));
	}
	
	@Override
	public boolean hit(Fixture f, boolean mega, Vector2 loc, Vector2 norm)
	{
		if(!_dead)
		{
			if(!_handler.isEyeVulnerable())
				return false;
			else
				_handler.onEyeHit();
		}
		
		return false;
	}

	@Override
	protected void onFree()
	{
		// no op
		
		Z.screen.sim().target(null);
	}

	@Override
	public float radius()
	{
		return RADIUS * (13f/11f);
	}
	
	@Override
	public TextureRegion texture()
	{
		return _tex ;
	}
	
	@Override
	public Color color()
	{
		return _color;
	}
	
	@Override
	public int layer()
	{
		return 2;
	}
	
	@Override
	public float angle()
	{
		return super.angle() + 90;
	}
	
	public void doNotification()
	{
		badgeNotification.color.set(_color);
		badgeNotification.icon = Z.texture("zipzap-notification-boss");
		badgeNotification.worldLoc.set(BossEye.instance().origin());
		Z.sim.fireEvent(Sim.EV_ENQUEUE_NOTIFICATION, badgeNotification);	
	}
}
