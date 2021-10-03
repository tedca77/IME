package IC;
import IC.openmaps.OpenMaps;
import IC.openmaps.Place;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icafe4j.image.meta.Metadata;
import com.icafe4j.image.meta.MetadataEntry;
import com.icafe4j.image.meta.MetadataType;
import com.icafe4j.image.meta.adobe.IRB;
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
import java.util.concurrent.TimeUnit;

import static IC.ImageProcessing.createThumbFromPicture;

/**
 * Main class
 */
public class ImageCatalogue {
    static int count = 0;
    static int countTooSmall = 0;
    static int countFromMake = 0;
    static int countGEOCODED = 0;
    static int countErrors = 0;

    static int countDrive = 0;
    static int countDriveTooSmall = 0;
    static int countDriveGEOCODED = 0;
    static ArrayList<CameraObject> cameras = new ArrayList<>();
    static ArrayList<FileObject> fileObjects = new ArrayList<>();
    static ArrayList<FileObject> duplicateObjects = new ArrayList<>();
    static ArrayList<Place> places = new ArrayList<>();
    static ArrayList<TrackObject> tracks = new ArrayList<>();
    // Default Variables - that can be modified..
    static int messageLength=160; //length of Console Message
    static String videoDefaults="mp4~mp4a";
    static String imageDefaults="jpg~jpeg~bmp";
    static String jsonDefault="config.json";
    static Long minFileSizeDefault=4000L;
    static String thumbSizeDefault="240x180";
    static ArrayList<String> isocountryDefault=new ArrayList<>(Collections.singletonList("country_code"));
    static ArrayList<String> countryDefault=new ArrayList<>(Collections.singletonList("country"));
    static ArrayList<String> stateprovinceDefault=new ArrayList<>(Arrays.asList("county","state_district"));
    static ArrayList<String> cityDefault=new ArrayList<>(Arrays.asList("town","city","village"));
    static ArrayList<String> sublocationDefault=new ArrayList<>(Arrays.asList("amenity","house_number","road","hamlet","suburb"));
    static int cacheDistanceDefault=100; // this is the distance from a point that detemrines this is the same place..Metres.
    static int pauseSecondsDefault=2; // this is the pause before a geocode (to not overload the server...
    //
    static Date startTime;
    static Date endTime;
    /**
     * Main Method for the program - arguments passes as Java arguments..
     * @param args - either single json file or two files, first one is root directory and second is output file, followed by parameters
     */
    public static void main(String[] args) {
        startTime= new Date();
        ConfigObject config=null;
        Path fileName=null;
        try {
            if(FilenameUtils.getExtension(args[0]).equalsIgnoreCase("json"))
            {
                fileName = Path.of(args[0]);
                config = readConfig(args[0]);
                if(config==null)
                {
                    System.exit(0);
                }
            }
            else
            {
                message("No json file present - requiring two directories");
                if(args.length>1) {
                    config = new ConfigObject();
                    setDrives(config, args[0], args[1]);
                    fileName=Path.of(args[1]+"\\"+jsonDefault);
                    message("Directory to search:"+fileName);
                    message("Temporary Directory:"+args[1]);
                }
                else
                {
                    message("Please provide at least two arguments, for the Root directory for searching and the temporary output directory");
                }
            }
            //
            setDefaults(Objects.requireNonNull(config), args);
            messageLine("*");
            //this prints out the main options
            displayDefaults(config);
            // set the ArrayLists with values from the config file
            readConfigLists(config);
            createTempDirForUser(config.getTempdir());
            readDrives(config);
            try {
                fileObjects.sort(Comparator.comparing(FileObject::getFileName));
            }
            catch(Exception e){
                message("eerror"+e);
            }

            checkDuplicates();
            // sort any ArrayLists....
            fileObjects.sort(Comparator.comparing(FileObject::getBestDate));
            addLinksToPlaces(config.getWidth(),config.getTempdir());
            //create tracks - this will also update the geoObjects
            if(!createTracks())
            {
                message("Failed to create tracks");
            }
            addLinksToTracks(config.getWidth(),config.getTempdir());
            //sets config object with new values
            config.setCameras(cameras);
            config.setPhotos(fileObjects);
            config.setPlaces(places);
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
            // print out total number of photos if more than one drive...
            if(config.getDrives().size()>1) {
                messageLine("*");
                message("All Drives -" + "Photos found:" + count);
                message("All Drives - " + "Photos too small:" + countTooSmall);
                message("All Drives - " + "Photos geocoded on drive:" + countGEOCODED);
                message("All Drives - " + "Errors in processing:" + countErrors);
            }

        } catch (Exception e) {
            message("Error reading json file " + e);
        }
        endTime= new Date();
        long duration=(endTime.getTime()-startTime.getTime());
        String durationString= String.format("%02d hrs, %02d min, %02d sec",
               TimeUnit.MILLISECONDS.toHours(duration),
               TimeUnit.MILLISECONDS.toMinutes(duration),
               TimeUnit.MILLISECONDS.toSeconds(duration) -
               TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
        message("End Time:" + endTime);
        message("Duration:" + durationString);

    }

    /**
     * pauses operation to prevent swamping geocoder
     * @param dur - duration of wait , in seconds
     */
    public static void waitForGeo(int dur)
    {
        try {
            long start = System.currentTimeMillis();
            Thread.sleep(dur*1000L);
            message("Sleep time in ms = " + (System.currentTimeMillis() - start));
        }
        catch(Exception e)
        {
              message("Error pausing"+e);
        }
    }
    /**
     * Reads a JSON Config File - this holds all run parameters
     * @param configFile - filenamee
     * @return - config object
     */
    public static ConfigObject readConfig(String configFile)
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm ss");
        mapper.setDateFormat(df);
        String result;
        try {
            Path fileName = Path.of(configFile);
            result = Files.readString(fileName);
        }
        catch(Exception e)
        {
            message("Failed to open JSON config file - check file exists:"+configFile + " or is being edited"+e);
            return null;
        }
        try {
            return mapper.readValue(result, ConfigObject.class);
        }
        catch(Exception e)
        {
            message("Error in JSON file:"+e);
            return null;
        }
    }
    /**
     * Sets the defaults if the values are not in the JSON (note this is pass by value - and we can change values)
     * @param config - passes ConfigObject
     */
    public static void setDefaults(ConfigObject config,String[] args)
    {
        if(config.getMinfilesize()==null){config.setMinfilesize(minFileSizeDefault);}
        if(config.getPauseSeconds()==null){config.setPauseSeconds(pauseSecondsDefault);}
        if(config.getPauseSeconds()<1){config.setPauseSeconds(pauseSecondsDefault);}
        if(config.getThumbsize()==null){config.setThumbsize(thumbSizeDefault);}
        // sets the defaults
        if(config.getUpdate()==null){config.setUpdate(false);}
        if(config.getShowmetadata()==null) {config.setShowmetadata(false);}
        if(config.getOverwrite()==null) {config.setOverwrite(false);}
        if(config.getRedoGeocode()==null) {config.setRedoGeocode(false);}
        //checks args
        for(String s:args)
        {
            if(s.trim().equalsIgnoreCase(Enums.argOptions.update.toString()))
            {
                config.setUpdate(true);
            }
            if(s.trim().equalsIgnoreCase(Enums.argOptions.overwriteValues.toString()))
            {
                config.setOverwrite(true);
            }
            if(s.trim().equalsIgnoreCase(Enums.argOptions.showmetadata.toString()))
            {
                config.setShowmetadata(true);
            }
            if(s.trim().equalsIgnoreCase(Enums.argOptions.redoGeocoding.toString()))
            {
                config.setRedoGeocode(true);
            }
        }
        if(config.getImageextensions()==null) {config.setImageextensions(imageDefaults);}
        if(config.getVideoextensions()==null) {config.setVideoextensions(videoDefaults);}
        if(config.getSublocation()==null) {config.setSublocation(sublocationDefault);}
        if(config.getCity()==null) {config.setCity(cityDefault);}
        if(config.getCountry()==null) {config.setCountry(countryDefault);}
        if(config.getIsocountrycode()==null) {config.setIsocountrycode(isocountryDefault);}
        if(config.getStateprovince()==null) {config.setStateprovince(stateprovinceDefault);}
        if (config.getCacheDistance() == null) {
            config.setCacheDistance(cacheDistanceDefault);
        }
        //return config;
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
            proot.put( "places", places);
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
            //
            Template tkmltemplate = cfg.getTemplate("trackkml.ftl");
            FileWriter tkmlwriter = new FileWriter(tempDir+"/"+"track.kml");
            Map<String, Object> tkmlroot = new HashMap<>();
            tkmlroot.put( "tracks",tracks );
            tkmltemplate.process(tkmlroot, tkmlwriter);
            tkmlwriter.close();
            //
            Template pkmltemplate = cfg.getTemplate("pointkml.ftl");
            FileWriter pkmlwriter = new FileWriter(tempDir+"/"+"point.kml");
            Map<String, Object> pkmlroot = new HashMap<>();
            pkmlroot.put( "places", places);
            pkmltemplate.process(pkmlroot, pkmlwriter);
            pkmlwriter.close();
            //
            Template ftemplate = cfg.getTemplate("photosbydate.ftl");
            FileWriter fwriter = new FileWriter(tempDir+"/"+"photosbydate.html");
            Map<String, Object> froot = new HashMap<>();
            froot.put( "photos",fileObjects );
            froot.put("root",tempDir);
            ftemplate.process(froot, fwriter);
            twriter.close();
//
            Template dtemplate = cfg.getTemplate("duplicates.ftl");
            FileWriter dwriter = new FileWriter(tempDir+"/"+"duplicates.html");
            Map<String, Object> droot = new HashMap<>();
            droot.put( "photos",duplicateObjects );
            droot.put("root",tempDir);
            dtemplate.process(droot, dwriter);
            dwriter.close();
        } catch (IOException e) {
            message("Cannot write output files to directory - please check the path exists:"+tempDir);
        }
        catch(Exception ee)
        {
            message("Error writing output files :"+ee);
        }
        return true;
    }
    /**
     *  Displays the options have been selected on the console output
     * @param c - Configuration Object
     */
    public static void displayDefaults(ConfigObject c)
    {
        if(c.getUpdate())
        {
            message("FILES WILL BE UPDATED");
            if (c.getShowmetadata()) {
                message("METADATA WILL BE SHOWN BEFORE AND AFTER UPDATING");
            }
        }
        else {
            if (c.getShowmetadata()) {
                message("METADATA WILL BE SHOWN");
            }
        }
        if(c.getRedoGeocode()) {
            message("EXISTING GEOCODING WILL BE REPLACED");
        }
        if(c.getOverwrite()) {
            message("EXISTING METADATA WILL BE OVERWRITTEN");
        }
        else
        {
            if(c.getRedoGeocode()) {
                message("ONLY GEOCODE METADATA WILL BE OVERWRITTEN ");
            }
            else
            {
                message("EXISTING METADATA WILL NOT BE OVERWRITTEN");
            }
        }
        message("Fields for Sub Location: "+c.getSublocation().toString());
        message("Fields for State/Province: "+c.getStateprovince().toString());
        message("Fields for City: "+c.getCity().toString());
        message("Fields for Country Name: "+c.getCountry().toString());
        message("Fields for Country Code: "+c.getIsocountrycode().toString());
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
    /**
     * Runs Console reports at the end of the program run
     */
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
        for (Place g : places) {
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
                                    if (fileSize > config.getMinfilesize()) {
                                        count++;
                                        countDrive++;
                                        messageLine("-");
                                        message("File number:["+count + "], file:" + file.getCanonicalPath());
                                        message("File size is:" + fileSize +" ( bytes)");
                                        if(config.getShowmetadata()) {
                                            if (!readMetadata(file)) {
                                                message("Could not read metadata before update");
                                            }
                                            messageLine("~");
                                        }
                                        String thumbName = makeThumbName(file);
                                        if(!processFile(file, thumbName,config, drive))
                                        {
                                            countErrors++;
                                            message("Could not process file:"+file.getCanonicalPath());
                                        }
                                        if(config.getShowmetadata() && config.getUpdate()) {
                                            messageLine("~");
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
                        message("Error reading files:" + file.getCanonicalPath()+"Error:"+ee);
                    }
                }
            }
        } catch (Exception e) {
            message("Error reading directory:" + drive.getStartdir()+", Error:"+e);
        }
    }
    /**
     * THis adds one or more geocoding fields to a string which is then used to update the metadata fields used
     * @param g - object retrieved from OpenMaps API
     * @param fieldnames - a list of field names we are interested in
     * @return - String to use for metadata field (it is never NULL)
     */
    public static String assembleLocation(Place g, ArrayList<String> fieldnames) {
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
            if (f.equals("suburb")) {
                s = addIfNotNull(s, g.getAddress().getSuburb());
            }
            if (f.equals("amenity")) {
                s = addIfNotNull(s, g.getAddress().getAmenity());
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
    /**
     * this adds some HTML for images to the Place object so we can incorporate into Freemarker report easily
     * @param width - width of image
     * @param root - directory where images will be
     */
    public static void addLinksToPlaces(Integer width, String root)
    {
        for(Place r : places)
        {
            StringBuilder s= new StringBuilder();
            for(FileObject f: fileObjects)
            {
                try {
                    if(f.getPlaceKey()!=null) {
                        if (f.getPlaceKey().equals(r.getInternalKey())) {
                            if (!(f.getOrientation() == 8 || f.getOrientation() == 6)) {
                                s.append("<img src=\"").append(root).append("\\").append(f.getThumbnail()).append("\" width=\"").append(width).append("\" class=\"padding\" >");
                            } else {
                                Integer newWidth = width * f.getHeight() / f.getWidth();
                                s.append("<img src=\"").append(root).append("\\").append(f.getThumbnail()).append("\" width=\"").append(newWidth).append("\" class=\"padding\" >");
                            }
                        }
                    }
                }
                catch(Exception e)
                {
                    message("error adding links to Places:"+f.getDisplayName()+e);
                }
            }
            r.setImagelinks(s.toString());
        }
    }
    /**
     * this adds some HTML for images to the Track object so we can incorporate into Freemarker report easily
     * @param width - width of image
     * @param root - directory where images will be
     */
    public static void addLinksToTracks(Integer width, String root)
    {

        // for each track, get all the photos for the day, as some may not have location...and
        // they could be slotted in, in sequence (i.e. date order)
        for(TrackObject t : tracks) {
            StringBuilder s = new StringBuilder();
                    for (FileObject f : fileObjects) {
                        try {
                            if (getDateWithoutTimeUsingCalendar(f.getBestDate()).equals(t.getTrackDate())) {
                                if(!(f.getOrientation()==8 || f.getOrientation()==6)) {
                                    s.append("<img src=\"").append(root).append("\\").append(f.getThumbnail()).append("\" width=\"").append(width).append("\" class=\"padding\" >");
                                }
                                else
                                {
                                    Integer newWidth=width*f.getHeight()/f.getWidth();
                                    s.append("<img src=\"").append(root).append("\\").append(f.getThumbnail()).append("\" width=\"").append(newWidth).append("\" class=\"padding\" >");
                                }
                            }
                        } catch (Exception e) {
                            message("error adding links to tracks:" + f.getDisplayName() + e);
                        }
            }
            t.setImagelinks(s.toString());
        }
    }
    /**
     * Adds tracks to the tracks arraylist
     * @param points - one or more geo points in an array list - referenced by internal key
     * @param startDate - start date time of track
     * @param endDate - end date time of track
     * @param trackDay - Date time, normalised to be 12 midnight
     * @param coordinates - a String containing all coordinates with CR between each value
     */
    public static void addTrack(ArrayList<Integer> points, Date startDate,Date endDate, Date trackDay,String coordinates)
    {
        // write out track
        if(points.size()>0) {
            TrackObject t = new TrackObject();
            t.setPoints(points);
            t.setTrackKey(tracks.size() + 1);
            t.setStartDate(startDate);
            t.setEndDate(endDate);
            t.setPlaceCount(points.size());
            t.setTrackDate(trackDay);
            t.setCoordinates(coordinates);
            t.setStartAndEndPlace(getStartAndEndString(t));
            tracks.add(t);
        }
    }

    /**
     * Adds a Place to the array with the correct next key
     * @param g - Place object (new)
     * @param bestDate - Date for PlaceObject
     * @return - key value
     */
    public static Integer addPlace(Place g,Date bestDate)
    {
        message("Adding a new Place:["+(places.size() + 1) +"]" + g.getDisplay_name());
        // there may be gaps in numbering, so add one to the highest in the sorted list
        int newKey;
        if( places.size()==0)
        {
            newKey=1;
        }
        else
        {
            newKey= places.get(places.size()-1).getInternalKey()+1;
        }
        g.setInternalKey(newKey);
        g.setEndDate(bestDate);
        g.setStartDate(bestDate);
        places.add(g);
        return newKey;
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
        // we have not found a camera, so add a new one to the ArrayList -
        // there may be gaps in numbering, so add one to the highest in the sorted list
        int newKey;
        if( cameras.size()==0)
        {
            newKey=1;
        }
        else
        {
            newKey=cameras.get(cameras.size()-1).getCamerakey()+1;
        }
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
    public static boolean checkIPTCComments( List<String> existingComments)
    {
        for(String s : existingComments)
        {
            if(s.contains("#geocoded:") && s.length()==29)
            {
                message("File has already been geocoded:"+s);
                return true;

            }
        }
        return false;
    }
    /**
     *  Checks for duplicate filenames and adds tot he duplicate list
     */
    public static void checkDuplicates()
    {
       FileObject lastObject = new FileObject();
        for (FileObject f : fileObjects) {
            try {
                if(lastObject.getFileName()!=null) {
                    if (lastObject.getFileName().equals(f.getFileName())) {
                        if (duplicateObjects.size() > 0) {
                            // we may need to include the previous lastObject but it may already be included e.g. if there are more than two duplicates
                            if (!duplicateObjects.get(duplicateObjects.size() - 1).equals(lastObject)) {
                                duplicateObjects.add(lastObject);
                            }
                        } else {
                            duplicateObjects.add(lastObject);
                        }
                        duplicateObjects.add(f);
                    }
                }
                lastObject = f;

            } catch (Exception e) {
                message("error checking duplicates:" + f.getDisplayName() + e);
            }
        }
    }
    /**
     * Checks whether we already have a geocode object - if we do, then we just update the start or end date
     * @param lat - latitude (of image)
     * @param lon - longitude (of image)
     * @param d - date  (of image)
     * @return - Place Object ..
     */
    public static Place checkCachedGeo(double lat, double lon, Date d, double checkDistance) {
        for (Place g : places) {
            double glat = Double.parseDouble(g.getLat());
            double glon = Double.parseDouble(g.getLon());
            double distance=distance_Between_LatLong(lat,lon,glat,glon);
            if ( distance< checkDistance) {
                message("Found cache entry: distance between points (metres)"+distance);
                g.setCountPlace(g.getCountPlace()+1);
                if(g.getStartDate()==null)
                {
                    g.setStartDate(d);
                }
                else {
                    if (d.before(g.getStartDate())) {
                        g.setStartDate(d);
                    }
                }
                if(g.getEndDate()==null)
                {
                    g.setEndDate(d);
                }
                else {
                    if (d.after(g.getEndDate())) {
                        g.setEndDate(d);
                    }
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
            if(!result)
            {
                message("Did not create new output directory - it may already exist:"+temp);
            }

        } catch (Exception e) {
            message("Error creating new output directory - it may already exist for: "+temp);
        }
    }
    /**
     * Creates all tracks from the FileObjects
     * @return - returns tracks ArrayList
     */
    public static Boolean createTracks()
    {
        tracks = new ArrayList<>();
        // Each Place can be part of one or more tracks...
        // if we already have a track for this date... do we redo it...
        Date lastDay=null;
        Integer placeKey=0;
        Date startDate=null;
        Date endDate=null;
        StringBuilder coordinates = new StringBuilder();
        ArrayList<Integer> points = new ArrayList<>();
        for(FileObject f : fileObjects)
        {
            //
            Date d= getDateWithoutTimeUsingCalendar(f.getBestDate());
            if(d.equals(lastDay))
            {
                if(f.getPlaceKey()!=null) {
                    if (!f.getPlaceKey().equals(placeKey)) {
                        points.add(f.getPlaceKey());
                        coordinates.append(f.getLongitude()).append(",").append(f.getLatitude()).append(",").append(f.getAltitude()).append(System.getProperty("line.separator"));
                        placeKey=f.getPlaceKey();
                    }
                }
                else
                {
                    // if place is null, we want to ensure that next place is identified
                    placeKey=-1;
                }
                endDate=f.getBestDate();
            }
            else
            {
                addTrack(points,startDate,endDate,lastDay,coordinates.toString());
                points = new ArrayList<>();
                coordinates = new StringBuilder();
                if(f.getPlaceKey()!=null) {
                    points.add(f.getPlaceKey());
                    coordinates.append(f.getLongitude()).append(",").append(f.getLatitude()).append(",").append(f.getAltitude()).append(System.getProperty("line.separator"));
                    placeKey=f.getPlaceKey();
                }
                else
                {
                    placeKey=-1;
                }
                startDate=f.getBestDate();
                endDate=f.getBestDate();
                lastDay=getDateWithoutTimeUsingCalendar(f.getBestDate());

            }
        }
        // write out last track
        addTrack(points,startDate,endDate,lastDay,coordinates.toString());
        return true;
    }
    /**
     * Writes out message to the console
     * @param messageString - message string
     */
    public static void message(String messageString)
    {
        String str=" ";

        String[] lines = messageString.split("\\R",-1);
        for(String l: lines) {
            if (l.length() > (messageLength - 4)) {
                System.out.println("* " + l.substring(0, messageLength - 4) + " *");
            } else {
                int width = messageLength - (l.length() + 4);
                String ss = "* " + l + " " + str.repeat(width);
                if (ss.length() < messageLength) {
                    ss = ss + "*";
                }
                System.out.println(ss);
            }
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
     * Reads any existing cameras, places or events if in the json file
     * @param c - Configuration Object
     */
    public static void readConfigLists(ConfigObject c)
    {
        if(c.getCameras()!=null)
        {
            message("NUMBER OF CAMERAS READ FROM CONFIG FILE:"+c.getCameras().size());
            cameras=c.getCameras();
            //sort in case there are gaps in numbering
            cameras.sort(Comparator.comparing(CameraObject::getCamerakey));
            for(CameraObject cc : cameras)
            {
                cc.setCameracount(0);
            }
        }
        if(c.getPlaces()!=null)
        {
            message("NUMBER OF PLACES READ FROM CONFIG FILE:"+c.getPlaces().size());
            places =c.getPlaces();
            //sort in case there are gaps in numbering
            places.sort(Comparator.comparing(Place::getInternalKey));
            for(Place g : places)
            {
                g.setCountPlace(0);
            }
        }
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
    /**
     * Checks if a file directory should be excluded from the processing
     * @param fdir - directory name to check
     * @param startDir - start directory for searching
     * @param excludeDir - array of directories to exclude
     * @return - either true (excluded) or false(not excluded)
     */
    public static Boolean isExcluded(String fdir, String startDir, ArrayList<DirectoryObject> excludeDir) {
        if(excludeDir==null)
        {
            return false;
        }
        for (DirectoryObject i : excludeDir) {
            if(i.getName()!=null) {
                if (fdir.equals(startDir + i.getName())) {
                    message("Excluded:" + fdir);
                    return true;
                }
            }
        }
        //  message("Not Excluded:"+fname);
        return false;
    }
    /**
     * Checks if a file name, based on a prefix,  should be excluded from the processing
     * @param fname - filename
     * @param excludePrefix - array of prefixes to exclude
     * @return - either true (exclude) or false (not excluded)
     */
    public static Boolean isExcludedPrefix(String fname, ArrayList<DirectoryObject> excludePrefix) {
        if(excludePrefix==null)
        {
            return false;
        }
        for (DirectoryObject i : excludePrefix) {
            if(i.getName()!=null) {
                if (fname.indexOf(i.getName()) == 0) {
                    message("Excluded Prefix:" + fname);
                    return true;
                }
            }
        }
        //  message("Not Excluded:"+fname);
        return false;
    }
    /**
     * Checks if a file is an image
     * @param fname - filename
     * @param extensions - list of valid extensions, separated by tilda (~)
     * @return - either true (image) or false
     */
    public static Boolean isImage(String fname,String extensions) {
        String[] pics = extensions.toLowerCase().split("~", -1);
        String result = FilenameUtils.getExtension(fname).toLowerCase();
        for (String pic : pics) {
            if (result.equals(pic)) {
                return true;
            }
        }
        return false;
    }
    /**
     * gets file date from file attributes
     * @param file - filename
     * @return - date
     */
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
    /**
     * converts date so it does not have hours, mins and seconds
     * @param d - date to convert
     * @return - modified date
     */
    public static Date getDateWithoutTimeUsingCalendar(Date d) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
    /**
     * Creates a string from the display names for start and end point
     * @param t - Track Object to create the string
     * @return - string to be used in display for the track
     */
    private static String getStartAndEndString(TrackObject t)
    {
        if(t.getPoints().size()==1)
        {
            try {
                return Objects.requireNonNull(getPlace(t.getPoints().get(0))).getDisplay_name();
            }
            catch(Exception e)
            {
                return "";
            }
        }
        else
        {
            try {
                return Objects.requireNonNull(getPlace(t.getPoints().get(0))).getDisplay_name() + " to " + Objects.requireNonNull(getPlace(t.getPoints().get(t.getPoints().size() - 1))).getDisplay_name();
            }
            catch(Exception e)
            {
                return "";
            }
        }
    }
    /**
     * Finds geoObject from its internal key
     * @param i - key value
     * @return - Place Object
     */
    private static Place getPlace(Integer i)
    {
        for(Place r : places)
        {
            if(r.getInternalKey().equals(i))
            {
                return r;
            }
        }
        message("Error -could not retrieve Place with a key of:"+i);
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
    private static Integer getTagValueInteger(final JpegImageMetadata jpegMetadata,
                                            final TagInfo tagInfo) {
        final TiffField field = jpegMetadata.findEXIFValueWithExactMatch(tagInfo);
        if (field == null) {
            return null;
        } else {
            try {
                return field.getIntValue();
            }
            catch(Exception e)
            {
                return 1;
            }
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
    /**
     * Creates a FileObject and Updates the file metadata (or displays information only, depending on options chosen)
     * @param file - File to process
     * @param thumbName - name of theumbnail
     * @param config - Configuration Object
     * @param drive - Drive name
     * @return - returns true if successful, false if failure
     */
    public static Boolean processFile(File file, String thumbName,ConfigObject config, DriveObject drive) {
        FileObject existingMetadata = new FileObject();
        FileObject fNew = new FileObject();
        Place g;
        boolean alreadyGeocoded = false;
        readSystemDates(existingMetadata,file);
        IPTC iptc = new IPTC();
        List<String> existingComments;
        JpegExif exif = new JpegExif();
        ArrayList<String> existingCommentsString=new ArrayList<>();
        // reads existing values from metadata
        try {
            Map<MetadataType, Metadata> metadataMap = Metadata.readMetadata(file.getPath());
            for (Map.Entry<MetadataType, Metadata> entry : metadataMap.entrySet()) {
                Metadata meta = entry.getValue();
                if (meta instanceof XMP) {
                    XMP.showXMP((XMP) meta);
                } else if (meta instanceof Exif) {
                    exif=(JpegExif)meta;
                } else if (meta instanceof Comments) {
                    existingComments = (((Comments) meta).getComments());
                    alreadyGeocoded=checkIPTCComments(existingComments);
                } else if (meta instanceof IPTC) {
                    iptc = (IPTC) meta;
                    if(!readIPTCdata(existingMetadata,meta)){message("Cannot read IPTC data");}
                }
            }
        } catch (Exception e) {
            message("error reading metadata" + e);
        }
        try {
            //sets values on the FileObject
            final ImageMetadata metadata = Imaging.getMetadata(file);
            if (metadata instanceof JpegImageMetadata) {
                final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
                setFileObjectValues(fNew,existingMetadata,jpegMetadata,file);
            }
            else
            {
                setFileObjectValues(fNew,existingMetadata,null,file);
            }
        } catch (Exception e) {
            message("Error reading metadata:"+e);
        }
        //create thumbnail and update FileObject
        fNew.setThumbnail(createThumbFromPicture(file, config.getTempdir(), thumbName, config.getWidth(),config.getHeight(),fNew.getOrientation()));
        // Geocodes if lat and long present
        if (fNew.getLatitude()!=null && fNew.getLongitude()!=null) {
            g = checkCachedGeo(fNew.getLatitude(), fNew.getLongitude(),fNew.getBestDate(),  Double.valueOf(config.getCacheDistance()));
            if (g == null) {
                waitForGeo(config.getPauseSeconds());
                g = OpenMaps.reverseGeocode(String.valueOf(fNew.getLatitude()), String.valueOf(fNew.getLongitude()),config);
                if (g != null) {
                     fNew.setPlaceKey(addPlace(g,fNew.getBestDate()));
                } else {
                    fNew.setDisplayName("");
                    message("Could not geocode :Lat" + fNew.getLatitude() + ", Long:" + fNew.getLongitude());
                }
            }
            else
            {
                message("Found Lat / Long in cache : ["+g.getInternalKey()+"]"+g.getDisplay_name());
                fNew.setPlaceKey(g.getInternalKey());
            }
            if(g!=null)
            {
                countGEOCODED++;
                countDriveGEOCODED++;
                setFileObjectGEOValues(fNew,g);
            }
        } else {
            fNew.setDisplayName("No Lat and Long date found");
        }
        if(config.getUpdate()) {
            if(!updateFile(config,drive,file,existingCommentsString,iptc,exif,alreadyGeocoded,existingMetadata,fNew)){message("Cannot update File");}
        }
        fileObjects.add(fNew);
        return true;
    }
    public static void setFileObjectGEOValues(FileObject fNew,Place g)
    {
        fNew.setDisplayName(g.getDisplay_name());
        fNew.setCity(g.getIPTCCity());
        fNew.setCountry_code(g.getIPTCCountryCode());
        fNew.setCountry_name(g.getIPTCCountry());
        fNew.setStateProvince(g.getIPTCStateProvince());
        fNew.setSubLocation(g.getIPTCSublocation());

    }
    /**
     * @param fNew - this sets values on the new FileObject
     * @param existingMetadata - existing values read from file
     * @param jpegMetadata - existing values read from metadata
     * @param file - current file being processed
     */
    public static void setFileObjectValues(FileObject fNew,FileObject existingMetadata,JpegImageMetadata jpegMetadata,File file)
    {
        if(jpegMetadata!=null)
        {
            fNew.setCameraMaker(getStringOrUnknown(jpegMetadata, TiffTagConstants.TIFF_TAG_MAKE));
            fNew.setCameraModel(getStringOrUnknown(jpegMetadata, TiffTagConstants.TIFF_TAG_MODEL));
            fNew.setFStop(getTagValueDouble(jpegMetadata, ExifTagConstants.EXIF_TAG_FNUMBER));
            fNew.setProgramName(getStringOrUnknown(jpegMetadata, ExifTagConstants.EXIF_TAG_SOFTWARE));
            fNew.setOrientation(getTagValueInteger(jpegMetadata, TiffTagConstants.TIFF_TAG_ORIENTATION));
            fNew.setExifOriginal(getTagValueDate(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL));
            fNew.setExifDigitised(getTagValueDate(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED));
            fNew.setTiffDate( getTagValueDate(jpegMetadata, TiffTagConstants.TIFF_TAG_DATE_TIME));
            fNew.setBestDate(fNew.getExifOriginal());
            if (fNew.getBestDate() == null) {
                fNew.setBestDate(fNew.getExifDigitised());
            }
            if (fNew.getBestDate() == null) {
                fNew.setBestDate(fNew.getTiffDate()) ;
            }
            // simple interface to GPS data
            fNew.setAltitude(getTagValueDouble(jpegMetadata,GpsTagConstants.GPS_TAG_GPS_ALTITUDE));
            try {


                final TiffImageMetadata exifMetadata = jpegMetadata.getExif();
                if (null != exifMetadata) {
                    final TiffImageMetadata.GPSInfo gpsInfo = exifMetadata.getGPS();
                    if (null != gpsInfo) {
                        fNew.setLongitude(gpsInfo.getLongitudeAsDegreesEast());
                        fNew.setLatitude(gpsInfo.getLatitudeAsDegreesNorth());
                        message("    " + "GPS Longitude (Degrees East): " + fNew.getLongitude());
                        message("    " + "GPS Latitude (Degrees North): " + fNew.getLatitude());
                    }
                }
            }
            catch(Exception e)
            {
                message("Error acessing exif metadata");
            }

        }
        fNew.setFileModified(existingMetadata.getFileModified());
        fNew.setFileAccessed(existingMetadata.getFileAccessed());
        fNew.setFileCreated(existingMetadata.getFileCreated());
        if(fNew.getOrientation()==null)
        {
            fNew.setOrientation(1);
        }
        if(fNew.getCameraMaker()==null)
        {
            fNew.setCameraMaker("Unknown");
        }
        if(fNew.getCameraModel()==null)
        {
            fNew.setCameraModel("Unknown");
        }
        if (fNew.getBestDate() == null) {
            fNew.setBestDate(getFileDate(file));
        }
        fNew.setFileName(file.getName());
        fNew.setFileSize(new BigDecimal(file.length()));
        fNew.setDirectory(FilenameUtils.getFullPath(file.getPath()));
        fNew.setFileKey(count);
        try {
            BufferedImage bimg = ImageIO.read(file);
            fNew.setWidth(bimg.getWidth());
            fNew.setHeight(bimg.getHeight());
        }
        catch(Exception e)
        {
            message("Could not read width and height for "+file.getName());
        }
        fNew.setCameraKey(addCamera(fNew.getCameraMaker(),fNew.getCameraModel(), fNew.getBestDate()));
        if(cameras.get(fNew.getCameraKey()-1).getFriendlyname()!=null)
        {
            fNew.setCameraName(cameras.get(fNew.getCameraKey()-1).getFriendlyname());
        }
    }
    public static void readSystemDates(FileObject f,File file)
    {
        try {
            BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            f.setFileModified(new Date(attr.lastModifiedTime().toMillis()));
            f.setFileCreated(new Date(attr.creationTime().toMillis()));
            message("System Creation Date/Time: " +  f.getFileCreated());
            f.setFileAccessed(new Date(attr.lastAccessTime().toMillis()));
        } catch (Exception e) {
            message("Could not read File Basic Attributes: " + e);
        }

    }
    public static boolean readIPTCdata(FileObject f, Metadata meta) {
        try {
            for (MetadataEntry item : meta) {
                if (item.getKey().equals(IPTCApplicationTag.CITY.getName())) {
                    if (item.getValue().length() > 0) {
                        f.setCity(item.getValue());
                    }
                    message("City current value is:" + item.getValue());
                } else if (item.getKey().equals(IPTCApplicationTag.COUNTRY_CODE.getName())) {
                    if (item.getValue().length() > 0) {
                        f.setCountry_code(item.getValue());
                        message("Country code current value is:" + item.getValue());
                    }
                } else if (item.getKey().equals(IPTCApplicationTag.COUNTRY_NAME.getName())) {
                    if (item.getValue().length() > 0) {
                        f.setCountry_name(item.getValue());
                        message("Country name current value is:" + item.getValue());
                    }
                } else if (item.getKey().equals(IPTCApplicationTag.SUB_LOCATION.getName())) {
                    if (item.getValue().length() > 0) {
                        f.setSubLocation(item.getValue());
                        message("Sublocation current value is:" + item.getValue());
                    }
                } else if (item.getKey().equals(IPTCApplicationTag.PROVINCE_STATE.getName())) {
                    if (item.getValue().length() > 0) {
                        f.setStateProvince(item.getValue());
                        message("Province / State current value is:" + item.getValue());
                    }
                } else if (item.getKey().equals(IPTCApplicationTag.COPYRIGHT_NOTICE.getName())) {
                    if (item.getValue().length() > 0) {
                        f.setIPTCCopyright(item.getValue());
                        message("IPTC Copyright value is:" + item.getValue());
                    }

                } else if (item.getKey().equals(IPTCApplicationTag.CATEGORY.getName())) {
                    if (item.getValue().length() > 0) {
                        f.setIPTCCategory(item.getValue());
                        message("IPTC Category value is:" + item.getValue());
                    }
                } else if (item.getKey().equals(IPTCApplicationTag.KEY_WORDS.getName())) {
                    if (item.getValue().length() > 0) {
                        f.setIPTCKeywords(item.getValue());
                        message("IPTC Keywords value is:" + item.getValue());
                    }
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public static boolean updateFile(ConfigObject config,DriveObject drive, File file,ArrayList<String> existingCommentsString,IPTC iptc,JpegExif exif,Boolean alreadyGeocoded,FileObject existingMetadata,FileObject fNew)
    {
        boolean fileGeocoded=false;
        try {
            List<IPTCDataSet> iptcs = new ArrayList<>();
            // update if the existing value is not set, or if update is forced by overwrite or redogeocode
            //
            if ((StringUtils.isNullOrEmpty(existingMetadata.getCountry_code()) &&  !alreadyGeocoded)  ||   (config.getOverwrite() &&  !alreadyGeocoded)|| config.getRedoGeocode()) {
                iptcs.add(new IPTCDataSet(IPTCApplicationTag.COUNTRY_CODE, fNew.getCountry_code().toUpperCase()));
                message("New Value found:"+ IPTCApplicationTag.COUNTRY_CODE.toString()+ " - "+ fNew.getCountry_code());
                fileGeocoded=true;
            }
            if ((StringUtils.isNullOrEmpty(existingMetadata.getCountry_name())  &&  !alreadyGeocoded)  ||  (config.getOverwrite() &&  !alreadyGeocoded)|| config.getRedoGeocode()) {
                iptcs.add(new IPTCDataSet(IPTCApplicationTag.COUNTRY_NAME, fNew.getCountry_name()));
                message("New Value found:"+ IPTCApplicationTag.COUNTRY_NAME.toString()+ " - "+ fNew.getCountry_name());
                fileGeocoded=true;
            }
            if ((StringUtils.isNullOrEmpty(existingMetadata.getStateProvince()) &&  !alreadyGeocoded)  ||  (config.getOverwrite() &&  !alreadyGeocoded)|| config.getRedoGeocode()) {
                iptcs.add(new IPTCDataSet(IPTCApplicationTag.PROVINCE_STATE,  fNew.getStateProvince()));
                message("New Value found:"+ IPTCApplicationTag.PROVINCE_STATE.toString()+ " - "+ fNew.getStateProvince());
                fileGeocoded=true;
            }
            if ((StringUtils.isNullOrEmpty(existingMetadata.getSubLocation()) &&  !alreadyGeocoded)  ||  (config.getOverwrite() &&  !alreadyGeocoded) || config.getRedoGeocode()) {
                iptcs.add(new IPTCDataSet(IPTCApplicationTag.SUB_LOCATION, fNew.getSubLocation()));
                message("New Value found:"+ IPTCApplicationTag.SUB_LOCATION.toString()+ " - "+ fNew.getSubLocation());
                fileGeocoded=true;
            }
            if ((StringUtils.isNullOrEmpty(existingMetadata.getCity())  &&  !alreadyGeocoded)  || (config.getOverwrite() && !alreadyGeocoded) || config.getRedoGeocode()) {
                iptcs.add(new IPTCDataSet(IPTCApplicationTag.CITY, fNew.getCity()));
                message("New Value found:"+ IPTCApplicationTag.CITY.toString()+ " - "+ fNew.getCity());
                fileGeocoded=true;
            }
            //     List<IPTCDataSet> iptcs =  new ArrayList<IPTCDataSet>();
            if (StringUtils.isNullOrEmpty(existingMetadata.getIPTCCopyright())  || config.getOverwrite()) {
                if (!StringUtils.isNullOrEmpty(drive.getIPTCCopyright())) {
                    iptcs.add(new IPTCDataSet(IPTCApplicationTag.COPYRIGHT_NOTICE, drive.getIPTCCopyright()));
                }
            }
            if (StringUtils.isNullOrEmpty(existingMetadata.getIPTCCategory()) || config.getOverwrite()) {
                if (!StringUtils.isNullOrEmpty(drive.getIPTCCategory())) {
                    iptcs.add(new IPTCDataSet(IPTCApplicationTag.CATEGORY, drive.getIPTCCategory()));
                }
            }
            if (StringUtils.isNullOrEmpty(existingMetadata.getIPTCKeywords())  || config.getOverwrite()) {
                if (!StringUtils.isNullOrEmpty(drive.getIPTCKeywords())) {
                    iptcs.add(new IPTCDataSet(IPTCApplicationTag.KEY_WORDS, drive.getIPTCKeywords()));
                }
            }
            FileInputStream fin = new FileInputStream(file.getPath());
            String fout_name = FilenameUtils.getFullPath(file.getPath()) + "out" + FilenameUtils.getName(file.getPath());
            File outFile = new File(fout_name);
            FileOutputStream fout = new FileOutputStream(outFile, false);
            List<Metadata> metaList = new ArrayList<>();

            metaList.add(populateExif(exif));
            iptc.addDataSets(iptcs);
            metaList.add(iptc);
            if(fileGeocoded) {
                DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                existingCommentsString.add("#geocoded:" + formatter.format(new Date()));
                metaList.add(new Comments(existingCommentsString));
            }
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
            Files.setAttribute(file.toPath(), "creationTime", FileTime.fromMillis(existingMetadata.getFileCreated().getTime()));
            Files.setAttribute(file.toPath(), "lastAccessTime", FileTime.fromMillis(existingMetadata.getFileAccessed().getTime()));
            Files.setAttribute(file.toPath(), "lastModifiedTime", FileTime.fromMillis(existingMetadata.getFileModified().getTime()));
            return true;
        } catch (Exception e) {
            message("Cannot write metadata:" + e);
            return false;
        }


    }
    // This method is for testing only
    private static Exif populateExif(Exif exif)  {

        exif.addExifField(ExifTag.WINDOWS_XP_AUTHOR, FieldType.WINDOWSXP, "Author");
        exif.addExifField(ExifTag.WINDOWS_XP_KEYWORDS, FieldType.WINDOWSXP, "Exif Copyright:  Ted Carroll ");
        // Insert ThumbNailIFD
        // Since we don't provide thumbnail image, it will be created later from the input stream
        exif.setThumbnailRequired(true);

        return exif;
    }
    // Points will be converted to radians before calculation
    // returns value in metres
    public static double distance_Between_LatLong(double lat1, double lon1, double lat2, double lon2) {
        lat1 = Math.toRadians(lat1);
        lon1 = Math.toRadians(lon1);
        lat2 = Math.toRadians(lat2);
        lon2 = Math.toRadians(lon2);
        double earthRadius = 6371.01; //Kilometers
        return (earthRadius * Math.acos(Math.sin(lat1)*Math.sin(lat2) + Math.cos(lat1)*Math.cos(lat2)*Math.cos(lon1 - lon2)))*1000;
    }
    /**
     * gets either a string or "unknown" string from metadata
     * @param jpegMetadata - jpegmetadata object
     * @param tagInfo - tag value
     * @return - string value
     */
    public static String getStringOrUnknown(final JpegImageMetadata jpegMetadata, final TagInfo tagInfo)
    {
        String v=getTagValueString(jpegMetadata, tagInfo);
        if (v.length() > 0) {
            return v.replaceAll("'","");
        } else {
            return "Unknown";
        }
    }
    /**
     *  Reads metadata
     *  THIS EXAMPLE LARGELY COPIED FROM ICAFE SAMPLE CODE
     * @param file - file to read
     * @return - either true (sucessful) or false
     */
    public static Boolean readMetadata(File file) {
        try {
            Map<MetadataType, Metadata> metadataMap = Metadata.readMetadata(file.getPath());
            for (Map.Entry<MetadataType, Metadata> entry : metadataMap.entrySet()) {
                Metadata meta = entry.getValue();
                if (meta instanceof XMP) {
                  XMP.showXMP((XMP) meta);
                } else if (meta instanceof IRB) {
                    for (MetadataEntry item : meta) {
                        printMetadata(item,"IRB - "+ item.getKey()+":","    ");
                    }
                } else if (meta instanceof ImageMetadata) {
                    for (MetadataEntry item : meta) {
                        printMetadata(item,"Image metadata - "+ item.getKey()+":","    ");
                    }
                } else if (meta instanceof Exif) {
                    for (MetadataEntry item : meta) {
                       printMetadata(item,"JpegExif - "+ item.getKey()+":","    ");
                    }
                } else if (meta instanceof IPTC) {
                    for (MetadataEntry item : meta) {
                       printMetadata(item,"IPTC - "+ item.getKey()+":","    ");
                    }
                } else if (meta instanceof Comments) {
                    List<String> existingComments ;
                    existingComments = ((Comments) meta).getComments();
                    int count=1;
                    for(String s : existingComments)
                    {
                        message("Comments["+count+"]:" + s);
                        count++;
                    }
                } else {
                    for (MetadataEntry item : entry.getValue()) {
                        printMetadata(item, "Other - "+meta.getType().toString()+item.getKey()+":", "     ");
                    }
                }
            }
        } catch (Exception e) {
            message("error reading metadata" + e);
        }
        return true;
    }
    /**
     * Set Drive structure if there is no json input file
     * @param config - config object
     * @param rootDrive - root drive (passed as an argument at program start)
     * @param tempDir - temp directory (passed as an argument at program start)
      */
    public static void setDrives(ConfigObject config,String rootDrive,String tempDir)
    {
        config.setTempdir(tempDir);
        DriveObject d= new DriveObject();
        d.setStartdir(rootDrive);
        ExcludeSpec es = new ExcludeSpec();
        DirectoryObject dir = new DirectoryObject();
        ArrayList<DirectoryObject> directories = new ArrayList<>();
        directories.add(dir);
        DirectoryObject prefix = new DirectoryObject();
        ArrayList<DirectoryObject> fileprefixes = new ArrayList<>();
        fileprefixes.add(prefix);
        es.setDirectories(directories);
        es.setFileprefixes(fileprefixes);
        d.setExcludespec(es);
        ArrayList<DriveObject> drives = new ArrayList<>();
        drives.add(d);
        config.setDrives(drives);
    }
    /**
     * Reads each drive in turn and processes directory content
     * @param c - config object
     */
    public static void readDrives(ConfigObject c)
    {
        for (DriveObject d : c.getDrives()) {
            countDrive = 0;
            countDriveTooSmall = 0;
            countDriveGEOCODED = 0;
            message("Reading drive: "+d.getStartdir());

            //recursively find all files
            readDirectoryContents(new File(d.getStartdir()), d, c.getTempdir(), c);
            messageLine("-");
            message("Photos found on drive "+d.getStartdir()+" :" + countDrive );
            message("Photos too small on drive "+d.getStartdir()+" :" + countDriveTooSmall );
            message("Photos geocoded on drive "+d.getStartdir()+" :" + countDriveGEOCODED );
        }
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
