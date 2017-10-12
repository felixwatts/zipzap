package com.monkeysonnet.zipzap;

import java.util.Hashtable;
import java.util.LinkedList;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Bounce;
import aurelienribon.tweenengine.equations.Quad;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Pool;
import com.monkeysonnet.engine.ActorTweener;
import com.monkeysonnet.engine.ButtonActor;
import com.monkeysonnet.engine.ColorTools;
import com.monkeysonnet.engine.Game;

public class Console extends Actor implements TweenCallback
{
	private static final float WRITE_DELAY = 40; // 60;
	
	private static final TextureRegion nullAvatar = new TextureRegion();
	
	private int _cursorX;
	private TextureRegion[][] _content;
	private int _lineSize;
	private float _charSize;
	private LinkedList<BufferItem> _buffer;
	private BufferItemPool _bufferItemPool;
	private boolean _busy;
	private IConsoleEventHandler _handler;
	private Hashtable<Character, TextureRegion> _charTextures;
	private boolean _skip;
	private Color[] _lineColours, _backgroundColours;
	private float _writeDelay;
	private boolean _awaitingTouch;
	private final StringBuilder _textEntry = new StringBuilder();

	private Timeline _blinkCursorTimeline;
	private Tween _processBufferTween;

	private TextureRegion _texShadow = Z.texture("zipzap-letter-bg");
	
	private ButtonActor _avatar1, _avatar2, _nextAvatar;

	private float _lineSpacing;

	private boolean _alignBottom;

	private int _cursorLine;

	private final Color _screenColor = new Color();

	private boolean _inputMode;
	
	private class BufferItem
	{
		char _char;
		CharSequence _str;
		float _pauseTime;
		public Color _col, _bgCol;
		public Object _callbackArg;
		public TextureRegion _avatar;
		
		public BufferItem set(Object arg)
		{
			_char = '\t';	
			_col = null;
			_bgCol = null;
			_str = null;
			_callbackArg = arg;
			_avatar = null;
			return this;
		}
		
		public BufferItem set(char c)
		{
			_char = c;	
			_col = null;
			_str = null;
			_callbackArg = null;
			_avatar = null;
			return this;
		}
		
		public BufferItem set(float pauseTime)
		{
			_char = '\t'; // means none
			_str = null;
			_col = null;
			_callbackArg = null;
			_pauseTime = pauseTime;
			_avatar = null;
			return this;
		}
		
		public BufferItem set(Color fg, Color bg)
		{
			_char = '\t'; // means none
			_str = null;
			_col = fg;
			_bgCol = bg;
			_callbackArg = null;
			_avatar = null;
			return this;
		}
		
		public BufferItem set(CharSequence s)
		{
			_char = '\t'; // means none
			_str = s;
			_col = null;
			_callbackArg = null;
			_avatar = null;
			return this;
		}
		
		public BufferItem set(TextureRegion avatar, Color c)
		{
			_char = '\t'; // means none
			_str = null;
			_col = c;
			_callbackArg = null;
			_avatar = avatar;
			return this;
		}
	}
	
	public void setHandler(IConsoleEventHandler h)
	{
		_handler = h;
	}
	
	private class BufferItemPool extends Pool<BufferItem>
	{
		@Override
		protected BufferItem newObject()
		{
			return new BufferItem();
		}
	}
	
	@Override
	public Actor hit(float x, float y)
	{
		return this;
//		Actor result = x > 0 && x < width && y > 0 && y < height ? this : null;
//		return result;
	}

	@Override
	public boolean touchDown(float x, float y, int cursor)
	{		
		_skip = true;
		
		if(_inputMode)
		{
			getStage().setKeyboardFocus(this);
			Gdx.input.setOnscreenKeyboardVisible(true);
		}
		return true;
	}

	@Override
	public void touchUp(float x, float y, int pointer)
	{
		_skip = false;
		
		if(_awaitingTouch)
		{
			_awaitingTouch = false;
			processBuffer();
		}		
		else if(_handler != null && _buffer.isEmpty())
		{
			_handler.tap(toRow(y));
			//if(_buffer.isEmpty())
			_handler.dismiss();
		}
	}
	
	public int toRow(float y)
	{
		return (int)(y / (_charSize + _lineSpacing));
	}
	
	public Console(int lineSize, float x, float y, float w, float h, Stage stage)
	{
		this(lineSize, x, y, w, h, 1f, stage, true, Color.CLEAR);
	}
	
	public Console(int lineSize, float x, float y, float w, float h, Stage stage, Color screenColor)
	{
		this(lineSize, x, y, w, h, 1f, stage, true, screenColor);
	}
	
	public Console(int lineSize, float x, float y, float w, float h, float speed, Stage stage, boolean alignBottom, Color screenColor)
	{
		_alignBottom = alignBottom;
		
		color.set(Z.colorTutorial);
		_screenColor.set(screenColor);
		
		_writeDelay = WRITE_DELAY / speed;
		
		_charTextures = new Hashtable<Character, TextureRegion>();
		CharSequence letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		for(int n = 0; n < letters.length(); n++)
		{
			// bc lame windows + dropbox
			if(Character.isUpperCase(letters.charAt(n)))
				_charTextures.put(letters.charAt(n), Z.texture("zipzap-letter-" + letters.charAt(n) + "1"));
			else
				_charTextures.put(letters.charAt(n), Z.texture("zipzap-letter-" + letters.charAt(n)));
		}
		
		_charTextures.put('!', Z.texture("zipzap-letter-exclam"));
		_charTextures.put('.', Z.texture("zipzap-letter-dot"));
		_charTextures.put('?', Z.texture("zipzap-letter-question"));
		_charTextures.put('*', Z.texture("zipzap-letter-star"));
		_charTextures.put('%', Z.texture("zipzap-letter-pct"));
		_charTextures.put('>', Z.texture("zipzap-letter-gt"));
		_charTextures.put('<', Z.texture("zipzap-letter-lt"));
		_charTextures.put('(', Z.texture("zipzap-letter-open"));
		_charTextures.put(')', Z.texture("zipzap-letter-close"));
		_charTextures.put('=', Z.texture("zipzap-letter-eq"));
		_charTextures.put('-', Z.texture("zipzap-letter-minus"));
		_charTextures.put('\'', Z.texture("zipzap-letter-apos"));
		_charTextures.put(',', Z.texture("zipzap-letter-comma"));
		_charTextures.put(':', Z.texture("zipzap-letter-colon"));
		
		this.x = x;
		this.y = y;
		this.width = w;
		this.height = h;
		_lineSize = lineSize;
		_charSize = width / lineSize;
		_lineSpacing = 0;// _charSize / 8f;
		
		_buffer = new LinkedList<Console.BufferItem>();
		_bufferItemPool = new BufferItemPool();
		
		int numLines = (int)(this.height / _charSize);
		
		_content = new TextureRegion[numLines][];
		_lineColours = new Color[numLines];
		_backgroundColours = new Color[numLines];
		for(int l = 0; l < numLines; l++)
		{
			_content[l] = new TextureRegion[lineSize];
			_lineColours[l] = Z.colorTutorial;
			_backgroundColours[l] = Z.colorTutorialBg;
		}
		
		if(!_alignBottom)
			_cursorLine = numLines-1;
		
		if(stage != null)
		{
			_avatar1 = new ButtonActor(null, null);
			_avatar2 = new ButtonActor(null,  null);
			stage.addActor(_avatar1);
			stage.addActor(_avatar2);		
			_nextAvatar = _avatar1;
		}
		
		clearNow();
		setColour(Z.colorTutorial, Z.colorTutorialBg);
		
		_blinkCursorTimeline = Timeline
			.createSequence()
			.push(Tween
					.set(this, ActorTweener.VAL_COLOR_RGBA)
					.target(0, 1, 0, 0)
					.delay(500))
			.push(Tween
					.set(this, ActorTweener.VAL_COLOR_RGBA)
					.target(0, 1, 0, 1)
					.delay(500))
			.repeat(Tween.INFINITY, 0)
			.start(Game.TweenManager);		
		
		if(stage != null)
			stage.addActor(this);
	}

	public void dispose()
	{
		if(_blinkCursorTimeline != null)
		{
			_blinkCursorTimeline.kill();
			_blinkCursorTimeline = null;
		}
		
		if(_processBufferTween != null)
		{
			_processBufferTween.kill();
			_processBufferTween = null;
		}
		
		_handler = null;
	}
	
	public Console callback(Object arg)
	{
		_buffer.addLast(_bufferItemPool.obtain().set(arg));

		if(!_busy)
			processBuffer();
		
		return this;
	}
	
	public Console setColour(Color fg, Color bg)
	{
		_buffer.addLast(_bufferItemPool.obtain().set(fg, bg));

		if(!_busy)
			processBuffer();
		
		return this;
	}
	
	public Console setAvatar(TextureRegion avatar, Color c)
	{
		setAvatar(avatar, c, true);
		return this;
	}
	
	public Console clearAvatar()
	{
		setAvatar(nullAvatar, Color.CLEAR);
		return this;
	}
	
	public Console setAvatar(TextureRegion avatar, Color c, boolean insertTouch)
	{
		if(!_buffer.isEmpty())
			touch();
		clear();
		setColour(c, Color.CLEAR);
		_buffer.addLast(_bufferItemPool.obtain().set(avatar, c));

		if(!_busy)
			processBuffer();
		
		return this;
	}
	
	public Console write(CharSequence text)
	{
		for(int n = 0; n < text.length(); n++)
		{
			_buffer.addLast(_bufferItemPool.obtain().set(text.charAt(n)));
		}
		
		if(!_busy)
			processBuffer();
		
		return this;
	}
	
	public Console getInput()
	{
		_buffer.addLast(_bufferItemPool.obtain().set('\r'));

		if(!_busy)
			processBuffer();
		
		return this;
	}
	
	public Console clear()
	{
		_buffer.addLast(_bufferItemPool.obtain().set('\b'));
		
		if(!_busy)
			processBuffer();
		
		return this;
	}
	
	public Console print(CharSequence text)
	{
		_buffer.addLast(_bufferItemPool.obtain().set(text));
		
		if(!_busy)
			processBuffer();
		
		return this;
	}
	
	public Console pause(float time)
	{
		_buffer.addLast(_bufferItemPool.obtain().set(time));
		if(!_busy)
			processBuffer();
		
		return this;
	}
	
	public Console touch()
	{
		_buffer.addLast(_bufferItemPool.obtain().set(-1f));
		
		if(!_busy)
			processBuffer();
		
		return this;
	}
	
	public Console clearNow()
	{
		if(_processBufferTween != null)
		{
			_processBufferTween.kill();
			_processBufferTween = null;
		}
		
		if(_avatar1 != null)
		{
			Game.TweenManager.killTarget(_avatar1);
			Game.TweenManager.killTarget(_avatar2);
			_avatar1.visible = false;
			_avatar1.setTexture(null);
			_avatar2.visible = false;
			_avatar2.setTexture(null);
		}
		
		_buffer.clear();
		
		clearScreen();
		
		_busy = false;
		
		return this;
	}
	
	private void clearScreen()
	{
		for(int l = 0; l < _content.length; l++)
		{
			for(int c = 0; c < _lineSize; c++)
				_content[l][c] = null;// ' ';
		}
		if(!_alignBottom)
			_cursorLine = _content.length-1;
		
		_content[cursorLine()][0] = getCharTexture('>');
		_cursorX = 1;	
	}
	
	public void flushBuffer()
	{
		while(!_buffer.isEmpty())
			processBuffer(true);
	}
	
	private void processBuffer()
	{
		processBuffer(false);
	}
	
	private void processBuffer(boolean flushMode)
	{
		if(!_buffer.isEmpty())
		{
			_busy = true;
			
			BufferItem item = _buffer.removeFirst();
			
			if(item._avatar != null)
			{				
				setAvatarNow(item._avatar, item._col);
				scheduleProcessBuffer(_skip ? _writeDelay / 8f : _writeDelay);
			}
			else if(item._callbackArg != null)
			{
				if(_handler != null)
					_handler.callback(item._callbackArg);
				scheduleProcessBuffer(_skip ? _writeDelay / 8f : _writeDelay);
			}
			else if(item._col != null)
			{
				setColorNow(item._col, item._bgCol);
				
				if(!flushMode)
					scheduleProcessBuffer(_skip ? _writeDelay / 8f : _writeDelay);
			}
			else if(item._str != null)
			{
				for(int n = 0; n < item._str.length(); n++)
					printCharacter(item._str.charAt(n));
				
				if(!flushMode)
				{
					scheduleProcessBuffer(_skip ? _writeDelay / 2f : _writeDelay * 4f);
					Z.sfx.play(12);
				}
			}
			else if(item._char == '\t')
			{
				if(!flushMode)
				{
					if(item._pauseTime < 0)
						_awaitingTouch = true;
					else scheduleProcessBuffer(_skip ? item._pauseTime / 8f : item._pauseTime);
				}
			}
			else if(item._char == '\b')
			{
				clearScreen();
				
				if(!flushMode)
					scheduleProcessBuffer(_skip ? _writeDelay / 8f : _writeDelay);
			}
			else if(item._char == '\r')
			{
				inputMode(true);
			}
//			else if(item._char == '\f')
//			{
//				inputMode(false);
//			}
			else
			{
				printCharacter(item._char);
				
				if(!flushMode)
				{
					scheduleProcessBuffer(_skip ? _writeDelay / 8f : _writeDelay);
					Z.sfx.play(12);
				}
			}
			
			_bufferItemPool.free(item);
		}
	}
	
	private void setColorNow(Color col, Color bgCol)
	{
		_lineColours[cursorLine()] = col;
		_backgroundColours[cursorLine()] = bgCol;
		this.color.set(col);
	}

	private void scheduleProcessBuffer(float delay)
	{
		if(_processBufferTween != null)
		{
			_processBufferTween.kill();
			_processBufferTween = null;
		}
		
		_processBufferTween = Tween.call(this).delay(delay).start(Game.TweenManager);
	}
	
	private int cursorLine()
	{
		if(_alignBottom)
			return 0;
		else return _cursorLine;
	}
	
	private void printCharacter(char c)
	{
		if(c == '\n')
		{
			newline();
		}
		else if(c != '\t')
		{
			_content[cursorLine()][_cursorX] = getCharTexture(c);
			_cursorX++;
			
			if(c == ' ')
			{
				int s = _cursorX;
				int i = -1;
				while(true)
				{
					i++;
					
					if(i >= _buffer.size())
						break;
					BufferItem x = _buffer.get(i);
					if(x._char == ' ' || x._char == '\n')
						break;
					else if(x._char == '\t')
						continue;
					else
					{							
						s++;
						if(s >= _lineSize)
							break;
					}
				}
				
				if(s >= _lineSize)
					newline();
			}
			else if(_cursorX >= _lineSize)
				newline();
		}
	}
	
	private void newline()
	{		
		if(_alignBottom)
		{
			shiftContentUp();
		}
		else
		{
			_cursorLine--;
			if(_cursorLine < 0)
			{
				_cursorLine = 0;
				shiftContentUp();
			}
			else
			{
				_content[_cursorLine][0] = getCharTexture('>');
				_lineColours[_cursorLine] = _lineColours[_cursorLine+1];
				_backgroundColours[_cursorLine] = _backgroundColours[_cursorLine+1];
				_cursorX = 1;
			}
		}
	}
	
	private void shiftContentUp()
	{
		TextureRegion[] tmp = _content[_content.length-1];
		for(int l = _content.length-1; l > 0; l--)
		{
			int nextLine = l-1;
			_content[l] = _content[nextLine];
			_lineColours[l] = _lineColours[nextLine];
			_backgroundColours[l] = _backgroundColours[nextLine];
		}
		_content[0] = tmp;			
		
		for(int c = 0; c < _lineSize; c++)
			_content[0][c] = null;
		
		_content[0][0] = getCharTexture('>');
		_cursorX = 1;
	}
	
	public TextureRegion getCharTexture(char c)
	{
		return _charTextures.get(c);
	}
	
	public void screenColor(Color c)
	{
		_screenColor.set(c);
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha)
	{		
		if(_screenColor != Color.CLEAR)
		{
			batch.setColor(ColorTools.combineAlpha(_screenColor, parentAlpha));
			batch.draw(_texShadow, x, y, 0, 0, width, height, 1f, 1f, 0);
		}
		
		for(int l = 0; l < _content.length; l++)
		{
			//batch.setColor(_lineColours[l]);
			for(int c = 0; c < _lineSize; c++)
			{
				if(_backgroundColours[l] != Color.CLEAR && _content[l][0] != null)
				{
					batch.setColor(ColorTools.combineAlpha(_backgroundColours[l], parentAlpha));
					batch.draw(_texShadow , x + c * _charSize, lineY(l) + (_charSize/8f), 0, 0, _charSize, _charSize, 1, 1, 0);
				}
				
				TextureRegion t = _content[l][c];// _charTextures.get(_content[l][c]);
				if(t != null)
				{					
					batch.setColor(ColorTools.combineAlpha(_lineColours[l], parentAlpha));
					batch.draw(t, x + c * _charSize, lineY(l), 0, 0, _charSize, _charSize, 1, 1, 0);
				}
			}
		}
		
//		if(_backgroundColours[cursorLine()] != Color.CLEAR)
//		{
//			batch.setColor(ColorTools.combineAlpha(_backgroundColours[cursorLine()], parentAlpha));
//			batch.draw(_texShadow , x + (_cursorX * _charSize), lineY(cursorLine()) + (_charSize/8f), 0, 0, _charSize, _charSize, 1, 1, 0);
//		}
		
		batch.setColor(_lineColours[cursorLine()].r, _lineColours[cursorLine()].g, _lineColours[cursorLine()].b,  this.color.a * parentAlpha);
		batch.draw(_texShadow, x + (_cursorX * _charSize), lineY(cursorLine()) + (_charSize/8f), 0, 0, _charSize * (3f/4f), _charSize, 1, 1, 0);
	}
	
	private float lineY(int line)
	{
		return ((_charSize + _lineSpacing) * line) + y;
	}

	@Override
	public void onEvent(int type, BaseTween<?> source)
	{
		if(_processBufferTween != null)
		{
			_processBufferTween.kill();
			_processBufferTween = null;
		}
		
		if(_buffer.isEmpty())
		{
			_busy = false;

			if(_handler != null)
				_handler.bufferEmpty();
		}
		else processBuffer();
	}
	
	private void setAvatarNow(TextureRegion tex, Color c)
	{
		Tween.to(_nextAvatar, ActorTweener.VAL_POS_XY, 500)
			.target(stage.width(), _nextAvatar.y)
			.ease(Quad.INOUT)
			.start(Game.TweenManager);
		
		_nextAvatar = _nextAvatar == _avatar1 ? _avatar2 : _avatar1;
		
		if(tex != nullAvatar)
		{
			_nextAvatar.setTexture(tex);
			_nextAvatar.height = stage.height() * (4f/8f); 
			_nextAvatar.width = _nextAvatar.height * (((float)tex.getRegionWidth()) / ((float)tex.getRegionHeight()));
			_nextAvatar.x = stage.width() - _nextAvatar.width;
			_nextAvatar.y = -_nextAvatar.height;
			_nextAvatar.color.set(c);
			_nextAvatar.visible = true;
			
			Tween.to(_nextAvatar, ActorTweener.VAL_POS_XY, 500)
				.target(_nextAvatar.x, -(_nextAvatar.height / 8f))
				.ease(Bounce.OUT)
				.start(Game.TweenManager);	
		}
	}
	
	public void inputMode(boolean on)
	{
		_inputMode = on;		
		//Gdx.input.setOnscreenKeyboardVisible(on);
		//getStage().setKeyboardFocus(this);
		
		if(_inputMode)
			_busy = false;
		else
		{
			Gdx.input.setOnscreenKeyboardVisible(false);
			printCharacter('\n');
			processBuffer();
		}
	}
	
	@Override
	public boolean keyDown(int keycode)
	{
		return true;
	}
	
	@Override
	public boolean keyUp(int keycode)
	{
		switch(keycode)
		{
			case Keys.ESCAPE:
			case Keys.BACK:
				if(_inputMode && _handler != null)
				{
					_handler.cancelInput();
				}
		}
		
		return true;
	}
	
	@Override
	public boolean keyTyped(char character)
	{
		if(_inputMode && _buffer.isEmpty())
		{
			switch(character)
			{
				case '\b':				
				{
					if(_textEntry.length() > 0)
						_textEntry.deleteCharAt(_textEntry.length()-1);
					
					if(_cursorX > 0)
						_cursorX--;
					
					_content[cursorLine()][_cursorX] = getCharTexture(' ');
					
					break;
				}
				case '\r':
				case '\n':
				{
					String text = _textEntry.toString();
					_textEntry.delete(0, _textEntry.length());
					
					inputMode(false);

					if(_handler != null)
						_handler.textEntered(text);
					
					break;
				}
				default:
				{
					if(_cursorX < (_content.length-1))
					{
						printCharacter(character);
						_textEntry.append(character);
					}
					
					break;
				}
			}
			
			return true;
		}
		else return false;
	}
}
