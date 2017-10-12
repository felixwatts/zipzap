package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.monkeysonnet.b2dFluent.B2d;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.IHitable;
import com.monkeysonnet.zipzap.IRenderablePolygon;
import com.monkeysonnet.zipzap.Z;

public class Bubble implements IEntity, IRenderablePolygon //, IContactHandler
{
	private static final float RADIUS = 4f;
	private static final int NUM_VERTS = 10;
	
	private static final int SFX_INIT = -1003;
	private static final int SFX_BREAK = -1018;
	
	private Body _body;
	private int _type = -1;
	private Vector2[] _verts;
	private boolean _dead;
	private boolean _isStrike;
	
	public Bubble(World w)
	{
		_body = B2d
				.staticBody()
				.active(false)
//				.withFixture(B2d
//						.circle()
//						.sensor(true)
//						.radius(RADIUS)
//						.category(Sim.COL_CAT_BUBBLE)
//						.mask(Sim.COL_CAT_METEORITE)
//						.userData(new FixtureTag(this, this)))
				.create(w);
		
		_verts = new Vector2[NUM_VERTS];
		for(int n = 0; n < NUM_VERTS; n++)
			_verts[n] = new Vector2().set(RADIUS, 0).rotate(n * (360f/NUM_VERTS));		
	}

	@Override
	public float angle()
	{
		return 0;
	}

	@Override
	public Vector2 origin()
	{
		return Z.screen.sim().ship().origin();
	}

	@Override
	public Vector2[] verts()
	{
		return _verts;
	}

	@Override
	public Color color()
	{
		switch(_type)
		{
			case PowerUp.TYPE_SHIELD:
			default:
				return Color.CYAN;
			case PowerUp.TYPE_BOMB:
				return Color.RED;
		}
	}

	@Override
	public float lineWidth()
	{
		return 1f;
	}

	@Override
	public void update(float dt)
	{
		_body.setTransform(Z.ship().origin(), 0);
		
		if(_dead && _isStrike)
		{
			_isStrike = false;
			
			switch(_type)
			{
				case PowerUp.TYPE_SHIELD:			
					break;
				case PowerUp.TYPE_BOMB:
					
					for(int n = Z.sim().entities().size-1; n >= 0; n--)
					{
						if(Z.sim().entities().get(n) instanceof IHitable)
						{
							((IHitable)Z.sim().entities().get(n)).hit(null, true, null, null);
						}
					}
		
					break;
			}
			
			Z.sim().spawnDebris(this, _body.getLinearVelocity());
			
			_type = PowerUp.TYPE_NONE;
			
			free();
			
			//Z.sfx.play(18, 3f, false);
			Z.sim.fireEvent(SFX_BREAK, null);
		}
	}
	
	public void strike()
	{
		_dead = true;
		_isStrike = true;
	}
	
	public boolean active()
	{
		return _body.isActive();
	}
	
	public void init(int type)
	{
		if(type == _type)
			return;
		
		if(!_body.isActive())
		{
			Z.sim().entities().add(this);
			_body.setActive(true);
		}
		
		_type = Math.max(_type, type);
		
		_dead = false;
		
		Z.sim.fireEvent(SFX_INIT, null);
	}
	
	public int type()
	{
		return _type;
	}
	
	public void reinit()
	{
		if(_type != -1)
		{
			init(_type);			
		}
	}

	@Override
	public void free()
	{		
		Z.sim().entities().removeValue(this, true);
		_body.setActive(false);
		//_type = -1;
	}

	@Override
	public int layer()
	{
		return 0;
	}
	
	@Override
	public float clipRadius()
	{
		return RADIUS;
	}
}
