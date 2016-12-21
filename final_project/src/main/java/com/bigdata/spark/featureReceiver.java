package com.bigdata.spark;

import com.google.common.io.Closeables;

import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.receiver.Receiver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;


/**
 * Custom Receiver that receives data over a socket. Received bytes is interpreted as
 * text and \n delimited lines are considered as records. They are then counted and printed.
 *
 * Usage: JavaCustomReceiver <master> <hostname> <port>
 *   <master> is the Spark master URL. In local mode, <master> should be 'local[n]' with n > 1.
 *   <hostname> and <port> of the TCP server that Spark Streaming would connect to receive data.
 *
 * To run this on your local machine, you need to first run a Netcat server
 *    `$ nc -lk 9999`
 * and then run the example
 *    `$ bin/run-example org.apache.spark.examples.streaming.JavaCustomReceiver localhost 9999`
 */

public class featureReceiver extends Receiver<String> {

  String host = null;
  int port = -1;

  public featureReceiver(String host_ , int port_) {
    super(StorageLevel.MEMORY_AND_DISK());
    host = host_;
    port = port_;
  }

  public void onStart() {
    // Start the thread that receives data over a connection
    new Thread()  {
      @Override public void run() {
        receive();
      }
    }.start();
  }

  public void onStop() {
    // There is nothing much to do as the thread calling receive()
    // is designed to stop by itself isStopped() returns false
  }

  /** Create a socket connection and receive data until receiver is stopped */
  private void receive() {
    try {
      Socket socket = null;
      BufferedReader reader = null;
      String userInput = null;
      int featureDimension = 4096;
      DataOutputStream out = null;
      try {
        // connect to the server
        socket = new Socket(host, port);
        reader = new BufferedReader(
            new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        // Until stopped or connection broken continue reading
        out = new DataOutputStream(socket.getOutputStream()); 
        while (!isStopped() && (userInput = reader.readLine()) != null) {
          String str = null;
          String[] inputArray = userInput.split("\t");
          System.out.println("Received id '" + inputArray[0] + "'");
          if(inputArray.length==2)
          {
          	  store(userInput);
          	  str = inputArray[0] + " received";
          }
          else
          {
        	  str = inputArray[0] + " wrong dimension";
          }
          out.writeUTF(str);
        }
      } finally {
    	Closeables.close(out, /* swallowIOException = */ true);
        Closeables.close(reader, /* swallowIOException = */ true);
        Closeables.close(socket,  /* swallowIOException = */ true);
      }
      // Restart in an attempt to connect again when server is active again
      restart("Trying to connect again");
    } catch(ConnectException ce) {
      // restart if could not connect to server
      restart("Could not connect", ce);
    } catch(Throwable t) {
      restart("Error receiving data", t);
    }
  }
}