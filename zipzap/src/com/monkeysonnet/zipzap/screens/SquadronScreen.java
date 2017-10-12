package com.monkeysonnet.zipzap.screens;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.monkeysonnet.engine.ButtonActor;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IButtonEventHandler;
import com.monkeysonnet.engine.ICallback;
import com.monkeysonnet.engine.IScreen;
import com.monkeysonnet.engine.ISimulationEventHandler;
import com.monkeysonnet.engine.WorldButton;
import com.monkeysonnet.zipzap.GlowButton;
import com.monkeysonnet.zipzap.IConsoleEventHandler;
import com.monkeysonnet.zipzap.Sim;
import com.monkeysonnet.zipzap.Squadron;
import com.monkeysonnet.zipzap.WorldLabel;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.entities.Ship;
import com.monkeysonnet.zipzap.entities.Speck;
import com.monkeysonnet.zipzap.entities.Squadey;

public class SquadronScreen implements IScreen, IButtonEventHandler, InputProcessor, ISimulationEventHandler, IConsoleEventHandler
{
	private float LABEL_CHAR_SIZE = 12f * Gdx.graphics.getDensity();
	
	private Stage _stage;
	private ButtonActor _btnShowFriendCode, _btnScanFriendCode, _btnName1, _btnName2;
	private Squadron _squadron = new Squadron();
	private Sim _sim;
	private static final Vector2 focalPoint = new Vector2(0, -10f);
	private static final float CONVOY_SPEED = Ship.SPEED_NORMAL;
	
	@Override
	public void show()
	{
		_stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
		
		_btnShowFriendCode = createButton(Z.texture("button-show-friend-code"), Z.texture("button-show-friend-code-glow"), 0);
		_btnScanFriendCode = createButton(Z.texture("button-scan-friend-code"), Z.texture("button-scan-friend-code-glow"), 1);

		// workaround for HTC Wildfire S with sense UI - broken landscape keyboard.
		_btnName1 = createButton(Z.texture("button-name-1"), Z.texture("button-name-1"), 0);
		_btnName1.visible = _btnName1.touchable = false;
		_btnName2 = createButton(Z.texture("button-name-2"), Z.texture("button-name-2"), 1);	
		_btnName2.visible = _btnName2.touchable = false;		
		
		_stage.addActor(_btnScanFriendCode);
		_stage.addActor(_btnShowFriendCode);
		
		setupSim();			
	}
	
	@Override
	public void pause()
	{
	}
	
	private void doIntro()
	{
		_stage.getRoot().touchable = false;
		
		Z.console().clearNow()
			.setAvatar(Z.texture("miner"), Z.colorTutorial)
			.setColour(Z.colorTutorial, Z.colorTutorialBg)
			.write("Welcome to the Squadron Screen!")
			.touch()
			.clear()
			.write("This is where you add friends to your squadron.")
			.touch()
			.clear()
			.write("Each friend you add gives you an extra life!")
			.touch()
			.clear()
			.write("Add a friend by scanning their friend code with your device's camera.")
			.touch()
			.clear()
			.write("Tap a button below to get started.");
		Z.console().setHandler(this);
		Game.ScreenManager.push(Z.consoleScreen);
		
//		Tween.call(new TweenCallback()
//		{			
//			@Override
//			public void onEvent(int type, BaseTween<?> source)
//			{
//				Z.console().clear()
//				.setAvatar(Z.texture("miner"), Z.colorTutorial)
//				.setColour(Z.colorTutorial, Z.colorTutorialBg)
//				.write("Welcome to the Squadron Screen!")
//				.touch()
//				.clear()
//				.write("This is where you add friends to your squadron.")
//				.touch()
//				.clear()
//				.write("Each friend you add gives you an extra life!")
//				.touch()
//				.clear()
//				.write("Add a friend by scanning their friend code with your device's camera.")
//				.touch()
//				.clear()
//				.write("Tap a button below to get started.");
//			Z.console().setHandler(SquadronScreen.this);
//			Game.ScreenManager.push(Z.consoleScreen);
//			}
//		}).delay(1500).start(Game.TweenManager);
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
	
	private ButtonActor createButton(TextureRegion fg, TextureRegion bg, int position)
	{
		float unit = _stage.width() / 32f;
		
		float buttonWidth = unit * 14;
		TextureRegion tex = fg;
		float aspect = ((float)tex.getRegionHeight()) / ((float)tex.getRegionWidth());
		float buttonHeight = buttonWidth * aspect;
		
		GlowButton btn = new GlowButton(fg, bg, this);
		btn.width = buttonWidth;
		btn.height = buttonHeight;
		btn.x = unit + (position * unit * 16);
		btn.y = unit;
		btn.originX = btn.width / 2f;
		btn.originY = btn.height / 2f;
		
		_stage.addActor(btn);

		return btn;
	}
	
//	private void appearButton(Actor btn, int position)
//	{
//		Game.TweenManager.killTarget(btn.color);
//		Game.TweenManager.killTarget(btn);
//		
//		btn.scaleX = btn.scaleY = 0f;
//		
//		btn.color.set(ColorTools.hslToColor(0.5f, 1, 0.5f, 1f));
//		
//		Timeline.createSequence()
//			.push(Tween.set(btn.color, ColorTweener.VAL_HSLA).target(1f/2f, 1, 0.5f, 1f))
//			.push(Tween.to(btn.color, ColorTweener.VAL_HSLA, 1000).target(2f/3f, 1f, 0.5f, 1f))
//			.pushPause(2000)
//			.repeatYoyo(Tween.INFINITY, 0)
//			.delay(position * 250)
//			.start(Game.TweenManager);	
//	
//		Tween
//			.to(btn, ActorTweener.VAL_SCALE, 1000)
//			.target(1f)
//			.ease(Elastic.OUT)
//			.delay(100 * position)
//			.start(Game.TweenManager);
//	}
	
	private void setupSim()
	{
		if(_sim == null)
			_sim = new Sim(this);
		
		Z.sim = _sim;
		
		_sim.clear();
		
		for(int n = 0; n < 20; n++)
		{
			Speck s = new Speck().init(0f, 1f);
			Z.sim.entities().add(s);
		}
		
		for(int n = 0; n <= _squadron.numInSquad(); n++)
		{
			Squadey s = Squadey.spawn(n);
			_sim.entities().add(s);

			//if(n != 0)
				addLabelForSquadey(n, s);
		}
		
		_sim.focalPoint(focalPoint );
	}

	@Override
	public void focus()
	{
		Gdx.input.setInputProcessor(this);
		Z.renderer().toColour();		
		_btnScanFriendCode.visible = _btnScanFriendCode.touchable = _squadron.numInSquad() < 4;
		TitleScreen.appearButton(_btnScanFriendCode, 0);
		TitleScreen.appearButton(_btnShowFriendCode, 1);
		
		if(Z.prefs.getBoolean("first-show-squadron", true))
		{			
			Z.prefs.putBoolean("first-show-squadron", false);
			Z.prefs.flush();
			
			doIntro();
		}
	}

	@Override
	public void render()
	{
		focalPoint.add(0, Sim.WORLD_STEP_TIME * CONVOY_SPEED);
		
		Z.sim().update(
				Z.renderer().cam().position.x - (Z.renderer().cam().viewportWidth/2f), 
				Z.renderer().cam().position.y - (Z.renderer().cam().viewportHeight/2f), 
				Z.renderer().cam().viewportWidth, 
				Z.renderer().cam().viewportHeight);
	
		Z.renderer().renderBackground();
		Z.renderer().renderForeground(_sim);
        
		_stage.act(Gdx.graphics.getDeltaTime());
		_stage.draw();
	}

	@Override
	public void blur()
	{
		Z.renderer().toBackAndWhite();
	}

	@Override
	public void hide()
	{
		Game.TweenManager.killTarget(_btnScanFriendCode.color);
		Game.TweenManager.killTarget(_btnShowFriendCode.color);
		
		Game.TweenManager.killTarget(_btnScanFriendCode);
		Game.TweenManager.killTarget(_btnShowFriendCode);
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
	public boolean onButtonDown(Actor sender)
	{
		return true;
	}

	@Override
	public void onButtonUp(Actor sender)
	{
		if(Z.android != null)
		{
			if(sender == _btnShowFriendCode)
			{				
				if(Z.username == null)
				{
					Z.console().clearNow()
					.setAvatar(Z.texture("miner"), Z.colorTutorial)
					.setColour(Z.colorTutorial, Z.colorTutorialBg)
					.write("Enter your name, pilot:\n")
					.callback(null)
					.getInput();

					Z.console().setHandler(new IConsoleEventHandler()
					{			
						@Override
						public void textEntered(String text)
						{
							setUsername(text);
							
//							Z.prefs.putString("username", text);
//							Z.prefs.flush();
//							Z.username = text;
//							_btnName1.visible = _btnName1.touchable = false;
//							_btnName2.visible = _btnName2.touchable = false;
//							
//							_btnScanFriendCode.visible = _btnScanFriendCode.touchable = true;
//							_btnShowFriendCode.visible = _btnShowFriendCode.touchable = true;
//							
//							Game.ScreenManager.pop();
//							showFriendCode();
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
							_btnName1.visible = _btnName1.touchable = true;
							_btnName2.visible = _btnName2.touchable = true;
							
							_btnScanFriendCode.visible = _btnScanFriendCode.touchable = false;
							_btnShowFriendCode.visible = _btnShowFriendCode.touchable = false;
						}
						
						@Override
						public void bufferEmpty()
						{
						}

						@Override
						public void cancelInput()
						{
							_btnName1.visible = _btnName1.touchable = false;
							_btnName2.visible = _btnName2.touchable = false;
							
							_btnScanFriendCode.visible = _btnScanFriendCode.touchable = true;
							_btnShowFriendCode.visible = _btnShowFriendCode.touchable = true;
							
							Game.ScreenManager.pop();
						}
					});
					
					Game.ScreenManager.push(Z.consoleScreen);
				}
				else showFriendCode();
			}
			else if(sender == _btnScanFriendCode)
			{
				Z.android.scanText(onScanComplete);
			}
			else if(sender == _btnName1)
			{
				setUsername("Ziggy");
			}
			else if(sender == _btnName2)
			{
				setUsername("Zaphod");
			}
		}
	}
	
	private void setUsername(String name)
	{
		Z.prefs.putString("username", name);
		Z.prefs.flush();
		Z.username = name;
		_btnName1.visible = _btnName1.touchable = false;
		_btnName2.visible = _btnName2.touchable = false;
		
		_btnScanFriendCode.visible = _btnScanFriendCode.touchable = true;
		_btnShowFriendCode.visible = _btnShowFriendCode.touchable = true;
		
		Game.ScreenManager.pop();
		showFriendCode();
	}
	
	private void showFriendCode()
	{
		try
		{
			if(Z.username != null)
			{
				Z.android.shareText(Squadron.makeUri(Z.username));
			}						
		} 
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		} 
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}
	
	private void addLabelForSquadey(int n, Squadey s)
	{
		Actor label = new WorldLabel(
				Z.renderer, 
				_squadron.name(n-1), 
				LABEL_CHAR_SIZE, 
				s.labelPos,
				s.origin().x < 0 ? WorldButton.ALIGN_FAR : WorldButton.ALIGN_NEAR,
				WorldButton.ALIGN_NEAR);
		
		label.color.set(1f, 1f, 116f/255f, 1f);
		_stage.addActor(label);
	}
	
	private final ICallback onScanComplete = new ICallback()
	{
		@Override
		public void callback(Object arg)
		{
			String code = (String)arg;			
			try
			{
				if(_squadron.add(code))
				{
					Squadey s = Squadey.spawn(_squadron.numInSquad());
					_sim.entities().add(s);
					addLabelForSquadey(_squadron.numInSquad(), s);
				}
			} 
			catch (NoSuchAlgorithmException e)
			{
				e.printStackTrace();
			} 
			catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
		}
	};

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
		return _stage.keyTyped(character);
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

	@Override
	public void onSimulationEvent(int eventType, Object argument)
	{
		// TODO Auto-generated method stub
		
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

	@Override
	public void deserialize(Preferences dict)
	{
	}
}
