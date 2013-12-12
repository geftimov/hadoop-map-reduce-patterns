package com.eftimoff.mapreduce.summarization.numerical.wordcount;

import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.eftimoff.mapreduce.summarization.numerical.wordcount.CommentWordCount.WordCountReducer;

@RunWith(MockitoJUnitRunner.class)
public class CommentWordCountReducerTest {

	@Mock
	private Reducer<Text, IntWritable, Text, IntWritable>.Context context;
	private WordCountReducer wordCountReducer;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		wordCountReducer = new WordCountReducer();
	}

	/**
	 * Test method for
	 * {@link org.in.hadoop.ch1.WordCountReducer#reduce(org.apache.hadoop.io.Text, java.lang.Iterable, org.apache.hadoop.mapreduce.Reducer.Context)}
	 * .
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 */
	@Test
	public void testReduceTextIterableOfIntWritableContext() throws IOException,
			InterruptedException {

		List<IntWritable> values = Arrays.asList(new IntWritable(1), new IntWritable(2));
		wordCountReducer.reduce(new Text("Sample"), values, context);

		verify(context).write(new Text("Sample"), new IntWritable(3));

	}

}