package com.monkeysonnet.zipzap;

import java.util.LinkedList;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.equations.Bounce;
import aurelienribon.tweenengine.equations.Quad;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.monkeysonnet.engine.ActorTweener;
import com.monkeysonnet.engine.ButtonActor;
import com.monkeysonnet.engine.Game;

public class NotificationQueue
{	
	private static final float BUTTON_SIZE = 48f * Gdx.graphics.getDensity();// 64;
	private Stage _stage;
	private final Vector2 _nextLoc = new Vector2();
	private LinkedList<Actor> _buttons = new LinkedList<Actor>();
	
	public NotificationQueue(Stage stage)
	{
		_stage = stage;
		_nextLoc.set(_stage.width() - BUTTON_SIZE, _stage.height() - BUTTON_SIZE).add(-BUTTON_SIZE/4f, -BUTTON_SIZE/4f);
	}
	
	public void enqueue(TextureRegion tex, Vector2 worldLoc, Color color)
	{
		ButtonActor a = new ButtonActor(tex, null);
		Z.renderer.worldToScreen(Vector2.tmp.set(worldLoc));
		
		a.width = a.height = BUTTON_SIZE;
		Vector2.tmp.add(-BUTTON_SIZE/2f, -BUTTON_SIZE/2f);
		a.x = Vector2.tmp.x;
		a.y = Vector2.tmp.y;
		a.color.set(color);
		
		_stage.addActor(a);
		_buttons.addFirst(a);
		
		Tween
			.to(a, ActorTweener.VAL_POS_XY, 500)
			.target(_nextLoc.x, _nextLoc.y)
			.ease(Quad.OUT)
			.start(Game.TweenManager);
		
		_nextLoc.x -= BUTTON_SIZE;
	}
	
	public void dequeue()
	{
		if(_buttons.size() == 0)
			return;
		
		Actor a = _buttons.removeLast();
		
		Game.TweenManager.killTarget(a);
		_stage.removeActor(a);
		
		for(int n = 0; n < _buttons.size(); n++)
		{
			a = _buttons.get(n);
			Game.TweenManager.killTarget(a);
			
			float tx = _stage.width() - ((_buttons.size()-n) * BUTTON_SIZE);
			
			Tween
				.to(a, ActorTweener.VAL_POS_XY, 500)
				.target(tx, _nextLoc.y)
				.ease(Bounce.OUT)
				.start(Game.TweenManager);
		}
		
		_nextLoc.x += BUTTON_SIZE;
	}
}
