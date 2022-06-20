<html>
<head>
  <title>Warnings</title>
  <meta charset="UTF-8">
</head>
<body>
  <h1>Warnings ${parttext}</h1>

<TABLE BORDER>
<tr>

  <th WIDTH=200>Filename</th>
  <th WIDTH=200>Directory</th>
  <th>Date</th>
  <th WIDTH=400>Message</th>
</tr>
<#list comments as comment>
<tr>
    <td>${(comment.fileName)!}</td>
    <td>${(comment.directory)!}</td>
    <td>${(comment.fileDate.format('yyyy-MM-dd  HH:mm:ss'))!}</td>
    <td>${(comment.message)!}</td>
</tr>
</#list>
</TABLE>
</body>
</html>