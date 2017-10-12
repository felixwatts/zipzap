package com.monkeysonnet.zipzap;

import com.monkeysonnet.engine.ISimulationEventHandler;

public interface IGameController extends ISimulationEventHandler
{
	void init();
	void update(float dt);
	void cleanup();
	void pause();
	void resume();
}
