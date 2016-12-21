package com.bigdata.spark;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.spark.api.java.function.ForeachFunction;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.linalg.Vectors;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

public class selfKMeansUtils implements Serializable {


	public static Vector findKClosest(final int k, Dataset<Row> centers,final Vector point)
	{
		final List<Point> result = new ArrayList<Point>(k);
		final List<Integer> index = new ArrayList<Integer>(1);
		final List<Double> maxDistance = new ArrayList<Double>(1);
		maxDistance.add(Double.MAX_VALUE);
		index.add(0);
		final double[] clusters = new double[k];
		
		centers.foreach(new ForeachFunction<Row>(){
			public void call(Row arg0) throws Exception {
				// TODO Auto-generated method stub
				
				double distance = Vectors.sqdist((Vector) arg0.get(1), point);
				if(result.size()<=k)
				{
					if(distance>maxDistance.get(0))
					{
						maxDistance.set(0,distance);
						result.add(new Point(arg0.getInt(0),distance));
						index.set(0,result.size());
					}
					else
					{
						result.add(new Point(arg0.getInt(0),distance));
					}
				}
				else
				{
					if(distance<maxDistance.get(0))
					{
						result.set(index.get(0), new Point(arg0.getInt(0),distance));
						//find the max distance in list and its index
						double max=0.0;
						int idx=0;
						for(int i=0;i<result.size();++i)
						{
							if(result.get(i).getDistance()>max)
							{
								max = result.get(i).getDistance();
								idx = i;
							}		
						}
						index.set(0,idx);
						maxDistance.add(0,max);
					}
				}
				System.out.println(result.size());
			}
		});
		
		System.out.println(result.size());
		for(int i=0;i<result.size();++i)
		{
			clusters[i] = result.get(i).getClusterId();
		}
		
		return Vectors.dense(clusters);
	}
	
	
	
}
