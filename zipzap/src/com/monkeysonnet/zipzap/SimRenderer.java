package com.monkeysonnet.zipzap;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.equations.Quad;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.monkeysonnet.engine.ColorTools;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.IEntity;
import com.monkeysonnet.engine.IProjection;
import com.monkeysonnet.engine.ISimulationEventHandler;

public class SimRenderer implements ISimulationEventHandler, IProjection
{
	private static final Plane _xyPlane = new Plane(new Vector3(0, 0, 1), 0);
	protected static final float VIEWPORT_WIDTH = 100f;
	//private static final float LINE_WIDTH = 2f;
	
	protected SpriteBatch _sprites;
	private OrthographicCamera _cam;	
	private TextureRegion _texNode, _texEdge, _texBg, _texSolid;	
	protected float _blackAndWhite = 0;
	public static final Color defaultBackgroundColor = new Color(0, 0, 0.2f, 1f);
	protected final Color _bwbgColor =  new Color(0, 0, 0.2f, 1f);// new Color(0.3f, 0.3f, 0.3f, 1f);//36f/255f, 31f/255f, 28f/255f, 1f);
	private final Color _bgColor = new Color(defaultBackgroundColor);
	private final Color _baseBgColor = new Color(defaultBackgroundColor);
	public static final Color blackAndWhiteEntityColor = new Color(0.5f, 0.5f, 0.5f, 0.5f); //145f/255f, 124f/255f, 111f/255f, 0.5f);
	protected final Color _scanlinesColor = new Color().set(ColorTools.darken(blackAndWhiteEntityColor));
	private float _shakeCameraTime;
	private float _shakeCameraIntensity;
	private float _shakeCameraIntensityDelta;
	private float _pixelSize;
	private float _clipLeft;
	private float _clipRight;
	private float _clipBottom;
	private float _clipTop;
	private boolean _renderNodes;
	private final Color colorCache = new Color();
	private final Texture _texScanlines = Z.texture("zipzap-scanlines").getTexture();
	
	public SimRenderer()
	{
		_cam = new OrthographicCamera();
		_cam.viewportWidth = VIEWPORT_WIDTH;
		_cam.viewportHeight = ((float)Gdx.graphics.getHeight() / (float)Gdx.graphics.getWidth()) * VIEWPORT_WIDTH;
		_cam.zoom = 1;
		_sprites = new SpriteBatch();
		_texEdge = Z.texture("zipzap-edge");
		_texNode = Z.texture("zipzap-node");
		_texBg = Z.texture("zipzap-border");
		_texSolid = Z.texture("solid");
		
		_pixelSize = VIEWPORT_WIDTH / (Gdx.graphics.getWidth() * Gdx.graphics.getDensity());
	}
	
	public void shakeCamera(float intensity, float time)
	{
		_shakeCameraTime = time;
		_shakeCameraIntensity = intensity;
		_shakeCameraIntensityDelta = _shakeCameraIntensity / _shakeCameraTime;
	}
	
	public void zoom(float z)
	{
		_cam.zoom = z;
	}
	
	public Color backgroundColor()
	{
		return _baseBgColor;
	}
	
	public void backgroundColor(Color c)
	{
		_baseBgColor.set(c);
		_bgColor.set(c);
	}
	
	public float getBlackAndWhite()
	{
		return _blackAndWhite;
	}
	
	public void flashBackground(Color c)
	{
		Game.TweenManager.killTarget(_bgColor);
		
		if(c == null)
		{
			switch(Game.Dice.nextInt(6))
			{
				case 0:
				default:
					_bgColor.set(Color.RED);
					break;
				case 1:
					_bgColor.set(Color.YELLOW);
					break;
				case 2:
					_bgColor.set(Color.GREEN);
					break;
				case 3:
					_bgColor.set(Color.CYAN);
					break;
				case 4:
					_bgColor.set(Color.BLUE);
					break;
				case 5:
					_bgColor.set(Color.MAGENTA);
					break;		
			}
		}
		else _bgColor.set(c);
		
		Tween
			.to(_bgColor, 0, 100f)
			.target(_baseBgColor.r, _baseBgColor.g, _baseBgColor.b, _baseBgColor.a)
			.ease(Quad.IN)
			.start(Game.TweenManager);
	}
	
	public void setBlackAndWhite(float f)
	{
		_blackAndWhite = f;
	}
	
	protected Color setColor(Color c)
	{
		if(c == null)
			return null;
		
		if(_blackAndWhite == 0)
			_sprites.setColor(c);
		else
		{
			float a = c.a;
			c = ColorTools.blend(c, blackAndWhiteEntityColor, _blackAndWhite);
			c.a = Math.min(c.a, a);
			_sprites.setColor(c);
		}
		
		return c;
	}
	
	private void renderBorder()
	{
		_sprites.setColor(ColorTools.darken(ColorTools.blend(_bgColor, _bwbgColor, _blackAndWhite)));
		
//		if(_blackAndWhite == 1)
//			_sprites.setColor(ColorTools.darken(_bwbgColor));
//		else 
//			_sprites.setColor(ColorTools.darken(_bgColor));
        
        _sprites.draw(
        		_texBg, 
        		_cam.position.x - (_cam.viewportWidth/2f), 
        		_cam.position.y - (_cam.viewportWidth/2f), 
        		0, 
        		0, 
        		_cam.viewportWidth, 
        		_cam.viewportWidth, 
        		1f, 
        		1f, 
        		0);
	}
	
	private void renderScanlines()
	{
		_sprites.setColor(ColorTools.combineAlpha(_scanlinesColor, _blackAndWhite));
		_sprites.draw(
				_texScanlines, 
				_cam.position.x - (_cam.viewportWidth/2f), 
				_cam.position.y - (_cam.viewportHeight/2f), 
				_cam.viewportWidth, 
				_cam.viewportHeight, 
				0, 
				0, 
				_texScanlines.getWidth(), 
				(int)(((float)Gdx.graphics.getHeight())/2f),
				false,
				false);
	}
	
	//private final Color colorCache = new Color();
	
	private void renderSprite(IRenderableSprite s)
	{
		colorCache .set(s.sprite().getColor());
		s.sprite().setColor(setColor(s.sprite().getColor()));
		s.sprite().draw(_sprites);//, setColor(Color.WHITE).a);
		s.sprite().setColor(colorCache);
	}
	
	public OrthographicCamera cam()
	{
		return _cam;
	}
	
	public void renderBackground()
	{
		GL10 gl = Gdx.app.getGraphics().getGL10();		
		
//		if(Gdx.graphics.getFramesPerSecond() < 55)
//			gl.glClearColor(0.5f, 0, 0, 1f);
//		else 
//		{
			Color c = ColorTools.blend(_bgColor, _bwbgColor, _blackAndWhite);
			gl.glClearColor(c.r, c.g, c.b, 1f);
			
//			if(_blackAndWhite == 1f)
//				gl.glClearColor(_bwbgColor.r, _bwbgColor.g, _bwbgColor.b, 1f);
//			else gl.glClearColor(_bgColor.r, _bgColor.g, _bgColor.b, 1f);
				
//		}
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);		
	}
	
	public void renderForeground(Sim sim)
	{
		GL10 gl = Gdx.app.getGraphics().getGL10();	

		_cam.position.set(sim.focalPoint().x, sim.focalPoint().y, 0);
		
		_clipLeft = _cam.position.x - (_cam.viewportWidth / 2f);
		_clipRight = _cam.position.x + (_cam.viewportWidth / 2f);
		_clipBottom = _cam.position.y - (_cam.viewportHeight / 2f);
		_clipTop = _cam.position.y + (_cam.viewportHeight / 2f);
		
		_renderNodes = true;// Gdx.graphics.getFramesPerSecond() > 55f;
		
		if(_shakeCameraTime > 0)
		{
			_shakeCameraTime -= Gdx.graphics.getDeltaTime();
			_shakeCameraIntensity -= Gdx.graphics.getDeltaTime() * _shakeCameraIntensityDelta;
			if(_shakeCameraIntensity < 0)
				_shakeCameraIntensity = 0;
			
			_cam.position.add(
					(float)(Game.Dice.nextGaussian() * _shakeCameraIntensity), 
					(float)(Game.Dice.nextGaussian() * _shakeCameraIntensity), 
					0);
		}
		
		_cam.update();
        _cam.apply(gl);        
        _sprites.setProjectionMatrix(_cam.combined);
        _sprites.begin();
        
        Array<IEntity> env = sim.environment().get(
        		_cam.position.x - (_cam.viewportWidth/2f), 
        		_cam.position.y  - (_cam.viewportHeight/2f), 
        		_cam.viewportWidth, 
        		_cam.viewportHeight);
        
        //Gdx.app.log("# env:", ""+env.size);
        

        for(int l = 0; l < 4; l++)
        {
        	Array<IEntity> ents = sim.entities();
        	for(int n = 0; n < ents.size; n++)
        	{
        		IEntity e = ents.get(n);
				if(e.layer() == l)
				{
					renderEntity(e, false);
				}
        	}
        	
        	for(int n = 0; n < env.size; n++)
        	{
        		IEntity e = env.get(n);
				if(e.layer() == l)
				{
					renderEntity(e, true);
				}
        	}
        }
        
		renderBorder();
		
		if(_blackAndWhite > 0)
			renderScanlines();

		_sprites.end();
	}
	
	private void renderEntity(IEntity e, boolean isWorld)
	{
		if(e instanceof IRenderablePolygon)
		{
			renderPoly((IRenderablePolygon)e, isWorld);
		}
		else if(e instanceof IRenderableMultiPolygon)
		{
			renderMultiPoly((IRenderableMultiPolygon)e, isWorld);
		}
		else if(e instanceof IRenderableTexture)
		{
			renderTex((IRenderableTexture)e);
		}
		else if(e instanceof IRenderableChain)
		{
			renderChain((IRenderableChain)e, isWorld);
		}
		else if(e instanceof IRenderableSprite)
		{
			renderSprite((IRenderableSprite)e);
		}
	}
	
	protected void renderMultiPoly(IRenderableMultiPolygon e, boolean isWorld)
	{
		for(int n = 0; n < e.getNumPolys(); n++)
		{
			Color c = e.color(n);
			if(c != null)
				renderPoly(e.origin(n), e.angle(n), e.verts(n), c, e.lineWidth(n), e.isLoop(n), isWorld, e.clipRadius());
		}
	}
	
	protected void renderMultiPoly(IRenderableMultiPolygon e, boolean isWorld, Color c)
	{
		if(c != null)
			for(int n = 0; n < e.getNumPolys(); n++)
			{
				renderPoly(e.origin(n), e.angle(n), e.verts(n), c, e.lineWidth(n), e.isLoop(n), isWorld, e.clipRadius());
			}
	}
	
	public void screenToWorld(Vector2 v)
	{
		Ray ray = _cam.getPickRay(v.x, v.y);
		Intersector.intersectRayPlane(ray, _xyPlane, Game.workingVector3);
		v.set(Game.workingVector3.x, Game.workingVector3.y);
	}
	
	private void renderTex(IRenderableTexture t)
	{
		setColor(t.color());
		//_sprites.setColor(t.color());
		_sprites.draw(
				t.texture(), // Textures.solid, 
				t.origin().x - t.radius(), 
				t.origin().y - t.radius(), 
				t.radius(), 
				t.radius(), 
				t.radius() * 2, 
				t.radius() * 2, 
				1f, 
				1f, 
				t.angle());
	}
	
	protected void renderPoly(IRenderablePolygon p, boolean isWorld)
	{
		renderPoly(p, isWorld, null);
	}
	
	protected void renderPoly(IRenderablePolygon p, boolean isWorld, Color forceColor)
	{
		Color c = (forceColor == null ? p.color() : forceColor);
		if(c != null)
			renderPoly(p.origin(), p.angle(), p.verts(), c, p.lineWidth(), true, isWorld, p.clipRadius());
	}
	
	private void renderChain(IRenderableChain p, boolean isWorld)
	{
		Color c = p.color();
		if(c != null)
			renderPoly(p.origin(), p.angle(), p.verts(), c, p.lineWidth(), false, isWorld, p.clipRadius());
	}
	
	private boolean isInView(Vector2 origin, float clipRadius)
	{
		if(origin.x + clipRadius < _clipLeft || origin.x - clipRadius > _clipRight || origin.y + clipRadius < _clipBottom || origin.y - clipRadius > _clipTop)
			return false;
		else return true;
	}
	
	protected void renderPoly(Vector2 origin, float angle, Vector2[] verts, Color c, float lineWidth, boolean loop, boolean isWorld, float clipRadius)
	{		
		if(c == null)
			return;
		
		if(!isWorld && !isInView(origin, clipRadius))
			return;
		
		setColor(c);
		
		if(verts == null || verts.length == 1)
		{
			if(verts != null)
			{
				Z.v1().set(verts[0]);
				if(!isWorld)
					toWorld(Z.v1(), origin, angle);
			}
			else Z.v1().set(origin);

			if(lineWidth <= 0)
			{
				//float w = (_pixelSize / 2f) * (1 + -lineWidth);
				
				//float w = - lineWidth;
				//w/=2f;
				
				float w = (_pixelSize/2f) - (lineWidth/2f);
				
				_sprites.draw(
						_texSolid, // Textures.solid, 
						Z.v1().x - w, 
						Z.v1().y - w, 
						w, 
						w, 
						w*2, 
						w*2, 
						1f, 
						1f, 
						0);
			}
			else
			{
				_sprites.draw(
						_texNode, // Textures.solid, 
						Z.v1().x - lineWidth, 
						Z.v1().y - lineWidth, 
						lineWidth, 
						lineWidth, 
						lineWidth * 2, 
						lineWidth * 2, 
						1f, 
						1f, 
						0);
			}			
		}
		
		if(verts == null || verts.length < 2)
			return;
		
		if(verts.length == 2)
			loop = false;
		
		Vector2 prev = Game.workingVector2a;
		Vector2 diff = Game.workingVector2b;
		
		int numVerts = verts.length;
		while(numVerts > 0 && verts[numVerts-1] == null)
			numVerts--;

		for(int n = 1; n < numVerts+1; n++) // (loop ? numVerts+1 : numVerts)
		{
			prev.set(verts[n-1]);
			if(!isWorld)
				toWorld(prev, origin, angle);
			int vCurr = n % numVerts;
			
			diff.set(verts[vCurr]);
			if(!isWorld)
				toWorld(diff, origin, angle);
			diff.sub(prev);
					
			if(_renderNodes)
				_sprites.draw(
						_texNode, 
						prev.x - (lineWidth / 2f), 
						prev.y - (lineWidth / 2f), 
						lineWidth / 2f, 
						lineWidth / 2f, 
						lineWidth, 
						lineWidth, 
						1f, 
						1f, 
						0);
			
			if(n < numVerts || loop)
				_sprites.draw(
						_texEdge, 
						prev.x, 
						prev.y - (lineWidth / 2f), 
						0, 
						lineWidth / 2f, 
						diff.len(), 
						lineWidth, 
						1f, 
						1f, 
						diff.angle());
		}
	}
	
	private Vector2 toWorld(Vector2 v, Vector2 origin, float angle)
	{
		if(angle == 0f)
			return v.add(origin);
		else if(angle == 180f)		
			return v.set(-v.x, -v.y).add(origin);
		else if(angle == 90f)
			return v.set(-v.y, v.x).add(origin);
		else if(angle == 270f)
			return v.set(v.y, -v.x).add(origin);
		else 			
			return v.rotate(angle).add(origin);
	}
	
	public void dispose()
	{
		Game.TweenManager.killTarget(this);
		_sprites.dispose();
	}
	
	public void toBackAndWhite()
	{
		//_blackAndWhite = 1;
		
		Game.TweenManager.killTarget(this);
		Tween
			.to(this, 0, 500)
			.target(1f)
			.start(Game.TweenManager);
	}
	
	public void toColour()
	{
//		Game.TweenManager.killTarget(this);
		//_blackAndWhite = 0;
		
		Game.TweenManager.killTarget(this);
		Tween
			.to(this, 0, 250)
			.target(0)
			.start(Game.TweenManager);
	}

	@Override
	public void onSimulationEvent(int eventType, Object argument)
	{
		switch(eventType)
		{
			case Sim.EV_FLASH:
				flashBackground((Color)argument);
				break;
			case Sim.EV_RUMBLE:
				shakeCamera((Float)argument, (Float)argument);
				break;
		}
	}

	@Override
	public void worldToScreen(Vector2 worldPoint)
	{
		Game.workingVector3.set(worldPoint.x, worldPoint.y, 0);
		_cam.project(Game.workingVector3);
		worldPoint.set(Game.workingVector3.x, Game.workingVector3.y);
	}
}
