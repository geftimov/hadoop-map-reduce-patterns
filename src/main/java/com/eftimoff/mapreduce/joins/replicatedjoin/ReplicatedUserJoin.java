package com.eftimoff.mapreduce.joins.replicatedjoin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.eftimoff.mapreduce.joins.reducesidejoin.ReduceSideJoin.CommentJoinMapper;
import com.eftimoff.mapreduce.joins.reducesidejoin.bloomfilter.ReduceSideJoinBloomFilter.UserJoinMapper;
import com.eftimoff.mapreduce.joins.reducesidejoin.bloomfilter.ReduceSideJoinBloomFilter.UserJoinReducer;
import com.eftimoff.mapreduce.utils.MRDPUtils;

public class ReplicatedUserJoin extends Configured implements Tool {

	public static class ReplicatedJoinMapper extends
			Mapper<Object, Text, Text, Text> {
		private static final Text EMPTY_TEXT = new Text("");
		private HashMap<String, String> userIdToInfo = new HashMap<String, String>();
		private Text outvalue = new Text();
		private String joinType = null;

		public void setup(Context context) throws IOException,
				InterruptedException {
			Path[] files = DistributedCache.getLocalCacheFiles(context
					.getConfiguration());
			// Read all files in the DistributedCache
			for (Path p : files) {
				BufferedReader rdr = new BufferedReader(new InputStreamReader(
						new GZIPInputStream(new FileInputStream(new File(
								p.toString())))));
				String line = null;
				// For each record in the user file
				while ((line = rdr.readLine()) != null) {
					// Get the user ID for this record
					Map<String, String> parsed = MRDPUtils
							.transformXmlToMap(line);
					String userId = parsed.get("Id");
					// Map the user ID to the record
					userIdToInfo.put(userId, line);
				}
				rdr.close();
			}
			// Get the join type from the configuration
			joinType = context.getConfiguration().get("join.type");
		}

		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			Map<String, String> parsed = MRDPUtils.transformXmlToMap(value
					.toString());
			String userId = parsed.get("UserId");
			String userInformation = userIdToInfo.get(userId);
			// If the user information is not null, then output
			if (userInformation != null) {
				outvalue.set(userInformation);
				context.write(value, outvalue);
			} else if (joinType.equalsIgnoreCase("leftouter")) {
				// If we are doing a left outer join,
				// output the record with an empty value
				context.write(value, EMPTY_TEXT);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		System.exit(new ReplicatedUserJoin().run(args));
	}

	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = new Configuration();
		GenericOptionsParser parser = new GenericOptionsParser(conf, args);
		String[] otherArgs = parser.getRemainingArgs();
		if (otherArgs.length != 4) {
			printUsage();
		}
		Job job = new Job(conf, "ReduceSideJoin");
		job.setJarByClass(ReplicatedUserJoin.class);

		// Use MultipleInputs to set which input uses what mapper
		// This will keep parsing of each data set separate from a logical
		// standpoint
		// The first two elements of the args array are the two inputs
		MultipleInputs.addInputPath(job, new Path(args[0]),
				TextInputFormat.class, UserJoinMapper.class);
		MultipleInputs.addInputPath(job, new Path(args[1]),
				TextInputFormat.class, CommentJoinMapper.class);
		job.getConfiguration().set("join.type", args[2]);

		job.setReducerClass(UserJoinReducer.class);

		job.setOutputFormatClass(TextOutputFormat.class);
		TextOutputFormat.setOutputPath(job, new Path(args[3]));

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		return job.waitForCompletion(true) ? 0 : 2;
	}

	private void printUsage() {
		System.err
				.println("Usage: ReduceSideJoin <user_in> <comments_in> <join_type> <out>");
		ToolRunner.printGenericCommandUsage(System.err);
		System.exit(2);
	}

}
