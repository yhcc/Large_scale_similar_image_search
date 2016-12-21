package com.bigdata.spark;

import java.io.Serializable;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.linalg.Vectors;

/*
 * Used to encode image data
 */
public class imageClass implements Serializable{
	private String id;
	
	private int featureDimension=3;
	private Vector features;
	
	public Vector getFeatures()
	{
		return this.features;
	}
	public String getId()
	{
		return id;
	}

	public void setFeatures(double[] feature)
	{
		if(feature.length!=featureDimension)
			this.features = null;
		else
			this.features = Vectors.dense(feature);
	}
	public void setId(String Id)
	{
		this.id = Id;
	}
	
	
}
