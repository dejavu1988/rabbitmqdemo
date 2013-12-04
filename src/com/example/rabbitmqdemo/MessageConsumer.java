package com.example.rabbitmqdemo;

import java.io.IOException;

import android.os.Handler;
import android.util.Log;

import com.rabbitmq.client.QueueingConsumer;

/**
 *Consumes messages from a RabbitMQ broker
 *
 */
public class MessageConsumer extends IConnectToRabbitMQ{
 
	// Debugging
    private static final String TAG = "RabbitMQDemo";
    private static final boolean D = true;
    
    //The Queue name for this consumer
    private String mQueueRecv, mQueueSend;
    private QueueingConsumer MySubscription;
    
    public MessageConsumer(String server, String queueRecv, String queueSend) {
        super(server);
        mQueueRecv = queueRecv;
        mQueueSend = queueSend;
    }
 
    
 
    //last message to post back
    private byte[] mLastMessage;
 
    // An interface to be implemented by an object that is interested in messages(listener)
    public interface OnReceiveMessageHandler{
        public void onReceiveMessage(byte[] message);
    };
 
    //A reference to the listener, we can only have one at a time(for now)
    private OnReceiveMessageHandler mOnReceiveMessageHandler;
 
    /**
     *
     * Set the callback for received messages
     * @param handler The callback
     */
    public void setOnReceiveMessageHandler(OnReceiveMessageHandler handler){
        mOnReceiveMessageHandler = handler;
    };
 
    private Handler mMessageHandler = new Handler();
    private Handler mConsumeHandler = new Handler();
 
    // Create runnable for posting back to main thread
    final Runnable mReturnMessage = new Runnable() {
        public void run() {
            mOnReceiveMessageHandler.onReceiveMessage(mLastMessage);
        }
    };
 
    final Runnable mConsumeRunner = new Runnable() {
        public void run() {
            Consume();
        }
    };
    
 
    /**
     * Create Exchange and then start consuming. A binding needs to be added before any messages will be delivered
     */
    @Override
    public boolean connectToRabbitMQ()
    {
       if(super.connectToRabbitMQ())
       {
    	   if(D) Log.e(TAG, "super connection successful");
           try {
               //mQueue = mModel.queueDeclare().getQueue();
        	   mModel.queueDeclare(mQueueRecv, false, false, false, null);
        	   mModel.queueDeclare(mQueueSend, false, false, false, null);
        	   mModel.queuePurge(mQueueRecv);
        	   mModel.queuePurge(mQueueSend);
        	   if(D) Log.e(TAG, "connection: queue declared and purged");
               MySubscription = new QueueingConsumer(mModel);
               mModel.basicConsume(mQueueRecv, true, MySubscription);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
             
            Running = true;
            mConsumeHandler.post(mConsumeRunner);
 
           return true;
       }
       if(D) Log.e(TAG, "super connection failed");  
       return false;
    }
 
    
    private void Consume()
    {
        Thread thread = new Thread()
        {
 
             @Override
             public void run() {
            	 if(D) Log.e(TAG, "consume");
                 while(Running){
                    QueueingConsumer.Delivery delivery;
                    try {
                        delivery = MySubscription.nextDelivery();
                        mLastMessage = delivery.getBody();
                        if(D) Log.e(TAG, "msg received: " + new String(mLastMessage));
                        mMessageHandler.post(mReturnMessage);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                 }
             }
        };
        thread.start();
 
    }
    
    public void Publish(final String message){
    	Thread thread = new Thread(){
    		@Override
            public void run(){
    			try {    				
					mModel.basicPublish("", mQueueSend, null, message.getBytes());
					if(D) Log.e(TAG, "msg sent: " + message);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	};
    	thread.start();
    }
 
    public void dispose(){
        Running = false;
    }
}
