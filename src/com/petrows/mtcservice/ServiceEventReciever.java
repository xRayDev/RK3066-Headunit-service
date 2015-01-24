package com.petrows.mtcservice;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;

public class ServiceEventReciever extends BroadcastReceiver {
	final static String TAG = "ServiceEventReciever";

    //dsa
    public static boolean syn = false;
    private boolean ord = false;

	@Override
	public void onReceive(Context context, Intent intent) 
	{
		//int keyCode_ = intent.getIntExtra("keyCode", 0);
		//toast(context, "Action " + intent.getAction() + ", key " + keyCode_);
		
		Log.d(TAG, "Action " + intent.getAction() + ", key " + intent.getIntExtra("keyCode", -1));
		Log.d(TAG, "Service enable " + Settings.get(context).getServiceEnable());
		
		if (Settings.get(context).getServiceEnable() && !ServiceMain.isRunning)
		{
			// Run our service (if needed)
			context.startService(new Intent(context, ServiceMain.class));
		}
		
		// Microntek keys?
		if (intent.getAction().equals(Settings.MTCBroadcastIrkeyUp))
		{
			// Key pressed...
			
			int keyCode = intent.getIntExtra("keyCode", 0);
			Log.d(TAG, "MTCService recieve key "+keyCode);
			
			// Test media keys?
			if (Settings.get(context).getMediaKeysEnable())
			{			
				if (Settings.MTCKeysPrev.contains(keyCode))
				{
                    Settings.get(context).showToast("<<");
                    sendKey(context, KeyEvent.KEYCODE_MEDIA_PREVIOUS);

				}
				if (Settings.MTCKeysNext.contains(keyCode))
				{
                    Settings.get(context).showToast(">>");
                    sendKey(context, KeyEvent.KEYCODE_MEDIA_NEXT);

				}				
				if (Settings.MTCKeysPause.contains(keyCode))
				{
                    Settings.get(context).showToast("||");
                    sendKey(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);

				}
			} else {
				Log.d(TAG, "Media keys handler is disabled in settings");
			}
		}
		
		if (intent.getAction().equals(Settings.MTCBroadcastACC))
		{
			String accState = intent.getStringExtra("accstate");
			Log.d(TAG, "Acc state: " + accState);
			if ("accoff" == accState)
			{
				// We are powering-off
				
				// Set safe volume?
				if (Settings.get(context).getSafeVolumeEnable())
				{
					Settings.get(context).setVolumeSafe();
				}
			}
		}
		
		// Microntek launch app?
		if (intent.getAction().equals(Settings.MTCBroadcastWidget))
		{
			// Install widget?
			int wdgAction  = intent.getIntExtra("myWidget.action", 0);
			String wdgPackage = intent.getStringExtra("myWidget.packageName");
			
			Log.d(TAG, "MTCService recieve widget "+wdgPackage+", "+wdgAction);
			// Install widget?
			if (Settings.MTCWidgetAdd == wdgAction)
			{
				String[] wdgPackageSplit = wdgPackage.split("\\.");
				if (wdgPackageSplit.length > 0)
				{
					String pkgShort = wdgPackageSplit[wdgPackageSplit.length - 1];
					pkgShort = String.valueOf(pkgShort.charAt(0)).toUpperCase() + pkgShort.subSequence(1, pkgShort.length());
					Log.d(TAG, "Started mode: " + pkgShort);						
					Settings.get(context).showToast(pkgShort);
				}
				
				killMusic(context);
			}
		}
	}
	
	public void sendKey(Context ctx, int keycode) {
		Log.d(TAG, "Send key " + keycode);
		long eventtime = SystemClock.uptimeMillis();

        //dsa
        if( false == ord ) if( syn ) ord = true;

        Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        //downIntent.setComponent( new ComponentName( "com.maxmpz.audioplayer", "com.maxmpz.audioplayer.player.PlayerMediaButtonReceiver" ) );
		KeyEvent downEvent = new KeyEvent(eventtime, eventtime,
				KeyEvent.ACTION_DOWN, keycode, 0);
		downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
        //dsa
        if( ord )
            ctx.sendOrderedBroadcast( downIntent, null );
        else
            ctx.sendBroadcast( downIntent );

		Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        //upIntent.setComponent( new ComponentName( "com.maxmpz.audioplayer", "com.maxmpz.audioplayer.player.PlayerMediaButtonReceiver" ) );
		KeyEvent upEvent = new KeyEvent(eventtime, eventtime,
				KeyEvent.ACTION_UP, keycode, 0);
		upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
        //dsa
        if( ord )
            ctx.sendOrderedBroadcast( upIntent, null );
        else
            ctx.sendBroadcast( upIntent );
	}
	
	public void killMusic(Context ctx)
	{
		Log.d(TAG, "Killing music");	
		// Stop playback (NORMAL players)
		sendKey(ctx, KeyEvent.KEYCODE_MEDIA_STOP);
	}	
}
