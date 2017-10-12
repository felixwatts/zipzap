package com.monkeysonnet.lander;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.GestureAndKeyDetector;
import com.monkeysonnet.engine.GestureAndKeyListener;
import com.monkeysonnet.engine.ISimulationEventHandler;
import com.monkeysonnet.zipzap.NotificationQueue;
import com.monkeysonnet.zipzap.Sim;
import com.monkeysonnet.zipzap.TargetArrowActor;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.achievements.Notification;
import com.monkeysonnet.zipzap.screens.PauseScreen;

public class Hud implements ISimulationEventHandler
{
	private GestureAndKeyDetector _gestures;
	private Stage _stage;
	private BarActor _energyBar;//, _fuelBar;
	private TargetArrowActor _arrow;
	private NotificationQueue _notifications;
	private Group _mainGroup;

	public Hud()
	{
		_gestures = new GestureAndKeyDetector(gestureHandler);
		
		_stage = new Stage(
				Gdx.graphics.getWidth(), 
				Gdx.graphics.getHeight(), 
				true);
		
		_mainGroup = new Group();
		_mainGroup.x = 0;
		_mainGroup.y = 0;
		_mainGroup.width = _stage.width();
		_mainGroup.height = _stage.height();
		_stage.addActor(_mainGroup);
		
		//_stage.getRoot().touchable = false;
		
//		_fuelBar = new BarActor(null, "Fuel", _stage.width() * 0.75f, _stage.width() * 0.05f);
//		_fuelBar.x = _stage.width() * 0.125f;
//		_fuelBar.y = _stage.width() * 0.05f; 
//		_fuelBar.color.set(Color.RED);
//		_stage.addActor(_fuelBar);
		
		_energyBar = new BarActor(null, "Health", _stage.width() * 0.75f, _stage.width() * 0.05f);
		_energyBar.x = _stage.width() * 0.125f;
		_energyBar.y = _stage.height() - _energyBar.height - (_stage.width() * 0.05f);
		_energyBar.color.set(new Color(0f, 1f, 204f/255f, 1f));
		_mainGroup.addActor(_energyBar);
		
		_arrow = new TargetArrowActor(_stage, Color.YELLOW);
		
//		_btnFireLeft = new ButtonActor(0, 0, BUTTON_SIZE, BUTTON_SIZE, null, this);
//		_stage.addActor(_btnFireLeft);
		
		_notifications = new NotificationQueue(_stage);
		
		Gdx.input.setInputProcessor(_gestures);
	}
	
	private final GestureAndKeyListener gestureHandler = new GestureAndKeyListener()
	{		
		@Override
		public boolean touchDown(int x, int y, int pointer)
		{
			return false;
		}

		@Override
		public boolean tap(int x, int y, int count)
		{
			Z.renderer.screenToWorld(Vector2.tmp.set(x, y));
			L.sim.guy().tap(Vector2.tmp.x, Vector2.tmp.y);
			return false;
		}

		@Override
		public boolean longPress(int x, int y)
		{
			return false;
		}

		@Override
		public boolean fling(float velocityX, float velocityY)
		{
			L.sim.guy().fling(velocityX, velocityY);
			return true;
		}

		@Override
		public boolean pan(int x, int y, int deltaX, int deltaY)
		{
			L.sim.guy().pan(deltaX, deltaY);
			return true;
		}

		@Override
		public boolean zoom(float originalDistance, float currentDistance)
		{
			return false;
		}

		@Override
		public boolean pinch(Vector2 initialFirstPointer,
				Vector2 initialSecondPointer, Vector2 firstPointer,
				Vector2 secondPointer)
		{
			return false;
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
					Game.ScreenManager.push(new PauseScreen());
					return true;
				default:
					return _stage.keyUp(keycode);
			}
		}

		@Override
		public boolean touchDown(int x, int y, int pointer, int button)
		{
			return false;
		}

		@Override
		public boolean touchUp(int x, int y, int pointer, int button)
		{
			return false;
		}

	};
	
	public void hide(boolean yes)
	{
		_mainGroup.visible = !yes;
		
		if(!yes)
			Gdx.input.setInputProcessor(_gestures);
	}
	
	public void render()
	{
		_stage.act(Gdx.graphics.getDeltaTime());
		_stage.draw();
	}

	@Override
	public void onSimulationEvent(int eventType, Object argument)
	{
		switch(eventType)
		{
			case Sim.EV_START:
				_energyBar.data(L.sim.guy().energyBar);
				//_fuelBar.data(L.sim.guy().fuelBar);
				break;
			case Sim.EV_TARGET_CHANGED:
				_arrow.visible = Z.sim.target() != null;
				break;
			case Sim.EV_ENQUEUE_NOTIFICATION:
				Notification n = (Notification)argument;
				_notifications.enqueue(n.icon, n.worldLoc, n.color);
				break;
			case Sim.EV_DEQUEUE_NOTIFICATION:
				_notifications.dequeue();
				break;
		}
	}
}
