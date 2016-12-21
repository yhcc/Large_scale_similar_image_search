package com.bigdata.spark;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.api.java.function.ReduceFunction;
import org.apache.spark.api.java.function.VoidFunction2;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.linalg.Vectors;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.util.LongAccumulator;
import org.apache.spark.streaming.*;

import org.apache.spark.sql.Column;


/*
 * Spark online searching part
 */
public class onlineSearching{
	public static void main(String[] args) throws InterruptedException
	{
		//
    	/*
    	 * The command line is 
    	 * spark-submit --class com.bigdata.spark.onlineSearching
    	 * target/project2-0.0.1-SNAPSHOT-shaded.jar local[4] test 10 localhost 9999
    	 * localhost 10000 9 9 file:///Users/yh/Desktop/bigdata/code/project2/model/data
    	 * file:///Users/yh/Desktop/bigdata/code/project2/imageFeaturesClusters
    	 */
		//local[4]
    	String mode = args[0];
    	//appName
    	String appName = args[1];
    	//Time duration for streaming
    	long duration = Long.parseLong(args[2]);
    	//host name for streaming receiver
    	String hostReceiver = args[3];
    	//host port for streaming receiver
    	int portReceiver = Integer.parseInt(args[4]);
    	//host port for streaming output
    	String hostSender = args[5];
    	//host port for streaming output
    	int portSender = Integer.parseInt(args[6]);
    	//number of kmeans clusters to use
    	final int k = Integer.parseInt(args[7]);
    	//number of queries to return
    	final int numOfQuery = Integer.parseInt(args[8]);
    	//where to read kmeans center data
    	String centerData = args[9];
    	//where to read image features labelled with clusters
    	String imageFeaturesClusters = args[10];
    	
		SparkConf conf = new SparkConf().setMaster(mode).setAppName("onlinesearching");
		JavaStreamingContext jssc = new JavaStreamingContext(conf,Durations.seconds(duration));
		
		//JavaSparkContext jsc = new JavaSparkContext(conf);
		SparkSession spark = SparkSession.builder().config(conf).getOrCreate();
		
		
		final int featuresDimension = 4096;
		//read cluster center data
		Dataset<Row> centers = spark.read().format("parquet").load(centerData);
		centers.persist();
		System.out.println(String.format("Read %d centers.", centers.count()));
		//make centers broadcast
		final Broadcast<Dataset<Row>> centersBroadCast = jssc.sparkContext().broadcast(centers);
		//read image vectors
		Dataset<Row> imageDF = spark.read().load(imageFeaturesClusters);
		imageDF.persist();
		System.out.println(String.format("Read %d image vectors.", imageDF.count()));
		
		LongAccumulator receivedQuery = jssc.sparkContext().sc().longAccumulator();
		LongAccumulator validQuery = jssc.sparkContext().sc().longAccumulator();
		LongAccumulator returnQuery = jssc.sparkContext().sc().longAccumulator();
		
		//streaming
		JavaReceiverInputDStream<String> lines = jssc.receiverStream(
      	      new featureReceiver(hostReceiver, portReceiver));
		//filter strings, the highest index cannot bigger than 4095;
		JavaDStream<String> filteredLines = lines.filter(new Function<String,Boolean>(){
			@Override
			public Boolean call(String s) throws Exception {
				// TODO Auto-generated method stub
				String[] ss = s.split("\t")[1].split(",");
				receivedQuery.add(1);
				for(String i:ss)
				{
					if(Integer.parseInt(i.split(":")[0])>4096)
						return false;
					if(Double.parseDouble(i.split(":")[1])>1.0)
						return false;
				}
				return true;
			}
		});
		//convert string into vectors
		JavaDStream<sparseVector> convertedLines = filteredLines.map(new Function<String,sparseVector>(){

			@Override
			public sparseVector call(String s) throws Exception {
				sparseVector v = new sparseVector();
				v.setId(s.split("\t")[0]);
				String[] ss = s.split("\t")[1].split(",");
				int[] indexes = new int[ss.length];
				double[] values = new double[ss.length];
				for(int i=0;i<ss.length;++i)
				{
					indexes[i] = Integer.parseInt(ss[i].split(":")[0]);
					values[i] = Double.parseDouble(ss[i].split(":")[1]);
				}
				v.setFeatures(Vectors.sparse(featuresDimension, indexes, values));
				validQuery.add(1);
				return v;
			}
			
		});
		
		final Encoder<Center> centerEncoder = Encoders.bean(Center.class);
		//find the closest centers
		convertedLines = convertedLines.map(new Function<sparseVector, sparseVector>(){
			public sparseVector call(final sparseVector ve) throws Exception {
				// TODO Auto-generated method stub
				
				//separate id and feature
				String idx = ve.getId();
				
				//construct feature vector
				Vector v = ve.getFeatures();
				
				Dataset<Center> result = centersBroadCast.value().map(new MapFunction<Row,Center>(){
					public Center call(Row r) throws Exception {
						// TODO Auto-generated method stub
						Vector v1 = (Vector) r.get(r.fieldIndex("clusterCenter"));
						return new Center(r.getInt(0),Vectors.sqdist(v1, v));
					}
				}, 
				centerEncoder);
				//sort these distance
				result = result.sort(new Column("distance").asc());
				//take the first k center
				result = result.limit(k);
				//get the centers for top k result
				Center c = result.reduce(new ReduceFunction<Center>(){
					public Center call(Center c1, Center c2) throws Exception {
						// TODO Auto-generated method stub
						return c1.add(c2);
					}
				});
				String[] ss = c.getClusterId().split(",");
				double[] arr = new double[k];
				//convert clusters into vector
				for(int i=0;i<k;++i)
				{
					arr[i] = Double.parseDouble(ss[i]);
				}
				
				ve.setClusters(Vectors.dense(arr));
			
				return ve;
			}
		});
		
		//rdd cannot be used in foreach, so first collect data back to master, then do a loop
		convertedLines.foreachRDD(new VoidFunction2<JavaRDD<sparseVector>,Time>(){

			@Override
			public void call(JavaRDD<sparseVector> vectors, Time t) throws Exception {
				// TODO Auto-generated method stub
				List<sparseVector> data1 = vectors.collect();
				//scoket to send result back
				if(data1.size()>0)
				{
					Socket socket = new Socket(hostSender, portSender);
					DataOutputStream out = null;
					out = new DataOutputStream(socket.getOutputStream()); 
					for(sparseVector v:data1)
					{
						//get features
						final Vector v1 = v.getFeatures();
						//get the clusters
						List<String> clusters = new ArrayList<String>(k);
						for(double i:v.getClusters().toArray()){
							clusters.add(String.valueOf((int)i));
						};
						//get query id
						String idx = v.getId();
	
						//choose image belongs to a certain clusters
						Dataset<Row> selectedImage = imageDF.where(new Column("cluster").isin(
								clusters.stream().toArray(String[]::new)));
						
						Dataset<Center> result_ = selectedImage.map(new MapFunction<Row,Center>(){
							public Center call(Row v2) throws Exception {
								// TODO Auto-generated method stub
								double[] vector1 = v1.toArray();
								double[] vector2 = ((Vector)v2.get(v2.fieldIndex("features"))).toArray();
								double distance = 0;
								for(int i=0;i<featuresDimension;++i)
								{
									distance += Math.min(vector1[i], vector2[i]);
								}
								distance = 1-distance;
								return new Center(v2.getString(v2.fieldIndex("id")),distance);
							}
						}
						, centerEncoder);
						result_ = result_.sort(new Column("distance").asc());
						result_ = result_.limit(numOfQuery);
						result_.show();
						List<Row> result = result_.select(new Column("clusterId")).toJavaRDD().collect();
						
						//get cluster result
						String[] setback = new String[numOfQuery+1]; 
						setback[0] = idx;
						int i = 1;
						for(Row r:result)
						{
							setback[i] = r.getString(0);
							i = i+1;
						}
						//send information back to server
						StringBuilder s = new StringBuilder(); 
						Date time=new Date();
						System.out.println( "________" + time.toString() + "________");
						for(i=0;i<numOfQuery+1;++i)
						{
							s.append(setback[i]);
							s.append(",");
							System.out.print(setback[i]+",");
						}
						returnQuery.add(1);
						out.writeUTF(s.toString());
						out.flush();
					}
					out.close();
					socket.close();
				}else{
					System.out.println("Receive nothing.");
				}
			}
		});
		
        jssc.start();
        jssc.awaitTermination();
		
	}

}

