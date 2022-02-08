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
import no.api.freemarker.java8.Java8ObjectWrapper;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.MicrosoftTagConstants;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.apache.commons.io.FileUtils;
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
import java.time.format.DateTimeFormatter;
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
        Path configFileName=null;


        try {
            if(FilenameUtils.getExtension(args[0]).equalsIgnoreCase("json"))
            {
                configFileName = Path.of(args[0]);
                message("json file present:"+configFileName);
                config = readConfig(configFileName.toString());
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
                    configFileName=Path.of(args[0]+"\\"+jsonDefault);
                    message("Default Config File:"+configFileName);
                    message("Temporary Directory:"+args[1]);

                }
                else
                {
                    message("Please provide at least two arguments, for the Root directory for searching and the temporary output directory");
                }
            }
            //
            readRestOfArgs(config, args);
            setDefaults(Objects.requireNonNull(config),z);
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
            if(!exportConfig(config,configFileName))
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
            else if(s.trim().equalsIgnoreCase(Enums.argOptions.overwrite.toString()))
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
    public static void setFileAttributesForTest(String fileName,String dateParam)
    {
        File file = new File(fileName);
        LocalDateTime d=  createLocalDate(dateParam);
        try {
            Files.setAttribute(file.toPath(), "creationTime", FileTime.fromMillis(convertToDateViaInstant(d).getTime()));
            Files.setAttribute(file.toPath(), "lastAccessTime", FileTime.fromMillis(convertToDateViaInstant(d).getTime()));
            Files.setAttribute(file.toPath(), "lastModifiedTime", FileTime.fromMillis(convertToDateViaInstant(d).getTime()));
        }
        catch(Exception e)
        {

        }
    }
    /**
     * Sets the defaults if the values are not in the JSON (note this is pass by value - and we can change values)
     * @param config - passes ConfigObject
     */
    public static void setDefaults(ConfigObject config,ZoneId z)
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
        message("Number of Events found:"+events.size());
        message("Number of Tracks found:"+tracks.size());
        message("Number of Errors:"+errorObjects.size());
        message("Number of Duplicates found:"+duplicateObjects.size());
        messageLine("*");

    }

    /**
     *  Reads JPEG metadata and updates fNew object with values - this uses Apache Imaging
     *  If there is no metadata, then default values are added
     * @param file - file path specification
     * @param fNew - FileObject to be updated
     */
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
                            System.out.println(countFiles+":"+countDriveFiles+":"+file.getName());
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
                                        FileObject fNew=processFile(file, thumbName,config, drive,false);
                                        if(fNew==null)
                                        {
                                            countErrors++;
                                            countDriveErrors++;
                                            message("Could not process file:"+file.getCanonicalPath());
                                        }
                                        else {
                                            fileObjects.add(fNew);
                                        }
                                        if(config.getShowmetadata() && config.getUpdate()) {
                                            messageLine("~");
                                            if (!readMetadata(new File(fNew.getDirectory()+"/"+fNew.getFileName()))) {
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

                s = addWithCommaIfNotNull(s, g.getAddress().getCountry_code().toUpperCase());
            }
            if (f.equals("country")) {
                s = addWithCommaIfNotNull(s, g.getAddress().getCountry());
            }
            if (f.equals("county")) {
                s = addWithCommaIfNotNull(s, g.getAddress().getCounty());
            }
            if (f.equals("city")) {
                s = addWithCommaIfNotNull(s, g.getAddress().getCity());
            }
            if (f.equals("postcode")) {
                s = addWithCommaIfNotNull(s, g.getAddress().getPostcode());
            }
            if (f.equals("road")) {
                s = addWithCommaIfNotNull(s, g.getAddress().getRoad());
            }
            if (f.equals("house_number")) {
                s = addWithCommaIfNotNull(s, g.getAddress().getHouse_number());
            }
            if (f.equals("state")) {
                s = addWithCommaIfNotNull(s, g.getAddress().getState());
            }
            if (f.equals("state_district")) {
                s = addWithCommaIfNotNull(s, g.getAddress().getState_district());
            }
            if (f.equals("village")) {
                s = addWithCommaIfNotNull(s, g.getAddress().getVillage());
            }
            if (f.equals("hamlet")) {
                s = addWithCommaIfNotNull(s, g.getAddress().getHamlet());
            }
            if (f.equals("postcode")) {
                s = addWithCommaIfNotNull(s, g.getAddress().getPostcode());
            }
            if (f.equals("town")) {
                s = addWithCommaIfNotNull(s, g.getAddress().getTown());
            }
            if (f.equals("suburb")) {
                s = addWithCommaIfNotNull(s, g.getAddress().getSuburb());
            }
            if (f.equals("amenity")) {
                s = addWithCommaIfNotNull(s, g.getAddress().getAmenity());
            }
            if (f.equals("city_district")) {
                s = addWithCommaIfNotNull(s, g.getAddress().getCity_district());
            }
            if (f.equals("leisure")) {
                s = addWithCommaIfNotNull(s, g.getAddress().getLeisure());
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
     * adds a space and a string value to a string (checks nulls). If the same, then do not concatenate
     * @param s - string to add to = this is either blank or a value
     * @param valString - new value - it can be null;
     * @return - modified s (or existing value if null)
     */
    public static String addNotNull(String s, String valString) {
        if(s==null)
        {
            return valString;
        }
        if (valString != null) {
            if(s.equals(valString))
            {
                return s;
            }
           String newS= s + " "+ valString ;
           return newS.trim();
        }
        return s;
    }
    /**
     * adds a comma and a value to a string
     * @param s - string to add to = this is either blank or a value ending in a comma (never null)
     * @param valString - new value - it can be null;
     * @return - modified s (or existing value if null)
     */
    public static String addWithCommaIfNotNull(String s, String valString) {
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
                                s.append(getLink(root,f));

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
                        if (f.getPlaceKey().equals(r.getPlaceid())) {
                          s.append(getLink(root,f));
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
    public static String getLink(String root,FileObject f)
    {
        StringBuilder s= new StringBuilder();
        if (!(f.getOrientation() == 8 || f.getOrientation() == 6)) {
            s.append(" <div class=\"item\">");
            //      s.append("<img src=\"").append(root).append("\\").append(f.getThumbnail()).append("\" width=\"").append(width).append("\"  >");
            s.append("<img src=\"").append(root).append("\\").append(f.getThumbnail()).append("\" >");
            s.append("<span class=\"caption\"><small>").append(f.getDirectory()).append("</small><br>");
            s.append("<small>").append(f.getFileName()).append("</small>");
            s.append("<small>").append("["+f.getOrientation()+"]").append("</small></span>");
            s.append("</div>");
        } else {
            // Integer newWidth = width * f.getHeight() / f.getWidth();

            s.append(" <div class=\"item\">");
            //  s.append("<img  src=\"").append(root).append("\\").append(f.getThumbnail()).append("\" width=\"").append(newWidth).append("\"  >");
            s.append("<img  src=\"").append(root).append("\\").append(f.getThumbnail()).append("\"  >");
            s.append("<span class=\"caption\"><small>").append(f.getDirectory()).append("</small></br>");
            s.append("<small>").append(f.getFileName()).append("</small>");
            s.append("<small>").append("["+f.getOrientation()+"]").append("</small></span>");
            s.append("</div>");
        }
        return s.toString();


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
                                s.append(getLink(root,f));

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
            newKey= places.get(places.size()-1).getPlaceid()+1;
        }
        g.setPlaceid(newKey);
        g.setEndDate(bestDate);
        g.setStartDate(bestDate);
        places.add(g);
        return newKey;
    }
    private static String formatIPTCdate(LocalDateTime d)
    {
        DateTimeFormatter formatter = DateTimeFormatter.BASIC_ISO_DATE;
        String formattedDate = formatter.format(d);
        System.out.println(formattedDate);
        return formattedDate;
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
    public static String findJSONFile(File startDir)
    {
        LocalDateTime d=LocalDateTime.now();
        String newestFile="";
        File[] files = startDir.listFiles();
        if(files!=null) {
            for (File file : files) {
                try {
                    if (!file.isDirectory()) {

                        if(FilenameUtils.getExtension(file.getPath()).equalsIgnoreCase("json"))
                        {
                             try {
                                String fileName = FilenameUtils.getBaseName(file.getName());
                                if(fileName.length()>14)
                                {
                                   DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                                   if(newestFile.length()<1)
                                   {
                                       System.out.println("date to convert:"+fileName.substring(fileName.length()-14));

                                       //some dates have slashes instead of colons
                                       d= LocalDateTime.parse(fileName.substring(fileName.length()-14),formatter);
                                       newestFile=file.getName();
                                   }
                                   else
                                   {
                                       LocalDateTime dd= LocalDateTime.parse(fileName.substring(fileName.length()-14));
                                       if(dd.isAfter(d))
                                       {
                                           d=dd;
                                           newestFile=file.getName();
                                       }
                                   }
                                }
                            }
                            catch(Exception e)
                            {
                                message("Could not parse JSON file for Date"+file.getPath());
                            }
                        }
                    }
                }
                catch(Exception e)
                {
                    message("Could not find JSON file");
                    return "";
                }
            }
        }
        return newestFile;
    }
    /**
     * This is only used for testing - renames image files by adding a prefix "T_"
     * This is because Lightroom will not see a file as a new file if it has the same name but in a different
     * directory.
     * @param startDir - directory to start the renaming
     * @return - true if successful
     */
    public static boolean renameFiles(File startDir)
    {
        File[] files = startDir.listFiles();
        if(files!=null) {
            for (File file : files) {
                try {

                    if (file.isDirectory()) {
                        //message("directory:" + file.getCanonicalPath());

                        if (!file.getCanonicalPath().equals(startDir.getCanonicalPath())) {
                            renameFiles(file);

                        }

                    } else {
                        System.out.println(file.getCanonicalPath());
                        System.out.println(file.getPath());
                        System.out.println(file.getParent());
                        //dont rename json file !!
                        if(!FilenameUtils.getExtension(file.getPath()).equalsIgnoreCase("json") &&
                                !FilenameUtils.getExtension(file.getPath()).equalsIgnoreCase("db")
                        )
                        {
                            boolean result = file.renameTo(new File(file.getParent() + "/" + "T_" + file.getName()));
                            if (!result) {
                                message("Could not rename file:" + file.getAbsolutePath());
                                return false;
                            }
                        }
                    }
                }
                catch(Exception e)
                {
                    message("Renaming files error"+e);
                    return false;
                }


            }
        }
        return true;
    }
    /**
     *  Only used for testing - copies files from a TestSource subdirectory to a Test directory , and then renames
     *  start at Start dir - and move to copyDir
     * @param startDir - TestSource directory
     * @param copyDir - Test (destination) directory
     */
    public static boolean copyToTestArea(String startDir, String copyDir)
    {
        try {
               FileUtils.copyDirectory(new File(startDir), new File(copyDir));
            }
            catch(Exception e)
            {
                message("Could not copy test directory"+e);
                return false;
            }
        return renameFiles(new File(copyDir));
    }
    /**
     * This is only used for testing. Clears the test area of three subdirectories:
     * /Test , /TestRESULTS and /TestNewDir
     * @param rootDir - root directory for clearing the folders
     */
    public static boolean clearTestArea(String rootDir)
    {
        File rootFile = new File(rootDir);
        if(rootFile.exists())
        {
               try {
                    FileUtils.cleanDirectory(new File(rootDir + "/Test"));
                }
                catch(Exception e) {
                    message("Test folder is missing");
                }
                try {
                    FileUtils.cleanDirectory(new File(rootDir + "/TestRESULTS"));
                }
                catch(Exception e) {
                    message("TestResults folder is missing");
                }
                try {

                    FileUtils.cleanDirectory(new File(rootDir + "/TestNewDir"));
                }
                catch(Exception e) {
                    message("TestNewDir folder is missing");
                }
                return true;
        }
        else
        {
            message("Clear Test Area - Root Directory does not exist:"+ rootDir );
            return false;
        }
    }
    public static void addComment(List<String> existingCommentsString,String newComment)
    {
        DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
        existingCommentsString.add(newComment+":" + formatter.format(new Date()));
    }
    /**
     * Adds a new camera if it does not exist in the array and sets start and end date
     * If it is not a new camera, start and/or end date are updated in the ArrayList only
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
                        return c.getCameraid();
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
                        return c.getCameraid();
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
            newKey=cameras.get(cameras.size()-1).getCameraid()+1;
        }
        CameraObject cNew = new CameraObject();
        cNew.setCameraid(newKey);
        cNew.setCameramaker(make);
        cNew.setCameramodel(model);
        cNew.setStartdate(d);
        cNew.setEnddate(d);
        cNew.setCameracount(1);
        cameras.add(cNew);
        return newKey;
    }

    /**
     * Goes through existing JPEG comments looking for a string - this is to prevent redoing the geocoding for instance
     * @param existingComments - array of Comments
     * @param test - string to look for
     * @return - either true if found or false
     */
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
     *  Checks for duplicate filenames and adds to the duplicate list if found
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
     * The test is whether the object is within a certain distance of the object being checked using distance_Between_LatLong
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

    /**
     * Creates a LocalDateTime from a string whith can be YYYY or YYYY-MM  or YYYY-MM-DD
     * @param param - string to convert
     * @return - returns LocalDateTime or null if there is an error
     */
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

    /**
     * Creates a LocalDateTime from a string which can be MM-DD or YYYY-MM-DD
     * We are not interested in the Year for this test e g. a birthday
     * @param param - string to convert
     * @return - returns LocalDateTime or null if there is an error
     */
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
           // message("Error creating new output directory - it may already exist for: "+temp);
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
     *
     * finds highest placeid if it has not been filled in - it may be zero
     * @return
     */
    public static Integer getHighestPlace()
    {
        Integer highest=0;
        for(Place g : places)
        {
            if(g.getPlaceid()!=null) {
                if (g.getPlaceid() > highest) {
                    highest = g.getPlaceid();
                }
            }
        }

        return highest;
    }
    /**
     * Reads any existing cameras, places or events if in the json file
     * Sorts cameras and places and sets any counters to zero e.g. places and cameras
     * Checks if the events have correct dates specified and removes ifg they do not
     * @param c - Configuration Object
     */
    public static void readConfigLists(ConfigObject c)
    {

        if(c.getCameras()!=null)
        {
            message("NUMBER OF CAMERAS READ FROM CONFIG FILE:"+c.getCameras().size());
            cameras=c.getCameras();
            //sort in case there are gaps in numbering
            cameras.sort(Comparator.comparing(CameraObject::getCameraid));
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
            places.sort(Comparator.comparing(Place::getPlaceid));
            int highestPlace=getHighestPlace();
            for(Place g : places)
            {
                g.setCountPlace(0);
                if(g.getPlaceid()==null)
                {
                    highestPlace++;
                    g.setPlaceid(highestPlace);
                }
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

    /**
     * Identifies new directory for a file based on the file date (YYYY and MM) and then creates the directory
     * @param config - COnfiguration Object
     * @param f - filename
     * @return - name of new directory
     */
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
                if(!renameResultthumb)
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
     * Constructs a new thumbnail name replacing slashes and colon with underscores - as we need to create a valid file name
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
     * @param drive - drive object
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

    /**
     * Replaces back slashes with forward slashes for consistency of file names on all platforms
     * @param s - string to change
     * @return - return string
     */
    public static String fixSlash(String s)

    {
        return s.replace("\\","/");
    }

    /**
     * Adds to a list of keys to an existing list
     * @param current String array list of Keys
     * @param newKeys - new keys to add
     */
    public static void joinKeys(ArrayList<String> current,ArrayList<String> newKeys)
    {
        for(String n : newKeys)
        {
            current.add(n);
        }
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
            if(r.getPlaceid().equals(i))
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

    /**
     * Checks a File Object's dates against an event date
     * @param config - Config Object
     * @param fNew - File Object
     * @param e - Event OBject (providing dates)
     * @param existingCommentsString - JPEG comments QArray
     * @return - returns 1 if found a date
     */
    public static Integer checkEvent(ConfigObject config,FileObject fNew,EventObject e,ArrayList<String> existingCommentsString)
    {
        LocalDateTime d= fNew.getBestDate();
        if(checkEventKey(fNew.getEventKeys(),e.getEventid()))
        {
            return 0;
        }
        if(!checkIPTCComments(existingCommentsString, "#Event:" + e.getEventid() + "DONE:") || config.getOverwrite()) {
            //event date
            if (e.eventcalendar != null) {
                if (d.getMonth() == e.getExactStartTime().getMonth() && d.getDayOfMonth() == e.getExactStartTime().getDayOfMonth()) {

                    updateEvent(config, fNew, e, existingCommentsString);
                    message("Event calendar match for event:" + e.getEventid() + " " + e.getTitle());

                    countEventsFound++;
                    countDriveEventsFound++;
                    return 1;

                }
            } else {
                if ((d.isAfter(e.getExactStartTime()) || d.isEqual(e.getExactStartTime()))
                        &&
                        (d.isBefore(e.getExactEndTime()) || d.isEqual(e.getExactEndTime()))
                ) {
                    // we have a match... so process..
                    message("Event date match for event:" + e.getEventid() + " " + e.getTitle());
                    updateEvent(config, fNew, e, existingCommentsString);
                    countEventsFound++;
                    countDriveEventsFound++;
                    return 1;
                  }
            }
        }
        return 0;
    }

    /**
     * Processes all events for a FileObject
     * @param config - Config Object
     * @param fNew - File Object
     * @param existingCommentsString - JPEG Comments Array
     * @return - number of Events found for this FileObject
     */
    public static Integer processEvents(ConfigObject config,FileObject fNew,ArrayList<String> existingCommentsString)
    {
        int eventFound=0;

        for(EventObject e : events)
        {
           eventFound=eventFound+checkEvent(config,fNew,e,existingCommentsString);
        }
        if(eventFound>0) {
            message("NUmber of Events found :"+eventFound);
        }
        return eventFound;

    }
    /**
     * Looks for Date instructions and changes the dates on the FileObject as a result
     * @param fNew - File Object
     * @param existingCommentsString - Existing comments
     * @return - either true (if processed) or false
     */
    public static LocalDateTime processDates(FileObject fNew,ArrayList<String> existingCommentsString)
    {
        LocalDateTime dateUpdated=null;
        String param=getInstructionFromEitherField(fNew,Enums.processMode.date);
        if(param.length()>0) {
            dateUpdated = updateDate(param, fNew);
        }
        if(dateUpdated!=null)
        {
            updateBothFields(fNew,param,Enums.processMode.date,existingCommentsString);
        }
        return dateUpdated;
    }
    /**
     * Creates a FileObject and Updates the file metadata (or displays information only, depending on options chosen)
     * Reads ICAFE metadata objects to use when writing back values
     * @param file - File to process
     * @param thumbName - name of theumbnail
     * @param config - Configuration Object
     * @param drive - Drive name
     * @return - returns true if successful, false if failure
     */
    public static FileObject processFile(File file, String thumbName,ConfigObject config, DriveObject drive,boolean readOnly) {
        FileObject fNew = new FileObject();
   /*     File blankFile = new File(FilenameUtils.getFullPathNoEndSeparator(file.getAbsolutePath())+"/"+"blank"+file.getName());


        try {
         //   updateWindowsFields(file, blankFile);

        }
catch(Exception e)
{
    System.out.println("e"+e);
}

    */
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
                    fNew.setWindowsComments(exif.getImageIFD().getFieldAsString(ExifTag.WINDOWS_XP_COMMENT));
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
                    if(!readIPTCdata(fNew,meta)){
                        message("Cannot read IPTC data");
                        addError(fNew.getFileName(),fNew.getDirectory(),fNew.getBestDate(),"Could not read UIPTC data");
                    }
                }
            }
        } catch (Exception e) {
            message("error reading metadata" + e);
            return null;
        }
        readJPEGMetadata(file, fNew);
        //create thumbnail and update FileObject
        if(!readOnly) {
            fNew.setThumbnail(createThumbFromPicture(file, config.getTempdir(), thumbName, config.getWidth(), config.getHeight(), fNew.getOrientation()));
            // Geocodes if lat and long present
            LocalDateTime dateUpdated = processDates(fNew, existingCommentsString);
            if (dateUpdated != null) {
                message("File updated with a new date:" + dateUpdated);
            }
            if (fNew.getLatitude() != null && fNew.getLongitude() != null) {
                geocodeLatLong(alreadyGeocoded, config, fNew, existingCommentsString);
            }
            Boolean forwardUpdated = forwardCode(config, fNew, existingCommentsString, null);
            Integer eventsUpdated = processEvents(config, fNew, existingCommentsString);
            updateFile(config, drive, file, existingCommentsString, iptc, exif, alreadyGeocoded, fNew);
        }
        return fNew;
    }
    /**
     * Decides whether to Reverse Geocodes a file object
     * @param alreadyGeocoded - flag wshows whether it has already been geocoded
     * @param config - Config Object
     * @param fNew - File Object
     * @param existingCommentsString - JPEG Comments String
     */
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

    /**
     * Revers4e Geocodes an Object
     * @param fNew - file Object
     * @param lat - latitude
     * @param lon - longitude
     * @param config - Config Object
     * @param alreadyGeocoded - flag if already geocoded
     * @return - true or false , if successful
     */
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
            message("Found Lat / Long in cache : [" + g.getPlaceid() + "]" + g.getDisplay_name());
            fNew.setPlaceKey(g.getPlaceid());
        }
        if (g != null) {
            setFileObjectGEOValues(fNew, g,alreadyGeocoded,config);
            countGEOCODED++;
            countDriveGEOCODED++;
            return true;
        }
        return false;
     }
    public static ArrayList<String> convertPathToKeywords(String dir,String root)
    {
        // change all slashes to forward
        // remove root and then replace slashes with spaces and return an array of values
        ArrayList<String> keys = new ArrayList<>();
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

    /**
     * UPdate file Object if event information has been found
     * @param config - Config Object
     * @param fNew - File Object
     * @param e - Event object
     * @param existingCommentsString - JPEG Comments String
     */
    public static void updateEvent(ConfigObject config, FileObject fNew, EventObject e,ArrayList<String> existingCommentsString)
    {
       fNew.setWindowsTitle(addNotNull(fNew.getWindowsTitle(),e.getTitle()));
       fNew.setIPTCKeywords(addNotNull(fNew.getIPTCKeywords(),e.getKeywords()));
       fNew.setWindowsSubject(addNotNull(fNew.getWindowsSubject(),e.getDescription()));
       if(e.getLocation()!=null)
       {
            // sets the Windows Comment to the value in order to do forward processing...
             fNew.setWindowsComments(fNew.getWindowsComments()+e.getLocation());
             if(forwardCode(config,fNew,existingCommentsString,e.getLocation()))
             {
                 message("Forward geocoding completed from Event");
             }
        }
        fNew.setEventKeys(fNew.getEventKeys()+e.getEventid()+";");
        updateBothFields(fNew,e.getEventid().toString(),Enums.processMode.event,existingCommentsString);

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
    public static String getInstructionFromEitherField(FileObject fNew, Enums.processMode processMode)
    {
        String param = getParam(fNew.getWindowsComments(), "#"+processMode+":");
        if(param.length()>0)
        {
            return param;
        }
        param = getParam(fNew.getIPTCInstructions(), "#"+processMode+":");
        if(param.length()>0)
        {
            return param;
        }
        return "";
    }
    /*
    updates the date in ExifOriginal - and also the BestDate...
     */
    public static LocalDateTime updateDate(String param, FileObject fNew) {
        LocalDateTime c =createLocalDate(param);
        if(c!=null) {
            fNew.setExifOriginal(c);
            fNew.setBestDate(fNew.getExifOriginal());
            countDriveDateUpdate++;
            countDateUpdate++;

        }
        return c;
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
    public static Boolean forwardCode(ConfigObject config, FileObject fNew,ArrayList<String> existingCommentsString,String eventLocation) {

         String param ;
         boolean paramFound=false;
         for(Enums.processMode p :Enums.processMode.values()) {
             // this is a special case where the forwardcode is driven from event location field
             if(eventLocation!=null)
             {
                 param = getParam(eventLocation, "#" + p + ":");
             }
             else {
                 param = getInstructionFromEitherField(fNew, p);
             }

            if(param.length()>0) {
                paramFound=true;
                if (p.equals(Enums.processMode.latlon)) {

                    String[] values = param.split(",", -1);
                    if (values.length == 2) {
                        try {
                            Double lat = Double.valueOf(values[0]);
                            Double lon = Double.valueOf(values[1]);
                            geocode(fNew, lat, lon, config, false);
                            // we should also set lat and lon if it is correct
                            if (fNew.getPlaceKey() != null) {
                                if (updateLatLon(lat, lon, fNew, config)) {
                                    countAddedLATLONG++;
                                    countDriveAddedLATLONG++;
                                    updateBothFields(fNew,param,p,existingCommentsString);

                                }


                            }
                        } catch (Exception e) {
                            message("could not convert provided values to lat long:" + param);
                            addError(fNew.getFileName(), fNew.getDirectory(), fNew.getBestDate(), "could not convert provided values to lat, lon:" + param);
                        }
                    } else {
                        message("Incorrect number of parameters for lat long:" + param);
                        addError(fNew.getFileName(), fNew.getDirectory(), fNew.getBestDate(), "Incorrect number of parameters for lat lon:" + param);
                    }

                } else if (p.equals(Enums.processMode.event)) {
                    try {
                        EventObject e;
                        e = getEvent(Integer.valueOf(param));
                        if (e == null) {
                            message("This Event has not been found in the JSON - event:" + param);
                            addError(fNew.getFileName(), fNew.getDirectory(), fNew.getBestDate(), "Event not found for:" + param);
                        } else {
                            updateEvent(config, fNew, e, existingCommentsString);
                            fNew.setBestDate(e.getExactStartTime());
                            message("Event ID match for event:" + e.getEventid() + " " + e.getTitle());
                            countAddedEvent++;
                            countDriveAddedEvent++;
                       }
                    } catch (Exception e) {
                        message("could not convert provided values to a place:" + param);
                        addError(fNew.getFileName(), fNew.getDirectory(), fNew.getBestDate(), "Could not convert provided values to a place:" + param);
                    }

                } else if (p.equals(Enums.processMode.place)) {

                    try {
                        Place g;
                        g = getPlace(Integer.valueOf(param));
                        if (g == null) {
                            message("This place has not been added in the JSON - place:" + param);
                            addError(fNew.getFileName(), fNew.getDirectory(), fNew.getBestDate(), "This place does not exist in the JSON:" + param);
                        } else {
                            message("Place has been found - place:" + param);
                            if (updateLatLon(g.getLatAsDouble(), g.getLonAsDouble(), fNew, config)) {
                                fNew.setPlaceKey(g.getPlaceid());
                                setFileObjectGEOValues(fNew, g, false, config);
                                countAddedPlace++;
                                countDriveAddedPlace++;
                                updateBothFields(fNew,param,p,existingCommentsString);
                            }

                        }
                    } catch (Exception e) {
                        message("could not convert provided values to a place:" + param);
                        addError(fNew.getFileName(), fNew.getDirectory(), fNew.getBestDate(), "Could not convert provided values to a place:" + param);
                    }

                } else if (p.equals(Enums.processMode.postcode)) {


                    String[] values3 = param.split(",", -1);
                    try {
                        if (config.getOpenAPIKey() != null) {
                            String newLat = "";
                            if (values3.length > 1) {
                                newLat = checkPostCode(values3[0], config.getOpenAPIKey(), values3[1]);
                            } else if (values3.length == 1) {
                                newLat = checkPostCode(values3[0], config.getOpenAPIKey(), "GBR");
                            }
                            String[] values2 = newLat.split(",", -1);
                            Double lat = Double.valueOf(values2[0]);
                            Double lon = Double.valueOf(values2[1]);
                            Boolean result = geocode(fNew, lat, lon, config, false);
                            // we should also set lat and lon if it is correct
                            if (fNew.getPlaceKey() != null) {
                                if (updateLatLon(lat, lon, fNew, config)) {
                                    countAddedPostcode++;
                                    countDriveAddedPostcode++;
                                    updateBothFields(fNew,param,p,existingCommentsString);
                                }

                            } else {
                                addError(fNew.getFileName(), fNew.getDirectory(), fNew.getBestDate(), "Could not find postcode:" + param);
                            }
                        } else {
                            addError(fNew.getFileName(), fNew.getDirectory(), fNew.getBestDate(), "API not available:" + param);
                        }
                    } catch (Exception e) {
                        message("could not convert provided values to a postcode:" + param);
                    }
                }
            }
        }
         return paramFound;

    }

    /**
     * Sets Geographic fields onthe fileObject from a Place object
     * @param fNew - fileObject
     * @param g - placeObject
     * @param alreadyGeocoded - flag if already Geocoded
     * @param config - config object
     */
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
     * @param fNew - this sets values on the new FileObject (reads metadata using Apache Imaging)

     * @param jpegMetadata - existing values read from metadata - this uses Apache Imaging
     * @param file - current file being processed
     */
    public static void setFileObjectValues(FileObject fNew,JpegImageMetadata jpegMetadata,File file)
    {
        if(jpegMetadata!=null)
        {

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

    /**
     * Reads three system date properties using windows attributes Modified, Accessed and Created
     * @param f - fileObject (this is updated)
     * @param file - file
     */
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

    /**
     *  Reads all IPTC data fields using the ICAFE library
     * @param f - fileObject
     * @param meta - ICAFE meta object
     * @return - either true or false, if successful
     */
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
                } else if (item.getKey().equals(IPTCApplicationTag.DATE_CREATED.getName())) {
                    if (item.getValue().length() > 0) {
                        f.setIPTCDateCreated(item.getValue());
                        message("Date Created current value is:" + item.getValue());
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
                    // this concatenates the subject field
                    if (item.getValue().length() > 0) {
                        if (f.getWindowsSubject()==null) {
                            f.setWindowsSubject(item.getValue());
                        } else {
                            f.setWindowsSubject(addNotNull(f.getWindowsSubject(),item.getValue()));
                            message("Possible Caption Abstract conflict" + item.getValue() + " : " + f.getWindowsSubject());
                        }
                        message("IPTC Description / Caption is:" + item.getValue());
                    }

                } else if (item.getKey().equals(IPTCApplicationTag.OBJECT_NAME.getName())) {
                    // this concatenates the title field
                    if (item.getValue().length() > 0) {
                        if (f.getWindowsTitle() ==null) {
                            f.setWindowsTitle(item.getValue());
                        } else {
                            f.setWindowsTitle(addNotNull(f.getWindowsTitle(),item.getValue()));
                            message("Possible title conflict" + item.getValue() + " : " + f.getWindowsTitle());
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
    public static String conditionallyUpdateField(String currentValue, String newValue,String fieldName,ConfigObject config)
    {
        // we update field if:
        // 1. the existing field is empty OR
        // 2. overwrite is set (i.e. it will overwrite existing values)
        // note the new value might be blank
        if (StringUtils.isNullOrEmpty(currentValue)  || config.getOverwrite() ) {
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
    /**
     *  Updates both instruction fields and adds a comment to the JPEG comments section
     * @param fNew - fileObject
     * @param param - parameter for the instruction / processMode
     * @param p - processMode
     * @param existingCommentsString - JPEG comments array
      */
    public static void updateBothFields(FileObject fNew,String param, Enums.processMode p,ArrayList<String> existingCommentsString)
    {
        fNew.setWindowsComments(updateInstructions(fNew.getWindowsComments(),p,param));
        fNew.setIPTCInstructions(updateInstructions(fNew.getIPTCInstructions(),p,param));
        addComment(existingCommentsString,"#"+p.toString()+"DONE:"+param);
    }
    public static void updateIPTCFields(List<IPTCDataSet> iptcs,FileObject fNew,ConfigObject config,DriveObject drive)
    {
        ArrayList<String> newKeys = new ArrayList<>();
        if(drive.getIPTCKeywords()!=null) {
            newKeys = new ArrayList<>(Arrays.asList(drive.getIPTCKeywords().split(";", -1)));
        }
        if(config.getNewdir()!=null)
        {
            joinKeys(newKeys,convertPathToKeywords(fNew.getDirectory(),drive.getStartdir()));
        }
        for(String n : newKeys) {
            iptcs.add(new IPTCDataSet(IPTCApplicationTag.KEY_WORDS, n));
        }
            iptcs.add(new IPTCDataSet(IPTCApplicationTag.COUNTRY_CODE, fNew.getCountry_code()));
            iptcs.add(new IPTCDataSet(IPTCApplicationTag.COUNTRY_NAME, fNew.getCountry_name()));
            iptcs.add(new IPTCDataSet(IPTCApplicationTag.PROVINCE_STATE, fNew.getStateProvince()));
            iptcs.add(new IPTCDataSet(IPTCApplicationTag.SUB_LOCATION, fNew.getSubLocation()));
            iptcs.add(new IPTCDataSet(IPTCApplicationTag.CITY, fNew.getCity()));
           //Copyright is a multi value field so this always adds a new one...
           if(drive.getIPTCCopyright()!=null) {
               iptcs.add(new IPTCDataSet(IPTCApplicationTag.COPYRIGHT_NOTICE, drive.getIPTCCopyright()));
           }
            iptcs.add(new IPTCDataSet(IPTCApplicationTag.CAPTION_ABSTRACT, fNew.getWindowsSubject()));

        DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        if (!StringUtils.isNullOrEmpty(fNew.getIPTCDateCreated()) || config.getOverwrite()) {
            iptcs.add(new IPTCDataSet(IPTCApplicationTag.DATE_CREATED, formatter.format(convertToDateViaInstant(fNew.getBestDate()))));
        }
    }
    /**
     * Updates file and moves it.
     * First updates IPTC metadata and then substitutes the IPTC, EXIF and COMMENTS in three passes with
     * It then updates the EXIF data and moves the file if required
     * Lastly, the file modified, created dates are reset to what they were before.
     * @param config - config object
     * @param drive - drive
     * @param file - filepath
     * @param existingCommentsString - JPEG comments array
     * @param iptc - IPTC metadata block
     * @param exif - EXIF metadata block
     * @param alreadyGeocoded - flag if already geocoded
     * @param fNew - fileObject (with metadata values)
     */
    public static void updateFile(ConfigObject config,DriveObject drive, File file,ArrayList<String> existingCommentsString,IPTC iptc,JpegExif exif,Boolean alreadyGeocoded,FileObject fNew)
    {

        if(config.getUpdate()) {
            try {
               List<IPTCDataSet> iptcs = new ArrayList<>();
               updateIPTCFields(iptcs,fNew,config,drive);
                FileInputStream fin = new FileInputStream(file.getPath());
                String fout_name = FilenameUtils.getFullPath(file.getPath()) + "out" + FilenameUtils.getName(file.getPath());
                File outFile = new File(fout_name);
                FileOutputStream fout = new FileOutputStream(outFile, false);
                List<Metadata> metaList = new ArrayList<>();
                String newDirectory=null;
                if(config.getNewdir()!=null) {
                    newDirectory = getNewDirectory(config,fNew);
                    File newFile = new File(newDirectory + "/" + fNew.getFileName());
                    if (!newFile.exists()) {
                        addComment(existingCommentsString, "#Moved fileDONE:" + fNew.getDirectory());
                        fNew.setWindowsComments(fNew.getWindowsComments()+"#Moved fileDONE:"+ fNew.getDirectory());
                        fNew.setIPTCInstructions(fNew.getIPTCInstructions()+"#Moved fileDONE:"+ fNew.getDirectory());
                    }
                    else
                    {
                        newDirectory=null;
                    }
                }
                // we update the Special Instructions if they have been provided....
                if (!StringUtils.isNullOrEmpty(fNew.getIPTCInstructions())) {
                    iptcs.add(new IPTCDataSet(IPTCApplicationTag.SPECIAL_INSTRUCTIONS, fNew.getIPTCInstructions()));
                }
                metaList.add(exif);
                iptc.addDataSets(iptcs);
                metaList.add(iptc);
                metaList.add(new Comments(existingCommentsString));

                Metadata.insertIPTC(fin, fout,iptcs,true);
               // Metadata.insertMetadata(metaList, fin, fout);
                fin.close();
                fout.close();
                // now add in the comments
                FileInputStream fin2 = new FileInputStream(fout_name);
                String fout_name2 = FilenameUtils.getFullPath(file.getPath()) + "out2" + FilenameUtils.getName(file.getPath());
                File outFile2 = new File(fout_name2);
                FileOutputStream fout2 = new FileOutputStream(outFile2, false);
                Metadata.insertComments(fin2, fout2,existingCommentsString);
                fout2.close();
                fin2.close();
                // now add the exif
                FileInputStream fin3 = new FileInputStream(fout_name2);
                String fout_name3 = FilenameUtils.getFullPath(file.getPath()) + "out3" + FilenameUtils.getName(file.getPath());
                File outFile3 = new File(fout_name3);
                FileOutputStream fout3 = new FileOutputStream(outFile3, false);
                Metadata.insertExif(fin3, fout3,exif);
                fin3.close();
                fout3.close();
                if (!file.delete()) {
                    message("Cannot delete file:" + file.getPath());
                }
                if (!changeExifMetadata(outFile3, file, fNew)) {
                    addError(fNew.getFileName(), fNew.getDirectory(), fNew.getBestDate(), "Error in updating Exif metadata");
                    message("Error in updating Exif metadata");
                    Files.copy(outFile3.toPath(), file.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
                }
                File finalFile = file;
                if (newDirectory != null)
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
                if (!outFile.delete()) {
                    addError(fNew.getFileName(),fNew.getDirectory(),fNew.getBestDate(),"Could not delete temporary file out"+ outFile.getName());
                    message("Cannot delete file:" + outFile.getPath());
                }
                if (!outFile2.delete()) {
                    addError(fNew.getFileName(),fNew.getDirectory(),fNew.getBestDate(),"Could not delete temporary file out3"+ outFile2.getName());
                    message("Cannot delete temp out2 file:" + outFile2.getPath());
                }
                if (!outFile3.delete()) {
                    addError(fNew.getFileName(),fNew.getDirectory(),fNew.getBestDate(),"Could not delete temporary file out2"+ outFile3.getName());
                    message("Cannot delete temp out2 file:" + outFile3.getPath());
                }
                countUPDATED++;
                countDriveUPDATED++;
            } catch (Exception e) {
                message("Cannot update File"+e);
            }
        }
    }
    /**
     *
     * @param jpegImageFile - source image file
     * @param dst - destination image file
     */
    public static boolean updateWindowsFields(final File jpegImageFile, final File dst)
             {

        try (FileOutputStream fos = new FileOutputStream(dst);
             OutputStream os = new BufferedOutputStream(fos)) {
            TiffOutputSet outputSet = null;
            // note that metadata might be null if no metadata is found.
            final ImageMetadata metadata = Imaging.getMetadata(jpegImageFile);
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            if (null != jpegMetadata) {
                // note that exif might be null if no Exif metadata is found.
                final TiffImageMetadata exif = jpegMetadata.getExif();
                if (null != exif) {
                    outputSet = exif.getOutputSet();
                }
            }

            // if file does not contain any exif metadata, we create an empty
            // set of exif metadata. Otherwise, we keep all of the other
            // existing tags.
            if (null == outputSet) {
                outputSet = new TiffOutputSet();
            }

            final TiffOutputDirectory rootDir = outputSet.getOrCreateRootDirectory();
            rootDir.removeField(MicrosoftTagConstants.EXIF_TAG_XPTITLE);
            rootDir.add(MicrosoftTagConstants.EXIF_TAG_XPTITLE, "new title");

            rootDir.removeField(MicrosoftTagConstants.EXIF_TAG_XPSUBJECT);
            rootDir.add(MicrosoftTagConstants.EXIF_TAG_XPSUBJECT, "new subject");
            //
            rootDir.removeField(MicrosoftTagConstants.EXIF_TAG_XPCOMMENT);
            rootDir.add(MicrosoftTagConstants.EXIF_TAG_XPCOMMENT, "new comment");
            //
            rootDir.removeField(MicrosoftTagConstants.EXIF_TAG_XPKEYWORDS);
            rootDir.add(MicrosoftTagConstants.EXIF_TAG_XPKEYWORDS, "key1;key2");
            //
            rootDir.removeField(MicrosoftTagConstants.EXIF_TAG_RATING);
            rootDir.add(MicrosoftTagConstants.EXIF_TAG_RATING, (short) 4);
            //
            rootDir.removeField(MicrosoftTagConstants.EXIF_TAG_XPAUTHOR);
            rootDir.add(MicrosoftTagConstants.EXIF_TAG_XPAUTHOR, "new author");

            new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os,
                    outputSet);
            return true;
        }
        catch(Exception e)
        {
            return false;
        }

    }
    /**
     *  this uses Apache Imaging to write out the latitude and Longitude - can't figure out how to do in ICAFE !
     * @param jpegImageFile - source image file
     * @param dst - destination image file
     */
    public static boolean changeExifMetadata(final File jpegImageFile, final File dst, FileObject fNew)
             {

        try (FileOutputStream fos = new FileOutputStream(dst);
             OutputStream os = new BufferedOutputStream(fos)) {
            TiffOutputSet outputSet = null;
            // note that metadata might be null if no metadata is found.
            final ImageMetadata metadata = Imaging.getMetadata(jpegImageFile);
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            if (null != jpegMetadata) {
                // note that exif might be null if no Exif metadata is found.
                final TiffImageMetadata exif = jpegMetadata.getExif();
                if (null != exif) {
                    outputSet = exif.getOutputSet();
                }
            }
            // if file does not contain any exif metadata, we create an empty
            // set of exif metadata. Otherwise, we keep all of the other
            // existing tags.
            if (null == outputSet) {
                outputSet = new TiffOutputSet();
            }
            if (fNew.getLongitude() != null && fNew.getLatitude() != null) {
                outputSet.setGPSInDegrees(fNew.getLongitude(), fNew.getLatitude());
            }
            final TiffOutputDirectory exifDir = outputSet.getOrCreateExifDirectory();
            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            exifDir.removeField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
            exifDir.add(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL, formatter.format( convertToDateViaInstant(fNew.getBestDate())));

            final TiffOutputDirectory rootDir = outputSet.getOrCreateRootDirectory();
            rootDir.removeField(MicrosoftTagConstants.EXIF_TAG_XPCOMMENT);
            rootDir.add(MicrosoftTagConstants.EXIF_TAG_XPCOMMENT, fNew.getWindowsComments());

            rootDir.removeField(TiffTagConstants.TIFF_TAG_IMAGE_DESCRIPTION);
            rootDir.add(TiffTagConstants.TIFF_TAG_IMAGE_DESCRIPTION, fNew.getWindowsTitle());

            rootDir.removeField(MicrosoftTagConstants.EXIF_TAG_XPTITLE);
            rootDir.add(MicrosoftTagConstants.EXIF_TAG_XPTITLE, fNew.getWindowsTitle());

            rootDir.removeField(MicrosoftTagConstants.EXIF_TAG_XPSUBJECT);
            rootDir.add(MicrosoftTagConstants.EXIF_TAG_XPSUBJECT, fNew.getWindowsSubject());
            new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os,
                    outputSet);
            return true;
        }
        catch(Exception e)
        {
            return false;
        }

    }
    /**
     * Updates the instructions in a string , based on the format #<instruction>:param to #<instruction>DONE:param
     * If blank or null, then a new string is added e.g. #<instruction>DONE:param
     * @param s - string to check
     * @param pMode - processMode / instruction
     * @param param - value to be used
     * @return - return string
     */
    private static String updateInstructions(String s,Enums.processMode pMode,String param)
    {
        if(s==null)
        {
            s="";
        }
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
            if(startPoint<0)
            {
                s=s+"#"+pMode+"DONE:"+param;
            }
            else {
                s = s.substring(0, startPoint + 1 + pMode.toString().length()) + "DONE" +
                        s.substring(startPoint + 1 + pMode.toString().length());
            }
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
    /**
     * Checks the distance between two lat and lon points ( Points will be converted to radians before calculation)
     * @param lat1 - first latitude
     * @param lon1 - first longtitude
     * @param lat2 - second latitude
     * @param lon2 - second longitude
     * @return - distance between first and second points (in metres)
     */
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
    public static boolean checkEventKey(String keyString,int keyValue)
    {
        String[] splits=keyString.split(";",-1);
        for(String s : splits)
        {
            if(s.equals(""+keyValue))
            {
                return true;
            }
        }
        return false;
    }
    /**
     *  Checks the Events and removes any which do not have correct dates
     *  There are two types of events - Calendar events where every year is checked and
     *  Exact date events - where a single date is checked.
     *  The exactStartTime and EndTime are set, in order to make searching efficient.
     *
     */
    public static void checkEvents()
    {
        Iterator<EventObject> i = events.iterator();
        EventObject e;
        while (i.hasNext()) {

            e = i.next();
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
     *  Reads metadata using ICAFE library
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
