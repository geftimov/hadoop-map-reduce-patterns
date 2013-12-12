## MinMaxCount
### Description
Given a list of user’s comments, determine the first and last time a user commented and the total number of comments from that user.


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
//userId firstComment lastComment totalComment
10	2010-07-01T14:21:43.903	2010-07-10T04:51:28.953	7
100	2010-07-08T11:42:24.297	2012-08-08T10:06:30.780	28
10002	2012-06-16T03:17:10.297	2012-11-14T14:28:50.067	3
1002	2010-07-07T21:38:42.127	2010-07-07T21:38:42.127	1
10022	2011-04-22T10:35:05.713	2011-04-22T10:35:05.713	1
10025	2011-06-06T11:17:15.313	2011-06-06T11:17:52.100	2
10026	2012-05-15T14:22:17.387	2012-05-15T14:22:17.387	1
...
10027	2013-04-24T16:52:02.790	2013-04-24T16:53:34.860	2
10039	2011-06-14T10:36:14.333	2011-06-14T10:36:14.333	1
1004	2012-05-08T09:05:21.957	2013-08-18T18:33:50.937	40
10056	2012-08-28T17:19:28.623	2013-04-18T04:57:45.010	2
```

