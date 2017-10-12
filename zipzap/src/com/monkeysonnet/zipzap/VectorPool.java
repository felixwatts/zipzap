package com.monkeysonnet.zipzap;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

public class VectorPool extends Pool<Vector2>
{
//	private int _v;

	@Override
	protected Vector2 newObject()
	{
//		_v++;
//		Gdx.app.log("felix", "# v:" + _v);
		
		return new Vector2();
	}
}
