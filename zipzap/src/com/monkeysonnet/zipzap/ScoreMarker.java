package com.monkeysonnet.zipzap;

import java.util.Hashtable;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.engine.ActorTweener;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.MutableInteger;
import com.monkeysonnet.engine.WorldButton;

public class ScoreMarker extends WorldButton
{
	private static final MutableInteger _numStringLookupKey = new MutableInteger(0);
	private static final Hashtable<MutableInteger, CharSequence> _numStrings = new Hashtable<MutableInteger, CharSequence>();
	private static final float DIGIT_SIZE = 16 * Gdx.graphics.getDensity();
	private static final float ONE_SIZE = 3f/5f;// DIGIT_SIZE * (3f/5f); // DIGIT_SIZE;// 
	private static final ScoreMarkerPool pool = new ScoreMarkerPool();
	private static final float[] _fadeColour = new float[]{ 1f, 1f, 116f/255f, 0f };
	
	private static final TweenCallback callbackFree = new TweenCallback()
	{		
		@Override
		public void onEvent(int type, BaseTween<?> source)
		{
			if(type == TweenCallback.COMPLETE)
			{
				ScoreMarker m = (ScoreMarker)source.getUserData();
				//Game.TweenManager.killTarget(m);
				Z.hud().stage().removeActor(m);
				pool.free(m);
			}
		}
	};
	
	private CharSequence _str;
	private boolean _isMainScore;
	private float _digitSize;
	
	private static class ScoreMarkerPool extends Pool<ScoreMarker>
	{
		@Override
		protected ScoreMarker newObject()
		{
			return new ScoreMarker();
		}
	}
	
	private ScoreMarker()
	{
		super(0, 0, 0, 0, false, false, -1, null, null, Z.renderer());
	}
	
	private ScoreMarker(Hud hud)
	{
		super(0, 0, 0, 0, false, false, -1, null, null, Z.renderer());
		_isMainScore = true;
				
		color.set(1f, 1f, 116f/255f, 1f);
		x = 0;
		y = hud.stage().height() - (DIGIT_SIZE*2) - (DIGIT_SIZE / 2f);
		hud.stage().addActor(this);
		setScore(0);
	}
	
	public static ScoreMarker spawn(Vector2 loc, int score, Group parent)
	{		
		ScoreMarker m = pool.obtain();
		m._isMainScore = false;
		m._worldLoc.set(loc);
		m._renderer = Z.renderer();
		
		m._str = getString(score);
		
		if(score >= 500)
			m._digitSize = DIGIT_SIZE * 8f;
		else if(score >= 100)
			m._digitSize = DIGIT_SIZE * 4f;
		else
			m._digitSize = DIGIT_SIZE;

		
		m.width = m._digitSize * m._str.length();
		m.height = m._digitSize;
		m.originX = m.width / 2f;
		m.originY = m.height / 2f;
		m.scaleX = m.scaleY = 1f;
		
		if(Z.screen.sim().comboLevel() == 9)
			m.color.set(Color.MAGENTA);
		else if(Z.screen.sim().comboLevel() > 1)
			m.color.set(Color.RED);
		else 
			m.color.set(1f, 1f, 116f/255f, 1f);
		
		Timeline
			.createParallel()
				.push(Tween
						.to(m, ActorTweener.VAL_SCALE, 1000)
						.target(2f))
				.push(Tween
						.to(m, ActorTweener.VAL_COLOR_RGBA, 1000)
						.target(_fadeColour))
				.setUserData(m)
				.setCallbackTriggers(TweenCallback.COMPLETE)
				.setCallback(callbackFree)
				.start(Game.TweenManager);
		
		parent.addActor(m);
		
		return m;
	}
	
	public void setScore(int score)
	{
		_str = Integer.toString(score);
		width = DIGIT_SIZE * _str.length();
		if(_isMainScore)
			width *= 2f;
		x = (stage.width() - width) / 2f;
	}

	private static CharSequence getString(int score)
	{
		_numStringLookupKey.set(score);
		if(!_numStrings.containsKey(_numStringLookupKey))
			_numStrings.put(new MutableInteger(score), Integer.toString(score));
		return _numStrings.get(_numStringLookupKey);
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha)
	{
		if(!_isMainScore)
			updatePosition();
		
		float x = this.x;
		
		float middle = ((_str.length() * _digitSize) / 2f) + this.x;
		
		for(int d = 0; d < _str.length(); d++)
		{
			float w = _str.charAt(d) == '1' ? ONE_SIZE * _digitSize : _digitSize;
			
			if(_isMainScore)
				w*=2f;
			
			float ox = middle - x;
			
			TextureRegion tex = Z.hud().getNumberTexture(_str.charAt(d));
			batch.setColor(color);
			batch.draw(tex, x, this.y, ox, _digitSize/2f, w, _isMainScore ? _digitSize*2 : _digitSize, this.scaleX, this.scaleY, 0);
			
			x += w;
		}
	}
}
