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

import com.icafe4j.image.tiff.TiffTag;
import com.icafe4j.string.StringUtils;
import com.icafe4j.image.meta.xmp.XMP;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import no.api.freemarker.java8.Java8ObjectWrapper;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static IC.ImageProcessing.createThumbFromPicture;
import static IC.openmaps.OpenMaps.checkPostCode;

/**
 * Main class
 */
public class ImageCatalogue {
    static ZoneId z;
    static int countFiles = 0;   //total number of images found
    static int countImages = 0;   //total number of images found
    static int countTooSmall = 0; // images too small (images processed is countImages-countTooSmall)
    static int countProcessed = 0; // images processed (images processed is countImages-countTooSmall)
    static int countLATLONG = 0;  // images with lat/long info
    static int countALREADYGEOCODED = 0; // images already geocoded
    static int countGEOCODED = 0; // images which were successfully geocoded
    static int countNOTGEOCODED = 0; // images which were not successfully geocoded (but with lat/long)
    static int countDateUpdate = 0;   // images where the date was updated.
    static int countEventsFound = 0;   // images where events were found through date checks...
    static int countAddedPlace = 0;   // images where a place has been specified
    static int countAddedLATLONG = 0;   // images where a LatLon has been added (either directly or via an event)
    static int countAddedPostcode = 0;   // images where a postcode has been used to determine LatLon (either directly or via an event)
    static int countAddedEvent = 0;   // images where an event has been added  manually
    static int countUPDATED = 0; // images which were not successfully updated
    static int countErrors = 0;   // images where there were errors (e.g. failure of geocode or others)....
//
    static int countDriveFiles = 0;
    static int countDriveImages = 0;
    static int countDriveTooSmall = 0;
    static int countDriveProcessed = 0;
    static int countDriveLATLONG = 0;
    static int countDriveALREADYGEOCODED = 0;
    static int countDriveGEOCODED = 0;
    static int countDriveNOTGEOCODED = 0;
    static int countDriveDateUpdate = 0;
    static int countDriveEventsFound = 0;
    static int countDriveAddedPlace = 0;
    static int countDriveAddedLATLONG = 0;
    static int countDriveAddedPostcode = 0;
    static int countDriveAddedEvent = 0;
    static int countDriveUPDATED = 0;
    static int countDriveErrors = 0;
    static ArrayList<CameraObject> cameras = new ArrayList<>();
    static ArrayList<FileObject> fileObjects = new ArrayList<>();
    static ArrayList<FileObject> duplicateObjects = new ArrayList<>();
    static ArrayList<ErrorObject> errorObjects = new ArrayList<>();
    static ArrayList<Place> places = new ArrayList<>();
    static ArrayList<EventObject> events = new ArrayList<>();

    static ArrayList<TrackObject> tracks = new ArrayList<>();
    // Default Variables - that can be modified
    static int messageLength=160; //length of Console Message
    static String videoDefaults="mp4~mp4a";
    static String imageDefaults="jpg~jpeg~bmp";
    static String jsonDefault="config.json";
    static Long minFileSizeDefault=4000L;
    static String thumbSizeDefault="240x180";
    static String timeZoneDefault="Europe/London";
    static ArrayList<String> newfileNamesDefault=new ArrayList<>(Arrays.asList("camera","day"));
    static ArrayList<String> isocountryDefault=new ArrayList<>(Collections.singletonList("country_code"));
    static ArrayList<String> countryDefault=new ArrayList<>(Collections.singletonList("country"));
    static ArrayList<String> stateprovinceDefault=new ArrayList<>(Arrays.asList("county","state_district"));
    static ArrayList<String> cityDefault=new ArrayList<>(Arrays.asList("town","city","village"));
    static ArrayList<String> sublocationDefault=new ArrayList<>(Arrays.asList("amenity","leisure","house_number","road","hamlet","suburb","city_district"));
    static int cacheDistanceDefault=100; // this is the distance from a point that detemrines this is the same place..Metres.
    static int pauseSecondsDefault=2; // this is the pause before a geocode (to not overload the server...
    //
    static Date startTime;
    static Date endTime;
    /**
     * Main Method for the program - arguments passes as Java arguments..
     * @param args - either single json file or two/three args, first one is root directory, second is output file, third is followed by parameters
     */
    public static void main(String[] args) {
        z= ZoneId.systemDefault();
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
                    setDrives(config, args[0]);
                    config.setTempdir(args[1]);
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
            readRestOfArgs(config, args);
            setDefaults(Objects.requireNonNull(config));
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
            addLinksToEvents(config.getWidth(),config.getTempdir());
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
            config.setEvents(events);
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
                message("All Drives -" + "Files found                         :" + countFiles);
                message("All Drives -" + "Photos found                        :" + countImages);
                message("All Drives - " + "Photos too small                   :" + countTooSmall);
                message("All Drives - " + "Photos processed                   :" + countProcessed);
                message("All Drives - " + "Photos with Lat Long               :" + countLATLONG);
                message("All Drives - " + "Photos already Geocoded            :" + countALREADYGEOCODED);
                message("All Drives - " + "Photos Geocoded                    :" + countGEOCODED);
                message("All Drives - " + "Photos with failed Geocoding       :" + countNOTGEOCODED);
                message("All Drives - " + "Photos where date added            :" + countDateUpdate);
                message("All Drives - " + "Photos where Events found from date:" + countEventsFound);
                message("All Drives - " + "Photos with Place added            :" + countAddedPlace);
                message("All Drives - " + "Photos with Lat Lon added          :" + countAddedLATLONG);
                message("All Drives - " + "Photos with Postcode added         :" + countAddedPostcode);
                message("All Drives - " + "Photos with Event added            :" + countAddedEvent);
                message("All Drives - " + "Photos Updated                     :" + countUPDATED);
                message("All Drives - " + "Photos with Errors in processing   :" + countErrors);
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
        mapper.findAndRegisterModules();
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
            System.out.println("Error in JSON file:"+e);
            return null;
        }
    }
    public static void readRestOfArgs(ConfigObject config,String[] args)
    {
        //checks args - first 3 may not be
        int count=0;
        for(String s:args)
        {
            count++;
            if(s.trim().equalsIgnoreCase(Enums.argOptions.update.toString()))
            {
                config.setUpdate(true);
            }
            else if(s.trim().equalsIgnoreCase(Enums.argOptions.overwriteValues.toString()))
            {
                config.setOverwrite(true);
            }
            else if(s.trim().equalsIgnoreCase(Enums.argOptions.showmetadata.toString()))
            {
                config.setShowmetadata(true);
            }
            else if(s.trim().equalsIgnoreCase(Enums.argOptions.redoGeocoding.toString()))
            {
                config.setRedoGeocode(true);
            }
            else
            {
                //  the third parameter can be the newdirectory - if this is set, then files will be moved, if update is set to true
                if(count==3)
                {
                    config.setNewdir(s.trim());
                }
            }

        }


    }
    /**
     * Sets the defaults if the values are not in the JSON (note this is pass by value - and we can change values)
     * @param config - passes ConfigObject
     */
    public static void setDefaults(ConfigObject config)
    {
        if(config.getTimeZone()==null){config.setTimeZone(z.toString());}

        if(config.getMinfilesize()==null){config.setMinfilesize(minFileSizeDefault);}
        if(config.getPauseSeconds()==null){config.setPauseSeconds(pauseSecondsDefault);}
        if(config.getPauseSeconds()<1){config.setPauseSeconds(pauseSecondsDefault);}
        if(config.getThumbsize()==null){config.setThumbsize(thumbSizeDefault);}
        // sets the defaults
        if(config.getUpdate()==null){config.setUpdate(false);}
        if(config.getShowmetadata()==null) {config.setShowmetadata(false);}
        if(config.getOverwrite()==null) {config.setOverwrite(false);}
        if(config.getRedoGeocode()==null) {config.setRedoGeocode(false);}
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
        cfg.setObjectWrapper(new Java8ObjectWrapper(Configuration.VERSION_2_3_23));
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
            //
            Template etemplate = cfg.getTemplate("events.ftl");
            FileWriter ewriter = new FileWriter(tempDir+"/"+"events.html");
            Map<String, Object> eroot = new HashMap<>();
            eroot.put( "events",events );
            eroot.put("root",tempDir);
            etemplate.process(eroot, ewriter);
            ewriter.close();
            //
            Template rtemplate = cfg.getTemplate("errors.ftl");
            FileWriter rwriter = new FileWriter(tempDir+"/"+"errors.html");
            Map<String, Object> rroot = new HashMap<>();
            rroot.put( "comments",errorObjects );
            rroot.put("root",tempDir);
            rtemplate.process(rroot, rwriter);
            rwriter.close();
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
     //   SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm ss");
      //  objectMapper.setDateFormat(df);
        objectMapper.findAndRegisterModules();
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
        message("Number of Files found:"+fileObjects.size());
        message("Number of Cameras found:"+cameras.size());
        message("Number of Places found:"+places.size());
        message("Number of Errors:"+errorObjects.size());
        message("Number of Duplicates found:"+duplicateObjects.size());
        messageLine("*");

    }
    public static void readJPEGMetadata(File file,FileObject fNew)
    {
        try {
            //sets values on the FileObject
            final ImageMetadata metadata = Imaging.getMetadata(file);
            if (metadata instanceof JpegImageMetadata) {
                final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
                setFileObjectValues(fNew,jpegMetadata,file);
            }
            else
            {
                setFileObjectValues(fNew,null,file);
            }
        } catch (Exception e) {
            message("Error reading metadata:"+e);
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
                            if (!isExcluded(file.getCanonicalPath(), drive)) {
                                if (!file.getCanonicalPath().equals(tempDir)) {
                                    readDirectoryContents(file, drive, tempDir, config);
                                }
                            }
                        } else {
                            countFiles++;
                            countDriveFiles++;
                            if (isImage(file.getName(),config.getImageextensions())) {
                                if (!isExcludedPrefix(file.getName(), drive)) {
                                    countImages++;
                                    countDriveImages++;
                                    long fileSize = file.length();
                                    if (fileSize > config.getMinfilesize()) {
                                        countProcessed++;
                                        countDriveProcessed++;
                                        messageLine("-");
                                        message("File number:["+ countProcessed + "], file:" + file.getCanonicalPath());
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
                                            countDriveErrors++;
                                            message("Could not process file:"+file.getCanonicalPath());
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
            if (f.equals("city_district")) {
                s = addIfNotNull(s, g.getAddress().getCity_district());
            }
            if (f.equals("leisure")) {
                s = addIfNotNull(s, g.getAddress().getLeisure());
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
    public static void addLinksToEvents(Integer width, String root)
    {
        for(EventObject r : events)
        {
            StringBuilder s= new StringBuilder();
            for(FileObject f: fileObjects)
            {
                try {
                    if(f.getEventKeys().length()>0) {
                        String[] keys = f.getEventKeys().split(";",-1);
                        for(String k : keys)
                        {
                            if (k.equals(r.getEventid().toString())) {
                                if (!(f.getOrientation() == 8 || f.getOrientation() == 6)) {
                                    s.append("<img src=\"").append(root).append("\\").append(f.getThumbnail()).append("\" width=\"").append(width).append("\" class=\"padding\" >");
                                } else {
                                    Integer newWidth = width * f.getHeight() / f.getWidth();
                                    s.append("<img src=\"").append(root).append("\\").append(f.getThumbnail()).append("\" width=\"").append(newWidth).append("\" class=\"padding\" >");
                                }
                            }
                        }


                    }
                }
                catch(Exception e)
                {
                    message("error adding links to Events:"+f.getDisplayName()+e);
                }
            }
            if(s.toString().length()>0) {
                r.setImagelinks(s.toString());
            }
        }
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
                                s.append("<br><small>").append(f.getDirectory()).append("</small><br>");
                                s.append("<small>").append(f.getFileName()).append("</small><br>");
                            } else {
                                Integer newWidth = width * f.getHeight() / f.getWidth();
                                s.append("<img src=\"").append(root).append("\\").append(f.getThumbnail()).append("\" width=\"").append(newWidth).append("\" class=\"padding\" >");
                                s.append("<br><small>").append(f.getDirectory()).append("</small><br>");
                                s.append("<small>").append(f.getFileName()).append("</small><br>");
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
                           // System.out.println("best date:"+f.getBestDate().toLocalDate());
                           // System.out.println("trackdate date:"+t.getTrackDate());
                            if (f.getBestDate().toLocalDate().equals(t.getTrackDate())) {
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
            t.setImageLinks(s.toString());

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
    public static void addTrack(ArrayList<Integer> points, LocalDateTime startDate,LocalDateTime endDate, LocalDate trackDay,String coordinates)
    {
        // write out track
        if(points.size()>0) {
            TrackObject t = new TrackObject();
            t.setPoints(points);
            t.setTrackKey(tracks.size() + 1);
            t.setStartDate( startDate);
            t.setEndDate( endDate);
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
    public static Integer addPlace(Place g,LocalDateTime bestDate)
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
    public static void addError(String fileName,String directory,LocalDateTime d,String message)
    {
        ErrorObject error = new ErrorObject();
        error.setFileName(fileName);
        error.setFileDate(d);
        error.setMessage(message);
        error.setDirectory(directory);
        errorObjects.add(error);
    }
    public static void addComment(List<String> existingCommentsString,String newComment)
    {
        DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
        existingCommentsString.add("#"+newComment+"DONE:" + formatter.format(new Date()));
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
    public static Integer addCamera(String make, String model, LocalDateTime d) {
        for (CameraObject c : cameras) {
            if (make.equals(c.getCameramaker())) {
                if (model != null) {
                    if (model.equals(c.getCameramodel())) {
                        if (d.isBefore(c.getStartdate())) {
                            c.setStartdate(d);
                        }
                        if (d.isAfter(c.getEnddate())) {
                            c.setEnddate(d);
                        }
                        c.setCameracount(c.getCameracount() + 1);
                        return c.getCamerakey();
                    }
                } else {
                    if (c.getCameramodel() == null) {
                        if (d.isBefore(c.getStartdate())) {
                            c.setStartdate(d);
                        }
                        if (d.isAfter(c.getEnddate())) {
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
    public static boolean checkIPTCComments( List<String> existingComments,String test)
    {
        for(String s : existingComments)
        {
            if(s.contains(test))
            {

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
    public static Place checkCachedGeo(double lat, double lon, LocalDateTime d, double checkDistance) {
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
                    if (d.isBefore(g.getStartDate())) {
                        g.setStartDate(d);
                    }
                }
                if(g.getEndDate()==null)
                {
                    g.setEndDate(d);
                }
                else {
                    if (d.isAfter(g.getEndDate())) {
                        g.setEndDate(d);
                    }
                }
                return g;
            }
        }
        return null;
    }
    public static LocalDateTime createLocalDate(String param)
    {
        String[] values = param.split("-", -1);
        try {
            int year = 1900;
            int month = 1;
            int day = 1;
            if (values.length == 1) {
                //YYYY
                year = convertYear(values[0]);
            } else if (values.length == 2) {
                year = convertYear(values[0]);
                month = convertMonth(values[1]);

            } else if (values.length == 3) {
                year = convertYear(values[0]);
                month = convertMonth(values[1]);
                day = convertDay(values[2]);
            }
            return  LocalDateTime.of(year, month, day, 0, 0);
        }
        catch(Exception e) {
            return null;
        }
    }
    public static LocalDateTime createLocalDateCalendar(String param)
    {
        String[] values = param.split("-", -1);
        try {
            int year = 1900;
            int month = 1;
            int day = 1;
            if (values.length == 1) {
                //YYYY
                return null;
            } else if (values.length == 2) {
                month = convertMonth(values[0]);
                day = convertDay(values[1]);

            } else if (values.length == 3) {
                year = convertYear(values[0]);
                month = convertMonth(values[1]);
                day = convertDay(values[2]);
            }
            return  LocalDateTime.of(year, month, day, 0, 0);
        }
        catch(Exception e) {
            return null;
        }
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
        LocalDate lastDay=null;
        Integer placeKey=0;
        LocalDateTime startDate=null;
        LocalDateTime endDate=null;
        StringBuilder coordinates = new StringBuilder();
        ArrayList<Integer> points = new ArrayList<>();
        for(FileObject f : fileObjects)
        {
            //
            LocalDate d= f.getBestDate().toLocalDate();
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
                lastDay=f.getBestDate().toLocalDate();

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
        if(c.getEvents()!=null)
        {
            int numEvents=c.getEvents().size();
            message("NUMBER OF EVENTS READ FROM CONFIG FILE: "+c.getEvents().size());
            events =c.getEvents();
            checkEvents();
            if(numEvents!=c.getEvents().size())
            {
                message("NUMBER OF EVENTS AFTER CHECKING IS: "+c.getEvents().size());

            }
            for(EventObject e : events)
            {
                e.setImagelinks("No files found");
            }
            //sort in case there are gaps in numbering
            events.sort(Comparator.comparing(EventObject::getEventid));
        }
    }
    public static String getNewDirectory(ConfigObject config,FileObject f)
    {
        //getYear
        int year=f.getBestDate().getYear();
        // create directory
        createTempDirForUser(config.getNewdir()+"/"+year);
        int month=f.getBestDate().getMonth().getValue();
        String newDirectory= config.getNewdir()+"/"+year+"/"+month;
        createTempDirForUser(newDirectory);
        return newDirectory;
    }
    /**
     * Moves a file if it does not already exist in the new structure
     * @param config - config Object (contains program variables)
     * @param f - fileObject to be moved
     * @return - returns new directory or null
     */
    public static String moveFile(ConfigObject config, FileObject f)
    {
        //

            String newDirectory = getNewDirectory(config,f);
            File oldFile= new File(f.getDirectory()+"/"+f.getFileName());
            File newFile =new File(newDirectory+"/"+f.getFileName());
            if(newFile.exists()) {
                addError(f.getFileName(),newDirectory,f.getBestDate(),"File already exists in the new directory structure - it will not be copied");
                newDirectory=null;
            }
            else
            {
                String oldThumbName = makeThumbName(oldFile);
                boolean renameResult=oldFile.renameTo(newFile);
                if(!renameResult)
                {
                    message("Could not move file from "+oldFile.getPath() + " to "+newFile.getPath());
                }
                String newThumbName = makeThumbName(newFile);

                File oldThumb = new File(config.getTempdir() + "/" + oldThumbName);
                File newThumb = new File(config.getTempdir() + "/" + newThumbName);
                //rename file
                boolean renameResultthumb=oldThumb.renameTo(newThumb);
                if(!renameResult)
                {
                    message("Could not move file from "+oldThumb.getName() + " to "+newThumb.getName());
                }
                //if rename files - change the name and directory....
                f.setDirectory(newDirectory);
                f.setThumbnail(newThumbName);
            }

            //thee fiule name st

        return newDirectory;
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
        * @return - either true (excluded) or false(not excluded)
     */
    public static Boolean isExcluded(String fdir, DriveObject drive) {

        if(drive.getExcludespec()==null)
        {
            return false;
        }
        if(drive.getExcludespec().getDirectories()==null)
        {
            return false;
        }
        for (DirectoryObject i : drive.getExcludespec().getDirectories()) {
           System.out.println("exclude"+fdir+ ",start:"+drive.getStartdir()+i.getName());
            System.out.println("exclude"+fixSlash(fdir)+ ",start:"+drive.getStartdir()+fixSlash(i.getName()));
            if(i.getName()!=null) {
                if (fixSlash(fdir).equals(drive.getStartdir() + fixSlash(i.getName()))) {
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

     * @return - either true (exclude) or false (not excluded)
     */
    public static Boolean isExcludedPrefix(String fname, DriveObject drive) {
        if(drive.getExcludespec()==null)
        {
            return false;
        }
        if(drive.getExcludespec().getFileprefixes()==null)
        {
            return false;
        }
        for (DirectoryObject i : drive.getExcludespec().getFileprefixes()) {
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
    public static String fixSlash(String s)
    {
        return s.replace("\\","/");
    }
    public static ArrayList<String> joinKeys(ArrayList<String> current,ArrayList<String> newKeys)
    {


        for(String n : newKeys)

        {
            current.add(n);

        }
        return current;
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
     * Finds Place from its internal key
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
    /**
     * Finds Event from its internal key
     * @param i - key value
     * @return - Event Object
     */
    private static EventObject getEvent(Integer i)
    {
        for(EventObject r : events)
        {
            if(r.getEventid().equals(i))
            {
                return r;
            }
        }
        message("Error -could not retrieve Event with a key of:"+i);
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
                //some dates have slashes instead of colons
                return inputFormatter.parse(field.getValueDescription().replace("'", "").replace("/",":"));

            } catch (Exception e) {
                try {
                    SimpleDateFormat inputFormatter = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.ENGLISH);
                    return inputFormatter.parse(field.getValueDescription().replace("'", "").replace("/",":"));

                } catch (Exception ee) {
                    message("******* error converting date:" + field.getValueDescription() + e);
                    addError("-","-",null,"error parsing date:"+field);
                    return null;
                }

            }
        }
    }
    public static Enums.processMode processForwardCodeFromLocation(ConfigObject config,FileObject fNew,ArrayList<String> existingCommentsString,String location)
    {
        Enums.processMode processMode= forwardCode(location,config,fNew,existingCommentsString);
        if(processMode!=null)
        {
            addComment(existingCommentsString,processMode.toString());
        }
        return processMode;
    }
    public static Enums.processMode processForwardCode(ConfigObject config,FileObject fNew,ArrayList<String> existingCommentsString)
    {
        Enums.processMode processMode= forwardCode(fNew.getWindowsComments(),config,fNew,existingCommentsString);
        if(processMode!=null)
        {
            fNew.setWindowsComments(updateInstructions(fNew.getWindowsComments(),processMode));
            fNew.setIPTCInstructions(updateInstructions(fNew.getIPTCInstructions(),processMode));
        }
        else
        {
            processMode=forwardCode(fNew.getIPTCInstructions(),config,fNew,existingCommentsString);
            if(processMode!=null)
            {
                fNew.setIPTCInstructions(updateInstructions(fNew.getIPTCInstructions(),processMode));
                fNew.setWindowsComments(updateInstructions(fNew.getWindowsComments(),Enums.processMode.date));
            }
        }
        return processMode;
    }
    public static Integer checkEvent(ConfigObject config,FileObject fNew,EventObject e,ArrayList<String> existingCommentsString)
    {
        LocalDateTime d= fNew.getBestDate();
        if(checkIPTCComments(existingCommentsString,"#Event:"+e.getEventid()+"DONE:")!=true || config.getOverwrite()) {
            //event date
            if (e.eventcalendar != null) {
                if (d.getMonth() == e.getExactStartTime().getMonth() && d.getDayOfMonth() == e.getExactStartTime().getDayOfMonth()) {
                    if(updateEvent(config, fNew, e, existingCommentsString, e.getLocation()))
                    {
                        return 1;
                    }
                    message("Event calendar match for event:" + e.getEventid() + " " + e.getTitle());
                }
            } else {
                if ((d.isAfter(e.getExactStartTime()) || d.isEqual(e.getExactStartTime()))
                        &&
                        (d.isBefore(e.getExactEndTime()) || d.isEqual(e.getExactEndTime()))
                ) {
                    // we have a match... so process..
                    message("Event date match for event:" + e.getEventid() + " " + e.getTitle());
                    if(updateEvent(config, fNew, e, existingCommentsString, e.getLocation()))
                    {
                        return 1;
                    }
                }
            }

        }
        return 0;
    }
    public static Integer processEvents(ConfigObject config,FileObject fNew,ArrayList<String> existingCommentsString)
    {
        int eventFound=0;

        for(EventObject e : events)
        {
           eventFound=eventFound+checkEvent(config,fNew,e,existingCommentsString);
        }
         return eventFound;

    }
    public static Boolean processDates(FileObject fNew,ArrayList<String> existingCommentsString)
    {
        Boolean dateUpdated=false;
        dateUpdated=updateDate(fNew.getWindowsComments(),fNew);
        if(dateUpdated)
        {
            fNew.setWindowsComments(updateInstructions(fNew.getWindowsComments(),Enums.processMode.date));
            fNew.setIPTCInstructions(updateInstructions(fNew.getIPTCInstructions(),Enums.processMode.date));
            addComment(existingCommentsString,Enums.processMode.date.toString());
        }
        else
        {
            dateUpdated=updateDate(fNew.getIPTCInstructions(),fNew);
            if(dateUpdated)
            {
                fNew.setWindowsComments(updateInstructions(fNew.getWindowsComments(),Enums.processMode.date));
                fNew.setIPTCInstructions(updateInstructions(fNew.getIPTCInstructions(),Enums.processMode.date));
                addComment(existingCommentsString,Enums.processMode.date.toString());
            }
        }
        return dateUpdated;
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

        FileObject fNew = new FileObject();

        boolean alreadyGeocoded = false;
        readSystemDates(fNew,file);
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

                    fNew.setWindowsComments(exif.getExifIFD().getFieldAsString(ExifTag.WINDOWS_XP_COMMENT));
                    fNew.setWindowsTitle(exif.getImageIFD().getFieldAsString(ExifTag.WINDOWS_XP_TITLE));
                    fNew.setWindowsSubject(exif.getImageIFD().getFieldAsString(ExifTag.WINDOWS_XP_SUBJECT));
                } else if (meta instanceof Comments) {
                    existingComments = (((Comments) meta).getComments());
                    alreadyGeocoded=checkIPTCComments(existingComments,"#"+Enums.processMode.geocode+"DONE:");
                    if(alreadyGeocoded)
                    {
                        message("File has already been geocoded:"+file.getName());
                    }
                } else if (meta instanceof IPTC) {
                    iptc = (IPTC) meta;
                    if(!readIPTCdata(fNew,meta)){message("Cannot read IPTC data");}
                }
            }
        } catch (Exception e) {
            message("error reading metadata" + e);
        }
        readJPEGMetadata(file,fNew);
        //create thumbnail and update FileObject
        fNew.setThumbnail(createThumbFromPicture(file, config.getTempdir(), thumbName, config.getWidth(),config.getHeight(),fNew.getOrientation()));
        // Geocodes if lat and long present

        Boolean dateUpdated=processDates(fNew,existingCommentsString);
        Enums.processMode processMode=null;
        if (fNew.getLatitude()!=null && fNew.getLongitude()!=null) {
            geocodeLatLong(alreadyGeocoded,config,fNew,existingCommentsString);
        }
        Enums.processMode forwardUpdated=processForwardCode(config,fNew,existingCommentsString);
        Integer eventsUpdated=eventsUpdated=processEvents(config,fNew,existingCommentsString);

        updateFile(config,drive,file,existingCommentsString,iptc,exif,alreadyGeocoded,fNew);
        fileObjects.add(fNew);
        if(config.getShowmetadata() && config.getUpdate()) {
            messageLine("~");
            if (!readMetadata(new File(fNew.getDirectory()+"/"+fNew.getFileName()))) {
                message("Could not read metadata after update");
            }
        }
        return true;
    }
    public static void geocodeLatLong(Boolean alreadyGeocoded,ConfigObject config,FileObject fNew,ArrayList<String> existingCommentsString)
    {
        countLATLONG++;
        countDriveLATLONG++;
        if(alreadyGeocoded)
        {
            countALREADYGEOCODED++;
            countDriveALREADYGEOCODED++;
        }
        if(!alreadyGeocoded || config.getRedoGeocode() ) {
            if(geocode(fNew, fNew.getLatitude(),fNew.getLongitude(),config,alreadyGeocoded)) {
                addComment(existingCommentsString, Enums.processMode.geocode.toString());
            }
        }
    }
    public static Boolean geocode(FileObject fNew,Double lat,Double lon,ConfigObject config,Boolean alreadyGeocoded)
    {
        Place g;
        g = checkCachedGeo(lat,lon, fNew.getBestDate(), Double.valueOf(config.getCacheDistance()));
        if (g == null) {
            waitForGeo(config.getPauseSeconds());
            g = OpenMaps.reverseGeocode(String.valueOf(lat), String.valueOf(lon), config);
            if (g != null) {
                fNew.setPlaceKey(addPlace(g, fNew.getBestDate()));
            } else {
                countNOTGEOCODED++;
                countDriveNOTGEOCODED++;
                fNew.setDisplayName("");
                message("Could not geocode :Lat" + fNew.getLatitude() + ", Long:" + fNew.getLongitude());
                addError(fNew.getFileName(),fNew.getDirectory(),fNew.getBestDate(),"could not geocode:"+fNew.getLongitude()+","+fNew.getLatitude());
            }
        } else {
            message("Found Lat / Long in cache : [" + g.getInternalKey() + "]" + g.getDisplay_name());
            fNew.setPlaceKey(g.getInternalKey());
        }
        if (g != null) {
            setFileObjectGEOValues(fNew, g,alreadyGeocoded,config);
            countGEOCODED++;
            countDriveGEOCODED++;
            return true;
        }
        return false;
     }
    public static Enums.processMode testOptions(String s)
    {
        if(s==null)
        {
            return null;
        }
        for(Enums.processMode p :Enums.processMode.values())
        {
            if(s.toLowerCase().contains("#"+p.toString() +":"))
            {
                return p;
            }

        }
        return null;
    }
    public static ArrayList<String> convertPathToKeywords(String dir,String root)
    {
        // change all slashes to forward
        // remove root and then replace slashes with spaces and return an array of values
        ArrayList<String> keys = new ArrayList<String>();
        String news=fixSlash(dir).replace(fixSlash(root),"").replace("/"," ").trim();
        String[] ss = news.split(" ",-1);
        if(ss.length==0)
        {
            return keys;
        }

        for(String s:ss)
        {
            keys.add(s);
        }
        return keys;
    }
    public static int convertYear(String s)
    {
        int year=0;
        try {
            year = Integer.parseInt(s);
            if (year < 100) {
                year = 2000 + year;
            }
        }
        catch(Exception e)
        {
           message("Cannot convert year:"+s);
        }
        return year;
    }
    public static int convertMonth(String s)
    {
        int month=0;
        try {
            month= Integer.parseInt(s);
            if (month < 1 || month >12) {
                month=1;
            }
        }
        catch(Exception e)
        {
            message("Cannot convert month:"+s);
        }
        return month;
    }
    public static int convertDay(String s)
    {
        int day=0;
        try {
            day= Integer.parseInt(s);
            if (day < 1 || day >31) {
                day=1;
            }
        }
        catch(Exception e)
        {
            message("Cannot convert day:"+s);
        }
        return day;
    }
    public static Boolean updateEvent(ConfigObject config, FileObject fNew, EventObject e,ArrayList<String> existingCommentsString,String location)
    {

        if(e.getTitle()!=null)
        {
            fNew.setWindowsTitle(e.getTitle() + " "+ fNew.getWindowsTitle());

        }
        if(e.getKeywords()!=null)
        {
            fNew.setIPTCKeywords(e.getKeywords()+";"+fNew.getIPTCKeywords());

        }
        if(e.getDescription()!=null)
        {
            fNew.setWindowsSubject(e.getDescription()+" "+fNew.getWindowsSubject());

        }
        if(location!=null)
        {
            Enums.processMode processMode=null;
            processMode=processForwardCodeFromLocation(config,fNew,existingCommentsString,location);
            if(processMode!=null)
            {
               addComment(existingCommentsString,processMode.toString());
            }
        }
        fNew.setEventKeys(fNew.getEventKeys()+e.getEventid()+";");
        addComment(existingCommentsString,Enums.processMode.event+":"+e.getEventid());

        return false;
    }
    public static Boolean updateLatLon(Double lat, Double lon,FileObject fNew,ConfigObject config)
    {
        if((fNew.getLatitude()==null && fNew.getLongitude()==null) || config.getOverwrite())
        {
            fNew.setLatitude(lat);
            fNew.setLongitude(lon);
            return true;
        }
        else
        {
            message("Will not overwrite existing Lat and Lon information for this file:");
            return false;
        }
    }
    /*
    updates the date in ExifOriginal - and also the BestDate...
     */
    public static Boolean updateDate(String instructions, FileObject fNew) {
        String param = getParam(instructions, "#"+Enums.processMode.date+":");
        if(param.length()<1)
        {
             return false;
        }
        LocalDateTime c =createLocalDate(param);
        if(c!=null) {
            fNew.setExifOriginal(c);
            fNew.setBestDate(fNew.getExifOriginal());
            countDriveDateUpdate++;
            countDateUpdate++;
            return true;
        }
        return false;
    }
    public static LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
    public static LocalDateTime convertToLocalDateTimeViaInstant(Date dateToConvert) {
        if(dateToConvert==null)
        {
            return null;
        }
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
    public static Date convertToDateViaInstant(LocalDateTime dateToConvert) {
        return java.util.Date
                .from(dateToConvert.atZone(ZoneId.systemDefault())
                        .toInstant());
    }
    public static Enums.processMode forwardCode(String instructions, ConfigObject config, FileObject fNew,ArrayList<String> existingCommentsString) {

            String param ;
            Enums.processMode p = testOptions(instructions);
            if(p==null)
            {
                return null;
            }
            if (p.equals(Enums.processMode.latlon)) {
                param = getParam(instructions, "#"+Enums.processMode.latlon+":");
                String[] values = param.split(",", -1);
                if (values.length == 2) {
                    try {
                        Double lat = Double.valueOf(values[0]);
                        Double lon = Double.valueOf(values[1]);
                        geocode(fNew, lat, lon, config,false);
                        // we should also set lat and lon if it is correct
                        if (fNew.getPlaceKey() != null) {
                            if(updateLatLon(lat,lon,fNew,config)) {
                                countAddedLATLONG++;
                                countDriveAddedLATLONG++;
                                addComment(existingCommentsString, p.toString());
                            }

                            return p;
                        }
                    } catch (Exception e) {
                        message("could not convert provided values to lat long:" + param);
                        addError(fNew.getFileName(),fNew.getDirectory(),fNew.getBestDate(),"could not convert provided values to lat, lon:"+param);
                    }
                } else {
                    message("Incorrect number of parameters for lat long:" + param);
                    addError(fNew.getFileName(),fNew.getDirectory(),fNew.getBestDate(),"Incorrect number of parameters for lat lon:"+param);
                }
                return null;
            } else if (p.equals(Enums.processMode.event)) {
                param = getParam(instructions, "#"+Enums.processMode.event+":");
                try {
                    EventObject e;
                    e= getEvent(Integer.valueOf(param));
                    if (e == null) {
                        message("This Event has not been found in the JSON - event:" + param);
                        addError(fNew.getFileName(),fNew.getDirectory(),fNew.getBestDate(),"Event not found for:"+param);
                    } else {
                        message("Event has been found - event:" + param);
                        if(checkEvent(config,fNew,e,existingCommentsString)>0)
                        {
                            countAddedEvent++;
                            countDriveAddedEvent++;
                        }
                        return p;
                    }
                } catch (Exception e) {
                    message("could not convert provided values to a place:" + param);
                    addError(fNew.getFileName(),fNew.getDirectory(),fNew.getBestDate(),"Could not convert provided values to a place:"+param);
                }
                return null;
            } else if (p.equals(Enums.processMode.place)) {
                param = getParam(instructions, "#"+Enums.processMode.place+":");
                try {
                    Place g;
                    g = getPlace(Integer.valueOf(param));
                    if (g == null) {
                        message("This place has not been added in the JSON - place:" + param);
                        addError(fNew.getFileName(),fNew.getDirectory(),fNew.getBestDate(),"This place does not exist in the JSON:"+param);
                    } else {
                        message("Place has been found - place:" + param);
                        if(updateLatLon(g.getLatAsDouble(),g.getLonAsDouble(),fNew,config)) {
                            fNew.setPlaceKey(g.getInternalKey());
                            setFileObjectGEOValues(fNew, g,false,config);
                            countAddedPlace++;
                            countDriveAddedPlace++;
                            addComment(existingCommentsString, p.toString());
                        }
                        return p;
                    }
                } catch (Exception e) {
                    message("could not convert provided values to a place:" + param);
                    addError(fNew.getFileName(),fNew.getDirectory(),fNew.getBestDate(),"Could not convert provided values to a place:"+param);
                }
                return null;
            } else if (p.equals(Enums.processMode.postcode)) {

                param = getParam(instructions, "#"+Enums.processMode.postcode+":");
                String[] values3 = param.split(",", -1);
                try {
                    if( config.getOpenAPIKey()!=null) {
                        String newLat = "";
                        if (values3.length > 1) {
                            newLat = checkPostCode(values3[0], config.getOpenAPIKey(), values3[1]);
                        } else if (values3.length == 1) {
                            newLat = checkPostCode(values3[0], config.getOpenAPIKey(), "GBR");
                        }
                        String[] values2 = newLat.split(",", -1);
                        Double lat = Double.valueOf(values2[0]);
                        Double lon = Double.valueOf(values2[1]);
                        Boolean result=geocode(fNew, lat, lon, config,false);
                        // we should also set lat and lon if it is correct
                        if (fNew.getPlaceKey() != null) {
                            if(updateLatLon(lat,lon,fNew,config)) {
                                countAddedPostcode++;
                                countDriveAddedPostcode++;
                                addComment(existingCommentsString, p.toString());
                            }
                            return p;
                        }
                        else
                        {
                            addError(fNew.getFileName(),fNew.getDirectory(),fNew.getBestDate(),"Could not find postcode:"+param);
                        }
                    }
                    else
                    {
                        addError(fNew.getFileName(),fNew.getDirectory(),fNew.getBestDate(),"API not available:"+param);
                    }
                } catch (Exception e) {
                    message("could not convert provided values to a postcode:" + param);
                }
                return null;
            }
            else
            {
                return null;
            }


    }
    public static void setFileObjectGEOValues(FileObject fNew,Place g,Boolean alreadyGeocoded, ConfigObject config)
    {
        fNew.setDisplayName(conditionallyUpdateGeoField(fNew.getDisplayName(),g.getDisplay_name(),"Display Name,",alreadyGeocoded,config));
        fNew.setCity(conditionallyUpdateGeoField(fNew.getCity(),g.getIPTCCity(),"City",alreadyGeocoded,config));
        fNew.setCountry_code(conditionallyUpdateGeoField(fNew.getCountry_code(),g.getIPTCCountryCode(),"Country Code",alreadyGeocoded,config));
        fNew.setCountry_name(conditionallyUpdateGeoField(fNew.getCountry_name(),g.getIPTCCountry(),"Country Name",alreadyGeocoded,config));
        fNew.setStateProvince(conditionallyUpdateGeoField(fNew.getStateProvince(),g.getIPTCStateProvince(),"State / Province",alreadyGeocoded,config));
        fNew.setSubLocation(conditionallyUpdateGeoField(fNew.getSubLocation(),g.getIPTCSublocation(),"Sub Location",alreadyGeocoded,config));
    }
    /**
     * @param fNew - this sets values on the new FileObject

     * @param jpegMetadata - existing values read from metadata
     * @param file - current file being processed
     */
    public static void setFileObjectValues(FileObject fNew,JpegImageMetadata jpegMetadata,File file)
    {
        if(jpegMetadata!=null)
        {
                        fNew.setWindowsComments(getStringOrUnknown(jpegMetadata, ExifTagConstants.EXIF_TAG_USER_COMMENT));
            fNew.setCameraMaker(getStringOrUnknown(jpegMetadata, TiffTagConstants.TIFF_TAG_MAKE));
            fNew.setCameraModel(getStringOrUnknown(jpegMetadata, TiffTagConstants.TIFF_TAG_MODEL));
            fNew.setFStop(getTagValueDouble(jpegMetadata, ExifTagConstants.EXIF_TAG_FNUMBER));
            fNew.setProgramName(getStringOrUnknown(jpegMetadata, ExifTagConstants.EXIF_TAG_SOFTWARE));
            fNew.setOrientation(getTagValueInteger(jpegMetadata, TiffTagConstants.TIFF_TAG_ORIENTATION));
            fNew.setExifOriginal(convertToLocalDateTimeViaInstant(getTagValueDate(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL)));
            fNew.setExifDigitised(convertToLocalDateTimeViaInstant(getTagValueDate(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED)));
            fNew.setTiffDate( convertToLocalDateTimeViaInstant(getTagValueDate(jpegMetadata, TiffTagConstants.TIFF_TAG_DATE_TIME)));
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
        fNew.setEventKeys(""); //set this to blank at start....
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
            fNew.setBestDate(convertToLocalDateTimeViaInstant(getFileDate(file)));
        }
        fNew.setFileName(file.getName());
        fNew.setFileSize(new BigDecimal(file.length()));
        fNew.setDirectory(FilenameUtils.getFullPath(file.getPath()));
        fNew.setFileKey(countProcessed);
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
            f.setFileModified(convertToLocalDateTimeViaInstant(new Date(attr.lastModifiedTime().toMillis())));
            f.setFileCreated(convertToLocalDateTimeViaInstant(new Date(attr.creationTime().toMillis())));

            message("System Creation Date/Time: " +  f.getFileCreated());
            f.setFileAccessed(convertToLocalDateTimeViaInstant(new Date(attr.lastAccessTime().toMillis())));
           // f.setWindowsComments(getWindowsMetadata(file,"WindowsXP Comment"));

        } catch (Exception e) {
            message("Could not read File Basic Attributes: " + e);
        }

    }
    public static boolean readIPTCdata(FileObject f, Metadata meta) {
        try {
            // some of these fields may legitimately be blank so we need to set to blank not null.
            f.setCountry_code("");
            f.setCity("");
            f.setStateProvince("");
            f.setCountry_name("");
            f.setSubLocation("");
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
                } else if (item.getKey().equals(IPTCApplicationTag.KEY_WORDS.getName())) {
                    if (item.getValue().length() > 0) {
                        f.setIPTCKeywords(item.getValue());
                        message("IPTC Keywords value is:" + item.getValue());
                    }
                } else if (item.getKey().equals(IPTCApplicationTag.SPECIAL_INSTRUCTIONS.getName())) {
                    if (item.getValue().length() > 0) {
                        f.setIPTCInstructions(item.getValue());
                        message("IPTC Instructions is:" + item.getValue());
                    }
                } else if (item.getKey().equals(IPTCApplicationTag.CAPTION_ABSTRACT.getName())) {
                    if (item.getValue().length() > 0) {
                        if (f.getWindowsSubject().length() < 1) {
                            f.setWindowsSubject(item.getValue());
                        } else {
                            message("title conflict" + item.getValue() + " : " + f.getWindowsSubject());
                        }
                        message("IPTC Description / Caption is:" + item.getValue());
                    }

                } else if (item.getKey().equals(IPTCApplicationTag.OBJECT_NAME.getName())) {
                    if (item.getValue().length() > 0) {
                        if (f.getWindowsTitle().length() < 1) {
                            f.setWindowsTitle(item.getValue());
                        } else {
                            message("title conflict" + item.getValue() + " : " + f.getWindowsTitle());
                        }
                        message("IPTC title is:" + item.getValue());
                    }
                }


            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public static String conditionallyUpdateGeoField(String currentValue, String newValue,String fieldName,Boolean alreadyGeocoded,ConfigObject config)
    {
        // we update field if:
        // 1. it has not already been geocoded and the existing field is empty OR
        // 2. overwrite is set (i.e. it will overwrite existing values) but not if it has  already been geocoded
        // 3. we have asked to force redo the geocoding

        // note the new value might be blank after geocoding....
        if (StringUtils.isNullOrEmpty(currentValue) && !alreadyGeocoded || (config.getOverwrite() && !alreadyGeocoded) || config.getRedoGeocode()) {
            if(!StringUtils.isNullOrEmpty(currentValue)) {
                message("New Value found and overwritten for:" + fieldName + " - " + newValue + "  , current value:" + currentValue);
            }
            else
            {
                message("New Value found and written for:" + fieldName + " - " + newValue + "  , current value:" + currentValue);
            }
            return newValue;
        }
        else
        {
            if(!StringUtils.isNullOrEmpty(currentValue)) {
                message("New Value not overwritten:" + fieldName + " - " + newValue + "  , current value:" + currentValue);
            }
            return currentValue;
        }
    }
    public static void updateFile(ConfigObject config,DriveObject drive, File file,ArrayList<String> existingCommentsString,IPTC iptc,JpegExif exif,Boolean alreadyGeocoded,FileObject fNew)
    {

        if(config.getUpdate()) {
            try {
                List<IPTCDataSet> iptcs = new ArrayList<>();
                iptcs.add(new IPTCDataSet(IPTCApplicationTag.COUNTRY_CODE, fNew.getCountry_code()));
                iptcs.add(new IPTCDataSet(IPTCApplicationTag.COUNTRY_NAME, fNew.getCountry_name()));
                iptcs.add(new IPTCDataSet(IPTCApplicationTag.PROVINCE_STATE, fNew.getStateProvince()));
                iptcs.add(new IPTCDataSet(IPTCApplicationTag.SUB_LOCATION, fNew.getSubLocation()));
                iptcs.add(new IPTCDataSet(IPTCApplicationTag.CITY, fNew.getCity()));

                if (!StringUtils.isNullOrEmpty(fNew.getIPTCCopyright()) || config.getOverwrite()) {
                    if (!StringUtils.isNullOrEmpty(drive.getIPTCCopyright())) {
                        iptcs.add(new IPTCDataSet(IPTCApplicationTag.COPYRIGHT_NOTICE, drive.getIPTCCopyright()));
                    }
                }

                ArrayList<String> newKeys = new ArrayList<>();
                if(drive.getIPTCKeywords()!=null) {
                    newKeys = new ArrayList<>(Arrays.asList(drive.getIPTCKeywords().split(";", -1)));
                }
                if(config.getNewdir()!=null)
                {
                   newKeys=joinKeys(newKeys,convertPathToKeywords(fNew.getDirectory(),drive.getStartdir()));
                }
                for(String n : newKeys) {
                    iptcs.add(new IPTCDataSet(IPTCApplicationTag.KEY_WORDS, n));
                }
                // we may update the title and description /caption /subject
                if (!StringUtils.isNullOrEmpty(fNew.getWindowsTitle()) || config.getOverwrite()) {
                    iptcs.add(new IPTCDataSet(IPTCApplicationTag.OBJECT_NAME,fNew.getWindowsTitle() ));
                }
                if (!StringUtils.isNullOrEmpty(fNew.getWindowsSubject()) || config.getOverwrite()) {
                     iptcs.add(new IPTCDataSet(IPTCApplicationTag.CAPTION_ABSTRACT, fNew.getWindowsSubject()));
                }
                // we update the Special Instructions if they have been provided....
                if (!StringUtils.isNullOrEmpty(fNew.getIPTCInstructions()) || config.getOverwrite()) {
                    iptcs.add(new IPTCDataSet(IPTCApplicationTag.SPECIAL_INSTRUCTIONS, fNew.getIPTCInstructions()));
                }
                FileInputStream fin = new FileInputStream(file.getPath());
                String fout_name = FilenameUtils.getFullPath(file.getPath()) + "out" + FilenameUtils.getName(file.getPath());
                File outFile = new File(fout_name);
                FileOutputStream fout = new FileOutputStream(outFile, false);
                List<Metadata> metaList = new ArrayList<>();
                DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                String newDirectory=null;
                if(config.getNewdir()!=null) {
                    newDirectory = getNewDirectory(config,fNew);
                    File newFile = new File(newDirectory + "/" + fNew.getFileName());
                    if (!newFile.exists()) {
                        addComment(existingCommentsString, "Moved file from:" + fNew.getDirectory());
                        fNew.setWindowsComments(fNew.getWindowsComments()+"Moved file from:"+ fNew.getDirectory());
                    }
                    else
                    {
                        newDirectory=null;
                    }
                }
                if(fNew.getPlaceKey()!=null)
                {
                    fNew.setWindowsComments(fNew.getWindowsComments()+"Geocoded:");
                }
                if(fNew.getEventKeys().length()>0)
                {
                    fNew.setWindowsComments(fNew.getWindowsComments()+"Event:"+ fNew.getDirectory());
                }
             //   exif.addImageField(TiffTag.WINDOWS_XP_TITLE,FieldType.WINDOWSXP,fNew.getWindowsTitle()+"this is the title");
              //  exif.addImageField(TiffTag.WINDOWS_XP_SUBJECT,FieldType.WINDOWSXP,fNew.getWindowsSubject()+"this is te subject");
                iptcs.add(new IPTCDataSet(IPTCApplicationTag.OBJECT_NAME, fNew.getWindowsTitle()));

                exif.addImageField(TiffTag.WINDOWS_XP_COMMENT,FieldType.WINDOWSXP,fNew.getWindowsComments());
                exif.addExifField(ExifTag.DATE_TIME_ORIGINAL, FieldType.ASCII,formatter.format( convertToDateViaInstant(fNew.getBestDate())));

                metaList.add(exif);
                iptc.addDataSets(iptcs);
                metaList.add(iptc);

                metaList.add(new Comments(existingCommentsString));
                Metadata.insertMetadata(metaList, fin, fout);
                fin.close();
                fout.close();
                if (!file.delete()) {
                    message("Cannot delete file:" + file.getPath());
                }
                Files.copy(outFile.toPath(), file.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
                if (!outFile.delete()) {
                    message("Cannot delete file:" + outFile.getPath());
                }
                File finalFile=file;
                if(newDirectory!=null)
                {
                    String ok=moveFile(config,fNew);
                    if(ok!=null) {
                        finalFile = new File(newDirectory + "/" + fNew.getFileName());
                    }
                    else
                    {
                        message("Did not move file"+fNew.getFileName());
                    }
                }
                Files.setAttribute(finalFile.toPath(), "creationTime", FileTime.fromMillis(convertToDateViaInstant(fNew.getFileCreated()).getTime()));
                Files.setAttribute(finalFile.toPath(), "lastAccessTime", FileTime.fromMillis(convertToDateViaInstant(fNew.getFileAccessed()).getTime()));
                Files.setAttribute(finalFile.toPath(), "lastModifiedTime", FileTime.fromMillis(convertToDateViaInstant(fNew.getFileModified()).getTime()));

                countUPDATED++;
                countDriveUPDATED++;
            } catch (Exception e) {
                message("Cannot update File"+e);
            }
        }
    }
    private static String updateInstructions(String s,Enums.processMode pMode)
    {
        if(pMode==null)
        {
            return s;
        }
        if(pMode.equals(Enums.processMode.geocode)) {
            s=s + "#"+pMode+ "DONE:";
        }
        else
        {
            int startPoint=s.toLowerCase().indexOf("#"+pMode+":");
            s=s.substring(0,startPoint+1+pMode.toString().length())+"DONE"+
                    s.substring(startPoint+1+pMode.toString().length());
            System.out.println("Updated instruction is:"+s);
        }
        return s;
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
    public static String getParam(String searchString,String keyValue) {
        if(searchString==null)
        {
            return "";
        }
        int startPoint = searchString.toLowerCase().indexOf(keyValue.toLowerCase());
        if(startPoint<0)
        {
            return "";
        }
        int endPoint = searchString.indexOf(" ", startPoint + 1);
        if (endPoint > -1) {
            return searchString.substring(startPoint+keyValue.length(), endPoint - 1);
        } else {
            return searchString.substring(startPoint+keyValue.length());
        }
    }
    /**
     *  Reads metadata
     *  THIS EXAMPLE LARGELY COPIED FROM ICAFE SAMPLE CODE
     * @param file - file to read
     * @return - either true (sucessful) or false
     */
    public static String getWindowsMetadata(File file,String keyValue) {
        try {
            Map<MetadataType, Metadata> metadataMap = Metadata.readMetadata(file.getPath());
            for (Map.Entry<MetadataType, Metadata> entry : metadataMap.entrySet()) {
                    for (MetadataEntry item : entry.getValue()) {
                       String s= findMetadata(item,keyValue);
                       if(s.length()>0)
                       {
                           return s;
                       }
                    }

            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
    public static void checkEvents()
    {
        Iterator<EventObject> i = events.iterator();
        EventObject e;
        while (i.hasNext()) {

            e = (EventObject) i.next();
            if(e.getEventdate()==null && e.getEventcalendar()==null )
            {
                message("Event calendar or event date must be specified - event :"+e.getEventid()+" "+e.getTitle());
                addError("-","-",null,"Error in Event ID: "+e.getEventid() +"- Calendar or Event date not specified");
                i.remove();
            }
            else {


                    if (e.getEventcalendar() != null) {
                        // we have a valid calendar
                        // alendar so set start and end times
                        LocalDateTime d = createLocalDateCalendar(e.getEventcalendar());

                        if(d!=null) {
                            e.setExactStartTime(d);
                            e.setExactEndTime( d.plusDays(1).minusNanos(1));
                            if(e.getEventtime()!=null)
                            {
                                e.setExactStartTime(e.getExactStartTime().plusHours(e.getEventtime().getHour()));
                                e.setExactStartTime( e.getExactStartTime().plusMinutes(e.getEventtime().getMinute()));
                                e.setExactStartTime( e.getExactStartTime().plusSeconds(e.getEventtime().getSecond()));
                                e.setExactStartTime( e.getExactStartTime().plusNanos(e.getEventtime().getNano()));
                            }
                            if(e.getEndtime()!=null)
                            {
                                e.setExactEndTime(e.getExactEndTime().plusHours(e.getEventtime().getHour()));
                                e.setExactEndTime(e.getExactEndTime().plusMinutes(e.getEventtime().getMinute()));
                                e.setExactEndTime(e.getExactEndTime().plusSeconds(e.getEventtime().getSecond()));
                                e.setExactEndTime(e.getExactEndTime().plusNanos(e.getEventtime().getNano()));
                            }
                        }
                        else
                        {
                            addError("-","-",null,"Error in Event ID: "+e.getEventid() +"-cannot parse event");
                            message("cannot parse date for event"+e.getEventid());
                            i.remove();
                        }
                    }
                    else
                    {
                        //set the start date and time
                        e.setExactStartTime(e.getEventdate().atStartOfDay());
                        //set the end date and time
                        e.setExactEndTime(e.getEventdate().atStartOfDay().plusDays(1).minusNanos(1));
                        //if start time is present, then adjust
                        if(e.getEventtime()!=null)
                        {
                           e.setExactStartTime(e.getExactStartTime().plusHours(e.getEventtime().getHour()));
                           e.setExactStartTime( e.getExactStartTime().plusMinutes(e.getEventtime().getMinute()));
                           e.setExactStartTime( e.getExactStartTime().plusSeconds(e.getEventtime().getSecond()));
                           e.setExactStartTime( e.getExactStartTime().plusNanos(e.getEventtime().getNano()));
                        }
                        //if end date is provided then modify to the end of the day
                        if(e.getEnddate()!=null)
                        {
                            e.setExactEndTime(e.getEnddate().atStartOfDay().plusDays(1).minusNanos(1));
                        }
                        if(e.getEndtime()!=null)
                        {
                            e.setExactEndTime(e.getExactEndTime().plusHours(e.getEventtime().getHour()));
                            e.setExactEndTime(e.getExactEndTime().plusMinutes(e.getEventtime().getMinute()));
                            e.setExactEndTime(e.getExactEndTime().plusSeconds(e.getEventtime().getSecond()));
                            e.setExactEndTime(e.getExactEndTime().plusNanos(e.getEventtime().getNano()));
                        }
                    }

            }
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
      */
    public static void setDrives(ConfigObject config,String rootDrive)
    {

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
     //   d.setExcludespec(es);
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
            countDriveFiles = 0;
            countDriveImages = 0;
            countDriveTooSmall = 0;
            countDriveProcessed = 0;
            countDriveLATLONG=0;
            countDriveALREADYGEOCODED=0;
            countDriveGEOCODED = 0;
            countDriveNOTGEOCODED=0;
            countDriveDateUpdate=0;
            countDriveEventsFound=0;
            countDriveAddedPlace=0;
            countDriveAddedLATLONG=0;
            countDriveAddedPostcode=0;
            countDriveAddedEvent=0;
            countDriveUPDATED=0;
            countDriveErrors=0;
            message("Reading drive: "+d.getStartdir());

            //recursively find all files
            readDirectoryContents(new File(d.getStartdir()), d, c.getTempdir(), c);
            messageLine("-");
            String ex=" on drive "+d.getStartdir()+" :";
            message("Files found                        "+ex + countDriveFiles);
            message("Photos found                       "+ex + countDriveImages);
            message("Photos too small                   "+ex + countDriveTooSmall);
            message("Photos processed                   "+ex + countDriveProcessed);
            message("Photos with Lat Long               "+ex + countDriveLATLONG);
            message("Photos already Geocoded            "+ex + countDriveALREADYGEOCODED);
            message("Photos Geocoded                    "+ex + countDriveGEOCODED);
            message("Photos with failed Geocoding       "+ex + countDriveNOTGEOCODED);
            message("Photos where date added            "+ex + countDriveDateUpdate);
            message("Photos where Events found from date"+ex + countDriveEventsFound);
            message("Photos with Place added            "+ex + countDriveAddedPlace);
            message("Photos with Lat Lon added          "+ex + countDriveAddedLATLONG);
            message("Photos with Postcode added         "+ex + countDriveAddedPostcode);
            message("Photos with Event added            "+ex + countDriveAddedEvent);
            message("Photos updated                     "+ex + countDriveUPDATED);
            message("Photos with Errors in processing   "+ex + countDriveErrors);
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
    private static String findMetadata(MetadataEntry entry, String keyValue) {
       String foundValue;
        if (entry.isMetadataEntryGroup()) {

            Collection<MetadataEntry> entries = entry.getMetadataEntries();
            for (MetadataEntry e : entries) {
                foundValue=findMetadata(e, keyValue);
                if(foundValue.length()>0)
                {
                    return foundValue;
                }
            }
            return "";
        }
        else
        {
            System.out.println("FIND:"+entry.getKey()+"Value:"+entry.getValue()+", Key value"+keyValue);
            if(entry.getKey().contains(keyValue))
            {
                return entry.getValue();
            }
            return "";
        }
    }
}
