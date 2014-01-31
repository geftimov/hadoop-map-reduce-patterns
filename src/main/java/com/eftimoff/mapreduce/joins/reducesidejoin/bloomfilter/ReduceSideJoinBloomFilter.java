package com.eftimoff.mapreduce.joins.reducesidejoin.bloomfilter;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.util.bloom.BloomFilter;
import org.apache.hadoop.util.bloom.Key;

import com.eftimoff.mapreduce.utils.MRDPUtils;

public class ReduceSideJoinBloomFilter extends Configured implements Tool {

	public static class UserJoinMapper extends Mapper<Object, Text, Text, Text> {
		private Text outkey = new Text();
		private Text outvalue = new Text();

		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			// Parse the input string into a nice map
			Map<String, String> parsed = MRDPUtils.transformXmlToMap(value
					.toString());
			String userId = parsed.get("Id");
			if (userId == null) {
				return;
			}
			String reputation = parsed.get("Reputation");
			if (reputation == null) {
				return;
			}

			// If the reputation is greater than 1,500,
			// output the user ID with the value
			if (Integer.parseInt(reputation) > 1500) {
				outkey.set(userId);
				outvalue.set("A" + value.toString());
				context.write(outkey, outvalue);
			}
		}
	}

	public static class CommentJoinMapperWithBloom extends
			Mapper<Object, Text, Text, Text> {
		private BloomFilter bfilter = new BloomFilter();
		private Text outkey = new Text();
		private Text outvalue = new Text();

		public void setup(Context context) throws IOException {
			Path[] files = DistributedCache.getLocalCacheFiles(context
					.getConfiguration());
			DataInputStream strm = new DataInputStream(new FileInputStream(
					new File(files[0].toString())));
			bfilter.readFields(strm);
		}

		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			Map<String, String> parsed = MRDPUtils.transformXmlToMap(value
					.toString());
			String userId = parsed.get("UserId");
			if (userId == null) {
				return;
			}
			if (bfilter.membershipTest(new Key(userId.getBytes()))) {
				outkey.set(userId);
				outvalue.set("B" + value.toString());
				context.write(outkey, outvalue);
			}
		}
	}

	public static class UserJoinReducer extends Reducer<Text, Text, Text, Text> {
		private static final Text EMPTY_TEXT = new Text("");
		private Text tmp = new Text();
		private ArrayList<Text> listA = new ArrayList<Text>();
		private ArrayList<Text> listB = new ArrayList<Text>();
		private String joinType = null;

		public void setup(Context context) {
			// Get the type of join from our configuration
			joinType = context.getConfiguration().get("join.type");
		}

		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			// Clear our lists
			listA.clear();
			listB.clear();
			// iterate through all our values, binning each record based on what
			// it was tagged with. Make sure to remove the tag!
			while (values.iterator().hasNext()) {
				tmp = values.iterator().next();
				if (tmp.charAt(0) == 'A') {
					listA.add(new Text(tmp.toString().substring(1)));
				} else if (tmp.charAt('0') == 'B') {
					listB.add(new Text(tmp.toString().substring(1)));
				}
			}
			// Execute our join logic now that the lists are filled
			executeJoinLogic(context);
		}

		private void executeJoinLogic(Context context) throws IOException,
				InterruptedException {

			if (joinType.equalsIgnoreCase("inner")) {
				// If both lists are not empty, join A with B
				if (!listA.isEmpty() && !listB.isEmpty()) {
					for (Text A : listA) {
						for (Text B : listB) {
							context.write(A, B);
						}
					}
				}
			} else if (joinType.equalsIgnoreCase("leftouter")) {
				// For each entry in A,
				for (Text A : listA) {
					// If list B is not empty, join A and B
					if (!listB.isEmpty()) {
						for (Text B : listB) {
							context.write(A, B);
						}
					} else {
						// Else, output A by itself
						context.write(A, EMPTY_TEXT);
					}
				}
			} else if (joinType.equalsIgnoreCase("rightouter")) {
				// For each entry in B,
				for (Text B : listB) {
					// If list A is not empty, join A and B
					if (!listA.isEmpty()) {
						for (Text A : listA) {
							context.write(A, B);
						}
					} else {
						// Else, output B by itself
						context.write(EMPTY_TEXT, B);
					}
				}
			} else if (joinType.equalsIgnoreCase("fullouter")) {
				// If list A is not empty
				if (!listA.isEmpty()) {
					// For each entry in A
					for (Text A : listA) {
						// If list B is not empty, join A with B
						if (!listB.isEmpty()) {
							for (Text B : listB) {
								context.write(A, B);
							}
						} else {
							// Else, output A by itself
							context.write(A, EMPTY_TEXT);
						}
					}
				} else {
					// If list A is empty, just output B
					for (Text B : listB) {
						context.write(EMPTY_TEXT, B);
					}
				}
			} else if (joinType.equalsIgnoreCase("anti")) {
				// If list A is empty and B is empty or vice versa
				if (listA.isEmpty() ^ listB.isEmpty()) {
					// Iterate both A and B with null values
					// The previous XOR check will make sure exactly one of
					// these lists is empty and therefore the list will be
					// skipped
					for (Text A : listA) {
						context.write(A, EMPTY_TEXT);
					}
					for (Text B : listB) {
						context.write(EMPTY_TEXT, B);
					}
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		System.exit(new ReduceSideJoinBloomFilter().run(args));
	}

	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = new Configuration();
		GenericOptionsParser parser = new GenericOptionsParser(conf, args);
		String[] otherArgs = parser.getRemainingArgs();
		if (otherArgs.length != 4) {
			printUsage();
		}
		Job job = new Job(conf, "ReduceSideJoinBloomFilter");
		job.setJarByClass(ReduceSideJoinBloomFilter.class);

		// Use MultipleInputs to set which input uses what mapper
		// This will keep parsing of each data set separate from a logical
		// standpoint
		// The first two elements of the args array are the two inputs
		MultipleInputs.addInputPath(job, new Path(args[0]),
				TextInputFormat.class, UserJoinMapper.class);
		MultipleInputs.addInputPath(job, new Path(args[1]),
				TextInputFormat.class, CommentJoinMapperWithBloom.class);
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
				.println("Usage: ReduceSideJoinBloomFilter <user_in> <comments_in> <join_type> <out>");
		ToolRunner.printGenericCommandUsage(System.err);
		System.exit(2);
	}

}
