package com.bigdata.spark;

import java.io.*; // wildcard import for brevity in tutorial
import java.net.*;
import java.util.Random;
import java.util.concurrent.*;

public class sendDemoData {
    private static final Executor SERVER_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final int PORT = 9999;
    private static final String DELIMITER = ":";
    private static final double EVENT_PERIOD_SECONDS = 1;
    private static final Random random = new Random();

    public static void main(String[] args) throws IOException, InterruptedException {
        BlockingQueue<String> eventQueue = new ArrayBlockingQueue<String>(100);
        SERVER_EXECUTOR.execute(new SteamingServer(eventQueue));
        while (true) {
            eventQueue.put(generateEvent());
            //Thread.sleep(TimeUnit.SECONDS.toMillis(EVENT_PERIOD_SECONDS));
            Thread.sleep((long) (EVENT_PERIOD_SECONDS*1000));
        }
    }

    private static String generateEvent() {
    	/*
        int userNumber = random.nextInt(10);
        String event = random.nextBoolean() ? "login" : "purchase";
        // In production use a real schema like JSON or protocol buffers
        return String.format("user-%s", userNumber) + DELIMITER + event;
        */
        System.out.print("请输入:\t");    
        // 发送键盘输入的一行    
        String s=null;
		try {
			s = new BufferedReader(new InputStreamReader(System.in)).readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
    	return s;
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
                    //test for receicing data
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