# Image Metadata Enhancer
This program enhances the metadata information on images and helps you discover and organise photos on your drives.  

It is particularly useful for photo collections with **latitude and longitude** geographic information (e.g. usually taken on mobile phones, but also on many digital cameras) where you want to convert latitude and longitude to recognisable place addresses.  (This is known as “reverse geocoding”.) It is designed to update address information in the IPTC metadata so it is visible in tools such as Adobe Lightroom and Adobe Bridge which support the IPTC metadata standard and which can search enhanced metadata.

IME works through all photos on one or more drives or directories and reads the geographical information (latitude and longitude) and then uses the **Open Street Map** web lookup service to populate the IPTC section with the full location.  (You need to be connected to the Internet for this to work.) Where the dates or location are missing or incorrect, there are a number of options for manually adding dates or providing locations via postcode, latitute and longitude, already identified places and through events. 

IME will discover photos on your storage drives, and identify duplicates building up over time in different folders. IME has been designed so that it can be run repeatedly on a collection of photos – it will not attempt to redo geocoding that has been successful. However, if additional information is provided on Dates, Places or Events, then IME will reprocess photos.  It can also copy files to a new, organised structure based on Year and Month.

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

IME is simple to run, from a Command Prompt on Windows. An "exe" version is available for Windows, and a jar file can be downloaded also.  Once you have got the hang of the program, parameters can be adjusted by providing a JSON input file.  You will need to understand the basics of JSON to make use of the advanced features (examples are provided below).  The program always outputs a correctly formatted JSON file which can be used as input for future runs. 

IME is copyright but open source - source code is available on this Github repository. The software is currently only tested on Windows. 
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
 IME will leave the standard Windows fields (Date Modified, Date Accessed and Date Created) unchanged after processing.
# Getting Started
**BEFORE RUNNING ENSURE YOU HAVE A VALID BACKUP OF YOUR PHOTO FILES.  IF YOU DON'T HAVE A BACKUP - THEN CREATE ONE BEFORE YOU START !**
1. Copy the ImageMetadataEnhancer.exe to your drive e.g. D:?IME/ImageMetadataEnhancer.exe 
2. Creata batch file 
3. 
4. Install Java
Copy this file to a directory
Run with two (or three) parameters
			Ime <directory to search> <output directory> <new directory structure>
ime “d:/Photos” “d:/Results” “d:/newDir”
•	The directory with existing photos is: d:/Photos
•	The results will be sent to : d: /Results
•	Files will be copied to: d:/newDir
First parameter is the top level directory where the program will start to find image files photos
Second parameter is the location of the output files.  This should be in a different location than the photos.  If there are spaces in the directory names, you should enclose in double quotes as shown. 
Third parameter – is the new directory which files are to be copied to.  The new directory structure will be /Year/Month/Photo1 etc.  
The output directory (d:/Results) should then contain:
•	A number of HTML files (Places.html, Files.html, Cameras.,html, Duplicates..html). These can be opened with a browser.
•	A thumbnail for each photo
•	A number of KML and KML files
•	Output is written to a file output.txt
•	A JSON file, config.json, which can be used to modify parameters when running the program.
If the third parameter is a directory, then files will be copied to the new directory structure using the photo date to put in the correct place.  
In addition, further parameters can be added to the command. These are:
* **update** – this will update files with new metadata and move files. If this parameter is not provided, then no updates will take place. 
* **overwrite** – this will overwrite existing values in fields.  For instance, if there is already values in the IPTC location metadata fields, then they will be overwritten with new values.  If this is not set, then no overwriting will take place.
* **showmetadata** – this will show all metadata in the console before and after updates.  This option is useful when debugging issues with files.
* **redometadata** – once a file has been processed, a flag will be placed on the file and if subsequent runs are made, the file will not have metadata updated. If this parameter is added, it will force metadata to be updated.  this will force redoing of geocoding using lat, lon information.

	Use cases:
	we want to redo everything....this is based on a paramter
	all files have been processed,  but then new Place information is added to files...this will overwrite existing values - we can determine th 
	all files have been processed but then new events are added to the files...we can determine if this is a new event.  If it is, then we can process and overwrite values....
	
The user selects the “Root” location to start looking for photos and it will then search all subdirectories, looking for image files;

# JSON Output
Information for every file processed is written out to a JSON file.  The filename includes the date and time.  This can be used as input to other runs and provides a way of adding:
	* Events
	* Places
	* Camera names
	* Providing other parameters that impact the way the program runs.
# Places
Each longitude and latitude value found is represented as a “Place” in IME.  If two photos are taken at virtually the same place (and have very similar latitude and longitude), then this is identified as the same “Place” in IME and IME does not have to use the Open Street Map Service. (This is a good thing, because Open Street Map is a limited service, and condition of use is that it is not swamped with requests.)  The user can specify the distance that determines if the Place is the same (in metres).  Also, the geocoding may not identify the exact address e.g. house numbers may be slightly out.  IME can also be given a set of known Places in the JSON, before it runs, where the correct house address can be provided. e.g. if 86 Acacia Avenue is found, but the actual address is “85 Acacia Avenue”, then this can be provided and also given a user friendly name e.g. “Our first house”. (Each Place added is given a unique number which can be used to allocate images to this place if they do not have longitude and latitude – see next section.) The IPTC metadata specification has 5 fields for location - the user can modify how IME fills each field from the Open Stret Map values.  The default is as follows:
* **Sublocation** - Amenity, leisure, house-number,road,hamlet,suburb,city_district
* **City** - town, city, village
* **State/Province** - county,state_district
* **Country** - country
* **ISO Country Code** - country_code 
# Adding geolocation information to files without Latitude and Longitude
For photos which do not have geographical information (e.g., scanned photos and photos from cameras without geolocation) , geographical details can be added by adding an instruction to the metadata,  in one of three ways before processing:
* Through adding the latitude and longitude – enter ```#latlon:lat,long (no spaces)```
* Through adding a national Post Code – enter ```#postcode: SW1A1AA (no spaces)```
* Through adding a Place Identifier – enter ```#place: 1```.
Note that IME will never overwrite longitude and latitude information if it is already in the metadata. If you want to replace the longitude and latitude, you should clear out the old values first. (CHECK!!!)
If you only use Windows for editing and managing your collection, put the above value somewhere in the Windows Comments Field (with a space after the values) – if you are using Adobe Lightroom or Adobe Bridge, put the value in the Instructions (IPTC) field.
Note that Windows and tools such as Lightroom allow you to modify metadata on multiple fields at the same time so if you have a group of photos that are all taken at the same place you can add in one go. 
# Adding Dates
For scanned photos, the dates in the metadata are unlikely to be correct. An instruction can be added to the Windows Comment or Instructions (IPTC) fields to provide a correct date within the metadata. This is of the form:
```#date:YYYY``` or  ```#date:YYYY-MM```  or ```#date:YYYY-MM-DD```
This updates the **EXIFOriginalDate** field and will then be used with Events (see the next section) to provide location information related to events.  
If a file has already been processed by IME, and then new date information is added, then IME will re-process the file - this could including finding an event and geocoding. Adding a new date will overwrite the existing date, even if a previous date has been provided. 
# Finding Events
Events can be used to add information such as a Title, Description or Keywords to a photo, based on the day or date.  For instance,
*	Every photo taken on Christmas Day could be given a title of ‘Christmas’;
*	Every photo taken on a birthday could be given a title of the persons birthday
*	Every photo taken on a holiday between two dates can be marked with a location.

For scanned photos, you should use the #date: instruction to provide a correct date – the date is always processed before events.
Events are defined in the JSON file – if the date provided in the JSON file matches the photo creation date (**EXIFOriginalDate**), then the event info will be copied across to the photo – this can also include geographical information on the event, if it exists.  
Normally the date of the photo is sufficient to find the correct event.  However, if you want to hard code the event. i.e. not based on a date, you can enter an instruction for the event  – enter #event:123.
An event can have:
*	title
*	description (Subject)
*	keywords (separated by semi-colons)
*	eventcalendar : This is used to indicate that the same date each year will be found e.g. for a birthday (MM-DD or YYYY-MM-DD – the year is ignored)
*	eventdate; (YYYY-MM-DD) – the date of a single event
*	enddate: (YYYY-MM-DD) - Optional  - the end date of a single event
*	eventtime (HH.mm) – Optional – the start time of a single event
*	endtime: (HH.mm) – Optional – the end time of a single event
*	location :  either #latlon,~postcode or #place:

In order of priority:
•	Dates are set irrespective of other instructions. If a new date is found, this can determine if new events are applicable.
•	If longitude and latitude metadata is present, it will be used to geocode, if it has not already been geocoded
•	If there is no latitude and longitude present then any place, postcode or latlon instructions are used
•	If an event is specified, then it will not overwrite any existing latitude and longitude information.  However, if there is no latitude and longitude information, it will be updated.
What if multipl events are found with conflicting location data? 
	
	
# Audit Trail of Changes
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
# Finding Duplicates
When the program runs it will look for duplicate files.
In IME, a duplicate file is where two files have:
* the same name
* the same creation date
* the same camera maker and model.
	
Most new cameras produce a filename that is likely to be unique as it includes the time of capture in the file name down to a thousand of a second. However, earlier cameras and phones (and scanners) had a much shorter file names and the chance of duplicate file names is quite high.
	
IME produces a report of any possible duplicates and when in "update mode" puts a comment in the metadata (for any duplicates) to indicate it has been identified as a duplicate. It is recommended to run IME in read-only mode first, to determine any directories that have been duplicated. 
	
IME will only look for duplicates in the directory structure being processed. If you only process part of your collection, then it will not pick up duplicates elsewhere.  You can get round this by providing the JSON output file from other runs as an input file for the program - this will then match against all photos in your collection.  For instance, if your collection is split into **Photos before 2000** and **Photos after 2000** then run IME against the "Photos before 2000" directory, rename the JSON output file and provide as an input file when running against "Photos after 2000".  You will then get a report of photos for your complete collection and any duplicates will be identified across the complete collection.  
	
# Moving Files to a New Directory
One of the things that IME can do is move all photos to a new directory structure.  This is of the structure 
```YEAR / MONTH / FILENAME``` ie.  ```2016/05/WA123245.JPG```
 
If files have true duplicates, then the second and subsequent files are flagged as duplicates in the Comment section and will not be copied across to the new file directory (although they will be updated e.g. geocoded).
	
However, if there are two files with the same name, which were taken in the same month, but they are different files (ie.. different creation date, size, camera), then they are copied across to the new Directory, but they are renamed by adding a sequential number to the end of the file name when they are copied. e.g. 
File``` WA1234.JPG``` becomes ```WA1234_001.JPG```.

# Use of Open Street Map
IME uses Open Street Map APIs to carry out reverse geocoding and post code look up.  This requires an internet connection. For post code lookupo, you require an Open Street Map API key. THis is entered in the JSON file - further information available here.  IME minimises calls to Open Street Map by caching Places and checking Places in the cache before making a call to OpenStreet Map. (This also speeds up operation.)   For each new longitude and latidtude pair, IME calculates how close it is to each of the cached places and if within a specified distance, it will use the cached value. SO if you have taken 10 photos in the same location, only one call to Open Street Maop is required.   A sensible distance is 75 metres, although this can be modified in the JSON input file. Cached Places are also written out to the JSON file and can be used as input to other runs, removing the need for an Open Street Map look up. 
	
# Viewing information in other tools
* Adobe Lightroom - you may have to update the metadata in Lightroom.  Select the Images or directories and right clck on one image and select "Read Metadata from Selected Files"
* Adobe Bridge - 
* IrfanView - open a file and click on the Image / Information menu option.  You will see buttons for IPTC Info, EXIF Info and Comments.
* Windows - rigfht click on any image and select properties. This will allow viewing of most metadata. 
# Limitations
* Only tested on Windows, although it is believed that it will work unchanged on Linux, Apple and other systems that support Java. 
* Only supports JPEG / JPG files, although with testing may also support other formats.
* There is a bug in Apache Imaging library which means that if the Windows Title field already has a value, it cannot be overwritten. This only impacts the use of Events. 
	
# JSON File input
The best way of running IME is to run it once without a JSON input file and then modify the output file to form the input for the next run.  To run with as JSON file, the first paramter should be a JSON file. e.g. ```ImageMetadata.exe "d:/photos/config.json```. Note you can also add overwrite, redo and parameters to override the values in the JSON file.
	
## Top Level Parameters
	 
* update: - if false, no updates will take place to records ...the default is false
* showmetadata: - metadata will be shown before (and also after, if update is true) .. default false
* overwriteValues: - if metadata already exists in a field, it will be replaced, unless it has already been geocoded by IME -  default false. (If the new field value is blank, then it will write a blank value)
* appendPhotos
* tempdir - 
* newdir - 
* minfilesize - 
* thumbsize 
* cachedistance 
* pauseseconds - 
* timeZone - 
* isocountrycode
* country
* stateprovince
* city
* sublocation
* newfilenames
*imageextensions
*openapikey
* height
* width

	
	

## Drives
This section specifies one or more drives. (Repeat the section within the curly brackets and add a comma between).
* startdir
* iptccategory
* iptckeywords
* iptccopyright
* excludspec provides one or more directories to exclude and one or more fileprefixes to avoid.
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
			"iptckeywords": "Photos",
			"iptccopyright": "John Smith"
		}
	],
```
	
	
## Events
This section provides a number of events
## Cameras
This section allows you to provide user friendly names for each camera or phone. IME will try and match the camera maker, and camera model to determine the camera name. 
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

# Summary of Metadata Fields 	
	
|Field|Metadata Section|Field in Windows Properties|Field in Lightroom|Fields in IrfanView|Notes|
|-----|---------------|---------------------------|-------------------|------------------|------------|
|Title|IFD0|Details/Description/Title|Title|Visible in IPTC/Document Title. Also,XPTITLE & IMAGE_DESCRIPTION|a)Can be set in event processing using “title” in the JSON b)Due to an issue with Apache Imaging, this field is not written if there is an existing value present.|
|Subject|IFD0|Details/ Description/Subject|Available in Lightroom as Caption|Visible in EXIF/XPSubject|	Can be set in event processing using Description in the JSON|
|IPTC Keywords (In Windows, Tags)|IFD0|Details/Description/Tags – note  when entering values in Windows, values should be separated by semi colons “;”|Keywording, and unique values will appear in KeyWord List. note , when entering, values in Lightroom  should be separated by commas “,"|Appear in XPKeywords and IPTC Keywords|a)	Existing Keywords are retained b)The same Keywords can be added for all files by providing in the JSON c)When moving files to a new directory, the current directory structure is converted to keywords|
|IPTC Date Created|IPTC|Not visible|Not used if other dates are visible|Appear as IPTC/Credits-Origin/ Date Created in YYYYMMDD format|This is written as YYYYMMDD, in line with IrfanView, if the date is modified.|
|Instructions|IPTC|Not visible|Appears as  IPTC/Workflow/Instructions|Appears as IPTC/Description/Special Instructions|a)This field can be used to provide instructions to the program for each image. b)It is updated after processing along with the Comments field. c)This acts the same as the Comments Field (see next item)|
|Comments (Windows)|IFD0|Details/Description/Comments|User Comment – if this is entered in Lightroom, data is written back to Windows. It does not work the other way round – changes to Windows properties are not received by Lightroom.|Visible in EXIF /XP Comment|a)This field can be used to provide instructions to the program for each image.  b)It is updated after processing along with the Instructions field. c)This acts the same as the Instructions field (see previous item)
|Author|IFD0|Details/Origin/Authors|Visible as Contact/Creator and Exif/Artist|Visible in IPTC/Description/ Author (byline)|This can be updated for all files, by adding to JSON|
|Copyright|IFD0|Details/Origin/Copyright|Artist|Visible in IPTC/Description/Copyright|This can be updated for all files by adding to the JSON.|
|ISO Country Code|IPTC|Not visible|IPTC – ISO Country Code|Not visible|This is added by IME, either from lat, lon or from an event.|
|Country|IPTC|Not visible|IPTC – Country|Visible from IPTC /Credits Origin|This is added by IME, either from lat, lon or from an event.|
|Stateprovince|IPTC|Not visible|IPTC – State/Province|Visible from IPTC /Credits Origin|This is added by IME, either from lat, lon or from an event.|
|city|IPTC|Not visible|IPTC – City|Visible from IPTC /Credits Origin|This is added by IME, either from lat, lon or from an event.|
|sublocation|IPTC|Not visible|IPTC - Sublocation|Visible from IPTC /Credits Origin|This is added by IME, either from lat, lon or from an event.|
|Comments (JPG)|Comments|Not visible in Windows|Not visible in Lightroom Classic|Visible from Information / Comments|Each time a file is updated, a comment field is added by IME  e.g. when a file is moved or geocoded.|

# Providing a JSON File

A JSON file can be provided as input for:
* Specifying Events, Places and Cameras
* Modifying other parameters of the program
* Merging results with other runs from IME (across different root directories)

If you run IME without a JSON file (as described above) you can see the JSON file as output and modify the parameters.

	•	IME provides various updating modes, allowing processing without updating files – it is suggested that the program is run initially in read only mode first, to identify and correctly identify Places.
•	It can work across multiple drives and directories if your collection is spread around;
•	 Some directories, file types and files can be excluded from the processing e.g. jpg files are often found in program files e.g. icons, which should be excluded from the analysis.
•	Files below a certain size can also be excluded;
•	Other metadata can also be added :
•	IPTC Copyright
•	IPTC Keywords


# References
	
# For Developers
IME has been developed in Java 17 with Maven build on Intellij. 
	
## Use Cases
1. User wants to geocode entire collection of photos.  
   First run against part of the collection using read-only option to identify Places:
	```ImageMetadataEnhancer.exe "d:/ALL Photos/2000-2005" "d:/Results"  
   Then run against entire collection, providing input file with known place names.
	```ImageMetadataEnhancer.exe "d:/ALL Photos" "d:/Results/config.json" update```
2. User wants to geocode collection in two parts.  First run against part of the collection using read-only option
2. User wants to goecode collection in two parts.
