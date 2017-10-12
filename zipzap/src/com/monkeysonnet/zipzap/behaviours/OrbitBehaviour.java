package com.monkeysonnet.zipzap.behaviours;

import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.entities.Enemy;

public class OrbitBehaviour extends BehaviourBase
{
	private float _inner, _outer, _aVel;
	
	public OrbitBehaviour(float innerThreshold, float outerThreshold, float angularVelocity)
	{
		_aVel = angularVelocity;
		_inner = innerThreshold * innerThreshold;
		_outer = outerThreshold * outerThreshold;
	}
	
	@Override
	public void update(float dt, IEntity subject)
	{
//		if(Z.ship().ghostMode())
//			return;
		
		Enemy e = (Enemy)subject;
		
		float dst2 = e.body().getWorldCenter().dst2(Z.ship().origin());
		
		float aRel;
		if(dst2 < _inner)
			aRel = 180;
		else if(dst2 > _outer)
			aRel = 0;
		else aRel = 90;

		float angleToShip = (Z.v1().set(Z.ship().origin()).sub(e.body().getWorldCenter()).angle() + aRel) % 360;
		float angleOfTravel = e.targetDirection().angle();
		
		float deltaAngle = angleToShip - angleOfTravel;
		
		if(deltaAngle > 180)
			deltaAngle -= 360;
		
		float da = dt * _aVel;
		if(deltaAngle < 0)
			da = -da;
		
		Z.v1().set(e.targetDirection()).rotate(da);
		e.targetDirection(Z.v1());
		
	}
}
