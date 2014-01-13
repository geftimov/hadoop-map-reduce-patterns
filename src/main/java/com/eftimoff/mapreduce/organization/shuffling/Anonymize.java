package com.eftimoff.mapreduce.organization.shuffling;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.eftimoff.mapreduce.utils.MRDPUtils;

public class Anonymize extends Configured implements Tool {

	public static class AnonymizeMapper extends
			Mapper<Object, Text, IntWritable, Text> {
		private IntWritable outkey = new IntWritable();
		private Random rndm = new Random();
		private Text outvalue = new Text();

		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			Map<String, String> parsed = MRDPUtils.transformXmlToMap(value
					.toString());
			if (parsed.size() > 0) {
				StringBuilder bldr = new StringBuilder();
				// Create the start of the record
				bldr.append("<row ");
				// For each XML attribute
				for (Entry<String, String> entry : parsed.entrySet()) {
					// If it is a user ID or row ID, ignore it
					if (entry.getKey().equals("UserId")
							|| entry.getKey().equals("Id")) {
					} else if (entry.getKey().equals("CreationDate")) {
						// If it is a CreationDate, remove the time from the
						// date
						// i.e., anything after the 'T' in the value
						bldr.append(entry.getKey()
								+ "=\""
								+ entry.getValue().substring(0,
										entry.getValue().indexOf('T')) + "\" ");
					} else {
						// Otherwise, output the attribute and value as is
						bldr.append(entry.getKey() + "=\"" + entry.getValue()
								+ "\" ");
					}
				}
				// Add the /> to finish the record
				bldr.append("/>");
				// Set the sort key to a random value and output
				outkey.set(rndm.nextInt());
				outvalue.set(bldr.toString());
				context.write(outkey, outvalue);
			}
		}
	}

	public static class ValueReducer extends
			Reducer<IntWritable, Text, Text, NullWritable> {
		protected void reduce(IntWritable key, Iterable<Text> values,
				Context context) throws IOException, InterruptedException {
			for (Text t : values) {
				context.write(t, NullWritable.get());
			}
		}
	}

	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = new Configuration();
		GenericOptionsParser parser = new GenericOptionsParser(conf, args);
		String[] otherArgs = parser.getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: LastAccessDate <in> <out>");
			ToolRunner.printGenericCommandUsage(System.err);
			System.exit(2);
		}
		Job job = new Job(conf, "LastAccess Date");
		job.setJarByClass(Anonymize.class);
		job.setNumReduceTasks(4);
		job.setMapperClass(AnonymizeMapper.class);
		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(Text.class);
		job.setReducerClass(ValueReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(NullWritable.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		boolean success = job.waitForCompletion(true);
		return success ? 0 : 1;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new Anonymize(), args);
		System.exit(res);
	}
}
