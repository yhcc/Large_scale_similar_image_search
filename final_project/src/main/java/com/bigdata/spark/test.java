package com.bigdata.spark;

import java.util.ArrayList;
import java.util.List;

public class test {
	public static void main(String[] args)
	{
		String s = "/Users/yh/bigdatg.png";
		int index = s.lastIndexOf('/');
		System.out.println(s.substring(index+1));
	}
}
