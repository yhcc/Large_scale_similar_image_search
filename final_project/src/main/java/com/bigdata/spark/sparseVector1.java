package com.bigdata.spark;

import java.io.Serializable;

import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.linalg.Vectors;

/*
 * Used for KMeans
 */


public class sparseVector1 implements Serializable{
	private String id;
	private Vector features;
	
	public sparseVector1(String id,Vector features)
	{
		this.features = features;
		this.id = id;
	}
	public sparseVector1()
	{
		
	}
	
	public Vector getFeatures()
	{
		return this.features;
	}
	public String getId()
	{
		return id;
	}

	public void setFeatures(Vector feature)
	{
		this.features = feature;
	}
	public void setId(String Id)
	{
		this.id = Id;
	}
}
