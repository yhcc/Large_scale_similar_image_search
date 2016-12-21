package com.bigdata.spark;

import java.io.Serializable;

import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.linalg.Vectors;

/*
 * Used for online searching.
 */
public class sparseVector implements Serializable{
	private String id;
	private Vector features;
	private Vector clusters;
	
	public sparseVector(String id,Vector features)
	{
		this.features = features;
		this.id = id;
	}
	public sparseVector()
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
	
	public void setClusters(Vector clusters)
	{
		this.clusters = clusters;
	}
	public Vector getClusters()
	{
		return this.clusters;
	}
}
