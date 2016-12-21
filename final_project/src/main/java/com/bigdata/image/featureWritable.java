package com.bigdata.image;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;
/*
 * This class is feature writable class, for right now, it only includes numerical values
 */

public class featureWritable implements Writable{

	//This is feature dimension.
	private int featureDimension = 3;
	
	public float[] features = new float[featureDimension];

	private int featureDimeansion;

	//construction function
	public featureWritable(float[] value)
	{
		if(value.length!=featureDimension)
			features=null;
		else
			this.features = value.clone();
	}
	public featureWritable()
	{
		for(int i=0;i<featureDimension;++i)
		{
			this.features[i]=0;
		}
	}
	
	public void readFields(DataInput in) throws IOException {
		// TODO Auto-generated method stub
		for(int i=0;i<featureDimension;++i)
		{
			features[i] = in.readFloat();
		}
	}

	public void write(DataOutput out) throws IOException {
		// TODO Auto-generated method stub
		for(float i:features)
			out.writeFloat(i);
	}
	
	public String toString(){
		StringBuilder s = new StringBuilder();
		for(float i:features)
		{
			s.append(i);
			s.append(',');
		}
		return s.toString();
	}
	
	public boolean equals(Object o)
	{
		if(!(o instanceof featureWritable))
			return false;
		featureWritable s = (featureWritable) o;
		boolean flag = true;
		for(int i=0;i<featureDimeansion;++i)
		{
			if(s.features[i]!=features[i])
			{
				flag=false;
				break;
			}
		}
		return flag;	
	}
	
}
