package com.monkeysonnet.zipzap.behaviours;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.IOrigin;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.entities.Particle;

public class BlobTrailBehaviour extends BehaviourBase
{
	private static final float BLOB_PERIOD = 0.2f;
	private float _blobTime, _reloadTime, _maxAge, _radius, _dSize;
	private Color _colour;
	private final Vector2 _offset = new Vector2();
	
	public BlobTrailBehaviour(Color c)
	{
		this(c, BLOB_PERIOD, 2f, Z.v1().set(0, 0), 0.5f);
	}
	
	public BlobTrailBehaviour(Color c, float reloadTime, float maxAge)
	{
		this(c, reloadTime, maxAge, Z.v1().set(0, 0), 0.5f);
	}
	
	public BlobTrailBehaviour(Color c, float reloadTime, float maxAge, float radius)
	{
		this(c, reloadTime, maxAge, Z.v1().set(0, 0), radius);
	}
	
	public BlobTrailBehaviour(Color c, float reloadTime, float maxAge, Vector2 offset)
	{
		this(c, reloadTime, maxAge, offset, 0.5f);
	}
	
	public BlobTrailBehaviour(Color c, float reloadTime, float maxAge, Vector2 offset, float radius)
	{
		this(c, reloadTime, maxAge, offset, radius, 0);
	}
	
	public BlobTrailBehaviour(Color c, float reloadTime, float maxAge, Vector2 offset, float radius, float dSize)
	{
		_radius = radius;
		_offset.set(offset);
		_colour = new Color(c);
		_reloadTime = reloadTime;
		_maxAge = maxAge;
		_dSize = dSize;
	}

	@Override
	public void update(float dt, IEntity subject)
	{
		IOrigin o = (IOrigin)subject;
		
		_blobTime -= dt;
		if(_blobTime < 0)
		{
			_blobTime = _reloadTime;
			
			if(_offset.x == 0 && _offset.y == 0)
				Particle.spawn(o.origin(), Z.v1().set(0, 0), _colour, _radius, _dSize, _maxAge);				
			else
				Particle.spawn(Z.v2().set(_offset).rotate(o.angle()).add(o.origin()), Vector2.tmp.set(0, 0), _colour, _radius, _dSize, _maxAge);
		}
	}
	
	public void setColor(Color c)
	{
		_colour = c;
	}
}
