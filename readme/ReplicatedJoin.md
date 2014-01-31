# ReplicatedJoin
##Pattern Description
```
A replicated join is a special type of join operation between one large and many small
data sets that can be performed on the map-side.
```
##Intent
```
This pattern completely eliminates the need to shuffle any data to the reduce phase.
```
##Motivation
```
A replicated join is an extremely useful, but has a strict size limit on all but one of the
data sets to be joined. All the data sets except the very large one are essentially read into
memory during the setup phase of each map task, which is limited by the JVM heap. If
you can live within this limitation, you get a drastic benefit because there is no reduce
phase at all, and therefore no shuffling or sorting. The join is done entirely in the map
phase, with the very large data set being the input for the MapReduce job.
There is an additional restriction that a replicated join is really useful only for an inner
or a left outer join where the large data set is the “left” data set. The other join types
require a reduce phase to group the “right” data set with the entirety of the left data set.
Although there may not be a match for the data stored in memory for a given map task,
there could be match in another input split. Because of this, we will restrict this pattern
to inner and left outer joins.
```
##Applicability
```
A replicated join should be used when:
• The type of join to execute is an inner join or a left outer join, with the large input
data set being the “left” part of the operation.
• All of the data sets, except for the large one, can be fit into main memory of each
map task.
```
##Structure
![Structure](https://github.com/geftimov/MapReduce/tree/master/readme/img/ReplicatedJoin.png)
```
• The mapper is responsible for reading all files from the distributed cache during
the setup phase and storing them into in-memory lookup tables. After this setup
phase completes, the mapper processes each record and joins it with all the data
stored in-memory. If the foreign key is not found in the in-memory structures, the
record is either omitted or output, based on the join type. See Figure 5-2.
• No combiner, partitioner, or reducer is used for this pattern. It is map-only.
```
##Consequences
```
The output is a number of part files equivalent to the number of map tasks. The part
files contain the full set of joined records. If a left outer join is used, the input to the
MapReduce analytic will be output in full, with possible null values.
```
##Resemblances
###Pig

    Pig has native support for a replicated join through a simple modification to the
standard join operation syntax. Only inner and left outer joins are supported for
replicated joins, for the same reasons we couldn’t do it above. The order of the data
sets in the line of code matters because all but the first data sets listed are stored in-
memory.
huge = LOAD 'huge_data' AS (h1,h2);
smallest = LOAD 'smallest_data' AS (ss1,ss2);
small = LOAD 'small_data' AS (s1,s2);
A = JOIN huge BY h1, small BY s1, smallest BY ss1 USING 'replicated';
##Performance analysis
```
A replicated join can be the fastest type of join executed because there is no reducer
required, but it comes at a cost. There are limitations on the amount of data that can be
stored safely inside the JVM, which is largely dependent on how much memory you are
willing to give to each map and reduce task. Experiment around with your data sets to
see how much you can fit into memory prior to fully implementing this pattern. Also,
be aware that the memory footprint of your data set stored in-memory is not necessarily
the number of bytes it takes to store it on disk. The data will be inflated due to Java object
overhead. Thankfully, you can omit any data you know you will not need.
```