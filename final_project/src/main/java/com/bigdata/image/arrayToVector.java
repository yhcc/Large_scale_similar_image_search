package com.bigdata.image;

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class arrayToVector {
	public static void main(String[] args)
	{
		
	}
	
	/*
	 * Right now using 16*16*16 RGB cube to create index. 
	 */
	public static String getSparseVector(Path path,FileSystem fs) throws IOException
	{
		//binSize used to represent the size of color cube
		int binSize=16;
		int digits = (256/binSize);
		int featuresDimension = digits*digits*digits;
		int[][] pixels = convertToArray.convertToIntArray(path,fs);
		if(pixels!=null)
		{
			int totalPixels = pixels.length*pixels[0].length;
			int[] features = new int[featuresDimension];
			int order = 0;
			for(int[] i:pixels){
				for(int j: i)
				{
					 order += ((j>>16)&0xFF)/binSize;
					 order += ((j>>8)&0xFF)/binSize*digits;
					 order += ((j>>0)&0xFF)/binSize*digits*digits;
					 features[order] += 1;
					 order = 0;
				}
			}
			StringBuilder s = new StringBuilder();
			for(int k=0;k<featuresDimension;++k)
			{
				if(features[k]!=0)
				{
					s.append(String.valueOf(k) + ":" + String.valueOf(features[k]/(float)totalPixels)+",");
				}
			}
			return s.toString();
		}
		else
			return null;
	}
	
	/*
	 * Right now using 16*16*16 RGB cube to create index. 
	 */
	public static String getSparseVector(File file) throws IOException
	{
		//binSize used to represent the size of color cube
		int binSize=16;
		int digits = (256/binSize);
		int featuresDimension = digits*digits*digits;
		int[][] pixels = convertToArray.convertToIntArray(file);
		if(pixels!=null)
		{
			int totalPixels = pixels.length*pixels[0].length;
			int[] features = new int[featuresDimension];
			int order = 0;
			for(int[] i:pixels){
				for(int j: i)
				{
					 order += ((j>>16)&0xFF)/binSize;
					 order += ((j>>8)&0xFF)/binSize*digits;
					 order += ((j>>0)&0xFF)/binSize*digits*digits;
					 features[order] += 1;
					 order = 0;
				}
			}
			StringBuilder s = new StringBuilder();
			for(int k=0;k<featuresDimension;++k)
			{
				if(features[k]!=0)
				{
					s.append(String.valueOf(k) + ":" + String.valueOf(features[k]/(float)totalPixels)+",");
				}
			}
			return s.toString();
		}
		else
			return null;
	}
}
