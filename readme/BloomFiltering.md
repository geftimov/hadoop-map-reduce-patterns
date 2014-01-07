# Bloom Filtering
##Pattern Description
```
Bloom filtering does the same thing as the previous pattern, but it has a unique evaluation
function applied to each record.
```
##Intent
```
Filter such that we keep records that are member of some predefined set of values. It is
not a problem if the output is a bit inaccurate, because we plan to do further checking.
The predetermined list of values will be called the set of hot values .
For each record, extract a feature of that record. If that feature is a member of a set of
values represented by a Bloom filter, keep it; otherwise toss it out (or the reverse).
```
##Motivation
```
Bloom filtering is similar to generic filtering in that it is looking at each record and
deciding whether to keep or remove it. However, there are two major differences that
set it apart from generic filtering. First, we want to filter the record based on some sort
of set membership operation against the hot values. For example: keep or throw away
this record if the value in the user field is a member of a predetermined list of users.
Second, the set membership is going to be evaluated with a Bloom filter, described in
the Appendix A. In one sense, Bloom filtering is a join operation in which we don’t care
about the data values of the right side of the join.
This pattern is slightly related to the replicated join pattern covered later in Chapter 5.
It is comparing one list to another and doing some sort of join logic, using only map
tasks. Instead of replicating the hot list everywhere with the distributed cache, as in the
replicated join, we will send a Bloom filter data object to the distributed cache. This
allows a filter like operation with a Bloom filter instead of the list itself, which allows
you to perform this operation across a much larger data set because the Bloom filter is
much more compact. Instead of being constrained by the size of the list in memory, you
are mostly confined by the feature limitations of Bloom filters.
Using a Bloom filter to calculate set membership in this situation has the consequence
that sometimes you will get a false positive. That is, sometimes a value will return as a
member of the set when it should not have. If the Bloom filter says a value is not in the
Bloom filter, we can guarantee that it is indeed not in the set of values. For more infor‐
mation on why this happens, refer to Appendix A. However, in some situations, this is
not that big of a concern. In an example we’ll show code for at the end of this chapter,
we’ll gather a rather large set of “interesting” words, in which when we see a record that
contains one of those words, we’ll keep the record, otherwise we’ll toss it out. We want
to do this because we want to filter down our data set significantly by removing unin‐
teresting content. If we are using a Bloom filter to represent the list of watch words,
sometimes a word will come back as a member of that list, even if it should not have. In
this case, if we accidentally keep some records, we still achieved our goal of filtering out
the majority of the garbage and keeping interesting stuff.
```
##Applicability
```
The following criteria are necessary for Bloom filtering to be relevant:
• Data can be separated into records, as in filtering.
• A feature can be extracted from each record that could be in a set of hot values.
• There is a predetermined set of items for the hot values.
• Some false positives are acceptable (i.e., some records will get through when they
should not have).
```
##Structure
![Structure](https://github.com/geftimov/MapReduce/tree/master/readme/img/BloomFiltering.png)
```
Figure 3-2 shows the structure of Bloom filtering and how it is split into two major
components. First, the Bloom filter needs to be trained over the list of values. The re‐
sulting data object is stored in HDFS. Next is the filtering MapReduce job, which has
the same structure as the previous filtering pattern in this chapter, except it will make
use of the distributed cache as well. There are no reducers since the records are analyzed
one-by-one and there is no aggregation done.
The first step of this job is to train the Bloom filter from the list of values. This is done
by loading the data from where it is stored and adding each item to the Bloom filter. The
trained Bloom filter is stored in HDFS at a known location.
The second step of this pattern is to do the actual filtering. When the map task starts, it
loads the Bloom filter from the distributed cache. Then, in the map function, it iterates
through the records and checks the Bloom filter for set membership in the hot values
list. Each record is either forwarded or not based on the Bloom filter membership test.
The Bloom filter needs to be re-trained only when the data changes. Therefore, updating
the Bloom filter in a lazy fashion (i.e., only updating it when it needs to be updated) is
typically appropriate.
```
##Consequences
```
The output of the job will be a subset of the records in that passed the Bloom filter
membership test. You should expect that some records in this set may not actually be
in the set of hot values, because Bloom filters have a chance of false positives.
```
##Known uses
###Removing most of the nonwatched values
```
The most straightforward use case is cleaning out values that aren’t hot. For example,
you may be interested only in data that contains a word in a list of 10,000 words
that deal with Hadoop, such as “map,” “partitioning,” etc. You take this list, train a
Bloom filter on it, then check text as it is coming in to see whether you get a Bloom
filter hit on any of the words. If you do, forward the record, and if not don’t do
anything. The fact that you’ll get some false positives isn’t that big of a deal, since
you still got rid of most of the data.
```
###Prefiltering a data set for an expensive set membership check
```
Sometimes, checking whether some value is a member of a set is going to be ex‐
pensive. For example, you might have to hit a webservice or an external database
to check whether that value is in the set. The situations in which this may be the
case are far and few between, but they do crop up in larger organizations. Instead
of dumping this list periodically to your cluster, you can instead have the originating
system produce a Bloom filter and ship that instead. Once you have the Bloom filter
in place and filter out most of the data, you can do a second pass on the records that
make it through to double check against the authoritative source. If the Bloom filter
is able to remove over 95% of the data, you’ll see the external resource hit only 5%
as much as before! With this approach, you’ll eventually have 100% accuracy but
didn’t have to hammer the external resource with tons of queries.
```
    
##Resemblances
```
Bloom filters are relatively new in the field of data analysis, likely because the properties
of big data particularly benefit from such a thing in a way previous methodologies have
not. In both SQL and Pig, Bloom filters can be implemented as user-defined functions,
but as of the writing of this book, there is no native functionality out of the box.
```
##Performance analysis
```
The performance for this pattern is going to be very similar to simple filtering from a
performance perspective. Loading up the Bloom filter from the distributed cache is not
that expensive since the file is relatively small. Checking a value against the Bloom filter
is also a relatively cheap operation, as each test is executed in constant time.
```
