package com.monkeysonnet.zipzap;

import com.badlogic.gdx.physics.box2d.Fixture;

public interface IFixtureEventHandler
{
	void onEvent(Fixture f);
}
