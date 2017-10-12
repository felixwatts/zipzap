package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Array;
import com.monkeysonnet.engine.FixtureTag;
import com.monkeysonnet.engine.IContactHandler;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.engine.editor.Shape;
import com.monkeysonnet.zipzap.IHitable;
import com.monkeysonnet.zipzap.IOrigin;
import com.monkeysonnet.zipzap.IPhysical;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;
import com.monkeysonnet.zipzap.behaviours.IBehaviour;

public class Entity implements IEntity, IPhysical, IContactHandler, IOrigin, IHitable
{
	protected final Array<IBehaviour> _behaviours = new Array<IBehaviour>();
	
	protected Body _body;
	protected final FixtureTag _fixtureTag = new FixtureTag(this, this);
	protected boolean _dead;
	protected boolean _freed;
	protected int _killScore = 0;
	protected boolean _killed;
	
	protected static final Filter basicFilter = new Filter();
	
	static
	{
		basicFilter.categoryBits = ZipZapSim.COL_CAT_METEORITE;
		basicFilter.maskBits = ZipZapSim.COL_CAT_SHIP;
	}

	@Override
	public void update(float dt)
	{
		if(_dead)
			free();
		else
		{		
			for(IBehaviour b : _behaviours)
			{
				b.update(dt, this);
				
				if(_dead)
					break;
			}
		}
	}
	
	public int killScore()
	{
		return _killScore;
	}
	
	public boolean dead()
	{
		return _dead;
	}
	
	protected void onSpawn()
	{
		_dead = false;
		_freed = false;
		_killed = false;
		
		for(IBehaviour b : _behaviours)
			b.spawn(this);
	}
	
	public void addBehaviour(IBehaviour b)
	{
		_behaviours.add(b);
	}
	
	public void removeBehaviour(IBehaviour b)
	{
		_behaviours.removeValue(b, true);
	}

	@Override
	public void free()
	{
		if(Z.sim().inPhysicalUpdate())
			_dead = true;
		else if(!_freed)
		{
			_freed = true;
			
			for(IBehaviour b : _behaviours)
				b.onFree(this);
			
			onFree();
			
			Z.sim().entities().removeValue(this, true);
			
			if(_body != null)
			{
				Z.sim().world().destroyBody(_body);
				_body = null;
			}
			
			_dead = true;			
		}
	}
	
	protected void onFree()
	{		
		Gdx.app.debug("onFree not implemented", getClass().getName());
	}

	@Override
	public int layer()
	{
		return 2;
	}

	@Override
	public Body body()
	{
		return _body;
	}

	@Override
	public void onBeginContact(Contact c, Fixture me, Fixture other)
	{
		for(IBehaviour b : _behaviours)
			b.onBeginContact(this, c, me, other);
	}

	@Override
	public void onEndContact(Contact c, Fixture me, Fixture other)
	{
		for(IBehaviour b : _behaviours)
			b.onEndContact(this, c, me, other);
	}

	@Override
	public void postSolve(Contact c, ContactImpulse impulse, Fixture me,
			Fixture other)
	{
		for(IBehaviour b : _behaviours)
			b.postSolve(this, c, impulse, me, other);
	}

	@Override
	public Vector2 origin()
	{
		return _body.getWorldCenter();
	}
	
	public float angle()
	{
		return (float)Math.toDegrees(_body.getAngle());
	}

	@Override
	public boolean hit(Fixture f, boolean mega, Vector2 loc, Vector2 norm)
	{
		if(_dead)
			return false;
		
		for(IBehaviour b : _behaviours)
		{
			if(!_dead)
				b.hit(this, f, mega, loc, norm);
		}
		
		return false;
	}

	@Override
	public boolean isHitable()
	{
		return !_dead;
	}
	
	protected static Color[] getColours(Map map)
	{
		Color[] result = new Color[map.numShapes()];
		
		for(int n = 0; n < map.numShapes(); n++)
		{
			Shape s = map.shape(n);
			
			if(s.properties == null)
			{
				result[n] = Color.GRAY;
			}			
			else if(s.properties.equals("red"))
			{
				result[n] = Color.RED;
			}
			else if(s.properties.equals("gray"))
			{
				result[n] = Color.GRAY;
			}
			else if(s.properties.equals("cyan"))
			{
				result[n] = Color.CYAN;
			}
			else if(s.properties.equals("blue"))
			{
				result[n] = Color.BLUE;
			}
			else if(s.properties.equals("green"))
			{
				result[n] = Color.GREEN;
			}
			else if(s.properties.equals("yellow"))
			{
				result[n] = Color.YELLOW;
			}
			else if(s.properties.equals("white"))
			{
				result[n] = Color.WHITE;
			}
			else if(s.properties.equals("magenta"))
			{
				result[n] = Color.MAGENTA;
			}
			else result[n] = null;
			
		}
		
		return result;
	}
	
	public void targetDirection(Vector2 v)
	{
		_body.setLinearVelocity(v);
	}
	
	public Vector2 targetDirection()
	{
		return _body.getLinearVelocity();
	}
	
	public void onKill()
	{
		Z.sim.fireEvent(ZipZapSim.EV_EXPLOSION_MEDIUM, null);
		_killed = true;
	}
}
