package IC;
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
import java.io.IOException;
import java.util.List;

import static IC.ImageProcessing.createThumbFromPicture;

// run -include "d:~c:\Photos" -exclude "d:\IC\exclude.txt" -temp "d:\IC\temp" -crypto key -openmapskey sadfsdadad
//
public class ImageCatalogue {
    static int count = 0;
    static String[] excludeDir;
    static String startDir;
    static String tempDir ;

    public static void main(String[] args) {
        startDir = "D:\\";
        tempDir = "D:\\tempIC";
        excludeDir = "$RECYCLE.~Audacity~Work Dump\\Visual Studio 2013_old~Work Resources\\Capgemini~Tomcat 8.5~Program Files\\Java~Windows~Ted\\Documents\\Visual Studio 2013~Ted\\Documents\\Family Tree Maker~Place3~Python27~Place2~Program Files (x86)~VisualStudio2013~.pnpm-store~AppData~apache-tomcat-8.0.21~Eclipse~eclipse-workspace~Eclipseoxygen~FamilyTreeMaker~git_old~Lightroom2~More Software~MusicBee~Motor18~motor18download".split("~", -1);
        System.out.println("Starting");
        if (createTempDirForUser(tempDir)) {
            //recursively find all file
            readDirectoryContents(new File(startDir));
        }
        System.out.println("End of program" + "Photos found:" + count);


    }
    //recursively find files


    // convert grid reference to Google Coordinates

    // read metadata;


    // create objects for every file

    // sort object name

    // check duplicates....
    public static void readDirectoryContents(File dir) {
        try {
            File[] files = dir.listFiles();

            for (File file : files) {
                try {
                    if (file.isDirectory()) {
                        //System.out.println("directory:" + file.getCanonicalPath());
                        if (!isExcluded(file.getCanonicalPath())) {
                            if(!file.getCanonicalPath().equals(tempDir)) {
                                readDirectoryContents(file);
                            }
                        }

                    } else {
                        if (isImage(file.getName())) {
                            long fileSize = file.length();
                            long minSize=4880L;
                            System.out.println("File size is:"+fileSize);
                            if(fileSize > minSize) {
                                count++;
                                System.out.println(count + "     file:" + file.getCanonicalPath() + ",Name:" + file.getName());
                                displayMetadata(file);
                                String thumbName = makeThumbName(file, startDir);
                                System.out.println("Thumbfilename is:" + thumbName);
                                createThumbFromPicture(file,tempDir,thumbName,400,400);
                            }
                            else
                            {
                                System.out.println("*********************file:" + file.getCanonicalPath() + ",Name:" + file.getName());
                                System.out.println("*********************File too small - size is:"+fileSize);
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

    public static boolean createTempDirForUser(String temp) {
        try {
            new File(temp).mkdir();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public static String makeThumbName(File f,String root)
    {
        try {
            String newName = f.getCanonicalPath().replace(root, "").replace("\\", "_");
            //remove the root

            // replace any directory slashes with underscores
            return newName;
        }
        catch(Exception e)
        {
            System.out.println("Could not determine the Thumbname");
            return root;
        }

    }
    public static Boolean isExcluded(String fname) {
        for (int i = 0; i < excludeDir.length; i++) {
            if (fname.equals(startDir + excludeDir[i])) {
                System.out.println("Excluded:" + fname);
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

    public static Boolean displayMetadata(File file) {
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
                        final String gpsDescription = gpsInfo.toString();
                        final double longitude = gpsInfo.getLongitudeAsDegreesEast();
                        final double latitude = gpsInfo.getLatitudeAsDegreesNorth();

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
            }
            return true;
        }
        catch(Exception e) {

            System.out.println(e);
            return false;
        }


    }


}
