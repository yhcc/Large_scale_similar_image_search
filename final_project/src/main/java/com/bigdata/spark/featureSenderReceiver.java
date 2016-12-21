package com.bigdata.spark;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.bigdata.image.arrayToVector;

public class featureSenderReceiver {
    private static final Executor SERVER_EXECUTOR1 = Executors.newSingleThreadExecutor();
    private static final Executor SERVER_EXECUTOR2 = Executors.newSingleThreadExecutor();
    private static final int PORT1 = 9999;
    private static final int PORT2 = 10000;
    private static final double EVENT_PERIOD_SECONDS = 3;

    public static void main(String[] args) throws IOException, InterruptedException {
        BlockingQueue<String> eventQueue = new ArrayBlockingQueue<String>(100);
        SERVER_EXECUTOR1.execute(new SteamingServer(eventQueue));
        SERVER_EXECUTOR2.execute(new StreamingReceiver());
        while (true) {
            eventQueue.put(generateEvent());
            //Thread.sleep(TimeUnit.SECONDS.toMillis(EVENT_PERIOD_SECONDS));
            Thread.sleep((long) (EVENT_PERIOD_SECONDS*1000));
        }
    }

    private static String generateEvent() throws IOException {
    	/*
    	 * generate image data to spark 
    	 */
        String s = arrayToVector.getSparseVector(new File("/Users/yh/Desktop/bigdata/code/project2/images/0001.png"));  
    	return "001\t" + s;
    }
    
    /*
     * This is used to receive result from spark cluster
     */
    private static class StreamingReceiver implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(true)
			{
				ServerSocket serverSocket = null;
				try{
					serverSocket = new ServerSocket(PORT2);
	           	 	Socket clientSocket = serverSocket.accept();
	           	 	DataInputStream input = new DataInputStream(clientSocket.getInputStream());
	           	 	while(true)
	           	 	{
	                    String s = input.readUTF();
	                    System.out.println("!!!!!!!" + s + '\n');
	           	 	}
				}catch (IOException e) {
					// TODO Auto-generated catch block
					//When one connection close, it will throw this exception.
					//e.printStackTrace();
				}finally{
		            try{
		                if(serverSocket!=null) 
		                	serverSocket.close();//close this socket.
		            }catch(IOException e){
		            	e.printStackTrace();}
		        }				
			}
		}
  
    }
    /*
     * This is used to send data to spark cluster
     */
    private static class SteamingServer implements Runnable {
        private final BlockingQueue<String> eventQueue;

        public SteamingServer(BlockingQueue<String> eventQueue) {
            this.eventQueue = eventQueue;
        }

        public void run() {
            	while(true)
            	{
            		ServerSocket serverSocket = null;
            		try{
		               	 serverSocket = new ServerSocket(PORT1);
		                 Socket clientSocket = serverSocket.accept();
		                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
		                 DataInputStream input = new DataInputStream(clientSocket.getInputStream());
		                 while (true) {
		                    String event = eventQueue.take();
		                    System.out.println(String.format("Writing \"%s\" to the socket.", event));
		                    out.println(event);
		                    //receive the feedback from spark receiver, needs further modified.
		                    String receivedInfo = input.readUTF();
		                    System.out.println(String.format("接收到的结果：%s", receivedInfo));
		                }
	            } catch (InterruptedException e) {
	                throw new RuntimeException("Server error", e);
	            } catch (IOException e) {
					// TODO Auto-generated catch block
	            	//this exception will raise when the connection is disconnected from spark.
					//e.printStackTrace();
				} finally{
					try{
						if(!serverSocket.isClosed())
							serverSocket.close();
					}catch(IOException e){
						e.printStackTrace();
					}
				}
            	}

        }
    }
}
