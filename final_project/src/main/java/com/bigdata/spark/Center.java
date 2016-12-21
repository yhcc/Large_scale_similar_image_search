package com.bigdata.spark;

import java.io.Serializable;

public class Center implements Serializable{
	private String clusterId;
	private double distance;
	
	public Center(int clusterId,double distance)
	{
		this.clusterId = String.valueOf(clusterId);
		this.distance = distance;
	}
	
	public Center()
	{
	}
	public Center(String clusterId,double distance)
	{
		this.clusterId = clusterId;
		this.distance = distance;
	}
	
	public String getClusterId()
	{
		return clusterId;
	}
	
	public double getDistance()
	{
		return distance;
	}
	
	public void setClusterId(String clusterId)
	{
		this.clusterId = clusterId;
	}
	
	public void setDistance(double distance)
	{
		this.distance = distance;
	}
	
	public Center add(Center that)
	{
		StringBuilder s = new StringBuilder();
		s.append(this.clusterId);
		s.append(',');
		s.append(that.clusterId);
		this.clusterId = s.toString();
		return this;
	}
	
	
}
