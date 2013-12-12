package com.eftimoff.mapreduce.summarization.numerical.minmaxcount;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.hadoop.io.Writable;

public class MinMaxCountTuple implements Writable {
	private Date min = new Date();
	private Date max = new Date();
	private long count = 0;
	private final static SimpleDateFormat frmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

	public Date getMin() {
		return min;
	}

	public void setMin(Date min) {
		this.min = min;
	}

	public Date getMax() {
		return max;
	}

	public void setMax(Date max) {
		this.max = max;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public void readFields(DataInput in) throws IOException {
		// Read the data out in the order it is written,
		// creating new Date objects from the UNIX timestamp
		min = new Date(in.readLong());
		max = new Date(in.readLong());
		count = in.readLong();
	}

	public void write(DataOutput out) throws IOException {
		// Write the data out in the order it is read,
		// using the UNIX timestamp to represent the Date
		out.writeLong(min.getTime());
		out.writeLong(max.getTime());
		out.writeLong(count);
	}

	public String toString() {
		return frmt.format(min) + "\t" + frmt.format(max) + "\t" + count;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (count ^ (count >>> 32));
		result = prime * result + ((max == null) ? 0 : max.hashCode());
		result = prime * result + ((min == null) ? 0 : min.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MinMaxCountTuple other = (MinMaxCountTuple) obj;
		if (count != other.count)
			return false;
		if (max == null) {
			if (other.max != null)
				return false;
		} else if (!max.equals(other.max))
			return false;
		if (min == null) {
			if (other.min != null)
				return false;
		} else if (!min.equals(other.min))
			return false;
		return true;
	}

}
