package com.monkeysonnet.lander;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.monkeysonnet.engine.ColorTools;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.zipzap.Sim;
import com.monkeysonnet.zipzap.SimRenderer;
import com.monkeysonnet.zipzap.Tools;
import com.monkeysonnet.zipzap.Z;

public class Renderer extends SimRenderer
{
	private static final float ZOOM = 1;
	private OrthographicCamera _cam;	
	private static final Color colorPad = new Color(Color.YELLOW);
	private static final Color colorPowerUp = new Color(0f, 1f, 204f/255f, 1f);
	private final TextureRegion texSolid = Z.texture("solid");	
	
	Renderer()
	{
		float aspect = (float)Gdx.graphics.getHeight() / (float)Gdx.graphics.getWidth();
		_cam = new OrthographicCamera(VIEWPORT_WIDTH * 4f, VIEWPORT_WIDTH * 4f * aspect);
		_cam.zoom = ZOOM;
		_bwbgColor.set(0.2f, 0.2f, 0.2f, 1f);
		_scanlinesColor.set(0, 0, 0, 0.5f);
	}
	
	public void renderMinimap(Sim sim)
	{
		GL10 gl = Gdx.app.getGraphics().getGL10();	

		_cam.position.set(sim.focalPoint().x, sim.focalPoint().y + 30f, 0);
		
		float clipLeft = (_cam.position.x - (_cam.viewportWidth * ZOOM / 2f));
		//float clipRight = (_cam.position.x + (_cam.viewportWidth / 2f)) * ZOOM;
		float clipBottom = (_cam.position.y - (_cam.viewportHeight * ZOOM / 2f));
		//float clipTop = (_cam.position.y + (_cam.viewportHeight / 2f)) * ZOOM;
		
		_cam.update();
        _cam.apply(gl);        
        _sprites.setProjectionMatrix(_cam.combined);
        _sprites.begin();
        
        Array<IEntity> env = sim.environment().get(
        		clipLeft, 
        		clipBottom, 
        		_cam.viewportWidth * ZOOM, 
        		_cam.viewportHeight * ZOOM);
        
        //Gdx.app.log("# env:", ""+env.size);
        
        for(int l = 0; l < 4; l++)
        {
            for(IEntity e : env)
            {
            	if(e.layer() == l)
				{
					renderMinimapEntity(e, true);
				}
            }
        }
        
        for(int l = 0; l < 4; l++)
        {
			for(IEntity e : sim.entities())
			{
				if(e.layer() == l)
				{
					renderMinimapEntity(e, false);
				}
			}
        }
        
		_sprites.end();
	}

	private void renderMinimapEntity(IEntity e, boolean b)
	{
		if(e instanceof Wall)
		{
			Wall w = (Wall)e;
			Color c;
			if((w.colCat() & LanderSim.COL_CAT_PAD) != 0)
				c = ColorTools.combineAlpha(colorPad, 0.5f * (1f-_blackAndWhite));
			else if(e instanceof Switch)
				c = ColorTools.combineAlpha(Color.RED, 0.5f * (1f-_blackAndWhite));
			else 
				c = ColorTools.combineAlpha(colorPowerUp, 0.5f * (1f-_blackAndWhite));
			
			renderMultiPoly(w, true, c);
		}
		else if(e instanceof LaserBeam)
		{
			LaserBeam g = (LaserBeam)e;
			
			if(g.color(0) != null)
			{
				renderPoly(Tools.zeroVector, 0, g.verts(0), ColorTools.combineAlpha(Color.RED, 0.5f * (1f-_blackAndWhite)), 1f, false, true, 0);
			}
		}
		else if(e instanceof Guy)
		{
			Guy g = (Guy)e;
			
			//_sprites.setColor(1, 0, 0, 0.5f);
			
			setColor(ColorTools.combineAlpha(Color.RED, 0.5f * (1f-_blackAndWhite)));			
			_sprites.draw(texSolid, g.origin().x - Guy.WIDTH/2f, g.origin().y - Guy.HEIGHT/2f, 0, 0, Guy.WIDTH, Guy.HEIGHT, 1f, 1f, 0f);
		}
		else if(e instanceof SentryGun)
		{
			SentryGun g = (SentryGun)e;
			
			//_sprites.setColor(1, 0, 0, 0.5f);
			
			setColor(ColorTools.combineAlpha(Color.RED, 0.5f * (1f-_blackAndWhite)));			
			_sprites.draw(texSolid, g.origin().x - Guy.WIDTH/2f, g.origin().y - Guy.WIDTH/2f, 0, 0, Guy.WIDTH, Guy.WIDTH, 1f, 1f, 0f);
		}
		else if(e instanceof Bullet)
		{
			Bullet g = (Bullet)e;
			
			//_sprites.setColor(1, 0, 0, 0.5f);
			
			setColor(ColorTools.combineAlpha(Color.YELLOW, 0.5f * (1f-_blackAndWhite)));			
			_sprites.draw(texSolid, g.origin().x - Bullet.RADIUS, g.origin().y - Bullet.RADIUS, 0, 0, Bullet.RADIUS*2f, Bullet.RADIUS*2f, 1f, 1f, 0f);
		}
//		else if(e instanceof PowerUp)
//		{
//			PowerUp g = (PowerUp)e;
//			
//			//_sprites.setColor(1, 0, 1, 0.5f);
//			setColor(ColorTools.combineAlpha(colorPowerUp, 0.5f * (1f-_blackAndWhite)));		
//			_sprites.draw(texSolid, g.origin().x - PowerUp.SIZE/2f, g.origin().y - PowerUp.SIZE/2f, 0, 0, PowerUp.SIZE, PowerUp.SIZE, 1f, 1f, 0f);
//		}
		else if(e instanceof PowerBox)
		{
			PowerBox g = (PowerBox)e;
			
			//_sprites.setColor(1, 0, 1, 0.5f);
			setColor(ColorTools.combineAlpha(Color.RED, 0.5f * (1f-_blackAndWhite)));		
			_sprites.draw(texSolid, g.origin().x - PowerBox.WIDTH/2f, g.origin().y - PowerBox.HEIGHT/2f, 0, 0, PowerBox.WIDTH, PowerBox.HEIGHT, 1f, 1f, 0f);
		}
		else if(e instanceof Crate)
		{
			Crate c = (Crate)e;
			
			setColor(ColorTools.combineAlpha(colorPowerUp, 0.5f * (1f-_blackAndWhite)));		
			_sprites.draw(texSolid, c.origin().x - Crate.DEFAULT_RADIUS, c.origin().y - Crate.DEFAULT_RADIUS, Crate.DEFAULT_RADIUS, Crate.DEFAULT_RADIUS, 2 * Crate.DEFAULT_RADIUS, 2 * Crate.DEFAULT_RADIUS, 1f, 1f, c.angle());
		}
	}
}
