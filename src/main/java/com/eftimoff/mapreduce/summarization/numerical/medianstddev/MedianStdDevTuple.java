package com.eftimoff.mapreduce.summarization.numerical.medianstddev;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

public class MedianStdDevTuple implements Writable {
	private float median;
	private float stdDev;

	public float getMedian() {
		return median;
	}

	public void setMedian(float median) {
		this.median = median;
	}

	public float getStdDev() {
		return stdDev;
	}

	public void setStdDev(float stdDev) {
		this.stdDev = stdDev;
	}

	@Override
	public void readFields(DataInput arg0) throws IOException {
		median = arg0.readFloat();
		stdDev = arg0.readFloat();
	}

	@Override
	public void write(DataOutput arg0) throws IOException {
		arg0.writeFloat(median);
		arg0.writeFloat(stdDev);
	}

	@Override
	public String toString() {
		return median + "\t" + stdDev;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(median);
		result = prime * result + Float.floatToIntBits(stdDev);
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
		MedianStdDevTuple other = (MedianStdDevTuple) obj;
		if (Float.floatToIntBits(median) != Float.floatToIntBits(other.median))
			return false;
		if (Float.floatToIntBits(stdDev) != Float.floatToIntBits(other.stdDev))
			return false;
		return true;
	}

}
