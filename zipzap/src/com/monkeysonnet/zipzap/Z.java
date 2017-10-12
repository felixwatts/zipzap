package com.monkeysonnet.zipzap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.ITextureSource;
import com.monkeysonnet.zipzap.achievements.Achievements;
import com.monkeysonnet.zipzap.entities.Ship;
import com.monkeysonnet.zipzap.screens.AchievementsScreen;
import com.monkeysonnet.zipzap.screens.ConsoleScreen;
import com.monkeysonnet.zipzap.screens.TitleScreen;
import com.monkeysonnet.zipzap.screens.ZipZapScreen;
import com.monkeysonnet.zipzap.sound.Music;
import com.monkeysonnet.zipzap.sound.Sfx;

public class Z
{
	public static Sim sim;
	
	public static Sim sim()
	{
		return sim; // todo tidy up
	}
	
	public static Ship ship()
	{
		return screen.sim().ship();
	}
	
	public static ZipZapScreen screen;	
	public static SimRenderer renderer;
	
	public static SimRenderer renderer()
	{
		return renderer; // todo tidy up;
	}
	
	public static Hud hud()
	{
		return screen.hud();
	}
	
	public static Console console()
	{
		return consoleScreen.console();
	}	
	
	public static Vector2 v1()
	{
		return Game.workingVector2a;
	}
	
	public static Vector2 v2()
	{
		return Game.workingVector2b;
	}
	
	public static TextureRegion texture(String name)
	{
		return textures.get(name);
	}
	
	public static ITextureSource textures;
	public static ConsoleScreen consoleScreen;
	public static Preferences prefs;	
	public static IScript script;	
	public static TitleScreen titleScreen;	
	public static Achievements achievments;	
	public static AchievementsScreen achievementsScreen;
	public static IAndroid android;
	public static String deviceId = "bleh1";
	public static String username;
	public static Sfx sfx;
	public static Music music;
	public static final Color colorUi = Color.GREEN;// Color.RED;
	public static final Color colorTutorial = new Color(0f/255f , 255f/255f, 204f/255f, 1f);
	public static final Color colorTutorialBg = new Color(0f/255f , 255f/255f, 204f/255f, 0.2f);
	
	public static final boolean isDemo = false;
	
	public static boolean loaded;
	
	public static void load()
	{
		if(!loaded)
		{
			Z.prefs = Gdx.app.getPreferences("preferences");
//			Z.prefs.clear();
//			Z.prefs.flush();
			
			Z.consoleScreen = new ConsoleScreen();			
			Z.sfx = new Sfx();
			Z.music = new Music();
			Z.username = Z.prefs.getString("username", null);
			Z.titleScreen = new TitleScreen();
			Z.achievments = new Achievements();
			Z.achievementsScreen = new AchievementsScreen();
			Z.loaded = true;
		}
	}
	
	public static void dispose()
	{
		if(loaded)
		{
			Z.sfx.dispose();
			Z.sfx = null;
			Z.music.dispose();
			Z.music = null;
			Z.loaded = false;
		}
	}
	
	public static final String uriDemo = "http://monkeysonnet.com/astronomo/";// "https://play.google.com/store/apps/details?id=com.monkeysonnet.zipzap.demo";
	//public static final String uriFull = "https://play.google.com/store/apps/details?id=com.monkeysonnet.zipzap.full";
}
