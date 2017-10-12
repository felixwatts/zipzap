package com.monkeysonnet.zipzap.script;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.zipzap.IConsoleEventHandler;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.entities.MinerDude;

public class PlanetoidIntroController extends EnvironmentController implements IConsoleEventHandler
{
	public PlanetoidIntroController()
	{
		super(new Map("planetoid.v", PlanetoidController.SCALE, 0));
	}
	
	@Override
	public void init()
	{
		super.init();
		
		Z.console().setHandler(this);
		
		Z.ship().setPosition(_map.point("start").point, 90);
		Z.ship().lockControls(true);
		
		MinerDude.spawn(_map.point("man-1").point, Color.CYAN);
		MinerDude.spawn(_map.point("man-2").point, Color.YELLOW);
		
		Tween.call(new TweenCallback()
		{
			@Override
			public void onEvent(int type, BaseTween<?> source)
			{
				TextureRegion avatar = Z.texture("miner");
				
				Z.console()
					.setAvatar(avatar, Color.YELLOW)
					.write("A ship.. ")
					.pause(300)
					.write("We're saved!")
					.setAvatar(avatar, Color.CYAN)
					.write("careful brian, he could be scary..")
					.setAvatar(avatar, Color.YELLOW)
					.write("Hail captain. We welcome you.")
					.pause(300)
					.write(" We are peaceful miners..")
					.setAvatar(avatar, Color.CYAN)
					.write("heavily armed!")
					.setAvatar(avatar, Color.YELLOW)
					.write("what?")
					.setAvatar(avatar, Color.CYAN)
					.write("tell him we're heavily armed.")
					.setAvatar(avatar, Color.YELLOW)
					.write("..")
					.pause(1000)
					.write(" peaceful miners from Earth. ")
					.pause(1000)
					.write("We need your help.. ")
					.touch()
					.pause(200)
					.write("\n\nSome kind of huge alien worm has taken up residence inside our little asteroid..")
					.setAvatar(avatar, Color.CYAN)
					.write("it's scary!")
					.setAvatar(avatar, Color.YELLOW)
					.write("Yes, well.. it's already eaten most of our crew.")
					.pause(500)
					.write(" Frankly we'd rather like to see it blown to bits..")
					.setAvatar(avatar, Color.CYAN)
					.write("KAPOW!")
					.setAvatar(avatar, Color.YELLOW)
					.write(".. ")
					.pause(500)
					.write("quite.")
					.pause(500)
					.write(" So here's the plan..")
					.touch()
					.clear()
					.write("We've rigged the entrance to its tunnel with high explosives. ")
					.pause(1000)
					.write("If you can lure it to the surface then we'll detonate the explosives..")
					.setAvatar(avatar, Color.CYAN)
					.write("BOOOOOM!")
					.setAvatar(avatar, Color.YELLOW)
					.write("then it'll be smoked alien worm flesh for dinner.")
					.setAvatar(avatar, Color.CYAN)
					.write("Beats vacuum packed fungal protein!")
					.setAvatar(avatar, Color.YELLOW)
					.write("Off you go now captain. The tunnel entrance is just to the west of here.")
					.pause(1000)
					.write(" We'll be rooting for you..")
					.setAvatar(avatar, Color.CYAN)
					.write("From the safety of the surface.")
					.setAvatar(avatar, Color.YELLOW)
					.write("Oh one more thing about that terrifying megaworm captain..")
					.pause(1000)
					.write(" We think she's been laying eggs down there, so she may be extra aggressive towards any intruders.")
					.setAvatar(avatar, Color.CYAN)
					.write("LOL. That's an understatement.")
					.setAvatar(avatar, Color.YELLOW)
					.write(".. Yea.")
					.pause(750)
					.write(" So, good luck and all.");
				
				Game.ScreenManager.push(Z.consoleScreen);
			}
		}).delay(3000).start(Z.sim().tweens());
	}

	@Override
	public void bufferEmpty()
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
		Z.screen.sim().advanceScript();
	}

	@Override
	public void callback(Object arg)
	{
	}
	
	@Override
	public void cleanup()
	{
		Z.sim().clear();
		Z.ship().lockControls(false);
	}

	@Override
	public void textEntered(String text)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cancelInput()
	{
		// TODO Auto-generated method stub
		
	}
}
