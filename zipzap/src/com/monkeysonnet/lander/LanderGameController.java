package com.monkeysonnet.lander;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.monkeysonnet.engine.ColorTools;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.ICallback;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.engine.editor.Point;
import com.monkeysonnet.engine.editor.Shape;
import com.monkeysonnet.zipzap.IConsoleEventHandler;
import com.monkeysonnet.zipzap.IGameController;
import com.monkeysonnet.zipzap.Sim;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.achievements.BadgeJetPakWings1;
import com.monkeysonnet.zipzap.achievements.BadgeJetPakWings2;
import com.monkeysonnet.zipzap.achievements.Notification;
import com.monkeysonnet.zipzap.entities.Patch;
import com.monkeysonnet.zipzap.entities.Speck;

public class LanderGameController implements IGameController
{
	private static final float VOL_DST_DROPOFF = 16f;
	private static final int SFX_JET = 35;
	
	private int _level;
	//private long _jetSfxId;

	protected Tween _tweenRestart;
	private final ICallback onAchievementsCompleteRestartCallback = new ICallback()
	{		
		@Override
		public void callback(Object arg)
		{
			L.sim.start();
		}
	};
	
	private final ICallback onAchievementsCompleteAdvanceCallback = new ICallback()
	{		
		@Override
		public void callback(Object arg)
		{
			L.sim.advanceScript();
		}
	};
	
	public static final Color 
		colorRockOutline = new Color(145f/255f, 138f/255f, 111f/255f, 1f), 
		colorPadOutline = new Color(212f/255f, 170f/255f, 0, 1f);
	
	public LanderGameController(int level)
	{
		_level = level;
	}
	
	@Override
	public void update(float dt)
	{
	}
	
	@Override
	public void onSimulationEvent(int eventType, Object argument)
	{
		switch(eventType)
		{			
			case LanderSim.EV_PLAYER_DIED:	
				
				if(_tweenRestart != null)
					_tweenRestart.kill();
				Z.sfx.stop(SFX_JET);
				
				_tweenRestart = Tween.call(restartCallback).delay(3000).start(L.sim.tweens());

				break;
			case LanderSim.EV_LEVEL_COMPLETE:
				
				if(_tweenNextLevel != null)
					_tweenNextLevel.kill();				
				Z.sfx.stop(SFX_JET);
				_tweenNextLevel = Tween.call(nextLevelCallback).delay(1500).start(L.sim.tweens());
				break;	
	
			case LanderSim.EV_SCRIPT_COMPLETE:
				
				if(!BadgeJetPakWings1.instance().isEarned())
				{
					Z.console().clearNow()
						.setAvatar(Z.texture("miner"), Z.colorTutorial)
						.setColour(Z.colorTutorial, Z.colorTutorialBg)
						.write("Good job soldier!")
						.touch()
						.clear()
						.write("You have survived basic jet-pak training with all your limbs still attached.")
						.touch()
						.clear()
						.write("Have a badge.")
						.callback(new Object())
						.touch()
						.clear()
						.write("You will now progress to Advanced Jet-Pak Training, where you must complete the same set of challenges with FULL HEALTH.")
						.touch()
						.clear()
						.write("Good luck with that.. ")
						.pause(750)						
						.write("LOLZ");
				
					Z.console().setHandler(new IConsoleEventHandler()
					{
						@Override
						public void textEntered(String text)
						{

						}
						
						@Override
						public void tap(int row)
						{
						}
						
						@Override
						public void dismiss()
						{
							Game.ScreenManager.pop();
							
							Z.achievementsScreen.doAchievements(new ICallback()
							{
								@Override
								public void callback(Object arg)
								{
									Z.script.reset();
									Z.sim.start();
								}
							});
						}
						
						@Override
						public void cancelInput()
						{
						}
						
						@Override
						public void callback(Object arg)
						{
							BadgeJetPakWings1.instance().queue();
							
							Notification n = new Notification();
							n.color.set(BadgeJetPakWings1.instance().color());
							n.icon = Z.texture("notification-wings");
							n.worldLoc.set(Vector2.tmp.set(Z.renderer.cam().position.x, Z.renderer.cam().position.y));							
							Z.sim.fireEvent(Sim.EV_ENQUEUE_NOTIFICATION, n);
						}
						
						@Override
						public void bufferEmpty()
						{
						}
					});
					Game.ScreenManager.push(Z.consoleScreen);
				}
				else if(!BadgeJetPakWings2.instance().isEarned())
				{
					Z.console().clearNow()
					.setAvatar(Z.texture("miner"), Z.colorTutorial)
					.setColour(Z.colorTutorial, Z.colorTutorialBg)
					.write("Well I never..")
					.touch()
					.clear()
					.write("You have survived Advanced Jet-Pak Training with most of your head still attached.")
					.touch()
					.clear()
					.write("This is a first for the US Army Rocket Trooper Academy.")
					.touch()
					.clear()
					.write("Have a badge.")
					.callback(new Object())
					.touch()
					.clear()
					.write("You may now use the training facilities at your leisure.");
			
				Z.console().setHandler(new IConsoleEventHandler()
				{
					@Override
					public void textEntered(String text)
					{

					}
					
					@Override
					public void tap(int row)
					{
					}
					
					@Override
					public void dismiss()
					{
						Game.ScreenManager.pop();
						
						Z.achievementsScreen.doAchievements(new ICallback()
						{
							@Override
							public void callback(Object arg)
							{
								Z.script.reset();
								Z.sim.start();
							}
						});
					}
					
					@Override
					public void cancelInput()
					{
					}
					
					@Override
					public void callback(Object arg)
					{
						BadgeJetPakWings2.instance().queue();
						
						Notification n = new Notification();
						n.color.set(BadgeJetPakWings2.instance().color());
						n.icon = Z.texture("notification-wings");
						n.worldLoc.set(Vector2.tmp.set(Z.renderer.cam().position.x, Z.renderer.cam().position.y));							
						Z.sim.fireEvent(Sim.EV_ENQUEUE_NOTIFICATION, n);
					}
					
					@Override
					public void bufferEmpty()
					{
					}
				});
				Game.ScreenManager.push(Z.consoleScreen);
				}
				
				break;
				
			case LanderSim.EV_EXPLOSION_SMALL:
				
				Z.sfx.explosionSmall.play(argument);
				
				break;
				
			case LanderSim.EV_EXPLOSION_MEDIUM:
				
				Z.sfx.explosionMedium.play(argument);
				
				break;
				
			case LanderSim.EV_LASER_SMALL:
				
				Z.sfx.laserSmall.play(argument);
				
				break;
				
			case LanderSim.EV_OUCH:
				
				Z.sfx.ouch.play();
				
				break;
				
			case LanderSim.EV_BEGIN_THRUST:
				
				Z.sfx.play(SFX_JET, 0.1f, true);
				
				break;
				
			case LanderSim.EV_END_THRUST:
				
				Z.sfx.stop(SFX_JET);
				
				break;
				
			case LanderSim.EV_SET_NEXT_LEVEL:
				
				((Script)Z.script).nextLevel((Integer)argument);
		}
				
		if(eventType <= -1000)
		{			
			float vol = 1f;
			if(argument != null && argument instanceof Vector2)
			{
				float dst = L.sim.focalPoint().dst((Vector2)argument);
				vol = 1f / ((dst/VOL_DST_DROPOFF)+1);
			}
			
			Z.sfx.play((-eventType)-1000, vol);
		}
	}
	
	protected final TweenCallback restartCallback = new TweenCallback()
	{
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			_tweenRestart = null;			
			Z.achievementsScreen.doAchievements(onAchievementsCompleteRestartCallback);
		}
	};

	protected Tween _tweenNextLevel;
	
	protected final TweenCallback nextLevelCallback = new TweenCallback()
	{
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			_tweenNextLevel = null;
			Z.achievementsScreen.doAchievements(onAchievementsCompleteAdvanceCallback);
			//L.sim.advanceScript();			
		}
	};

	@Override
	public void init()
	{
		Z.renderer.backgroundColor(new Color(36f/255f, 31f/255f, 28f/255f, 1f));
		
		Map map;
		if(_level < 0)
			map = new Map("jetpak/menu.map", 10f, 0f);
		else map = new Map("jetpak/" + _level + ".map", 10f, 0f);
		
		buildMap(map);
		L.sim.spawnWarpCurtain();
	}
	
	protected void buildMap(Map map)
	{
		Z.sim.clear();
		
		Point ps = map.point("scale");
		if(ps != null)
		{
			float scale = Float.parseFloat(ps.properties);
			map.scale(scale);
		}
		
		if(map.gfxBg() != null)
			for(int n = 0; n < map.gfxBg().length; n++)
			{
				Patch.spawn(map.gfxBg()[n], false);
			}
		
		for(int n = 0; n < map.numShapes(); n++)
		{
			Shape s = map.shape(n);
			
			if(s.hasProperty("wall") || s.hasProperty("pad") || s.properties == null)
			{
				Color c = s.hasProperty("pad") 
						? colorPadOutline 
						: (s.hasProperty("colour") 
								? ColorTools.parse(s.propertyValue("colour")) 
								: colorRockOutline);				
				int colCat = s.hasProperty("pad") ? LanderSim.COL_CAT_PAD : LanderSim.COL_CAT_WALL;	
				int nextLevel = -1;
				if(s.hasProperty("next-level"))
					nextLevel = Integer.parseInt(s.propertyValue("next-level"));
				
				Wall.spawn(map.shape(n).shape, c, colCat, s.type == Shape.TYPE_LOOP, nextLevel);
			}
			else if(s.hasProperty("switch"))
			{
				if(s.hasProperty("target"))
				{					
					Color c = s.hasProperty("pad") ? colorPadOutline : (s.hasProperty("colour") ? ColorTools.parse(s.propertyValue("colour")) : colorRockOutline);					
					int colCat = s.hasProperty("pad") ? LanderSim.COL_CAT_PAD : LanderSim.COL_CAT_WALL;		
					Switch.spawnSwitch(map.shape(n).shape, c, colCat, s.type == Shape.TYPE_LOOP, s.propertyValue("target"));
				}
			}
			
//			if(s.hasProperty("pad"))
//			{
//				Z.sim.target(new VectorOrigin(Tools.centre(s.shape)));
//			}
		}
		
		if(map.gfxFg() != null)
			for(int n = 0; n < map.gfxFg().length; n++)
			{
				Patch.spawn(map.gfxFg()[n], true);
			}
		
		for(int n = 0; n < map.numPoints(); n++)
		{
			Point p = map.point(n);
			
//			if(p.hasProperty("fuel"))
//			{
//				PowerUp.spawnFuelCan(p.point);
//			}
//			else if(p.hasProperty("food"))
//			{
//				PowerUp.spawnBurger(p.point);
//			}

			if(p.hasProperty("sentry-gun"))
			{
				Sprite s = new Sprite(Z.texture("sentry-stand"));
				s.setSize(((10f/7f)*SentryGun.RADIUS), ((9f/7f)*SentryGun.RADIUS));		
				
				
				if(p.hasProperty("inverted"))
				{
					s.flip(false, true);
					s.setPosition(p.point.x - ((5f/7f)*SentryGun.RADIUS), p.point.y-((9f/7f)*SentryGun.RADIUS));
					SentryGun.spawn(Vector2.tmp.set(p.point).add(0, -((9f/7f)*SentryGun.RADIUS)), !p.hasProperty("off"), p.label);
				}
				else
				{
					s.setPosition(p.point.x - ((5f/7f)*SentryGun.RADIUS), p.point.y);
					SentryGun.spawn(Vector2.tmp.set(p.point).add(0, ((9f/7f)*SentryGun.RADIUS)), !p.hasProperty("off"), p.label);
				}
		
				Patch.spawn(s, false, Integer.MAX_VALUE);
			}
			else if(p.hasProperty("crate"))
			{
				Crate.spawn(p.point);
			}
			else if(p.hasProperty("laser") || p.hasProperty("tripwire"))
			{
				if(p.hasProperty("target"))
				{
					Point t = map.point(p.propertyValue("target"));
					if(t != null)
					{
						LaserBeam.spawn(p.point, t.point, p.hasProperty("on"), p.label, p.hasProperty("tripwire"), p.propertyValue("trip-target"));
					}
				}
			}
			else if(p.hasProperty("power-box"))
			{
				if(p.hasProperty("target"))
				{					
					PowerBox.spawn(p.point, p.propertyValue("target"));
				}
			}
			else if(p.hasProperty("burger") && p.hasProperty("num"))
			{
				int bNum = Integer.parseInt(p.propertyValue("num"));
				PowerUp.spawn(p.point, bNum);
			}
		}
		
		LanderSim sim = ((LanderSim)Z.sim);
		
		sim.initShip(map.point("player-start").point);
		
		Z.sim.focalPoint(sim.guy().origin());
		
		for(int n = 0; n < 20; n++)
		{
			Speck s = new Speck().init(-1f, -0.5f);
			Z.sim.entities().add(s);
		}
	}

	@Override
	public void cleanup()
	{
		Z.sim.clear();
		if(_tweenNextLevel != null)
		{
			_tweenNextLevel.kill();
			_tweenNextLevel = null;			
		}
		
		if(_tweenRestart != null)
		{
			_tweenRestart.kill();
			_tweenRestart = null;			
		}
	}

	@Override
	public void pause()
	{
	}

	@Override
	public void resume()
	{
		//_jetSfxId = Z.sfx.play(SFX_JET, 1f, true);
	}
}
