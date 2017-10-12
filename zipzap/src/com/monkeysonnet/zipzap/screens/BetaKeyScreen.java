package com.monkeysonnet.zipzap.screens;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import com.badlogic.gdx.Preferences;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IScreen;
import com.monkeysonnet.zipzap.IConsoleEventHandler;
import com.monkeysonnet.zipzap.Tools;
import com.monkeysonnet.zipzap.Z;

public class BetaKeyScreen implements IScreen
{
	private static final String _key = "skiEl£v";
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
		if("exit".equals(Z.prefs.getString("beta-key", null)))
		{
			Z.prefs.remove("beta-key");
			Z.prefs.flush();
			Game.ScreenManager.clear();
		}
		else if(testBetaKey())
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
			
			Z.console().write("Your device ID is:\n")
				.write(shortDeviceId())
				.write("\nEnter your beta key:\n")
				.getInput();

			Z.console().setHandler(new IConsoleEventHandler()
			{			
				@Override
				public void textEntered(String text)
				{
					Z.prefs.putString("beta-key", text);
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
					Z.prefs.putString("beta-key", "exit");
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
	
	private boolean testBetaKey()
	{
		String betaKey = Z.prefs.getString("beta-key", null);		
		
		try
		{
			String sha1 = Tools.sha1(_key + shortDeviceId()).substring(0, 5);			
			return betaKey != null && betaKey.equals(sha1);			
		} 
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
			return false;
		} 
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	private String shortDeviceId()
	{
		String deviceId = Z.deviceId;
		if(deviceId.length() > 5)
			deviceId = deviceId.substring(0, 5);
		return deviceId;
	}
}
