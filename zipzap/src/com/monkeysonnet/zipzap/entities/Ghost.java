package com.monkeysonnet.zipzap.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.IRenderablePolygon;
import com.monkeysonnet.zipzap.Z;

public class Ghost implements IEntity, IRenderableMultiPolygon
{
	private static final int MAX_VERTS_PER_POLY = 16;
	private static final int MAX_POLYS = 8;
	
	private Vector2[][] _verts = new Vector2[MAX_POLYS][];
	private int _numPolys;
	private float _age;
	private float _maxAge;
	private float _dSize;
	private final Color _color = new Color(), _startColor = new Color(), _endColor = new Color();
	private boolean _dead;
	private float _angle;
	private final Vector2 _origin = new Vector2();
	
	private static final GhostPool pool = new GhostPool();
	private static class GhostPool extends Pool<Ghost>
	{
		@Override
		protected Ghost newObject()
		{
			return new Ghost();
		}
	}
	
	private Ghost()
	{
	}
	
	public static void spawn(IRenderablePolygon source, Color startColor, Color endColor, float maxAge, float dSize)
	{
		Ghost g = pool.obtain();
		
		g._dead = false;
		g._age = 0;
		g._maxAge = maxAge;
		g._dSize = dSize;
		g._color.set(startColor);
		g._startColor.set(startColor);
		g._endColor.set(endColor);
		g._numPolys = 1;
		g._angle = source.angle();
		g._origin.set(source.origin());
		

		if(g._verts[0] == null)
			g._verts[0] = new Vector2[MAX_VERTS_PER_POLY];
		
		Vector2[] verts = source.verts();
		for(int v = 0; v < verts.length; v++)
		{
			g._verts[0][v] = Z.sim().vector().obtain().set(verts[v]);
		}
				
		Z.sim().entities().add(g);
	}
	
	public static void spawn(IRenderableMultiPolygon source, Color startColor, Color endColor, float maxAge, float dSize)
	{
		Ghost g = pool.obtain();
		
		g._dead = false;
		g._age = 0;
		g._maxAge = maxAge;
		g._dSize = dSize;
		g._color.set(startColor);
		g._startColor.set(startColor);
		g._endColor.set(endColor);
		g._numPolys = source.getNumPolys();
		g._angle = source.angle(0);
		g._origin.set(source.origin(0));
		
		for(int p = 0; p < source.getNumPolys(); p++)
		{
			if(g._verts[p] == null)
				g._verts[p] = new Vector2[MAX_VERTS_PER_POLY];
			
			Vector2[] verts = source.verts(p);
			for(int v = 0; v < verts.length; v++)
			{
				g._verts[p][v] = Z.sim().vector().obtain().set(verts[v]);
			}
		}
		
		Z.sim().entities().add(g);
	}

	@Override
	public int getNumPolys()
	{
		return _numPolys;
	}

	@Override
	public float angle(int poly)
	{
		return _angle;
	}

	@Override
	public Vector2 origin(int poly)
	{
		return _origin;
	}

	@Override
	public Vector2[] verts(int poly)
	{
		return _verts[poly];
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
	public void update(float dt)
	{
		_age += dt;
		if(_age > _maxAge)
			free();
		else
		{
			float factor = _age / _maxAge;
			
			_color.r = _startColor.r + ((_endColor.r - _startColor.r) * factor);
			_color.g = _startColor.g + ((_endColor.g - _startColor.g) * factor);
			_color.b = _startColor.b + ((_endColor.b - _startColor.b) * factor);
			_color.a = _startColor.a + ((0 - _startColor.a) * factor);
			
			float scaleFactor = 1-((1-_dSize)*dt);
			
			for(int p = 0; p < _numPolys; p++)
			{
				for(int v = 0; v < MAX_VERTS_PER_POLY; v++)
				{
					if(_verts[p][v] != null)
						_verts[p][v].mul(scaleFactor);
				}
			}
		}
	}

	@Override
	public void free()
	{
		if(!_dead)
		{
			_dead = true;
			
			for(int p = 0; p < _numPolys; p++)
			{
				for(int v = 0; v < MAX_VERTS_PER_POLY; v++)
				{
					if(_verts[p][v] != null)
					{
						Z.sim().vector().free(_verts[p][v]);
						_verts[p][v] = null;
					}
				}
			}
			
			Z.sim().entities().removeValue(this, true);
		}
	}

	@Override
	public int layer()
	{
		return 0;
	}
	
	@Override
	public boolean isLoop(int poly)
	{
		return true; // todo
	}
	
	@Override
	public float clipRadius()
	{
		return 4f; // todo
	}
}
