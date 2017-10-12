package com.monkeysonnet.zipzap.script;

import com.badlogic.gdx.graphics.Color;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.engine.editor.Shape;
import com.monkeysonnet.zipzap.IGameController;
import com.monkeysonnet.zipzap.Tools;
import com.monkeysonnet.zipzap.Z;
import com.monkeysonnet.zipzap.entities.Wall;

public class EnvironmentController implements IGameController
{
	protected Map _map;
	
	public EnvironmentController(Map map)
	{
		_map = map;
	}
	
	@Override
	public void init()
	{
		Z.sim().clear();
		
		Z.ship().setPosition(0, 0, 90);
		
		Z.sim().focalPoint(Z.ship().origin());

		for(int n = 0; n < _map.numShapes(); n++)
		{
			Shape s = _map.shape(n);
			
			if(s.hasProperty("wall") || s.properties != null && s.properties.startsWith("w."))
			{
				Color c = Color.GRAY;
				
				if(s.hasProperty("colour"))
					c = Tools.stringToColour(s.propertyValue("colour"));
							
				Wall.spawn(_map.shape(n).shape, c);
			}
		}
	}

	@Override
	public void update(float dt)
	{
		// no op
	}

	@Override
	public void cleanup()
	{
		// no op
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
