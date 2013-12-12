package com.eftimoff.mapreduce.summarization.numerical.medianstddev;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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
import org.apache.log4j.Logger;

import com.eftimoff.mapreduce.utils.MRDPUtils;

public class MedianStdDev extends Configured implements Tool {

	public static class MedianStdDevMapper extends Mapper<Object, Text, IntWritable, IntWritable> {
		private IntWritable outHour = new IntWritable();
		private IntWritable outCommentLength = new IntWritable();
		private final static SimpleDateFormat frmt = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSS");
		private static Logger logger = Logger.getRootLogger();

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
			logger.info(strDate + "\t" + text);
			// get the hour this comment was posted in
			Date creationDate;
			try {
				creationDate = frmt.parse(strDate);
			} catch (ParseException e) {
				return;
			}
			outHour.set(creationDate.getHours());
			// set the comment length
			outCommentLength.set(text.length());
			// write out the hours and the comment length
			context.write(outHour, outCommentLength);
		}
	}

	public static class MedianStdDevReducer extends
			Reducer<IntWritable, IntWritable, IntWritable, MedianStdDevTuple> {
		private MedianStdDevTuple result = new MedianStdDevTuple();
		private ArrayList<Float> commentLengths = new ArrayList<Float>();

		public void reduce(IntWritable key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {
			float sum = 0;
			float count = 0;
			commentLengths.clear();
			result.setStdDev(0);
			// Iterate through all input values for this key
			for (IntWritable val : values) {
				commentLengths.add((float) val.get());
				sum += val.get();
				++count;
			}
			// sort commentLengths to calculate median
			Collections.sort(commentLengths);
			// if commentLengths is an even value, average middle two elements
			if (count % 2 == 0) {
				result.setMedian((commentLengths.get((int) count / 2 - 1) + commentLengths
						.get((int) count / 2)) / 2.0f);
			} else {
				// else, set median to middle value
				result.setMedian(commentLengths.get((int) count / 2));
			}
			// calculate standard deviation
			float mean = sum / count;
			float sumOfSquares = 0.0f;
			for (Float f : commentLengths) {
				sumOfSquares += (f - mean) * (f - mean);
			}
			result.setStdDev((float) Math.sqrt(sumOfSquares / (count - 1)));
			context.write(key, result);
		}
	}

	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: MedianStdDev <in> <out>");
			ToolRunner.printGenericCommandUsage(System.err);
			System.exit(2);
		}

		Job job = new Job(conf,
				"StackOverflow Median and Standard Deviation Comment Length By Hour");
		job.setJarByClass(MedianStdDev.class);
		job.setMapperClass(MedianStdDevMapper.class);
		job.setReducerClass(MedianStdDevReducer.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(IntWritable.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		boolean success = job.waitForCompletion(true);

		return success ? 0 : 1;
	}
}
