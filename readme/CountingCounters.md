
# Counting with Counters
##Pattern Description
```
This pattern utilizes the MapReduce framework’s counters utility to calculate a global
sum entirely on the map side without producing any output.
```
##Intent
```
An efficient means to retrieve count summarizations of large data sets.
```
##Motivation
```
A count or summation can tell you a lot about particular fields of data, or your data as
a whole. Hourly ingest record counts can be post processed to generate helpful histo‐
grams. This can be executed in a simple “word count” manner, in that for each input
record, you output the same key, say the hour of data being processed, and a count of 1.
The single reduce will sum all the input values and output the final record count with
the hour. This works very well, but it can be done more efficiently using counters. Instead
of writing any key value pairs at all, simply use the framework’s counting mechanism to
keep track of the number of input records. This requires no reduce phase and no sum‐
mation! The framework handles monitoring the names of the counters and their asso‐
ciated values, aggregating them across all tasks, as well as taking into account any failed
task attempts.
Say you want to find the number of times your employees log into your heavily used
public website every day. Assuming you have a few dozen employees, you can apply
filter conditions while parsing through your web logs. Rather than outputting the em‐
ployee’s user name with a count of ‘1’, you can simply create a counter with the employee’s
ID and increment it by 1. At the end of the job, simply grab the counters from the
framework and save them wherever your heart desires—the log, local file system, HDFS,
etc.
Some counters come built into the framework, such as number of input/output records
and bytes. Hadoop allows for programmers to create their own custom counters for
whatever their needs may be. This pattern describes how to utilize these custom counters
to gather count or summation metrics from your data sets. The major benefit of using
counters is all the counting can be done during the map phase.
The caveat to using counters is they are all stored in-memory by the
JobTracker. The counters are serialized by each map task and sent with
status updates. In order to play nice and not bog down the JobTracker,
the number of counters should be in the tens -- a hundred at most... and
thats a big “at most”! Counters are definitely not meant to aggregate lots
of statistics about your MapReduce job! Newer versions of Hadoop ac‐
tually limit the number of counters a job can create to prevent any per‐
manent damage to the JobTracker. The last thing you want is to have
your analytic take down the JobTracker because you created a few hun‐
dred custom counters!
```
##Applicability
```
Counting with counters should be used when:
• You have a desire to gather counts or summations over large data sets.
• The number of counters you are going to create is small—in the double digits.
```
##Structure
![Structure](https://github.com/geftimov/MapReduce/tree/master/readme/img/CountingCounters.png)
```
• The mapper processes each input record at a time to increment counters based on
certain criteria. The counter is either incremented by one if counting a single in‐
stance, or incremented by some number if executing a summation. These counters
are then aggregated by the TaskTrackers running the tasks and incrementally re‐
ported to the JobTracker for overall aggregation upon job success. The counters
from any failed tasks are disregarded by the JobTracker in the final summation.
• As this job is map only, there is no combiner, partitioner, or reducer required.
```
##Consequences
```
The final output is a set of counters grabbed from the job framework. There is no actual
output from the analytic itself. However, the job requires an output directory to execute.
This directory will exist and contain a number of empty part files equivalent to the
number of map tasks. This directory should be deleted on job completion.
```
##Known uses
###Count number of records [ReadMe](https://github.com/geftimov/MapReduce/tree/master/readme/CountNumUsersByState.md)
```
Simply counting the number of records over a given time period is very common.
It’s typically a counter provided by the framework, among other common things.
```
###Count a small number of unique instances
```
Counters can also be created on the fly by using a string variable. You might now
know what the value is, but the counters don’t have to be created ahead of time.
Simply creating a counter using the value of a field and incrementing it is enough
to solve this use case. Just be sure the number of counters you are creating is a small
number!
```
###Summations
```
Counters can be used to sum fields of data together. Rather than performing the
sum on the reduce side, simply create a new counter and use it to sum the field
values.
```
##Performance analysis
```
Using counters is very fast, as data is simply read in through the mapper and no output
is written. Performance depends largely on the number of map tasks being executed and
how much time it takes to process each record.
```
