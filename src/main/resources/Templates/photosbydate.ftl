<html>
<head>
  <title>Photos By Date</title>
  <meta charset="UTF-8">
</head>
<body>
  <h1>Photos by Date ${parttext}</h1>

<TABLE  style="table-layout: fixed; width: 100%" BORDER>
<tr>
  <th style="width: 60px;">File Key</th>
  <th style="width:200px;">Picture</th>
  <th style="width:100px;">Filename</th>
  <th style="width:100px;">Directory</th>
  <th style="width:100px;">Sub Location</th>
  <th style="width:100px;">City</th>
  <th style="width:100px;">State / Province</th>
  <th style="width:200px;">Location Name</th>
  <th style="width:200px;">IPTC Data</th>
  <th style="width:200px;">EXIF Data</th>
  <th style="width: 80px;">Date</th>
  <th style="width: 50px;">Place Key</th>
  <th style="width: 50px;">Events</th>
</tr>
<#list photos as photo>
<tr>
    <td style="width: 60px;">${(photo.fileKey)!}</td>
    <td>
    <#assign test = "${(photo.thumbnail)!}">
    <#if test?has_content>
        <img src="${root}//${(photo.thumbnail)!}" width="200">
    <#else>
       No thumbnail
    </#if>
    </td>
    <td style="width:100px;word-wrap: break-word">${(photo.fileName)!}</td>
    <td style="word-wrap: break-word">${(photo.directory)!}</td>
    <td style="word-wrap: break-word">${(photo.subLocation)!}</td>
    <td style="word-wrap: break-word">${(photo.city)!}</td>
    <td style="word-wrap: break-word">${(photo.stateProvince)!}</td>
    <td style="word-wrap: break-word">${(photo.displayName)!}</td>
    <td style="width:200px;word-wrap: break-word">
        <#assign test2 = "${(photo.IPTCObjectName)!}">
        <#if test2?has_content>
            <b>ObjectName:</b> ${(photo.IPTCObjectName)!}
        </#if>
        <#assign test3 = "${(photo.IPTCCaptionAbstract)!}">
        <#if test3?has_content>
            <br><b>CaptionAbstract:</b> ${(photo.IPTCCaptionAbstract)!}
        </#if>
        <#assign test4 = "${(photo.IPTCKeywords)!}">
        <#if test4?has_content>
            <br><b>Keywords:</b> ${(photo.IPTCKeywords)!}
        </#if>
        <#assign test8 = "${(photo.IPTCCopyright)!}">
                <#if test8?has_content>
                    <br><b>Copyright:</b> ${(photo.IPTCCopyright)!}
        </#if>
         <#assign test10 = "${(photo.IPTCCategory)!}">
                        <#if test10?has_content>
                        <br><b>Category:</b> ${(photo.IPTCCategory)!}
                 </#if>
         <#assign test11 = "${(photo.IPTCDateCreated)!}">
                <#if test11?has_content>
                <br><b>DateCreated:</b> ${(photo.IPTCDateCreated)!}
         </#if>
    </td>
    <td>
        <#assign test5 = "${(photo.windowsTitle)!}">
        <#if test5?has_content>
            <b>Title:</b> ${(photo.windowsTitle)!}<br>
        </#if>
        <#assign test6 = "${(photo.windowsSubject)!}">
        <#if test6?has_content>
            <b>Subject:</b> ${(photo.windowsSubject)!}
        </#if>
        <#assign test7 = "${(photo.windowsKeywords)!}">
        <#if test7?has_content>
            <br><b>Keywords:</b> ${(photo.windowsKeywords)!}
        </#if>
    </td>
    <td style="width: 80px;">${(photo.bestDate.format('yyyy-MM-dd  HH:mm:ss'))!}</td>
    <td style="width: 50px;">${(photo.placeKey)!}</td>
    <td style="width: 50px;">${(photo.eventKeys)!}</td>
</tr>
</#list>
</TABLE>
</body>
</html>