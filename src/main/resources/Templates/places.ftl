<html>
<head>
  <title>Places identified by Grid Reference</title>
    <style type="text/css">
    .img {
      border: 2px;
      padding: 10px;
    }
    </style>
</head>
<body>
  <h1>Places identified in Photos</h1>

<TABLE BORDER>
<tr>
 <th>Latitude</th>
  <th>Longitude</th>
    <th WIDTH=200>Display Name</th>
    <th WIDTH=200>Friendly Name</th>
    <th>Sub Location</th>
    <th>City</th>
     <th>State / Province</th>
    <th>Country</th>
 <th>Count</th>
 <th>First Date</th>
 <th>Last Date</th>
</tr>
<#list places as place>
<tr>
 <td>${place.lat}</td>
  <td>${place.lon}</td>
    <td>${(place.display_name)!}</td>
    <td>${(place.friendlyname)!}</td>
    <td>${(place.IPTCSublocation)!}</td>
    <td>${(place.IPTCCity)!}</td>
    <td>${(place.IPTCStateProvince)!}</td>
    <td>${(place.IPTCCountry)!}</td>
    <td>${(place.countPlace)!}</td>
    <td>${(place.startDate?datetime)!}</td>
    <td>${(place.endDate?datetime)!}</td>
</tr>
<tr>
<td COLSPAN=11>${(place.imagelinks)!}</td>
</tr>
</#list>
</TABLE>
</body>
</html>