package com.eftimoff.mapreduce.organization.hierarchical;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;

import com.eftimoff.mapreduce.utils.MRDPUtils;

public class PostCommentHierarchy extends Configured implements Tool {
	public enum ExceptionCounters {
		INVALID_XML, NO_ID_FIELD, UNRECORDABLE_DATA
	}

	public static void main(String[] args) throws Exception {
		System.exit(new PostCommentHierarchy().run(args));
	}

	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = new Configuration();
		Job job = new Job(conf, "PostCommentHeirarchy");
		job.setJarByClass(PostCommentHierarchy.class);

		MultipleInputs.addInputPath(job, new Path(args[0]),
				TextInputFormat.class, PostMapper.class);
		MultipleInputs.addInputPath(job, new Path(args[1]),
				TextInputFormat.class, CommentMapper.class);

		job.setReducerClass(PostCommentHierarchyReducer.class);

		job.setOutputFormatClass(TextOutputFormat.class);
		TextOutputFormat.setOutputPath(job, new Path(args[2]));

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		return job.waitForCompletion(true) ? 0 : 2;
	}

	public static abstract class HierarchyMapper extends
			Mapper<Object, Text, Text, Text> {
		private Text outKey = new Text(), outValue = new Text();

		private final String fieldName;
		private final String valuePrefix;

		protected HierarchyMapper(String fieldName, String valuePrefix) {
			this.fieldName = fieldName;
			this.valuePrefix = valuePrefix;
		}

		@Override
		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			Map<String, String> parsed = MRDPUtils.transformXmlToMap(value
					.toString());
			String parsedId = parsed.get(fieldName);
			if (parsedId == null) {
				context.getCounter(ExceptionCounters.NO_ID_FIELD).increment(1);
				return;
			}

			outKey.set(parsedId);
			outValue.set(valuePrefix + value.toString());
			context.write(outKey, outValue);
		}
	}

	public static class PostMapper extends HierarchyMapper {
		public PostMapper() {
			super("Id", "P");
		}
	}

	public static class CommentMapper extends HierarchyMapper {
		public CommentMapper() {
			super("PostId", "C");
		}
	}

	public static class PostCommentHierarchyReducer extends
			Reducer<Text, Text, Text, NullWritable> {
		private List<String> comments = new LinkedList<String>();
		private String post = null;

		public enum DataTypeCounters {
			TOTAL_POSTS, TOTAL_COMMENTS
		}

		@Override
		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			// Reset variables
			reset();
			// For each input value
			for (Text t : values) {
				ReduceSideDataDiscriminator pcd = ReduceSideDataDiscriminator
						.parse(t.toString());
				if (pcd.isUnrecordable()) {
					context.getCounter(ExceptionCounters.UNRECORDABLE_DATA)
							.increment(1);
					return;
				}// If this is the post record, store it, minus the flag
				if (pcd.getType() == ReduceSideDataDiscriminator.Type.POST) {
					post = pcd.getData();
					context.getCounter(DataTypeCounters.TOTAL_POSTS).increment(
							1);
				} else {// Else, it is a comment record. Add it to the list, minus
					// the flag
					comments.add(pcd.getData());
					context.getCounter(DataTypeCounters.TOTAL_COMMENTS)
							.increment(1);
				}
			}
			HierarchicalXmlFragment doc;
			try {
				doc = new HierarchicalXmlFragment(post, comments);
			} catch (Exception e) {
				e.printStackTrace();
				context.getCounter(ExceptionCounters.INVALID_XML).increment(1);
				return;
			}
			context.write(new Text(doc.toString()), NullWritable.get());
		}

		private void reset() {
			post = null;
			comments.clear();
		}
	}
}