package com.bigdata.spark;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.List;

import org.apache.log4j.*;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.*;

public class verySimpleStreamingApp {

    public static void main(String[] args) throws InterruptedException {
    	/*
    	 * The command line is 
    	 * spark-submit --class com.bigdata.spark.verySimpleStreamingApp 
    	 * target/project2-0.0.1-SNAPSHOT-shaded.jar local[4] test 3 localhost 10000 
    	 * localhost 9999
    	 */
    	//local[*]
    	String mode = args[0];
    	String appName = args[1];
    	long duration = Long.parseLong(args[2]);
    	String hostReceiver = args[3];
    	int portReceiver = Integer.parseInt(args[4]);
    	String hostSender = args[5];
    	int portSender = Integer.parseInt(args[6]);
    	
    	for(String s:args)
    	{
    		System.out.println(s);
    	}
    	
        // Configure and initialize the SparkStreamingContext
        SparkConf conf = new SparkConf()
                .setMaster(mode)
                .setAppName(appName);
        JavaStreamingContext streamingContext =
                new JavaStreamingContext(conf, Durations.seconds(duration));
        Logger.getRootLogger().setLevel(Level.ERROR);

        // Receive streaming data from the source
        JavaReceiverInputDStream<String> lines = streamingContext.
        		socketTextStream(hostReceiver, portReceiver);
        lines.print();
        lines.foreachRDD(new VoidFunction<JavaRDD<String>>(){

			@Override
			public void call(JavaRDD<String> rdd) throws Exception {
				// TODO Auto-generated method stub
				List<String>ss = rdd.collect();
				Socket socket = new Socket(hostSender,portSender);
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				for(String s:ss)
				{
					out.writeUTF(s);
				}
				out.close();
				socket.close();
				/*
				rdd.foreach(new VoidFunction<String>(){
				
					@Override
					public void call(String s) throws Exception {
						// TODO Auto-generated method stub
						Socket socket = new Socket("localhost",10000);
						DataOutputStream out = new DataOutputStream(socket.getOutputStream());
						out.writeUTF(s);
						out.close();
						socket.close();
					}
				});
				*/
			}
        	
        });

        // Execute the Spark workflow defined above
        streamingContext.start();
        streamingContext.awaitTermination();
    }
}