
# Numerical Summarizations
##Pattern Description
```
The numerical summarizations pattern is a general pattern for calculating aggregate statistical values over your data is discussed in detail. Be careful of how deceptively simple this pattern is! It is extremely important to use the combiner properly and to understand the calculation you are performing.
```
##Intent
```
Group records together by a key field and calculate a numerical aggregate per group to get a top-level view of the larger data set. Consider θ to be a generic numerical summarization function we wish to execute over some list of values (v1, v2, v3, ..., vn) to find a value λ, i.e. λ = θ(v1, v2, v3, ..., vn). Examples of θ include a minimum, maximum, average, median, and standard deviation.
```
##Motivation
```
Many data sets these days are too large for a human to get any real meaning out it by reading through it manually. For example, if your website logs each time a user logs onto the website, enters a query, or performs any other notable action, it would be extremely difficult to notice any real usage patterns just by reading through terabytes of log files with a text reader. If you group logins by the hour of the day and perform a count of the number of records in each group, you can plot these counts on a histogram and recognize times when your website is more active. Similarly, if you group advertisements by types, you can determine how affective your ads are for better targeting. Maybe you want to cycle ads based on how effective they are at the time of day. All of these types of questions can be answered through numerical summarizations to get a top-level view of your data.
```
##Applicability
```
Numerical summarizations should be used when both of the following are true:
* You are dealing with numerical data or counting.
* he data can be grouped by specific fields.
```
##Structure
![Structure](https://github.com/geftimov/MapReduce/tree/master/readme/img/NumericalSummarizations.png)
##Consequences
```
The output of the job will be a set of part files containing a single record per reducer input group. Each record will consist of the key and all aggregate values. 
```
##Known uses
###Word count [ReadMe](https://github.com/geftimov/MapReduce/tree/master/readme/CommentWordCount.md)

    The “Hello World” of MapReduce. The application outputs each word of a document as the key and “1” as the value, thus grouping by words. The reduce phase then adds up the integers and outputs each unique word with the sum.

###Record count

    A very common analytic to get a heartbeat of your data flow rate on a particular interval (weekly, daily, hourly, etc.).
###Min/Max/Count [ReadMe](https://github.com/geftimov/MapReduce/tree/master/readme/MinMaxCount.md)
    
    An analytic to determine the minimum, maximum, and count of a particular event,such as the first time a user posted, the last time a user posted, and the number of times they posted in between that time period. You don’t have to collect all three of these aggregates at the same time, or any of the other use cases listed here if you are only interested in one of them.
###Average/Median/Standard deviation
    
    Similar to Min/Max/Count, but not as straightforward of an implementation because these operations are not associative. A combiner can be used for all three, but requires a more complex approach than just reusing the reducer implementation.
    
##Resemblances
###SQL

    The Numerical Aggregation pattern is analogous to using aggregates after a GROUP BY in SQL:
`SELECT MIN(numericalcol1), MAX(numericalcol1),COUNT(*) FROM table GROUP BY groupcol2;`
###Pig

    The GROUP ... BY expression, followed by a FOREACH ... GENERATE:
`b = GROUP a BY groupcol2;c = FOREACH b GENERATE group, MIN(a.numericalcol1),MAX(a.numericalcol1), COUNT_STAR(a);`
##Performance analysis

    Aggregations performed by jobs using this pattern typically perform well when the combiner is properly used. These types of operations are what MapReduce was built for. Like most of the patterns in this book, developers need to be concerned about the appropriate number of reducers and take into account any data skew that may be present in the reduce groups. That is, if there are going to be many more intermediate key/value pairs with a specific key than other keys, one reducer is going to have a lot more work to do than others.


