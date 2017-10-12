package com.monkeysonnet.zipzap.behaviours;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.monkeysonnet.engine.ICallback;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.IPhysical;
import com.monkeysonnet.zipzap.Z;

public class FireWhenFacingBehaviour extends BehaviourBase
{
	private float _angleThreshold, _fireFrequency, _reloadTime;
	private ICallback _fireCallback;
	
	public FireWhenFacingBehaviour(float angleThreshold, float reloadTime, ICallback fireCallback)
	{
		_angleThreshold = angleThreshold;
		_fireFrequency = reloadTime;
		_fireCallback = fireCallback;
	}
	
	@Override
	public void update(float dt, IEntity subject)
	{
		super.update(dt, subject);
		
		if(_reloadTime > 0)
		{
			_reloadTime -= dt;
		}
		
		if(_reloadTime <= 0)
		{
			Body b1 = ((IPhysical)subject).body();
			float angleToShip = Vector2.tmp.set(Z.ship().origin()).sub(b1.getPosition()).angle();
			float angleOfTravel = (float)Math.toDegrees(b1.getAngle());
			
			if(angleOfTravel > 180)
				angleOfTravel -= 360;
			if(angleToShip > 180)
				angleToShip -= 360;
			
			if((angleToShip > angleOfTravel - _angleThreshold) && (angleToShip < angleOfTravel + _angleThreshold))
			{
				_fireCallback.callback(null);
				_reloadTime = _fireFrequency;
			}
		}
	}
}
