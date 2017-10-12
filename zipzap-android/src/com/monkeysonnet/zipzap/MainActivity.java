package com.monkeysonnet.zipzap;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.view.WindowManager;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.monkeysonnet.engine.ICallback;
import com.monkeysonnet.zipzap.Z;

public class MainActivity extends AndroidApplication implements IAndroid
{
	protected static final int MSG_SHARE_TEXT = 0;
	private static final int MSG_GET_TEXT = 1;
	private ICallback _scanBarcodeCallback;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.useGL20 = false;
        cfg.r = cfg.g = cfg.b = cfg.a = 8;
        //cfg.resolutionStrategy = new FixedResolutionStrategy(533, 320);
        
        initialize(new ZipZapGame(), cfg);
        
        Z.deviceId = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);             
        Z.android = this;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) 
    {
    	super.onActivityResult(requestCode, resultCode, intent);
    	
    	  IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
    	  if (scanResult != null) 
    	  {    		  
    		  if(_scanBarcodeCallback != null)
    		  {
        		  postRunnable(new CallbackWrapper(_scanBarcodeCallback, scanResult.getContents()));
    		  }        		  
    	  }
    }
    
	public void shareText(CharSequence str)
	{
		shareTextHandler.sendMessage(shareTextHandler.obtainMessage(MSG_SHARE_TEXT, str));
	}
	
	public void scanText(ICallback onComplete) 
	{
		_scanBarcodeCallback = onComplete;
		shareTextHandler.sendMessage(shareTextHandler.obtainMessage(MSG_GET_TEXT));
	}

	private final Handler shareTextHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg) 
		{
			switch(msg.what)
			{
				case MSG_SHARE_TEXT:
				{
					IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
					integrator.shareText((CharSequence)msg.obj);
				}
					break;
				case MSG_GET_TEXT:
				{
					IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
					integrator.initiateScan();
				}
					break;
			}			
		};
	};
	
	private class CallbackWrapper implements Runnable
	{
		private ICallback _cb;
		private Object _arg;
		
		
		public CallbackWrapper(ICallback cb, Object arg)
		{
			_cb = cb;
			_arg = arg;
		}

		public void run()
		{
			_cb.callback(_arg);
		}
	}
	
	public void openUri(String uri)
	{
		Intent i = new Intent(
				Intent.ACTION_VIEW, 
				Uri.parse(uri));
		startActivity(i);
	}
	
	public void share(String text, String image)
	{		
		Intent i = new Intent(android.content.Intent.ACTION_SEND);
		i.setType("text/plain");
		i.putExtra(android.content.Intent.EXTRA_TEXT, text);
		startActivity(Intent.createChooser(i, "Share via"));
	}	
}