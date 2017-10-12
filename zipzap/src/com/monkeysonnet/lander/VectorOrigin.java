package com.monkeysonnet.lander;

import com.badlogic.gdx.math.Vector2;
import com.monkeysonnet.zipzap.IOrigin;

public class VectorOrigin implements IOrigin
{
	private Vector2 _origin;

	public VectorOrigin(Vector2 v)
	{
		_origin = new Vector2(v);
	}

	@Override
	public Vector2 origin()
	{
		return _origin;				
	}

	@Override
	public float angle()
	{
		return 0;
	}
}
