# Image Metadata Enhancer
This program enhances the metadata information on images and helps you discover and organise photos on your drives.  

It is particularly useful for photo collections with **latitude and longitude** geographic information (e.g. usually taken on mobile phones, but also on many digital cameras) where you want to convert latitude and longitude to recognisable place addresses.  (This is known as “reverse geocoding”.) It is designed to update address information in the IPTC metadata so it is visible in tools such as Adobe Lightroom and Adobe Bridge which support the IPTC metadata standard and which can search enhanced metadata.

It is a useful tool for discovering photos on your storage drives, particularly where there are duplicates building up over time in different folders.

Image Metadata Enhancer has extensive reporting and output of metadata information, including  geographical information.
* A set of HTML pages is generated, which can be viewed, with a browser – this includes thumbnails of each photo;
* A set of KML and KMZ files are created which can be uploaded to Google Maps, so you can visualise collections as a series of tracks or points
* A JSON file containing all the metadata information for each file – the JSON file can also be used as an input file for future runs of the program or for transferring information to other systems, so it can be integrated in the workflow for bringing in new content in business environments.

IME makes extensive use of open source java libraries,  including 
*	ICAFE – read and writing IPTC data
*	Javaxt – thumbnail creation
*	Apache Commons Imaging – read and writing EXIF and Windows data 
*	Freemarker – for outputting HTML reports
*	JavaAPI for KML generation.

An Executable version is available for Windows, and a jar file can be downloaded also. Source code is available on Github. The software is currently only tested on Windows. 
