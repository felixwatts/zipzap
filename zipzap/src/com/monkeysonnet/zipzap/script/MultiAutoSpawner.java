package com.monkeysonnet.zipzap.script;

public class MultiAutoSpawner implements IAutoSpawner
{
	IAutoSpawner[] _spawners;
	
	public MultiAutoSpawner(IAutoSpawner... autoSpawners)
	{
		_spawners = autoSpawners;
	}

	@Override
	public void updateVal(float v)
	{
		for(int n = 0; n < _spawners.length; n++)
			_spawners[n].updateVal(v);
	}

	@Override
	public void update(float dt)
	{
		for(int n = 0; n < _spawners.length; n++)
			_spawners[n].update(dt);
	}
}
