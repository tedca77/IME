<html>
<head>
  <title>Events identified by date</title>
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
  <h1>Events for Photos ${parttext}</h1>

<TABLE BORDER>
<tr>
 <th>Event</th>
 <th>Title</th>
 <th WIDTH=200>Description</th>
 <th WIDTH=200>Keywords</th>
 <th>Calendar Date</th>
 <th>Event Date</th>
 <th>Event Location</th>
</tr>
<#list events as event>
<tr>
 <td>${event.eventid}</td>
 <td>${event.title}</td>
 <td>${(event.description)!}</td>
 <td>${(event.keywords)!}</td>
 <td>${(event.eventcalendar)!}</td>
 <td>${(event.eventdate)!}</td>
 <td>${(event.location)!}</td>
</tr>
<tr>
<td COLSPAN=11 WIDTH=1000>
<div class="myGallery">
${(event.imagelinks)!}</td>
</div>
</tr>
</#list>
</TABLE>
</body>
</html>