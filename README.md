MapReduce
=========

Hadoop Map-Reduce Design Patterns

How to use the MapReduce:
--------------------------

Clone the repository:
```
git clone git@github.com:geftimov/MapReduce.git
```
Go in to the folder:
```
cd MapReduce
```
Build it with Maven:
```
mvn clean install
```
Example run in each individual pattern example.

#### Summarization Patterns 
>#####1. Numerical Summarization [ReadMe](https://github.com/geftimov/MapReduce/tree/master/readme/NumericalSummarization.md)
>*   [CommentWordCount](https://github.com/geftimov/MapReduce/tree/master/readme/CommentWordCount.md)
>*   [MinMaxCount](https://github.com/geftimov/MapReduce/tree/master/readme/MinMaxCount.md)
>*   [Average](https://github.com/geftimov/MapReduce/tree/master/readme/Average.md)
>*   [MedianStdDev](https://github.com/geftimov/MapReduce/tree/master/readme/MedianStdDev.md) (With In-Memory Map)
>*   [MedianAndStandardDeviationCommentLengthByHour](https://github.com/geftimov/MapReduce/tree/master/readme/MedianAndStandardDeviationCommentLengthByHour.md) (Without the Map, more efficient)
>
>#####2.  Inverted Index Summarization [ReadMe](https://github.com/geftimov/MapReduce/tree/master/readme/InvertedIndexSummarization.md)
>*   [WikipediaExtractor](https://github.com/geftimov/MapReduce/tree/master/readme/WikipediaExtractor.md)
>
>#####3.  Counting with Counters [ReadMe](https://github.com/geftimov/MapReduce/tree/master/readme/CountingCounters.md)
>*   [CountNumUsersByState](https://github.com/geftimov/MapReduce/tree/master/readme/CountNumUsersByState.md)

#### Filtering Patterns 
>#####1. Filtering [ReadMe](https://github.com/geftimov/MapReduce/tree/master/readme/Filtering.md)
>*   [DistributedGrep](https://github.com/geftimov/MapReduce/tree/master/readme/DistributedGrep.md)
>*   [SimpleRandomSampling](https://github.com/geftimov/MapReduce/tree/master/readme/SimpleRandomSampling.md)

>#####2. Bloom Filtering [ReadMe](https://github.com/geftimov/MapReduce/tree/master/readme/BloomFiltering.md)

>#####3. Top Ten [ReadMe](https://github.com/geftimov/MapReduce/tree/master/readme/TopTen.md)
>*   [TopTenUsersByReputation](https://github.com/geftimov/MapReduce/tree/master/readme/TopTenUsersByReputation.md)

>#####4. Distinct [ReadMe](https://github.com/geftimov/MapReduce/tree/master/readme/Distinct.md)
>*   [DistinctUser](https://github.com/geftimov/MapReduce/tree/master/readme/DistinctUser.md)

#### Data Organization Patterns 
>#####1. Structured to Hierarchical [ReadMe](https://github.com/geftimov/MapReduce/tree/master/readme/Hierarchical.md)
>*   [PostCommentHierarchy](https://github.com/geftimov/MapReduce/tree/master/readme/PostCommentHierarchy.md)
>*   [QuestionAnswerBuilder](https://github.com/geftimov/MapReduce/tree/master/readme/QuestionAnswerBuilder.md)

>#####2. Partitioning [ReadMe](https://github.com/geftimov/MapReduce/tree/master/readme/Partitioning.md)
>*   [LastAccessDate](https://github.com/geftimov/MapReduce/tree/master/readme/LastAccessDate.md)

>#####3. Binning [ReadMe](https://github.com/geftimov/MapReduce/tree/master/readme/Binning.md)
>*   [BinningTags](https://github.com/geftimov/MapReduce/tree/master/readme/BinningTags.md)

>#####4. TotalOrderSorting [ReadMe](https://github.com/geftimov/MapReduce/tree/master/readme/TotalOrderSorting.md)
>*   [TotalOrderSortingStage](https://github.com/geftimov/MapReduce/tree/master/readme/TotalOrderSortingStage.md)

>#####5. Shuffling [ReadMe](https://github.com/geftimov/MapReduce/tree/master/readme/Shuffling.md)
>*   [Anonymize](https://github.com/geftimov/MapReduce/tree/master/readme/Anonymize.md)

#### Join Patterns 
>#####1. Reduce Side Join [ReadMe](https://github.com/geftimov/MapReduce/tree/master/readme/ReduceSideJoin.md)
>*   [UserJoin](https://github.com/geftimov/MapReduce/tree/master/readme/UserJoin.md)
>*   [UserJoinBloomFilter](https://github.com/geftimov/MapReduce/tree/master/readme/UserJoinBloomFilter.md)

>#####2. Replicated Join [ReadMe](https://github.com/geftimov/MapReduce/tree/master/readme/ReplicatedJoin.md)
>*   [ReplicatedUserJoin](https://github.com/geftimov/MapReduce/tree/master/readme/ReplicatedUserJoin.md)

>#####2. Composite Join [ReadMe](https://github.com/geftimov/MapReduce/tree/master/readme/CompositeJoin.md)


About Me
---------
```
Georgi Kalinov Eftimov
jokatavr@gmail.com
```
