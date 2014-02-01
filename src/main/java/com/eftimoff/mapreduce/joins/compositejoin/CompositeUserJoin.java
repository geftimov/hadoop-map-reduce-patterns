package com.eftimoff.mapreduce.joins.compositejoin;

import java.io.IOException;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.KeyValueTextInputFormat;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.mapred.join.CompositeInputFormat;
import org.apache.hadoop.mapred.join.TupleWritable;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class CompositeUserJoin extends Configured implements Tool {

	public static class CompositeMapper extends MapReduceBase implements
			Mapper<Text, TupleWritable, Text, Text> {
		public void map(Text key, TupleWritable value, OutputCollector<Text, Text> output,
				Reporter reporter) throws IOException {
			// Get the first two elements in the tuple and output them
			output.collect((Text) value.get(0), (Text) value.get(1));
		}
	}

	@Override
	public int run(String[] args) throws Exception {
		if (args.length != 4) {
			printUsage();
		}
		Path userPath = new Path(args[0]);
		Path commentPath = new Path(args[1]);
		Path outputDir = new Path(args[2]);
		String joinType = args[3];
		JobConf conf = new JobConf("CompositeJoin");
		conf.setJarByClass(CompositeUserJoin.class);
		conf.setMapperClass(CompositeMapper.class);
		conf.setNumReduceTasks(0);
		// Set the input format class to a CompositeInputFormat class.
		// The CompositeInputFormat will parse all of our input files and output
		// records to our mapper.
		conf.setInputFormat(CompositeInputFormat.class);
		// The composite input format join expression will set how the records
		// are going to be read in, and in what input format.
		conf.set("mapred.join.expr", CompositeInputFormat.compose(joinType,
				KeyValueTextInputFormat.class, userPath, commentPath));
		TextOutputFormat.setOutputPath(conf, outputDir);
		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);
		RunningJob job = JobClient.runJob(conf);
		while (!job.isComplete()) {
			Thread.sleep(1000);
		}
		return job.isSuccessful() ? 0 : 1;
	}

	private void printUsage() {
		System.err.println("Usage: ReduceSideJoin <user_in> <comments_in> <out> <join_type>");
		ToolRunner.printGenericCommandUsage(System.err);
		System.exit(2);
	}

}
