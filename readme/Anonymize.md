## Anonymize
### Description
Given a large data set of StackOverflow comments, anonymize each comment
by removing IDs, removing the time from the record, and then randomly shuffling the
records within the data set.

### Input
```
<row Id="1" 
PostId="7" 
Score="2" 
Text="Just ask this @Robert how annoying this can be..." 
CreationDate="2010-06-30T19:29:03.500" 
UserId="17" />
```
### Output
```
Randomized data.Total shuffling.
<row Text="Ok thanks mate!" CreationDate="2012-12-09" PostId="13530" />
<row Text="+1 for real world examples." CreationDate="2013-05-15" PostId="15757" />
<row Text="Well then WITHOUT even having to go to the LBS ;o) (I'd rather do these stuff at home, too)" CreationDate="2012-02-16" PostId="8129" />
...
```

