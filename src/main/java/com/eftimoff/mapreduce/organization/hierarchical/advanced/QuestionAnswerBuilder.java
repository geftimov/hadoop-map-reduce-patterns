package com.eftimoff.mapreduce.organization.hierarchical.advanced;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

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
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.w3c.dom.Element;

import com.eftimoff.mapreduce.organization.hierarchical.HierarchicalXmlFragment;
import com.eftimoff.mapreduce.organization.hierarchical.ReduceSideDataDiscriminator;
import com.eftimoff.mapreduce.utils.MRDPUtils;

public class QuestionAnswerBuilder extends Configured implements Tool {
	public enum ExceptionCounters {
		INVALID_XML, NO_ID_FIELD, UNRECORDABLE_DATA, INVALID_POST_TYPE_ID, INVALID_ID, INVALID_PARENT_ID
	}

	public static void main(String[] args) throws Exception {
		System.exit(new QuestionAnswerBuilder().run(args));
	}

	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = new Configuration();
		Job job = new Job(conf, "QuestionAnswerBuilder");
		job.setJarByClass(QuestionAnswerBuilder.class);

		job.setMapperClass(HierarchyMapper.class);

		FileInputFormat.addInputPath(job, new Path(args[0]));

		job.setReducerClass(QuestionAnswerReducer.class);

		job.setOutputFormatClass(TextOutputFormat.class);
		TextOutputFormat.setOutputPath(job, new Path(args[1]));

		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);

		return job.waitForCompletion(true) ? 0 : 2;
	}

	public static class HierarchyMapper extends
			Mapper<Object, Text, IntWritable, Text> {
		private IntWritable outKey = new IntWritable();
		private Text outValue = new Text();

		@Override
		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			Element post = null;
			try {
				post = HierarchicalXmlFragment.getXmlElementFromString(value
						.toString());
			} catch (Exception e) {
				e.printStackTrace();
				context.getCounter(ExceptionCounters.INVALID_XML).increment(1);
				return;
			}

			int postType = getIntegerAttributeOrFail(post, "PostTypeId",
					context, ExceptionCounters.INVALID_POST_TYPE_ID);
			if (postType == 0)
				return;
			int id;
			if (postType == 1) {
				id = getIntegerAttributeOrFail(post, "Id", context,
						ExceptionCounters.INVALID_ID);
				if (id == 0)
					return;
				outValue.set("Q" + value.toString());
			} else {
				id = getIntegerAttributeOrFail(post, "ParentId", context,
						ExceptionCounters.INVALID_PARENT_ID);
				if (id == 0)
					return;
				outValue.set("A" + value.toString());
			}
			outKey.set(id);
			context.write(outKey, outValue);
		}
	}

	@SuppressWarnings("unchecked")
	private static int getIntegerAttributeOrFail(Element element,
			String attributeName,
			@SuppressWarnings("rawtypes") Mapper.Context context,
			ExceptionCounters counter) {
		String str = element.getAttribute(attributeName);
		if (MRDPUtils.isNullOrEmpty(str) || !MRDPUtils.isInteger(str)) {
			context.getCounter(counter).increment(1);
			return 0;
		}
		return Integer.parseInt(str);
	}

	public static class QuestionAnswerReducer extends
			Reducer<IntWritable, Text, Text, NullWritable> {
		private List<String> answers = new LinkedList<String>();
		private String question = null;

		public enum DataTypeCounters {
			TOTAL_QUESTIONS, TOTAL_ANSWERS;
		}

		@Override
		public void reduce(IntWritable key, Iterable<Text> values,
				Context context) throws IOException, InterruptedException {
			reset();

			for (Text t : values) {
				ReduceSideDataDiscriminator pcd = ReduceSideDataDiscriminator
						.parse(t.toString());
				if (pcd.isUnrecordable()) {
					context.getCounter(ExceptionCounters.UNRECORDABLE_DATA)
							.increment(1);
					return;
				}
				if (pcd.getType() == ReduceSideDataDiscriminator.Type.QUESTION) {
					question = pcd.getData();
					context.getCounter(DataTypeCounters.TOTAL_QUESTIONS)
							.increment(1);
				} else {
					answers.add(pcd.getData());
					context.getCounter(DataTypeCounters.TOTAL_ANSWERS)
							.increment(1);
				}
			}
			HierarchicalXmlFragment doc;
			try {
				doc = new HierarchicalXmlFragment(question, answers);
			} catch (Exception e) {
				e.printStackTrace();
				context.getCounter(ExceptionCounters.INVALID_XML).increment(1);
				return;
			}
			context.write(new Text(doc.toString()), NullWritable.get());
		}

		private void reset() {
			question = null;
			answers.clear();
		}
	}
}