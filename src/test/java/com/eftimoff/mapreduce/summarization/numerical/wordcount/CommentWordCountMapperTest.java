package com.eftimoff.mapreduce.summarization.numerical.wordcount;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.eftimoff.mapreduce.summarization.numerical.wordcount.CommentWordCount.WordCountMapper;

@RunWith(MockitoJUnitRunner.class)
public class CommentWordCountMapperTest {

	private WordCountMapper wordCountMapper;
	@Mock
	private Mapper<Object, Text, Text, IntWritable>.Context context;
	private String inputRow;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		wordCountMapper = new WordCountMapper();
		inputRow = "<row Id=\"1\" PostId=\"2\" Score=\"4\"" + " Text=\"Sample\" "
				+ "CreationDate=\"2010-08-10T20:47:19.800\" UserId=\"73\" />";
	}

	/**
	 * Test method for
	 * {@link org.in.hadoop.ch1.WordCountMapper#map(java.lang.Object, org.apache.hadoop.io.Text, org.apache.hadoop.mapreduce.Mapper.Context)}
	 * .
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 */
	@Test
	public void testMap() throws IOException, InterruptedException {

		wordCountMapper.map(new Object(), new Text(inputRow), context);

		verify(context, times(1)).write(new Text("sample"), new IntWritable(1));

		verifyNoMoreInteractions(context);
	}

}
