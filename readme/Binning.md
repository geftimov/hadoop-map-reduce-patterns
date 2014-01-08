# Binning
##Pattern Description
```
The binning pattern, much like the previous pattern, moves the records into categories
irrespective of the order of records.
```
##Intent
```
For each record in the data set, file each one into one or more categories.
```
##Motivation
```
Binning is very similar to partitioning and often can be used to solve the same problem.
The major difference is in how the bins or partitions are built using the MapReduce
framework. In some situations, one solution works better than the other.
Binning splits data up in the map phase instead of in the partitioner. This has the major
advantage of eliminating the need for a reduce phase, usually leading to more efficient
resource allocation. The downside is that each mapper will now have one file per possible
output bin. This means that, if you have a thousand bins and a thousand mappers, you
are going to output a total of one million files. This is bad for NameNode scalability and
follow-on analytics. The partitioning pattern will have one output file per category and
does not have this problem.
```
##Structure
![Structure](https://github.com/geftimov/MapReduce/tree/master/readme/img/Binning.png)
```
• This pattern’s driver is unique in using the MultipleOutputs class, which sets up
the job’s output to write multiple distinct files.
• The mapper looks at each line, then iterates through a list of criteria for each bin.
If the record meets the criteria, it is sent to that bin. See Figure 4-3.
• No combiner, partitioner, or reducer is used in this pattern.
```
##Consequences
```
Each mapper outputs one small file per bin.
```  
##Resemblances
###Pig

    The SPLIT operation in Pig implements this pattern.
SPLIT data INTO
eights IF col1 == 8,
bigs IF col1 > 8,
smalls IF (col1 < 8 AND col1 > 0);
##Performance analysis
```
This pattern has the same scalability and performance properties as other map-only
jobs. No sort, shuffle, or reduce needs to be performed, and most of the processing is
going to be done on data that is local.
```
