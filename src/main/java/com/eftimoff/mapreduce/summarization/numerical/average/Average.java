package com.eftimoff.mapreduce.summarization.numerical.average;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
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

public class Average extends Configured implements Tool {
	public static class AverageMapper extends Mapper<Object, Text, IntWritable, CountAverageTuple> {
		private IntWritable outHour = new IntWritable();
		private CountAverageTuple outCountAverage = new CountAverageTuple();
		private final static SimpleDateFormat frmt = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSS");

		@SuppressWarnings("deprecation")
		public void map(Object key, Text value, Context context) throws IOException,
				InterruptedException {
			Map<String, String> parsed = MRDPUtils.transformXmlToMap(value.toString());
			// Grab the "CreationDate" field,
			// since it is what we are grouping by
			String strDate = parsed.get("CreationDate");
			// Grab the comment to find the length
			String text = parsed.get("Text");
			if (strDate == null || text == null) {
				return;
			}
			// get the hour this comment was posted in
			Date creationDate;
			try {
				creationDate = frmt.parse(strDate);
			} catch (ParseException e) {
				return;
			}
			outHour.set(creationDate.getHours());
			// get the comment length
			outCountAverage.setCount(1);
			outCountAverage.setAverage(text.length());
			// write out the hour with the comment length
			context.write(outHour, outCountAverage);
		}
	}

	public static class AverageReducer extends
			Reducer<IntWritable, CountAverageTuple, IntWritable, CountAverageTuple> {
		private CountAverageTuple result = new CountAverageTuple();

		public void reduce(IntWritable key, Iterable<CountAverageTuple> values, Context context)
				throws IOException, InterruptedException {
			float sum = 0;
			float count = 0;
			// Iterate through all input values for this key
			for (CountAverageTuple val : values) {
				sum += val.getCount() * val.getAverage();
				count += val.getCount();
			}
			result.setCount(count);
			result.setAverage(sum / count);
			context.write(key, result);
		}
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new Average(), args);
		System.exit(res);
	}

	@Override
	public int run(String[] arg0) throws Exception {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, arg0).getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: Average <in> <out>");
			System.exit(2);
		}
		Job job = new Job(conf, "StackOverflow Comment Average");
		job.setJarByClass(Average.class);
		job.setMapperClass(AverageMapper.class);
		job.setCombinerClass(AverageReducer.class);
		job.setReducerClass(AverageReducer.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(CountAverageTuple.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		boolean success = job.waitForCompletion(true);

		return success ? 0 : 1;
	}
}
