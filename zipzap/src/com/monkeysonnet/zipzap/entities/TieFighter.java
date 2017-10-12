package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.FixtureTag;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IContactHandler;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.zipzap.IHitable;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;

public class TieFighter implements IEntity, IRenderableMultiPolygon,
IContactHandler, IHitable
{
	private static final TieFighterPool pool = new TieFighterPool();
	private static Vector2[][] vertsArr;
	
	private static final int BH_APPROACH = 0;
	private static final int BH_FLEE = 2;
	private static final float SPEED = 15;
	private static final float PROJECTILE_SPEED = 20;
	private static final int ENERGY = 1;
	private static final float GLOW_TIME = 0.1f;
	private static final float FLEE_DST_2 = 400;
	private static final float APPROACH_DST_2 = 2500f;
	private static final float FIRE_PERIOD = 3f;
	
	
	private Body _body;
	private float _justHit;
	private FixtureTag _fixtureTag = new FixtureTag(this, this);
	private int _energy;
	private boolean _dead;
	private int _behaviour;
	private float _fireTime;
	private Fixture _fixtureBody;
	
	static
	{
		Map m = new Map("tie-fighter.v");
		
		vertsArr = new Vector2[3][];
		int i = 0;
		for(String s : new  String[]{ "p1", "w1", "w2" })
		{
			Array<Vector2> n = new Array<Vector2>(m.shape(s).shape);
			vertsArr[i] = new Vector2[n.size];
			for(int x = 0; x < n.size; x++)
			{
				vertsArr[i][x] = n.get(x);
				vertsArr[i][x].mul(1.25f);
			}

			i++;
		}		
	}
	
	private static class TieFighterPool extends Pool<TieFighter>
	{
		@Override
		protected TieFighter newObject()
		{
			return new TieFighter();
		}
	}
	
	public static void spawn()
	{
		float angle = Game.Dice.nextFloat() * 360;
		Game.workingVector2a.set(ZipZapSim.SPAWN_DISTANCE, 0).rotate(angle).add(Z.ship().origin());
		spawn(Game.workingVector2a);
	}
	
	public static void spawn(Vector2 loc)
	{
		TieFighter s = pool.obtain();

		Game.workingVector2b.set(Z.ship().origin()).sub(loc).nor().mul(SPEED);
		
		s._dead = false;
		s._body = B2d
				.kinematicBody()
				.at(loc)
				.withFixture(B2d
						.loop(vertsArr[0])
						.category(ZipZapSim.COL_CAT_METEORITE)
						.mask(ZipZapSim.COL_CAT_SHIP | ZipZapSim.COL_CAT_BUBBLE)
						.userData(s._fixtureTag))
				.withFixture(B2d
						.loop(vertsArr[1])
						.category(ZipZapSim.COL_CAT_METEORITE)
						.mask(ZipZapSim.COL_CAT_SHIP | ZipZapSim.COL_CAT_BUBBLE)
						.userData(s._fixtureTag))
				.withFixture(B2d
						.loop(vertsArr[2])
						.category(ZipZapSim.COL_CAT_METEORITE)
						.mask(ZipZapSim.COL_CAT_SHIP | ZipZapSim.COL_CAT_BUBBLE)
						.userData(s._fixtureTag))
				.create(Z.sim().world());
		
		s._fixtureBody = s._body.getFixtureList().get(0);
		
		s._energy = ENERGY;
		s._behaviour = BH_APPROACH;
		
		Z.sim().entities().add(s);
	}

	@Override
	public float angle(int poly)
	{
		return (float)(Math.toDegrees(_body.getAngle()) % 360f);
	}

	@Override
	public Vector2 origin(int poly)
	{
		return _body.getPosition();
	}

	@Override
	public Vector2[] verts(int poly)
	{
		return vertsArr[poly];
	}

	@Override
	public Color color(int poly)
	{
		switch(poly)
		{
			case 0:
				return Color.RED;
			default:
				return Color.GRAY;
		}		
	}

	@Override
	public float lineWidth(int poly)
	{
		return 1f;
	}

	@Override
	public void update(float dt)
	{
		if(_dead)
			free();
		else 
		{
			if(!Z.ship().dead())
			{					
				Game.workingVector2a.set(Z.ship().origin()).sub(origin(0));	
				if(_behaviour == BH_APPROACH && Game.workingVector2a.len2() < FLEE_DST_2)
				{
					flee();
				}
				else if(_behaviour == BH_FLEE && Game.workingVector2a.len2() > APPROACH_DST_2)
					_behaviour = BH_APPROACH;
				
				Game.workingVector2a.set(Z.ship().origin()).sub(origin(0));	
				if(_behaviour == BH_APPROACH)
				{	
					_body.setLinearVelocity(Game.workingVector2a.nor().mul(SPEED));
					_body.setTransform(origin(0), (float)Math.toRadians(Game.workingVector2a.angle()));
				}
				
				if(_behaviour == BH_APPROACH)
				{
					if(_fireTime > 0)
						_fireTime -= dt;
					if(_fireTime <= 0)
					{
						Projectile.spawn(origin(0), Game.workingVector2a.set(_body.getLinearVelocity()).nor().mul(PROJECTILE_SPEED));
						_fireTime = FIRE_PERIOD;
					}
				}
				else if(_behaviour == BH_FLEE)
				{
					Game.workingVector2a.set(Z.ship().origin()).sub(origin(0).rotate(90));	
					Game.workingVector2b.set(SPEED, 0).rotate(Game.workingVector2a.angle()+180);
					_body.setLinearVelocity(Game.workingVector2b);
					_body.setTransform(origin(0), (float)Math.toRadians(Game.workingVector2b.angle()));
				}
			}				
			
			if(_justHit > 0)
				_justHit -= dt;
		}			
	}

	private void flee()
	{
		_behaviour = BH_FLEE;
		Game.workingVector2a.set(Z.ship().origin()).sub(origin(0).rotate(-90));	
		Game.workingVector2b.set(SPEED, 0).rotate(Game.workingVector2a.angle()+180);
		_body.setLinearVelocity(Game.workingVector2b);
		_body.setTransform(origin(0), (float)Math.toRadians(Game.workingVector2b.angle()));
		
//		Game.workingVector2a.set(Z.ship().origin()).sub(origin(0).rotate(90));	
//		_body.setLinearVelocity(Game.workingVector2a.nor().mul(SPEED));
//		_body.setTransform(origin(0), (float)Math.toRadians(Game.workingVector2a.angle()));
	}

	@Override
	public void free()
	{
		Z.sim().spawnDebris(this, _body.getLinearVelocity());
		Z.sim().spawnExlosion(origin(0));
		Z.sim().entities().removeValue(this, true);
		Z.sim().world().destroyBody(_body);
		pool.free(this);
	}

	@Override
	public int layer()
	{
		return 0;
	}

	@Override
	public void onBeginContact(Contact c, Fixture me, Fixture other)
	{
		_dead = true;	
		
		if(other.getBody().getUserData() == Z.ship())
		{
			Z.ship().strike();
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

	@Override
	public boolean hit(Fixture f, boolean mega, Vector2 loc, Vector2 norm)
	{
		if(f == _fixtureBody)
		{		
			Z.sim().spawnExlosion(origin(0));
			flee();
			_justHit = GLOW_TIME;
			_energy--;
			if(_energy == 0)
				_dead = true;
		}
		
		return false;
	}

	@Override
	public int getNumPolys()
	{
		return 3;
	}
	
	@Override
	public boolean isHitable()
	{
		return true;
	}
	
	@Override
	public boolean isLoop(int poly)
	{
		return true; // todo
	}
		
	@Override
	public float clipRadius()
	{
		return 4f;
	}
}
