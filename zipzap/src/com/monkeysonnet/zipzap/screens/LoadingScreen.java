package com.monkeysonnet.zipzap.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.monkeysonnet.engine.AtlasTextureSource;
import com.monkeysonnet.engine.ButtonActor;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.ICallback;
import com.monkeysonnet.engine.IScreen;
import com.monkeysonnet.zipzap.Z;

public class LoadingScreen implements IScreen
{
	private int _renderNum = 0;
	private ICallback _onComplete;
	
	public LoadingScreen()
	{
		this(null);
	}
	
	public LoadingScreen(ICallback onComplete)
	{
		_onComplete = onComplete;
	}

	@Override
	public void show()
	{
	}

	@Override
	public void focus()
	{
	}
	
	@Override
	public void pause()
	{
	}
	
	private void renderSplashScreen()
	{
		Stage stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
		
		TextureRegion tex = Z.texture("splash-screen");
		float aspect = ((float)tex.getRegionWidth())/((float)tex.getRegionHeight());	
		
		float height = stage.height();
		float width = stage.height() * aspect;
		float y = (stage.height() - height) / 2f;
		
		stage.addActor(new ButtonActor(0, y, width, height, false, false, tex, null));
		stage.draw();
	}

	@Override
	public void render()
	{
		switch(_renderNum)
		{
			case 0:
				Z.textures = new AtlasTextureSource(new TextureAtlas(Gdx.files.internal("pack")));	
				renderSplashScreen();
				_renderNum++;
				break;
			case 1:
				load();
				Game.ScreenManager.pop();
				
				if(_onComplete != null)
					_onComplete.callback(null);
				else
				{
					Game.ScreenManager.push(new ZipZapScreen());					
					Game.ScreenManager.push(Z.titleScreen);						
					//Game.ScreenManager.push(new PromoUnlockScreen());
					//Game.ScreenManager.push(new BetaKeyScreen());
				}
				_renderNum++;
				break;
		}
	}
	
	private void load()
	{
		Z.load();
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
		return true;
	}

	@Override
	public void serialize(Preferences dict)
	{
	}
	
	@Override
	public void deserialize(Preferences dict)
	{
	}
	
//	private class LoadTask extends AsyncTask<Void, Void, Void> 
//	{
//		@Override
//		protected Void doInBackground(Void... arg0)
//		{			
//			
//			
//			return null;
//		}
//		
//		@Override
//		protected void onPostExecute(Void result)
//		{
//			Game.ScreenManager.pop();
//			Game.ScreenManager.push(new ZipZapScreen());
//			Game.ScreenManager.push(new BetaKeyScreen());
//		}
//	}
}
