## BinningTags
### Description
Given a set of StackOverflow posts, bin the posts into four bins based on the
tags hadoop, pig, hive, and hbase. Also, create a separate bin for posts mentioning ha‐
doop in the text or title.


### Input
```
<row Id="1" 
PostTypeId="1" 
AcceptedAnswerId="29" 
CreationDate="2010-08-25T19:41:17.837" 
Score="28" 
ViewCount="15427" 
Body="&lt;p&gt;I keep losing pressure in my tires, and among other things."
OwnerUserId="7" 
LastEditorUserId="4239" 
LastEditDate="2013-08-30T18:06:36.450" 
LastActivityDate="2013-08-30T20:25:22.773" 
Title="Can I use a Presta tube in a Schrader rim?" 
Tags="&lt;mountain-bike&gt;&lt;innertube&gt;&lt;presta&gt;&lt;schrader&gt;" 
AnswerCount="11" 
FavoriteCount="3" />
```
### Output
```
4 different files : hadoop-tag-m-xxxx,hbase-tag-m-xxxx ... etc.
```

