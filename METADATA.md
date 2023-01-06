# Summary of Metadata Fields

This section provides information on the metadate fields that can be modified by IME.

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

## Notes on Processing

* **Title** : Can be set in event processing. Due to an issue with Apache Imaging, this field is not written if there is an existing value present.  If this value is set, then the IPTC Object Name is also set to the same value.
* **Keywords / Windows Tags** : Windows combines together the EXIF and IPTC keywords for display of properties and searching.  Note, when entering values in Windows, values should be separated by semi colons “;”.   In Lightroom, Keywording, and unique values will appear in the KeyWord List. When entering, values in Lightroom  should be separated by commas “,". In irfanView, enter each IPTC keyword on a separate line. The same Keywords can be added for all files by providing in the JSON - these are added to IPTC fields only. When moving files to a new directory, the current directory structure is converted to IPTC keywords.
* **Subject** : If no description is provided, Windows will show the Subject to be the same as the Title field - can be set in Event processing.
* **JPG Comments** Each time a file is updated, a comment is added by IME. Metadata allows multiple comments to be added to a file. These are displayed separately in IrfanView. e.g. when a file is moved or geocoded.
* **Comments (Windows)** : If this is entered in Lightroom, data is written back to Windows. It does not work the other way round – changes to Windows properties are not received by Lightroom.
* **Instructions** : This field can be used to provide instructions to the program for each image. It is updated after processing along with the Comments (Windows) field. and JPG Comments.
* **IPTC Date Created** : This is written as YYYYMMDD, and is the same value as Original Date Time. This is a text value, allowing partial dates to be entered e.g. YYYY.

<a href="https://github.com/tedca77/IME/blob/main/README.md">Back to ReadMe page.</a>
