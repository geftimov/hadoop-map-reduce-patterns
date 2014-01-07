# Top Ten
##Pattern Description
```
The top ten pattern is a bit different than previous ones in that you know how many
records you want to get in the end, no matter what the input size. In generic filtering,
however, the amount of output depends on the data.
```
##Intent
```
Retrieve a relatively small number of top K records, according to a ranking scheme in
your data set, no matter how large the data.
```
##Motivation
```
Finding outliers is an important part of data analysis because these records are typically
the most interesting and unique pieces of data in the set. The point of this pattern is to
find the best records for a specific criterion so that you can take a look at them and
perhaps figure out what caused them to be so special. If you can define a ranking function
or comparison function between two records that determines whether one is higher
than the other, you can apply this pattern to use MapReduce to find the records with
the highest value across your entire data set.
The reason why this pattern is particularly interesting springs from a comparison with
how you might implement the top ten pattern outside of a MapReduce context. In SQL,
you might be inclined to sort your data set by the ranking value, then take the top K
records from that. In MapReduce, as we’ll find out in the next chapter, total ordering is
extremely involved and uses significant resources on your cluster. This pattern will in‐
stead go about finding the limited number of high-values records without having to sort
the data.
Plus, seeing the top ten of something is always fun! What are the highest scoring posts
on Stack Overflow? Who is the oldest member of your service? What is the largest single
order made on your website? Which post has the word “meow” the most number of
times?
```
##Applicability
```
• This pattern requires a comparator function ability between two records. That is,
we must be able to compare one record to another to determine which is “larger.”
• The number of output records should be significantly fewer than the number of
input records because at a certain point it just makes more sense to do a total or‐
dering of the data set.
```
##Structure
![Structure](https://github.com/geftimov/MapReduce/tree/master/readme/img/TopTen.png)
```
This pattern utilizes both the mapper and the reducer. The mappers will find their local
top K, then all of the individual top K sets will compete for the final top K in the reducer.
Since the number of records coming out of the mappers is at most K and K is relatively
small, we’ll only need one reducer. You can see the structure of this pattern in Figure 3-3.

class mapper:
setup():
initialize top ten sorted list
map(key, record):
insert record into top ten sorted list
if length of array is greater-than 10 then
truncate list to a length of 10
cleanup():
for record in top sorted ten list:
emit null,record
class reducer:
setup():
initialize top ten sorted list
reduce(key, records):
sort records
truncate records to top 10
for record in records:
emit record

The mapper reads each record and keeps an array object of size K that collects the largest
K values. In the cleanup phase of the mapper (i.e., right before it exits), we’ll finally emit
the K records stored in the array as the value, with a null key. These are the lowest K for
this particular map task.
We should expect K * M records coming into the reducer under one key, null, where M
is the number of map tasks. In the reduce function, we’ll do what we did in the mapper:
keep an array of K values and find the top K out of the values collected under the null
key.
The reason we had to select the top K from every mapper is because it is conceivable
that all of the top records came from one file split and that corner case needs to be
accounted for.
```
##Consequences
```
The top K records are returned.
```
##Known uses
###Outlier analysis
```
Outliers are usually interesting. They may be the users that are having difficulty
using your system, or power users of your website. Outliers, like filtering and
grouping, may give you another perspective from your data set.
```
###Select interesting data
```
If you are able to score your records by some sort of value score, you can pull the
“most valuable” data. This is particularly useful if you plan to submit data to follow-
on processing, such as in a business intelligence tool or a SQL database, that cannot
handle the scale of your original data set. Value scoring can be as complex as you
make it by applying advanced algorithms, such as scoring text based on how gram‐
matical it is and how accurate the spelling is so that you remove most of the junk.
```
##Catchy dashboards
```
This isn’t a psychology book, so who knows why top ten lists are interesting to
consumers, but they are. This pattern could be used to publish some interesting top
ten stats about your website and your data that will encourage users to think more
about your data or even to instill some competition.
```
##Performance analysis
```
The performance of the top ten pattern is typically very good, but there are a number
of important limitations and concerns to consider. Most of these limitations spring from
the use of a single reducer, regardless of the number of records it is handling.
```
