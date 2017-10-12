package com.monkeysonnet.zipzap;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.monkeysonnet.engine.Game;
import com.monkeysonnet.engine.editor.Map;
import com.monkeysonnet.engine.editor.Shape;

public class Tools
{
	public static final Vector2 zeroVector = new Vector2(0,0);
	
	public static Vector2 centre(Vector2[] verts)
	{
		Vector2.tmp.set(0, 0);
		for(int n = 0; n < verts.length; n++)
			Vector2.tmp.add(verts[n]);
		return Vector2.tmp.mul(1f/verts.length);
	}
	
	public static Color stringToColour(String str)
	{
		if(str.equals("red"))
			return Color.RED;
		else if(str.equals("orange"))
			return Color.ORANGE;
		else if(str.equals("yellow"))
			return Color.YELLOW;
		else if(str.equals("green"))
			return Color.GREEN;
		else if(str.equals("cyan"))
			return Color.CYAN;
		else if(str.equals("blue"))
			return Color.BLUE;
		else if(str.equals("magenta"))
			return Color.MAGENTA;
		else if(str.equals("grey"))
			return Color.GRAY;
		else if(str.equals("dark-grey"))
			return Color.DARK_GRAY;
		else if(str.equals("white"))
			return Color.WHITE;
		else return Color.MAGENTA;
	}
	
	public static Color[] mapColours(Map map)
	{
		Color[] colors = new Color[map.numShapes()];
		for(int n = 0; n < map.numShapes(); n++)
		{
			Shape s = map.shape(n);
			
			Color c = null;
				
			if(s.hasProperty("colour"))
				c = Tools.stringToColour(s.propertyValue("colour"));
							
			colors[n] = c;			
		}
		
		return colors;
	}	
	
	public static Vector2 randomSpawnLoc()
	{
		return Vector2.tmp.set(ZipZapSim.SPAWN_DISTANCE, 0).rotate(Game.Dice.nextFloat()*360).add(Z.sim.focalPoint()); //ship().origin());
	}
	
	public static float angleToShip(Vector2 loc)
	{
		return Z.v2().set(Z.ship().origin()).sub(loc).angle();
	}
	
	public static float random(float mean, float sd)
	{
		return mean + (float)(Game.Dice.nextGaussian()) * sd;
	}
	
    public static String sha1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException  
    {
	    MessageDigest md = MessageDigest.getInstance("SHA-1");
	    byte[] sha1hash = new byte[40];
	    md.update(text.getBytes("iso-8859-1"), 0, text.length());
	    sha1hash = md.digest();
	    return toHex(sha1hash);
    }
    
	private static String toHex(byte[] data) 
	{
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < data.length; i++) 
        {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do 
            {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                        buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } 
            while(two_halfs++ < 1);
        }
        return buf.toString();
    }
	
	public static boolean prob(float probability)
	{
		return Game.Dice.nextFloat() < probability;
	}
	
	public static float aspect(TextureRegion tex, boolean height)
	{
		float h = tex.getRegionHeight();
		float w = tex.getRegionWidth();
		
		return height ? (h/w) : (w/h);
	}
}
