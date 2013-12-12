package com.eftimoff.mapreduce.summarization.numerical.average;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

public class CountAverageTuple implements Writable {
	private float count = 0;
	private float average;

	public void readFields(DataInput in) throws IOException {
		count = in.readFloat();
		average = in.readFloat();
	}

	public void write(DataOutput out) throws IOException {
		out.writeFloat(count);
		out.writeFloat(average);
	}

	public float getCount() {
		return count;
	}

	public void setCount(float count) {
		this.count = count;
	}

	public float getAverage() {
		return average;
	}

	public void setAverage(float average) {
		this.average = average;
	}

	@Override
	public String toString() {
		return count + "\t" + average;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(average);
		result = prime * result + Float.floatToIntBits(count);
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
		CountAverageTuple other = (CountAverageTuple) obj;
		if (Float.floatToIntBits(average) != Float.floatToIntBits(other.average))
			return false;
		if (Float.floatToIntBits(count) != Float.floatToIntBits(other.count))
			return false;
		return true;
	}

}
