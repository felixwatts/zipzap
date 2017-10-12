package com.monkeysonnet.zipzap;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class Squadron
{
	private static final String _key = "<redacted>";
	
	private String[] _names = new String[4];
	private String[] _ids = new String[4];

	public Squadron()
	{
//		_ids[0] = "wingman";
//		_names[0] = "Wingman";
		
		for(int n = 0; n < 4; n++)
		{
			if(Z.prefs.getBoolean("squad-" + n, false))
			{
				_ids[n] = Z.prefs.getString("squad-" + n + "-id");
				_names[n] = Z.prefs.getString("squad-" + n + "-name");
			}
		}
	}
	
	public boolean isAlreadyInSquadron(String deviceId)
	{
		for(int n = 0; n < 4; n++)
		{
			if(deviceId.equals(_ids[n]))
				return true;
		}
		return false;
	}
	
	public String id(int n)
	{
		return _ids[n];
	}
	
	public String name(int n)
	{
		if(n < 0)
			return Z.username == null ? "Captain Astronomo" : Z.username;
		
		else return _names[n];
	}
	
	public static String makeUri(String name) throws NoSuchAlgorithmException, UnsupportedEncodingException
	{
		return makeUri(name, false);
	}
	
	public static String makeUri(String name, boolean unlimited) throws NoSuchAlgorithmException, UnsupportedEncodingException
	{				
		long date = unlimited ? 0 : new Date().getTime();
		String id = unlimited ? name : Z.deviceId;
		String sha1 = Tools.sha1(_key + id + date);
		return "afc://" + name + "-" + id + "-" + date + "-" + sha1;
	}
	
	public boolean add(String uri) throws NoSuchAlgorithmException, UnsupportedEncodingException
	{
		if(uri == null)
			return false;
		
		if(numInSquad() == 4)
			return false;
		
		if(!uri.startsWith("afc://"))
			return false;
		
		String[] parts = uri.substring(6).split("-");
		
		if(parts.length != 4)
			return false;
		
		String name = parts[0];
		//String color = parts[1];
		String id = parts[1];
		String dateStr = parts[2];
		String key = parts[3];
		
		long date;
		date = Long.parseLong(dateStr);
		
		String sha1 = Tools.sha1(_key + id + date);
		
		if(!key.equals(sha1))
			return false;
		
		if(date != 0)
		{
			long now = new Date().getTime();
			long maxAge = 60 * 60 * 1000;
			if((date < now - maxAge) || (date > now + maxAge))
				return false;
		}
		
		if(isAlreadyInSquadron(id))
			return false;
		
//		if(color.length() != 8)
//			return false;
//		
//		String r = color.substring(0, 2);
//		String g = color.substring(2, 4);
//		String b = color.substring(4, 6);
//		
//		Color c;
//		
//		try
//		{
//			c = new Color(
//					((float)Integer.parseInt(r, 16))/255f,
//					((float)Integer.parseInt(g, 16))/255f,
//					((float)Integer.parseInt(b, 16))/255f,
//					1f);
//		}
//		catch(NumberFormatException ex)
//		{
//			return false;
//		}
		
		int n = numInSquad();
		
		_names[n] = name;
		_ids[n] = id;
	//	_colors[n] = c;
		
		Z.prefs.putBoolean("squad-" + n, true);
		Z.prefs.putString("squad-" + n + "-id", id);
		Z.prefs.putString("squad-" + n + "-name", name);
//		Z.prefs.putFloat("squad-" + n + "color-r", c.r);
//		Z.prefs.putFloat("squad-" + n + "color-g", c.g);
//		Z.prefs.putFloat("squad-" + n + "color-b", c.b);
		Z.prefs.flush();
		
		return true;
	}
	
	public int numInSquad()
	{
		for(int n = 0; n < 4; n++)
		{
			if(_ids[n] == null)
				return n;
		}
		
		return 4;
	}
}
