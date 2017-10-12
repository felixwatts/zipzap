package com.monkeysonnet.zipzap.script;

public interface IScriptEvent
{
	IScriptEvent fire();
	float time();
	void free();
}
