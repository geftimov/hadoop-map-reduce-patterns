## DistributedGrep
### Description
Filter records that match regular expression.


### Input
```
 <row Id="-1"
 Reputation="1" 
 CreationDate="2010-08-25T18:10:17.440"
 DisplayName="Community"
 LastAccessDate="2010-08-25T18:10:17.440"
 Location="on the server farm"
 AboutMe="&lt;p&gt;Hi, I'm not really a person."
 Views="0"
 UpVotes="47"
 DownVotes="436"
 EmailHash="a007be5a61f6aa8f3e85ae2fc18dd66e" />
```
### Output
```
//rows with "Bulgaria in it."
  <row Id="353" Reputation="101" CreationDate="2010-09-09T08:43:42.670" DisplayName="Vladimir Grigorov" LastAccessDate="2013-05-16T14:04:59.320" Location="Sofia, Bulgaria" Views="0" UpVotes="0" DownVotes="0" EmailHash="608d484f574f28d1ae2897b859267433" />
  <row Id="1122" Reputation="101" CreationDate="2011-02-18T09:36:55.790" DisplayName="Tanparmaiel" LastAccessDate="2011-10-08T11:26:04.463" WebsiteUrl="http://tanparmaiel.blogspot.com" Location="Sofia, Bulgaria" AboutMe="&lt;p&gt;JQA&lt;/p&gt;&#xA;" Views="0" UpVotes="0" DownVotes="0" EmailHash="08a52efe5d44e017220279e981d1936a" Age="25" />
  <row Id="1926" Reputation="101" CreationDate="2011-07-31T13:52:45.210" DisplayName="Boris" LastAccessDate="2011-08-07T00:29:06.337" WebsiteUrl="http://www.virtuoza.com" Location="Bulgaria" AboutMe="Software Developer, Entrepreneur" Views="0" UpVotes="0" DownVotes="0" EmailHash="d6a4a9a484b80d97724cc69738eb6a09" Age="32" />
  <row Id="3228" Reputation="108" CreationDate="2012-01-11T12:18:28.277" DisplayName="speedyGonzales" LastAccessDate="2013-07-11T08:57:19.863" WebsiteUrl="http://www.google.com" Location="Sofia, Bulgaria" AboutMe="&lt;p&gt;Something like BS in Informatics.&#xA;Java,GWT,Android, J2EE&lt;/p&gt;&#xA;" Views="5" UpVotes="2" DownVotes="0" EmailHash="2918e31aa4ddc7818ddbb52c7a52df90" />
  <row Id="3927" Reputation="1308" CreationDate="2012-04-20T09:22:12.647" DisplayName="Vorac" LastAccessDate="2013-09-05T12:19:05.573" Location="Bulgaria" Views="29" UpVotes="368" DownVotes="2" EmailHash="065408758852145010d28c2aa811ca68" />
  <row Id="4516" Reputation="101" CreationDate="2012-07-12T08:38:04.713" DisplayName="Georgi Hristozov" LastAccessDate="2013-01-18T16:41:35.133" WebsiteUrl="http://georgi.forkbomb.nl/" Location="Sofia, Bulgaria" AboutMe="&lt;p&gt;A &lt;a href=&quot;http://fmi.uni-sofia.bg/&quot; rel=&quot;nofollow&quot;&gt;CS student&lt;/a&gt; from Sofia, Bulgaria.&lt;/p&gt;&#xA;" Views="3" UpVotes="4" DownVotes="0" EmailHash="598be8ae7b3348f50f67cae230b0cab6" Age="23" />
  <row Id="5308" Reputation="1" CreationDate="2012-10-19T09:41:06.583" DisplayName="Dichev" LastAccessDate="2012-10-19T09:41:06.583" WebsiteUrl="http://dichev.com/" Location="Bulgaria" AboutMe="&lt;p&gt;I am a lucky guy.&lt;/p&gt;&#xA;" Views="0" UpVotes="0" DownVotes="0" EmailHash="bf9bdefe63377cb68c0d7f2fff1e976b" Age="36" />
  <row Id="5778" Reputation="103" CreationDate="2012-12-27T09:57:04.690" DisplayName="Antigona" LastAccessDate="2012-12-27T19:05:46.093" WebsiteUrl="" Location="Sofia, Bulgaria" AboutMe="" Views="0" UpVotes="0" DownVotes="0" EmailHash="7c43573dea28849f26ee7b5fd13c4219" Age="23" />
  <row Id="7858" Reputation="1" CreationDate="2013-08-20T07:19:11.273" DisplayName="user2320677" LastAccessDate="2013-08-20T07:19:11.273" Location="Sofia, Bulgaria" AboutMe="&#xA;  I am Bozh&#xA;&#xA;" Views="0" UpVotes="0" DownVotes="0" EmailHash="636929e7902720d8875bda63ff8ea174" Age="29" />

```