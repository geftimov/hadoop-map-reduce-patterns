# Filtering
##Pattern Description
```
As the most basic pattern, filtering serves as an abstract pattern for some of the other
patterns. Filtering simply evaluates each record separately and decides, based on some
condition, whether it should stay or go.
```
##Intent
```
Filter out records that are not of interest and keep ones that are.
Consider an evaluation function f that takes a record and returns a Boolean value of
true or false. If this function returns true, keep the record; otherwise, toss it out.
```
##Motivation
```
Your data set is large and you want to take a subset of this data to focus in on it and
perhaps do follow-on analysis. The subset might be a significant portion of the data set
or just a needle in the haystack. Either way, you need to use the parallelism of MapReduce
to wade through all of your data and find the keepers.
For example, you might be interested only in records that have something to do with
Hadoop: Hadoop is either mentioned in the raw text or the event is tagged by a “Hadoop”
tag. Filtering can be used to keep records that meet the “something to do with Hadoop”
criteria and keep them, while tossing out the rest of the records.
Big data and processing systems like Hadoop, in general, are about bringing all of your
organization’s data to one location. Filtering is the way to pull subsets back out and
deliver them to analysis shops that are interested in just that subset. Filtering is also used
to zoom in on a particular set of records that match your criteria that you are more
curious about. The exploration of a subset of data may lead to more valuable and com‐
plex analytics that are based on the behavior that was observed in the small subset.
```
##Applicability
```
Filtering is very widely applicable. The only requirement is that the data can be parsed
into “records” that can be categorized through some well-specified criterion determin‐
ing whether they are to be kept.
```
##Structure
![Structure](https://github.com/geftimov/MapReduce/tree/master/readme/img/Filtering.png)
```
Filtering is unique in not requiring the “reduce” part of MapReduce. This is because it
doesn’t produce an aggregation. Each record is looked at individually and the evaluation
of whether or not to keep that record does not depend on anything else in the data set.
The mapper applies the evaluation function to each record it receives. Typically, the
mapper outputs the same key/value type as the types of the input, since the record is left
unchanged. If the evaluation function returns true, the mapper simply output the key
and value verbatim.
```
##Consequences
```
The output of the job will be a subset of the records that pass the selection criteria. If
the format was kept the same, any job that ran over the larger data set should be able to
run over this filtered data set, as well.
```
##Known uses
###Closer view of data
```
Prepare a particular subset of data, where the records have something in common
or something of interest, for more examination. For example, a local office in Mary‐
land may only care about records originating in Maryland from your international
dataset.
```
###Tracking a thread of events
```
Extract a thread of consecutive events as a case study from a larger data set. For
example, you may be interested in how a particular user interacts with your website
by analyzing Apache web server logs. The events for a particular user are inter‐
spersed with all the other events, so it’s hard to figure out what happened. By filtering
for that user’s IP address, you are able to get a good view of that particular user’s
activities.
```
###Distributed grep
```
Grep, a very powerful tool that uses regular expressions for finding lines of text of
interest, is easily parallelized by applying a regular expression match against each
line of input and only outputting lines that match.
```
###Data cleansing
```  
Data sometimes is dirty, whether it be malformed, incomplete, or in the wrong
format. The data could have missing fields, a date could be not formatted as a date,
or random bytes of binary data could be present. Filtering can be used to validate
that each record is well-formed and remove any junk that does occur.
```
###Simple random sampling
```
If you want a simple random sampling of your data set, you can use filtering where
the evaluation function randomly returns true or false. A simple random sample is
a sample of the larger data set in which each item has the same probability of being
selected. You can tweak the number of records that make it through by having the
evaluation function return true a smaller percentage of the time. For example, if
your data set contains one trillion records and you want a sample size of about one
million, have the evaluation function return true once in a million (because there
are a million millions in a trillion).
```
###Removing low scoring data
```  
If you can score your data with some sort of scalar value, you can filter out records
that don’t meet a certain threshold. If you know ahead of time that certain types of
records are not useful for analysis, you can assign those records a small score and
they will get filtered out. This effectively has the same purpose as the top ten pattern
discussed later, except that you do not know how many records you will get.
```
    
##Resemblances
###SQL

    The filter pattern is synonymous to using the WHERE clause in a SELECT * statement.
The records stay the same, but some are simply filtered out. For example:
SELECT * FROM table WHERE value < 3;
###Pig

    The FILTER keyword.
b = FILTER a BY value < 3;
##Performance analysis
```
This pattern is basically as efficient as MapReduce can get because the job is map-only.
There are a couple of reasons why map-only jobs are efficient.
• Since no reducers are needed, data never has to be transmitted between the map
and reduce phase. Most of the map tasks pull data off of their locally attached disks
and then write back out to that node.
• Since there are no reducers, both the sort phase and the reduce phase are cut out.
This usually doesn’t take very long, but every little bit helps.
One thing to be aware of is the size and number of the output files. Since this job is
running with mappers only, you will get one output file per mapper with the prefix
part-m- (note the m instead of the r ). You may find that these files will be tiny if you
filter out a lot of data, which can cause problems with scalability limitations of the
NameNode further down the road.
If you are worried about the number of small files and do not mind if your job runs just
a little bit longer, you can use an identity reducer to collect the results without doing
anything with them. This will have the mapper send the reducer all of the data, but the
reducer does nothing other than just output them to one file per reducer. The appropriate
number of reducers depends on the amount of data that will be written to the file system
and just how many small files you want to deal with.
```
