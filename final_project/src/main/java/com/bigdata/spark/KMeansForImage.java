package com.bigdata.spark;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.ml.clustering.KMeans;
import org.apache.spark.ml.clustering.KMeansModel;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.linalg.Vectors;

/*
 * This java class is used to cluster each image into different clusters. And save the result.
 */
public class KMeansForImage {
	public static void main(String[] args) throws IOException
	{
		/*
		 * spark-submit --class com.bigdata.spark.KMeansForImage 
		 * target/project2-0.0.1-SNAPSHOT-shaded.jar local[4] 100 20
		 * file:///Users/yh/Desktop/bigdata/code/project2/imageFeature 
		 * file:///Users/yh/Desktop/bigdata/code/project2/imageFeaturesClusters
		 * file:///Users/yh/Desktop/bigdata/code/project2/model
		 */
		String mode = args[0];
		//number of clusters
		final int k=Integer.parseInt(args[1]);
		//maximum iteration
		final int maxIteration = Integer.parseInt(args[2]);
		//where to read image features data
		final String imageFeatures = args[3];
		//where to save image features labelled with cluster
		final String imageFeaturesClusters = args[4];
		//where to save the kmeans model
		final String modelPath = args[5];
		
		
	    SparkSession spark = SparkSession
	      .builder()
	      .appName("KMeans")
	      .master(mode)
	      .getOrCreate();
	    
	    //inferred from arrayToVector
	    final int featuresDimension=4096;
	    
	    
	    List<sparseVector1> list = new ArrayList<sparseVector1>();
	    JavaRDD<sparseVector1> data = spark.sparkContext().textFile(imageFeatures, 1).toJavaRDD()
	    		.map(new Function<String,sparseVector1>(){
					@Override
					public sparseVector1 call(String s) throws Exception {
						// TODO Auto-generated method stub
						sparseVector1 v = new sparseVector1();
						String[] ss = s.split("\t");
						v.setId(ss[0]);
						ss = ss[1].split(",");
						int[] indexes = new int[ss.length];
						double[] values = new double[ss.length];
						for(int i=0;i<ss.length;++i)
						{
							indexes[i] = Integer.parseInt(ss[i].split(":")[0]);
							values[i] = Double.parseDouble(ss[i].split(":")[1]);
						}
						v.setFeatures(Vectors.sparse(featuresDimension, indexes, values));
						return v;
					}
	    		}); 
	   
	   Dataset<Row> imageDF = spark.createDataFrame(data, sparseVector1.class);
	   
	   //This part is for KMeans;
	   KMeans kmeans = new KMeans().setK(k).setFeaturesCol("features").setMaxIter(maxIteration).setPredictionCol("cluster");
	   KMeansModel model = kmeans.fit(imageDF);
	   
	   Dataset<Row> result = model.transform(imageDF);
	   
	   //for right now save as parquet
	   //result.show();
	   result.write().save(imageFeaturesClusters);
	   System.out.println(model.computeCost(imageDF));
	   
	   //get the number of elements in each clusters
	   JavaRDD<Integer> clusters = result.select(new Column("cluster")).toJavaRDD().map(new Function<Row,Integer>(){
		@Override
		public Integer call(Row r) throws Exception {
			// TODO Auto-generated method stub
			return r.getInt(0);
		}
	   });
	   
	   List<Integer> cluster_sizes = clusters.collect();
	   
	   long max = 0;
	   int index = 0;
	   System.out.println("Number of elements in each cluster:");
	   
	   for(int i=0;i<k;++i)
	   {
		   int freq = Collections.frequency(cluster_sizes, i);
		   System.out.print(String.valueOf(i) + ":" + freq + '\t');
		   if(freq>max)
		   {
			   max = freq;
			   index = i;
		   }
	   }
	   System.out.println();
	   System.out.println(String.format("Maximum clusters is %d with %d elements", index,max));
	   
	   
	   //save model
	   model.save(modelPath);
	   /*
	   Vector[] centers = model.clusterCenters();
	   System.out.println("Cluster Centers: ");
	   for (Vector center: centers) {
	     System.out.println(center.toSparse());
	   }
	   */
	}
}
