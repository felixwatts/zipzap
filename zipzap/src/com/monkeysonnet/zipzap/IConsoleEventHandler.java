package com.monkeysonnet.zipzap;

public interface IConsoleEventHandler
{
	void bufferEmpty();
	void tap(int row);
	void dismiss();
	void callback(Object arg);
	void textEntered(String text);
	void cancelInput();
}
