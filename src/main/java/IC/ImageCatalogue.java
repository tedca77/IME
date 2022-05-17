/*
 *    Copyright 2021 E.M.Carroll
 *    ==========================
 *    This file is part of Image Metadata Enhancer (IME).
 *
 *     Image Metadata Enhancer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Image Metadata Enhancer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Image Metadata Enhancer.  If not, see <https://www.gnu.org/licenses/>.
 */
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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    static int countALREADYPROCESSED = 0; // images already processed
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
    static int countMoved = 0;    // images which have been moved
    static int countDuplicates = 0;  // images which are duplicates
//
    static int countDriveFiles = 0;
    static int countDriveImages = 0;
    static int countDriveTooSmall = 0;
    static int countDriveProcessed = 0;
    static int countDriveLATLONG = 0;
    static int countDriveALREADYPROCESSED = 0;
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
    static int countDriveMoved = 0;
    static int countDriveDuplicates = 0;
    static ArrayList<CameraObject> cameras = new ArrayList<>();
    static ArrayList<FileObject> fileObjects = new ArrayList<>();
    static ArrayList<FileObject> duplicateObjects = new ArrayList<>();
    static ArrayList<ErrorObject> errorObjects = new ArrayList<>();
    static ArrayList<ErrorObject> warningObjects = new ArrayList<>();

    static ArrayList<Place> places = new ArrayList<>();
    static ArrayList<EventObject> events = new ArrayList<>();

    static ArrayList<TrackObject> tracks = new ArrayList<>();
    // Default Variables - that can be modified
    static int messageLength=140; //length of Console Message
    static String imageDefaults="jpg~jpeg~bmp";
    static String jsonDefault="config.json";
    static Long minFileSizeDefault=4000L;
    static String thumbSizeDefault="360x270";
    static ArrayList<String> isocountryDefault=new ArrayList<>(Collections.singletonList("country_code"));
    static ArrayList<String> countryDefault=new ArrayList<>(Collections.singletonList("country"));
    static ArrayList<String> stateprovinceDefault=new ArrayList<>(Arrays.asList("county","state_district"));
    static ArrayList<String> cityDefault=new ArrayList<>(Arrays.asList("town","city","village"));
    static ArrayList<String> sublocationDefault=new ArrayList<>(Arrays.asList("amenity","leisure","house_number","road","hamlet","suburb","city_district"));
    static int cacheDistanceDefault=75; // this is the distance from a point that determines this is the same place in Metres.
    static int htmlLimitDefault =2000; // this is the limit to the number of images in an output file 2000
    static int kmlLimitDefault =200; // this is the limit to the number of points in a kml output file 200
    static int pauseSecondsDefault=2; // this is the pause before a geocode (to not overload the server)
    //
    static Date startTime;
    static Date endTime;
    /**
     * Main Method for the program - arguments passes as Java arguments
     * @param args - either single json file or two/three args, first one is root directory, second is output file, third is followed by parameters
     */
    public static void main(String[] args) {

        z= ZoneId.systemDefault();
        startTime= new Date();
        ConfigObject config=null;
        Path configFileName=null;

        // clear ArrayLists - required for JUNIT testing...
        clearArrayLists();
        try {
            if(FilenameUtils.getExtension(args[0]).equalsIgnoreCase("json"))
            {
                configFileName = Path.of(args[0]);
                message("JSON file name provided:"+configFileName);
                config = readConfig(configFileName.toString());
                if(config==null)
                {
                    System.exit(0);
                }
            }
            else
            {
                message("No json file present - requiring two parameters");
                if(args.length>1) {
                    config = new ConfigObject();
                    setDrives(config, args[0]);
                    config.setTempdir(args[1]);
                    configFileName=Path.of(args[0]+"\\"+jsonDefault);
                    message("Parameter 1 - Root Directory: "+args[0]);
                    message("Output Config File: "+configFileName);
                    message("Parameter 2 - Output Directory: "+args[1]);



                }
                else
                {
                    message("Please provide at least two parameters, for the Root directory for searching and the temporary output directory");
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
                message("Error sorting file objects"+e);
            }
            // sort any ArrayLists....
            fileObjects.sort(Comparator.comparing(FileObject::getBestDate));

            addLinksToPlaces(config.getTempdir());
            addLinksToEvents(config.getTempdir());
            //create tracks - this will also update the geoObjects
            if(!createTracks())
            {
                message("Failed to create tracks");
            }
            addLinksToTracks(config.getTempdir());
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
            message("Number of Errors:"+errorObjects.size());
            message("Number of Warnings:"+warningObjects.size());
            message("Number of Duplicates found:"+duplicateObjects.size());
            messageLine("*");
            //Exports reports for cameras, fileObjects etc...
            if(!exportHTML(config))
            {
                message("Failed to export HTML");
            }
            // print out total number of photos if more than one drive...
            if(config.getDrives().size()>1) {
                messageLine("*");
                message("All Drives - " + "Files found                         :" + countFiles);
                message("All Drives - " + "Photos found                        :" + countImages);
                message("All Drives - " + "Photos too small                   :" + countTooSmall);
                message("All Drives - " + "Photos to be processed             :" + countProcessed);
                message("All Drives - " + "Photos already Processed           :" + countALREADYPROCESSED);
                message("All Drives - " + "Photos Updated                     :" + countUPDATED);
                message("All Drives - " + "Photos where metadata not read     :" + countErrors);
                message("All Drives - " + "Photos which have been moved       :" + countMoved);
                message("All Drives - " + "Photos which are duplicates        :" + countDuplicates);
                message("All Drives - " + "------------------------------------" );
                message("All Drives - " + "Photos with Lat Long               :" + countLATLONG);
                message("All Drives - " + "Photos Geocoded                    :" + countGEOCODED);
                message("All Drives - " + "Photos with failed Geocoding       :" + countNOTGEOCODED);
                message("All Drives - " + "Photos where Date added            :" + countDateUpdate);
                message("All Drives - " + "Photos where Events found from date:" + countEventsFound);
                message("All Drives - " + "Photos with Place added            :" + countAddedPlace);
                message("All Drives - " + "Photos with Lat Lon added          :" + countAddedLATLONG);
                message("All Drives - " + "Photos with Postcode added         :" + countAddedPostcode);
                message("All Drives - " + "Photos with Event added            :" + countAddedEvent);
            }
        } catch (Exception e) {
            message("Error reading json file " + e);
        }
        endTime= new Date();
        long duration=(endTime.getTime()-startTime.getTime());
        String durationString= String.format("%02d hrs, %02d min, %02d sec",
               TimeUnit.MILLISECONDS.toHours(duration),
                (TimeUnit.MILLISECONDS.toMinutes(duration) % 60),
                (TimeUnit.MILLISECONDS.toSeconds(duration) % 3600));
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
    public static ConfigObject readConfigFromString(String s)
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm ss");
        mapper.setDateFormat(df);
        try {
            return mapper.readValue(s, ConfigObject.class);
        }
        catch(Exception e)
        {
            System.out.println("Error in JSON file:"+e);
            return null;
        }
    }
    /**
     * Reads a JSON Config File - this holds all run parameters
     * @param configFile - filename
     * @return - config object
     */
    public static ConfigObject readConfig(String configFile)
    {
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
            return readConfigFromString(result);
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
            else if(s.trim().equalsIgnoreCase(Enums.argOptions.redo.toString()))
            {
                config.setRedo(true);
            }
            else if(s.trim().equalsIgnoreCase(Enums.argOptions.append.toString()))
            {
                config.setAppend(true);
            }
            else if(s.trim().equalsIgnoreCase(Enums.argOptions.clear.toString()))
            {
                config.setClear(true);
            }
            else if(s.trim().equalsIgnoreCase(Enums.argOptions.addxpkeywords.toString()))
            {
                config.setAddxpkeywords(true);
            }
            else if(s.trim().equalsIgnoreCase(Enums.argOptions.addiptckeywords.toString()))
            {
                config.setAddiptckeywords(true);
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
            if (d != null) {
                Files.setAttribute(file.toPath(), "creationTime", FileTime.fromMillis(convertToDateViaInstant(d).getTime()));
                Files.setAttribute(file.toPath(), "lastAccessTime", FileTime.fromMillis(convertToDateViaInstant(d).getTime()));
                Files.setAttribute(file.toPath(), "lastModifiedTime", FileTime.fromMillis(convertToDateViaInstant(d).getTime()));
            }

        }
        catch(Exception e)
        {
            message("Could not set date time attributes");
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
        if(config.getHtmlLimit()==null){config.setHtmlLimit(htmlLimitDefault);}
        if(config.getKmlLimit()==null){config.setKmlLimit(kmlLimitDefault);}
        if(config.getPauseSeconds()<1){config.setPauseSeconds(pauseSecondsDefault);}
        if(config.getThumbsize()==null){config.setThumbsize(thumbSizeDefault);}
        // sets the defaults
        if(config.getUpdate()==null){config.setUpdate(false);}
        if(config.getShowmetadata()==null) {config.setShowmetadata(false);}
        if(!config.getUpdate())
        {
            // overwrite and redo options are not possible if Update has not been set
            config.setOverwrite(false);
            config.setRedo(false);
        }
        else
        {
            if(config.getOverwrite()==null) {config.setOverwrite(false);}
            if(config.getRedo()==null) {config.setRedo(false);}
        }
        if(config.getAppend()==null) {config.setAppend(false);}
        if(config.getImageextensions()==null) {config.setImageextensions(imageDefaults);}
        if(config.getSublocation()==null) {config.setSublocation(sublocationDefault);}
        if(config.getCity()==null) {config.setCity(cityDefault);}
        if(config.getCountry()==null) {config.setCountry(countryDefault);}
        if(config.getIsocountrycode()==null) {config.setIsocountrycode(isocountryDefault);}
        if(config.getStateprovince()==null) {config.setStateprovince(stateprovinceDefault);}
        if (config.getCacheDistance() == null) {
            config.setCacheDistance(cacheDistanceDefault);
        }
        if(config.getAddxpkeywords()==null) {config.setAddxpkeywords(false);}
        if(config.getAddiptckeywords()==null) {config.setAddiptckeywords(false);}
        if(config.getClear()==null) {config.setClear(false);}
    }
    /**
     * Writes out HTML reports using freemarker for the main objects.  Freemarker templates are in the project.
     * Files are written to the tempDir
     * @return - returns true or false
     */
    public static Boolean exportHTML(ConfigObject config)
    {
        String tempDir=config.getTempdir();
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);
        cfg.setObjectWrapper(new Java8ObjectWrapper(Configuration.VERSION_2_3_23));
        cfg.setClassForTemplateLoading(ImageCatalogue.class, "/Templates");
        // Recommended settings for new projects:
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);

            makeReport(config.getHtmlLimit(),cfg,tempDir,"cameras.ftl","cameras.html","cameras",cameras);
            makeReportLimitCount(config.getHtmlLimit(),cfg,tempDir,"places.ftl","places.html","places",places);
            makeReportLimitCount(config.getHtmlLimit(),cfg,tempDir,"tracks.ftl","tracks.html","tracks",tracks);
            makeReportKML(config.getKmlLimit(),cfg,tempDir,"trackkml.ftl","pointkml.ftl","track.kml","point.kml","tracks","places");
           // makeReportLimitCount(config.getKmlLimit(),cfg,tempDir,"pointkml.ftl","point.kml","places",places);
            makeReport(config.getHtmlLimit(),cfg,tempDir,"photosbydate.ftl","photosbydate.html","photos",fileObjects);
            makeReport(config.getHtmlLimit(),cfg,tempDir,"duplicates.ftl","duplicates.html","photos",duplicateObjects);
            makeReportLimitCount(config.getHtmlLimit(),cfg,tempDir,"events.ftl","events.html","events",events);
            makeReport(config.getHtmlLimit(),cfg,tempDir,"errors.ftl","errors.html","comments",errorObjects);
            makeReport(config.getHtmlLimit(),cfg,tempDir,"warnings.ftl","warnings.html","comments",warningObjects);


        return true;
    }
    public static ArrayList<Place> makePointsForTrack(List<TrackObject> tr)
    {
        ArrayList<Place> p = new ArrayList<>();
        for(TrackObject t : tr)
        {
            for(int i : t.getPoints())
            {
                if(!p.contains(getPlace(i)))
                {
                    p.add(getPlace(i));
                }

            }
        }
        return p;

    }
    public static void makeReportKML(Integer limit,Configuration cfg,String tempDir,String trackTemplate,String pointTemplate,String trackOutputFile,String pointOutputFile,String trackObjectName,String pointObjectName)
    {
        try {
            Template ftemplate = cfg.getTemplate(trackTemplate);
            Template ptemplate = cfg.getTemplate(pointTemplate);
            int counter=0;
            for (int i = 0; i < tracks.size(); i ++) {
                      counter=counter +  tracks.get(i).getCountTrack();
              }
            System.out.println("Counter total is:"+counter);
            if(counter<=limit) {
                runFreeMarker(tempDir + "/" + trackOutputFile,
                        trackObjectName,tempDir," ",ftemplate,tracks);
                runFreeMarker(tempDir + "/" + pointOutputFile,
                        pointObjectName,tempDir," ",ptemplate,places);
            }
            else
            {
                counter=0;
                int fileCounter=1;
                int startObject=0;
                for (int i = 0; i < tracks.size(); i ++) {

                    counter=counter +  tracks.get(i).getCountTrack();

                    if(counter>=limit) {
                        ArrayList<TrackObject> t = new ArrayList<>(tracks.subList(startObject,i+1));
                                runFreeMarker(tempDir + "/" + FilenameUtils.getBaseName(trackOutputFile) + (fileCounter) + "." + FilenameUtils.getExtension(trackOutputFile),
                                trackObjectName,tempDir,"Part "+fileCounter,ftemplate,t);
                        ArrayList<Place> p = makePointsForTrack(t) ;
                        runFreeMarker(tempDir + "/" + FilenameUtils.getBaseName(pointOutputFile) + (fileCounter) + "." + FilenameUtils.getExtension(pointOutputFile),
                                pointObjectName,tempDir,"Part "+fileCounter,ptemplate,p);

                        counter=0;
                        startObject=i+1;
                        fileCounter++;
                    }
                }
                if(counter>0) {
                    ArrayList<TrackObject> t = new ArrayList<>(tracks.subList(startObject,tracks.size())) ;
                    ArrayList<Place> p = makePointsForTrack(t) ;

                    runFreeMarker(tempDir + "/" + FilenameUtils.getBaseName(trackOutputFile) + (fileCounter) + "." + FilenameUtils.getExtension(trackOutputFile),
                            trackObjectName,tempDir,"Part "+fileCounter,ftemplate,t);

                    runFreeMarker(tempDir + "/" + FilenameUtils.getBaseName(pointOutputFile) + (fileCounter) + "." + FilenameUtils.getExtension(pointOutputFile),
                            pointObjectName,tempDir,"Part "+fileCounter,ptemplate,p);

                }
            }

        } catch (IOException e) {
            message("Cannot write output files to directory - please check the path exists:"+tempDir);
        }
        catch(Exception ee)
        {
            message("Error writing output files :"+ee);
        }
    }
    public static void makeReportLimitCount(Integer limit,Configuration cfg,String tempDir,String template,String outputFile,String objectName,ArrayList<?> objects)
    {
        try {
            Template ftemplate = cfg.getTemplate(template);
            int counter=0;
            for (int i = 0; i < objects.size(); i ++) {
                if(objects.get(i) instanceof Place)
                {
                    counter=counter + ((Place) objects.get(i)).getCountPlace();
                }
                if(objects.get(i) instanceof EventObject)
                {
                    counter=counter + ((EventObject) objects.get(i)).getCountEvent();
                }
                if(objects.get(i) instanceof TrackObject)
                {
                    counter=counter + ((TrackObject) objects.get(i)).getCountTrack();
                }
            }
            System.out.println("Counter total Mke Report Limit  is:"+counter);
            if(counter<=limit) {
                runFreeMarker(tempDir + "/" + outputFile,
                        objectName,tempDir," ",ftemplate,objects);
            }
            else
            {
                counter=0;
                int fileCounter=1;
                int startObject=0;
                for (int i = 0; i < objects.size(); i ++) {
                    if(objects.get(i) instanceof Place)
                    {
                        counter=counter + ((Place) objects.get(i)).getCountPlace();
                    }
                    if(objects.get(i) instanceof EventObject)
                    {
                        counter=counter + ((EventObject) objects.get(i)).getCountEvent();
                    }
                    if(objects.get(i) instanceof TrackObject)
                    {
                        counter=counter + ((TrackObject) objects.get(i)).getCountTrack();
                    }
                    if(counter>=limit) {

                        runFreeMarker(tempDir + "/" + FilenameUtils.getBaseName(outputFile) + (fileCounter) + "." + FilenameUtils.getExtension(outputFile),
                                objectName,tempDir,"Part "+fileCounter,ftemplate,objects.subList(startObject,i+1));
                          counter=0;
                        startObject=i+1;
                        fileCounter++;
                    }
                }
                if(counter>0) {

                    runFreeMarker(tempDir + "/" + FilenameUtils.getBaseName(outputFile) + (fileCounter) + "." + FilenameUtils.getExtension(outputFile),
                            objectName,tempDir,"Part "+fileCounter,ftemplate,objects.subList(startObject,objects.size()));

                }
            }

        } catch (IOException e) {
            message("Cannot write output files to directory - please check the path exists:"+tempDir);
        }
        catch(Exception ee)
        {
            message("Error writing output files :"+ee);
        }
    }
    public static void runFreeMarker(String fileName,String objectName,String tempDir,String partText,Template template,List<?> objects)
    {
        try {
            FileWriter fwriter = new FileWriter(fileName);
            Map<String, Object> froot = new HashMap<>();
            froot.put(objectName, objects);
            froot.put("root", tempDir);
            froot.put("parttext", partText);
            template.process(froot, fwriter);
            fwriter.close();

    } catch (IOException e) {
        message("Cannot write output files to directory - please check the path exists:"+tempDir);
    }
        catch(Exception ee)
    {
        message("Error writing output files :"+ee);
    }
    }
    public static void makeReport(Integer limit,Configuration cfg,String tempDir,String template,String outputFile,String objectName,ArrayList<?> objects)
    {
        try {
            Template ftemplate = cfg.getTemplate(template);

            if(objects.size()<=limit) {
                runFreeMarker(tempDir + "/" + outputFile,
                        objectName,tempDir," ",ftemplate,objects);

            }
            else
            {
                int counter=1;
                for (int i = 0; i < objects.size(); i += limit) {
                    runFreeMarker(tempDir + "/" + FilenameUtils.getBaseName(outputFile) + (counter) + "." + FilenameUtils.getExtension(outputFile),
                            objectName,tempDir,"Part "+counter,ftemplate,objects.subList(i, Math.min(i + limit, objects.size())));
                    counter++;
                }
            }

        } catch (IOException e) {
            message("Cannot write output files to directory - please check the path exists:"+tempDir);
        }
        catch(Exception ee)
        {
            message("Error writing output files :"+ee);
        }
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
            if(c.getClear())
            {
                message("JPG COMMENTS WILL BE REMOVED ONLY ");
            }
            else {
                if (c.getShowmetadata()) {
                    message("METADATA WILL BE SHOWN BEFORE AND AFTER UPDATING");
                }
                if (c.getOverwrite()) {
                    message("EXISTING METADATA WILL BE OVERWRITTEN");
                } else {
                    if (c.getRedo()) {
                        message("REDO - GEOCODE METADATA WILL BE OVERWRITTEN ");
                    } else {
                        message("EXISTING METADATA WILL NOT BE OVERWRITTEN");
                    }
                }

            }
        }
        else {
            message("FILES WILL NOT BE UPDATED");
            if (c.getShowmetadata()) {
                message("METADATA WILL BE SHOWN");
            }
        }

        if(c.getAppend()) {
            message("PHOTOS WILL BE APPENDED TO PHOTOS LISTED IN JSON");
        }

        message("Fields for Sub Location: "+c.getSublocation().toString());
        message("Fields for State/Province: "+c.getStateprovince().toString());
        message("Fields for City: "+c.getCity().toString());
        message("Fields for Country Name: "+c.getCountry().toString());
        message("Fields for Country Code: "+c.getIsocountrycode().toString());
    }
    /**
     * Exports JSON config file with new lists of objects included.  File is same as input file but with date and time added.
     * @param c - config object
     * @param inputPath - input Path Name
     * @return - returns true or false
     */
    public static Boolean exportConfig(ConfigObject c, Path inputPath)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
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
        message("Number of Files :"+fileObjects.size());
        message("Number of Cameras :"+cameras.size());
        message("Number of Places :"+places.size());
        message("Number of Events :"+events.size());
        message("Number of Tracks :"+tracks.size());

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
            //Set values on the FileObject
            final ImageMetadata metadata = Imaging.getMetadata(file);
            if (metadata instanceof JpegImageMetadata) {
                final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
                setFileObjectValues(fNew,jpegMetadata,file);
            }
            else
            {
                setFileObjectValues(fNew,null,file);
            }
            //set the Windows Title and Subject if it is not set or combine if it is...
            if (fNew.getWindowsSubject()==null) {
                fNew.setWindowsSubject(fNew.getIPTCCaptionAbstract());
            } else {
                fNew.setWindowsSubject(addNotNull(fNew.getWindowsSubject(),fNew.getIPTCCaptionAbstract()," "));
                if(fNew.getIPTCCaptionAbstract()!=null) {
                    if(!fNew.getIPTCCaptionAbstract().equals(fNew.getWindowsSubject())) {
                        message("Possible Caption Abstract conflict - IPTC:" + fNew.getIPTCCaptionAbstract() + " :Windows: " + fNew.getWindowsSubject());
                    }
                }
            }
            if(fNew.getWindowsSubject()==null)
            {
                fNew.setWindowsSubject("");
            }

            if (fNew.getWindowsTitle()==null) {
                fNew.setWindowsTitle(fNew.getIPTCObjectName());
            } else {
                fNew.setWindowsTitle(addNotNull(fNew.getWindowsTitle(),fNew.getIPTCObjectName()," "));
              if(fNew.getIPTCObjectName()!=null) {
                    if(!fNew.getIPTCObjectName().equals(fNew.getWindowsTitle())) {
                        message("Possible Title conflict - IPTC:" + fNew.getIPTCObjectName() + " :Windows: " + fNew.getWindowsTitle());
                    }
                }
            }
            if(fNew.getWindowsTitle()==null)
            {
                fNew.setWindowsTitle("");
            }
            if(fNew.getComments()==null)
            {
                fNew.setComments(new ArrayList<>());
            }


        } catch (Exception e) {
            message("Error reading metadata:"+e);
            setFileObjectValues(fNew,null,file);
            addError(fNew.getFileName(),fNew.getDirectory(),fNew.getBestDate(),"Error reading metadata:"+e,false);
        }
    }
    /**
     * this method recursively looks at directories and subdirectories and identifies image files
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
                                    Long fileSize = file.length();
                                    Long testFileSize=FileUtils.sizeOf(file);
                                    if(!fileSize.equals(testFileSize))
                                    {
                                        message ("Discrepancy in file size"+fileSize + ": " +testFileSize);
                                    }
                                    if (fileSize > config.getMinfilesize()) {
                                        countProcessed++;
                                        countDriveProcessed++;
                                        messageLine("-");
                                        message("File number:["+ countProcessed + "], file:" + fixSlash(file.getCanonicalPath()));
                                        message("File size is:" + fileSize +" ( bytes)");
                                        if(config.getShowmetadata()) {
                                            if (!readMetadata(file)) {
                                                message("Could not read metadata before update");
                                            }
                                            messageLine("~");
                                        }
                                        String thumbName = makeThumbName(file);
                                        FileObject fNew= readAndUpdateFile(file, thumbName,config, drive,false);
                                        if(fNew==null)
                                        {
                                            countErrors++;
                                            countDriveErrors++;
                                            message("Could not process file:"+file.getCanonicalPath());
                                            addError(file.getName(),fixSlash(FilenameUtils.getFullPath(file.getPath())),convertToLocalDateTimeViaInstant(getFileDate(file)),"Error - Could not process file",false);
                                        }
                                        else {
                                            fileObjects.add(fNew);
                                        }
                                        if(config.getShowmetadata() && config.getUpdate()) {
                                            messageLine("~");
                                                if(fNew!=null) {
                                                    if (!readMetadata(new File(fNew.getDirectory() + "/" + fNew.getFileName()))) {
                                                        message("Could not read metadata after update");
                                                    }
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
                        message("Error reading Files in Directory Contents:" + file.getCanonicalPath()+"Error:"+ee);
                    }
                }
            }
        } catch (Exception e) {
            message("Error reading directory:" + drive.getStartdir()+", Error:"+e);
        }
    }
    public static ArrayList<String> assembleKeywords(Place g)
    {
        ArrayList<String> s = new ArrayList<>();
        s=addNotNullArray(s,g.getAddress().getCountry_code().toUpperCase());
        s=addNotNullArray(s,g.getAddress().getCountry());
        s=addNotNullArray(s,g.getAddress().getCounty());
        s=addNotNullArray(s,g.getAddress().getCity());
        s=addNotNullArray(s,g.getAddress().getPostcode());
        s=addNotNullArray(s,g.getAddress().getRoad());
        s=addNotNullArray(s,g.getAddress().getHouse_number());
        s=addNotNullArray(s,g.getAddress().getState());
        s=addNotNullArray(s,g.getAddress().getState_district());
        s=addNotNullArray(s,g.getAddress().getVillage());
        s=addNotNullArray(s,g.getAddress().getHamlet());
        s=addNotNullArray(s,g.getAddress().getPostcode());
        s=addNotNullArray(s,g.getAddress().getTown());
        s=addNotNullArray(s,g.getAddress().getSuburb());
        s=addNotNullArray(s,g.getAddress().getAmenity());
        s=addNotNullArray(s,g.getAddress().getCity_district());
        s=addNotNullArray(s,g.getAddress().getLeisure());
        return s;
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
               s = addWithCommaIfNotNull(s,g.getAddress().getCountry_code().toUpperCase() );

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
    public static String addNotNull(String s, String valString,String spacer) {
        if(s==null)
        {
            return valString;
        }
        if (valString != null) {
            if(s.equals(valString))
            {
                return s;
            }
           String newS= s + spacer+ valString ;
           return newS.trim();
        }
        return s;
    }
    /**
     * adds to an array if not null (checks nulls). If the same, then do not concatenate
     * @param s - ArrayList to add to = this is either blank or a value
     * @param valString - new value - it can be null;
     * @return - modified s (or existing value if null)
     */
    public static ArrayList<String> addNotNullArray(ArrayList<String> s, String valString) {

        if (valString != null) {
            if(valString.trim().length()>0) {
                if(s==null)
                {
                    s=new ArrayList<>();
                }
                if (s.contains(valString)) {
                    return s;
                }
                s.add(valString.trim());

             }
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
            if (s != null) {
                return s + valString + ", ";
            } else {
                return valString;
            }
        }
        return s;
    }
    public static void addLinksToEvents( String root)
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
     * Checks that the latest PlaceKey in comments exists as a Place Object and that the lat,lon is correct if not, then returns false, and it will be geocoded again.
     * This covers the situation where the Places have been renumbered, and don't now match - it will be necessary to redo in this case...
     * @param fNew - File Object
     * @param config - Config Object
     * @return - returns true if found
     */
    public static boolean checkPlaceKeyFound(FileObject fNew,ConfigObject config)
    {
        int placeKey=getPlaceKeyFromIPTCComments(fNew.getComments());
        if(placeKey<1)
        {
          //  message("Could not find place key in Comments:");
            return false;
        }
        Place g = getPlace(placeKey);
        if(g!=null)
        {
            double glat = Double.parseDouble(g.getLat());
            double glon = Double.parseDouble(g.getLon());
            double distance=distance_Between_LatLong(fNew.getLatitude(),fNew.getLongitude(),glat,glon);
            if ( distance< Double.valueOf(config.getCacheDistance())) {
                fNew.setPlaceKey(placeKey);
                return true;
            }
            else
            {
                message("Place Key found"+g.getPlaceid()+" but it does not match. lat/Lon distance is: "+distance +"need to redo...");
                return false;
            }
        }
        else
        {
            message("Place key not found in JSON with a key of :"+placeKey +" geocoding lat and lon");
            return false;
        }

    }
    /**
     * finds out the previously matched Place key by looking in the comments section....
     * @param existingComments - Array containing JPEG Comments for the File Object
     * @return - key of Place Object
     */
    public static int getPlaceKeyFromIPTCComments(List<String> existingComments)
    {
        ArrayList<Integer> keys = new ArrayList<>();
        for(String s : existingComments)
        {
            String test="#"+Enums.processMode.geocode+Enums.doneValues.DONE+":";
            if(s.contains(test))
            {
                String ss=s.substring(test.length());
                String[] splits=ss.split(":",-1);
                  if(splits.length>2) {
                      try {


                          if (!keys.contains(Integer.valueOf(splits[1]))) {
                              keys.add(Integer.valueOf(splits[1]));
                          }
                      } catch (Exception e) {
                          message("Illegal Place value" + splits[1]);
                      }
                  }
                  else
                  {
                      return -1;
                  }

            }
        }
        if(keys.size()<1)
        {
            return -1;
        }
        return keys.get(keys.size()-1);

    }
    /**
     * this adds some HTML for images to the Place object, so we can incorporate into Freemarker report easily
     * @param root - directory where images will be
     */
    public static void addLinksToPlaces( String root)
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
            s.append("<small>").append("[").append(f.getOrientation()).append("]").append("</small></span>");
            s.append("</div>");
        } else {
            // Integer newWidth = width * f.getHeight() / f.getWidth();

            s.append(" <div class=\"item\">");
            //  s.append("<img  src=\"").append(root).append("\\").append(f.getThumbnail()).append("\" width=\"").append(newWidth).append("\"  >");
            s.append("<img  src=\"").append(root).append("\\").append(f.getThumbnail()).append("\"  >");
            s.append("<span class=\"caption\"><small>").append(f.getDirectory()).append("</small></br>");
            s.append("<small>").append(f.getFileName()).append("</small>");
            s.append("<small>").append("[").append(f.getOrientation()).append("]").append("</small></span>");
            s.append("</div>");
        }
        return s.toString();


    }
    /**
     * this adds some HTML for images to the Track object, so we can incorporate into Freemarker report easily
      * @param root - directory where images will be
     */
    public static void addLinksToTracks( String root)
    {

        // for each track, get all the photos for the day, as some may not have location...and
        // they could be slotted in, in sequence (i.e. date order)
        for(TrackObject t : tracks) {
            StringBuilder s = new StringBuilder();
                    for (FileObject f : fileObjects) {
                        try {
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
            t.setCountTrack(points.size());
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
    public static void addError(String fileName,String directory,LocalDateTime d,String message,Boolean warning)
    {
        ErrorObject error = new ErrorObject();
        error.setFileName(fileName);
        error.setFileDate(d);
        error.setMessage(message);
        error.setDirectory(directory);
        if(warning) {
            warningObjects.add(error);
        }
        else
        {
            errorObjects.add(error);

        }
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
                                   String dateString = fileName.substring(fileName.length()-14);
                                   if(newestFile.length()<1)
                                   {
                                      // System.out.println("date to convert:"+dateString);

                                       //some dates have slashes instead of colons
                                       d= LocalDateTime.parse(dateString,formatter);
                                       newestFile=file.getName();
                                   }
                                   else
                                   {
                                       LocalDateTime dd= LocalDateTime.parse(dateString);
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
                        //don't rename json file !!
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
    public static ArrayList<String> clearBlanks(ArrayList<String> ss)
    {
        ss.removeIf(s-> s.trim().length()<1);
        return ss;
    }
    /**
     * this is used for clearing arraylists - required for testing only - where variables may not be cleared...
     */
    public static void clearArrayLists()
    {
        fileObjects.clear();
        duplicateObjects.clear();
        cameras.clear();
        places.clear();
        events.clear();
        errorObjects.clear();

    }
    /**
     * This is only used for testing. Clears the test area of three subdirectories:
     * /Test , /TestRESULTS and /TestNewDir
     * @param rootDir - root directory for clearing the folders
     */
    public static boolean clearTestArea(String rootDir) {
        File rootFile = new File(rootDir);
        if (rootFile.exists()) {
            try {
                FileUtils.cleanDirectory(new File(rootDir + "/Test/DirKeyword1 DirKeyword2"));
            } catch (Exception e) {
                message("Test/DirKeyword1 DirKeyword2 folder is missing");
            }
            try {
                FileUtils.cleanDirectory(new File(rootDir + "/Test"));
            } catch (Exception e) {
                message("Test folder is missing");
            }
            try {
                FileUtils.cleanDirectory(new File(rootDir + "/TestRESULTS"));
            } catch (Exception e) {
                message("TestResults folder is missing");
            }
            try {

                FileUtils.cleanDirectory(new File(rootDir + "/TestNewDir"));
            } catch (Exception e) {
                message("TestNewDir folder is missing or cannot be cleared:"+e);
            }
            return true;
        } else {
            message("Clear Test Area - Root Directory does not exist:" + rootDir);
            return false;
        }
    }
    public static String appendComment (String existingComment,String newComment)
    {
        DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");

        if(existingComment==null)
        {
            return newComment+":" + formatter.format(new Date());
        }
        return existingComment+newComment+":" + formatter.format(new Date());
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
     * @return - revised ExistingComments
     */
    public static ArrayList<String> removeIPTCComments( ArrayList<String> existingComments,String test)
    {

        existingComments.removeIf(s -> s.contains(test));
        return existingComments;
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
     * Goes through existing JPEG comments looking for a string from the process mode e.g. to find event keys in the comments
     * @param existingComments - array of Comments
     * @param p - Process Mode to look for
     * @return - either true if found or false
     */
    public static String getEventKeysFromIPTCComments(List<String> existingComments, Enums.processMode p)
    {
        ArrayList<String> keyStrings = new ArrayList<>();
        for(String s : existingComments)
        {
            String test="#"+p+Enums.doneValues.DONE+":";
            if(s.contains(test))
            {
                String ss = s.substring(test.length(),s.indexOf(":",test.length()+2));
                if(!keyStrings.contains(ss))
                {
                    keyStrings.add(ss);
                }

            }
        }
        return String.join(";",keyStrings);
    }

    /**
     * Checks disk space available - path must be a top level disk directory
     * @param destPath - string for path
     * @return - true if ennough space
     */
    public static boolean checkDiskSpace(String destPath)
    {
        String[]  splits=destPath.split(":");
        File file = new File(splits[0]+":");

        long usableSpace = file.getUsableSpace(); ///unallocated / free disk space in bytes.


        System.out.println("Space free : " + usableSpace /1024 /1024 + " mb");

        long usableSpaceMB = usableSpace /1024 /1024;
        return usableSpaceMB >= 100;

    }
    /**
     *  Checks for duplicate files and adds to the duplicate list if found.
     */
    public static void checkDuplicateFile(FileObject fNew) {

        for (FileObject f : fileObjects) {
            try {

                if (fNew.getFileName().equals(f.getFileName())) {
                    if (f.getCameraModel().equals(fNew.getCameraModel()) && f.getCameraMaker().equals(fNew.getCameraMaker()) &&
                      f.getBestDate().isEqual(fNew.getBestDate())) {

                        if (!duplicateObjects.contains(f)) {
                            duplicateObjects.add(f);
                        }
                        duplicateObjects.add(fNew);
                        countDriveDuplicates++;
                        countDuplicates++;
                        fNew.setDuplicate(true);
                        message("Duplicate file - also in:" + f.getDirectory());
                        addError(fNew.getFileName(), fNew.getDirectory(),fNew.getBestDate(), "Warning - duplicate file - also in:" + f.getDirectory(),true);
                    } else {
                        addError(fNew.getFileName(), fNew.getDirectory(),fNew.getBestDate(), "Warning - duplicate file name, but files are not the same: other directory is: "+f.getDirectory(),true);
                        message("Duplicate filename but files are different"+f.getDirectory()+":"+f.getFileName());
                    }
                }
            } catch (Exception e) {
                message("error checking duplicates:" + f.getDisplayName() + e);
            }
        }
    }
    public static String getUniqueFileName(String dirName,String fileName)
    {
         String root= FilenameUtils.getBaseName(fileName);
         String ext = FilenameUtils.getExtension(fileName);
         int count=1;
         while(count<1000) {
             String testName=root+"_"+String.format("%03d",count)+"."+ext;
             String testPath= dirName+"/"+testName;
             File f = new File(testPath);
             if(!f.exists()) {
                 return testName;
             }
             count++;
         }
         addError(fileName,dirName,null,"Too many duplicates for:"+fileName,false);
         return null;

    }
    /**
     * Checks whether we already have a geocode object - if we do, then we just update the start or end date
     * The test is whether the object is within a certain distance of the object being checked using distance_Between_LatLong
     * @param lat - latitude (of image)
     * @param lon - longitude (of image)
     * @param d - date  (of image)
     * @return - Place Object
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
     * Creates a LocalDateTime from a string with can be YYYY or YYYY-MM  or YYYY-MM-DD
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
     * We are not interested in the Year for this test e g. a birthday if a start year is not provided - if it is, then nothing before this year will
     *  be found
     * @param param - string to convert
     * @return - returns LocalDateTime or null if there is an error
     */
    public static LocalDateTime createLocalDateCalendar(String param)
    {
        String[] values = param.split("-", -1);
        try {
            int year = 1800;
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
        boolean result=true;
        try {
          result = new File(temp).mkdir();

        } catch (Exception e) {
           if(e.getMessage().equals("OK"))
           {
               message("ok"+result);
           }
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
     * @return - returns an integer placeID
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
        if(c.getAppend()) {
            // we need to read in the photos
            message("NUMBER OF PHOTOS READ FROM CONFIG FILE:"+c.getPhotos().size());
            fileObjects=c.getPhotos();
            for(FileObject f : fileObjects) {
                if (f.getIPTCKeywords() != null) {
                    f.setIPTCKeywordsArray(new ArrayList<>(Arrays.asList(f.getIPTCKeywords().split(";", -1))));
                }
                if (f.getWindowsKeywords() != null) {
                    f.setWindowsKeywordsArray(new ArrayList<>(Arrays.asList(f.getWindowsKeywords().split(";", -1))));
                }
            }
        }
        if(c.getCameras()!=null)
        {
            message("NUMBER OF CAMERAS READ FROM CONFIG FILE:"+c.getCameras().size());
            cameras=c.getCameras();
            //sort in case there are gaps in numbering
            cameras.sort(Comparator.comparing(CameraObject::getCameraid));
            if(!c.getAppend()) {
                for (CameraObject cc : cameras) {
                    cc.setCameracount(0);
                }
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
                if(!c.getAppend()) {
                    g.setCountPlace(0);
                }
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
                e.setCountEvent(0);
                if(e.getKeywords()!=null)
                {
                    e.setKeywordsArray(new ArrayList<>(Arrays.asList(e.getKeywords().split(";", -1))));
                }
            }
            //sort in case there are gaps in numbering
            events.sort(Comparator.comparing(EventObject::getEventid));
        }
        runReport();
        messageLine("*");
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
     * Moves a file to the new structure
     * @param config - config Object (contains program variables)
     * @param f - fileObject to be moved
     * @return - returns result
     */
    public static Boolean moveFile(ConfigObject config, FileObject f,String newFileName) {
        //

        String newDirectory = getNewDirectory(config, f);
        File oldFile = new File(f.getDirectory() + "/" + f.getFileName());
        File newFile = new File(newDirectory + "/" + newFileName);
        if(!checkDiskSpace(newDirectory)) {
            message("Could not move file from " + oldFile.getPath() + " to " + newFile.getPath() + " - Not enough space");
            addError(f.getFileName(),f.getDirectory(),f.getBestDate(),"Could not move image file - not enough space",false);
            return false;
        }

        String oldThumbName = makeThumbName(oldFile);
        boolean renameResult = oldFile.renameTo(newFile);
        if (!renameResult) {
            message("Could not move file from " + oldFile.getPath() + " to " + newFile.getPath());
            addError(f.getFileName(),f.getDirectory(),f.getBestDate(),"Could not move image file",false);
            return false;
        }
        else {
            String newThumbName = makeThumbName(newFile);

            File oldThumb = new File(config.getTempdir() + "/" + oldThumbName);
            File newThumb = new File(config.getTempdir() + "/" + newThumbName);
            //rename file
            boolean renameResultthumb = oldThumb.renameTo(newThumb);
            if (!renameResultthumb) {
                message("Could not move thumb file from " + oldThumb.getName() + " to " + newThumb.getName());
                addError(f.getFileName(), f.getDirectory(),f.getBestDate(), "Could not move thumbnail file",false);
            }
            //if rename files - change the name and directory....
            f.setDirectory(newDirectory);
            f.setThumbnail(newThumbName);
        }
        return true;
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
          //  System.out.println("exclude"+fixSlash(fdir)+ ",start:"+drive.getStartdir()+fixSlash(i.getName()));
            if(i.getName()!=null) {
                if (fixSlash(fdir).equals(drive.getStartdir() + fixSlash(i.getName()))) {
                    message("Excluded directory:" + fixSlash(fdir));
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
     * Replaces backslashes with forward slashes for consistency of file names on all platforms
     * @param s - string to change
     * @return - return string
     */
    public static String fixSlash(String s)

    {
        return s.replace("\\","/");
    }

    /**
     * Adds to a list of keys to an existing list (checks that the key is not already there)
     * @param current String array list of Keys
     * @param newKeys - new keys to add
     */
    public static ArrayList<String> joinKeys(ArrayList<String> current,ArrayList<String> newKeys)
    {
        if(current==null)
        {
            return newKeys;
        }
        for(String s: newKeys)
        {
            if(!current.contains(s))
            {
                current.add(s);
            }
        }
        return current;
    }
    public static FileObject initialiseFileObject()
    {
        FileObject f = new FileObject();
        f.setCountry_code("");
        f.setCity("");
        f.setStateProvince("");
        f.setCountry_name("");
        f.setSubLocation("");
        f.setIPTCKeywordsArray(new ArrayList<>());
        f.setEventKeys("");
        f.setDisplayName("");
        f.setComments(new ArrayList<>());

        return f;
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
     * Finds Camera from its internal key
     * @param i - key value
     * @return - Camera Object
     */
    private static CameraObject getCamera(Integer i)
    {
        for(CameraObject c : cameras)
        {
            if(c.getCameraid().equals(i))
            {
                return c;
            }
        }
        message("Error -could not retrieve Camera with a key of:"+i);
        return null;
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
       // message("Error -could not retrieve Place with a key of:"+i);
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
                                        final TagInfo tagInfo,File file) {
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
                    addError(file.getName(),fixSlash(FilenameUtils.getFullPath(file.getPath())),convertToLocalDateTimeViaInstant(getFileDate(file)),"error parsing date:"+field,false);
                    return null;
                }

            }
        }
    }

    /**
     * Checks a File Object's dates against an event date. If the Event has already been found before (in the eventKeys field), then it will not be rematched.
     * @param config - Config Object
     * @param fNew - File Object
     * @param e - Event Object (providing dates)
     * @return - returns 1 if found a date
     */
    public static Integer checkEvent(ConfigObject config,FileObject fNew,EventObject e)
    {
        LocalDateTime d= fNew.getBestDate();
        if(checkEventKey(fNew.getEventKeys(),e.getEventid()))
        {
            return 0;
        }
        if(!checkIPTCComments(fNew.getComments(), "#Event:" + e.getEventid() + Enums.doneValues.DONE+":") || config.getOverwrite()) {
            //eventcalendar - month and date must be exact (no time value) and year must be greater than or equal to event year....
            if (e.eventcalendar != null) {
                if (d.getMonth() == e.getExactStartTime().getMonth() && d.getDayOfMonth() == e.getExactStartTime().getDayOfMonth() &&
                     d.getYear()>=e.getExactStartTime().getYear()) {
                    updateEvent(config, fNew, e);
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
                    updateEvent(config, fNew, e);
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
     * @return - number of Events found for this FileObject
     */
    public static Integer processEvents(ConfigObject config,FileObject fNew)
    {
        int eventFound=0;

        for(EventObject e : events)
        {
           eventFound=eventFound+checkEvent(config,fNew,e);
        }
        if(eventFound>0) {
            message("Number of Events found :"+eventFound);
        }
        return eventFound;

    }
    /**
     * Looks for Date instructions and changes the dates on the FileObject as a result
     * @param fNew - File Object
     * @return - either true (if processed) or false
     */
    public static String processDates(FileObject fNew,ConfigObject c)
    {
        String dateUpdated=null;
        String param=getInstructionFromEitherField(fNew,Enums.processMode.date);
        if(param.length()>0) {
            dateUpdated = updateDate(param, fNew,c);
        }
        if(dateUpdated!=null)
        {
            updateCommentFields(fNew,param,Enums.processMode.date);
        }
        return dateUpdated;
    }
    /**
     * Creates a FileObject and Updates the file metadata (or displays information only, depending on options chosen)
     * Reads ICAFE metadata objects to use when writing back values
     * @param file - File to process
     * @param thumbName - name of thumbnail
     * @param config - Configuration Object
     * @param drive - Drive name
     * @return - returns true if successful, false if failure
     */
    public static FileObject readAndUpdateFile(File file, String thumbName, ConfigObject config, DriveObject drive, boolean readOnly) {
        FileObject fNew = initialiseFileObject();

        boolean alreadyProcessed = false;
        readSystemDates(fNew,file);
        IPTC iptc = new IPTC();
        Metadata meta=null;
        JpegExif exif = new JpegExif();
        // reads existing values from metadata
        try {
            Map<MetadataType, Metadata> metadataMap = Metadata.readMetadata(file.getPath());
            for (Map.Entry<MetadataType, Metadata> entry : metadataMap.entrySet()) {
                meta = entry.getValue();
                if (meta instanceof XMP) {
                    XMP.showXMP((XMP) meta);
                } else if (meta instanceof Exif) {
                    exif=(JpegExif)meta;
                    fNew.setWindowsComments(exif.getImageIFD().getFieldAsString(ExifTag.WINDOWS_XP_COMMENT));
                    fNew.setWindowsTitle(exif.getImageIFD().getFieldAsString(ExifTag.WINDOWS_XP_TITLE));
                    fNew.setWindowsSubject(exif.getImageIFD().getFieldAsString(ExifTag.WINDOWS_XP_SUBJECT));
                    fNew.setWindowsKeywords(exif.getImageIFD().getFieldAsString(ExifTag.WINDOWS_XP_KEYWORDS));
                    if(fNew.getWindowsKeywords()!=null ) {
                        if(fNew.getWindowsKeywords().length()>0) {
                            fNew.setWindowsKeywordsArray(new ArrayList<>(Arrays.asList(fNew.getWindowsKeywords().split(";", -1))));
                        }
                    }
                } else if (meta instanceof Comments) {
                    fNew.setComments(clearBlanks(new ArrayList<>(((Comments) meta).getComments())));

                    alreadyProcessed=checkIPTCComments(fNew.getComments(),"#"+Enums.statusValues.processed+Enums.doneValues.DONE+":");
                    if(alreadyProcessed)
                    {
                        message("File has already been processed:"+file.getName());
                        countALREADYPROCESSED++;
                        countDriveALREADYPROCESSED++;
                    }
                    else
                    {
                        message("File has not been processed:"+fNew.getComments());
                    }
                    //set event keys based on values in comments.  This will prevent reprocessing the same events again
                    fNew.setEventKeys(getEventKeysFromIPTCComments(fNew.getComments(),Enums.processMode.event));
                } else if (meta instanceof IPTC) {
                    iptc = (IPTC) meta;
                    if(!readIPTCdata(fNew,meta)){
                        message("Cannot read IPTC data");
                        addError(fNew.getFileName(),fNew.getDirectory(),fNew.getBestDate(),"Could not read IPTC data",false);
                    }
                }
            }
        } catch (Exception e) {
            message("error reading metadata section - file is probably corrupt");
            return null;
        }
        // read in JPEG metadata
        readJPEGMetadata(file, fNew);
        // the readOnly check is used when reading in files but not doing processing - it is used for reading files for testing
        // assertions only - i.e. for testing purposes only
        if(!readOnly) {
            if(config.getClear())
            {
                clearFile(fNew, config, file,   drive,  iptc, exif);
            }
            else {
                processFile(fNew, config, file, thumbName, alreadyProcessed, drive, iptc, exif);
            }
        }
        return fNew;
    }

    /**
     * Code that carries out processing on a file, prior to any updating - the flag updateRequired determines whether the file needs to be updated / moved
     * @param fNew - FileObject
     * @param config - Config Object
     * @param file - File being processed
     * @param thumbName - name of thumbnail
     * @param alreadyProcessed - flag to identify if the file has already been processed
     * @param drive - Drive object
     * @param iptc - IPTC Object
     * @param exif - Exif object
     */
    public static void processFile(FileObject fNew,ConfigObject config,File file, String thumbName,Boolean alreadyProcessed,DriveObject drive,IPTC iptc,JpegExif exif)
    {
        boolean updateRequired=false;
        //always check for duplicates
        checkDuplicateFile(fNew);

        // if it has not been processed or needs to be redone
        // We always have to process a file at least once, in order to update IPTC metadata specified on the drive
        // If this is changed, then you must redo...
        if (config.getRedo() || !alreadyProcessed) {
            updateRequired = true;
        }
        // create a thumbnail, if we need to
        File outputfile = new File(config.getTempdir() + "/" + thumbName);
        if (outputfile.exists()) {
            fNew.setThumbnail(thumbName);
        } else {
            // create a thumbnail
            fNew.setThumbnail(createThumbFromPicture(file, config.getTempdir(), thumbName, config.getWidth(), config.getHeight(), fNew.getOrientation()));
        }
        // if a date has been added to the metadata, this need processing
        String dateUpdated = processDates(fNew,config);
        if (dateUpdated != null) {
            message("File updated with a new date:" + dateUpdated);
            updateRequired=true;
        }
        // Geocodes if lat and long present
        if (fNew.getLatitude() != null && fNew.getLongitude() != null) {
            countLATLONG++;
            countDriveLATLONG++;
            if(config.getRedo() || !alreadyProcessed) {
                if(geocode(fNew, fNew.getLatitude(),fNew.getLongitude(),config,true)) {
                    updateCommentFields(fNew,fNew.getLatitude()+","+fNew.getLongitude()+":"+fNew.getPlaceKey(),Enums.processMode.geocode);
                    message("Reverse Geocoding completed");
                    updateRequired = true;
                }
            }
            else
            {
                // we still want to match the file with a Place record
                if(checkPlaceKeyFound(fNew,config))
                {
                    message("Place key found matching lat, lon values:"+fNew.getPlaceKey()+" no need to geocode");
                    countGEOCODED++;
                    countDriveGEOCODED++;
                }
                else {
                    // geocode but do not update fields - we need to do this if there is no matched place Object Place Objects...
                    geocode(fNew, fNew.getLatitude(), fNew.getLongitude(), config, false);
                    message ("Reverse geocoding done to update Place list - key is: "+fNew.getPlaceKey());
                }
            }
        }
        else
        {
            // If a location has been added to the metadata, this needs processing - only if no longitude and latitude
            if(forwardCode(config, fNew, null))
            {
                message("Forward coding done");
                updateRequired=true;
            }
        }
        // events can be processed
        if(processEvents(config, fNew)>0)
        {
            if(config.getRedo() || !alreadyProcessed) {
                message("Events processing done");
                updateRequired = true;
            }
        }
        // We could add in additional processing here
        //
        if( config.getUpdate() && (updateRequired || checkIfToBeMoved(config,fNew)) ) {
            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");

            fNew.getComments().add("#"+Enums.statusValues.processed+Enums.doneValues.DONE+":" + formatter.format(new Date()));
          //  addComment(newCommentsString, "#"+Enums.statusValues.processed+Enums.doneValues.DONE);
            fNew.setWindowsComments(appendComment(fNew.getWindowsComments(),"#"+Enums.statusValues.processed+Enums.doneValues.DONE ));
            fNew.setIPTCInstructions(appendComment(fNew.getIPTCInstructions(),"#"+Enums.statusValues.processed+Enums.doneValues.DONE));
            message("Processing with update");
            updateFile(config, drive, file, iptc, exif, fNew);
        }
    }
    /**
     * Code that carries out processing on a file - this just clears out the three comments fields of any IME instructions...
     * @param fNew - FileObject
     * @param config - Config Object
     * @param file - File being processed
     * @param drive - Drive object
     * @param iptc - IPTC Object
     * @param exif - Exif object
     */
    public static void clearFile(FileObject fNew,ConfigObject config,File file, DriveObject drive,IPTC iptc,JpegExif exif)
    {
        for(Enums.processMode p :Enums.processMode.values()) {
            fNew.setComments(removeIPTCComments(fNew.getComments(),"#"+p+Enums.doneValues.DONE+":"));
        }
        for(Enums.statusValues s :Enums.statusValues.values()) {
            fNew.setComments(removeIPTCComments(fNew.getComments(),"#"+s+Enums.doneValues.DONE+":"));
        }
        message("Clearing JPG Comments update");
        updateFile(config, drive, file, iptc, exif, fNew);

    }
    /**
     * Reverse Geocodes an Object and sets the PlaceKey
     * @param fNew - file Object
     * @param lat - latitude
     * @param lon - longitude
     * @param config - Config Object
     * @param doUpdate - update IPTC fields, if file has not been processed before, if this is false, it is geocoding but not updating

     * @return - true or false , if successful
     */
    public static Boolean geocode(FileObject fNew,Double lat,Double lon,ConfigObject config,boolean doUpdate)
    {
        Place g;
        g = checkCachedGeo(lat,lon, fNew.getBestDate(), Double.valueOf(config.getCacheDistance()));
        if (g == null) {
            waitForGeo(config.getPauseSeconds());
            g = OpenMaps.reverseGeocode(String.valueOf(lat), String.valueOf(lon), config);
            if (g != null) {
                fNew.setPlaceKey(addPlace(g, fNew.getBestDate()));
               // message("Successfully geocoded:" + g.getPlaceid());
            } else {
                countNOTGEOCODED++;
                countDriveNOTGEOCODED++;

                message("Could not geocode :Lat" + fNew.getLatitude() + ", Long:" + fNew.getLongitude());
                addError(fNew.getFileName(),fNew.getDirectory(),fNew.getBestDate(),"could not geocode:"+fNew.getLongitude()+","+fNew.getLatitude(),false);
            }
        } else {
            message("Found Lat / Long in cache : [" + g.getPlaceid() + "]" + g.getDisplay_name());
            fNew.setPlaceKey(g.getPlaceid());
        }
        if (g != null) {
            if(doUpdate) {
                setFileObjectGEOValues(fNew, g, config);

            }
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

        Collections.addAll(keys, ss);
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
     * Update file Object if event information has been found
     * @param config - Config Object
     * @param fNew - File Object
     * @param e - Event object

     */
    public static void updateEvent(ConfigObject config, FileObject fNew, EventObject e)
    {
       fNew.setWindowsTitle(addNotNull(fNew.getWindowsTitle(),e.getTitle()," "));
       fNew.setIPTCObjectName(addNotNull(fNew.getWindowsTitle(),e.getTitle()," "));
       fNew.setIPTCKeywords(addNotNull(fNew.getIPTCKeywords(),e.getKeywords(),";"));
       fNew.setWindowsSubject(addNotNull(fNew.getWindowsSubject(),e.getDescription()," "));
       // we cannot process locations for Event Calendars - just event dates
       // and only process if lat and lon is missing
       if(e.getLocation()!=null && e.getEventcalendar()==null && fNew.getLatitude()==null && fNew.getLongitude()==null)
       {
            // sets the Windows Comment to the value in order to do forward processing...
             fNew.setWindowsComments(appendComment(fNew.getWindowsComments(),e.getLocation()));
             if(forwardCode(config,fNew,e.getLocation()))
             {
                 message("Forward geocoding completed from Event");
             }
        }
        fNew.setEventKeys(fNew.getEventKeys()+e.getEventid()+";");
        updateCommentFields(fNew,e.getEventid().toString(),Enums.processMode.event);
        e.setCountEvent(e.getCountEvent()+1);

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

    /**
     * When a Date is provided, will update the EXIFOriginal and Best Date
     * Will update the IPTC date with a partial date either YYYY, YYYYMM or YYYYMMDD
     * @param param - parameter provided with the Date tag
     * @param fNew - fileObject being processed
     * @return - returns a string of the value
     */
    public static String updateDate(String param, FileObject fNew, ConfigObject config) {

        LocalDateTime c =createLocalDate(param);
        if(c!=null) {
            fNew.setExifOriginal(c);
            fNew.setBestDate(fNew.getExifOriginal());
            countDriveDateUpdate++;
            countDateUpdate++;
            if (StringUtils.isNullOrEmpty(fNew.getIPTCDateCreated()) || config.getOverwrite()) {
               fNew.setIPTCDateCreated(param.replace("-",""));
               return param;
            }
            return c.toString();
        }
        return null;
    }

    /**
     * converts from a Date to LocalDateTime
     * @param dateToConvert - Date object
     * @return - localDateTime object
     */
    public static LocalDateTime convertToLocalDateTimeViaInstant(Date dateToConvert) {
        if(dateToConvert==null)
        {
            return null;
        }
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    /**
     * Converts LocalTimeDate to an Instant - used for date conversion
     * @param dateToConvert LocalDateTime date
     * @return - returns Date object
     */
    public static Date convertToDateViaInstant(LocalDateTime dateToConvert) {
        return java.util.Date
                .from(dateToConvert.atZone(ZoneId.systemDefault())
                        .toInstant());
    }

    /**
     * Processes any Location in comments field - note it will only do this if lon and lat are not present on the file.
     * @param config - ConfigObject
     * @param fNew - FileObject being processed
     * @param eventLocation - if driven by event, this is passed through....
     * @return - returns either true of false depending on whether it has processed
     */
    public static Boolean forwardCode(ConfigObject config, FileObject fNew,String eventLocation) {

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
                            geocode(fNew, lat, lon, config,true);
                            // we should also set lat and lon if it is correct
                            if (fNew.getPlaceKey() != null) {
                                if (updateLatLon(lat, lon, fNew, config)) {
                                    countAddedLATLONG++;
                                    countDriveAddedLATLONG++;
                                    updateCommentFields(fNew,lat+","+lon,p);

                                }


                            }
                        } catch (Exception e) {
                            message("could not convert provided values to lat long:" + param);
                            addError(fNew.getFileName(), fNew.getDirectory(), fNew.getBestDate(), "could not convert provided values to lat, lon:" + param,false);
                        }
                    } else {
                        message("Incorrect number of parameters for lat long:" + param);
                        addError(fNew.getFileName(), fNew.getDirectory(), fNew.getBestDate(), "Incorrect number of parameters for lat lon:" + param,false);
                    }

                } else if (p.equals(Enums.processMode.event)) {
                    try {
                        EventObject e;
                        e = getEvent(Integer.valueOf(param));
                        if (e == null) {
                            message("This Event has not been found in the JSON - event:" + param);
                            addError(fNew.getFileName(), fNew.getDirectory(), fNew.getBestDate(), "Event not found for:" + param,false);
                        } else {
                            updateEvent(config, fNew, e);
                            fNew.setBestDate(e.getExactStartTime());
                            message("Event ID match for event:" + e.getEventid() + " " + e.getTitle());
                            countAddedEvent++;
                            countDriveAddedEvent++;
                       }
                    } catch (Exception e) {
                        message("could not convert provided values to a place:" + param);
                        addError(fNew.getFileName(), fNew.getDirectory(), fNew.getBestDate(), "Could not convert provided values to a place:" + param,false);
                    }

                } else if (p.equals(Enums.processMode.place)) {

                    try {
                        Place g;
                        g = getPlace(Integer.valueOf(param));
                        if (g == null) {
                            message("This place has not been added in the JSON - place:" + param);
                            addError(fNew.getFileName(), fNew.getDirectory(), fNew.getBestDate(), "This place does not exist in the JSON:" + param,false);
                        } else {
                            message("Place has been found - place:" + param);
                            if (updateLatLon(g.getLatAsDouble(), g.getLonAsDouble(), fNew, config)) {
                                fNew.setPlaceKey(g.getPlaceid());
                                setFileObjectGEOValues(fNew, g,  config);
                                countAddedPlace++;
                                countDriveAddedPlace++;
                                updateCommentFields(fNew,param,p);
                            }

                        }
                    } catch (Exception e) {
                        message("could not convert provided values to a place:" + param);
                        addError(fNew.getFileName(), fNew.getDirectory(), fNew.getBestDate(), "Could not convert provided values to a place:" + param,false);
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
                            if(newLat!=null) {
                                String[] values2 = newLat.split(",", -1);

                                if (values2.length == 2) {
                                    Double lat = Double.valueOf(values2[0]);
                                    Double lon = Double.valueOf(values2[1]);
                                    geocode(fNew, lat, lon, config,true);
                                    // we should also set lat and lon if it is correct
                                    if (fNew.getPlaceKey() != null) {
                                        if (updateLatLon(lat, lon, fNew, config)) {
                                            countAddedPostcode++;
                                            countDriveAddedPostcode++;
                                            updateCommentFields(fNew, param, p);
                                        }

                                    } else {
                                        addError(fNew.getFileName(), fNew.getDirectory(), fNew.getBestDate(), "Could not find postcode:" + param,false);
                                    }
                                }
                            }
                            else
                            {
                                addError(fNew.getFileName(), fNew.getDirectory(), fNew.getBestDate(), "Lat Lon is null:" + param,false);
                            }

                        } else {
                            addError(fNew.getFileName(), fNew.getDirectory(), fNew.getBestDate(), "API not available:" + param,false);
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
     * Sets Geographic fields on the fileObject from a Place object 0 6 fields and then optionally a set of keywords to be added to Windows Keywords (not IPTC keywords)
     * @param fNew - fileObject
     * @param g - placeObject
     * @param config - config object
     */
    public static void setFileObjectGEOValues(FileObject fNew,Place g, ConfigObject config)

    {
        fNew.setDisplayName(conditionallyUpdateGeoField(fNew.getDisplayName(),g.getDisplay_name(),"Display Name,",config));
        fNew.setCity(conditionallyUpdateGeoField(fNew.getCity(),g.getIPTCCity(),"City",config));
        fNew.setCountry_code(conditionallyUpdateGeoField(fNew.getCountry_code(),g.getIPTCCountryCode(),"Country Code",config));
        fNew.setCountry_name(conditionallyUpdateGeoField(fNew.getCountry_name(),g.getIPTCCountry(),"Country Name",config));
        fNew.setStateProvince(conditionallyUpdateGeoField(fNew.getStateProvince(),g.getIPTCStateProvince(),"State / Province",config));
        fNew.setSubLocation(conditionallyUpdateGeoField(fNew.getSubLocation(),g.getIPTCSublocation(),"Sub Location",config));
        if(config.getAddxpkeywords() || config.getAddiptckeywords() )
        {
            // if it is a saved Place then the keywords will need to be recreated as they are not written out to the Place
            if(g.getKeywords()==null)
            {
                g.setKeywords(assembleKeywords(g));
            }

            if(g.getKeywords().size()>0)
            {
                if(config.getAddxpkeywords()) {
                    fNew.setWindowsKeywordsArray(joinKeys(fNew.getWindowsKeywordsArray(),g.getKeywords()));
                }
                if(config.getAddiptckeywords()) {
                    fNew.setIPTCKeywordsArray(joinKeys(fNew.getIPTCKeywordsArray(), g.getKeywords()));
                }
            }
        }
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
            LocalDateTime oDate=convertToLocalDateTimeViaInstant(getTagValueDate(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL,file));
            if(oDate!=null)
            {
                fNew.setExifOriginal(oDate);
            }
            LocalDateTime dDate=convertToLocalDateTimeViaInstant(getTagValueDate(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED,file));
            if(dDate!=null) {
                fNew.setExifDigitised(dDate);
            }
            LocalDateTime tDate=convertToLocalDateTimeViaInstant(getTagValueDate(jpegMetadata, TiffTagConstants.TIFF_TAG_DATE_TIME,file));
            if(tDate!=null) {
                fNew.setTiffDate(tDate);
            }
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
                message("Error accessing exif metadata");
                addError(file.getName(),fixSlash(FilenameUtils.getFullPath(file.getPath())),convertToLocalDateTimeViaInstant(getFileDate(file)),"Error accessing exif metadata"+e,false);
            }
        }
        fNew.setDuplicate(false);

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
        fNew.setDirectory(fixSlash(FilenameUtils.getFullPath(file.getPath())));
        fNew.setFileKey(countProcessed);
        try {
            BufferedImage bimg = ImageIO.read(file);
            fNew.setWidth(bimg.getWidth());
            fNew.setHeight(bimg.getHeight());
        }
        catch(Exception e)
        {
            addError(fNew.getFileName(),fNew.getDirectory(),fNew.getBestDate(),"Error - Could not read width and height",false);
            message("Could not read width and height for "+file.getName());
        }
        fNew.setCameraKey(addCamera(fNew.getCameraMaker(),fNew.getCameraModel(), fNew.getBestDate()));
        try {
            if (getCamera(fNew.getCameraKey()).getFriendlyname() != null) {
                fNew.setCameraName(getCamera(fNew.getCameraKey()).getFriendlyname());
            }
        }
        catch(Exception e)
        {
            addError(fNew.getFileName(),fNew.getDirectory(),fNew.getBestDate(),"Error - Could not access Camera friendly name",false);
            message("Error - Could not read width and height for "+file.getName());
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
            // some of these fields may legitimately be blank, so we need to set to blank not null.

            for (MetadataEntry item : meta) {
                if (item.getKey().equals(IPTCApplicationTag.CITY.getName())) {
                    if (item.getValue().length() > 0) {
                        f.setCity(item.getValue());
                    }
                    message("IPTC City current value is:" + item.getValue());
                } else if (item.getKey().equals(IPTCApplicationTag.COUNTRY_CODE.getName())) {
                    if (item.getValue().length() > 0) {
                        f.setCountry_code(item.getValue());
                        message("IPTC Country code current value is:" + item.getValue());
                    }
                } else if (item.getKey().equals(IPTCApplicationTag.COUNTRY_NAME.getName())) {
                    if (item.getValue().length() > 0) {
                        f.setCountry_name(item.getValue());
                        message("IPTC Country name current value is:" + item.getValue());
                    }
                } else if (item.getKey().equals(IPTCApplicationTag.SUB_LOCATION.getName())) {
                    if (item.getValue().length() > 0) {
                        f.setSubLocation(item.getValue());
                        message("IPTC Sublocation current value is:" + item.getValue());
                    }
                } else if (item.getKey().equals(IPTCApplicationTag.PROVINCE_STATE.getName())) {
                    if (item.getValue().length() > 0) {
                        f.setStateProvince(item.getValue());
                        message("IPTC Province / State current value is:" + item.getValue());
                    }
                } else if (item.getKey().equals(IPTCApplicationTag.DATE_CREATED.getName())) {
                    if (item.getValue().length() > 0) {
                        f.setIPTCDateCreated(item.getValue());
                        message("IPTC Date Created current value is:" + item.getValue());
                    }
                } else if (item.getKey().equals(IPTCApplicationTag.KEY_WORDS.getName())) {
                    if (item.getValue().length() > 0) {
                        // this is a single String separated by a semicolon.. we store as an Array to simplify processing
                        f.setIPTCKeywords(item.getValue());
                        f.setIPTCKeywordsArray(new ArrayList<>(Arrays.asList(item.getValue().split(";", -1))));

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
                        f.setIPTCCaptionAbstract(item.getValue());

                        message("IPTC Description / Caption is:" + item.getValue());
                    }
                } else if (item.getKey().equals(IPTCApplicationTag.CATEGORY.getName())) {
                    // this concatenates the subject field
                    if (item.getValue().length() > 0) {
                        f.setIPTCCategory(item.getValue());
                        message("IPTC Category is:" + item.getValue());
                    }
                } else if (item.getKey().equals(IPTCApplicationTag.OBJECT_NAME.getName())) {
                    // this concatenates the title field
                    if (item.getValue().length() > 0) {
                        f.setIPTCObjectName(item.getValue());
                        message("IPTC Object Name is:" + item.getValue());
                    }
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Update an IPTC field - we want to show the value of the field even for read only mode, so we decide whether to update the field, even if we are
     * not in update mode....
     * @param currentValue - current value of the field
     * @param newValue - new value of the field
     * @param fieldName - field name
     * @param config - config object, used to determine whether to overwrite or not
     * @return - returns string
     */
    public static String conditionallyUpdateGeoField(String currentValue, String newValue,String fieldName,ConfigObject config)

    {
        // we update field if:
        // 1. the current value is blank
        // 2. overwrite is set (i.e. it will overwrite existing values)

        // note the new value might be blank after geocoding....
        if (StringUtils.isNullOrEmpty(currentValue)  || config.getOverwrite() ) {

            message("New Value found and written for:" + fieldName + " - " + newValue + "  , current value:" + currentValue);

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
     */
    public static void updateCommentFields(FileObject fNew, String param, Enums.processMode p)
    {
        fNew.setWindowsComments(updateInstructions(fNew.getWindowsComments(),p,param));
        fNew.setIPTCInstructions(updateInstructions(fNew.getIPTCInstructions(),p,param));
        DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
        fNew.getComments().add("#"+p.toString()+Enums.doneValues.DONE+":"+param+":" + formatter.format(new Date()));
      //  ArrayList comments = fNew.getComments();
      //  comments.add("#"+p.toString()+Enums.doneValues.DONE+":"+param+":" + formatter.format(new Date()));

      //fNew.setComments(comments);
        //addComment(newCommentsString,"#"+p.toString()+Enums.doneValues.DONE+":"+param);
    }

    public static void updateIPTCFields(List<IPTCDataSet> iptcs, FileObject fNew, ConfigObject config, DriveObject drive) {
        ArrayList<String> newKeys = new ArrayList<>();
        // add drive keywords
        if (drive.getIPTCKeywords() != null) {
            joinKeys(newKeys,new ArrayList<>(Arrays.asList(drive.getIPTCKeywords().split(";", -1))));
        }
        // add path keywords
        if (config.getNewdir() != null) {
            joinKeys(newKeys, convertPathToKeywords(fNew.getDirectory(), drive.getStartdir()));
        }
        // add existing keywords and location keywords - which should be in the IPTC keywords
            joinKeys(newKeys,fNew.getIPTCKeywordsArray() );

        for (String n : newKeys) {
            iptcs.add(new IPTCDataSet(IPTCApplicationTag.KEY_WORDS, n));
        }
        //put keywords back in the Array and String
        fNew.setIPTCKeywordsArray(newKeys);
        fNew.setIPTCKeywords(String.join(";", newKeys));

        if ( fNew.getCountry_code() != null) {
            iptcs.add(new IPTCDataSet(IPTCApplicationTag.COUNTRY_CODE, fNew.getCountry_code()));
        }
        if ( fNew.getCountry_name() != null) {
            iptcs.add(new IPTCDataSet(IPTCApplicationTag.COUNTRY_NAME, fNew.getCountry_name()));
        }
        if ( fNew.getStateProvince() != null) {
            iptcs.add(new IPTCDataSet(IPTCApplicationTag.PROVINCE_STATE, fNew.getStateProvince()));
        }
        if ( fNew.getSubLocation() != null){
            iptcs.add(new IPTCDataSet(IPTCApplicationTag.SUB_LOCATION, fNew.getSubLocation()));
        }
        if ( fNew.getCity() != null) {
            iptcs.add(new IPTCDataSet(IPTCApplicationTag.CITY, fNew.getCity()));
        }
        if ( fNew.getIPTCObjectName() != null) {
            iptcs.add(new IPTCDataSet(IPTCApplicationTag.OBJECT_NAME, fNew.getIPTCObjectName()));
        }
        if ( drive.getIPTCCategory() != null) {
            iptcs.add(new IPTCDataSet(IPTCApplicationTag.CATEGORY, drive.getIPTCCategory()));
            fNew.setIPTCCategory(addWithCommaIfNotNull(fNew.getIPTCCategory(),drive.getIPTCCategory()));
        }
        if ( fNew.getWindowsSubject() != null) {
            iptcs.add(new IPTCDataSet(IPTCApplicationTag.CAPTION_ABSTRACT, fNew.getWindowsSubject()));
            fNew.setIPTCCaptionAbstract(fNew.getWindowsSubject());
        }
        if (StringUtils.isNullOrEmpty(fNew.getIPTCDateCreated())) {
            // this allows partial dates to be entered....if no data is present, the BestDate is used...
            DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            iptcs.add(new IPTCDataSet(IPTCApplicationTag.DATE_CREATED, formatter.format(convertToDateViaInstant(fNew.getBestDate()))));
            fNew.setIPTCDateCreated(formatter.format(convertToDateViaInstant(fNew.getBestDate())));
        } else {

            iptcs.add(new IPTCDataSet(IPTCApplicationTag.DATE_CREATED, fNew.getIPTCDateCreated()));
        }

    }
    public static Boolean checkIfToBeMoved(ConfigObject config,FileObject fNew)
    {
        try {
            if (config.getNewdir() != null) {
                String newDirectory = getNewDirectory(config, fNew);
                return !newDirectory.equals(fNew.getDirectory());
            } else {
                return false;
            }
        }
        catch(Exception e)
        {
            message("Could not move file"+e);
            addError(fNew.getFileName(),fNew.getDirectory(),fNew.getBestDate(),"Error - Could not move file"+e,false);
            return true;
        }

    }
    public static void updateTextFile(String pathName,String oldValue,String newValue) {
        Path path = Paths.get(pathName);
        Charset charset = StandardCharsets.UTF_8;
        try {


            String content = new String(Files.readAllBytes(path), charset);
            content = content.replaceAll(oldValue, newValue);
            Files.write(path, content.getBytes(charset));
        } catch (Exception e) {
            message("Cannot update text file" + e);
        }
    }
    /**
     * Updates file and moves it.
     * First updates IPTC metadata and then substitutes the IPTC, EXIF and COMMENTS in four passes
     * It then updates the EXIF data and moves the file if required
     * Lastly, the file modified, created dates are reset to what they were before.
     * @param config - config object
     * @param drive - drive
     * @param file - filepath
     * @param iptc - IPTC metadata block
     * @param exif - EXIF metadata block
     * @param fNew - fileObject (with metadata values)
     */
    public static void updateFile(ConfigObject config,DriveObject drive, File file,IPTC iptc,JpegExif exif,FileObject fNew)
    {
            try {
               List<IPTCDataSet> iptcs = new ArrayList<>();
               updateIPTCFields(iptcs,fNew,config,drive);
                FileInputStream fin1 = new FileInputStream(file.getPath());
                String fout_name1 = FilenameUtils.getFullPath(file.getPath()) + Enums.prog.temp+"1" + FilenameUtils.getName(file.getPath());
                File outFile = new File(fout_name1);
                FileOutputStream fout1 = new FileOutputStream(outFile, false);
                List<Metadata> metaList = new ArrayList<>();
                String newDirectory=null;
                String newFileName=fNew.getFileName();
                if(config.getNewdir()!=null) {
                    newDirectory = getNewDirectory(config,fNew);
                    File newFile = new File(newDirectory + "/" + fNew.getFileName());
                    if(!fNew.getDuplicate()) {
                        if (newFile.exists()) {
                            newFileName = getUniqueFileName(newDirectory, fNew.getFileName());
                            fNew.getComments().add("#"+Enums.statusValues.renamedfile+Enums.doneValues.DONE+":" + fixSlash(fNew.getFileName()));
                          //  addComment(newCommentsString, "#"+Enums.statusValues.renamedfile+Enums.doneValues.DONE+":" + fixSlash(fNew.getFileName()));
                            fNew.setWindowsComments(fNew.getWindowsComments() + "#"+Enums.statusValues.renamedfile+Enums.doneValues.DONE+":" + fixSlash(fNew.getFileName()));
                            fNew.setIPTCInstructions(fNew.getIPTCInstructions() + "#"+Enums.statusValues.renamedfile+Enums.doneValues.DONE+":" + fixSlash(fNew.getFileName()));
                            addError(fNew.getFileName(), fNew.getDirectory(),fNew.getBestDate(), "Warning - clash in filenames in new directory :" + fixSlash(newDirectory)+" renamed to:"+newFileName,true);
                        }
                        fNew.getComments().add("#"+Enums.statusValues.movedfile+Enums.doneValues.DONE+":" + fixSlash(fNew.getDirectory()));
                    //    addComment(newCommentsString, "#"+Enums.statusValues.movedfile+Enums.doneValues.DONE+":" + fixSlash(fNew.getDirectory()));
                        fNew.setWindowsComments(fNew.getWindowsComments() + "#"+Enums.statusValues.movedfile+Enums.doneValues.DONE+":" + fixSlash(fNew.getDirectory()));
                        fNew.setIPTCInstructions(fNew.getIPTCInstructions() + "#"+Enums.statusValues.movedfile+Enums.doneValues.DONE+":" + fixSlash(fNew.getDirectory()));
                    }
                }
                // we update the Special Instructions if they have been provided....
                if (!StringUtils.isNullOrEmpty(fNew.getIPTCInstructions())) {
                    iptcs.add(new IPTCDataSet(IPTCApplicationTag.SPECIAL_INSTRUCTIONS, fNew.getIPTCInstructions()));
                }
              //  metaList.add(exif);
             //   iptc.addDataSets(iptcs);
              //  metaList.add(iptc);
              //  metaList.add(new Comments(fNew.getComments()));
                Metadata.insertIPTC(fin1, fout1,iptcs,true);
               // Metadata.insertMetadata(metaList, fin, fout);
                fin1.close();
                fout1.close();
                // now remove the comments
                FileInputStream fin2 = new FileInputStream(fout_name1);
                String fout_name2 = FilenameUtils.getFullPath(file.getPath()) + Enums.prog.temp+"2" + FilenameUtils.getName(file.getPath());
                File outFile2 = new File(fout_name2);
                FileOutputStream fout2 = new FileOutputStream(outFile2, false);
                Metadata.removeMetadata(fin2,fout2,MetadataType.COMMENT);
                fout2.close();
                fin2.close();
                // now add in the comments
                FileInputStream fin3 = new FileInputStream(fout_name2);
                String fout_name3 = FilenameUtils.getFullPath(file.getPath()) +  Enums.prog.temp+"3" + FilenameUtils.getName(file.getPath());
                File outFile3 = new File(fout_name3);
                FileOutputStream fout3 = new FileOutputStream(outFile3, false);
                Metadata.insertComments(fin3, fout3,fNew.getComments());
                fout3.close();
                fin3.close();
                // now add the exif section back in...
                /*
                FileInputStream fin4 = new FileInputStream(fout_name3);
                String fout_name4 = FilenameUtils.getFullPath(file.getPath()) + "out4" + FilenameUtils.getName(file.getPath());
                File outFile4 = new File(fout_name4);
                FileOutputStream fout4 = new FileOutputStream(outFile4, false);
                Metadata.insertExif(fin4, fout4,exif);
                fin4.close();
                fout4.close();

                 */
                if (!file.delete()) {
                    message("Cannot delete file:" + file.getPath());
                }
                //Now update the EXIF section using Apache Imaging
                if (!updateExifMetadata(outFile3, file, fNew)) {
                    message("File size is:"+fNew.getFileSize());
                    addError(fNew.getFileName(), fNew.getDirectory(), fNew.getBestDate(), "Error in updating Exif metadata - possible file corruption"+"File size:"+fNew.getFileSize(),false);
                    message("Error in updating Exif metadata");
                    try {
                        message("Trying to copy file");
                        Files.copy(outFile3.toPath(), file.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
                    }
                    catch(Exception e)
                    {
                        message("trying to move...");
                        Files.move(outFile3.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        addError(fNew.getFileName(),fNew.getDirectory(),fNew.getBestDate(),"Trying to move file3"+e,false);
                    }

                }
                File finalFile = file;
                // if we are moving the file
                if (config.getNewdir()!= null) {
                    //don't move if a duplicate.
                    if (!fNew.getDuplicate()) {

                        if (moveFile(config, fNew, newFileName)) {
                            finalFile = new File(newDirectory + "/" + newFileName);
                            fNew.setFileName(newFileName);
                            countDriveMoved++;
                            countMoved++;
                        } else {
                            message("Did not move file" + fNew.getFileName());
                            addError(fNew.getFileName(), fNew.getDirectory(), fNew.getBestDate(), "Error moving Final file",false);
                        }
                    }



                }
                Files.setAttribute(finalFile.toPath(), "creationTime", FileTime.fromMillis(convertToDateViaInstant(fNew.getFileCreated()).getTime()));
                Files.setAttribute(finalFile.toPath(), "lastAccessTime", FileTime.fromMillis(convertToDateViaInstant(fNew.getFileAccessed()).getTime()));
                Files.setAttribute(finalFile.toPath(), "lastModifiedTime", FileTime.fromMillis(convertToDateViaInstant(fNew.getFileModified()).getTime()));
                cleanUpTempFiles(file,fNew);

                countUPDATED++;
                countDriveUPDATED++;
            } catch (Exception e) {
                message("Cannot update File - it will not be moved"+e);
                cleanUpTempFiles(file,fNew);
            }

    }
    public static void cleanUpTempFiles(File file,FileObject fNew)
    {

        String fout_name3 = FilenameUtils.getFullPath(file.getPath()) + Enums.prog.temp+"3" + FilenameUtils.getName(file.getPath());
        File outFile3 = new File(fout_name3);
        String fout_name2 = FilenameUtils.getFullPath(file.getPath()) + Enums.prog.temp+"2" + FilenameUtils.getName(file.getPath());
        File outFile2 = new File(fout_name2);
        String fout_name = FilenameUtils.getFullPath(file.getPath()) +  Enums.prog.temp+"1" + FilenameUtils.getName(file.getPath());
        File outFile = new File(fout_name);
        if (!outFile.delete()) {
            addError(fNew.getFileName(),fNew.getDirectory(),fNew.getBestDate(),"Could not delete temporary file "+Enums.prog.temp+"1:"+ outFile.getName(),false);
            message("Cannot delete temp file 1:" + outFile.getPath());
        }
        if (!outFile2.delete()) {
            addError(fNew.getFileName(),fNew.getDirectory(),fNew.getBestDate(),"Could not delete temporary file "+Enums.prog.temp+"2:"+ outFile2.getName(),false);
            message("Cannot delete temp file 2:" + outFile2.getPath());
        }
        if (!outFile3.delete()) {
            addError(fNew.getFileName(),fNew.getDirectory(),fNew.getBestDate(),"Could not delete temporary file "+Enums.prog.temp+"3:"+ outFile3.getName(),false);
            message("Cannot delete temp file 3:" + outFile3.getPath());
        }
    }

    /**
     *  this uses Apache Imaging to write out EXIF information -
     * @param jpegImageFile - source image file
     * @param dst - destination image file
     */
    public static boolean updateExifMetadata(final File jpegImageFile, final File dst, FileObject fNew)
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
            // set of exif metadata. Otherwise, we keep all the other
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
            rootDir.removeField(MicrosoftTagConstants.EXIF_TAG_XPTITLE);
            if(fNew.getWindowsTitle().length()>0) {
                rootDir.add(TiffTagConstants.TIFF_TAG_IMAGE_DESCRIPTION, fNew.getWindowsTitle());
                rootDir.add(MicrosoftTagConstants.EXIF_TAG_XPTITLE, fNew.getWindowsTitle());
            }
            rootDir.removeField(MicrosoftTagConstants.EXIF_TAG_XPSUBJECT);
            if(fNew.getWindowsSubject().length()>0) {
                rootDir.add(MicrosoftTagConstants.EXIF_TAG_XPSUBJECT, fNew.getWindowsSubject());
            }
            rootDir.removeField(MicrosoftTagConstants.EXIF_TAG_XPKEYWORDS);
            if(fNew.getWindowsKeywordsArray()!=null) {
                if (fNew.getWindowsKeywordsArray().size() > 0) {
                    rootDir.add(MicrosoftTagConstants.EXIF_TAG_XPKEYWORDS, String.join(";", fNew.getWindowsKeywordsArray()));
                    fNew.setWindowsKeywords(String.join(";", fNew.getWindowsKeywordsArray()));
                }
            }
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
            s=s + "#"+pMode+ Enums.doneValues.DONE+":"+param;
        }
        else
        {
            int startPoint=s.toLowerCase().indexOf("#"+pMode+":");
            if(startPoint<0)
            {
                s=s+"#"+pMode+Enums.doneValues.DONE+":"+param;
            }
            else {
                int pos=startPoint + 1 + pMode.toString().length();
                s = s.substring(0, pos) + Enums.doneValues.DONE +
                        s.substring(pos);
            }
           // System.out.println("Updated instruction is:"+s);
        }
        return s;
    }

    /**
     * Checks the distance between two lat and lon points ( Points will be converted to radians before calculation)
     * @param lat1 - first latitude
     * @param lon1 - first longitude
     * @param lat2 - second latitude
     * @param lon2 - second longitude
     * @return - distance between first and second points (in metres)
     */
    public static double distance_Between_LatLong(double lat1, double lon1, double lat2, double lon2) {
        double THRESHOLD=0.000000001;
        lat1 = Math.toRadians(lat1);
        lon1 = Math.toRadians(lon1);
        lat2 = Math.toRadians(lat2);
        lon2 = Math.toRadians(lon2);
        if(Math.abs(lat1-lat2)<THRESHOLD && Math.abs(lon1-lon2)<THRESHOLD)
        {
            return 0.0;
        }
        double earthRadius = 6371.01; //Kilometers
        return (earthRadius * Math.acos(Math.sin(lat1)*Math.sin(lat2) + Math.cos(lat1)*Math.cos(lat2)*Math.cos(lon1 - lon2)))*1000;
    }
    /**
     * gets either a string or "unknown" string from metadata
     * @param jpegMetadata - jpeg metadata object
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
     * Checks the Events and removes any which do not have correct dates
     * There are two types of events - Calendar events where every year is checked and
     * Exact date events - where a single date is checked.
     * The exactStartTime and EndTime are set, in order to make searching efficient.
     */
    public static void checkEvents() {
        Iterator<EventObject> i = events.iterator();
        EventObject e;
        while (i.hasNext()) {

            e = i.next();
            if (e.getEventdate() == null && e.getEventcalendar() == null) {
                message("Event calendar or event date must be specified - event :" + e.getEventid() + " " + e.getTitle());
                addError("-", "-", null, "Error in Event ID: " + e.getEventid() + "- Calendar or Event date not specified",false);
                i.remove();
            }
             else if (e.getEventdate() != null && e.getEventcalendar() != null) {
                    message("You cannot specify both an Event calendar and an event date - event :" + e.getEventid() + " " + e.getTitle());
                    addError("-", "-", null, "Error in Event ID: " + e.getEventid() + "- Calendar and Event date both specified",false);
                    i.remove();
            } else {
                if (e.getEventcalendar() != null) {
                    // we have a valid calendar, so set start and end times
                    LocalDateTime d = createLocalDateCalendar(e.getEventcalendar());
                    if (d != null) {
                        e.setExactStartTime(d);
                        e.setExactEndTime(d.plusDays(1).minusNanos(1));
                        if (e.getEventtime() != null) {
                            e.setExactStartTime(e.getExactStartTime().plusHours(e.getEventtime().getHour()));
                            e.setExactStartTime(e.getExactStartTime().plusMinutes(e.getEventtime().getMinute()));
                            e.setExactStartTime(e.getExactStartTime().plusSeconds(e.getEventtime().getSecond()));
                            e.setExactStartTime(e.getExactStartTime().plusNanos(e.getEventtime().getNano()));
                        }
                        if (e.getEndtime() != null) {
                            e.setExactEndTime(e.getExactEndTime().plusHours(e.getEventtime().getHour()));
                            e.setExactEndTime(e.getExactEndTime().plusMinutes(e.getEventtime().getMinute()));
                            e.setExactEndTime(e.getExactEndTime().plusSeconds(e.getEventtime().getSecond()));
                            e.setExactEndTime(e.getExactEndTime().plusNanos(e.getEventtime().getNano()));
                        }
                        if(e.getLocation()!=null)
                        {
                            addError("-", "-", null, "Cannot add location to a Calendar Event : " + e.getEventid() ,false);
                            message("Cannot add location to a Calendar Event - " + e.getEventid());
                        }
                    } else {
                        addError("-", "-", null, "Error in Event ID: " + e.getEventid() + "-cannot parse event",false);
                        message("cannot parse date for event" + e.getEventid());
                        i.remove();
                    }
                } else {
                    // We have a specific date
                    // set the start date and time
                    e.setExactStartTime(e.getEventdate().atStartOfDay());
                    //set the end date and time
                    e.setExactEndTime(e.getEventdate().atStartOfDay().plusDays(1).minusNanos(1));
                    //if start time is present, then adjust
                    if (e.getEventtime() != null) {
                        e.setExactStartTime(e.getExactStartTime().plusHours(e.getEventtime().getHour()));
                        e.setExactStartTime(e.getExactStartTime().plusMinutes(e.getEventtime().getMinute()));
                        e.setExactStartTime(e.getExactStartTime().plusSeconds(e.getEventtime().getSecond()));
                        e.setExactStartTime(e.getExactStartTime().plusNanos(e.getEventtime().getNano()));
                    }
                    //if end date is provided then modify to the end of the day
                    if (e.getEnddate() != null) {
                        e.setExactEndTime(e.getEnddate().atStartOfDay().plusDays(1).minusNanos(1));
                    }
                    if (e.getEndtime() != null) {
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
     * @return - either true (successful) or false
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
        d.setIPTCKeywords(null);
        d.setIPTCCategory(null);
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
            countDriveALREADYPROCESSED =0;
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
            countDriveDuplicates=0;
            countDriveMoved=0;
            message("Reading drive: "+d.getStartdir());

            //recursively find all files
            readDirectoryContents(new File(d.getStartdir()), d, c.getTempdir(), c);
            messageLine("-");
            String ex=" on drive "+d.getStartdir()+" :";
            message("Files found                        "+ex + countDriveFiles);
            message("Photos found                       "+ex + countDriveImages);
            message("Photos too small                   "+ex + countDriveTooSmall);
            message("Photos to be processed             "+ex + countDriveProcessed);
            message("Photos already Processed           "+ex + countDriveALREADYPROCESSED);
            message("Photos updated                     "+ex + countDriveUPDATED);
            message("Photos where metadata not read     "+ex + countDriveErrors);
            message("Photos which have been moved       "+ex + countDriveMoved);
            message("Photos which are duplicates        "+ex + countDriveDuplicates);
            message("-----------------------------------");
            message("Photos with Lat Long               "+ex + countDriveLATLONG);
            message("Photos Geocoded                    "+ex + countDriveGEOCODED);
            message("Photos with failed Geocoding       "+ex + countDriveNOTGEOCODED);
            message("Photos where Date added            "+ex + countDriveDateUpdate);
            message("Photos where Events found from date"+ex + countDriveEventsFound);
            message("Photos with Place added            "+ex + countDriveAddedPlace);
            message("Photos with Lat Lon added          "+ex + countDriveAddedLATLONG);
            message("Photos with Postcode added         "+ex + countDriveAddedPostcode);
            message("Photos with Event added            "+ex + countDriveAddedEvent);



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
