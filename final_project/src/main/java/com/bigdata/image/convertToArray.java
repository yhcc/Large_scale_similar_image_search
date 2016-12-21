package com.bigdata.image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class convertToArray {
	public static void main(String args[])
	{
		/*
		StringBuilder s=new StringBuilder();
		int[] test={1,3,4,5,3};
		for(int i:test)
		{
			s.append(i);
			s.append(',');
		}
		System.out.println(s);
		*/
		BufferedImage image = null;
		
		int[][] pixels = null;
		
		try {
			image = ImageIO.read(new File("/Users/yh/demo_images/0.png"));
			pixels = convertTo2DWithoutUsingGetRGB(image);
			for(int[] i:pixels)
				for(int j:i)
					System.out.print((j >> 16) & 0xFF);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	//function takes in an image, then convert to a vector
	public static int[][] convertToIntArray(Path path,FileSystem fs) throws IOException
	{
	/*
	 * param:
	 * @param: path.
	 * @param: fileSystem.
	 */
		//first find the suffix to judge its type
		try {
			BufferedImage image = ImageIO.read(fs.open(path));
			if(image==null)
				return null;
			return convertTo2DWithoutUsingGetRGB(image);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//!!!!!!!!!!!!!!!need to write into logs
			System.out.println(String.format("!!!!Fail to read Image:",path.toString()));
			return null;
		}
	}
	
	//function takes in an image, then convert to a vector
	public static int[][] convertToIntArray(File file) throws IOException
	{
	/*
	 * param:
	 * file: File object. type:File	
	 */
		//first find the suffix to judge its type
		try {
			BufferedImage image = ImageIO.read(file);
			if(image==null)
				return null;
			return convertTo2DWithoutUsingGetRGB(image);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//!!!!!!!!!!!!!!!need to write into logs
			System.out.println(String.format("!!!!Fail to read Image:", file.toString()));
			return null;
		}
	}
	
	/*
	 * function to convert image into int array with the same shape as (height,width)
	 *The data structure in each pixel
	 *00000000 00000000 00000000 11111111
	 *^ Alpha  ^Red     ^Green   ^Blue
	 *To get RGB result for each pixel. 
	 *		Alpha:(pixel[i][j] >> 24) & 0xFF;
	 *		Red:(pixel[i][j] >> 16) & 0xFF;
	 *		Green:(pixel[i][j] >> 8) & 0xFF;
	 *		Blue:pixel[i][j] >> 0) & 0xFF;
	*/
   static int[][] convertTo2DWithoutUsingGetRGB(BufferedImage image) {

	      final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
	      final int width = image.getWidth();
	      final int height = image.getHeight();
	      final boolean hasAlphaChannel = image.getAlphaRaster() != null;

	      int[][] result = new int[height][width];
	      if (hasAlphaChannel) {
	         final int pixelLength = 4;
	         for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
	            int argb = 0;
	            argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
	            argb += ((int) pixels[pixel + 1] & 0xff); // blue
	            argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
	            argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
	            result[row][col] = argb;
	            col++;
	            if (col == width) {
	               col = 0;
	               row++;
	            }
	         }
	      } else {
	         final int pixelLength = 3;
	         for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
	            int argb = 0;
	            argb += -16777216; // 255 alpha
	            argb += ((int) pixels[pixel] & 0xff); // blue
	            argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
	            argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
	            result[row][col] = argb;
	            col++;
	            if (col == width) {
	               col = 0;
	               row++;
	            }
	         }
	      }

	      return result;
	   }
   /*
    * function to convert image into int array with the same shape as (height,width)
    * slower than previous one.
	 *The data structure in each pixel
	 *00000000 00000000 00000000 11111111
	 *^ Alpha  ^Red     ^Green   ^Blue
	 *To get RGB result for each pixel. 
	 *		Alpha:(pixel[i][j] >> 24) & 0xFF;
	 *		Red:(pixel[i][j] >> 16) & 0xFF;
	 *		Green:(pixel[i][j] >> 8) & 0xFF;
	 *		Blue:pixel[i][j] >> 0) & 0xFF;
	*/
   private static int[][] convertTo2DUsingGetRGB(BufferedImage image) {
	      int width = image.getWidth();
	      int height = image.getHeight();
	      int[][] result = new int[height][width];

	      for (int row = 0; row < height; row++) {
	         for (int col = 0; col < width; col++) {
	            result[row][col] = image.getRGB(col, row);
	         }
	      }

	      return result;
	   }
}
