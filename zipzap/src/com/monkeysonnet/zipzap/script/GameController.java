package com.monkeysonnet.zipzap.script;

import com.badlogic.gdx.utils.Array;
import com.monkeysonnet.zipzap.IGameController;
import com.monkeysonnet.zipzap.SimRenderer;
import com.monkeysonnet.zipzap.Z;

public class GameController implements IGameController
{
	private Array<IScriptEvent> _events = new Array<IScriptEvent>();
	protected float _time;
	private IAutoSpawner _autoSpawner;
	
	public GameController()
	{
		this(null);
	}
	
	public GameController(IAutoSpawner autoSpawner)
	{
		_autoSpawner = autoSpawner; 
	}

	@Override
	public void init()
	{
		_time = 0;
		Z.sim().focalPoint(Z.ship().origin());
		Z.renderer.backgroundColor(SimRenderer.defaultBackgroundColor);
	}

	@Override
	public void update(float dt)
	{
		if(_time >= 0)
			_time += dt;
		
		while(true)
		{
			if(_events.size == 0)
				break;
			else if(_events.peek().time() > _time)
				break;
			else _events.pop().fire().free();
		}
		
		if(_autoSpawner != null)
		{
			_autoSpawner.updateVal(_time);
			_autoSpawner.update(dt);
		}
	}

	@Override
	public void cleanup()
	{
		for(IScriptEvent e : _events)
			e.free();
		_events.clear();
	}
	
	public GameController addEvent(IScriptEvent e)
	{
		_events.insert(0, e);
		return this;
	}

	@Override
	public void onSimulationEvent(int eventType, Object argument)
	{
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
