
# Inverted Index Summarizations
##Pattern Description
```
The inverted index pattern is commonly used as an example for 
MapReduce analytics.We’re going to discuss the general case 
where we want to build a map of some term toa list of identifiers.
```
##Intent
```
Generate an index from a data set to allow for faster searches or 
data enrichment capabilities.
```
##Motivation
```
It is often convenient to index large data sets on keywords, so that searches can trace
terms back to records that contain specific values. While building an inverted index
does require extra processing up front, taking the time to do so can greatly reduce the
amount of time it takes to find something.
Search engines build indexes to improve search performance. Imagine entering a key‐
word and letting the engine crawl the Internet and build a list of pages to return to you.
Such a query would take an extremely long amount of time to complete. By building an
inverted index, the search engine knows all the web pages related to a keyword ahead
of time and these results are simply displayed to the user. These indexes are often ingested
into a database for fast query responses. Building an inverted index is a fairly straight‐
forward application of MapReduce because the framework handles a majority of the
work.
```
##Applicability
```
Inverted indexes should be used when quick search query responses are required. The
results of such a query can be preprocessed and ingested into a database.
```
##Structure
![Structure](https://github.com/geftimov/MapReduce/tree/master/readme/img/InvertedIndexSummarizations.png)
##Consequences
```
The final output of is a set of part files that contain a mapping of field value 
to a set of unique IDs of records containing the associated field value.
```
##Performance analysis
```
The performance of building an inverted index depends mostly on the computational
cost of parsing the content in the mapper, the cardinality of the index keys, and the
number of content identifiers per key.
Parsing text or other types of content in the mapper can sometimes be the most com‐
putationally intense operation in a MapReduce job. This is especially true for semi-
structured data, such as XML or JSON, since these typically require parsing arbitrary
quantities of information into usable objects. It’s important to parse the incoming re‐
cords as efficiently as possible to improve your overall job performance.
If the number of unique keys and the number of identifiers is large, more data will be
sent to the reducers. If more data is going to the reducers, you should increase the
number of reducers to increase parallelism during the reduce phase.
Inverted indexes are particularly susceptible to hot spots in the index keys, since the
index keys are rarely evenly distributed. For example, the reducer that handles the word
“the” in a text search application is going to be particularly busy since “the” is seen in
so much text. This can slow down your entire job since a few reducers will take much
longer than the others. To avoid this problem, you might need to implement a custom
partitioner, or omit common index keys that add no value to your end goal.

```
