package com.bigdata.image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;



public class Test_image {
	public static void main(String[] args) throws IOException
	{
		int binSize=16;
		int digits = (256/binSize);
		int featuresDimension = digits*digits*digits;
		BufferedImage image = null;
		image = ImageIO.read(new File("/Users/yh/demo_images/0.png"));
		int[][] pixels = convertToArray.convertTo2DWithoutUsingGetRGB(image);
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
					s.append(String.valueOf(k) + ":" + 
							String.valueOf(features[k]/(float)totalPixels) + ",");
				}
			}
			System.out.println(s.toString());
		}
		
	}
}
