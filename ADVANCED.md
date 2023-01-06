# Advanced Features

IME can also be used to add dates to individual photos or groups of photos.  Where the dates or location are missing or incorrect, there are a number of options for manually adding dates or providing locations via postcode, latitute and longitude, already identified "Places" or "Events".

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
This updates the **EXIF Original Date** field (which appears as **"Date Taken"** in Windows properties).     
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
Normally the date of the image is sufficient to find the correct Event.  However, if you want to hard code the event. i.e. not based on a date, you can enter an instruction for the Event  – enter ```#event:123```.
An Event can have the following information in the JSON:
*	**title**: This populates the Title / ImageDescription field;
*	**description**: This populates the Subject / Caption field;
*	**keywords**: (separated by semi-colons) This populates the IPTC keywords section;
*	**eventcalendar**: This is used to indicate that the same date each year will be found e.g. for a birthday (MM-DD or YYYY-MM-DD – if the year is provided, then IME will only find dates equal or after the year provided);
*	**eventdate**: (YYYY-MM-DD) – the date (or start date) of a single event;
*	**enddate**: (YYYY-MM-'DD) - Optional  - the end date of a single event;
*	**eventtime**: (HH.mm) – Optional – the start time of a single event;
*	**endtime**: (HH.mm) – Optional – the end time of a single event;
*	**location**:  a location can be provided it latitude and longitude is not present on the files - the format is either #latlon:,#postcode: or #place:, followed by appropriate values (as described in the "adding geolocation" section).

In order of priority, IME will process files as follows:
* Dates are set irrespective of other instructions. If a new date is found, this can determine if new Events are applicable;
* If longitude and latitude metadata is present in a file, it will be used to geocode, if it has not already been geocoded;
* If there is no latitude and longitude present, then any Place, Postcode or Latlon instructions can be used;
* If an Event is found, then metadata can be updated, although IME will not overwrite any existing latitude and longitude information.  If multiple Events are matched (e.g. a holiday and a birthday during the holiday), the title, description and keywords are concatenated together in the metadata.

# Moving Files to a New Directory
One of the features of IME is to move all images to a new directory structure.  This is of the structure:
```YEAR / MONTH / FILENAME``` ie.  ```2016/05/WA123245.JPG```.

When moving files, the old directory location is written in the Comments metadata. The old directory structure is also added to the Windows keywords metadata, as this could include information on the origin of the files that have been moved (such as Date).

If files have true duplicates, then the second and subsequent files will not be copied across to the new file directory (although they will be updated e.g. geocoded).

However, if there are two files with the same name, which were taken in the same month, but they are different files (ie.. different creation date, size, camera), then they are copied across to the new Directory, but they are renamed by adding a sequential number to the end of the file name when they are copied. e.g.
File``` WA1234.JPG``` becomes ```WA1234_001.JPG```.

# Finding Duplicates
When the program runs it will look for duplicate files.
In IME, a duplicate file is where two files have:
* the same name
* the same creation date
* the same camera maker and model.

Most new cameras produce a filename that is likely to be unique as it includes the time of capture down to a thousandth of a second in the file name. However, earlier cameras and phones (and scanners) had much shorter file names and the chance of duplicate file names in your archives is high for older material.

IME produces a report of any possible duplicates and when in "update mode" puts a comment in the metadata (for any duplicates) to indicate it has been identified as a duplicate. It is recommended to run IME in read-only mode first, to determine any images that have been duplicated, and so that dupicates can be removed before processing.

IME will only look for duplicates in the directory structure being processed. If you only process part of your collection, then it will not pick up duplicates elsewhere.  You can get round this in two ways a) by processing all files in one run or b)by providing the JSON output file from other runs as an input file for the program - this will then match against all images in your collection.  For instance, if your collection is split into **Photos before 2000** and **Photos after 2000** then run IME against the "Photos before 2000" directory, rename the JSON output file and provide as an input file when running against "Photos after 2000".  You will then get a report of images for your complete collection and any duplicates will be identified across the complete collection.

# Use of Open Street Map
IME uses Open Street Map APIs to carry out reverse geocoding and post code look up.  This requires an internet connection. For **post code** lookup, you will also require an Open Street Map API key. This is entered in the JSON file - further information available below.  IME minimises calls to Open Street Map by saving Places and checking Places before making a call to OpenStreet Map. (This also speeds up operation.)   For each new longitude and latidtude pair, IME calculates how close it is to each of the saved places and if within a specified distance, it will use the saved Place. So if you have taken 10 photos in the same location, only one call to Open Street Map is required.   A sensible distance is 75 metres, although this can be modified in the JSON input file. Places are also written out to the JSON file and can be used as input to other runs, removing the need for an Open Street Map look up when an image is next found close to other images.  It is recommended that Places information in the JSON output file is always copied to the next JSON input file, to save making calls to Open Street Map.

<a href="https://github.com/tedca77/IME/blob/main/README.md">Back to ReadMe page.</a>
