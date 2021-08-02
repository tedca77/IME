package IC;

import IC.openmaps.OpenMaps;
import IC.openmaps.ReverseGeocodeObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icafe4j.image.meta.Metadata;
import com.icafe4j.image.meta.MetadataEntry;
import com.icafe4j.image.meta.MetadataType;
import com.icafe4j.image.meta.exif.Exif;
import com.icafe4j.image.meta.exif.ExifTag;

import com.icafe4j.image.meta.image.Comments;
import com.icafe4j.image.meta.iptc.IPTC;
import com.icafe4j.image.meta.iptc.IPTCApplicationTag;
import com.icafe4j.image.meta.iptc.IPTCDataSet;
import com.icafe4j.image.meta.jpeg.JpegExif;

import com.icafe4j.image.tiff.FieldType;

import com.icafe4j.string.StringUtils;
import com.icafe4j.image.meta.xmp.XMP;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static IC.ImageProcessing.createThumbFromPicture;

/**
 *
 */
public class ImageCatalogue {
    static int count = 0;
    static int countTooSmall = 0;
    static int countFromMake = 0;
    static int countGEOCODED = 0;
    static int countDrive = 0;
    static int countDriveTooSmall = 0;
    static int countDriveGEOCODED = 0;
    static ArrayList<CameraObject> cameras = new ArrayList<>();
    static ArrayList<FileObject> fileObjects = new ArrayList<>();
    static ArrayList<ReverseGeocodeObject> geoObjects = new ArrayList<>();
    static ArrayList<TrackObject> tracks = new ArrayList<>();
    // Variables - that can be modified..
    static int messageLength=160; //length of Console Message
    static String videoDefaults="mp4~mp4a";
    static String imageDefaults="jpg~jpeg~bmp";
    static double cacheDistance=0.001d;
    /**
     * Main Method for the program - arguments passes as Java arguments..
     * @param args
     */
    public static void main(String[] args) {
        ConfigObject config;
        try {
            ObjectMapper mapper = new ObjectMapper();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm ss");
            mapper.setDateFormat(df);
            Path fileName = Path.of(args[0]);
            String result = Files.readString(fileName);
            config = mapper.readValue(result, ConfigObject.class);
            //
            config=setDefaults(config);
            messageLine("*");
            if(config.getUpdate()) {
                message("FILES WILL BE UPDATED");
            }
            //
            if(config.getShowmetadata()) {
                message("METADATA WILL BE SHOWN BEFORE AND AFTER UPDATING");
            }
            if(config.getOverwrite()) {
                message("EXISTING METADATA WILL BE OVERWRITTEN");
            }
            if(config.getCameras()!=null)
            {
                message("NUMBER OF CAMERAS READ FROM CONFIG FILE:"+config.getCameras().size());
                cameras=config.getCameras();
                int i=1;
                for(CameraObject c :cameras)
                {
                    c.setCamerakey(i);
                    c.setCameracount(0);
                    i++;
                }
            }
            if(config.getPlaces()!=null)
            {
                message("NUMBER OF PLACES READ FROM CONFIG FILE:"+config.getPlaces().size());
                geoObjects=config.getPlaces();
                int i=1;
                for(ReverseGeocodeObject r : geoObjects)
                {
                    r.setInternalKey(i);
                    r.setCountPlace(0);
                    i++;
                }
            }

            for (DriveObject d : config.getDrives()) {
                countDrive = 0;
                countDriveTooSmall = 0;
                countDriveGEOCODED = 0;
                message("Starting - drive"+d.getStartdir());
                createTempDirForUser(config.getTempdir());
                //recursively find all files
                readDirectoryContents(new File(d.getStartdir()), d, config.getTempdir(), config);
                messageLine("-");
                message("Photos found on drive "+d.getStartdir()+" :" + countDrive );
                message("Photos too small on drive "+d.getStartdir()+" :" + countDriveTooSmall );
                message("Photos found on drive "+d.getStartdir()+" :" + countDriveGEOCODED );


            }
            // print out total number of photos if more than one drive...
            if(config.getDrives().size()>1) {
                messageLine("*");
                message("All Drives -" + "Photos found:" + count);
                message("All Drives - " + "Photos too small:" + countTooSmall);
                message("All Drives - " + "Photos found:" + countGEOCODED);
            }
            // sort any ArrayLists....
            fileObjects.sort(Comparator.comparing(o -> o.getBestDate()));
            addLinksToPlaces(200,"d://tempIC//");
            //create tracks - this will also update the geoObjects
            if(!createTracks())
            {
                message("Failed to create tracks");
            }
            addLinksToTracks(200,"d://tempIC//");
            //sets config object with new values
            config.setCameras(cameras);
            config.setPhotos(fileObjects);
            config.setPlaces(geoObjects);
            config.setTracks(tracks);
            // exports JSON
            if(!exportConfig(config,fileName))
            {
                message("Failed to export HTML");
            }
            // runs report for console
            runReport();
            //Exports reports for cameras, fileObjects etc...
            if(!exportHTML(config.getTempdir()))
            {
                message("Failed to export HTML");
            }

        } catch (Exception e) {
            message("Error reading json file " + e);
        }

    }

    /**
     * Sets the defaults if the values are not in the JSON
     * @param config - passes ConfigObject
     * @return - returns modified ConfigObject
     */
    public static ConfigObject setDefaults(ConfigObject config)
    {
        if(config.getUpdate()==null){config.setUpdate(false);}
        if(config.getShowmetadata()==null) {config.setShowmetadata(false);}
        if(config.getOverwrite()==null) {config.setOverwrite(false);}
        if(config.getImageextensions()==null) {config.setImageextensions(imageDefaults);}
        if(config.getVideoextensions()==null) {config.setVideoextensions(videoDefaults);}
        return config;
    }
    /**
     * Writes out HTML reports using freemarker for the main objects.  Freemarker templates are in the project.
     * FIles are written to the tempDir
     * @param tempDir - Output directory
     * @return - returns true or false
     */
    public static Boolean exportHTML(String tempDir)
    {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);

        cfg.setClassForTemplateLoading(ImageCatalogue.class, "/Templates");
        // Recommended settings for new projects:
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);
        //
        try {
            Template ctemplate = cfg.getTemplate("cameras.ftl");
            FileWriter writer = new FileWriter(tempDir+"/"+"cameras.html");
            Map<String, Object> croot = new HashMap<>();
            croot.put( "cameras", cameras );
            ctemplate.process(croot, writer);
            writer.close();
            //
            Template ptemplate = cfg.getTemplate("places.ftl");
            FileWriter pwriter = new FileWriter(tempDir+"/"+"places.html");
            Map<String, Object> proot = new HashMap<>();
            proot.put( "places", geoObjects );
            ptemplate.process(proot, pwriter);
            pwriter.close();
            //
            Template ttemplate = cfg.getTemplate("tracks.ftl");
            FileWriter twriter = new FileWriter(tempDir+"/"+"tracks.html");
            Map<String, Object> troot = new HashMap<>();
            troot.put( "tracks",tracks );
            ttemplate.process(troot, twriter);
            twriter.close();
            //
            Template ftemplate = cfg.getTemplate("photosbydate.ftl");
            FileWriter fwriter = new FileWriter(tempDir+"/"+"photosbydate.html");
            Map<String, Object> froot = new HashMap<>();
            froot.put( "photos",fileObjects );
            ftemplate.process(froot, fwriter);
            twriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Exports JSON config file with new lists of objects included.  File is same as input file but with date and time added.
     * @param c - config opbject
     * @param inputPath - input Path Name
     * @return - returns true or false
     */
    public static Boolean exportConfig(ConfigObject c, Path inputPath)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm ss");
        objectMapper.setDateFormat(df);
        String fileName=inputPath.getFileName().toString();
        String newName = c.getTempdir() + "\\"+  FilenameUtils.getBaseName(fileName)+format.format(new Date()) + "."+FilenameUtils.getExtension(fileName);
        try {
            objectMapper.writeValue(new File(newName), c);
            return true;
        } catch (Exception e) {
            message("Cannot write out file"+ newName + " error:" +e);
            return false;
        }
    }
    public static void runReport()
    {

        messageLine("*");
        for (FileObject f : fileObjects) {
            String cameraName="";
            if(f.getCameraName()!=null)
            {
                cameraName="Camera Name: "+f.getCameraName()+",";
            }
            message("Key:[" + f.getFileKey() + "] "+cameraName+"Camera: " + f.getCameraMaker() + ", Model: " + f.getCameraModel() + ", Date: " + f.getFileCreated() );
            message("Directory: " + f.getDirectory()+", FileName: " + f.getFileName());
            if (f.getDisplayName()!= null) {
                  message("Location: " + f.getDisplayName());
            }
            else
            {
                message("No geolocation information "+f.getLongitude()+","+f.getLatitude());
            }
            messageLine("-");
        }
        messageLine("*");

        for (CameraObject c : cameras) {
            message("Key:[" + c.getCamerakey() + "] ,Camera:" + c.getCameramaker() + ", Model" + c.getCameramodel() + ", Start Date:" + c.getStartdate() + ", End Date:" + c.getEnddate() + ", Camera Count:" + c.getCameracount());
            countFromMake = countFromMake + c.getCameracount();
        }
        message("Count from Makes:" + countFromMake);

        messageLine("*");
        for (ReverseGeocodeObject g : geoObjects) {

            message("Key:[" +g.getInternalKey()+"] ,Lat:"+ g.getLat() + ", Lon:" + g.getLon());
            message( g.getDisplay_name());
            message("Country Code:" + g.getIPTCCountryCode());
            message("Country:" + g.getIPTCCountry());
            message("State Province:" + g.getIPTCStateProvince());
            message("City:" + g.getIPTCCity());
            message("Sublocation:" + g.getIPTCSublocation());
            messageLine("-");
        }

    }

    /**
     * this method recursively looks at directories and sdubdirectories and identifies image files
     * @param dir - root directory to start
     * @param drive - drive (this is used to make up the new filename)
     * @param tempDir - where files are to be written to
     * @param config - configuration object (this is set up at the start of the program)
     */
    public static void readDirectoryContents(File dir, DriveObject drive, String tempDir, ConfigObject config) {
        try {
            File[] files = dir.listFiles();
            if(files!=null) {
                for (File file : files) {
                    try {
                        if (file.isDirectory()) {
                            //message("directory:" + file.getCanonicalPath());
                            if (!isExcluded(file.getCanonicalPath(), drive.getStartdir(), drive.getExcludespec().getDirectories())) {
                                if (!file.getCanonicalPath().equals(tempDir)) {
                                    readDirectoryContents(file, drive, tempDir, config);
                                }
                            }

                        } else {
                            if (isImage(file.getName(),config.getImageextensions())) {
                                if (!isExcludedPrefix(file.getName(), drive.getExcludespec().getFileprefixes())) {
                                    long fileSize = file.length();
                                    long minSize = 4880L;

                                    if (fileSize > minSize) {
                                        count++;
                                        countDrive++;
                                        messageLine("-");
                                        message("File number:["+count + "], file:" + file.getCanonicalPath());
                                        message("File size is:" + fileSize);
                                        if(config.getShowmetadata()) {
                                            if (!readMetadata(file)) {
                                                message("Could not read metadata before update");
                                            }
                                        }
                                        String thumbName = makeThumbName(file);
                                        Boolean thumbResult=createThumbFromPicture(file, tempDir, thumbName, 400, 400);
                                        if(thumbResult)
                                        {
                                            message("Created Thumbnail:" + thumbName);
                                        }
                                        else
                                        {
                                            message("Could not create Thumbnail:" + thumbName);
                                            thumbName=null;
                                        }
                                        if(!updateMetadata(file, thumbName,config, drive))
                                        {
                                            message("Could not update metadata");
                                        }
                                        if(config.getShowmetadata()) {
                                            if (!readMetadata(file)) {
                                                message("Could not read metadata after update");
                                            }
                                        }

                                    } else {
                                        countTooSmall++;
                                        countDriveTooSmall++;

                                        message("File too small "+ file.getCanonicalPath() + ",Name:" + file.getName()+"- size is:" + fileSize);
                                    }
                                }
                            }
                        }
                    } catch (Exception ee) {
                        message("Cannot read directory:" + file.getCanonicalPath()+"Error:"+ee);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * THis adds one or more geocoding fields to a string which is then used to update the metadata fields used
     * @param g - object retrieved from OpenMaps API
     * @param fieldnames - a list of field names we are interested in
     * @return - String to use for metadata field
     */
    public static String assembleLocation(ReverseGeocodeObject g, ArrayList<String> fieldnames) {
        String s = "";
        for (String f : fieldnames) {
            if (f.equals("country_code")) {

                s = addIfNotNull(s, g.getAddress().getCountry_code().toUpperCase());
            }
            if (f.equals("country")) {
                s = addIfNotNull(s, g.getAddress().getCountry());
            }
            if (f.equals("county")) {
                s = addIfNotNull(s, g.getAddress().getCounty());
            }
            if (f.equals("city")) {
                s = addIfNotNull(s, g.getAddress().getCity());
            }
            if (f.equals("postcode")) {
                s = addIfNotNull(s, g.getAddress().getPostcode());
            }
            if (f.equals("road")) {
                s = addIfNotNull(s, g.getAddress().getRoad());
            }
            if (f.equals("house_number")) {
                s = addIfNotNull(s, g.getAddress().getHouse_number());
            }
            if (f.equals("state")) {
                s = addIfNotNull(s, g.getAddress().getState());
            }
            if (f.equals("state_district")) {
                s = addIfNotNull(s, g.getAddress().getState_district());
            }
            if (f.equals("village")) {
                s = addIfNotNull(s, g.getAddress().getVillage());
            }
            if (f.equals("hamlet")) {
                s = addIfNotNull(s, g.getAddress().getHamlet());
            }
            if (f.equals("postcode")) {
                s = addIfNotNull(s, g.getAddress().getPostcode());
            }
            if (f.equals("town")) {
                s = addIfNotNull(s, g.getAddress().getTown());
            }
        }
        // replace spare comma at the end....
        s = s.trim();
        if (s.endsWith(",")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    /**
     * adds a comma and a value to a string
     * @param s - string to add to = this is either blank or a value ending in a comma (never null)
     * @param valString - new value - it can be null;
     * @return - modified s (or existing value if null)
     */
    public static String addIfNotNull(String s, String valString) {
        if (valString != null) {
            return s + valString + ", ";
        }
        return s;
    }
    public static void addLinksToPlaces(Integer width, String root)
    {
        for(ReverseGeocodeObject r : geoObjects)
        {
            String s= "";
            for(FileObject f: fileObjects)
            {
                if(f.getPlaceKey().equals(r.getInternalKey()))
                {
                    s=s+"<img src=\"d://tempIC//"+f.getThumbnail()+"\" width=\"200\">";
                }
            }
            r.setImagelinks(s);
        }

    }
    public static void addLinksToTracks(Integer width, String root)
    {

        // for each track
        for(TrackObject t : tracks) {
            String s = "";
            // we have a list of points...
            for (Integer i : t.getPoints()) {

                for (FileObject f : fileObjects) {
                    if (f.getPlaceKey().equals(i)) {
                        s = s + "<img src=\"d://tempIC//" + f.getThumbnail() + "\" width=\"200\">";
                    }
                }
            }
            t.setImagelinks(s);
        }
    }
    /**
     * Adds a new camera if it does not exist in the array and sets start and end date
     * If it is not a new camera, start and/or end date are updated in the ArrayList
     * Allows for null camera model
     * @param make - camera make
     * @param model - camera model
     * @param d - date (of photo)
     * @return - returns camera key
     */
    public static Integer addCamera(String make, String model, Date d) {


        for (CameraObject c : cameras) {
            if (make.equals(c.getCameramaker())) {
                if (model != null) {
                    if (model.equals(c.getCameramodel())) {
                        if (d.before(c.getStartdate())) {
                            c.setStartdate(d);
                        }
                        if (d.after(c.getEnddate())) {
                            c.setEnddate(d);
                        }
                        c.setCameracount(c.getCameracount() + 1);
                        return c.getCamerakey();
                    }


                } else {
                    if (c.getCameramodel() == null) {
                        if (d.before(c.getStartdate())) {
                            c.setStartdate(d);
                        }
                        if (d.after(c.getEnddate())) {
                            c.setEnddate(d);
                        }
                        c.setCameracount(c.getCameracount() + 1);
                        return c.getCamerakey();
                    }
                }

            }
        }
        // we have not found a camera, so add a new one to the ArrayList
        Integer newKey = cameras.size() + 1;
        CameraObject cNew = new CameraObject();
        cNew.setCamerakey(newKey);
        cNew.setCameramaker(make);
        cNew.setCameramodel(model);
        cNew.setStartdate(d);
        cNew.setEnddate(d);
        cNew.setCameracount(1);
        cameras.add(cNew);
        return newKey;
    }

    /**
     * Checks whether we already have a geocode object - if we do, then we just update the start or end date
     * @param lat - latitude (of image)
     * @param lon - longitude (of image)
     * @param d - date  (of image)
     * @return - ReverseGeocodeObject i.e. a place...
     */
    public static ReverseGeocodeObject checkCachedGeo(double lat, double lon,Date d) {
      //  message("*****Checking latitude:"+lat);
      //  message("*****Checking longitude:"+lon);
        for (ReverseGeocodeObject g : geoObjects) {
            double glat = Double.parseDouble(g.getLat());
            double glon = Double.parseDouble(g.getLon());
         //   message("*****target latitude:"+glat);
         //   message("*****target longitude:"+glon);
         //   message("Checking difference:"+Math.abs(glat - lat));
         //   message("Checking difference:"+Math.abs(glon - lon));


            if (Math.abs(glat - lat) < cacheDistance && Math.abs(glon - lon) < cacheDistance) {

                Date startDate=g.getStartDate();
                Date endDate=g.getEndDate();
                int count=g.getCountPlace()+1;

                g.setCountPlace(count);
                if(d.before(startDate))
                {
                    g.setStartDate(d);
                }
                if(d.after(endDate))
                {
                    g.setEndDate(d);
                }
                return g;
            }
        }
        return null;
    }

    /**
     * Creates temporary folder for output from the program
     * @param temp - name (from the JSON)
     */
    public static void createTempDirForUser(String temp) {
        try {
            boolean result= new File(temp).mkdir();

        } catch (Exception e) {
            message("Directory may already exist for: "+temp);
        }
    }
    public static Boolean createTracks()
    {
        tracks = new ArrayList<>();
        // Each Place can be part of one or more tracks...
        // if we already have a track for this date... do we redo it...

        Date lastDay=null;
        Integer placeKey=0;
        Date startDate=null;
        Date endDate=null;

        ArrayList<Integer> points = new ArrayList<>();
        for(FileObject f : fileObjects)
        {
            //
            Date d= getDateWithoutTimeUsingCalendar(f.getBestDate());
            if(d.equals(lastDay))
            {
                if(f.getPlaceKey()!=placeKey) {
                    points.add(f.getPlaceKey());
                }
                endDate=f.getBestDate();

            }
            else
            {
                // write out track
                if(points.size()>0) {
                    TrackObject t = new TrackObject();
                    t.setPoints(points);
                    t.setTrackKey(tracks.size() + 1);
                    t.setStartDate(startDate);
                    t.setEndDate(endDate);
                    t.setPlaceCount(points.size());
                    t.setTrackDate(lastDay);
                    tracks.add(t);
                }
                points = new ArrayList<>();
                points.add(f.getPlaceKey());
                startDate=f.getBestDate();
                endDate=f.getBestDate();
                lastDay=getDateWithoutTimeUsingCalendar(f.getBestDate());
                placeKey=f.getPlaceKey();

            }


        }
        // write out track
        if(points.size()>0) {
            TrackObject t = new TrackObject();
            t.setPoints(points);
            t.setTrackKey(tracks.size() + 1);
            t.setStartDate(startDate);
            t.setEndDate(endDate);
            t.setPlaceCount(points.size());
            t.setTrackDate(lastDay);
            tracks.add(t);
        }
        return true;
    }
    public static void message(String s)
    {
        String str=" ";
        if(s.length()>(messageLength-4)) {
            System.out.println("* "+s.substring(0,messageLength-4)+" *");
        }
        else
        {
            int width=messageLength-(s.length()+4);
            String ss= "* "+s+ " "+str.repeat(width);
            if(ss.length()<messageLength)
            {
                ss=ss+"*";
            }
            System.out.println(ss);

        }
    }

    /**
     * Writes a line of characters across the console
     * @param s - single character to display
     */
    public static void messageLine(String s)
    {
         String ss= s.repeat(messageLength);
         System.out.println(ss);
    }
    /**
     * Makes a thumbnail name replacing slashes and colon with underscores - as we need to create a valid file name
     * @param f - File to create thumbnail name for
     * @return - returns a string
     */
    public static String makeThumbName(File f) {
        try {
            return f.getCanonicalPath().replace("\\", "_").replace(":", "_");
            //remove the root

            // replace any directory slashes with underscores

        } catch (Exception e) {
            message("Could not determine the Thumbname for " +f.getPath());
            return f.getName();
        }

    }

    public static Boolean isExcluded(String fdir, String startDir, ArrayList<DirectoryObject> excludeDir) {
        for (DirectoryObject i : excludeDir) {
            if (fdir.equals(startDir + i.getName())) {
                message("Excluded:" + fdir);
                return true;
            }
        }
        //  message("Not Excluded:"+fname);
        return false;
    }

    public static Boolean isExcludedPrefix(String fname, ArrayList<DirectoryObject> excludePrefix) {
        for (DirectoryObject i : excludePrefix) {
            if (fname.indexOf(i.getName()) == 0) {
                message("Excluded Prefix:" + fname);
                return true;
            }
        }
        //  message("Not Excluded:"+fname);
        return false;
    }

    public static Boolean isVideo(String fname,String extensions) {
        String[] vids = extensions.toLowerCase().split("~", -1);
        String result = FilenameUtils.getExtension(fname).toLowerCase();
        for (int kk = 0; kk < vids.length; kk++) {
            if (result.equals(vids[kk])) {
                return true;
            }
        }
        return false;
    }

    public static Boolean isImage(String fname,String extensions) {
        String[] pics = extensions.toLowerCase().split("~", -1);
        String result = FilenameUtils.getExtension(fname).toLowerCase();
        for (int kk = 0; kk < pics.length; kk++) {
            if (result.equals(pics[kk])) {
                return true;
            }
        }
        return false;
    }

    private static Date getFileDate(File file) {
        Date d;
        try {


            BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            try {
                d = new Date(attr.lastModifiedTime().toMillis());
                message("lastModifiedTime: " + attr.lastModifiedTime());
            } catch (Exception e) {
                try {
                    d = new Date(attr.creationTime().toMillis());
                    message("creationTime: " + attr.creationTime());
                    message("lastAccessTime: " + attr.lastAccessTime());
                } catch (Exception ee) {
                    d = new Date();
                }
            }

        } catch (Exception e) {
            message("Using current date");
            d = new Date();
        }

        return d;
    }
    public static Date getDateWithoutTimeUsingCalendar(Date d) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
    private static IPTCApplicationTag getTagFromName(String tagName) {

        for (IPTCApplicationTag e : IPTCApplicationTag.values()) {
            if (tagName.equals(e.getName())) return e;
        }
        return null;


    }
    private static ExifTag getExifTagFromName(String tagName) {

        for (ExifTag e : ExifTag.values()) {
            if (tagName.equals(e.getName())) return e;
        }
        return null;


    }

    private static String getTagValueString(final JpegImageMetadata jpegMetadata,
                                            final TagInfo tagInfo) {
        final TiffField field = jpegMetadata.findEXIFValueWithExactMatch(tagInfo);
        if (field == null) {
            return "";
        } else {
            return field.getValueDescription().replace(", ", "");
        }
    }
    private static double getTagValueDouble(final JpegImageMetadata jpegMetadata,
                                            final TagInfo tagInfo) {
        final TiffField field = jpegMetadata.findEXIFValueWithExactMatch(tagInfo);
        if (field == null) {
            return 0.0d;
        } else {
            try {
                return field.getDoubleValue();
            }
            catch(Exception e)
            {
                return 0.0d;
            }
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
                SimpleDateFormat inputFormatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                return inputFormatter.parse(field.getValueDescription().replace("'", ""));

            } catch (Exception e) {
                try {
                    SimpleDateFormat inputFormatter = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.ENGLISH);
                    return inputFormatter.parse(field.getValueDescription().replace("'", ""));

                } catch (Exception ee) {
                    message("******* error converting date:" + field.getValueDescription() + e);
                    return null;
                }

            }
        }
    }

    private static void printTagValue(final JpegImageMetadata jpegMetadata,
                                      final TagInfo tagInfo) {
        final TiffField field = jpegMetadata.findEXIFValueWithExactMatch(tagInfo);
        if (field == null) {
            message(tagInfo.name + ": " + "Not Found.");
        } else {
            message(tagInfo.name + ": "
                    + field.getValueDescription());
        }
    }

    public static Boolean updateMetadata(File file, String thumbName,ConfigObject config, DriveObject drive) {
        String make = "";
        String model = "";
        String programName = "";
        double fStop =0.0d;

        String dimensions = "";
        Date bestDate = null;
        Date exifDigitisedDate = null;
        Date exifOriginalDate = null;
        Date tiffDate = null;
        double longitude = 0.0d;
        double latitude = 0.0d;
        double altitude=0.0d;
        boolean geoFound = false;
        Date lastModifiedDate = null;
        Date createdDate = null;
        Date lastAccessDate = null;

        try {
            BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            lastModifiedDate = new Date(attr.lastModifiedTime().toMillis());

            createdDate = new Date(attr.creationTime().toMillis());
            message("System Creation Date/Time: " + createdDate);
            lastAccessDate = new Date(attr.lastAccessTime().toMillis());
        } catch (Exception e) {
            message("Could not read File Basic Attributes: " + e);
        }
        List<IPTCDataSet> iptcs = new ArrayList<>();
        // Create a TIFF EXIF wrapper
        IPTC iptc = new IPTC();
        JpegExif exif = new JpegExif();
        List<String> existingComments = new ArrayList<String>();
        try {
            Map<MetadataType, Metadata> metadataMap = Metadata.readMetadata(file.getPath());
            for (Map.Entry<MetadataType, Metadata> entry : metadataMap.entrySet()) {
                Metadata meta = entry.getValue();
                if (meta instanceof XMP) {
                    XMP.showXMP((XMP) meta);

                } else if (meta instanceof Exif) {
                    exif=(JpegExif)meta;
                } else if (meta instanceof Comments) {
                    existingComments = ((Comments) meta).getComments();

                } else if (meta instanceof IPTC) {
                    String city = "";
                    String country = "";
                    String country_code = "";
                    String subLocation = "";
                    String stateProvince = "";
                    boolean overwrite = false;

                    iptc= (IPTC)meta;
                    Iterator<MetadataEntry> iterator = meta.iterator();
                    while (iterator.hasNext()) {
                        MetadataEntry item = iterator.next();
                        if (item.getKey().equals(IPTCApplicationTag.CITY.getName())) {
                            if (item.getValue().length() > 0 && !overwrite) {
                                city = item.getValue();
                            }
                            message("City currently is:" + item.getValue());
                        } else if (item.getKey().equals(IPTCApplicationTag.COUNTRY_CODE.getName())) {
                            if (item.getValue().length() > 0 && !overwrite) {
                                country_code = item.getValue();
                            }
                        } else if (item.getKey().equals(IPTCApplicationTag.COUNTRY_NAME.getName())) {
                            if (item.getValue().length() > 0 && !overwrite) {
                                country_code = item.getValue();
                            }
                        } else if (item.getKey().equals(IPTCApplicationTag.SUB_LOCATION.getName())) {
                            if (item.getValue().length() > 0 && !overwrite) {
                                subLocation = item.getValue();
                            }
                        } else if (item.getKey().equals(IPTCApplicationTag.PROVINCE_STATE.getName())) {
                            if (item.getValue().length() > 0 && !overwrite) {
                                stateProvince = item.getValue();
                            }
                        } else {
                          /*  try {
                                iptcs.add(new IPTCDataSet(getTagFromName(item.getKey()), item.getValue()));
                            } catch (Exception e) {
                                message("Error copying ITPC data:" + item.getKey() + item.getValue() + e);
                            }

                           */

                        }

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

                } else {
                   // we may want to look at other metadata
                }
            }


        } catch (Exception e) {
            message("error reading metadata" + e);
        }


        try {
            final ImageMetadata metadata = Imaging.getMetadata(file);

            // message(metadata);

            if (metadata instanceof JpegImageMetadata) {
                final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;

                // Jpeg EXIF metadata is stored in a TIFF-based directory structure
                // and is identified with TIFF tags.
                // Here we look for the "x resolution" tag, but
                // we could just as easily search for any other tag.
                //
                // see the TiffConstants file for a list of TIFF tags.



                // print out various interesting EXIF tags.
                make = getStringOrUnknown(jpegMetadata, TiffTagConstants.TIFF_TAG_MAKE);
                model = getStringOrUnknown(jpegMetadata, TiffTagConstants.TIFF_TAG_MODEL);
                fStop = getTagValueDouble(jpegMetadata, ExifTagConstants.EXIF_TAG_FNUMBER);
                programName = getStringOrUnknown(jpegMetadata, ExifTagConstants.EXIF_TAG_SOFTWARE);

              //  dimensions = getStringOrUnknown(jpegMetadata, TiffTagConstants.TIFF_TAG_MODEL);
                exifOriginalDate =getTagValueDate(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
                exifDigitisedDate=getTagValueDate(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED);
                tiffDate =    getTagValueDate(jpegMetadata, TiffTagConstants.TIFF_TAG_DATE_TIME);

                bestDate =exifOriginalDate;
                if (bestDate == null) {
                    bestDate = exifDigitisedDate;
                }
                if (bestDate == null) {
                    bestDate =tiffDate ;
                }
                // simple interface to GPS data
                altitude= getTagValueDouble(jpegMetadata,GpsTagConstants.GPS_TAG_GPS_ALTITUDE);
                final TiffImageMetadata exifMetadata = jpegMetadata.getExif();
                if (null != exifMetadata) {

                    final TiffImageMetadata.GPSInfo gpsInfo = exifMetadata.getGPS();
                    if (null != gpsInfo) {
                        geoFound = true;
                        countGEOCODED++;
                        countDriveGEOCODED++;
                        final String gpsDescription = gpsInfo.toString();
                        longitude = gpsInfo.getLongitudeAsDegreesEast();
                        latitude = gpsInfo.getLatitudeAsDegreesNorth();
                        message("    "
                                + "GPS Longitude (Degrees East): " + longitude);
                        message("    "
                                + "GPS Latitude (Degrees North): " + latitude);
                    }
                }
            }

        } catch (Exception e) {

            message("Error reading metadata:"+e);

        }
        if (bestDate == null) {
            bestDate = getFileDate(file);
        }
        Integer cameraKey = addCamera(make, model, bestDate);
        FileObject fNew = new FileObject();
        fNew.setCameraKey(cameraKey);
        fNew.setCameraMaker(make);
        fNew.setCameraModel(model);
        if(cameras.get(cameraKey-1).getFriendlyname()!=null)
        {
            fNew.setCameraName(cameras.get(cameraKey-1).getFriendlyname());
        }
        fNew.setFileCreated(bestDate);
        fNew.setFileModified(lastModifiedDate);
        fNew.setFileAccessed(lastAccessDate);
        fNew.setFileName(file.getName());
        fNew.setFileSize(new BigDecimal(file.length()));
        fNew.setDirectory(FilenameUtils.getFullPath(file.getPath()));
        fNew.setFileKey(count);
        fNew.setBestDate(bestDate);
        fNew.setExifDigitised(exifDigitisedDate);
        fNew.setExifOriginal(exifOriginalDate);
        fNew.setTiffDate(tiffDate);
        fNew.setAltitude(altitude);
        fNew.setProgramName(programName);
        if(thumbName!=null)
        {
            fNew.setThumbnail(thumbName);
        }
        fNew.setFStop(fStop);
        try {
            BufferedImage bimg = ImageIO.read(file);
            fNew.setWidth(bimg.getWidth());
            fNew.setHeight(bimg.getHeight());
        }
        catch(Exception e)
        {
            message("Could not read width and height for "+file.getName());
        }
        //
        if (geoFound) {
            fNew.setLatitude(latitude);
            fNew.setLongitude(longitude);
            ReverseGeocodeObject g = checkCachedGeo(latitude, longitude,bestDate);
            if (g == null) {
                g = OpenMaps.reverseGeocode(String.valueOf(latitude), String.valueOf(longitude), config);

                if (g != null) {
                    message("Adding a new Place:["+(geoObjects.size() + 1) +"]" + g.getDisplay_name());
                    g.setInternalKey(geoObjects.size() + 1);
                    g.setEndDate(bestDate);
                    g.setStartDate(bestDate);
                    geoObjects.add(g);
                } else {
                    message("Could not geocode :Lat" + latitude + ", Long:" + longitude);
                }
            }
            else
            {
                 message("Found Lat / Long in cache : ["+g.getInternalKey()+"]"+g.getDisplay_name());
            }
            if (g != null) {
                // this links 
                fNew.setPlaceKey(g.getInternalKey());
                fNew.setDisplayName(g.getDisplay_name());
                fNew.setCity(g.getIPTCCity());
                fNew.setCountry_code(g.getIPTCCountryCode());
                fNew.setCountry_name(g.getIPTCCountry());
                fNew.setStateProvince(g.getIPTCStateProvince());
                fNew.setSubLocation(g.getIPTCSublocation());

                //
                iptcs.add(new IPTCDataSet(IPTCApplicationTag.CITY, fNew.getCity()));
                iptcs.add(new IPTCDataSet(IPTCApplicationTag.COUNTRY_CODE, fNew.getCountry_code()));
                iptcs.add(new IPTCDataSet(IPTCApplicationTag.COUNTRY_NAME, fNew.getCountry_name()));
                iptcs.add(new IPTCDataSet(IPTCApplicationTag.SUB_LOCATION, fNew.getSubLocation()));
                iptcs.add(new IPTCDataSet(IPTCApplicationTag.PROVINCE_STATE, fNew.getStateProvince()));
            }
        } else {
            fNew.setDisplayName("location not found");
        }

        //     List<IPTCDataSet> iptcs =  new ArrayList<IPTCDataSet>();
        iptcs.add(new IPTCDataSet(IPTCApplicationTag.COPYRIGHT_NOTICE, drive.getIPTCCopyright()));
        iptcs.add(new IPTCDataSet(IPTCApplicationTag.CATEGORY, drive.getIPTCCategory()));
        iptcs.add(new IPTCDataSet(IPTCApplicationTag.KEY_WORDS, drive.getIPTCKeywords()));

        if(config.getUpdate()) {
            try {
                FileInputStream fin = new FileInputStream(file.getPath());
                String fout_name = FilenameUtils.getFullPath(file.getPath()) + "out" + FilenameUtils.getName(file.getPath());
                File outFile = new File(fout_name);
                FileOutputStream fout = new FileOutputStream(outFile, false);
                List<Metadata> metaList = new ArrayList<>();


                metaList.add(populateExif(exif));

                iptc.addDataSets(iptcs);
                metaList.add(iptc);
                //  metaList.add(new Comments(Arrays.asList("comment 1","Comment2")));
                Metadata.insertMetadata(metaList, fin, fout);
                fin.close();
                fout.close();
                if(!file.delete())
                {
                    message("Cannot delete file:" + file.getPath());
                }
                Files.copy(outFile.toPath(), file.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
                if(!outFile.delete())
                {
                    message("Cannot delete file:" + outFile.getPath());
                }
                Files.setAttribute(file.toPath(), "creationTime", FileTime.fromMillis(createdDate.getTime()));
                Files.setAttribute(file.toPath(), "lastAccessTime", FileTime.fromMillis(lastAccessDate.getTime()));
                Files.setAttribute(file.toPath(), "lastModifiedTime", FileTime.fromMillis(lastModifiedDate.getTime()));
            } catch (Exception e) {
                message("Cannot write metadata:" + e);
            }
        }
        fileObjects.add(fNew);
        return true;
    }
    public static String getStringOrUnknown(final JpegImageMetadata jpegMetadata, final TagInfo tagInfo)
    {
        String v=getTagValueString(jpegMetadata, tagInfo);
        if (v.length() > 0) {
            return v.replaceAll("'","");
        } else {
            return "Unknown";
        }


    }
    public static Boolean readMetadata(File file) {


        try {

            Map<MetadataType, Metadata> metadataMap = Metadata.readMetadata(file.getPath());
            for (Map.Entry<MetadataType, Metadata> entry : metadataMap.entrySet()) {
                Metadata meta = entry.getValue();
                if (meta instanceof XMP) {
                    XMP.showXMP((XMP) meta);

                } else if (meta instanceof Exif) {
                    JpegExif exif=(JpegExif)meta;

                    message("Windows Author:"+exif.getAsString(ExifTag.WINDOWS_XP_AUTHOR));
                    message("Windows Comment:"+exif.getAsString(ExifTag.WINDOWS_XP_COMMENT));
                    message("Windows Keywords:"+exif.getAsString(ExifTag.WINDOWS_XP_KEYWORDS));
                    message("Windows Title:"+exif.getAsString(ExifTag.WINDOWS_XP_TITLE));
                    message("Windows Subject:"+exif.getAsString(ExifTag.WINDOWS_XP_SUBJECT));
                    message("User Comment:"+exif.getAsString(ExifTag.USER_COMMENT));
                    message("Subject Location:"+exif.getAsString(ExifTag.SUBJECT_LOCATION));

                } else if (meta instanceof IPTC) {
                    for (MetadataEntry item : meta) {
                        message("IPTC iterator:" + item.getKey() + item.getValue());
                    }
                } else {
                    for (MetadataEntry item : entry.getValue()) {
                        printMetadata(item, "", "     ");
                    }
                }
            }


        } catch (Exception e) {
            message("error reading metadata" + e);
        }


        try {
            final ImageMetadata metadata = Imaging.getMetadata(file);

            // message(metadata);

            if (metadata instanceof JpegImageMetadata) {
                final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;

                // Jpeg EXIF metadata is stored in a TIFF-based directory structure
                // and is identified with TIFF tags.
                // Here we look for the "x resolution" tag, but
                // we could just as easily search for any other tag.
                //
                // see the TiffConstants file for a list of TIFF tags.

                message("file: " + file.getPath());
                printTagValue(jpegMetadata,  TiffTagConstants.TIFF_TAG_IMAGE_DESCRIPTION);
                printTagValue(jpegMetadata,  TiffTagConstants.TIFF_TAG_DOCUMENT_NAME);
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

                // simple interface to GPS data
                final TiffImageMetadata exifMetadata = jpegMetadata.getExif();
                if (null != exifMetadata) {
                    final TiffImageMetadata.GPSInfo gpsInfo = exifMetadata.getGPS();
                    if (null != gpsInfo) {

                        countGEOCODED++;
                        final String gpsDescription = gpsInfo.toString();


                        message("    " + "GPS Description: "
                                + gpsDescription);

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
                    final RationalNumber[] gpsLatitude = (RationalNumber[]) (gpsLatitudeField.getValue());
                    final String gpsLongitudeRef = (String) gpsLongitudeRefField.getValue();
                    final RationalNumber[] gpsLongitude = (RationalNumber[]) gpsLongitudeField.getValue();

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

                    message("    " + "GPS Latitude: "
                            + gpsLatitudeDegrees.toDisplayString() + " degrees, "
                            + gpsLatitudeMinutes.toDisplayString() + " minutes, "
                            + gpsLatitudeSeconds.toDisplayString() + " seconds "
                            + gpsLatitudeRef);
                    message("    " + "GPS Longitude: "
                            + gpsLongitudeDegrees.toDisplayString() + " degrees, "
                            + gpsLongitudeMinutes.toDisplayString() + " minutes, "
                            + gpsLongitudeSeconds.toDisplayString() + " seconds "
                            + gpsLongitudeRef);

                }



                final List<ImageMetadata.ImageMetadataItem> items = jpegMetadata.getItems();

                for (final ImageMetadata.ImageMetadataItem item : items) {
                    message("    " + "item: " + item);
                }


            }

        } catch (Exception e) {

            message("Error accessing metadata:"+e);

        }



        return true;


    }
    // This method is for testing only
    private static Exif populateExif(Exif exif)  {

        exif.addExifField(ExifTag.WINDOWS_XP_AUTHOR, FieldType.WINDOWSXP, "Author");
        exif.addExifField(ExifTag.WINDOWS_XP_KEYWORDS, FieldType.WINDOWSXP, "Exif Copyright:  Ted Carroll ");

        DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");

        // Insert ThumbNailIFD
        // Since we don't provide thumbnail image, it will be created later from the input stream
        exif.setThumbnailRequired(true);

        return exif;
    }

    private static void printMetadata(MetadataEntry entry, String indent, String increment) {
        message(indent + entry.getKey() + (StringUtils.isNullOrEmpty(entry.getValue()) ? "" : ": " + entry.getValue()));
        if (entry.isMetadataEntryGroup()) {
            indent += increment;
            Collection<MetadataEntry> entries = entry.getMetadataEntries();
            for (MetadataEntry e : entries) {
                printMetadata(e, indent, increment);
            }
        }
    }


}
