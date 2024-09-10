<?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://www.opengis.net/kml/2.2">
<Document>
 <Style id="icon-503-DB4436-normal">
      <IconStyle>
        <color>ff3644db</color>
        <scale>1.1</scale>
        <Icon>
          <href>https://www.gstatic.com/mapspro/images/stock/503-wht-blank_maps.png</href>
        </Icon>
        <hotSpot x="16" xunits="pixels" y="32" yunits="insetPixels"/>
      </IconStyle>
      <LabelStyle>
        <scale>0</scale>
      </LabelStyle>
    </Style>
    <Style id="icon-503-DB4436-highlight">
      <IconStyle>
        <color>ff3644db</color>
        <scale>1.1</scale>
        <Icon>
          <href>https://www.gstatic.com/mapspro/images/stock/503-wht-blank_maps.png</href>
        </Icon>
        <hotSpot x="16" xunits="pixels" y="32" yunits="insetPixels"/>
      </IconStyle>
      <LabelStyle>
        <scale>1.1</scale>
      </LabelStyle>
    </Style>
    <StyleMap id="icon-503-DB4436">
      <Pair>
        <key>normal</key>
        <styleUrl>#icon-503-DB4436-normal</styleUrl>
      </Pair>
      <Pair>
        <key>highlight</key>
        <styleUrl>#icon-503-DB4436-highlight</styleUrl>
      </Pair>
    </StyleMap>
    <Style id="line-0000FF-5000-normal">
      <LineStyle>
        <color>ffff0000</color>
        <width>5</width>
      </LineStyle>
    </Style>
    <Style id="line-0000FF-5000-highlight">
      <LineStyle>
        <color>ffff0000</color>
        <width>7.5</width>
      </LineStyle>
    </Style>
    <StyleMap id="line-0000FF-5000">
      <Pair>
        <key>normal</key>
        <styleUrl>#line-0000FF-5000-normal</styleUrl>
      </Pair>
      <Pair>
        <key>highlight</key>
        <styleUrl>#line-0000FF-5000-highlight</styleUrl>
      </Pair>
    </StyleMap>
<#list places as place>
<Placemark>
 <styleUrl>#line-0000FF-5000</styleUrl>
 <name>Point: ${(place.display_name)!} </name>
<description> ${(place.cdata)!} ${(place.startDate.format('yyyy-MM-dd HH:mm:ss'))!} </description>
<Point>
<coordinates>
${(place.lon)!},${(place.lat)!},0
</coordinates>
</Point>
</Placemark>
</#list>
</Document>
</kml>