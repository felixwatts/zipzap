package com.monkeysonnet.zipzap.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.FlickScrollPane;
import com.badlogic.gdx.utils.Array;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IButtonEventHandler;
import com.monkeysonnet.engine.ICallback;
import com.monkeysonnet.engine.IScreen;
import com.monkeysonnet.zipzap.BadgeButton;
import com.monkeysonnet.zipzap.BadgeDetailsActor;
import com.monkeysonnet.zipzap.IConsoleEventHandler;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.achievements.IBadge;

public class BadgesScreen implements IScreen, IConsoleEventHandler, IButtonEventHandler, InputProcessor
{
	private Stage _stage;
	private BadgeDetailsActor _badgeDetails;
	private Array<BadgeButton> _buttons = new Array<BadgeButton>();
	private Group _groupButtons;
	
	private ICallback _onDetailsDismiss = new ICallback()
	{		
		@Override
		public void callback(Object arg)
		{
			for(BadgeButton b : _buttons)
				b.toForeground();
			_groupButtons.touchable = true;
		}
	};

	@Override
	public void show()
	{
		_stage = new Stage(Gdx.graphics.getWidth(),  Gdx.graphics.getHeight(), true);
		
		float buttonSize = _stage.height() / 5f;
		float numCols = Z.achievments.badges().size / 4;
		
		_groupButtons = new Group();
		_groupButtons.x = 0;
		_groupButtons.y = 0;
		_groupButtons.width = (numCols + 2) * buttonSize;
		_groupButtons.height = _stage.height() * (4f/5f);
		
		int n = 0;
		for(IBadge b : Z.achievments.badges())
		{
			BadgeButton a = createButton(b, buttonSize);
			
			a.y = _groupButtons.height - (((n % 4) + 1) * buttonSize);
			a.x = ((n / 4) * buttonSize) + (buttonSize/2f);
			
			_groupButtons.addActor(a);
			
			_buttons.add(a);
			
			n++;
		}
		
		FlickScrollPane p = new FlickScrollPane(_groupButtons);
		p.x = 0;
		p.y = buttonSize/2f;
		p.height = _stage.height();
		p.width = _stage.width();
		
		_stage.addActor(p);
		
		_badgeDetails = new BadgeDetailsActor(_stage, _onDetailsDismiss );
	}
	
	private BadgeButton createButton(IBadge badge, float size)
	{
		BadgeButton b = new BadgeButton(badge, this, size);
		return b;
	}

	@Override
	public void focus()
	{
		Gdx.input.setInputProcessor(this);
		
		for(BadgeButton c : _buttons)
			c.toForeground();
		
		if(Z.prefs.getBoolean("first-show-badges", true))
		{			
			Z.prefs.putBoolean("first-show-badges", false);
			Z.prefs.flush();
			
			doIntro();
		}
	}

	@Override
	public void render()
	{		
		_stage.act(Gdx.graphics.getDeltaTime());
		_stage.draw();
	}

	@Override
	public void blur()
	{
		for(BadgeButton c : _buttons)
			c.toBackground();
	}

	@Override
	public void hide()
	{
		_stage.dispose();
		_stage = null;
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

	private void doIntro()
	{
		_stage.getRoot().touchable = false;		

		Z.console()
			.clearNow()
			.setAvatar(Z.texture("miner"), Z.colorTutorial)
			.setColour(Z.colorTutorial, Z.colorTutorialBg)
			.write("Welcome to the Badge Screen!")
			.touch()
			.clear()
			.write("This is a kind of trophy room, where you display all the badges you have received in the line of duty.")
			.touch()
			.clear()
			.write("You can earn badges for things like finding hidden items or defeating bosses.")
			.touch()
			.clear()
			.write("Some badges unlock special treats! Tap on any badge for more info.");
		Z.console().setHandler(BadgesScreen.this);
		Game.ScreenManager.push(Z.consoleScreen);

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
		Z.consoleScreen.close(onConsoleClosed);		
	}
	
	private final ICallback onConsoleClosed = new ICallback()
	{
		
		@Override
		public void callback(Object arg)
		{
			_stage.getRoot().touchable = true;
		}
	};

	@Override
	public void callback(Object arg)
	{
	}

	@Override
	public boolean onButtonDown(Actor sender)
	{
		if(sender instanceof BadgeButton)
			return true;
		else return false;
	}

	@Override
	public void onButtonUp(Actor sender)
	{
		if(sender instanceof BadgeButton)
		{
			_groupButtons.touchable = false;
			BadgeButton b = (BadgeButton)sender;
			_badgeDetails.setBadge(b.badge());
			for(BadgeButton c : _buttons)
				c.toBackground();
			Z.sfx.play(10);
		}
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
			case Keys.BACK:
			case Keys.ESCAPE:
				return true;
			default:
				return _stage.keyDown(keycode);
		}
	}

	@Override
	public boolean keyUp(int keycode)
	{
		switch(keycode)
		{
			case Keys.BACK:
			case Keys.ESCAPE:
				
				if(_badgeDetails.visible)
					_badgeDetails.hide();
				
				Game.ScreenManager.pop();
				//Game.ScreenManager.push(Z.titleScreen);
				return true;
			default:
				return _stage.keyUp(keycode);
		}
	}

	@Override
	public boolean keyTyped(char character)
	{
		if(_stage != null)
			return _stage.keyTyped(character);
		else return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button)
	{
		if(_stage != null)
			return _stage.touchDown(x, y, pointer, button);
		else return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button)
	{
		if(_stage != null)
			return _stage.touchUp(x, y, pointer, button);
		else return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer)
	{
		if(_stage != null)
			return _stage.touchDragged(x, y, pointer);
		else return false;
	}

	@Override
	public boolean touchMoved(int x, int y)
	{
		if(_stage != null)
			return _stage.touchMoved(x, y);
		else return false;
	}

	@Override
	public boolean scrolled(int amount)
	{
		if(_stage != null)
			return _stage.scrolled(amount);
		else return false;
	}

	@Override
	public void textEntered(String text)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cancelInput()
	{
	}

	@Override
	public void pause()
	{
	}
}
