package com.eftimoff.mapreduce.organization.binning;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.eftimoff.mapreduce.utils.MRDPUtils;

public class BinningTags extends Configured implements Tool {

	public static class BinningMapper extends
			Mapper<Object, Text, Text, NullWritable> {
		private MultipleOutputs<Text, NullWritable> mos = null;

		protected void setup(Context context) {
			// Create a new MultipleOutputs using the context object
			mos = new MultipleOutputs<Text, NullWritable>(context);
		}

		protected void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			Map<String, String> parsed = MRDPUtils.transformXmlToMap(value
					.toString());
			String rawtags = parsed.get("Tags");
			if (rawtags == null) {
				return;
			}
			// Tags are delimited by ><. i.e. <tag1><tag2><tag3>
			String[] tagTokens = StringEscapeUtils.unescapeHtml(rawtags).split(
					"><");
			// For each tag
			for (String tag : tagTokens) {
				// Remove any > or < from the token
				String groomed = tag.replaceAll(">|<", "").toLowerCase();
				// If this tag is one of the following, write to the named bin
				if (groomed.equalsIgnoreCase("hadoop")) {
					mos.write("bins", value, NullWritable.get(), "hadoop-tag");
				}
				if (groomed.equalsIgnoreCase("schrader")) {
					mos.write("bins", value, NullWritable.get(), "schrader-tag");
				}
				if (groomed.equalsIgnoreCase("presta")) {
					mos.write("bins", value, NullWritable.get(), "presta-tag");
				}
				if (groomed.equalsIgnoreCase("innertube")) {
					mos.write("bins", value, NullWritable.get(),
							"innertube-tag");
				}
			}
			// Get the body of the post
			String post = parsed.get("Body");
			// If the post contains the word "hadoop", write it to its own bin
			if (post != null && post.toLowerCase().contains("hadoop")) {
				mos.write("bins", value, NullWritable.get(), "hadoop-post");
			}

		}

		protected void cleanup(Context context) throws IOException,
				InterruptedException {
			// Close multiple outputs!
			mos.close();
		}
	}

	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = new Configuration();
		GenericOptionsParser parser = new GenericOptionsParser(conf, args);
		String[] otherArgs = parser.getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: BinningTags <in> <out>");
			ToolRunner.printGenericCommandUsage(System.err);
			System.exit(2);
		}
		Job job = new Job(conf, "Binning Tags");
		job.setJarByClass(BinningTags.class);
		// Configure the MultipleOutputs by adding an output called "bins"
		// With the proper output format and mapper key/value pairs
		MultipleOutputs.addNamedOutput(job, "bins", TextOutputFormat.class,
				Text.class, NullWritable.class);
		// Enable the counters for the job
		// If there are a significant number of different named outputs, this
		// should be disabled
		MultipleOutputs.setCountersEnabled(job, true);
		// Map-only job
		job.setNumReduceTasks(0);
		job.setMapperClass(BinningMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(NullWritable.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(NullWritable.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		boolean success = job.waitForCompletion(true);
		return success ? 0 : 1;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new BinningTags(), args);
		System.exit(res);
	}
}