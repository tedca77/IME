<html>
<head>
  <title>Places identified by Grid Reference</title>
    <style type="text/css">
        .padding{
         padding:2px 2px 2px 2px;
        }
        .image-container {
          flex-direction: row
          float: left;
          display: flex;
          flex-wrap: wrap;
          max-width: 1500px;
          justify-content: flex-start
          align-content:flex-start
          flex-basis: auto
        }

        .cat-container {
          flex: 0 0 10%;
          padding: 0px 1rem;
          box-sizing: border-box;
        }

        img.cat {
          width: 100%;
          height: auto;
        }

        .caption {
          font-size: 14px;
          text-align: center;
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
    <td>${(place.startDate.format('yyyy-MM-dd  HH:mm:ss'))!}</td>
    <td>${(place.endDate.format('yyyy-MM-dd  HH:mm:ss'))!}</td>
</tr>
<tr>
<td COLSPAN=11 WIDTH=1000>

<div class="image-container">
${(place.imagelinks)!}</td>

</div>
</tr>
</#list>
</TABLE>
</body>
</html>