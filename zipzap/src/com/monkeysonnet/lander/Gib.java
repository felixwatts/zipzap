package com.monkeysonnet.lander;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.FixtureTag;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IContactHandler;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.IRenderableSprite;
import com.monkeysonnet.zipzap.Tools;
import com.monkeysonnet.zipzap.Z;

public class Gib implements IEntity, IRenderableSprite, IContactHandler
{
	private static final GibPool pool = new GibPool();
	private static final float AVG_VEL = 8f;
	private static final float VEL_VAR = 8f;
	
	private static final float AVG_MAX_AGE = 1.5f;
	private static final float MAX_AGE_VAR = 1f;
	
	private static final float AVG_ANGULAR_VEL = 3f;
	private static final float ANGULAR_VEL_VAR = 3f;
	private static final float DENSITY = 1f;
	private static final float AVG_RADIUS = 0.6f;
	private static final float RADIUS_VAR = 0.2f;
	private static final float FRICTION = 0.5f;
	private static final float RELOAD_TIME = 0.125f;
	
	public static final Color color = new Color(200f/255f, 55f/255f, 55f/255f, 1f);
	
	private static final TextureRegion[] textures = new TextureRegion[]
	{
		Z.texture("jetpak-gib-1"),
		Z.texture("jetpak-gib-2"),
		Z.texture("jetpak-gib-3")
	};
		
	private static class GibPool extends Pool<Gib>
	{
		@Override
		protected Gib newObject()
		{
			return new Gib();
		}
	}
	
	private final FixtureTag _fixtureTag = new FixtureTag(this, this);
	private final Sprite _sprite = new Sprite();
	
	private Body _body;
	private float _age;
	private float _maxAge;	
	private float _reloadTime;
	
	private Gib()
	{
		_sprite.setRegion(textures[Game.Dice.nextInt(textures.length)]);		
	}
	
	public static void spawn(Vector2 loc, Vector2 sourceVel)
	{
		Gib g = pool.obtain();
		
		float radius = Tools.random(AVG_RADIUS, RADIUS_VAR);
		
		g._age = 0;
		
		g._body = B2d
				.dynamicBody()
				.at(loc)				
				.linearVelocity(Vector2
						.tmp
						.set(Tools.random(AVG_VEL, VEL_VAR), 0)
						.rotate(Game.Dice.nextFloat()*360f)
						.add(sourceVel))
				.angularVelocity(Tools.random(AVG_ANGULAR_VEL, ANGULAR_VEL_VAR))
				.withFixture(B2d
						.circle()
						.friction(FRICTION)
						.radius(radius)
						.density(DENSITY)
						.userData(g._fixtureTag))
				.create(Z.sim.world());
		
		g._sprite.setSize(radius*2f, radius*2f);
		g._sprite.setOrigin(radius, radius);
		
		g._maxAge = Tools.random(AVG_MAX_AGE, MAX_AGE_VAR);
		
		Z.sim.entities().add(g);
	}

	@Override
	public Sprite sprite()
	{
		return _sprite;
	}

	@Override
	public void update(float dt)
	{
		_age += dt;
		if(_age > _maxAge)
			free();
		else
		{
			float angle = (float)Math.toDegrees(_body.getAngle());
			
			_reloadTime -= dt;
			if(_reloadTime < 0)
			{			
				SpriteParticle.spawn(_body.getPosition(), textures[0], 0.8f, AVG_RADIUS, angle);
				_reloadTime = RELOAD_TIME;			
			}
			_sprite.setRotation(angle);
			_sprite.setPosition(_body.getPosition().x - AVG_RADIUS, _body.getPosition().y - AVG_RADIUS);
		}
	}

	@Override
	public void free()
	{
		L.sim.spawnSparks(_body.getPosition(), _body.getLinearVelocity(), color);		
		Z.sim.entities().removeValue(this, true);	
		Z.sim.world().destroyBody(_body);
		pool.free(this);
	}

	@Override
	public int layer()
	{
		return 2;
	}

	@Override
	public void onBeginContact(Contact c, Fixture me, Fixture other)
	{
		//L.sim.fireEvent(SFX_HIT_WALL, _body.getPosition());
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
