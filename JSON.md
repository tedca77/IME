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

<a href="https://github.com/tedca77/IME/blob/main/README.md">Back to ReadMe page.</a>
