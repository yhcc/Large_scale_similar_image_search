package com.bigdata.spark;

import java.io.*; // wildcard import for brevity in tutorial
import java.net.*;
import java.nio.charset.StandardCharsets;


public class testForPythonSenderReceiver {
	public static void main(String[]args) throws UnknownHostException, IOException
	{
		/*
	    Socket socket = null;
	    BufferedReader reader = null;
	    String userInput = null;
	    DataOutputStream out = null;
		try{
		//This is used to test the receiver 
		  // connect to the server
		  socket = new Socket("localhost", 10000);
		  reader = new BufferedReader(
		      new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
		  // Until stopped or connection broken continue reading
		  out = new DataOutputStream(socket.getOutputStream()); 
		  while ((userInput = reader.readLine()) != null) {
			   System.out.println(userInput.length());
		        String str = "!1.png\t1.png,1.png,1.png,";
		        System.out.println(userInput);
		        out.writeUTF(str);
		        out.flush();
		        }
      }catch(Exception e)
      {
    	  out.close();
    	  socket.close();
      }*/ 
      
       //this is used to test the sender of spark output
		Socket socket = null;
		DataOutputStream out = null;
		try{
			socket = new Socket("localhost", 10000);
			out = new DataOutputStream(socket.getOutputStream());
			StringBuilder s = new StringBuilder();
			s.append("1.png\t1.png,1.png,");
			System.out.println(s.length());
			out.writeUTF(s.toString());
			out.flush();
		}catch(Exception e){
			out.close();
			socket.close();
		}
    }
 
}
