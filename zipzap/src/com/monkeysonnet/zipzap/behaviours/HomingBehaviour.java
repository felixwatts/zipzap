package com.monkeysonnet.zipzap.behaviours;

import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.entities.Entity;

public class HomingBehaviour extends BehaviourBase
{
	private float _lockAngle;
	private float _angularVelocity;
	private boolean _flee;
	
	public HomingBehaviour(float lockAngle, float angularVelocity, boolean flee)
	{
		_lockAngle = lockAngle;
		_angularVelocity = angularVelocity;
		_flee = flee;
	}
	
	@Override
	public void update(float dt, IEntity subject)
	{
		if(Z.ship().ghostMode())
			return;
		
		//IPhysical s = (IPhysical)subject;
		
		Entity e = (Entity)subject;
		
		float angleToShip = (Z.v1().set(Z.ship().origin()).sub(e.body().getWorldCenter()).angle() + (_flee ? 180 : 0)) % 360;
		float angleOfTravel = e.targetDirection().angle();
		
		float deltaAngle = angleToShip - angleOfTravel;
		
		if(deltaAngle > 180)
			deltaAngle -= 360;
		
		if(Math.abs(deltaAngle) < _lockAngle)
		{
			float da = dt * _angularVelocity;
			if(deltaAngle < 0)
				da = -da;
			
			Z.v1().set(e.targetDirection()).rotate(da);
			e.targetDirection(Z.v1());
			//s.body().setLinearVelocity(Z.v1());
		}
	}
}
