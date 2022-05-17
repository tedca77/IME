<html>
<head>
  <title>Cameras and Scanners</title>
</head>
<body>
  <h1>Cameras and Scanners identified in metadata ${parttext}</h1>
<TABLE BORDER>
<tr>
    <th>Camera Name</th>
    <th>Make</th>
    <th>Model</th>
    <th>Software</th>
    <th>First Date Used</th>
    <th>Last Date Used</th>
    <th>Photo Count</th>
</tr>
<#list cameras as camera>
<tr>
    <td>${(camera.friendlyname)!}</td>
    <td>${(camera.cameramaker)!}</td>
    <td>${(camera.cameramodel)!}</td>
    <td>${(camera.programname)!}</td>
    <td>${(camera.startdate.format('yyyy-MM-dd  HH:mm:ss'))!}</td>
    <td>${(camera.enddate.format('yyyy-MM-dd  HH:mm:ss'))!}</td>
    <td>${(camera.cameracount)!}</td>
</tr>
</#list>
</TABLE>
</body>
</html>