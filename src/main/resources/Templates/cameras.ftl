<html>
<head>
  <title>Cameras and Scanners</title>
</head>
<body>
  <h1>Cameras and Scanners identified in metadata</h1>
<TABLE BORDER>
<tr>
    <th>Make</th>
    <th>Model</th>
    <th>First Date Used</th>
    <th>Last Date Used</th>
</tr>
<#list cameras as camera>
<tr>
    <td>${camera.cameraMaker}</td>
    <td>${camera.cameraModel}</td>
    <td>${camera.startDate?datetime}</td>
    <td>${camera.endDate?datetime}</td>
</tr>
</#list>
</TABLE>
</body>
</html>