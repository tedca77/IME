<?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://www.opengis.net/kml/2.2">
<Document>
<#list tracks as track>
<Placemark><name>Track: ${(track.trackKey)!} Date: ${(track.startDate?datetime)!}</name>
<description> ${(track.startAndEndPlace)!} </description>
<LineString>
<coordinates>
${(track.coordinates)!}
</coordinates>
</LineString>
</Placemark>
</#list>
</Document>
</kml>