package com.monkeysonnet.zipzap.script;

import com.badlogic.gdx.graphics.Color;

public class AutoSpawner implements IAutoSpawner
{
	public static final float SPAWN_DELAY = 0.5f;
	
	private IActiveCount _spawner;
	
	private boolean _bool1;
	private int _type, _int1, _int2;
	private Color _color;
	private  float 
	_valueStart, 
	_valueEnd,
	_value,
	_xStart,
	_xEnd,
	_yStart, 
	_yEnd,
	_angleStart,
	_angleEnd,
	_speedStart,
	_speedEnd, 
	_itemCountStart, 
	_itemCountEnd,
	_spawnTimer;
	
	public AutoSpawner(
			IActiveCount spawner, 
			float endVal, 
			int type,
			float itemCountStart, 
			float itemCountEnd,
			float speedStart, 
			float speedEnd,
			float angleStart,
			float angleEnd,
			int int1,
			int int2,
			boolean bool1)
	{
		this(spawner, 
			0,
			endVal, 
			type,
			itemCountStart, 
			itemCountEnd,
			speedStart, 
			speedEnd,
			angleStart,
			angleEnd,
			int1,
			int2,
			bool1, null);
	}
	
	public AutoSpawner(
			IActiveCount spawner, 
			int type,
			float itemCountStart, 
			float itemCountEnd,
			float speedStart, 
			float speedEnd,
			float angleStart,
			float angleEnd,
			int int1,
			int int2,
			boolean bool1)
	{
		this(spawner, 
			0,
			100, 
			type,
			itemCountStart, 
			itemCountEnd,
			speedStart, 
			speedEnd,
			angleStart,
			angleEnd,
			int1,
			int2,
			bool1, null);
	}

	public AutoSpawner(
			IActiveCount spawner, 
			float startVal,
			float endVal, 
			int type,
			float itemCountStart, 
			float itemCountEnd,
			float speedStart, 
			float speedEnd,
			float angleStart,
			float angleEnd,
			int int1,
			int int2,
			boolean bool1,
			Color color)
	{
		_spawner = spawner;
		_valueStart = startVal;
		_valueEnd = endVal;
		_type = type;
		_angleStart = angleStart;
		_angleEnd = angleEnd;
		_int1 = int1;
		_int2 = int2;
		_bool1 = bool1;
		_speedStart = speedStart;
		_itemCountStart = itemCountStart;
		_speedEnd = speedEnd;
		_itemCountEnd = itemCountEnd;
		_color = color;
	}
	
	public void updateVal(float val)
	{
		if(val > _valueEnd || val < _valueStart)
		{
			val = _valueStart + ((val - _valueStart) % (_valueEnd - _valueStart));
		}
		
		_value = (val - _valueStart) / (_valueEnd - _valueStart);
		
//		while(_spawner.activeCount() < itemCountCurrent())
//		{
//			SpawnEvent.fire(
//					_type, 
//					currentValue(_xStart, _xEnd), 
//					currentValue(_yStart, _yEnd), 
//					_int1, 
//					_int2, 
//					currentValue(_angleStart, _angleEnd), 
//					currentValue(_speedStart, _speedEnd), 
//					_bool1);
//		}
	}
	
	public void update(float dt)
	{
		if(_spawnTimer > 0)
			_spawnTimer -= dt;
		
		if(_spawnTimer <= 0 && _spawner.activeCount() < itemCountCurrent())
		{
			_spawnTimer = SPAWN_DELAY;
			
			SpawnEvent.fire(
					_type, 
					currentValue(_xStart, _xEnd), 
					currentValue(_yStart, _yEnd), 
					_int1, 
					_int2, 
					currentValue(_angleStart, _angleEnd), 
					currentValue(_speedStart, _speedEnd), 
					_bool1,
					_color);
		}
	}
	
	private float currentValue(float start, float end)
	{
		return start + (_value * (end - start));
	}
	
	private float itemCountCurrent()
	{
		return currentValue(_itemCountStart, _itemCountEnd);
	}
}
