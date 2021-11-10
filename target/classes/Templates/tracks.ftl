<html>
<head>
  <title>Tracks</title>
 <style type="text/css">
    .padding{
     padding:2px 10px 2px 2px;
    }
    </style>
</head>
<body>
  <h1>Tracks</h1>
<TABLE BORDER >
<tr>
    <th>Track No</th>
    <th>Number of Points</th>
    <th>Start Time</th>
    <th>End Time</th>
</tr>
<#list tracks as track>
<tr>
    <td>${(track.trackKey)!}</td>
    <td>${(track.placeCount)!}</td>
    <td>${(track.startDate.format('yyyy-MM-dd HH:mm:ss'))!}</td>
    <td>${(track.endDate.format('yyyy-MM-dd HH:mm:ss'))!}</td>
</tr>
<tr>
 <td COLSPAN=4>${(track.startAndEndPlace)!}</td>
</tr>
<tr>
<td COLSPAN=4, WIDTH=1000>${(track.imageLinks)!}</td>
</tr>
</#list>
</TABLE>
</body>
</html>