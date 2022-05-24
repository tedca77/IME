<html>
<head>
  <title>Tracks</title>
  <meta charset="UTF-8">
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
  <h1>Tracks ${parttext}</h1>
<TABLE BORDER >
<tr>
    <th>Track No</th>
    <th>Number of Points</th>
    <th>Start Time</th>
    <th>End Time</th>
</tr>
<#list tracks as track>
<tr>
    <td>${(track.trackKey)!}</td>
    <td>${(track.countTrack)!}</td>
    <td>${(track.startDate.format('yyyy-MM-dd HH:mm:ss'))!}</td>
    <td>${(track.endDate.format('yyyy-MM-dd HH:mm:ss'))!}</td>
</tr>
<tr>
 <td COLSPAN=4>${(track.startAndEndPlace)!}</td>
</tr>
<tr>
<td COLSPAN=4, WIDTH=1000>
<div class="myGallery">
${(track.imageLinks)!}</td>
</div>
</tr>
</#list>
</TABLE>
</body>
</html>