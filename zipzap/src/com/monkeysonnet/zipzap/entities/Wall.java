package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
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
import com.monkeysonnet.zipzap.IHitable;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.KillOnContactBehaviour;

public class Wall implements IEntity, IRenderableMultiPolygon, IHitable
{
	private static final WallPool pool = new WallPool();	
	private static class WallPool extends Pool<Wall>
	{
		@Override
		protected Wall newObject()
		{
			return new Wall();
		}
	}
	
	public static final IContactHandler wallContactHandler = new IContactHandler()
	{
		@Override
		public void postSolve(Contact c, ContactImpulse impulse, Fixture me,
				Fixture other)
		{
		}
		
		@Override
		public void onEndContact(Contact c, Fixture me, Fixture other)
		{
		}
		
		@Override
		public void onBeginContact(Contact c, Fixture me, Fixture other)
		{
			KillOnContactBehaviour.alsoDie().onBeginContact(null, c, me, other);
		}
	};
	
	private final FixtureTag fixtureTag = new FixtureTag(this, wallContactHandler);	
	private Vector2[] _verts;
	private Color _colour;
	private Body _body;
	
	private Wall(){}
	
	public static Wall spawn(Vector2[] verts, Color colour)
	{
		Wall w = pool.obtain();
		
		w._colour = colour;
		w._verts = verts;	
		
		w._body = B2d
				.staticBody()
				.withFixture(B2d
						.loop(verts)
						.sensor(false)
						.category(ZipZapSim.COL_CAT_METEORITE)
						.mask(ZipZapSim.COL_CAT_SHIP | ZipZapSim.COL_CAT_METEORITE)
						.userData(w.fixtureTag))
				.create(Z.sim().world());
		
		Z.sim().addEnvironment(w, verts);
		
		return w;
	}
	
	@Override
	public void update(float dt)
	{
	}

	@Override
	public void free()
	{
		Z.sim().world().destroyBody(_body);
		Z.sim().environment().remove(this);
		pool.free(this);
	}

	@Override
	public int layer()
	{
		return 0;
	}

	@Override
	public boolean hit(Fixture f, boolean mega, Vector2 loc, Vector2 norm)
	{
		return false;
	}

	@Override
	public boolean isHitable()
	{
		return false;
	}

	@Override
	public int getNumPolys()
	{
		return 1;
	}

	@Override
	public float angle(int poly)
	{
		return 0;
	}

	@Override
	public Vector2 origin(int poly)
	{
		return Vector2.tmp.set(0, 0);
	}

	@Override
	public Vector2[] verts(int poly)
	{
		switch(poly)
		{
			case 0:
			default:
				return _verts;	
		}
	}

	@Override
	public Color color(int poly)
	{
		return _colour;
	}

	@Override
	public float lineWidth(int poly)
	{
		switch(poly)
		{
			case 0:
			default:
				return 1f;
		}
	}
	
	@Override
	public boolean isLoop(int poly)
	{
		return true;
	}	
	
	@Override
	public float clipRadius()
	{
		return 400f;
	}
}
