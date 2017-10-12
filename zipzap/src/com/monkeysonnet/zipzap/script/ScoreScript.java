package com.monkeysonnet.zipzap.script;

import com.badlogic.gdx.utils.Array;
import com.monkeysonnet.zipzap.IGameController;
import com.monkeysonnet.zipzap.SimRenderer;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.ZipZapSim;

public class ScoreScript implements IGameController
{
	private Array<IScriptEvent> _events = new Array<IScriptEvent>();
	private float _maxScore;
	private IAutoSpawner _autoSpawner;
	private float _startScore;
	private boolean _scoreChanged;
	
	public ScoreScript(float startScore, IAutoSpawner autoSpawner)
	{
		this(startScore, startScore + 1000, autoSpawner);
	}
	
	public ScoreScript(float startScore, float maxScore, IAutoSpawner autoSpawner)
	{
		_startScore = startScore;
		_maxScore = maxScore;
		_autoSpawner = autoSpawner;
	}

	@Override
	public void init()
	{
		Z.screen.sim().setScore((int)_startScore);
		Z.sim().focalPoint(Z.ship().origin());
		Z.renderer.backgroundColor(SimRenderer.defaultBackgroundColor);
		_scoreChanged = true;
	}

	@Override
	public void update(float dt)
	{
		if(_autoSpawner != null)
			_autoSpawner.update(dt);
		
		if(_scoreChanged)
		{
			float pct = ((Z.screen.sim().score() - _startScore) / (_maxScore - _startScore)) * 100f;
			
			while(true)
			{
				if(_events.size == 0)
					break;
				else if(_events.peek().time() > pct)
					break;
				else _events.pop().fire().free();
			}
			
			if(_autoSpawner != null)
				_autoSpawner.updateVal(pct);
			
			if(Z.screen.sim().score() >= _maxScore)
				Z.screen.sim().advanceScript();
			
			_scoreChanged = false;
		}
	}

	@Override
	public void cleanup()
	{
		for(IScriptEvent e : _events)
			e.free();
		_events.clear();
		
		Z.screen.sim().clearEnemies();
	}
	
	public ScoreScript addEvent(IScriptEvent e)
	{
		_events.insert(0, e);
		return this;
	}
	
	@Override
	public void onSimulationEvent(int eventType, Object argument)
	{
		switch(eventType)
		{
			case ZipZapSim.EV_SCORE:		
				
				_scoreChanged = true;
				
				break;
		}
	}

	@Override
	public void pause()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume()
	{
		// TODO Auto-generated method stub
		
	}
}
