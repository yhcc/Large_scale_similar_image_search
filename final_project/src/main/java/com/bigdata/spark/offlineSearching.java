package com.bigdata.spark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.api.java.function.ReduceFunction;
import org.apache.spark.api.java.function.VoidFunction;
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

import scala.collection.Seq;
import scala.collection.mutable.ArraySeq;

import org.apache.spark.sql.Column;


/*
 * Spark online searching part
 */
public class offlineSearching{
	public static void main(String[] args)
	{
		//!!!!!!!!!should be set according to settings
		SparkConf conf = new SparkConf().setMaster("local[3]").setAppName("onlinesearching");
		
		JavaSparkContext jsc = new JavaSparkContext(conf);
		SparkSession spark = SparkSession.builder().config(conf).getOrCreate();
		
		//number of kmeans clusters to use
		final int k = 2;
		final int featuresDimension = 3;
		final int numOfQuery = 2;
		//read cluster center data
		
		Dataset<Row> centers = spark.read().format("parquet").load("file:///Users/yh/Desktop/bigdata/code/project2/model_test/data");
		centers.persist();
		final Broadcast<Dataset<Row>> centersBroadCast = jsc.broadcast(centers);
		
		Vector v11 = Vectors.sparse(6,new int[]{0,2,3},new double[]{3,1,5});
		Vector v22 = Vectors.sparse(6, new int[]{1,2,3,5},new double[]{2,1,3,3});
		sparseVector[] vs = {new sparseVector("11111",v11),new sparseVector("22222",v22)};
		
		List<sparseVector> temp = Arrays.asList(vs);
		
		JavaRDD<sparseVector> data = jsc.parallelize(temp);
		
		final Encoder<Center> centerEncoder = Encoders.bean(Center.class);
		
		//System.out.println(data.collect());
		
		data = data.map(new Function<sparseVector, sparseVector>(){
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
		
		System.out.println(data.collect());
		
		final Dataset<Row> imageDF = spark.read().load("file:///Users/yh/Desktop/bigdata/code/project2/output2_test");
		imageDF.persist();
		//compare image data fall in the given clusters
		imageDF.show();
		
		//rdd cannot be used in foreach, so first collect data back to master, then do a loop
		List<sparseVector> data1 = data.collect();
		
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
			
		}
		
		
		
		jsc.stop();
	}

}

