<html>
<head>
  <title>Events identified by date</title>
    <style type="text/css">
        .padding{
         padding:2px 10px 2px 2px;
        }
        </style>
</head>
<body>
  <h1>Events for Photos</h1>

<TABLE BORDER>
<tr>
 <th>Event</th>
 <th>Title</th>
 <th WIDTH=200>Description</th>
 <th WIDTH=200>Keywords</th>
 <th>Calendar Date</th>
 <th>Event Date</th>
 <th>Event Location</th>
</tr>
<#list events as event>
<tr>
 <td>${event.eventid}</td>
 <td>${event.title}</td>
 <td>${(event.description)!}</td>
 <td>${(event.keywords)!}</td>
 <td>${(event.eventcalendar)!}</td>
 <td>${(event.eventdate)!}</td>
 <td>${(event.location)!}</td>
</tr>
<tr>
<td COLSPAN=11 WIDTH=1000>${(event.imagelinks)!}</td>
</tr>
</#list>
</TABLE>
</body>
</html>