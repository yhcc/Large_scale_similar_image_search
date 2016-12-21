Large-scale realtime similar image search
This is the code for the final project for Columbia E6893 big data analytics class. Our project is to use Spark to implement a online searching framework based on Spark. Java codes can be built by using pom.xml file.

To run this codes, you have to prepare input file for Hadoop. Input file should be a txt file includes the path for all images. Hadoop mapper will read images from these paths and convert them into feature vectors.

1. convert images into feature vectors
hadoop jar project2-0.0.1-SNAPSHOT-shaded.jar com.bigdata.hadoop.readToVectors file:/Users/yh/Desktop/bigdata/project/imagePath.txt file:/Users/yh/Desktop/bigdata/project/imageFeatures
The first argument is path from images path file, the second argument is the path to save results.

After the extraction, Spark KMeans is used to group these features into several clusters.
2. to run kmeans spark
spark-submit --class com.bigdata.spark.KMeansForImage project2-0.0.1-SNAPSHOT-shaded.jar local[4] 75 20 file:///Users/yh/Desktop/bigdata/project/imageFeatures file:///Users/yh/Desktop/bigdata/project/imageFeaturesClusters file:///Users/yh/Desktop/bigdata/project/model
The meaning of these paramters:
running mode/number of cluster centers/max KMeans iteration/where to read image feature data/where to save image features with cluster/where to save model

#When all data is ready, running 3 and 4.
3. to run onlineSearching spark
spark-submit --class com.bigdata.spark.onlineSearching project2-0.0.1-SNAPSHOT-shaded.jar local[4] onlineSearching 10 localhost 9999 localhost 10000 3 9 file:///Users/yh/Desktop/bigdata/project/model/data file:///Users/yh/Desktop/bigdata/project/imageFeaturesClusters
The meaning of these parameters:
running model/app name/Durations/receiver host/receiver port/result sender host/result sender port/number of clusters to search/number of queries to return/where to read cluster center data/where to read image features

4.run python receiver.ipython and sender.ipython
just open jupyter notebook, and run one by one. Sender should be running before spark streaming is working.




