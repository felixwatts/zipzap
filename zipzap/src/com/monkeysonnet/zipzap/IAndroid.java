package com.monkeysonnet.zipzap;

import com.monkeysonnet.engine.ICallback;

public interface IAndroid
{
	void shareText(CharSequence str);
	void scanText(ICallback onComplete);
	void openUri(String uri);	
	void share(String text, String img);
}
