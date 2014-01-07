# Structured to Hierarchical
##Pattern Description
```
The structured to hierarchical pattern creates new records from data that started in a
very different structure. Because of its importance, this pattern in many ways stands
alone in the chapter.
```
##Intent
```
Transform your row-based data to a hierarchical format, such as JSON or XML.
```
##Motivation
```
When migrating data from an RDBMS to a Hadoop system, one of the first things you
should consider doing is reformatting your data into a more conducive structure. Since
Hadoop doesn’t care what format your data is in, you should take advantage of hier‐
archical data to avoid doing joins.
For example, our StackOverflow data contains a table about comments, a table about
posts, etc. It is pretty obvious that the data is stored in an normalized SQL database.
When you visit a post on StackOverflow, all the different pieces need to be coalesced
into one view. This gets even more complicated when you are trying to do analytics at
the level of individual posts. Imagine trying to correlate the length of the post with the
length of the comments. This requires you to first do a join, an expensive operation,
then extract the data that allows you to do your real work. If instead you group the data
by post so that the comments are colocated with the posts and the edit revisions (i.e.,
denormalizing the tables), this type of analysis will be much easier and more intuitive.
Keeping the data in a normalized form in this case serves little purpose.
Unfortunately, data doesn’t always come grouped together. When someone posts an
answer to a StackOverflow question, Hadoop can’t insert that record into the hierarchy
immediately. Therefore, creating the denormalized records for MapReduce has to be
done in a batch fashion periodically.
Another way to deal with a steady stream of updates is HBase. HBase is able to store
data in a semi-structured and hierarchical fashion well. MongoDB would also be a good
candidate for storing this type of data.
```
##Applicability
```
The following should be true for this pattern to be appropriate:
• You have data sources that are linked by some set of foreign keys.
• Your data is structured and row-based.
```
##Structure
![Structure](https://github.com/geftimov/MapReduce/tree/master/readme/img/Hierarchical.png)
```
Figure 4-1 shows the structure for this pattern. The description of each component is
as follows:
• If you wish to combine multiple data sources into a hierarchical data structure, a
Hadoop class called MultipleInputs from org.apache.hadoop.mapre
duce.lib.input is extremely valuable. MultipleInputs allows you to specify dif‐
ferent input paths and different mapper classes for each input. The configuration
is done in the driver. If you are loading data from only one source in this pattern,
you don’t need this step.
• The mappers load the data and parse the records into one cohesive format so that
your work in the reducers is easier. The output key should reflect how you want to
identify the root of each hierarchical record. For example, in our StackOverflow
example, the root would be the post ID. You also need to give each piece of data
some context about its source. You need to identify whether this output record is a
post or a comment. To do this, you can simply concatenate some sort of label to the
output value text.
• In general, a combiner isn’t going to help you too much here. You could hypothet‐
ically group items with the same key and send them over together, but this has no
major compression gains since all you would be doing is concatenating strings, so
the size of the resulting string would be the same as the inputs.
• The reducer receives the data from all the different sources key by key. All of the
data for a particular grouping is going to be provided for you in one iterator, so all
that is left for you to do is build the hierarchical data structure from the list of data
items. With XML or JSON, you’ll build a single object and then write it out as output.
The examples in this section show XML, which provides several convenient meth‐
ods for constructing data structures. If you are using some other format, such as a
custom format, you’ll just need to use the proper object building and serialization
methods.
```
##Consequences
```
The output will be in a hierarchical form, grouped by the key that you specified.
However, be careful that many formats such as XML and JSON have some sort of top-
level root element that encompasses all of the records. If you actually need the document
to be well-formed top-to-bottom, it’s usually easier to add this header and footer text as some post-processing step.
```
##Known uses
###Pre-joining data
```
Data arrives in disjointed structured data sets, and for analytical purposes it would
be easier to bring the data together into more complex objects. By doing this, you
are setting up your data to take advantage of the NoSQL model of analysis.
```
###Preparing data for HBase or MongoDB
```
HBase is a natural way to store this data, so you can use this method to bring the
data together in preparation for loading into HBase or MongoDB. Creating a new
table and then executing a bulk import via MapReduce is particularly effective. The
alternative is to do several rounds of inserts, which might be less efficient.
```  
##Resemblances
###SQL

    It’s rare that you would want to do something like this in a relational database, since
storing data in this way is not conducive to analysis with SQL. However, the way
you would solve a similar problem in an RDBMS is to join the data and then perform
analysis on the result.
###Pig

    Pig has reasonable support for hierarchical data structures. You can have hierarch‐
ical bags and tuples, which make it easy to represent hierarchical structures and lists
of objects in a single record. The COGROUP method in Pig does a great job of bringing
data together while preserving the original structure. However, using the predefined
keywords to do any sort of real analysis on a complex record is more challenging
out of the box. For this, a user-defined function is the right way to go. Basically, you
would use Pig to build and group the records, then a UDF to make sense of the data.
data_a = LOAD '/data/comments/' AS PigStorage('|');
data_b = LOAD '/data/posts/' AS PigStorage(',');
grouped = COGROUP data_a BY $2, data_b BY $1;
analyzed = FOREACH grouped GENERATE udfs.analyze(group, $1, $2);
##Performance analysis
```
There are two performance concerns that you need to pay attention to when using this
pattern. First, you need to be aware of how much data is being sent to the reducers from
the mappers, and second you need to be aware of the memory footprint of the object
that the reducer builds.
Since records with the grouping key can be scattered anywhere in the data set, pretty
much all of data is going to move across the network. For this reason, you will need to
pay particular attention to having an adequate number of reducers. The same strategies
apply here that are employed in other patterns that shuffle everything over the network.
The next major concern is the possibility of hot spots in the data that could result in an
obscenely large record. With large data sets, it is conceivable that a particular output
record is going to have a lot of data associated with it. Imagine that for some reason a
post on StackOverflow has a million comments associated with it. That would be ex‐
tremely rare and unlikely, but not in the realm of the impossible. If you are building
some sort of XML object, all of those comments at one point might be stored in memory
before writing the object out. This can cause you to blow out the heap of the Java Virtual Machine, which obviously should be avoided.
Another problem with hot spots is a skew in how much data each reducer is handling.
This is going to be a similar problem in just about any MapReduce job. In many cases
the skew can be ignored, but if it really matters you can write a custom partitioner to
split the data up more evenly.
```
