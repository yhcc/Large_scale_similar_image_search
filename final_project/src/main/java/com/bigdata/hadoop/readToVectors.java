package com.bigdata.hadoop;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.CounterGroup;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.counters.GenericCounter;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.bigdata.image.arrayToVector;

import org.apache.hadoop.mapred.Counters.Counter;
/*
 * This is hadoop script used to convert path files into image vectors.
 * 
 */
public class readToVectors extends Configured implements Tool{
	
	public static void main(String[] args)
	{
		Configuration conf = new Configuration();
		try {
			int res = ToolRunner.run(conf,new readToVectors(),args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//!!!!!!!!!!log needed
			System.out.println("Cannot run main project");
		}
	}

	public int run(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf);
		job.setJarByClass(readToVectors.class);
		job.setMapperClass(myMapper.class);
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setNumReduceTasks(0);
		
		Path input = new Path(args[0]);
		Path output = new Path(args[1]);
		FileInputFormat.setInputPaths(job,input);
		FileOutputFormat.setOutputPath(job,output);
		
		boolean i = job.waitForCompletion(true);
		
		CounterGroup groups = job.getCounters().getGroup("UserDefined");
		Iterator<org.apache.hadoop.mapreduce.Counter> it = groups.iterator();
		while(it.hasNext())
		{
			GenericCounter ct = (GenericCounter) it.next();
			System.out.println(ct.getDisplayName()+":"+ct.getValue());
		}
		return i?1:0;
	}
	
	/*
	 * Mapper function
	 */
	public static class myMapper extends Mapper<LongWritable,Text,Text,Text>{
		public Configuration conf;
		public FileSystem fs;
		public void setup(Context context) throws IOException
		{
			conf = context.getConfiguration();
			fs = FileSystem.newInstance(conf);
		}
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
		{
		  //check whether this file exists
		  String fileName = value.toString();
		  if(fs.exists(new Path(fileName)))
		  {
			  //add statics information, calculate the total number of images for each type
			  int index = fileName.lastIndexOf('.');
			  String suffix = fileName.substring(index).toLowerCase();
			  Counter ct = (Counter) context.getCounter("UserDefined", suffix);
			  ct.increment(1);
			  
			  //need to process the keys for this task.right now only get the name of this image
			  int index_ = fileName.lastIndexOf('/');
			  String idName = fileName.substring(index_+1);
			  
			  //get feature vectors of this image. This is a sparse vector
			  String s = arrayToVector.getSparseVector(new Path(fileName),fs);
			  //write to the result, should add some filter conditions to make search faster
			  ct = (Counter) context.getCounter("UserDefined","imageProcessed");
			  ct.increment(1);
			  context.write(new Text(idName),new Text(s));
		  }
		  else
		  {
			  Counter ct = (Counter) context.getCounter("UserDefined", "imageNotExist");
			  ct.increment(1);
			  System.out.println(String.format("!!!!!!!Image does not exist:%s", fileName));
		  }
		}
	}
}

