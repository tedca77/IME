<html>
<head>
  <title>Photos By Date</title>
</head>
<body>
  <h1>Photos by Date</h1>

<TABLE BORDER>
<tr>
  <th>File Key</th>
  <th WIDTH=100>Picture</th>
  <th WIDTH=200>Filename</th>
  <th WIDTH=200>Directory</th>
  <th WIDTH=200>Sub Location</th>
  <th WIDTH=200>City</th>
  <th WIDTH=200>State / Province</th>
  <th WIDTH=400>Display Name</th>
  <th>Place Key</th>
  <th>Date</th>
</tr>
<#list photos as photo>
<tr>
    <td>${(photo.fileKey)!}</td>
    <td>

    <#assign test = "${(photo.thumbnail)!}">
    <#if test?has_content>
        <img src="${root}//${(photo.thumbnail)!}" width="200">
    <#else>
       No thumbnail
    </#if>
    </td>
    <td>${(photo.fileName)!}</td>
    <td>${(photo.directory)!}</td>
    <td>${(photo.subLocation)!}</td>
    <td>${(photo.city)!}</td>
    <td>${(photo.stateProvince)!}</td>
    <td>${(photo.displayName)!}</td>
    <td>${(photo.placeKey)!}</td>
    <td>${(photo.bestDate.format('yyyy-MM-dd  HH:mm:ss'))!}</td>
</tr>
</#list>
</TABLE>
</body>
</html>