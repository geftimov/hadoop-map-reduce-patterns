package com.eftimoff.mapreduce.summarization.counters;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.eftimoff.mapreduce.utils.MRDPUtils;

public class CountNumUsersByState extends Configured implements Tool {

	public static class CountNumUsersByStateMapper extends
			Mapper<Object, Text, NullWritable, NullWritable> {
		public static final String STATE_COUNTER_GROUP = "State";
		public static final String UNKNOWN_COUNTER = "Unknown";
		public static final String NULL_OR_EMPTY_COUNTER = "Null or Empty";
		private String[] statesArray = new String[] { "AL", "AK", "AZ", "AR",
				"CA", "CO", "CT", "DE", "FL", "GA", "HI", "ID", "IL", "IN",
				"IA", "KS", "KY", "LA", "ME", "MD", "MA", "MI", "MN", "MS",
				"MO", "MT", "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND",
				"OH", "OK", "OR", "PA", "RI", "SC", "SF", "TN", "TX", "UT",
				"VT", "VA", "WA", "WV", "WI", "WY" };
		private HashSet<String> states = new HashSet<String>(
				Arrays.asList(statesArray));

		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			Map<String, String> parsed = MRDPUtils.transformXmlToMap(value
					.toString());
			// Get the value for the Location attribute
			String location = parsed.get("Location");
			// Look for a state abbreviation code if the
			// location is not null or empty
			if (location != null && !location.isEmpty()) {
				// Make location uppercase and split on white space
				String[] tokens = location.toUpperCase().split("\\s");
				// For each token
				boolean unknown = true;
				for (String state : tokens) {
					// Check if it is a state
					if (states.contains(state)) {
						// If so, increment the state's counter by 1
						// and flag it as not unknown
						context.getCounter(STATE_COUNTER_GROUP, state)
								.increment(1);
						unknown = false;
						break;
					}
				}
				// If the state is unknown, increment the UNKNOWN_COUNTER
				// counter
				if (unknown) {
					context.getCounter(STATE_COUNTER_GROUP, UNKNOWN_COUNTER)
							.increment(1);
				}
			} else {
				// If it is empty or null, increment the
				// NULL_OR_EMPTY_COUNTER counter by 1
				context.getCounter(STATE_COUNTER_GROUP, NULL_OR_EMPTY_COUNTER)
						.increment(1);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(),
				new CountNumUsersByState(), args);
		System.exit(res);
	}

	@SuppressWarnings("deprecation")
	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args)
				.getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: NumberOfUsersByState <in> <out>");
			ToolRunner.printGenericCommandUsage(System.err);
			System.exit(2);
		}

		Job job = new Job(conf, "StackOverflow Number of Users by State");
		job.setJarByClass(CountNumUsersByState.class);
		job.setMapperClass(CountNumUsersByStateMapper.class);
		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(NullWritable.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		Path outputDir = new Path(otherArgs[1]);
		FileOutputFormat.setOutputPath(job, outputDir);
		boolean success = job.waitForCompletion(true);

		if (success) {
			for (Counter counter : job.getCounters().getGroup(
					CountNumUsersByStateMapper.STATE_COUNTER_GROUP)) {
				System.out.println(counter.getDisplayName() + "\t"
						+ counter.getValue());
			}
		}

		FileSystem.get(conf).delete(outputDir);

		return success ? 0 : 1;
	}
}
