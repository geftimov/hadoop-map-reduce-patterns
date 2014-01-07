package com.eftimoff.mapreduce.organization.partitioning;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.eftimoff.mapreduce.utils.MRDPUtils;

public class LastAccessDate extends Configured implements Tool {

	public static class LastAccessDateMapper extends
			Mapper<Object, Text, IntWritable, Text> {
		// This object will format the creation date string into a Date object
		private final static SimpleDateFormat frmt = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSS");
		private IntWritable outkey = new IntWritable();

		protected void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			Map<String, String> parsed = MRDPUtils.transformXmlToMap(value
					.toString());
			// Grab the last access date
			String strDate = parsed.get("LastAccessDate");

			if (strDate == null) {
				return;
			}
			// Parse the string into a Calendar object
			Calendar cal = Calendar.getInstance();
			try {
				cal.setTime(frmt.parse(strDate));
				outkey.set(cal.get(Calendar.YEAR));
				// Write out the year with the input value
				context.write(outkey, value);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}

	public static class LastAccessDatePartitioner extends
			Partitioner<IntWritable, Text> implements Configurable {
		private static final String MIN_LAST_ACCESS_DATE_YEAR = "min.last.access.date.year";
		private Configuration conf = null;
		private int minLastAccessDateYear = 0;

		public int getPartition(IntWritable key, Text value, int numPartitions) {
			return key.get() - minLastAccessDateYear;
		}

		public Configuration getConf() {
			return conf;
		}

		public void setConf(Configuration conf) {
			this.conf = conf;
			minLastAccessDateYear = conf.getInt(MIN_LAST_ACCESS_DATE_YEAR, 0);
		}

		public static void setMinLastAccessDate(Job job,
				int minLastAccessDateYear) {
			job.getConfiguration().setInt(MIN_LAST_ACCESS_DATE_YEAR,
					minLastAccessDateYear);
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
		job.setJarByClass(LastAccessDate.class);
		// Set custom partitioner and min last access date
		job.setPartitionerClass(LastAccessDatePartitioner.class);
		LastAccessDatePartitioner.setMinLastAccessDate(job, 2010);
		// Last access dates span between 2008-2011, or 4 years
		job.setNumReduceTasks(4);
		job.setMapperClass(LastAccessDateMapper.class);
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
		int res = ToolRunner.run(new Configuration(), new LastAccessDate(),
				args);
		System.exit(res);
	}
}
