package com.example.rabbitmqdemo;

import java.io.IOException;

import android.util.Log;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;


/**
 * Base class for objects that connect to a RabbitMQ Broker
 */
public abstract class IConnectToRabbitMQ{
	// Debugging
    private static final String TAG = "RabbitMQDemo";
    private static final boolean D = true;
    
      public String mServer;
 
      protected Channel mModel = null;
      protected Connection  mConnection;
 
      protected boolean Running ;
      private boolean mStatus = false;
 
      /**
       *
       * @param server The server address
       * @param exchange The named exchange
       * @param exchangeType The exchange type name
       */
      public IConnectToRabbitMQ(String server)
      {
          mServer = server;
      }
 
      public void Dispose()
      {
          Running = false;
 
            try {

                if (mModel != null)
                    mModel.abort();
                if (mConnection!=null)
                    mConnection.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
 
      }
 
      /**
       * Connect to the broker and create the exchange
       * @return success
       */
      public boolean connectToRabbitMQ()
      {
    	  mStatus = false;
          if(mModel!= null && mModel.isOpen() )//already declared
              return true;
          Thread thread = new Thread(){
        	  @Override
              public void run(){
        		  try
                  {
                      ConnectionFactory connectionFactory = new ConnectionFactory();
                      connectionFactory.setHost(mServer); 
                      mConnection = connectionFactory.newConnection();  
                      mModel = mConnection.createChannel();
                      mStatus = true;
                  }
                  catch (Exception e)
                  {
                      e.printStackTrace();
                      // Typical error: NetworkOnMainThreadException
                      if(D) Log.e(TAG, "super channel error: "+e);   
                      mStatus = false;
                  }
        	  }
          };
          thread.start();
          try {
			thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
          return mStatus;
          
      }
}
