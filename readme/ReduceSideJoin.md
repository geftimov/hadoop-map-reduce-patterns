# ReduceSideJoin
##Pattern Description
```
The reduce side join pattern can take the longest time to execute compared to the other
join patterns, but it is simple to implement and supports all the different join operations
discussed in the previous section.
```
##Intent
```
Join large multiple data sets together by some foreign key.
```
##Motivation
```
A reduce side join is arguably one of the easiest implementations of a join in MapReduce,
and therefore is a very attractive choice. It can be used to execute any of the types of
joins described above with relative ease and there is no limitation on the size of your
data sets. Also, it can join as many data sets together at once as you need. All that said,
a reduce side join will likely require a large amount of network bandwidth because the
bulk of the data is sent to the reduce phase. This can take some time, but if you have
resources available and aren’t concerned about execution time, by all means use it! Un‐
fortunately, if all of the data sets are large, this type of join may be your only choice.
```
##Applicability
```
A reduce side join should be used when:
• Multiple large data sets are being joined by a foreign key. If all but one of the data
sets can be fit into memory, try using the replicated join.
• You want the flexibility of being able to execute any join operation.
```
##Structure
![Structure](https://github.com/geftimov/MapReduce/tree/master/readme/img/ReduceSideJoin.png)
```
• The mapper prepares the join operation by taking each input record from each of
the data sets and extracting the foreign key from the record. The foreign key is
written as the output key, and the entire input record as the output value. This output
value is flagged by some unique identifier for the data set, such as A or B if two data
sets are used. See Figure 5-1.
• A hash partitioner can be used, or a customized partitioner can be created to dis‐
tribute the intermediate key/value pairs more evenly across the reducers.
• The reducer performs the desired join operation by collecting the values of each
input group into temporary lists. For example, all records flagged with A are stored
in the ‘A’ list and all records flagged with B are stored in the ‘B’ list. These lists are
then iterated over and the records from both sets are joined together. For an inner
join, a joined record is output if all the lists are not empty. For an outer join (left,
right, or full), empty lists are still joined with non empty lists. The antijoin is done
by examining that exactly one list is empty. The records of the non-empty list are
written with an empty writable.
```
##Consequences
```
The output is a number of part files equivalent to the number of reduce tasks. Each of
these part files together contains the portion of the joined records. The columns of each
record depend on how they were joined in the reducer. Some column values will be null
if an outer join or antijoin was performed.
```
##Resemblances
###SQL

    Joins are very common in SQL and easy to execute.
SELECT users.ID, users.Location, comments.upVotes
FROM users
[INNER|LEFT|RIGHT] JOIN comments
ON users.ID=comments.UserID
###Pig

    Pig has support for inner joins and left, right, and full outer joins.
    -- Inner Join
A = JOIN comments BY userID, users BY userID;
	-- Outer Join
A = JOIN comments BY userID [LEFT|RIGHT|FULL] OUTER, users BY userID;
##Performance analysis
```
A plain reduce side join puts a lot of strain on the cluster’s network. Because the foreign
key of each input record is extracted and output along with the record and no data can
be filtered ahead of time, pretty much all of the data will be sent to the shuffle and sort
step. For this reason, reduce side joins will typically utilize relatively more reducers than
your typical analytic.
If any of the other pattern described in this chapter can be used (other than Cartesian
product), it is recommended that you do so. Sometimes this basic join pattern is the
only one that fits the circumstances.
```
