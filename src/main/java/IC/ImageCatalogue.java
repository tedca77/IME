package IC;
import IC.openmaps.OpenMaps;
import IC.openmaps.ReverseGeocodeObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icafe4j.image.meta.Metadata;
import com.icafe4j.image.meta.MetadataEntry;
import com.icafe4j.image.meta.MetadataType;
import com.icafe4j.image.meta.iptc.IPTC;
import com.icafe4j.image.meta.iptc.IPTCApplicationTag;
import com.icafe4j.image.meta.iptc.IPTCDataSet;
import com.icafe4j.image.meta.iptc.IPTCObjectDataTag;
import com.icafe4j.string.StringUtils;
import com.icafe4j.image.meta.xmp.XMP;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.*;

import static IC.ImageProcessing.createThumbFromPicture;
//
public class ImageCatalogue {
    static int count = 0;
    static int countTooSmall = 0;
    static int countFromMake = 0;
    static int countGEOCODED = 0;
    static ArrayList<CameraObject> cameras = new ArrayList<>();
    static ArrayList<FileObject> fileObjects = new ArrayList<>();
    static ArrayList<ReverseGeocodeObject> geoObjects = new ArrayList<>();
    public static void main(String[] args) {
        ConfigObject config=null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            Path fileName = Path.of("D:\\ImageCatalogue\\config.json");
            String result = Files.readString(fileName);
            config = mapper.readValue(result, ConfigObject.class);
            for (DriveObject d : config.getDrives())
            {
                System.out.println("Starting==============================================================");
                if (createTempDirForUser(config.getTempdir())) {
                    //recursively find all file
                    readDirectoryContents(new File(d.getStartdir()),d.getExcludespec(),d.getStartdir(),config.getTempdir(),d.getExcludespec().getDirectories(),d.getExcludespec().getFileprefixes(),config);
                }
                System.out.println("End of program" + "Photos found:" + count+"============================================");
                System.out.println("              " + "Photos too small:" + countTooSmall+"============================================");
            }

        }
        catch(Exception e)
        {
           System.out.println("Error reading json file "+e);
        }
        int i=0;
        for(FileObject f:fileObjects)
        {
            System.out.println("Key"+f.getFileKey()+" ,Camera:"+f.getCameraMaker()+", Model"+f.getCameraModel()+", Date:"+f.getFileCreated()+", FileName:"+f.getFileName());
            i++;
            if(i!=f.getFileKey())
            {
                System.out.println("THIS ONE MISSING - Key"+f.getFileKey()+" ,Camera:"+f.getCameraMaker()+", Model"+f.getCameraModel()+", Date:"+f.getFileCreated()+", FileName:"+f.getFileName());


            }
            if(f.getLatitude()!=null)
            {
                System.out.println("Lattitude"+f.getLatitude());
                System.out.println("Longtitude"+f.getLongitude());
                System.out.println("Location"+f.getMetaData());


            }
        }
        System.out.println("Count from FileObjects:"+i);
        for(CameraObject c: cameras)
        {
            System.out.println("Key"+c.getCameraKey()+" ,Camera:"+c.getCameraMaker()+", Model"+c.getCameraModel()+", Start Date:"+c.getStartDate()+", End Date:"+c.getEndDate()+", Camera Count:"+c.getCameraCount());
            countFromMake=countFromMake+c.getCameraCount();
        }
        System.out.println("Count from Makes:"+countFromMake);
        System.out.println("Reverse Geolocation Objects:");
        for(ReverseGeocodeObject g: geoObjects)
        {
            System.out.println("Key"+g.getLat()+","+g.getLon()+","+g.getDisplay_name());
            System.out.println("Country Code:"+g.getIPTCCountryCode());
            System.out.println("Country:"+g.getIPTCCountry());
            System.out.println("State Province:"+g.getIPTCStateProvince());
            System.out.println("City:"+g.getIPTCCity());
            System.out.println("Sublocation:"+g.getIPTCSublocation());

        }
        config.setCameras(cameras);
        config.setPhotos(fileObjects);
        config.setPlaces(geoObjects);

        ObjectMapper objectMapper = new ObjectMapper();

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");

        String dateString = format.format( new Date()   );
        try {
            objectMapper.writeValue(new File("D:\\ImageCatalogue\\config" + format.format(new Date()) + ".json"), config);
        }
        catch(Exception e)
        {
            System.out.println("Cannot write out file"+e);
        }






    }
    //recursively find files


    // convert grid reference to Google Coordinates

    // read metadata;


    // create objects for every file

    // sort object name

    // check duplicates....
    public static void readDirectoryContents(File dir, ExcludeSpec exclude, String startDir, String tempDir, ArrayList<DirectoryObject> excludeDir,ArrayList<DirectoryObject> filePrefixes,ConfigObject config) {
        try {
            File[] files = dir.listFiles();

            for (File file : files) {
                try {
                    if (file.isDirectory()) {
                        //System.out.println("directory:" + file.getCanonicalPath());
                        if (!isExcluded(file.getCanonicalPath(),startDir,excludeDir)) {
                            if(!file.getCanonicalPath().equals(tempDir)) {
                                readDirectoryContents(file,exclude,startDir,tempDir,excludeDir,filePrefixes,config);
                            }
                        }

                    } else {
                        if (isImage(file.getName())) {
                            if(!isExcludedPrefix(file.getName(),filePrefixes)) {
                                long fileSize = file.length();
                                long minSize = 4880L;
                                System.out.println("File size is:" + fileSize);
                                if (fileSize > minSize) {
                                    count++;
                                    System.out.println(count + "     file:" + file.getCanonicalPath() + ",Name:" + file.getName());
                                    displayMetadata(file,config);
                                    String thumbName = makeThumbName(file);
                                    System.out.println("Thumbfilename is:" + thumbName);
                                    createThumbFromPicture(file, tempDir, thumbName, 400, 400);
                                } else {
                                    countTooSmall++;
                                    System.out.println("*********************file:" + file.getCanonicalPath() + ",Name:" + file.getName());
                                    System.out.println("*********************File too small - size is:" + fileSize);
                                }
                            }
                        }
                    }
                } catch (Exception ee) {
                    System.out.println("Cannot read directory:" + file.getCanonicalPath());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String assembleLocation(ReverseGeocodeObject g, ArrayList<String> fieldnames)
    {
        String s="";
        for(String f : fieldnames) {
            if (f.equals("country_code")) {

                s =  addIfNotNull(s,g.getAddress().getCountry_code().toUpperCase()) ;
            }
            if (f.equals("country")) {
                s =  addIfNotNull(s,g.getAddress().getCountry());
            }
            if (f.equals("county")) {
                s =  addIfNotNull(s,g.getAddress().getCounty());
            }
            if (f.equals("city")) {
                s =  addIfNotNull(s,g.getAddress().getCity());
            }
            if (f.equals("postcode")) {
                s =  addIfNotNull(s,g.getAddress().getPostcode());
            }
            if (f.equals("road")) {
                s =  addIfNotNull(s,g.getAddress().getRoad());
            }
            if (f.equals("house_number")) {
                s =  addIfNotNull(s,g.getAddress().getHouse_number());
            }
            if (f.equals("state")) {
                s =  addIfNotNull(s,g.getAddress().getState());
            }
            if (f.equals("state_district")) {
                s =  addIfNotNull(s,g.getAddress().getState_district());
            }
            if (f.equals("village")) {
                s =  addIfNotNull(s,g.getAddress().getVillage());
            }
            if (f.equals("hamlet")) {
                s = addIfNotNull(s,g.getAddress().getHamlet());
            }
            if (f.equals("postcode")) {
                s = addIfNotNull(s,g.getAddress().getPostcode());
            }
            if (f.equals("town")) {
                s = addIfNotNull(s,g.getAddress().getTown());
            }
        }
        // replace spare comma at the end....
        s=s.trim();
        if(s.endsWith(","))
        {
            s = s.substring(0,s.length() - 1);
        }
        return s;
    }
    public static String addIfNotNull(String s,String valString)
    {
        if(valString!=null)
        {
            return s+valString+", ";
        }
        return s;
    }
    public static  Integer addCamera(String make,String model,Date d)
    {


            for (CameraObject c : cameras) {
                if (make.equals(c.getCameraMaker())) {
                    if (model != null) {
                        if (model.equals(c.getCameraModel())) {

                            if (d.before(c.getStartDate())) {
                                c.setStartDate(d);
                            }
                            if (d.after(c.getEndDate())) {
                                c.setEndDate(d);
                            }
                            c.setCameraCount(c.getCameraCount()+1);
                            return c.getCameraKey();
                        }


                    } else {
                        if (c.getCameraModel() == null) {
                            if (d.before(c.getStartDate())) {
                                c.setStartDate(d);
                            }
                            if (d.after(c.getEndDate())) {
                                c.setEndDate(d);
                            }
                            c.setCameraCount(c.getCameraCount()+1);
                            return c.getCameraKey();
                        }
                    }

                }
            }


        Integer newKey =cameras.size()+1;

        CameraObject cNew = new CameraObject();
        cNew.setCameraKey(newKey);
        cNew.setCameraMaker(make);
        cNew.setCameraModel(model);
        cNew.setStartDate(d);
        cNew.setEndDate(d);
        cNew.setCameraCount(1);
        cameras.add(cNew);
        return newKey;
    }
    public static ReverseGeocodeObject checkCachedGeo(double lat,double lon)
    {
        for(ReverseGeocodeObject g : geoObjects)
        {
            Double glat=Double.parseDouble(g.getLat());
            Double glon=Double.parseDouble(g.getLon());
            if(Math.abs(glat-lat)<0.001 && Math.abs(glon-lon)<0.001)
            {
                System.out.println("found existing one");
                return g;
            }
        }

        return null;
    }
    public static boolean createTempDirForUser(String temp) {
        try {
            new File(temp).mkdir();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public static String makeThumbName(File f)
    {
        try {
            String newName = f.getCanonicalPath().replace("\\", "_").replace(":","_");
            //remove the root

            // replace any directory slashes with underscores
            return newName;
        }
        catch(Exception e)
        {
            System.out.println("Could not determine the Thumbname");
            return f.getName();
        }

    }
    public static Boolean isExcluded(String fdir,String startDir,ArrayList<DirectoryObject> excludeDir) {
        for (DirectoryObject i : excludeDir) {
            if (fdir.equals(startDir + i.getName())) {
                System.out.println("Excluded:" + fdir);
                return true;
            }
        }
        //  System.out.println("Not Excluded:"+fname);
        return false;
    }
    public static Boolean isExcludedPrefix(String fname,ArrayList<DirectoryObject> excludePrefix) {
        for (DirectoryObject i : excludePrefix) {
            System.out.println("Excluded Prefix checking:" + i.getName());
            System.out.println("Excluded Prefix checking:" + fname);
            if (fname.indexOf(i.getName())==0) {
                System.out.println("Excluded Prefix:" + fname);
                return true;
            }
        }
        //  System.out.println("Not Excluded:"+fname);
        return false;
    }

    public static Boolean isVideo(String fname) {
        String[] vids = "mp4~mp4a".toLowerCase().split("~", -1);
        String result = FilenameUtils.getExtension(fname).toLowerCase();
        for (int kk = 0; kk < vids.length; kk++) {
            if (result.equals(vids[kk])) {
                return true;
            }
        }
        return false;
    }

    public static Boolean isImage(String fname) {
        String[] pics = "jpg~jpeg~bmp".toLowerCase().split("~", -1);
        String result = FilenameUtils.getExtension(fname).toLowerCase();
        for (int kk = 0; kk < pics.length; kk++) {
            if (result.equals(pics[kk])) {
                return true;
            }
        }
        return false;
    }
    private static Date getFileDate(File file) {
        Date d = null;
        try {



            BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            try {
                d = new Date(attr.lastModifiedTime().toMillis());
                System.out.println("lastModifiedTime: " + attr.lastModifiedTime());
            }
            catch(Exception e)
            {
                try
                {
                    d = new Date(attr.creationTime().toMillis());
                    System.out.println("creationTime: " + attr.creationTime());
                    System.out.println("lastAccessTime: " + attr.lastAccessTime());
                }
                catch(Exception ee)
                {
                    d= new Date();
                }
            }

        }
        catch(Exception e)
        {
            System.out.println("Using current date");
            d=new Date();
        }

        return d;
    }
    private static String getTagValueString(final JpegImageMetadata jpegMetadata,
                                      final TagInfo tagInfo) {
        final TiffField field = jpegMetadata.findEXIFValueWithExactMatch(tagInfo);
        if (field == null) {
            return "";
        } else {
            return field.getValueDescription().replace(", ","");
        }
    }
    private static Date getTagValueDate(final JpegImageMetadata jpegMetadata,
                                        final TagInfo tagInfo) {
        final TiffField field = jpegMetadata.findEXIFValueWithExactMatch(tagInfo);
        //'2011:04:23 19:02:00'

        if (field == null) {
            return null;
        } else {
            try {
                SimpleDateFormat inputFormatter = new SimpleDateFormat("yyyy:MM:dd hh:mm:ss");
                 Date inputDate = inputFormatter.parse(field.getValueDescription().replace("'",""));
                return inputDate;
            }
            catch(Exception e)
            {
                try
                {
                    SimpleDateFormat inputFormatter = new SimpleDateFormat("dd MMM yyyy hh:mm:ss", Locale.ENGLISH);
                    Date inputDate = inputFormatter.parse(field.getValueDescription().replace("'",""));
                    return inputDate;
                }
                catch(Exception ee)
                {
                    System.out.println("******* error converting date:"+field.getValueDescription()+e);
                    return null;
                }

            }
        }
    }
    private static void printTagValue(final JpegImageMetadata jpegMetadata,
                                      final TagInfo tagInfo) {
        final TiffField field = jpegMetadata.findEXIFValueWithExactMatch(tagInfo);
        if (field == null) {
            System.out.println(tagInfo.name + ": " + "Not Found.");
        } else {
            System.out.println(tagInfo.name + ": "
                    + field.getValueDescription());
        }
    }

    public static Boolean displayMetadata(File file,ConfigObject config) {
        String make="No Metadata";
        String model="No Metadata";
        Date d = null;
        double longitude=0.0d ;
        double latitude=0.0d ;
        Boolean geoFound=false;
        Date lastModifiedDate=null;
        Date createdDate=null;
        Date lastAccessDate=null;

        try {
            BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            lastModifiedDate = new Date(attr.lastModifiedTime().toMillis());
            System.out.println("lastModifiedTime: " + attr.lastModifiedTime());
            createdDate = new Date(attr.creationTime().toMillis());
            lastAccessDate = new Date(attr.lastAccessTime().toMillis());
        }
        catch(Exception e)
        {

        }


try {
    System.out.println("tag"+IPTCApplicationTag.CITY);
    System.out.println("tag"+IPTCApplicationTag.COUNTRY_CODE);
    Map<MetadataType, Metadata> metadataMap = Metadata.readMetadata(file.getPath());
    for (Map.Entry<MetadataType, Metadata> entry : metadataMap.entrySet()) {
        Metadata meta = entry.getValue();
        if (meta instanceof XMP) {
            XMP.showXMP((XMP) meta);

        }
        else if (meta instanceof IPTC){
            String city="";
            String country="";
            String country_code="";
            String subLocation="";
            String stateProvince="";
            Boolean overwrite=false;
            List<IPTCDataSet> iptcs =  new ArrayList<IPTCDataSet>();
            iptcs.add(new IPTCDataSet(IPTCApplicationTag.COPYRIGHT_NOTICE, "Copyright 2014-2016, Ted Carroll"));
            iptcs.add(new IPTCDataSet(IPTCApplicationTag.CATEGORY, "ICAFE"));
            iptcs.add(new IPTCDataSet(IPTCApplicationTag.KEY_WORDS, "Welcome 'icafe' user!"));


            Iterator<MetadataEntry> iterator = meta.iterator();
            while (iterator.hasNext()) {
                MetadataEntry item = iterator.next();
                if(item.getKey().equals(IPTCApplicationTag.CITY.getName()))
                {
                    if(item.getValue().length()>0  && !overwrite)
                    {
                        city=item.getValue();
                    }
                    System.out.println("City currently is:"+item.getValue());
                }
                else if(item.getKey().equals(IPTCApplicationTag.COUNTRY_CODE.getName()))
                {
                    if(item.getValue().length()>0  && !overwrite)
                    {
                        country_code=item.getValue();
                    }
                }
                else if(item.getKey().equals(IPTCApplicationTag.COUNTRY_NAME.getName()))
                {
                    if(item.getValue().length()>0  && !overwrite)
                    {
                        country_code=item.getValue();
                    }
                }
                else if(item.getKey().equals(IPTCApplicationTag.SUB_LOCATION.getName()))
                {
                    if(item.getValue().length()>0  && !overwrite)
                    {
                        subLocation=item.getValue();
                    }
                }
                else if(item.getKey().equals(IPTCApplicationTag.PROVINCE_STATE.getName()))
                {
                    if(item.getValue().length()>0  && !overwrite)
                    {
                        stateProvince=item.getValue();
                    }
                }
                else
                {
                    try {
                        iptcs.add(new IPTCDataSet(IPTCApplicationTag.valueOf(item.getKey()), item.getValue()));
                    }
                    catch(Exception e)
                    {
                        System.out.println("Error copying ITPC data:"+ item.getKey()+item.getValue());
                    }

                }
                System.out.println("IPTC iterator:"+ item.getKey()+item.getValue());
            }
            if (country_code.length() > 0) {
                iptcs.add(new IPTCDataSet(IPTCApplicationTag.COUNTRY_CODE, country_code.toUpperCase()));
            }
            if (country.length() > 0) {
                iptcs.add(new IPTCDataSet(IPTCApplicationTag.COUNTRY_NAME, country));
            }
            if (stateProvince.length() > 0) {
                iptcs.add(new IPTCDataSet(IPTCApplicationTag.PROVINCE_STATE, stateProvince));
            }
            if (subLocation.length() > 0) {
                iptcs.add(new IPTCDataSet(IPTCApplicationTag.SUB_LOCATION, subLocation));
            }
            if (city.length() > 0) {
                iptcs.add(new IPTCDataSet(IPTCApplicationTag.CITY, city));
            }

        }
        else {
            Iterator<MetadataEntry> iterator = entry.getValue().iterator();
            while (iterator.hasNext()) {
                MetadataEntry item = iterator.next();
                printMetadata(item, "", "     ");
            }
        }
    }


}
catch(Exception e)
{
    System.out.println("error reading metadata"+e);
}



System.out.println("ICAFE COMPLETED");

        try {
            final ImageMetadata metadata = Imaging.getMetadata(file);

            // System.out.println(metadata);

            if (metadata instanceof JpegImageMetadata) {
                final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;

                // Jpeg EXIF metadata is stored in a TIFF-based directory structure
                // and is identified with TIFF tags.
                // Here we look for the "x resolution" tag, but
                // we could just as easily search for any other tag.
                //
                // see the TiffConstants file for a list of TIFF tags.

                System.out.println("file: " + file.getPath());

                // print out various interesting EXIF tags.
               make= getTagValueString(jpegMetadata,TiffTagConstants.TIFF_TAG_MAKE);
                if(make.length()>0) {
                    System.out.println("Make is:" + make);
                }
                else
                {
                    make="Unknown";
                }
                model= getTagValueString(jpegMetadata,TiffTagConstants.TIFF_TAG_MODEL);
                if(model.length()>0) {
                    System.out.println("Model is:" + model);
                }
                else
                {
                    model="Unknown";
                }
                d= getTagValueDate(jpegMetadata, TiffTagConstants.TIFF_TAG_DATE_TIME);
                if(d==null)
                {
                    d= getTagValueDate(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
                }
                if(d==null) {
                    d = getTagValueDate(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED);
                }





                printTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_XRESOLUTION);
                printTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_DATE_TIME);
                printTagValue(jpegMetadata,
                        ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
                printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED);
                printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_ISO);
                printTagValue(jpegMetadata,
                        ExifTagConstants.EXIF_TAG_SHUTTER_SPEED_VALUE);
                printTagValue(jpegMetadata,
                        ExifTagConstants.EXIF_TAG_APERTURE_VALUE);
                printTagValue(jpegMetadata,
                        ExifTagConstants.EXIF_TAG_BRIGHTNESS_VALUE);
                printTagValue(jpegMetadata,
                        GpsTagConstants.GPS_TAG_GPS_LATITUDE_REF);
                printTagValue(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LATITUDE);
                printTagValue(jpegMetadata,
                        GpsTagConstants.GPS_TAG_GPS_LONGITUDE_REF);
                printTagValue(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LONGITUDE);

                System.out.println();

                // simple interface to GPS data
                final TiffImageMetadata exifMetadata = jpegMetadata.getExif();
                if (null != exifMetadata) {
                    final TiffImageMetadata.GPSInfo gpsInfo = exifMetadata.getGPS();
                    if (null != gpsInfo) {
                        geoFound=true;
                        countGEOCODED++;
                        final String gpsDescription = gpsInfo.toString();
                        longitude = gpsInfo.getLongitudeAsDegreesEast();
                        latitude = gpsInfo.getLatitudeAsDegreesNorth();

                        System.out.println("    " + "GPS Description: "
                                + gpsDescription);
                        System.out.println("    "
                                + "GPS Longitude (Degrees East): " + longitude);
                        System.out.println("    "
                                + "GPS Latitude (Degrees North): " + latitude);
                    }
                }

                // more specific example of how to manually access GPS values
                final TiffField gpsLatitudeRefField = jpegMetadata.findEXIFValueWithExactMatch(
                        GpsTagConstants.GPS_TAG_GPS_LATITUDE_REF);
                final TiffField gpsLatitudeField = jpegMetadata.findEXIFValueWithExactMatch(
                        GpsTagConstants.GPS_TAG_GPS_LATITUDE);
                final TiffField gpsLongitudeRefField = jpegMetadata.findEXIFValueWithExactMatch(
                        GpsTagConstants.GPS_TAG_GPS_LONGITUDE_REF);
                final TiffField gpsLongitudeField = jpegMetadata.findEXIFValueWithExactMatch(
                        GpsTagConstants.GPS_TAG_GPS_LONGITUDE);
                if (gpsLatitudeRefField != null && gpsLatitudeField != null &&
                        gpsLongitudeRefField != null &&
                        gpsLongitudeField != null) {
                    // all of these values are strings.
                    final String gpsLatitudeRef = (String) gpsLatitudeRefField.getValue();
                    final RationalNumber gpsLatitude[] = (RationalNumber[]) (gpsLatitudeField.getValue());
                    final String gpsLongitudeRef = (String) gpsLongitudeRefField.getValue();
                    final RationalNumber gpsLongitude[] = (RationalNumber[]) gpsLongitudeField.getValue();

                    final RationalNumber gpsLatitudeDegrees = gpsLatitude[0];
                    final RationalNumber gpsLatitudeMinutes = gpsLatitude[1];
                    final RationalNumber gpsLatitudeSeconds = gpsLatitude[2];

                    final RationalNumber gpsLongitudeDegrees = gpsLongitude[0];
                    final RationalNumber gpsLongitudeMinutes = gpsLongitude[1];
                    final RationalNumber gpsLongitudeSeconds = gpsLongitude[2];

                    // This will format the gps info like so:
                    //
                    // gpsLatitude: 8 degrees, 40 minutes, 42.2 seconds S
                    // gpsLongitude: 115 degrees, 26 minutes, 21.8 seconds E

                    System.out.println("    " + "GPS Latitude: "
                            + gpsLatitudeDegrees.toDisplayString() + " degrees, "
                            + gpsLatitudeMinutes.toDisplayString() + " minutes, "
                            + gpsLatitudeSeconds.toDisplayString() + " seconds "
                            + gpsLatitudeRef);
                    System.out.println("    " + "GPS Longitude: "
                            + gpsLongitudeDegrees.toDisplayString() + " degrees, "
                            + gpsLongitudeMinutes.toDisplayString() + " minutes, "
                            + gpsLongitudeSeconds.toDisplayString() + " seconds "
                            + gpsLongitudeRef);

                }

                System.out.println();

                final List<ImageMetadata.ImageMetadataItem> items = jpegMetadata.getItems();

                for (int i = 0; i < items.size(); i++) {
                    final ImageMetadata.ImageMetadataItem item = items.get(i);
                    System.out.println("    " + "item: " + item);



                }

                System.out.println();
                System.out.println("Date is:" + d);

            }

        }
        catch(Exception e) {

            System.out.println(e);

        }
        if(d==null)
        {
            d=getFileDate(file);
        }
        Integer cameraKey=addCamera(make,model,d);
        FileObject fNew= new FileObject();
        fNew.setCameraKey(cameraKey);
        fNew.setCameraMaker(make);
        fNew.setCameraModel(model);
        fNew.setFileCreated(d);
        fNew.setFileName(file.getName());
        fNew.setFileKey(count);
        if(geoFound)
        {
            fNew.setLatitude(latitude);
            fNew.setLongitude(longitude);
            ReverseGeocodeObject g= checkCachedGeo(latitude,longitude);
            if(g==null) {
                g = OpenMaps.reverseGeocode(String.valueOf(latitude), String.valueOf(longitude),config);
                System.out.println("*****************Adding a new GeoObject:"+g.getLat()+","+g.getLon()+"Display name:"+g.getDisplay_name());
                if(g!=null) {
                    g.setInternalKey(geoObjects.size()+1);
                    geoObjects.add(g);
                }
                else
                {
                    System.out.println("Could not geocode :Lat"+String.valueOf(latitude)+", Long:" +String.valueOf(longitude));
                }

            }
            if(g!=null)
            {
                fNew.setDisplayName(g.getDisplay_name());
                fNew.setCity(g.getIPTCCity());
                fNew.setCountry_code(g.getIPTCCountryCode());
                fNew.setCountry_name(g.getIPTCCountry());
                fNew.setStateProvince(g.getIPTCStateProvince());
                fNew.setSubLocation(g.getIPTCSublocation());
                List<IPTCDataSet> iptcs =  new ArrayList<IPTCDataSet>();
                iptcs.add(new IPTCDataSet(IPTCApplicationTag.CITY, fNew.getCity()));
                iptcs.add(new IPTCDataSet(IPTCApplicationTag.COUNTRY_CODE, fNew.getCountry_code()));
                iptcs.add(new IPTCDataSet(IPTCApplicationTag.COUNTRY_NAME, fNew.getCountry_name()));
                iptcs.add(new IPTCDataSet(IPTCApplicationTag.SUB_LOCATION, fNew.getSubLocation()));
                iptcs.add(new IPTCDataSet(IPTCApplicationTag.PROVINCE_STATE, fNew.getStateProvince()));
                try {
                    FileInputStream fin = new FileInputStream(file.getPath());
                    String fout_name=FilenameUtils.getFullPath(file.getPath())+"out"+FilenameUtils.getName(file.getPath());
                    File outFile=new File(fout_name);
                    FileOutputStream fout = new FileOutputStream(outFile,false);
                    Metadata.insertIPTC(fin, fout, iptcs, true);
                    fin.close();
                    fout.close();
                    file.delete();
                    Files.copy(outFile.toPath(),file.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
                    outFile.delete();


                    Files.setAttribute(file.toPath(),"creationTime", FileTime.fromMillis(createdDate.getTime()));
                    Files.setAttribute(file.toPath(),"lastAccessTime", FileTime.fromMillis(lastAccessDate.getTime()));
                    Files.setAttribute(file.toPath(),"lastModifiedTime", FileTime.fromMillis(lastModifiedDate.getTime()));
                }
                catch(Exception e)
                {

                }
            }
            else
            {
                fNew.setDisplayName("location not found");
            }





        }
        fileObjects.add(fNew);


        return true;


    }
    private static void printMetadata(MetadataEntry entry, String indent, String increment) {
        System.out.println(indent + entry.getKey() + (StringUtils.isNullOrEmpty(entry.getValue())? "" : ": " + entry.getValue()));
        if(entry.isMetadataEntryGroup()) {
            indent += increment;
            Collection<MetadataEntry> entries = entry.getMetadataEntries();
            for(MetadataEntry e : entries) {
                printMetadata(e, indent, increment);
            }
        }
    }


}
