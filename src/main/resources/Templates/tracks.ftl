<html>
<head>
  <title>Tracks</title>
</head>
<body>
  <h1>Tracks</h1>
<TABLE BORDER>
<tr>
    <th>Track No</th>
    <th>Friendly Name</th>
    <th>Points</th>
    <th>Start Time</th>
    <th>End Time</th>
</tr>
<#list tracks as track>
<tr>
    <td>${(track.trackKey)!}</td>
    <td>${(track.friendlyName)!}</td>
    <td>${(track.placeCount)!}</td>
    <td>${(track.startDate?datetime)!}</td>
    <td>${(track.endDate?datetime)!}</td>
</tr>
<tr>
<td COLSPAN=5, WIDTH=1000>${(track.imagelinks)!}</td>
</tr>
</#list>
</TABLE>
</body>
</html>