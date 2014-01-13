# TotalOrderSorting
##Pattern Description
```
The total order sorting pattern is concerned with the order of the data from record to
record.
```
##Intent
```
You want to sort your data in parallel on a sort key.
```
##Motivation
```
Sorting is easy in sequential programming. Sorting in MapReduce, or more generally
in parallel, is not easy. This is because the typical “divide and conquer” approach is a bit
harder to apply here.
Each individual reducer will sort its data by key, but unfortunately, this sorting is not
global across all data. What we want to do here is a total order sorting where, if you
concatenate the output files, the records are sorted. If we just concatenate the output of
a simple MapReduce job, segments of the data will be sorted, but the whole set will not
be.
Sorted data has a number of useful properties. Sorted by time, it can provide a timeline
view on the data. Finding things in a sorted data set can be done with binary search
instead of linear search. In the case of MapReduce, we know the upper and lower
boundaries of each file by looking at the last and first records, respectively. This can be
useful for finding records, as well, and is one of the primary characteristics of HBase.
Some databases can bulk load data faster if the data is sorted on the primary key or
index column.
There are countless more reasons to have sorted data from an application standpoint
or follow-on system standpoint. However, having data sorted for use in MapReduce
serves little purpose, so hopefully this expensive operation only has to be done sparingly.
```
##Applicability
```
The main requirement here is pretty obvious: your sort key has to be comparable so the
data can be ordered.
```
##Structure
![Structure](https://github.com/geftimov/MapReduce/tree/master/readme/img/Filtering.png)
```
Total order sorting may be one of the more complicated patterns you’ll see. The reason
this is that you first have to determine a set of partitions divided by ranges of values that
will produce equal-sized subsets of data. These ranges will determine which reducer
will sort which range of data. Then something similar to the partitioning pattern is run:
a custom partitioner is used to partition data by the sort key. The lowest range of data
goes to the first reducer, the next range goes to the second reducer, so on and so forth.
This pattern has two phases: an analyze phase that determines the ranges, and the order
phase that actually sorts the data. The analyze phase is optional in some ways. You need
to run it only once if the distribution of your data does not change quickly over time,
because the value ranges it produces will continue to perform well. Also, in some cases,
you may be able to guess the partitions yourself, especially if the data is evenly dis‐
tributed. For example, if you are sorting comments by user ID, and you have a million
users, you can assume that with a thousand reducers, each range is going to have a range
of a thousand users. This is because comments by user ID should be spread out evenly
and since you know the number of total users, you can divide that number by the number
of reducers you want to use.
The analyze phase is a random sampling of the data. The partitions are then based on
that random sample. The principle is that partitions that evely split the random sample
should evenly split the larger data set well. The structure of the analyze step is as follows:
• The mapper does a simple random sampling. When dividing records, it outputs the
sort key as its output key so that the data will show up sorted at the reducer. We
don’t care at all about the actual record, so we’ll just use a null value to save on space.
• Ahead of time, determine the number of records in the total data set and figure out
what percentage of records you’ll need to analyze to make a reasonable sample. For
example, if you plan on running the order with a thousand reducers, sampling about
a hundred thousand records should give nice, even partitions. Assuming you have
a billion records, divide 100,000 by 1,000,000,000. This gives 0.0001, meaning .01%
of the records should be run through the analyze phase.
• Only one reducer will be used here. This will collect the sort keys together into a
sorted list (they come in sorted, so that will be easy). Then, when all of them have
been collected, the list of keys will be sliced into the data range boundaries.
The order phase is a relatively straightforward application of MapReduce that uses a
custom partitioner. The structure of the order step is as follows:
• The mapper extracts the sort key in the same way as the analyze step. However, this
time the record itself is stored as the value instead of being ignored.
• A custom partitioner is used that loads up the partition file. In Hadoop, you can
use the TotalOrderPartitioner , which is built specifically for this purpose. It takes
the data ranges from the partition file produced in the previous step and decides
which reducer to send the data to.
• The reducer’s job here is simple. The shuffle and sort take care of the heavy lifting.
The reduce function simply takes the values that have come in and outputs them.
The number of reducers needs to be equal to the number of partitions for the
TotalOrderPartitioner to work properly.
```
##Consequences
```
The output of the job will be a subset of the records that pass the selection criteria. If
the fThe output files will contain sorted data, and the output file names will be sorted such
that the data is in a total sorting. In Hadoop, you’ll be able to issue hadoop fs -cat
output/part-r-* and retrieve the data in a sorted manner.
```  
##Resemblances
###SQL

    Ordering in SQL is pretty easy!
SELECT * FROM data ORDER BY col1;
###Pig

    Ordering in Pig is syntactically pretty easy, but it’s a very expensive operation. Be‐
hind the scenes, it will run a multi-stage MapReduce job to first find the partitions,
and then perform the actual sort.
c = ORDER b BY col1;
##Performance analysis
```
This operation is expensive because you effectively have to load and parse the data twice:
first to build the partition ranges, and then to actually sort the data.
The job that builds the partitions is straightforward and efficient since it has only one
reducer and sends a minimal amount of data over the network. The output file is small,
so writing it out is trivial. Also, you may only have to run this now and then, which will
amortize the cost of building it over time.
The order step of the job has performance characteristics similar to the other data or‐
ganization patterns, because it has to move all of the data over the network and write
all of the data back out. Therefore, you should use a relatively large number of reducers.
```
