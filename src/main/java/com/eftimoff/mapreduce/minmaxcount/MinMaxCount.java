package com.eftimoff.mapreduce.minmaxcount;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import com.eftimoff.mapreduce.utils.MRDPUtils;

public class MinMaxCount {

	public static class MinMaxCountMapper extends Mapper<Object, Text, Text, MinMaxCountTuple> {
		// Our output key and value Writables
		private Text outUserId = new Text();
		private MinMaxCountTuple outTuple = new MinMaxCountTuple();
		// This object will format the creation date string into a Date object
		private final static SimpleDateFormat frmt = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSS");

		public void map(Object key, Text value, Context context) throws IOException,
				InterruptedException {
			Map<String, String> parsed = MRDPUtils.transformXmlToMap(value.toString());
			// Grab the "CreationDate" field since it is what we are finding
			// the min and max value of
			String strDate = parsed.get("CreationDate");
			// Grab the “UserID” since it is what we are grouping by
			String userId = parsed.get("UserId");
			// Parse the string into a Date object
			Date creationDate = null;
			try {
				creationDate = frmt.parse(strDate);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Set the minimum and maximum date values to the creationDate
			outTuple.setMin(creationDate);
			outTuple.setMax(creationDate);
			// Set the comment count to 1
			outTuple.setCount(1);
			// Set our user ID as the output key
			outUserId.set(userId);
			// Write out the hour and the average comment length
			context.write(outUserId, outTuple);
		}
	}

	public static class MinMaxCountReducer extends
			Reducer<Text, MinMaxCountTuple, Text, MinMaxCountTuple> {
		// Our output value Writable
		private MinMaxCountTuple result = new MinMaxCountTuple();

		public void reduce(Text key, Iterable<MinMaxCountTuple> values, Context context)
				throws IOException, InterruptedException {
			// Initialize our result
			result.setMin(null);
			result.setMax(null);
			result.setCount(0);
			int sum = 0;
			// Iterate through all input values for this key
			for (MinMaxCountTuple val : values) {
				// If the value's min is less than the result's min
				// Set the result's min to value's
				if (result.getMin() == null || val.getMin().compareTo(result.getMin()) < 0) {
					result.setMin(val.getMin());
				}
				// If the value's max is more than the result's max
				// Set the result's max to value's
				if (result.getMax() == null || val.getMax().compareTo(result.getMax()) > 0) {
					result.setMax(val.getMax());
				}
				// Add to our sum the count for value
				sum += val.getCount();
			}
			// Set our count to the number of input values
			result.setCount(sum);
			context.write(key, result);
		}
	}

}
