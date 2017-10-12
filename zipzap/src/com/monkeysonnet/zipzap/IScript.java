package com.monkeysonnet.zipzap;

public interface IScript
{
	IGameController current();
	IGameController next();
	IGameController prev();
	int level();
	void dispose();
	void reset();
}
