package com.monkeysonnet.zipzap.behaviours;

import com.badlogic.gdx.graphics.Color;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.IRenderableMultiPolygon;
import com.monkeysonnet.zipzap.entities.Ghost;

public class GhostTrailBehaviour extends BehaviourBase
{
	private final Color _startColor = new Color(), _endColor = new Color();
	private float _period;
	private float _maxAge;
	private float _time;
	private IRenderableMultiPolygon _source;
	private float _dSize;
	
	public GhostTrailBehaviour(IRenderableMultiPolygon source, float period, float maxAge, Color startColor, Color endColor, float dSize)
	{
		_source = source;
		_period = period;
		_maxAge = maxAge;
		_startColor.set(startColor);
		_endColor.set(endColor);
	}
	
	@Override
	public void update(float dt, IEntity subject)
	{
		_time -= dt;
		if(_time <= 0)
		{
			Ghost.spawn(_source, _startColor, _endColor, _maxAge, _dSize);
			_time = _period;			
		}
	}
}
