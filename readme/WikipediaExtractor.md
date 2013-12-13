## WikipediaExtractor
### Description
Given a set of user’s comments, build an inverted index of Wikipedia URLs to
a set of answer post IDs .



### Input
```
<row Id="1" 
PostTypeId="1" 
AcceptedAnswerId="22" 
CreationDate="2010-06-30T18:46:26.453" 
Score="121" 
ViewCount="7150" 
Body="&lt;p&gt;I don't trust Facebook's new privacy settings 
and have decided to delete my account, but I can't find the 
kill switch.&lt;/p&gt;&#xA;&#xA;&lt;p&gt;Does anyone know how 
I can delete my Facebook account?&lt;/p&gt;&#xA;" 
OwnerUserId="17" 
LastActivityDate="2013-08-14T19:03:51.817" 
Title="How do I delete my Facebook account?" 
Tags="&lt;facebook&gt;&lt;delete&gt;&lt;account-management&gt;" 
AnswerCount="4" 
CommentCount="3" 
FavoriteCount="12" />
```
### Output
```
http://de.wikipedia.org/wiki/m%c3%bcnchen	18437
http://en.m.wikipedia.org/	22918 22912
http://en.m.wikipedia.org/wiki/wikipedia%3asyndication	44780
http://en.wikipedia.org/	20435 2475
...
https://en.wikipedia.org/wiki/special%3apreferences	44083
https://en.wikipedia.org/wiki/webdav	39246
https://en.wikipedia.org/wiki/wikipedia%3anavframe	37280
https://en.wikipedia.org/wiki/wuala	36284
```
