# Partitioning
##Pattern Description
```
The partitioning pattern moves the records into categories (i.e., shards, partitions, or
bins) but it doesn’t really care about the order of records.
```
##Intent
```
The intent is to take similar records in a data set and partition them into distinct, smaller data sets.
```
##Motivation
```
If you want to look at a particular set of data—such as postings made on a particular
date—the data items are normally spread out across the entire data set. So looking at
just one of these subsets requires an entire scan of all of the data. Partitioning means
breaking a large set of data into smaller subsets, which can be chosen by some criterion
relevant to your analysis. To improve performance, you can run a job that takes the data
set and breaks the partitions out into separate files. Then, when a particular subset for
the data is to be analyzed, the job needs only to look at that data.
Partitioning by date is one of the most common schemes. This helps when we want to
analyze a certain span of time, because the data is already grouped by that criterion. For
instance, suppose you have event data that spans three years in your Hadoop cluster,
but for whatever reason the records are not ordered at all by date. If you only care about
data from January 27 to February 3 of the current year, you must scan all of the data
since those events could be anywhere in the data set. If instead you had the events
partitioned into months (i.e., you have a file with January data, a file with February data,
etc.), you would only need to run your MapReduce job over the January and February
partitions. It would be even better if they were partitioned by day!
Partitioning can also help out when you have several different types of records in the
same data set, which is increasingly common in NoSQL. For example, in a HTTP server
logs, you’ll have GET and POST requests, internal system messages, and error messages.
Analysis may care about only one category of this data, so partitioning it into these
categories will help narrow down the data the job runs over before it even runs.
In an RDBMS, a typical criterion for partitioning is what you normally filter by in the
WHERE clause. So, for example, if you are typically filtering down records by country,
perhaps you should partition by country. This applies in MapReduce as well. If you find
yourself filtering out a bunch of records in the mapper due to the same criteria over and
over, you should consider partitioning your data set.
There is no downside to partitioning other than having to build the partitions. A Map‐
Reduce job can still run over all the partitions at once if necessary.
```
##Applicability
```
The one major requirement to apply this pattern is knowing how many partitions you
are going to have ahead of time. For example, if you know you are going to partition by
day of the week, you know that you will have seven partitions.
You can get around this requirement by running an analytic that determines the number
of partitions. For example, if you have a bunch of timestamped data, but you don’t know
how far back it spans, run a job that figures out the date range for you.
```
##Structure
![Structure](https://github.com/geftimov/MapReduce/tree/master/readme/img/Partitioning.png)
```
This pattern is interesting in that it exploits the fact that the partitioner partitions data
(imagine that!). There is no actual partitioning logic; all you have to do is define the
function that determines what partition a record is going to go to in a custom partitioner.
Figure 4-2 shows the structure of this pattern.
• In most cases, the identity mapper can be used.
• The custom partitioner is the meat of this pattern. The custom partitioner will
determine which reducer to send each record to; each reducer corresponds to par‐
ticular partitions.
• In most cases, the identity reducer can be used. But this pattern can do additional
processing in the reducer if needed. Data is still going to get grouped and sorted,
so data can be deduplicated, aggregated, or summarized, per partition.
```
##Consequences
```
The output folder of the job will have one part file for each partition.
```
##Known uses
###Partition pruning by continuous value
```
You have some sort of continuous variable, such as a date or numerical value, and
at any one time you care about only a certain subset of that data. Partitioning the
data into bins will allow your jobs to load only pertinent data.
```
###Partition pruning by category
```
Instead of having some sort of continuous variable, the records fit into one of several
clearly defined categories, such as country, phone area code, or language.
```
###Sharding
```
A system in your architecture has divisions of data—such as different disks—and
you need to partition the data into these existing shards.
```
##Resemblances
###SQL

    Some SQL databases allow for automatically partitioned tables. This allows “par‐
tition pruning” which allows the database to exclude large portions of irrelevant
data before running the SQL.
###Other patterns

    This pattern is similar to the binning pattern in this chapter. In most cases, binning can perform the same partitioning behavior as this pattern.
##Performance analysis
```
The main performance concern with this pattern is that the resulting partitions will
likely not have similar number of records. Perhaps one partition turns out to hold 50%
of the data of a very large data set. If implemented naively, all of this data will get sent
to one reducer and will slow down processing significantly.
It’s pretty easy to get around this, though. Split very large partitions into several smaller
partitions, even if just randomly. Assign multiple reducers to one partition and then
randomly assign records into each to spread it out a bit better.
For example, consider the “last access date” field for a user in StackOverflow. If we
partitioned on this property equally over months, the most recent month will very likely
be much larger than any other month. To prevent skew, it may make sense to partition
the most recent month into days, or perhaps just randomly.
This method doesn’t affect processing over partitions, since you know that these set of
files represent one larger partition. Just include all of them as input.
```
