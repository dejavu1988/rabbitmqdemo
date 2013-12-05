package com.example.rabbitmqdemo;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings.Secure;
import android.util.Log;

public class DaemonService extends Service {

	// Debugging
    private static final String TAG = "RabbitMQDemo";
    private static final boolean D = true;
    
    //private PrefManager pM;
	public static Socket socket = null;  
	public static BufferedReader in = null;  
	public static PrintWriter out = null; 
	public static String content = "";  
	private Message message;
    private static String uuid;
    private Thread thrm;
    
    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new DaemonBinder();
    
    private static final Class[] mStartForegroundSignature = new Class[] {
        int.class, Notification.class};
    private static final Class[] mStopForegroundSignature = new Class[] {
        boolean.class};
    
    private NotificationManager mNM;
    private Method mStartForeground;
    private Method mStopForeground;
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];
    
    private MessageConsumer mConsumer;
    
    @Override
    public void onCreate() {
    	super.onCreate();
    	
        
        uuid = Secure.getString(this.getContentResolver(),Secure.ANDROID_ID);
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        
        try {
            mStartForeground = getClass().getMethod("startForeground",
                    mStartForegroundSignature);
            mStopForeground = getClass().getMethod("stopForeground",
                    mStopForegroundSignature);
        } catch (NoSuchMethodException e) {
            // Running on an older platform.
            mStartForeground = mStopForeground = null;
        }
        
        
        mConsumer = new MessageConsumer("54.229.32.28", "queue1", "queue2");
		
		//thrm = new Thread(new Daemon(this));
		//thrm.start();
    }
    
    void startForegroundCompat(int id, Notification notification) {
        // If we have the new startForeground API, then use it.
        if (mStartForeground != null) {
            mStartForegroundArgs[0] = Integer.valueOf(id);
            mStartForegroundArgs[1] = notification;
            try {
                mStartForeground.invoke(this, mStartForegroundArgs);
            } catch (InvocationTargetException e) {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke startForeground", e);
            } catch (IllegalAccessException e) {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke startForeground", e);
            }
            return;
        }
        
        
    }
    
    void stopForegroundCompat(int id) {
        // If we have the new stopForeground API, then use it.
        if (mStopForeground != null) {
            mStopForegroundArgs[0] = Boolean.TRUE;
            try {
                mStopForeground.invoke(this, mStopForegroundArgs);
            } catch (InvocationTargetException e) {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke stopForeground", e);
            } catch (IllegalAccessException e) {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke stopForeground", e);
            }
            return;
        }
        
        
    }
    
    public synchronized void stopThread(){
    	if(thrm != null){
    		Thread moribund = thrm;
    		thrm = null;
    		moribund.interrupt();
    	}
    }
    
    @Override
    public void onDestroy() {
        // Make sure our notification is gone.
        stopForegroundCompat(R.string.foreground_service_started);
        //connected = false;        
        //stopThread();
        mConsumer.dispose();
    }
    

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	
        handleCommand(intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        //new Thread(new Daemon(this)).start(); 
        boolean t = mConsumer.connectToRabbitMQ();
        if(D) Log.e(TAG, "connection "+t);
        return START_NOT_STICKY;
    }
    
    void handleCommand(Intent intent) {
        //if (ACTION_FOREGROUND.equals(intent.getAction())) {
            // In this sample, we'll use the same text for the ticker and the expanded notification
            CharSequence text = getText(R.string.foreground_service_started);

            // Set the icon, scrolling text and timestamp
            Notification notification = new Notification(R.drawable.ic_launcher, text,
                    System.currentTimeMillis());

            // The PendingIntent to launch our activity if the user selects this notification
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

            // Set the info for the views that show in the notification panel.
            notification.setLatestEventInfo(this, getText(R.string.foreground_service_label),
                           text, contentIntent);
            
            startForegroundCompat(R.string.foreground_service_started, notification);
            
        //} else if (ACTION_BACKGROUND.equals(intent.getAction())) {
        //    stopForegroundCompat(R.string.foreground_service_started);
        //}
    }
    
    public class DaemonBinder extends Binder {
        DaemonService getService() {
            return DaemonService.this;
        }
    }
    
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
	

}
