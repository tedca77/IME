# Image Metadata Enhancer
IME (Image Metadata Enhancer) improves the metadata information on images and helps you discover and organise photos on your drives.

It is designed for image collections with **latitude and longitude** geographic information (e.g. usually taken on mobile phones, but also on many digital cameras) by converting latitude and longitude to recognisable place addresses. (This is known as “reverse geocoding”.) The IME software updates address information in the **IPTC** metadata and **Windows Tags** so the information is visible in tools such as Adobe Lightroom and Adobe Bridge which support the IPTC metadata standard and also via Windows File Manager search.

IME works through all images on one or more drives or directories and uses the **Open Street Map** web lookup service to populate the IPTC section with the full location. (You need to be connected to the Internet for this to work.) IME will also identify duplicate images in different folders.

IME has been designed so that it can be run repeatedly on a collection of images – it will not redo geocoding if it has already been processed. It can also copy files to a new, organised structure based on Year and Month, leaving behind any duplicate files.  IME has extensive reporting and output of metadata information, including a set of HTML pages, which can be viewed, with a browser – this includes thumbnails of each image. (see below).

IME is simple to run, from a terminal session on Windows, MacOS or Linux. It can either be run as a jar file, or packaged as an "exe" file on Windows or a dmg file on MacOS. Once you have familiarised yourself with IME, parameters can be adjusted by providing a JSON input file.  You will need to understand the basics of JSON to make use of some of the <a href="https://github.com/tedca77/IME/blob/main/ADVANCED.md">advanced features</a>.   IME always outputs a correctly formatted JSON file which can be used as input for future runs.

IME is copyright but available under an open source licence - source code is available on this Github repository.

# Why is Image Metadata Enhancer needed?
1.	Many personal and small organisation image libraries are disorganised  – this software tool helps discover duplicates and organises files.
2.	Many "Cloud-based" image libraries provide reverse geocoding when a file is uploaded but do not update the metadata within photos. 
3.	IME helps you view the geographical information and remind you of trips and visits, and helps to identify where photos have been taken.
4.	Provides location searching with tools such as Adobe Lightroom Classic and Adobve Bridge, as well as Windows search.
# About Image Metadata
Image metadata in stored within each JPEG image file (JPEG files are produced by most mobile phones). The metadata is organised in a set of categories.  These categories include:
* **EXIF** – this contains the longitude and latitude information, as well as information on the camera / phone,  date of capture etc.
* **IPTC** – this contains the additional geographical information e.g. City, State /Province, Country etc.  Windows Explorer and Windows Properties do not show most of the IPTC data – the easiest way to see this is using the freeware IrfanView ( an excellent image viewer and editor which is useful for sizing and cropping images).  ExifTool is another excellent program that can be used to view all metadata.  You can also use Adobe Lightroom (which I also use) and other professional software most of which is quite expensive. Adobe Bridge is free software which provides equivalent features to Lightroom for editing IPTC metadata.
* **JPEG Comments** – this section allows multiple comments to be stored. (Adobe LightRoom Classic and Windows do not show these comments).
* **Windows metadata** (e.g. Title, Comments, Subject, Tags) – Windows shows some of the EXIF and IPTC data as Windows "Properties" – the Windows metadata is available by right clicking on a a file and selecting “Properties” in Windows. However, Windows will pick from both JPEG andf IPTC sections to populate some of these fields, so it is not always that clear which item of metadata is referred to.

More information on use of metadata fields is provided on <a href="https://github.com/tedca77/IME/blob/main/METADATA.md">this page</a>.

# Getting Started
**BEFORE RUNNING ENSURE YOU HAVE A BACKUP OF YOUR PHOTO FILES.**
Image Metadata Enhancer is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY, under the GNU General Public Licence.  While it has been tested extensively, the range of formats possible are large and the program may not operate correctly with all files, so if you have a backup, you should not lose any information.

## Running as jar file (on all platforms)
1. Download the <a href="https://github.com/tedca77/IME/blob/main/DOWNLOADS.md">ImageMetadataEnhancer.jar</a> to your drive e.g. D:/IME/ImageMetadataEnhancer.jar
2. Ensure that version 8 of Java Run Time Environment is installed.
3. Run from a Terminal session with two parameters
   ```java -jar ImageMetadataEnhancer.jar  <directory to search> <output directory>```

**First argument** (directory to search) is the top level directory where the program will start to find photos. IME will then search all subdirectories.
**Second argument** (output directory) is the location of the output.  This should be in a different location than the photos.

If there are spaces in the directory names, you should enclose in double quotes. As an example (also specifying the UTF character set for international characters):

```java -Dfile.encoding=UTF8 -jar d:/ImageMetadataEnhancer.jar “d:/Photos” “d:/Results”```
* The directory with existing photos (to search) is: ```d:/Photos```
* The results will be sent to : ```d:/Results```

## On Windows
1. Download the <a href="https://github.com/tedca77/IME/blob/main/DOWNLOADS.md">IME-1.0.exe</a> to your drive and install.  This will install in the Program Files directory.
2. Run as a Windows command  
   ```C:/Program Files/IME/IME-1.0.exe “d:/Photos” “d:/Results”```

## On MacOS
1. Download the <a href="https://github.com/tedca77/IME/blob/main/DOWNLOADS.md">IME-1.0.dmg</a>
2. Click on the downloaded file to install and copy to the Applications folder
2. Run from a Terminal Session
   ```/Applications/IME.app/Contents/MacOS/IME "/Users/jsmith/Pictures/Photos Library.photoslibrary/Masters" "/Users/jsmith/Documents/IMEResults"```

The output directory should then contain:

* A number of HTML files (Places.html, Files.html, Cameras.html, Duplicates.html etc.). These can be opened with a browser.
* A thumbnail for each photo
* Two KML files (point.kml and track.kml)
* A JSON output file called configYYYYMMDDhhMMss.json - where the date and time are added.

** Please note that for consistency, all file directories should be specified with a forward slash "/".**

It is recommended to follow this sequence when running the software :
1. Run in "read mode" - as above ```java -jar ImageMetadataEnhancer.jar “d:/Photos” “d:/Results”```
2. Look in one of the output files (duplicates.html) and check for duplicates, and delete from file system if true duplicates
3. Rename configYYYYMMDDhhMMss.json to config.json and provide this as an input file (this saves having to look up Open Street Map places again) e.g.
   ```java -jar ImageMetadataEnhancer.jar “d:/config.json”```  (If the JSON file is provided as an input, no other parameters are necessary.)
4. Rerun until all duplicates are removed
5. Run with an update parameter  ```java -jar ImageMetadataEnhancer.jar “d:/config.json” update```.
   If you don't have Adobe Lightroom or Bridge, on Windows then add the geocoding information to Windows Tags (Keywords)  
   ```java -jar ImageMetadataEnhancer.jar “d:/config.json” update addxpkeywords```.
   Lightroom allows searching of the IPTC data but Adobe Bridge does not - if using Adobe Bridge add keywords to the IPTC Keywords field by using the following parameter ```java -jar ImageMetadataEnhancer.jar “d:/config.json” update addiptckeywords```
6. You should now be able to search for photos using Country, State/Province, City and Sub Location:
* In Adobe Lightroom - you can use the four IPTC fields to search
* In AdobeBridge, search using the Keywords field in Advanced Search
* In Windows, search using the File Explorer (Search).
* In MacOS Finder, you can search on the City field.

# Running Multiple Times
IME has been designed so that it can run mutiple times across your image library, without having to rerun all the processing again. It does this by adding comments into each image which provides an audit trail of changes together with information on Places and Events that have been found.  
On completion of processing, IME will add a value to the following fields:
1.	**JPEG Comments section** – this is modified with a new comment for each time the file is processed - this contains a full log of activity;
2.	**Instructions (IPTC)** instructions are provided in this field, and will be updated after processing. This field contains the unique identifier for the Place matched;
3.	**Comments (Windows)** - instructions are provided in this field, and will be updated after processing. This field contains the unique identifier for the Place matched.
      In each case, the command which had been entered will be updated with “DONE” in the three fields described above.) For example:
```
#processedDONE:
#geocodeDONE:50.43,-2.444
#latlonDONE:50.43,-2.444
#placeDONE: 34
#postcodeDONE:SW1A1AA
```
When IME is first run in update mode, ```#processedDONE:``` is added to the JPEG comments sections.  If it is run again, and finds the ```processedDONE:``` text, it will not update metadata.  However, it will still:
* check for duplicates and identify them;
* check if new date information has been provided, and use this to find new Events;
* check if new location information has been provided, and if so, geocode the file;
* reverse geocode any longitude and latitude information, if the Place key is no longer correct, in order to generate a list of Places, and a summary of images for each Place. To avoid the extra processing, if you copy the Places found from a JSON output file to a JSON input file, it will be more efficient.  
  You can use the redo and redoevents parameters to force IME to update files again.

# Additional Argument and Parameters
**Third argument** (optional) – is the new directory which files are to be copied to.  The new directory structure will be /Year/Month/Photo1 etc.  This should be a different area than the directory to search and the output directory.  Files will be copied to the new directory structure using the "Original Date" to put in the correct place.

As an example:
```java -jar ImageMetadataEnhancer.jar “d:/Photos” “d:/Results” “d:/newDir”```
* The directory with existing photos (to search) is: ```d:/Photos```
* The results will be sent to : ```d:/Results```
* Files will be copied to a new structure at : ```d:/newDir``` i.e. ```d:/newDir/2021/08/photo1.jpg```

"Run time" **parameters**  which can be added to the command (after the first, second or third parameters) are:
* **update** – this will update files with new metadata and move files. If this parameter is not provided, then update and move will not take place.
* **overwrite** – this will overwrite existing values in fields.  For instance, if there are already values in the IPTC location metadata fields, then they will be overwritten with new values.  If this is not set, then no overwriting will take place.
* **showmetadata** – this will show all metadata in the output before and after updates.  This option is useful when debugging issues with files.
* **redo** – once a file has been processed, a flag will be placed on the file and if subsequent runs are made, the file will not not be geocoded again if the Place has been found. If this parameter is added, it will force reprocessing.
* **redoevents** – once a file has been processed, and matched against an Event, the file will not be rematched against the same Event.  If Event details have been changed, this will force re-processing of Event information.
* **addxpkeywords** – this copies location address information to XP Keywords (also known as Windows Tags) in addition to the iPTC fields.  This is useful if you want to use Windows search features.
* **addiptckeywords** – this copies location address information to IPTC Keywords in addition to the iPTC fields.  This is useful if you are using Adobe Bridge keyword searching.
* **clear** - this will remove any existing comments in the JPEG Comments section metadata, which have been added by IME. This will force reprocessing next time IME is run.
* **clearallcomments** - this will remove any existing comments in the JPEG Comments, Windows Comments and IPTC Comments section metadata.
* **savefilemetadata** - this will output the file information to the JSON. This is useful if processing your files in a number of batches (and using the append option).
* **append** - this will merge file information with file information in the input JSON file, to look for duplicate files across a number of batches.

# Outputs
## HTML Outputs ##
IME produces a number of HTML files that can be viewed with a browser.  Note that if there are more than 2,000 items, then the files are split up into multiple parts and are named places1.html, places2.html etc, as your browser may not be able to open a file with too many thumbnails.
* cameras.html - lists all cameras and the number of photos taken with each camera (or phone)
* photosbydate.html - lists all photos in date order with a summary of metadata
* tracks.html - links all photos as daily tracks i.e. in date and time order
* places.html - reports all geocoded photos by Place
* errors.html - lists all errors and warnings (e.g. with corrputed metadata)
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

The structure of this file is explained on <a href="https://github.com/tedca77/IME/blob/main/JSON.md">this page</a>.

## KML Output ##
Two KML files are produced for Google mapping:
* Points.kml - provides a point for every place found
* Track.kml - provides daily tracks.
  These are split into multiple parts, as Google has limits on the number of points in a kml file.  These can be be dragged onto a Google map and viewed.  Unfortunately, Google Map does not allow images to be added via a KML file (although KML supports embedded images.) Other mapping providers may support, however.  
  **Example of KML output layered on a Google Map:**

![KML Files layered on a Google Map](/Images/pointstracks.jpg)
# Places
Each longitude and latitude value found is represented as a **“Place”** in IME.  If two images are taken at virtually the same place (i.e. they have very similar latitude and longitude), then this is identified as the same “Place” in IME and IME does not have to use the Open Street Map Service to carry out a second geocoding. (This is a good thing, because Open Street Map is a limited use service, and condition of use is that it is not swamped with requests.)  The user can specify the distance that determines if the Place is the same (in metres).  Also, the geocoding may not identify the exact address e.g. house numbers may be slightly out.  IME can be given a set of known Places in the JSON, before it runs, where the correct house address can be provided. e.g. if 86 Acacia Avenue is found, but the actual address is “85 Acacia Avenue”, then this can be modified in the JSON file and also given a user friendly name e.g. “Our first house”. (Each Place added is given a unique number which can be used to allocate images to this Place if they do not have longitude and latitude – see <a href="https://github.com/tedca77/IME/blob/main/ADVANCED.md">this section</a>.)

The IPTC metadata specification has 4 fields for location - the OpenStreet Map address is applied as follows (by default), although this can be changed:
* **Sublocation** - Amenity, leisure, house-number,road,hamlet,suburb,city_district
* **City** - town, city, village
* **State/Province** - county,state_district
* **Country** - country.

# Viewing information in other tools
* **Adobe Lightroom** - you may have to update the metadata in Lightroom.  Select the Images or directories and right click on one image and select "Read Metadata from Selected Files". Lightroom provides sophisticated searching across IPTC location metadata.
* **Adobe Bridge** - this is free software from Adobe which has good metadata editing capability. However, the latest downloadable version can only search IPTC Keywords (not IPTC location fields).
* **IrfanView** - open a file and click on the Image / Information menu option.  You will see buttons for IPTC Info, EXIF Info and Comments.
* **Windows File Explorer** - right click on any image and select properties. This will allow viewing of some metadata. Most importantly, you can search using the Windows search bar - searching Tags is an efficient way to find information.
* **Apple Finder** - this will search the IPTC City and Province-State metadata field.
* **ExifTool** - this provides a very good report on all metadata on a file.
# Limitations
* IME is currently tested on Windows, Linux and MacOS, and it is believed to operate on all systems that support Java 8 or higher.
* IME is currently only tested with JPEG / JPG files, although should be able to support other formats.
* There is a bug in Apache Imaging library which means that if the Windows Title field already has a value, it cannot be overwritten. This only impacts the use of Events.
* IME has only been tested with UK national postcodes.
* IME support International character sets (i.e. UTF-8), but has currently only been tested on a small subset of languages (including Chinese).

# References
1. Adobe Lightroom [https://www.adobe.com/uk/products/photoshop-lightroom.html]
2. Adobe Bridge [https://www.adobe.com/uk/products/bridge.html]
3. IrfanView [https://www.irfanview.com]
4. ExifTool [https://exiftool.org]
5. IPTC Specification [https://iptc.org/std/photometadata/specification/IPTC-PhotoMetadata]
6. IPTC Message Group [https://groups.io/g/iptc-photometadata/messages]

	
	
