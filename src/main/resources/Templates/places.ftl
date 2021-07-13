<html>
<head>
  <title>Places identified by Grid Reference</title>
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

</tr>
</#list>
</TABLE>
</body>
</html>