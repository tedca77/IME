# Image Metadata Enhancer
IME (Image Metadata Enhancer) improves the metadata information on images and helps you discover and organise photos on your drives.  

It is particularly useful for image collections with **latitude and longitude** geographic information (e.g. usually taken on mobile phones, but also on many digital cameras) by converting latitude and longitude to recognisable place addresses. (This is known as “reverse geocoding”.) IME updates address information in the IPTC metadata and Windows Tags so the information is visible in tools such as Adobe Lightroom and Adobe Bridge which support the IPTC metadata standard. The addresses can also be used with Windows file search.

IME works through all images on one or more drives or directories and reads the latitude and longitude and then uses the **Open Street Map** web lookup service to populate the IPTC section with the full location.  (You need to be connected to the Internet for this to work.) Where the dates or location are missing or incorrect, there are a number of options for manually adding dates or providing locations via postcode, latitute and longitude, already identified places or through Events. 

IME will discover images on your storage drives, and identify duplicate images in different folders. IME has been designed so that it can be run repeatedly on a collection of images – it will not attempt to redo geocoding if it has already been processed. It can also copy files to a new, organised structure based on Year and Month, leaving behind any duplicate files.

Image Metadata Enhancer has extensive reporting and output of metadata information, indcluding a set of HTML page, which can be viewed, with a browser – this includes thumbnails of each image;

IME is simple to run, from a Command Prompt on Windows or other systems using a "jar" file. Once you have familiarised yourself with IME, parameters can be adjusted by providing a JSON input file.  You will need to understand the basics of JSON to make use of some of the advanced features (examples are provided below).  IME always outputs a correctly formatted JSON file which can be used as input for future runs. 

IME is copyright but available under an open source licence - source code is available on this Github repository. The software is currently only tested on Windows. 
# Why is this tool needed?
1.	Most personal image libraries are disorganised  – this tool helps discover duplicates and organises files by the date the photo was taken.
2.	Many "Cloud-based" image libraries provide reverse geocoding when a file is uploaded.  However, you may not want to put all photos in a single Cloud environment, due to the ongoing cost of storage.  Many Cloud systems do not update the metadata within photos, so any enhanced metadata created by the Cloud system is of no use if the photos are transferred to other storage environments.  This tool is designed for people with collections on local disk drives or SANs who require similar geocoding facilities to "Cloud systems" but without additional cost.
3.	It is difficult to search or review geographical and other metadata across a collection of images – in Windows you have to select properties on each image in turn.  IME helps you view the geographical information and remind you of trips and visits, and helps to identify where photos have been taken.
4.	Windows thumbnail generation can be slow for large collections – IME generates thumbnails which are faster to view in the HTML reports.
5.	Adobe Lightroom “Classic” is a great tool – but reverse Geocoding is only possible by purchasing one of the Lightroom Cloud-based packages. Adobe Bridge does not have geocoding capabilities. There is a plug-in for Lightroom Classic which does something similar, but this tool is independent of Lightroom or other tools.
6.	IME also generates Windows "Tag" information allowing Windows searching of location metadata withou any extra software.
# About Image Metadata
Image metadata in stored within each JPEG image file (JPEG files are produced by most mobile phones). The metadata is organised in a set of categories.  These categories include:
* **EXIF** – this contains the longitude and latitude information, as well as information on the camera / phone,  date of capture etc.
* **IPTC** – this contains the additional geographical information e.g. City, State /Province, Country etc.  Windows Explorer and Windows Properties do not show most of the IPTC data – the easiest way to see this is using the freeware IrfanView ( an excellent image viewer and editor which is useful for sizing and cropping images).  ExifTool is another excellent program that can be used to view all metadata.  You can also use Adobe Lightroom (which I also use) and other professional software most of which is quite expensive. Adobe Bridge is free software which provides equivalent features to Lightroom for editing IPTC metadata.
* **JPEG Comments** – this section allows multiple comments to be stored. (Adobe LightRoom Classic and Windows do not show these comments).
* **Windows metadata** (e.g. Title, Comments, Subject, Tags) – Windows shows some of the EXIF and IPTC data as Windows "Properties" – the Windows metadata is available by right clicking on a a file and selecting “Properties” in Windows. However, Windows will pick from both JPEG andf IPTC sections to populate some of these fields, so it is not always that clear which item of metadata is referred to.  At the end of this documentation, there is a table with the key metadata items relevant to image collections. 

# Getting Started
**BEFORE RUNNING ENSURE YOU HAVE A VALID BACKUP OF YOUR PHOTO FILES.  IF YOU DON'T HAVE A BACKUP - THEN CREATE ONE BEFORE YOU START !**
Image Metadata Enhancer is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY, under the GNU General Public Licence.  While it has been tested extensively, the range of formats possible are large and the program may not operate correctly with all files, so if you have a backup, you should not lose any photos. 

1. Download the <a href="https://github.com/tedca77/IME/blob/main/out/artifacts/ImageMetadataEnhancer_jar/ImageMetadataEnhancer.jar">ImageMetadataEnhancer.jar</a> to your drive e.g. D:/IME/ImageMetadataEnhancer.jar 
2. Ensure that version 17 or 18 of Java is installed.
3. Run as a Windows command  
```java -jar ImageMetadataEnhancer.jar  <directory to search> <output directory> <new directory structure> <parameters>```  

If there are spaces in the directory names, you should enclose in double quotes.

The following parameters are required:
**First parameter** (directory to search) is the top level directory where the program will start to find photos. IME will then search all subdirectories, looking for images.
**Second parameter** (output directory) is the location of the output.  This should be in a different location than the photos. The output directory should then contain:  

* A number of HTML files (Places.html, Files.html, Cameras.html, Duplicates.html etc.). These can be opened with a browser.
* A thumbnail for each photo
* Two KML files (point.kml and track.kml)
* A JSON output file called configYYYYMMDDhhMMss.json - where the date and time are added.   

As an example:
```java -jar ImageMetadataEnhancer.jar “d:/Photos” “d:/Results”```   
* The directory with existing photos (to search) is: ```d:/Photos```
* The results will be sent to : ```d:/Results```     

** Please note that for consistency, all file directories are recommended to be should be specified with a forward slash "/".**
It is recommended to follow this process:
1. Run in "read mode" - as above ```java -jar ImageMetadataEnhancer.jar “d:/Photos” “d:/Results”```
2. Look in one of the output files (duplicates.html) and check for duplicates, and delete from file system
3. Rename configYYYYMMDDhhMMss.json to config.json and provide this as an input file (this saves having to look up Open Street Map places again) - ```java -jar ImageMetadataEnhancer.jar “d:/config.json”```  (If the JSON file is provided as an input, any other parameters are not necessary.)
4. Check that all duplicates are removed by carrying out a second run
5. Run with an update parameter  ```java -jar ImageMetadataEnhancer.jar “d:/config.json” update```. If you don't have Lightroom or Bridge, then add the geocoding information to Windows Tags  ```java -jar ImageMetadataEnhancer.jar “d:/config.json” update addxpkeywords```. Lightroom allows searching of the IPTC data but Adobe Bridge does not - if using Adobe Bridge add keywords to the IPTC Keywords field by using the following parameter ```java -jar ImageMetadataEnhancer.jar “d:/config.json” update addiptckeywords```
6. You should now be able to search for photos using Country, State/Province, City and Sub Location:
In Adobe Lightroom - you can use the four IPTC fields to search
In AdobeBridge, search using the Keywords field in Advanced Search
in Windows, search using the File Explorer (Search).

# Additional Parameters
**Third parameter** (new directory structure) – is the new directory which files are to be copied to.  The new directory structure will be /Year/Month/Photo1 etc.  This should be a different area than the directory to search and the output directory.  Files will be be copied to the new directory structure using the "Original Date" to put in the correct place.

As an example:
```java -jar ImageMetadataEnhancer.jar “d:/Photos” “d:/Results” “d:/newDir”```   
* The directory with existing photos (to search) is: ```d:/Photos```
* The results will be sent to : ```d:/Results```
* Files will be copied to a new structure at : ```d:/newDir``` i.e. ```d:/newDir/2021/08/photo1.jpg```

In addition, further "run time" parameters can be added to the command. These are:
* **update** – this will update files with new metadata and move files. If this parameter is not provided, then update and move will not take place. 
* **overwrite** – this will overwrite existing values in fields.  For instance, if there are already values in the IPTC location metadata fields, then they will be overwritten with new values.  If this is not set, then no overwriting will take place.
* **showmetadata** – this will show all metadata in the output before and after updates.  This option is useful when debugging issues with files.
* **redo** – once a file has been processed, a flag will be placed on the file and if subsequent runs are made, the file will not not be geocoded again. If this parameter is added, it will force reprocessing. 
* **redoevents** – once a file has been processed, and matched against an event, the file will not be rematched against the same event.  If event details have been changed, this will force reprocessing of event information.  
* **addxpkeywords** – this copies location address information to XP Keywords (or Windows Tags) in addition to the iPTC fields.  This is particularly useful if you do not have a tool such as Adobe Lightroom, and simply want use use Windows search features. 
* **addiptckeywords** – this copies location address information to IPTC Keywords in addition to the iPTC fields.  This is useful if you are using Adobe Bridge keyword searching. 
* **clear** - this will remove any existing comments in the JPEG Comments section metadata, which have been added by IME. This will force reprocessing next time IME is run. (If you want to remove from the Windows Comments or IPTC Comments, you can edit the metadata for multiple files using your regular editing tools.) 
* **savefilemetadata** - this will output the file information to the JSON. This is useful if processing your files in a number of batches (and using the append option.
* **append** - this will merge file information with file information in the JSON file, to look for duplicate files across a number of batches.

# Outputs
## HTML Outputs ##
IME produces a number of HTML files that can be viewed with a browser.  Note that if there are more than 2,000 items, then the files are split up into multiple parts and are named places1.html, places2.html etc, as your browser may not be able to open a file with too many thumbnails.  
* cameras.html - lists all cameras and the number of photos taken with each camera (or phone)
* photosbydate.html - lists all photos in date order with a summary of metadata
* tracks.html - links all photos as daily tracks
* places.html - reports all geocoded photos by Place
* errors.html - lists all errors and warnings (e.g. for duplicate files)
* events.html - lists all photos that have been matched with Events
* duplicates.html - lists all duplicate photos.

**Example of HTML Output - showing all Photos in Date Order:**
![Photos By Date](/Images/photosbydate.jpg)
**Example of HTML Output - showing all Photos for each Place:**
![Places Report](/Images/places.jpg)
**Example of HTML Output - showing all Photos as Daily Tracks:**
![Daily Tracks Report](/Images/tracks.jpg)
## JSON Output ##
Information for every file processed is written out to a JSON file.  The JSON filename includes the date and time e.g. **config20220401120138.json**.  This can be used as input to other runs and provides a way of adding:   
* Events
* Places
* Camera names
* Providing other parameters that impact the way the program runs.

The structure of this file is explained later in this document.

## KML Output ##
Two KML files are produced:
* Points.kml - provides a point for every place found 
* Track.kml - provides daily tracks.
These are split into multiple parts, as Google has limits on the number of points in a kml file.  These can be be dragged onto a Google map and viewed.  Unfortunately, Google Map does not allow images to be added via a KML file (although KML supports embedded images.) Other mapping providers may support, however.  
**Example of KML output layered on a Google Map:**

![KML Files layered on a Google Map](/Images/pointstracks.jpg)
# Places
Each longitude and latitude value found is represented as a **“Place”** in IME.  If two images are taken at virtually the same place (i.e. they have very similar latitude and longitude), then this is identified as the same “Place” in IME and IME does not have to use the Open Street Map Service to carry out a second geocoding. (This is a good thing, because Open Street Map is a limited service, and condition of use is that it is not swamped with requests.)  The user can specify the distance that determines if the Place is the same (in metres).  Also, the geocoding may not identify the exact address e.g. house numbers may be slightly out.  IME can be given a set of known Places in the JSON, before it runs, where the correct house address can be provided. e.g. if 86 Acacia Avenue is found, but the actual address is “85 Acacia Avenue”, then this can be modified in the JSON file and also given a user friendly name e.g. “Our first house”. (Each Place added is given a unique number which can be used to allocate images to this Place if they do not have longitude and latitude – see next section.)   

The IPTC metadata specification has 5 fields for location - the OpenStreet Map address is applied as follows:
* **Sublocation** - Amenity, leisure, house-number,road,hamlet,suburb,city_district
* **City** - town, city, village
* **State/Province** - county,state_district
* **Country** - country
* **ISO Country Code** - country_code. 

# Adding geolocation information to files without Latitude and Longitude
For images which do not have latitude and longitude information (e.g. scanned images and images from cameras without geolocation), geographical details can be added by adding an instruction to the metadata, in one of three ways, before processing:
* Through adding the latitude and longitude – enter ```#latlon:50.2,-0.43218 (no spaces)```
* Through adding a national Post Code – enter ```#postcode:SW1A1AA (no spaces)```
* Through adding a Place Identifier – enter ```#place:1```.   

If you only use Windows for editing and managing your collection, put the above value somewhere in the Windows Comments Field (with a space after the values) – if you are using Adobe Lightroom or Adobe Bridge (or similar tool), you can put the value in the Instructions (IPTC) field. Note that Windows and tools such as Lightroom allow you to modify metadata on multiple fields at the same time so if you have a group of images that are all taken at the same place you can add these instructions in one go.   
Note that IME will never overwrite longitude and latitude information if it is already in the metadata. If you want to replace the longitude and latitude, you should clear out the old values first.
# Adding Dates
For scanned images, the dates in the metadata are unlikely to be correct. An instruction can be added to the Windows Comment or Instructions (IPTC) fields to provide a correct date within the metadata. This is of the form:
```#date:YYYY``` or  ```#date:YYYY-MM```  or ```#date:YYYY-MM-DD```
This updates the **EXIF Original Date** field which appears as **"Date Taken"** in Windows properties.     
If the camera date is out by a Year you can use the format ```#date:+1Y``` or ```#date:-1Y``` to change the date.
If a file has already been processed by IME, and then new information is added, then IME will re-process the file - this could including finding an Event or adding geolocation information. Adding a new date will overwrite the existing date, even if a previous date has been provided. 
# Adding IPTC Metadata
It is also possible to add some IPTC metadata to every image - this is specified at the Drive level (IME can operate across multiple drives or directories). The fields that can currently be updated are as follows:   
* **IPTC Category**: this will replace the existing category 
* **IPTC Keywords**: these should be entered separated by semi-colon i.e. ```"keyword1;keyword2"```.  These are added to existing keywords.

# Finding Events
Events can be used to add information such as a Title, Description or Keywords to an image, based on the day or date.  For instance,
*	Every image taken on Christmas Day could be given a title of ‘Christmas’;
*	Every image taken on a birthday could be given a title of the person's birthday
*	Every image taken on a holiday between two dates can be marked with a location.

For scanned images, you should use the #date: instruction to provide a correct date – the date is always processed before events (as described above).
Events are defined in the JSON file – if the date provided in the JSON file matches the creation date (**EXIFOriginalDate**), then the event information will be copied across to the image – this can also include geographical information on the event, if it exists.  
Normally the date of the image is sufficient to find the correct event.  However, if you want to hard code the event. i.e. not based on a date, you can enter an instruction for the event  – enter ```#event:123```.
An event can have the following information in the JSON:
*	**title**: This populates the Title / ImageDescription field; 
*	**description**: This populates the Subject / Caption field;
*	**keywords**: (separated by semi-colons) This populates the IPTC keywords section;
*	**eventcalendar**: This is used to indicate that the same date each year will be found e.g. for a birthday (MM-DD or YYYY-MM-DD – if the year is provided, then IME will only find dates equal or after the year provided);
*	**eventdate**: (YYYY-MM-DD) – the date (or start date) of a single event;
*	**enddate**: (YYYY-MM-'DD) - Optional  - the end date of a single event;
*	**eventtime**: (HH.mm) – Optional – the start time of a single event;
*	**endtime**: (HH.mm) – Optional – the end time of a single event;
*	**location** :  a location can be provided it lat, lon is not present on the files - the format is either #latlon:,#postcode: or #place:, followed by appropriate values (as described in the "adding geolocation" section).

In order of priority, IME will process files as follows:
* Dates are set irrespective of other instructions. If a new date is found, this can determine if new Events are applicable;
* If longitude and latitude metadata is present in a file, it will be used to geocode, if it has not already been geocoded;
* If there is no latitude and longitude present, then any Place, Postcode or Latlon instructions are used;
* If an Event is found, then metadata can be updated, although IME will not overwrite any existing latitude and longitude information.  If multiple events are matched (e.g. a holiday and a birthday during the holiday), the title, description and keywords are concatenated together in the metadata.

# Running Multiple Times
IME has been designed so that it can run mutiple times across your image library, without having to rerun all the processing again. It does this by adding comments into each image which provides an audit trail of changes, and information on Places and Events that have been found.  
On completion of processing, IME will add a value to the following fields:
1.	**JPEG Comments section** – this is modified with a new comment for each modification;
2.	**Instructions (IPTC)** metadata is updated with a new comment or an updated comment;
3.	**Comments (Windows)** metadata is updated with a new comment or an updated comment.
In each case, the command which had been entered will be updated with “DONE” in the three fields described above.) For example:
```
#processedDONE:
#geocodeDONE:50.43,-2.444
#latlonDONE:50.43,-2.444
#placeDONE: 34
#postcodeDONE:SW1A1AA
```
When IME is first run in update mode, #processedDONE: is added to the three comments sections.  If it is run again, and finds the processedDONE: text, it will not update metadata and will not recreate the thumbnail file, if it already exists.  However, it will:  
* check for duplicates and identify them;
* check if new date information has been provided, and use this to find new Events; 
* check if new location information has been provided, and if so, geocode the file; 
* check if new events have been found and update the file;
* reverse geocode any longitude and latitude information, in order to generate a list of Places, and a summary of images for each Place, but it will not update the file. To avoid the extra processing, if you can copy the Places found fromm a JSON output file, to a JSON input file, it will be more efficient.   

# Moving Files to a New Directory
One of the features of IME is to move all images to a new directory structure.  This is of the structure:
```YEAR / MONTH / FILENAME``` ie.  ```2016/05/WA123245.JPG```.    

When moving files, the old directory location is written to the comments metadata. The old directory structure is also added to the Windows keywords metadata, as this could include information on the origin of the files that have been moved (such as Date). 
 
If files have true duplicates, then the second and subsequent files will not be copied across to the new file directory (although they will be updated e.g. geocoded).
	
However, if there are two files with the same name, which were taken in the same month, but they are different files (ie.. different creation date, size, camera), then they are copied across to the new Directory, but they are renamed by adding a sequential number to the end of the file name when they are copied. e.g. 
File``` WA1234.JPG``` becomes ```WA1234_001.JPG```.

# Finding Duplicates
When the program runs it will look for duplicate files.
In IME, a duplicate file is where two files have:
* the same name
* the same creation date
* the same camera maker and model.
	
Most new cameras produce a filename that is likely to be unique as it includes the time of capture down to a thousandth of a second in the file name. However, earlier cameras and phones (and scanners) had much shorter file names and the chance of duplicate file names in your archives is high.
	
IME produces a report of any possible duplicates and when in "update mode" puts a comment in the metadata (for any duplicates) to indicate it has been identified as a duplicate. It is recommended to run IME in read-only mode first, to determine any images that have been duplicated, and so that dupicates can be removed before processing.
	
IME will only look for duplicates in the directory structure being processed. If you only process part of your collection, then it will not pick up duplicates elsewhere.  You can get round this in two ways a) by processing all files in one run or b)by providing the JSON output file from other runs as an input file for the program - this will then match against all images in your collection.  For instance, if your collection is split into **Photos before 2000** and **Photos after 2000** then run IME against the "Photos before 2000" directory, rename the JSON output file and provide as an input file when running against "Photos after 2000".  You will then get a report of images for your complete collection and any duplicates will be identified across the complete collection.  
	
# Use of Open Street Map
IME uses Open Street Map APIs to carry out reverse geocoding and post code look up.  This requires an internet connection. For **post code** lookup, you will also require an Open Street Map API key. This is entered in the JSON file - further information available below.  IME minimises calls to Open Street Map by saving Places and checking Places before making a call to OpenStreet Map. (This also speeds up operation.)   For each new longitude and latidtude pair, IME calculates how close it is to each of the saved places and if within a specified distance, it will use the saved Place. So if you have taken 10 photos in the same location, only one call to Open Street Map is required.   A sensible distance is 75 metres, although this can be modified in the JSON input file. Places are also written out to the JSON file and can be used as input to other runs, removing the need for an Open Street Map look up when an image is next found close to other images.  It is recommended that Places information in the JSON output file is always copied to the next JSON input file, to save making calls to Open Street Map. 
	
# Viewing information in other tools
* **Adobe Lightroom** - you may have to update the metadata in Lightroom.  Select the Images or directories and right click on one image and select "Read Metadata from Selected Files". Lightroom provides sophisticated searching across a metadata.
* **Adobe Bridge** - this is free software from Adobe which has good metadata editing capability. However, the latest downloadable version can only search filename and keywords, due to bugs. 
* **IrfanView** - open a file and click on the Image / Information menu option.  You will see buttons for IPTC Info, EXIF Info and Comments.
* **Windows** - right click on any image and select properties. This will allow viewing of most metadata. Most importantly, you can search using the Windows search bar - searching Tags is an efficient way to find information.
* **ExifTool** - this provides a very good report on all metadata on a file.
# Limitations
* IME is currently only tested on Windows, although it is believed that it will work unchanged on Linux, Apple and other systems that support Java. 
* IME is currently only tested with JPEG / JPG files, although should also support other formats.
* There is a bug in Apache Imaging library which means that if the Windows Title field already has a value, it cannot be overwritten. This only impacts the use of Events. 
* IME has only been tested with UK national postcodes.
* IME should support International character sets (i.e. UTF-8), but has currently only been tested on a small subset of countries.
	
# JSON File input
The best approach for using IME is to run it once without a JSON input file and then modify the JSON output file to form the input for the next run. 
A JSON file can provide input for:
* Specifying multiple drives to search and excluding specified directories or files
* Specifying Events, Places and Cameras
* Modifying other parameters of the program
* Merging results with other runs from IME (across different root directories)

To run with a JSON file, the first parameter should be a JSON filename. e.g. ```ImageMetadata.exe "d:/photos/config.json"```. All "run time" parameters (as described above) can be specified in the JSON file, but they can be overridden by adding to the command line.

The following sections outline the various sections of the JSON file - ytou can gain an understanding of the JSON file by looking at output from a run of IME.
	
## Top Level Parameters
	 
* **update**: - see Additional Parameters section.
* **showmetadata**: -  see Additional Parameters section.
* **append**: - see Additional Parameters section.
* **redo**: - see Additional Parameters section.
* **redoevents**: - see Additional Parameters section.
* **savefilemetadata**: - see Additional Parameters section.
* **overwrite**: - see Additional Parameters section.
* **addxpkeywords**: - see Additional Parameters section.
* **addiptckeywords**: - see Additional Parameters section.
* **clear**: - see Additional Parameters section.
* **tempdir**: - directory to hold results, including thumbnails 
* **newdir**: - directory to move files to 
* **htmllimit**: maximum number of photos referenced in a single html file (if more than this, the file is split up - default is 2,000). 
* **kmllimit**: maximum number of Places referenced in a single kml file (if more than this, the file is split up - default is 200).
* **minfilesize**: minimum size of the file to process  - in bytes - default is 4000 bytes
* **thumbsize**: size of the thumbnail - recommended is 360x270 
* **cachedistance**: distance in metres between points that determine if photos are the same place (default 75 metres)
* **pauseseconds**: pause time before making a call to OpenStreetMaps (default is 2 seconds, minimum is 1 second)
* **timeZone**: specified time zone of all photos (not currently used) (default is Europe/London)
* **isocountrycode**: specifies OpenStreetMap fields to use for mapping to IPTC Country Code (default is "country_code") 
* **country**: specifies OpenStreetMap fields to use for mapping to IPTC Country (default is "country") 
* **stateprovince**: specifies OpenStreetMap fields to use for mapping to IPTC State / Province (default is "county","state_district") 
* **city**: specifies OpenStreetMap fields to use for mapping to IPTC City (default is "town","city","village") 
* **sublocation**: specifies OpenStreetMap fields to use for mapping to IPTC Sub Location  (default is "leisure","amenity","house_number","road","hamlet","suburb","city_district") 
* **imageextensions**: a list of file extensions to process separated by "~" - for JPG should be ```"jpg~jpeg"```. IME has not been tested with other file formats.
* **openapikey**: An Open API key is required if carrying out Post Code look ups.


## Drives
This section specifies one or more drives. (Repeat the section within the curly brackets and add a comma between).  Use a single forward slash ```/```
* **startdir**: The start location to search - all subdirectories will be searched from this point
* **iptccategory**: value of IPTC Category to update - all photos will be updated with this valuye
* **iptckeywords**: values of IPTC Keywords to update - all photos will be updated with these values
* **excludspec**: provides one or more directories to exclude and one or more fileprefixes to avoid.
* **directories**: a list of directory names to ignore
* **name**: the names of directories to ignore
* **fileprefixes**: a list of the first few characters of file names to ignore i.e. if the file name starts with this string
* **name**: one or more file names to igonre
```
	"drives": [
		{
			"startdir": "H:/PhotoDrive/Our Photos 2015-2021",
			"excludespec": {
				"directories": [
					{
						"name": "/WhatsApp"
					}
				],
				"fileprefixes": [
					{
						"name": "$RECYCLE."
					}
				]
			},
			"iptccategory": "Our photos",
			"iptckeywords": "Photos"
			
		}
	],
```
	
	
## Events
This section provides a number of events

* **eventid**: provides an id for the event
* **description**: provides a description or subject which is written to all photos that match this event
* **keywords** : (separated by semi-colons): 
* **eventcalendar**: This is used to indicate that the same date each year will be found e.g. for a birthday (MM-DD or YYYY-MM-DD – if the year is provided, then IME will only find dates equal or after the year provided)
* **eventdate**: (YYYY-MM-DD) – the date of a single event
* **enddate**: (YYYY-MM-DD) - Optional  - the end date of a single event
* **eventtime**: (HH.mm) – Optional – the start time of a single event
* **endtime**: (HH.mm) – Optional – the end time of a single event
* **location**:  either #latlon,~postcode or #place:
```
                        {
				"eventid":10,
				"title":"Fred and Ginger's Wedding",
				"description":"Fred and Ginger's Wedding in LondonPutney",
				"eventdate":"2021-07-21",
				"location":"#postcode:SW151LB",
				"keywords": "Wedding;Fred;Ginger"
			},
```

## Cameras
This section allows you to provide user friendly names for each camera or phone. IME will try and match the camera maker, and camera model to determine the camera name. This information is only used for the output from IME, to help you identify different cameras.

* **friendlyname**: Name you can give to a camera based on the maker and model and program name
* **cameramaker**: Camera Maker
* **cameramodel**: Camera Model
* **programname**: Camera software program name
```
	"cameras": [
		{
			"friendlyname": "John's Xiaomi Phone",
			"cameramaker": "Xiaomi",
			"cameramodel": "Mi A1",
			"programname": null,
		},
		{
			"friendlyname": "Jane's Huawei Phone",
			"cameramaker": "HUAWEI",
			"cameramodel": "PRA-LX1",
			"programname":android,
		}
	],
```
## Places

This section is quite complicated, so it is recommended to simply copy from the output from an IME run and modify as appropriate - for instance adding a "friendlyname", or correcting a street address.

* **display_name**: this is shown in the HTML output but does not update the files
* **lat**: latitude value
* **lon**: longitude value
* **address**: a section that includes the various parts of the address
* **friendlyname**: a name the user can give to Places 
* **placeid**: a unique ID for Places.  This is generated by IME.  If running across your archive in sections, you have to ensure that this ID is unique.
* **iptcstateProvince**: value for IPTC State/Province 
* **iptccountry**:value for IPTC Countrye
* **iptcsublocation**: value for IPTC Sub Location
* **iptccity**: value for IPTC City
* **iptccountryCode**: value for IPTC State/Province 
```
{
			"display_name": "Tower of London, Tower Hill, Tower Liberty, Whitechapel, London Borough of Tower Hamlets, London, Greater London, England, EC3N 4AB, United Kingdom",
			"lat": "51.509191",
			"lon": "-0.076062",
			"address": {
				"amenity": null,
				"suburb": "Whitechapel",
				"city": "London",
				"county": null,
				"state_district": "Greater London",
				"state": "England",
				"postcode": "EC3N 4AB",
				"country": "United Kingdom",
				"country_code": "gb",
				"road": "Tower Hill",
				"village": null,
				"hamlet": null,
				"house_number": null,
				"town": null,
				"city_district": null,
				"leisure": null
			},
			"friendlyname": null,
			"placeid": 161,
			"iptcstateProvince": "Greater London",
			"iptccountry": "United Kingdom",
			"iptcsublocation": "Tower Hill, Whitechapel",
			"iptccity": "London",
			"iptccountryCode": "GB"
		},

```
## File

Details of each photo is written out to the JSON file.  This information does not need to be edited, but can be used in subsequent runs of IME to check for duplicates.  

```
{
			"fileName": "IMG_20220123_130520465.jpg",
			"fileKey": 29,
			"fileSize": 7241564,
			"bestDate": "2022-01-23 13:05:20",
			"exifOriginal": "2022-01-23 13:05:20",
			"exifDigitised": "2022-01-23 13:05:20",
			"tiffDate": "2022-01-23 13:05:20",
			"fileCreated": "2022-04-27 15:36:08",
			"fileModified": "2022-01-23 13:05:22",
			"fileAccessed": "2022-04-27 15:36:08",
			"directory": "H:/PhotoDrive2/Our Photos 2022/phone 26 Jan 2022/",
			"width": 4608,
			"height": 3456,
			"cameraMaker": "motorola",
			"cameraModel": "moto g(8) power",
			"cameraName": null,
			"cameraKey": 6,
			"placeKey": 77,
			"eventKeys": "",
			"programName": "sofiar_reteu-user 11 RPES31.Q4U-47-35-11 5f1af release-keys",
			"latitude": 51.617708,
			"longitude": -1.0213589722222223,
			"altitude": 230.502,
			"city": "South Oxfordshire",
			"country_name": "United Kingdom",
			"country_code": "GB",
			"subLocation": "Swyncombe",
			"stateProvince": "Oxfordshire, South East England",
			"thumbnail": "H__PhotoDrive2_Our Photos 2022_phone 26 Jan 2022_IMG_20220123_130520465.jpg",
			"displayName": "Swyncombe Downs, Swyncombe, South Oxfordshire, Oxfordshire, South East England, England, RG9 6EB, United Kingdom",
			"orientation": 1,
			"windowsComments": "#geocodeDONE:51.617708,-1.0213589722222223:77#processedDONE:2022:04:28 14:18:39",
			"windowsTitle": "",
			"windowsSubject": "",
			"windowsKeywords": "GB;United Kingdom;Oxfordshire;South Oxfordshire;RG9 6EB;England;South East England;Swyncombe",
			"comments": null,
			"duplicate": false,
			"iptcobjectName": null,
			"iptccaptionAbstract": "",
			"iptcinstructions": "#geocodeDONE:51.617708,-1.0213589722222223:77#processedDONE:2022:04:28 14:18:39",
			"iptcdateCreated": "20220123",
			"iptckeywords": "Photos;Home",
			"iptccategory": "Our photos",
			"fstop": 1.7
		},
```

# Summary of Metadata Fields 	
	
|Field|Metadata Section|Windows Properties|Lightroom|IrfanView|
|-----|----------|---------------------|-----------------|------------------|
|Title|EXIF|Not visible|Title|Information/EXIF/XPTitle (and ImageDescription)|
|Object Name|IPTC|Not visible|Title|Information/IPTC/Document Title|
|Keywords|EXIF|Shown in Tags|In Keyword List|Information/EXIF/XPKeywords|
|IPTC Keywords|IPTC|Shown in Tags|In Keyword List|Information/IPTC/Keywords|
|Subject (Caption)|EXIF|Subject|Caption|Information/EXIF/XPSubject & IPTC/Caption|
|Original Date Time|EXIF|Date Taken|EXIF - Date Time Original|Information/EXIF/DateTimeOriginal |
|IPTC Date Created|IPTC|Not visible|Not used if other dates are visible|Information/IPTC/Credits-Origin/Date |
|Instructions|IPTC|Not visible|IPTC/Workflow/Instructions|Information/IPTC/Description/Special Instructions|
|Comments (Windows)|EXIF|Details/Description/Comments|Not visible|Information/EXIF/XP Comment|
|Category|IPTC|Not visible|IPTC Category|Information/IPTC/Keywords/Category|
|ISO Country Code|IPTC|Not visible|IPTC – ISO Country Code|Not visible|
|Country|IPTC|Not visible|IPTC – Country|Information/Credits Origin/Credits Origin|
|Stateprovince|IPTC|Not visible|IPTC – State/Province|Information/IPTC/Credits Origin|
|city|IPTC|Not visible|IPTC – City|Information/IPTC/Credits Origin|
|sublocation|IPTC|Not visible|IPTC - Sublocation|Information/IPTC/Credits Origin|
|JPG Comments|Comments|Not visible|Not visible in Lightroom Classic|Information/Comments|
|Latitude|EXIF|GPS - shown as Degrees, Minutes, Seconds|Shown as Degrees, Minutes, Seconds|Information/EXIF/GPSLatitude - shows both formats|
|Longitude|EXIF|GPS - shown as Degrees, Minutes, Seconds|Shown as Degrees, Minutes, Seconds|Information/EXIF/GPSLongitude - shows both formats|

##Notes on Processing

* **Title** : Can be set in event processing. Due to an issue with Apache Imaging, this field is not written if there is an existing value present.  If this value is set, then the IPTC Object Name is also set to the same value.
* **Keywords / Windows Tags** : Windows combines together the EXIF and IPTC keywords for display of properties and searching.  Note, when entering values in Windows, values should be separated by semi colons “;”.   In Lightroom, Keywording, and unique values will appear in the KeyWord List. When entering, values in Lightroom  should be separated by commas “,". In irfanView, enter each IPTC keyword on a separate line. The same Keywords can be added for all files by providing in the JSON - these are added to IPTC fields only. When moving files to a new directory, the current directory structure is converted to IPTC keywords.
* **Subject** : If no description is provided, Windows will show the Subject to be the same as the Title field - can be set in Event processing. 
* **JPG Comments** Each time a file is updated, a comment is added by IME. Metadata allows multiple comments to be added to a file. These are displayed separately in IrfanView. e.g. when a file is moved or geocoded.
* **Comments (Windows)** : If this is entered in Lightroom, data is written back to Windows. It does not work the other way round – changes to Windows properties are not received by Lightroom.
* **Instructions** : This field can be used to provide instructions to the program for each image. It is updated after processing along with the Comments (Windows) field. and JPG Comments.
* **IPTC Date Created** : This is written as YYYYMMDD, and is the same value as Original Date Time. This is a text value, allowing partial dates to be entered e.g. YYYY.

# References
1. Adobe Lightroom [https://www.adobe.com/uk/products/photoshop-lightroom.html]
2. Adobe Bridge [https://www.adobe.com/uk/products/bridge.html]
3. IrfanView [https://www.irfanview.com]
4. ExifTool [https://exiftool.org]
5. IPTC Specification [https://iptc.org/std/photometadata/specification/IPTC-PhotoMetadata]
6. IPTC Message Group [https://groups.io/g/iptc-photometadata/messages]




	
	
