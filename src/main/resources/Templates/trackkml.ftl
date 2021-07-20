<?xml version="1.0" encoding="UTF-8"?><kml xmlns="http://www.opengis.net/kml/2.2">         <Document>
<Placemark><name>test</name>
<description>test Desc</description>
<LineString>
<coordinates>
<#list points as point>
<tr>
    <td>${(track.trackKey)!}</td>
    <td>${(track.friendlyName)!}</td>
    <td>${(track.placeCount)!}</td>
    <td>${(track.startDate?datetime)!}</td>
    <td>${(track.endDate?datetime)!}</td>

</#list>
${(point.trackKey)!},${(point.trackKey)!}
-80.54400115,43.4250264
-80.52674314,43.43127701
-80.5274517,43.43458707
-80.53223781,43.43876923
-80.54385782,43.44993036
-80.53949137,43.45723788
-80.53950793,43.46780893
-80.53352615,43.4730443
-80.53491389,43.47816267
-80.54136061,43.48417145
-80.54163034,43.48439869
</coordinates>
</LineString>
</Placemark></Document>
</kml>