package com.monkeysonnet.zipzap.behaviours;

import com.monkeysonnet.engine.ICallback;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.IOrigin;
import com.monkeysonnet.zipzap.Tools;
import com.monkeysonnet.zipzap.Z;

public class ProximityBehaviour extends BehaviourBase
{
	private float _minAngleToShip, _maxAngleToShip, _maxDstToShip2, _reloadTime, _time;
	private ICallback _callback;
	
	public ProximityBehaviour(float minAngleToShip, float maxAngleToShip, float maxDstToShip, float reloadTime, ICallback callback)
	{
		_minAngleToShip = minAngleToShip;
		_maxAngleToShip = maxAngleToShip;
		_maxDstToShip2 = maxDstToShip * maxDstToShip;
		_reloadTime = reloadTime;
		_callback = callback;
	}
	
	@Override
	public void update(float dt, IEntity subject)
	{
		if(_time > 0)
			_time -= dt;
		
		if(_time <= 0)
		{
			IOrigin e = (IOrigin)subject;
			
			if(Z.ship().origin().dst2(e.origin()) < _maxDstToShip2)
			{
				float angle = e.angle();
				if(angle > 180)
					angle -= 360;
				float angleToShip = Tools.angleToShip(e.origin());
				if(angleToShip > 180)
					angleToShip -= 360f;
				
				float relAngle = angleToShip - angle;
				
				if(relAngle >= _minAngleToShip && relAngle <= _maxAngleToShip)
				{
					_callback.callback(null);
					_time = _reloadTime;
				}
			}
		}
	}
}
