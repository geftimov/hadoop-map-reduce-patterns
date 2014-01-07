# Distinct
##Pattern Description
```
This pattern filters the whole set, but it’s more challenging because you want to filter out records that look like another record in the data set. The final output of this filter application is a set of unique records.
```
##Intent
```
You have data that contains similar records and you want to find a unique set of values.
```
##Motivation
```
Reducing a data set to a unique set of values has several uses. One particular use case
that can use this pattern is deduplication. In some large data sets, duplicate or extremely
similar records can become a nagging problem. The duplicate records can take up a
significant amount of space or skew top-level analysis results. For example, every time
someone visits your website, you collect what web browser and device they are using
for marketing analysis. If that user visits your website more than once, you’ll log that
information more than once. If you do some analysis to calculate the percentage of your
users that are using a specific web browser, the number of times users have used your
website will skew the results. Therefore, you should first deduplicate the data so that
you have only one instance of each logged event with that device.
Records don’t necessarily need to be exactly the same in the raw form. They just need
to be able to be translated into a form in which they will be exactly the same. For example,
if our web browser analysis done on HTTP server logs, extract only the user name, the
device, and the browser that user is using. We don’t care about the time stamp, the
resource they were accessing, or what HTTP server it came from.
```
##Applicability
```
The only major requirement is that you have duplicates values in your data set. This is
not a requirement, but it would be silly to use this pattern otherwise!
```
##Structure
```
This pattern is pretty slick in how it uses MapReduce. It exploits MapReduce’s ability to
group keys together to remove duplicates. This pattern uses a mapper to transform the
data and doesn’t do much in the reducer. The combiner can always be utilized in this
pattern and can help considerably if there are a large number of duplicates. Duplicate
records are often located close to another in a data set, so a combiner will deduplicate
them in the map phase.
map(key, record):
emit record,null
reduce(key, records):
emit key
The mapper takes each record and extracts the data fields for which we want unique
values. In our HTTP logs example, this means extracting the user, the web browser, and
the device values. The mapper outputs the record as the key, and null as the value.
The reducer groups the nulls together by key, so we’ll have one null per key. We then
simply output the key, since we don’t care how many nulls we have. Because each key is
grouped together, the output data set is guaranteed to be unique.
One nice feature of this pattern is that the number of reducers doesn’t matter in terms
of the calculation itself. Set the number of reducers relatively high, since the mappers
will forward almost all their data to the reducers.
```
##Consequences
```
The output data records are guaranteed to be unique, but any order has not been pre‐
served due to the random partitioning of the records.
```
##Known uses
###Deduplicate data
```
If you have a system with a number of collection sources that could see the same
event twice, you can remove duplicates with this pattern.
```
###Getting distinct values
```
This is useful when your raw records may not be duplicates, but the extracted in‐
formation is duplicated across records.
```
###Protecting from an inner join explosion
```
If you are about to do an inner join between two data sets and your foreign keys are
not unique, you risk retrieving a huge number of records. For example, if you have
3,000 of the same key in one data set, and 2,000 of the same key in the other data
set, you’ll end up with 6,000,000 records, all sent to one reducer! By running the
distinct pattern, you can pair down your values to make sure they are unique and
mitigate against this problem.
```
    
##Resemblances
###SQL

    SELECT DISTINCT performs this operation for us in SQL.
SELECT DISTINCT * FROM table;
###Pig

    The DISTINCT operation.
b = DISTINCT a;
##Performance analysis
```
Understanding this pattern’s performance profile is important for effective use. The
main consideration in determining how to set up the MapReduce job is the number of
reducers you think you will need. The number of reducers is highly dependent on the
total number of records and bytes coming out of the mappers, which is dependent on
how much data the combiner is able to eliminate. Basically, if duplicates are very rare
within an input split (and thus the combiner did almost nothing), pretty much all of the
data is going to be sent to the reduce phase.
You can find the number of output bytes and records by looking at the JobTracker status
of the job on a sample run. Take the number of output bytes and divide by the number
of reducers you are thinking about using. That is about how many bytes each reducer
will get, not accounting for skew. The number that a reducer can handle varies from
deployment to deployment, but usually you shouldn’t pass it more than a few hundred
megabytes. You also don’t want to pass too few records, because then your output files
will be tiny and there will be unnecessary overhead in spinning up the reducers. Aim
for each reducer to receive more than the block size of records (e.g., if your block size
is 64MB, have at least 64MB sent to the reducer).
Since most of the data in the data set is going to be sent to the reducers, you will use a relatively large number of reducers to run this job. Anywhere from one reducer per
hundred mappers, to one reducer per two mappers, will get the job done here. Start with
the theoretical estimate based on the output records, but do additional testing to find
the sweet spot. In general, with this pattern, if you want your reducers to run in half the
time, double the number of reducers... Just be careful of the files getting too small.
```
