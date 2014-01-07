## Average
### Description
Given a list of posts and comments, create a structured XML hierarchy to nest
comments with their related post.

### Input
####Comment
```
<row Id="1" 
PostId="7" 
Score="2" 
Text="Just ask this @Robert how annoying this can be..." 
CreationDate="2010-06-30T19:29:03.500" 
UserId="17" />
```
####Post
```
<row Id="2"
PostTypeId="1" 
AcceptedAnswerId="43"
CreationDate="2010-08-25T19:41:19.520" 
Score="11" 
ViewCount="2228" 
Body="&lt;p&gt;Are there adjustments that can be made instead of replacing it?&lt;/p&gt;&#xA;" 
OwnerUserId="13" 
LastActivityDate="2012-12-14T22:52:58.913" 
Title="What is the easiest way to fix a loose chain?" 
Tags="&lt;chain&gt;" 
AnswerCount="4" 
CommentCount="1" />
```
### Output
```
<post CreationDate="2011-10-06T14:27:56.437" Id="10085" PostId="6356" Text="I guess those cobblestones can be a bit rough, eh?" UserId="2102"/>
	<comment ...

	/>
```

