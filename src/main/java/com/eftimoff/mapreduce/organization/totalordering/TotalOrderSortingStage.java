package com.eftimoff.mapreduce.organization.totalordering;

import java.io.IOException;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.InputSampler;
import org.apache.hadoop.mapreduce.lib.partition.TotalOrderPartitioner;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.eftimoff.mapreduce.utils.MRDPUtils;

public class TotalOrderSortingStage extends Configured implements Tool {

	public static class LastAccessMapper extends
			Mapper<Object, Text, Text, Text> {
		private Text outkey = new Text();

		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			Map<String, String> parsed = MRDPUtils.transformXmlToMap(value
					.toString());
			if (parsed.get("LastAccessDate") == null) {
				return;
			}
			outkey.set(parsed.get("LastAccessDate"));
			context.write(outkey, value);
		}
	}

	public static class ValuesReducer extends
			Reducer<Text, Text, Text, NullWritable> {
		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			for (Text t : values) {
				context.write(t, NullWritable.get());
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = new Configuration();
		Path inputPath = new Path(args[0]);
		Path partitionFile = new Path(args[1] + "_partitions.lst");
		Path outputStage = new Path(args[1] + "_staging");
		Path outputOrder = new Path(args[1]);
		// Configure job to prepare for sampling
		Job sampleJob = new Job(conf, "TotalOrderSortingStage");
		sampleJob.setJarByClass(TotalOrderSortingStage.class);
		// Use the mapper implementation with zero reduce tasks
		sampleJob.setMapperClass(LastAccessMapper.class);
		sampleJob.setNumReduceTasks(0);
		sampleJob.setOutputKeyClass(Text.class);
		sampleJob.setOutputValueClass(Text.class);
		TextInputFormat.setInputPaths(sampleJob, inputPath);
		// Set the output format to a sequence file
		sampleJob.setOutputFormatClass(SequenceFileOutputFormat.class);
		SequenceFileOutputFormat.setOutputPath(sampleJob, outputStage);
		// Submit the job and get completion code.
		int code = sampleJob.waitForCompletion(true) ? 0 : 1;
		if (code == 0) {
			Job orderJob = new Job(conf, "TotalOrderSortingStage");
			orderJob.setJarByClass(TotalOrderSortingStage.class);
			// Here, use the identity mapper to output the key/value pairs in
			// the SequenceFile
			orderJob.setMapperClass(Mapper.class);
			orderJob.setReducerClass(ValuesReducer.class);
			// Set the number of reduce tasks to an appropriate number for the
			// amount of data being sorted
			orderJob.setNumReduceTasks(10);
			// Use Hadoop's TotalOrderPartitioner class
			orderJob.setPartitionerClass(TotalOrderPartitioner.class);
			// Set the partition file
			TotalOrderPartitioner.setPartitionFile(orderJob.getConfiguration(),
					partitionFile);
			orderJob.setOutputKeyClass(Text.class);
			orderJob.setOutputValueClass(Text.class);
			// Set the input to the previous job's output
			orderJob.setInputFormatClass(SequenceFileInputFormat.class);
			SequenceFileInputFormat.setInputPaths(orderJob, outputStage);
			// Set the output path to the command line parameter
			TextOutputFormat.setOutputPath(orderJob, outputOrder);
			// Set the separator to an empty string
			orderJob.getConfiguration().set(
					"mapred.textoutputformat.separator", "");
			// Use the InputSampler to go through the output of the previous
			// job, sample it, and create the partition file
			InputSampler.writePartitionFile(orderJob,
					new InputSampler.RandomSampler(.001, 10000));
			// Submit the job
			code = orderJob.waitForCompletion(true) ? 0 : 2;
		}
		// Clean up the partition file and the staging directory
		FileSystem.get(new Configuration()).delete(partitionFile, false);
		FileSystem.get(new Configuration()).delete(outputStage, true);
		return code;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(),
				new TotalOrderSortingStage(), args);
		System.exit(res);
	}

}
