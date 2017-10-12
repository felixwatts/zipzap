package com.monkeysonnet.zipzap.screens;

import com.badlogic.gdx.Preferences;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IScreen;
import com.monkeysonnet.zipzap.IConsoleEventHandler;
import com.monkeysonnet.zipzap.Z;

public class PromoUnlockScreen implements IScreen
{
	private static final String _key = "skoFl£$";
	private boolean _firstTry = true;

	@Override
	public void show()
	{
	}
	
	@Override
	public void pause()
	{
	}

	@Override
	public void focus()
	{
		if("exit".equals(Z.prefs.getString("promo-key", null)))
		{
			Z.prefs.remove("promo-key");
			Z.prefs.flush();
			Game.ScreenManager.clear();
		}
		else if(testPromoKey())
		{
			Game.ScreenManager.pop();
			Game.ScreenManager.push(Z.titleScreen);
		}
		else
		{
			Z.console().clearNow()
			.setAvatar(Z.texture("miner"), Z.colorTutorial)
			.setColour(Z.colorTutorial, Z.colorTutorialBg);
			
			if(!_firstTry)
				Z.console().write("Hmm, that doesn't seem right. ");
			_firstTry = false;
			
			Z.console().write("Enter your promo key:\n")
				.getInput();

			Z.console().setHandler(new IConsoleEventHandler()
			{			
				@Override
				public void textEntered(String text)
				{
					Z.prefs.putString("promo-key", text);
					Z.prefs.flush();
					Game.ScreenManager.pop();
				}
				
				@Override
				public void tap(int row)
				{
				}
				
				@Override
				public void dismiss()
				{					
				}
				
				@Override
				public void callback(Object arg)
				{
				}
				
				@Override
				public void bufferEmpty()
				{
				}

				@Override
				public void cancelInput()
				{
					Z.prefs.putString("promo-key", "exit");
					Z.prefs.flush();
					Game.ScreenManager.clear();
				}
			});
			
			Game.ScreenManager.push(Z.consoleScreen);
		}
	}

	@Override
	public void render()
	{
	}

	@Override
	public void blur()
	{
	}

	@Override
	public void hide()
	{
	}

	@Override
	public boolean isFullScreen()
	{
		return false;
	}

	@Override
	public void serialize(Preferences dict)
	{
	}
	
	@Override
	public void deserialize(Preferences dict)
	{
	}
	
	private boolean testPromoKey()
	{
		String betaKey = Z.prefs.getString("promo-key", null);		
		return "ab49f".equals(betaKey);	
	}
}
