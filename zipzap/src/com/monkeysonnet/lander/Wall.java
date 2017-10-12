package com.monkeysonnet.lander;

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
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.Tools;
import com.monkeysonnet.zipzap.Z;

public class Wall implements IEntity, IRenderableMultiPolygon, IContactHandler
{
	private static final float FRICTION = 0.6f;
	
	private static final WallPool pool = new WallPool();	
	private static class WallPool extends Pool<Wall>
	{
		@Override
		protected Wall newObject()
		{
			return new Wall();
		}
	}
	
	protected final FixtureTag fixtureTag = new FixtureTag(this, null);	
	protected final FixtureTag fixtureTagPad = new FixtureTag(this, this);	
	protected Body _body;
	private Vector2[] _verts;
	private final Color _colour = new Color();
	private int _colCat;
	private boolean _loop;

	private Object _nextLevel;
	
	public static void spawn(Vector2[] verts, Color color, int colCat, boolean loop, int nextLevel)
	{
		Wall w = pool.obtain();
		init(w, verts, color, colCat, loop, nextLevel);
	}
	
	protected static void init(Wall w, Vector2[] verts, Color color, int colCat, boolean loop, int nextLevel)
	{
		w._colCat = colCat;
		w._colour.set(color);
		w._verts = verts;	
		w._loop = loop;
		w._nextLevel = nextLevel;
		
		if(loop)
			w._body = B2d
					.staticBody()				
					.withFixture(B2d
							.loop(verts)
							.category(colCat)
							.mask(LanderSim.COL_CAT_GUY | LanderSim.COL_CAT_GUY_BULLET | LanderSim.COL_CAT_ENEMY_BULLET | LanderSim.COL_CAT_ENEMY | LanderSim.COL_CAT_WALL)
							.userData(nextLevel < 0 ? w.fixtureTag : w.fixtureTagPad)
							.friction(FRICTION))
					.create(Z.sim().world());
		else
			w._body = B2d
			.staticBody()				
			.withFixture(B2d
					.chain(verts)
					.category(colCat)
					.mask(LanderSim.COL_CAT_GUY | LanderSim.COL_CAT_GUY_BULLET | LanderSim.COL_CAT_ENEMY_BULLET | LanderSim.COL_CAT_ENEMY | LanderSim.COL_CAT_WALL)
					.userData(nextLevel < 0 ? w.fixtureTag : w.fixtureTagPad)
					.friction(FRICTION))
			.create(Z.sim().world());
		
		Z.sim().addEnvironment(w, verts);
	}

	@Override
	public void update(float dt)
	{
	}

	@Override
	public void free()
	{
		Z.sim.world().destroyBody(_body);
		pool.free(this);
	}

	@Override
	public int layer()
	{
		return 1;
	}
		
	@Override
	public float clipRadius()
	{
		return 400f;
	}
	
	public int colCat()
	{
		return _colCat;
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
		return Tools.zeroVector;
	}

	@Override
	public Vector2[] verts(int poly)
	{
		return _verts;
	}

	@Override
	public Color color(int poly)
	{
		return _colour;
	}

	@Override
	public float lineWidth(int poly)
	{
		return 1f;
	}

	@Override
	public boolean isLoop(int poly)
	{
		return _loop;
	}

	@Override
	public void onBeginContact(Contact c, Fixture me, Fixture other)
	{
		if((other.getFilterData().categoryBits & LanderSim.COL_CAT_GUY) != 0)
		{
			L.sim.fireEvent(LanderSim.EV_SET_NEXT_LEVEL, _nextLevel);
		}
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
