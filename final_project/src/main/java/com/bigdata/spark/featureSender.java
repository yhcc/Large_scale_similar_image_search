package com.bigdata.spark;

import java.io.*; // wildcard import for brevity in tutorial
import java.net.*;
import java.util.Random;
import java.util.concurrent.*;

import com.bigdata.image.arrayToVector;

public class featureSender {
    private static final Executor SERVER_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final int PORT = 9999;
    private static final double EVENT_PERIOD_SECONDS = 1;
    private static final Random random = new Random();

    public static void main(String[] args) throws IOException, InterruptedException {
        BlockingQueue<String> eventQueue = new ArrayBlockingQueue<String>(100);
        SERVER_EXECUTOR.execute(new SteamingServer(eventQueue));
        while (true) {
            eventQueue.put(generateEvent());
            //Thread.sleep(TimeUnit.SECONDS.toMillis(EVENT_PERIOD_SECONDS));
            Thread.sleep((long) (EVENT_PERIOD_SECONDS*2000));
        }
    }

    private static String generateEvent() throws IOException {
    	/*
    	 * send image data to spark 
    	 */
        String s = arrayToVector.getSparseVector(new File("/Users/yh/Desktop/bigdata/code/project2/images/0001.png"));  
    	return "001\t" + s;
    }

    private static class SteamingServer implements Runnable {
        private final BlockingQueue<String> eventQueue;

        public SteamingServer(BlockingQueue<String> eventQueue) {
            this.eventQueue = eventQueue;
        }

        public void run() {
            try {
            	 ServerSocket serverSocket = new ServerSocket(PORT);
                 Socket clientSocket = serverSocket.accept();
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                 DataInputStream input = new DataInputStream(clientSocket.getInputStream());
                while (true) {
                    String event = eventQueue.take();
                    System.out.println(String.format("Writing \"%s\" to the socket.", event));
                    out.println(event);
                    //测试接收反馈信息
                    String receivedInfo = input.readUTF();
                    System.out.println(String.format("接收到的结果：%s", receivedInfo));
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("Server error", e);
            } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
}