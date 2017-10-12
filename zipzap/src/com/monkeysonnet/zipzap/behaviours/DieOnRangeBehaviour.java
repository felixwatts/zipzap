package com.monkeysonnet.zipzap.behaviours;

import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.IOrigin;
import com.monkeysonnet.zipzap.Z;

public class DieOnRangeBehaviour extends BehaviourBase
{
	private static final float MAX_RANGE_2 = 10000;
	private static DieOnRangeBehaviour _instance;
	
	@Override
	public void update(float dt, IEntity subject)
	{
		if(((IOrigin)subject).origin().dst2(Z.sim().focalPoint()) > MAX_RANGE_2)
			subject.free();
	}
	
	public static DieOnRangeBehaviour instance()
	{
		if(_instance == null)
			_instance = new DieOnRangeBehaviour();
		return _instance;
	}
}
