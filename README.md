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
# Why is this tool needed?
1.	Most personal photo libraries are disorganised  – this tool helps discover duplicates and organises files by the date the photo was taken.
2.	Many Cloud based photo libraries provide reverse geocoding when a file is uploaded.  However, you may not want to put all their photos in a single Cloud environment, due to the ongoing cost of storage.  Many Cloud systems do not update the metadata within photos, so any enhanced metadata created is of no use if the photos are transferred to other storage environments.  This tool is designed for people with collections on local disk drives or SANs who require similar geocoding facilities but without additional cost.
3.	It is currently difficult to review geographical metadata across a collection of images – in Windows you have to select properties on each photo in turn.  IME helps you view the geographical information and remind you of trips and visits, and helps to identify where photos have been taken.
4.	Windows thumbnail generation can be slow for large collections – IME generates thumbnails which are faster to view in the HTML reports.
5.	Adobe Lightroom “Classic” is a great tool – but reverse Geocoding is only possible by purchasing one of the Lightroom Cloud-based packages.  There is a plug in for Lightroom Classic which does something similar, but this tool is independent of Lightroom.
# About Photo Metadata
Photo metadata in stored within each image file (such as JPEG files which are produced by most mobile phones).  The metadata is organised in a set of categories.  These categories include:
* EXIF – this contains the longitude and latitude information, as well as extensive information on the camera / phone,  date of photo etc.
* IPTC – this contains the additional geographical information e.g. City, State /Province, Country etc.  Windows Explorer and Windows Properties do not show most of the IPTC data – the easiest way to see this is using the freeware IrfanView,( a wonderful image viewer and editor which is an essential for sizing and cropping photos).  ExifTool is another program that can be used.  You can also use Adobe Lightroom (which I also use) and other professional software most of which is quite expensive. Adobe Bridge is free software which provides equivalent searching for IPTC metadata.
* JPEG Comments – this section allows multiple comments to be entered. (Adobe LightRoom Classic does not show these comments).
* Windows metadata (e.g. Title, Comments, Subject) – Windows shows some of the EXIF and IPTC data and in some cases, it uses the EXIF and IPTC data – the Windows metadata is  available by right clicking on a a file and selecting “Properties” in Windows. 
One of the difficulties is that different programs given metadata different names, so it is not always that clear which item of metadata is referred to.  At the end of this documentation, there is a table with the key metadata items relevant to this program.  (See further information.) 
# Places
Each longitude and latitude value found is represented as a “Place” in IME.  If two photos are taken at virtually the same place (and have very similar latitude and longitude), then this is identified as the same “Place” in IME and IME does not have to use the Open Street Map Service. (This is a good thing, because Open Street Map is a limited service, and condition of use is that it is not swamped with requests.)  The user can specify the distance that determines if the Place is the same (in metres).  Also, the geocoding may not identify the exact address e.g. house numbers may be slightly out.  IME can also be given a set of known Places in the JSON, before it runs, where the correct house address can be provided. e.g. if 86 Acacia Avenue is found, but the actual address is “85 Acacia Avenue”, then this can be provided and also given a user friendly name e.g. “Our first house”. (Each Place added is given a unique number which can be used to allocate images to this place if they do not have longitude and latitude – see next section.)
# Adding geolocation information to files without Latitude and Longitude
For photos which do not have geographical information (e.g., scanned photos and photos from cameras without geolocation) , geographical details can be added by adding an instruction to the metadata,  in one of three ways before processing:
* Through adding the latitude and longitude – enter ```#latlon:lat,long (no spaces)```
* Through adding a national Post Code – enter ```#postcode: SW1A1AA (no spaces)```
* Through adding a Place Identifier – enter ```#place: 1```.

If you only use Windows for editing and managing your collection, put the above value somewhere in the Windows Comments Field (with a space after the values) – if you are using Adobe Lightroom or Adobe Bridge, put the value in the Instructions (IPTC) field.
Note that Windows and tools such as Lightroom allow you to modify metadata on multiple fields at the same time so if you have a group of photos that are all taken at the same place you can add in one go. 
# Keeping Track of Changes
On completion of processing, IME will add a value to the following fields:
1.	JPEG Comments section – is modified with a new comment for each modification
2.	Instructions (IPTC) metadata 
3.	Windows Comments metadata
In each case, the command which had been entered will be updated with “DONE” in the three fields described above.) For example:
```
#geocodeDONE:50.43,-2.444
#latlonDONE:50.43,-2.444
#placeDONE: 34
#postcodeDONE:SW1A1AA
```
# Moving Files to a New Directory
One of the things that IME can do is move all photos to a new directory structure.  This is of the structure 
```YEAR / MONTH / FILENAME``` ie.  ```2016/05/WA123245.JPG```
During this process, it looks for duplicate files and will produce a report of all duplicates.  It initially looks at the file name to determine if there are other files with the same name.  There is a relatively high chance that file names are duplicated (particularly for older photo files) and so IME does a check to see if the camera make, camera model and date and time of the picture are the same.  
If they are duplicates, then the files are flagged as a duplicate and will not be copied across to the new file directory (although they will be updated e.g. geocoded).
If they are different files with the same file name, then they are renamed by adding a sequentrial number to the end of the file name e.g. 
File``` WA1234.JPG``` becomes ```WA1234_001.JPG```.
