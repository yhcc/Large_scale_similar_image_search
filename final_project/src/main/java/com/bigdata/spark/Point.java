package com.bigdata.spark;

import java.io.Serializable;
import java.util.Comparator;

public class Point implements Serializable{
	private int clusterId;
	private double distance;
	public Point(int clusterId,double distance)
	{
		this.clusterId = clusterId;
		this.distance = distance;
	}
	
	public int getClusterId()
	{
		return this.clusterId;
	}
	
	public double getDistance()
	{
		return this.distance;
	}
	
	public String toString()
	{
		return getClusterId() + "," + getDistance();
	}
	
}
