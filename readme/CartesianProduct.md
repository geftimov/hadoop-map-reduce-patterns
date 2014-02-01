# CartesianProduct
##Pattern Description
```
The Cartesian product pattern is an effective way to pair every record of multiple inputs
with every other record. This functionality comes at a cost though, as a job using this
pattern can take an extremely long time to complete.
```
##Intent
```
Pair up and compare every single record with every other record in a data set.
```
##Motivation
```
A Cartesian product allows relationships between every pair of records possible between
one or more data sets to be analyzed. Rather than pairing data sets together by a foreign
key, a Cartesian product simply pairs every record of a data set with every record of all
the other data sets.
With that in mind, a Cartesian product does not fit into the MapReduce paradigm very
well because the operation is not intuitively splittable, cannot be parallelized very well,
and thus requires a lot of computation time and a lot of network traffic. Any prepro‐
cessing of that data that can be done to improve execution time and reduce the byte
count should be done to improve runtimes.
It is very rare that you would need to do a Cartesian product, but sometimes there is
simply no foreign key to join on and the comparison is too complex to group by ahead
of time. Most use cases for using a Cartesian product are some sort of similarity analysis
on documents or media.
```
##Applicability
```
Use a Cartesian product when:
• You want to analyze relationships between all pairs of individual records.
• You’ve exhausted all other means to solve this problem.
• You have no time constraints on execution time.
```
##Structure
![Structure](https://github.com/geftimov/MapReduce/tree/master/readme/img/CartesianProduct.png)
```
• The cross product of the input splits is determined during job setup and configu‐
ration. After these are calculated, each record reader is responsible for generating
the cross product from both of the splits it is given. The record reader gives a pair
of records to a mapper class, which simply writes them both out to the file system.
See Figure 5-5.
• No reducer, combiner, or partitioner is needed. This is a map-only job.
```
##Consequences
```
The final data set is made up of tuples equivalent to the number of input data sets. Every
possible tuple combination from the input records is represented in the final output.
```
##Resemblances
```
SQL
	Although very rarely seen, the Cartesian product is the syntactically simplest of all
joins in SQL. Just select from multiple tables without a where clause.
SELECT * FROM tablea, tableb;
Pig
	Pig can perform a Cartesian product using the CROSS statement. It also comes
along with a warning that it is an expensive operation and should be used sparingly.
A = LOAD 'data1' AS (a1, a2, a3);
DUMP A;
(1,2,3)
(4,5,6)
B = LOAD 'data2' AS (b1, b2);
DUMP B;
(1,2)
(3,4)
(5,6)
C = CROSS A, B;
DUMP C;
(1,2,3,1,2)
(1,2,3,3,4)
(1,2,3,5,6)
(4,5,6,1,2)
(4,5,6,3,4)
(4,5,6,5,6)
```
##Performance analysis
```
The Cartesian product produces a massive explosion in data size, as even a self-join of
a measly million records produces a trillion records. It should be used very sparingly
because it will use up many map slots for a very long time. This will dramatically increase
the run time of other analytics, as any map slots taken by a Cartesian product are un‐
usable by other jobs until completion. If the number of tasks is greater than or equal to
the total number of map slots in the cluster, all other work won’t get done for quite some
time.
Each input split is paired up with every other input split—effectively creating a data set
of O(n 2 ), n being the number of bytes. A single record is read from the left input split,
and then the entire right input split is read and reset before the second record from the
left input split is read. If a single input split contains a thousand records, this means the
right input split needs to be read a thousand times before the task can finish. This is a
massive amount of processing time! If a single task fails for an odd reason, the whole
thing needs to be restarted! You can see why a Cartesian product is a terrible, terrible
thing to do in MapReduce.
```