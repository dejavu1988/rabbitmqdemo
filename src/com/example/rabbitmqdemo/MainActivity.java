package com.example.rabbitmqdemo;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {
	// Debugging
    private static final String TAG = "RabbitMQDemo";
    private static final boolean D = true;
    
	private MessageConsumer mConsumer;
    private TextView mOutput;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        if(D) Log.e(TAG, "-- ON CREATE --");
		//The output TextView we'll use to display messages
        mOutput =  (TextView) findViewById(R.id.output);
 
        //Create the consumer
        /*mConsumer = new MessageConsumer("54.229.32.28",
        		"hello", "echohello");*/
 
        //Connect to broker
        //boolean t = mConsumer.connectToRabbitMQ();
        //if(D) Log.e(TAG, "connection "+t);
 
        //register for messages
        /*mConsumer.setOnReceiveMessageHandler(new OnReceiveMessageHandler(){
 
            public void onReceiveMessage(byte[] message) {
                String text = new String(message); 
                mOutput.append(text+"\n");
                mConsumer.Publish("ACK: hello world");
            }
        });*/
        
        Intent intentToService = new Intent(this, DaemonService.class);
        startService(intentToService);
        
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
    protected void onResume() {
        if(D) Log.e(TAG, "-- ON RESUME --");
        super.onResume();
        //Connect to broker
        //boolean t = mConsumer.connectToRabbitMQ();
        //if(D) Log.e(TAG, "connection "+t);
    }
 
    @Override
    protected void onPause() {
        if(D) Log.e(TAG, "-- ON PAUSE --");
        super.onPause();
        //mConsumer.dispose();
    }
}
