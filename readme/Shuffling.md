# Shuffling
##Pattern Description
```
The total order sorting and shuffling patterns are opposites in terms of effect, but the
latter is also concerned with the order of data in records.
```
##Intent
```
You have a set of records that you want to completely randomize.
```
##Motivation
```
This whole chapter has been about applying some sort of order to your data set except
for this pattern which is instead about completely destroying the order.
The use cases for doing such a thing are definitely few and far between, but two stand
out. One is shuffling the data for the purposes of anonymizing it. Another is randomizing
the data set for repeatable random sampling.
Anonymizing data has recently become important for organizations that want to main‐
tain their users’ privacy, but still run analytics. The order of the data can provide some
information that might lead to the identity of a user. By shuffling the entire data set, the
organization is taking an extra step to anonymize the data.
Another reason for shuffling data is to be able to perform some sort of repeatable random
sampling. For example, the first hundred records will be a simple random sampling.
Every time we pull the first hundred records, we’ll get the same sample. This allows
analytics that run over a random sample to have a repeatable result. Also, a separate job
won’t have to be run to produce a simple random sampling every time you need a new
sample.
```
##Applicability
```
The main requirement here is pretty obvious: your sort key has to be comparable so the
data can be ordered.
```
##Structure
![Structure](https://github.com/geftimov/MapReduce/tree/master/readme/img/Filtering.png)
```
• All the mapper does is output the record as the value along with a random key.
• The reducer sorts the random keys, further randomizing the data.
In other words, each record is sent to a random reducer. Then, each reducer sorts on
the random keys in the records, producing a random order in that reducer.
```
##Consequences
```
Each reducer outputs a file containing random records.
```  
##Resemblances
###SQL

    The SQL equivalent to this is to order the data set by a random value, instead of
some column in the table. This makes it so each record is compared on the basis of
two random numbers, which will produce a random ordering. We don’t have to go
all the way and do a total ordering in MapReduce, as in the previous pattern. This
is because sending data to a random reducer is sufficient.
SELECT * FROM data ORDER BY RAND();
###Pig

    Shuffling in Pig can be done as we did it in SQL: performing an ORDER BY on a
random column. In this case, doing a total ordering is unnecessary. Instead, we can
GROUP BY a random key, and then FLATTEN the grouping. This effectively imple‐
ments the shuffle pattern we proposed behind the scenes.
c = GROUP b BY RANDOM();
d = FOREACH c GENERATE FLATTEN(b);
##Performance analysis
```
The shuffle has some very nice performance properties. Since the reducer each record
goes to is completely random, the data distribution across reducers will be completely
balanced. With more reducers, the data will be more spread out. The size of the files will
also be very predictable: each is the size of the data set divided by the number of reducers.
This makes it easy to get a specific desired file size as output.
Other than that, the typical performance properties for the other patterns in this chapter
apply. The pattern shuffles all of the data over the network and writes all of the data back
to HDFS, so a relatively high number of reducers should be used.
```