package com.monkeysonnet.zipzap.behaviours;

import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.IPhysical;

public class FaceDirectionOfTravelBehaviour extends BehaviourBase
{
	private static FaceDirectionOfTravelBehaviour _instance;
	
	private FaceDirectionOfTravelBehaviour(){}

	@Override
	public void update(float dt, IEntity subject)
	{
		IPhysical p = (IPhysical)subject;
		p.body().setTransform(p.body().getWorldCenter(), (float)Math.toRadians(p.body().getLinearVelocity().angle()));
	}
	
	public static FaceDirectionOfTravelBehaviour instance()
	{
		if(_instance == null)
			_instance = new FaceDirectionOfTravelBehaviour();
		return _instance;
	}
}
