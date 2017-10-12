package com.monkeysonnet.zipzap.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.monkeysonnet.engine.ButtonActor;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IButtonEventHandler;
import com.monkeysonnet.engine.ICallback;
import com.monkeysonnet.zipzap.Z;

public class PurchaseScreen extends ConsoleScreen implements IButtonEventHandler, InputProcessor
{
	private static final String URI_BUY = "market://details?id=com.monkeysonnet.zipzap.full";// "https://play.google.com/store/apps/details?id=com.monkeysonnet.zipzap";
	private ButtonActor _btnBuy;
	private ICallback _onComplete;
	private boolean _customMessage;
	
	public PurchaseScreen(ICallback onComplete)
	{
		this(onComplete, false);
	}
	
	public PurchaseScreen(ICallback onComplete, boolean customMessage)
	{
		super();
		
		_onComplete = onComplete;
		_customMessage = customMessage;
		
		float unit = _stage.height() / 4f;
		_btnBuy = new ButtonActor((_stage.width()/2f) - unit, unit/2f, 2*unit, unit, false, false, -1, Z.texture("button-buy"), this);
		_btnBuy.color.set(Z.colorUi);
		_stage.addActor(_btnBuy);		
	}
	
	@Override
	public void focus()
	{
		super.focus();
		
		_console.touchable = false;

		if(!_customMessage)
			_console.clearNow()
				.setAvatar(Z.texture("miner"), Z.colorTutorial)
				.setColour(Z.colorTutorial, Z.colorTutorialBg)
				.write("That super-good feature is available in the full version. Tap below to visit Google Play.");
		
		Gdx.input.setInputProcessor(this);
	}

	@Override
	public boolean onButtonDown(Actor sender)
	{
		return sender == _btnBuy;
	}

	@Override
	public void onButtonUp(Actor sender)
	{
		if(sender == _btnBuy && Z.android != null)
		{			
			// TODO change to full - not beta
			Z.android.openUri(URI_BUY);
		}
		
		Game.ScreenManager.pop();
		if(_onComplete != null)
			_onComplete.callback(null);
	}

	@Override
	public void onButtonDragged(Actor sender, Vector2 delta)
	{
	}

	@Override
	public boolean keyDown(int keycode)
	{
		switch(keycode)
		{
			case Keys.ESCAPE:
			case Keys.BACK:
				return true;
			default:
				return false;
		}
	}
	
	@Override
	public boolean keyUp(int keycode)
	{
		switch(keycode)
		{
			case Keys.ESCAPE:
			case Keys.BACK:
				Game.ScreenManager.pop();
				if(_onComplete != null)
					_onComplete.callback(null);
				return true;
			default:
				return false;
		}
	}

	@Override
	public boolean keyTyped(char character)
	{
		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button)
	{
		return _stage.touchDown(x, y, pointer, button);
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button)
	{
		return _stage.touchUp(x, y, pointer, button);
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer)
	{
		return _stage.touchDragged(x, y, pointer);
	}

	@Override
	public boolean touchMoved(int x, int y)
	{
		return _stage.touchMoved(x, y);
	}

	@Override
	public boolean scrolled(int amount)
	{
		return _stage.scrolled(amount);
	}
}
