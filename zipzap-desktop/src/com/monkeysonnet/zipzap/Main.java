package com.monkeysonnet.zipzap;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.tools.imagepacker.TexturePacker;
import com.badlogic.gdx.tools.imagepacker.TexturePacker.Settings;

public class Main {
	public static void main(String[] args) 
	{
//		Settings settings = new Settings();
//		settings.padding = 2;
//		settings.maxWidth = 1024;
//		settings.maxHeight = 1024;
//		settings.minWidth = 4;
//		settings.minHeight = 4;
//		settings.incremental = true;
//		settings.pot = true;
//		settings.edgePadding = false;
//		settings.defaultFormat = Format.RGBA8888;
//		TexturePacker.process(settings, "../images", "../zipzap-android/assets");
		
		
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "zipzap";
		cfg.useGL20 = false;
		cfg.width = 1280;
		cfg.height = 720;
		
		new LwjglApplication(new ZipZapGame(), cfg);
	}
}
