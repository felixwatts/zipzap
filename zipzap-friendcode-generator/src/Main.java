import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import com.monkeysonnet.zipzap.Squadron;


public class Main
{
	public static void main(String[] args)
	{
		if(args.length != 1)
			return;
		
		String name = args[0];		
		try
		{
			System.out.println(Squadron.makeUri(name, true));
		} catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		} catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}
}
