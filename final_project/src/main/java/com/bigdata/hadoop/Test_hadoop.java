package com.bigdata.hadoop;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileAsBinaryInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.bigdata.image.featureWritable;

/*
 * Used to do some hadoop test
 * 
 */
public class Test_hadoop extends Configured implements Tool{
	
	public static void main(String[] args)
	{
		Configuration conf = new Configuration();
		try {
			int res = ToolRunner.run(conf,new Test_hadoop(),args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//!!!!!!!!!!log needed
			System.out.println("Cannot run main project");
		}
	}

	public int run(String[] arg0) throws Exception {
		// TODO Auto-generated method stub
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf);
		job.setJarByClass(Test_hadoop.class);
		job.setMapperClass(myMapper.class);
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(featureWritable.class);
		job.setNumReduceTasks(0);
		
		Path input = new Path("file:/Users/yh/Desktop/bigdata/code/project/input/input.txt");
		Path output = new Path("output");
		FileInputFormat.setInputPaths(job,input);
		FileOutputFormat.setOutputPath(job,output);
		
		boolean i = job.waitForCompletion(true);
	
		return i?1:0;
	}
	
	/*
	 * Mapper function
	 */
	public static class myMapper extends Mapper<LongWritable,Text,Text,featureWritable>{
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
		{
			System.out.println(key.toString());
			String[] ss = value.toString().split(",");
			String name = ss[0];
			float[] features = new float[3]; 
			for(int i=0;i<3;++i)
			{
				features[i] = Float.parseFloat(ss[i+1]);
			}
			featureWritable f = new featureWritable(features);
			context.write(new Text(name),f);
		}
	}
}

