<html>
<head>
  <title>Places identified by Grid Reference</title>
    <style type="text/css">
    .myGallery {
      display: grid;
      grid-gap: 10px;
      grid-template-columns: repeat(auto-fit, minmax(240px, 240px));
       background-color: #E0E0E0;
    }
     .myGallery .item {
       position: relative;
       overflow: hidden;
     }

     .myGallery .item img {
       vertical-align: left;
         width: 240px;
     }

     .myGallery .caption {
       margin: 0;
       padding: 1em;
       position: absolute;
       z-index: 1;
       bottom: 0;
       left: 0;
       width: 100%;
       max-height: 100%;
       overflow: auto;
       box-sizing: border-box;
       transition: transform .5s;
       transform: translateY(100%);
       background: rgba(0, 0, 0, .7);
       color: rgb(255, 255, 255);
     }

     .myGallery .item:hover .caption {
       transform: translateY(0%);
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

<div class="myGallery">
${(place.imagelinks)!}</td>

</div>
</tr>
</#list>
</TABLE>
</body>
</html>