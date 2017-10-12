package com.monkeysonnet.zipzap.entities;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Quad;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.FixtureTag;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.engine.editor.Shape;
import com.monkeysonnet.zipzap.IColourable;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.DieOnRangeBehaviour;
import com.monkeysonnet.zipzap.script.IActiveCount;

public class RangeMine extends Enemy implements IColourable, IRenderableMultiPolygon
{
	private static final float TRIGGER_RADIUS = 12;
	private static final float EXPLOSION_RADIUS = 16;
	
	private static final Map map = new Map("range-mine.v", 1.5f, 0);
	
	private static final RangeMinePool pool = new RangeMinePool();
	private static class RangeMinePool extends Pool<RangeMine>
	{
		@Override
		protected RangeMine newObject()
		{
			return new RangeMine();
		}
	}

	protected static int _activeCount;
	
	public static final IActiveCount activeCount = new IActiveCount()
	{
		@Override
		public int activeCount()
		{
			return _activeCount;
		}
	};
	
	private Fixture _triggerFixture;
	private boolean _isTriggered;
	private final Color _color = new Color(Color.GRAY);
	
	private RangeMine()
	{
		_behaviours.add(DieOnRangeBehaviour.instance());
	}
	
	public static void spawn(Vector2 loc)
	{
		RangeMine m = pool.obtain();
		
		m._color.set(Color.GRAY);
		m._isTriggered = false;
		m._body = B2d
				.staticBody()
				.at(loc)
				.linearVelocity(Z.v1().set(0, 0))
				.withFixture(B2d
						.polygon(map.shape(0).shape)
						.mask(ZipZapSim.COL_CAT_SHIP)
						.category(ZipZapSim.COL_CAT_METEORITE)
						.sensor(true)
						.userData(m._fixtureTag))
				.withFixture(B2d
						.polygon(map.shape(1).shape)
						.mask(ZipZapSim.COL_CAT_SHIP)
						.category(ZipZapSim.COL_CAT_METEORITE)
						.sensor(true)
						.userData(m._fixtureTag))
				.create(Z.sim().world());
		
		m._triggerFixture = B2d
				.circle()
				.radius(TRIGGER_RADIUS)
				.sensor(true)
				.category(ZipZapSim.COL_CAT_METEORITE)
				.mask(ZipZapSim.COL_CAT_SHIP)
				.userData(new FixtureTag(null, m))
				.create(m._body);
		
		m.onSpawn();
		Z.sim().entities().add(m);
		
		_activeCount++;
	}
	
	@Override
	protected void onFree()
	{		
		Z.sim().tweens().killTarget(this);
		pool.free(this);
		_activeCount--;
	}
	
	@Override
	public void onBeginContact(Contact c, Fixture me, Fixture other)
	{
		super.onBeginContact(c, me, other);
		
		if(!_dead)
		{
			if(me == _triggerFixture)
			{
				trigger();
			}
			else
			{
				Z.ship().strike();
				explode();
			}
		}
	}

	private void trigger()
	{
		if(!_isTriggered)
		{
			_isTriggered = true;
			
			Tween
				.to(this, 0, 500)
				.cast(IColourable.class)
				.target(Color.ORANGE.r, Color.ORANGE.g, Color.ORANGE.b, 1f)
				.ease(Quad.INOUT)
				.repeatYoyo(2, 0)
				.setUserData(this)
				.setCallbackTriggers(TweenCallback.COMPLETE)
				.setCallback(explodeCallback)
				.start(Z.sim().tweens());
		}
	}
	
	private static final TweenCallback explodeCallback = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			RangeMine m = (RangeMine)source.getUserData();
			m.explode();
		}
	};
	
	private void explode()
	{
		Z.sim().spawnFlash(origin(), Color.WHITE);
		Z.sim().spawnExlosion(origin(), 2, Color.ORANGE, EXPLOSION_RADIUS*2);
		Z.sim().spawnExlosion(origin(), 2, Color.YELLOW, EXPLOSION_RADIUS);
		Z.sim().spawnExlosion(origin(), 2, Color.WHITE);
		//Z.sim().spawnDebris(this, Vector2.tmp.set(0, 0));
		Z.screen.sim().applyRangeDamage(origin(), EXPLOSION_RADIUS, true, true, false);
		Z.screen.sim().score(origin(), 100, true);
		free();
	}
	
	@Override
	public boolean hit(Fixture f, boolean mega, Vector2 loc, Vector2 norm)
	{
		super.hit(f, mega, loc, norm);
		
		if(!_dead)
		{
			if(mega)
				explode();
			else
			{
				Z.screen.sim().score(origin(), 50, false);
				trigger();
			}
		}
		
		return false;
	}

	@Override
	public Color color()
	{
		return _color;
	}

	@Override
	public void setColor(float r, float g, float b, float a)
	{
		_color.set(r, g, b, a);
	}

	@Override
	public int getNumPolys()
	{
		return map.numShapes();
	}

	@Override
	public float angle(int poly)
	{
		return 0f;
	}

	@Override
	public Vector2 origin(int poly)
	{
		return _body.getWorldCenter();
	}

	@Override
	public Vector2[] verts(int poly)
	{
		return map.shape(poly).shape;
	}

	@Override
	public Color color(int poly)
	{
		return _color;
	}

	@Override
	public float lineWidth(int poly)
	{
		return 1f;
	}
	
	@Override
	public boolean isLoop(int poly)
	{
		return map.shape(poly).type == Shape.TYPE_LOOP;
	}
		
	@Override
	public float clipRadius()
	{
		return 4f;
	}
}
