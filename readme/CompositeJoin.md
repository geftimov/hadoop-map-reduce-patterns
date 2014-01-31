# CompositeJoin
##Pattern Description
```
A composite join is a specialized type of join operation that can be performed on the
map-side with many very large formatted inputs.
```
##Intent
```
Using this pattern completely eliminates the need to shuffle and sort all the data to the
reduce phase. However, it requires the data to be already organized or prepared in a very
specific way.
```
##Motivation
```
Composite joins are particularly useful if you want to join very large data sets together.
However, the data sets must first be sorted by foreign key, partitioned by foreign key,
and read in a very particular manner in order to use this type of join. With that said, if
your data can be read in such a way or you can prepare your data, a composite join has
a huge leg-up over the other types.
Hadoop has built in support for a composite join using the CompositeInputFormat .
This join utility is restricted to only inner and full outer joins. The inputs for each mapper
must be partitioned and sorted in a specific way, and each input dataset must be divided
into the same number of partitions. In addition to that, all the records for a particular
foreign key must be in the same partition. Usually, this occurs only if the output of several
jobs has the same number of reducers and the same foreign key, and output files aren’t
splittable, i.e., not bigger than the HDFS block size or gzipped. In many cases, one of
the other patterns presented in this chapter is more applicable. If you find yourself having
to format the data prior to using a composite join, you are probably better off just using
a reduce side join unless this output is used by many analytics.
```
##Applicability
```
A composite join should be used when:
• An inner or full outer join is desired.
• All the data sets are sufficiently large.
• All data sets can be read with the foreign key as the input key to the mapper.
• All data sets have the same number of partitions.
• Each partition is sorted by foreign key, and all the foreign keys reside in the asso‐
ciated partition of each data set. That is, partition X of data sets A and B contain
the same foreign keys and these foreign keys are present only in partition X. For a
visualization of this partitioning and sorting key, refer to Figure 5-3.
• The data sets do not change often (if they have to be prepared).
![Applicability](https://github.com/geftimov/MapReduce/tree/master/readme/img/ApplicabilityJoin.png)
```
##Structure
![Structure](https://github.com/geftimov/MapReduce/tree/master/readme/img/CompositeJoin.png)
```
• The driver code handles most of the work in the job configuration stage. It sets up
the type of input format used to parse the data sets, as well as the join type to execute.
The framework then handles executing the actual join when the data is read. See
Figure 5-4.
• The mapper is very trivial. The two values are retrieved from the input tuple and
simply output to the file system.
• No combiner, partitioner, or reducer is used for this pattern. It is map-only.
```
##Consequences
```
The output is a number of part files equivalent to the number of map tasks. The part
files contain the full set of joined records. If configured for an outer join, there may be
null values.
```
##Performance analysis
```
A composite join can be executed relatively quickly over large data sets. However, the
MapReduce framework can only set up the job so that one of the two data sets are data
local. The respective files that are partitioned by the same key cannot be assumed to be
on the same node.
Any sort of data preparation needs to taken into account in the performance of this
analytic. The data preparation job is typically a MapReduce job, but if the data sets rarely
change, then the sorted and partitioned data sets can be used over and over. Thus, the
cost of producing these prepared data sets is averaged out over all of the runs.
```
