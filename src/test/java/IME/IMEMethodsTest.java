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
package IME;
import org.junit.jupiter.api.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import static IME.IMEMethods.*;
import static org.junit.jupiter.api.Assertions.*;
class IMEMethodsTest {
    String startDir = "R:/ICTEST";
    @BeforeAll
    static void initAll() {
    }
    @BeforeEach
    void init() {
        if (!clearTestArea(startDir)) {
            fail("Setup Clear Test Area could not complete");
        }
    }
    @Test
    @DisplayName("Testing Longitude / latitude distance calculation")
    @Disabled
    void distance_Between_LatLongTest() {
        //choose two points separated by 58742 metres
        // George V Way,WD3 6 Rickmansworth,United Kingdom 51.68250194,-0.49026778
        //GU10 2 Farnham,United Kingdom 51.1796025,-0.74975167
        assertEquals(distance_Between_LatLong(51.68250194, -0.49026778, 51.1796025, -0.74975167), 58742.02440817514, 0.1);
        assertEquals(distance_Between_LatLong(51.68250194, -0.49026778, 51.6825018, -0.49026776), 0.0, 0.1);
    }
    @Test
    @DisplayName("Test 1 - Geocoding with update")
    @Disabled
    void update1Test() {
        // Test: 1 - SIMPLE GEOCODING
        // Uses TestSource1
        // One image with no IPTC metadata and with lat and lon, so should geocode
        // No Json input file, but update parameter added and New Directory provided, so will copy to TestNewDir
        // File is in a sub-directory so the old directory name is added as keywords (one for each word in directory name)
        System.out.println("==========================TEST 1 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 1, startDir + "/Test")) {
            IMEMethods.main(new String[]{startDir + "/Test", startDir + "/TestRESULTS", startDir + "/TestNewDir","update"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("JSON  file found:"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            String fileName="T_"+"nodescriptivemetadata_haslonlat.jpg";
            FileObject fNew= readAndUpdateFile(new File(startDir+"/TestNewDir/2021/8/"+fileName), null,null, null,true);
            if(fNew!=null) {
                assertEquals("Corfe Castle", fNew.getCity());
                assertEquals("GB", fNew.getCountry_code());
                assertEquals("United Kingdom", fNew.getCountry_name());
                assertEquals("Dorset, South West England", fNew.getStateProvince());
                assertEquals("", fNew.getSubLocation());
                //other metadata
                assertEquals("DirKeyword1;DirKeyword2;IPTCkey1;IPTCkey2", fNew.getIPTCKeywords());
                assertEquals("IPTCCategory", fNew.getIPTCCategory());
                assertEquals("20210820", fNew.getIPTCDateCreated());
                assertEquals("IPTC Document Title", fNew.getWindowsTitle());
                //
                assertEquals(0,fNew.getWindowsComments().indexOf("#geocodeDONE:"));
                assertEquals(0,fNew.getIPTCInstructions().indexOf("#geocodeDONE:"));
                //
                assertTrue(checkIPTCComments(fNew.getComments(),"#geocodeDONE:"));
                ConfigObject c = readConfig(startDir + "/TestRESULTS/"+jsonFile);
                assertEquals("Corfe Castle",c.getPhotos().get(0).getCity());
                assertEquals("GB", c.getPhotos().get(0).getCountry_code());
                assertEquals("United Kingdom", c.getPhotos().get(0).getCountry_name());
                assertEquals("Dorset, South West England", c.getPhotos().get(0).getStateProvince());
                assertEquals("", c.getPhotos().get(0).getSubLocation());
                assertEquals(countDriveImages,1);

            }
            else
            {
                fail("Could not find file");
            }
         } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
    @DisplayName("Test 2 - Geocoding with no overwriting of existing values")
    @Disabled
    void update2Test() {
        //Test 2 - SIMPLE GEOCODING _ NO OVERWRITING
        // Uses TestSource2
        // One image, with current ITPC data, so it should not be overwritten
        //No Json input file, but update parameter added and New Directory provided, so will copy to TestNewDir
        System.out.println("==========================TEST 2 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 2, startDir + "/Test")) {
            IMEMethods.main(new String[]{startDir + "/Test", startDir + "/TestRESULTS", startDir + "/TestNewDir","update"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("JSON  file found:"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            String fileName="T_"+"no metadata IPTC location filled in.jpg";
            FileObject fNew= readAndUpdateFile(new File(startDir+"/TestNewDir/2021/8/"+fileName), null,null, null,true);
            if(fNew!=null) {
                assertEquals("Filled in city", fNew.getCity());
                assertEquals("Filled in country", fNew.getCountry_name());
                assertEquals("Filled in province state", fNew.getStateProvince());
                assertEquals("Filled in sublocation", fNew.getSubLocation());
                //
                ConfigObject c = readConfig(startDir + "/TestRESULTS/"+jsonFile);
                assertEquals("Filled in city",c.getPhotos().get(0).getCity());
                assertEquals("GB", c.getPhotos().get(0).getCountry_code());
                assertEquals("Filled in country", c.getPhotos().get(0).getCountry_name());
                assertEquals("Filled in province state", c.getPhotos().get(0).getStateProvince());
                assertEquals("Filled in sublocation", c.getPhotos().get(0).getSubLocation());

            }
             else
             {
                    fail("Did not find output file");
             }
        } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
    @DisplayName("Test 3 - Geocoding with overwrite")
    @Disabled
    void update3Test() {
        //Test: 3 - SIMPLE GEOCODING - WITH OVERWRITING
        // Uses TestSource2
        // One image with IPTC metadata and with lat and lon, so should geocode and overwrite existing IPTC values
        //No Json input file, but update parameter added and New Directory provided, so will copy to TestNewDir, overwriteValues parameter set
        System.out.println("==========================TEST 3 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 2, startDir + "/Test")) {
            IMEMethods.main(new String[]{startDir + "/Test", startDir + "/TestRESULTS", startDir + "/TestNewDir","update","overwrite"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("JSON  file found:"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            String fileName="T_"+"no metadata IPTC location filled in.jpg";
            FileObject fNew= readAndUpdateFile(new File(startDir+"/TestNewDir/2021/8/"+fileName), null,null, null,true);
            if(fNew!=null) {
                assertEquals("Corfe Castle", fNew.getCity());
                assertEquals("GB", fNew.getCountry_code());
                assertEquals("United Kingdom", fNew.getCountry_name());
                assertEquals("Dorset, South West England", fNew.getStateProvince());
                assertEquals("", fNew.getSubLocation());
                // other metadata
                assertEquals("filled keywords 2;filled keywords1",fNew.getIPTCKeywords());
                //
                ConfigObject c = readConfig(startDir + "/TestRESULTS/"+jsonFile);
                assertEquals("Corfe Castle",c.getPhotos().get(0).getCity());
                assertEquals("GB", c.getPhotos().get(0).getCountry_code());
                assertEquals("United Kingdom", c.getPhotos().get(0).getCountry_name());
                assertEquals("Dorset, South West England", c.getPhotos().get(0).getStateProvince());
                assertEquals("", c.getPhotos().get(0).getSubLocation());

            }
            else
            {
                fail("Did not find output file:"+startDir+"/TestNewDir/2021/8/"+fileName);
            }
        } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
    @Disabled
    @DisplayName("Test 4 -  image - rotated  thumbnail           ")
    void update4Test() {
        //Test: 4 - ROTATING THUMBNAIL
        // Uses TestSource3
        // One image - Checking thumbnail is rotated correctly based on orientation - we check the width of thumbnail
        // No Json input file, but update parameter added and New Directory provided, so will copy to TestNewDir
        System.out.println("==========================TEST 4 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 3, startDir + "/Test")) {
            IMEMethods.main(new String[]{startDir + "/Test", startDir + "/TestRESULTS", startDir + "/TestNewDir","update"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("JSON  file found:"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            String fileName="T_"+"IPTC metadata not origin lat lon rotated.jpg";
            String thumbName=makeThumbName(new File(new StringBuilder().append(startDir).append("/TestNewDir/2021/8/").append(fileName).toString()));
            try {
                BufferedImage bimg = ImageIO.read(new File(startDir + "/TestRESULTS/"+thumbName));
                assertEquals(270,bimg.getWidth());
            }
            catch(Exception e)
            {
                fail("Could not read thumbnail :"+ startDir + "/TestRESULTS/"+thumbName);
            }
         } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
    @DisplayName("Test 5 - geocode - checks JSON has geocode information - read only" )
    @Disabled
    void update5Test() {
        //Test: 5 - READ ONLY -  CHECKS JSON HAS GEOCODE INFORMATION
        // Uses TestSource1
        // One image with no descriptive metadata but with lat and lon, so should geocode
        //No Json input file, read only so file will not be updated or moved (HTML output wil includeGeocode details)
        String fileName="T_"+"nodescriptivemetadata_haslonlat.jpg";
        System.out.println("==========================TEST 5 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 1, startDir + "/Test")) {
            IMEMethods.main(new String[]{startDir + "/Test", startDir + "/TestRESULTS"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("JSON  file found:"+jsonFile);
            assertNotEquals(jsonFile.length(),0);

            FileObject fNew= readAndUpdateFile(new File(startDir+"/Test/DirKeyword1 DirKeyword2/"+fileName), null,null, null,true);
            if(fNew!=null)
            {
                assertEquals("",fNew.getCity());
                assertEquals("",fNew.getCountry_code());
                assertEquals("",fNew.getCountry_name());
                assertEquals("",fNew.getStateProvince());
                assertEquals("",fNew.getSubLocation());
                //
                ConfigObject c = readConfig(startDir + "/TestRESULTS/"+jsonFile);
                assertEquals("Corfe Castle",c.getPhotos().get(0).getCity());
                assertEquals("GB", c.getPhotos().get(0).getCountry_code());
                assertEquals("United Kingdom", c.getPhotos().get(0).getCountry_name());
                assertEquals("Dorset, South West England", c.getPhotos().get(0).getStateProvince());
                assertEquals("", c.getPhotos().get(0).getSubLocation());
            } else {
                fail("Did not find output file");
            }
        } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
    @DisplayName("Test 6 - geocode - checks JSON has geocode information - update")
    @Disabled
    void update6Test() {
        //Test: 6 UPDATE GEOCODE -  CHECKS JSON HAS GEOCODE INFORMATION
        // Uses TestSource1
        // One image with no descriptive metadata but with lat and lon, so should geocode
        //No Json input file, update parameter included, but no directory provided so will not move  (HTML output wil includeGeocode details)
        System.out.println("==========================TEST 6 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 1, startDir + "/Test")) {
            IMEMethods.main(new String[]{startDir + "/Test", startDir + "/TestRESULTS","update"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("JSON  file found:"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            String fileName="T_"+"nodescriptivemetadata_haslonlat.jpg";
            FileObject fNew= readAndUpdateFile(new File(startDir+"/Test/DirKeyword1 DirKeyword2/"+fileName), null,null, null,true);
            if(fNew!=null) {
                assertEquals("Corfe Castle", fNew.getCity());
                assertEquals("GB", fNew.getCountry_code());
                assertEquals("United Kingdom", fNew.getCountry_name());
                assertEquals("Dorset, South West England", fNew.getStateProvince());
                assertEquals("", fNew.getSubLocation());
                //
                ConfigObject c = readConfig(startDir + "/TestRESULTS/"+jsonFile);
                assertEquals("Corfe Castle",c.getPhotos().get(0).getCity());
                assertEquals("GB", c.getPhotos().get(0).getCountry_code());
                assertEquals("United Kingdom", c.getPhotos().get(0).getCountry_name());
                assertEquals("Dorset, South West England", c.getPhotos().get(0).getStateProvince());
                assertEquals("", c.getPhotos().get(0).getSubLocation());
            } else {
                fail("Did not find output file");
            }
        } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
    @Disabled
    @DisplayName("Test 7")
    void update7Test() {
        //Test: 7 - HARD CODE YEAR CHECKS IPTC DATE CREATED AND KEYWORDS
        // Uses TestSource4
        // Six images with YYYY or YYYY-MMM or YYYY-MM-DD provided, should update copyright
        //Json input file, update parameter included, but no directory provided so will not move  (HTML output wil includeGeocode details)
        String fileName="IPTC-win date year.jpg";
        System.out.println("==========================TEST 7 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 4, startDir + "/Test")) {
            updateTextFile(startDir+"/Test/config.json","<<startdir>>",startDir);

            IMEMethods.main(new String[]{startDir + "/Test/config.json","overwrite"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("JSON  file found:"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
           //
            FileObject fNew= readAndUpdateFile(new File(startDir+"/Test/"+"T_"+fileName), null,null, null,true);
            if (fNew != null) {

                assertEquals("IME1;IME2;Keyword1ref2021.1;Keyword2ref2021.1;Keyword3ref2021.1",fNew.getIPTCKeywords());
                assertEquals("1985",fNew.getIPTCDateCreated());
            }
            else
            {
                fail("Did not find output file");
            }

        } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
    @DisplayName("Test 8 - Hard coded Year from Windows Comments")
    @Disabled
    void update8Test() {
        //Test: 8 - HARD CODE YEAR - CHECK FILE DATES
        // Uses TestSource4
        // Six images with YYYY or YYYY-MMM or YYYY-MM-DD provided, should update copyright
        //Json input file, update parameter included, but no directory provided so will not move  (HTML output wil includeGeocode details)
        String fileName="IPTC-win date year.jpg";
        System.out.println("==========================TEST 8 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 4, startDir + "/Test")) {
            updateTextFile(startDir+"/Test/config.json","<<startdir>>",startDir);
            setFileAttributesForTest(startDir+"/Test/"+"T_"+fileName,"1999-11-23");


            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            IMEMethods.main(new String[]{startDir + "/Test/config.json","overwrite"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("JSON  file found:"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew= readAndUpdateFile(new File(startDir+"/Test/"+"T_"+fileName), null,null, null,true);
            if (fNew != null) {
                assertEquals("1985:01:01 00:00:00",formatter.format( convertToDateViaInstant(fNew.getBestDate())));
                assertEquals("1999:11:23 00:00:00",formatter.format( convertToDateViaInstant(fNew.getFileModified())));
                assertEquals("1999:11:23 00:00:00",formatter.format( convertToDateViaInstant(fNew.getFileCreated())));
               // assertEquals("1999:11:23 00:00:00",formatter.format( convertToDateViaInstant(fNew.getFileAccessed())));
                assertEquals("1985",fNew.getIPTCDateCreated());
            }
            else
            {
                fail("Did not find output file");
            }


        } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
    @DisplayName("Test 9")
    @Disabled
    void update9Test() {
        //Test: 9 - HARD CODE YEAR AND MONTH
        // Uses TestSource4
        // Six images with YYYY or YYYY-MMM or YYYY-MM-DD provided, should update copyright
        //Json input file, update parameter included, but no directory provided so will not move  (HTML output wil includeGeocode details)
        String fileName="IPTC-win date year month.jpg";
        System.out.println("==========================TEST 9 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 4, startDir + "/Test")) {
            updateTextFile(startDir+"/Test/config.json","<<startdir>>",startDir);
            setFileAttributesForTest(startDir+"/Test/"+"T_"+fileName,"1999-11-23");
            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            IMEMethods.main(new String[]{startDir + "/Test/config.json","overwrite"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("JSON  file found:"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew= readAndUpdateFile(new File(startDir+"/Test/"+"T_"+fileName), null,null, null,true);
            if (fNew != null) {
                assertEquals("1985:07:01 00:00:00", formatter.format(convertToDateViaInstant(fNew.getBestDate())));
                assertEquals("198507",fNew.getIPTCDateCreated());
            }
            else
            {
                fail("Did not find output file");
            }
        } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
    @DisplayName("Test 10 - Add Year Month Day via Windows Comment")
    @Disabled
    void update10Test() {
        //Test: 10 - HARD CODE YEAR MONTH DAY
        // Uses TestSource4
        // Six images with YYYY or YYYY-MMM or YYYY-MM-DD provided, should update copyright
        //Json input file, update parameter included, but no directory provided so will not move  (HTML output wil includeGeocode details)
        String fileName="IPTC-win date year month day.jpg";
        System.out.println("==========================TEST 10 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 4, startDir + "/Test")) {
            updateTextFile(startDir+"/Test/config.json","<<startdir>>",startDir);
            setFileAttributesForTest(startDir+"/Test/"+"T_"+fileName,"1999-11-23");

            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            IMEMethods.main(new String[]{startDir + "/Test/config.json","overwrite"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("JSON  file found:"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew= readAndUpdateFile(new File(startDir+"/Test/"+"T_"+fileName), null,null, null,true);
            if (fNew != null) {
                assertEquals("1985:07:14 00:00:00", formatter.format(convertToDateViaInstant(fNew.getBestDate())));
                assertEquals("19850714",fNew.getIPTCDateCreated());
            }
             else
                {
                    fail("Did not find output file");
                }

        } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
    @DisplayName("Test 11 - Add Year via IPTC Instruction")
    @Disabled
    void update11Test() {
        //Test: 11 - HARD CODE DATE USING IPTC INSTRUCTIONS FIELD
        // Uses TestSource4
        // Six images with YYYY or YYYY-MMM or YYYY-MM-DD provided, should update copyright
        //Json input file, update parameter included, but no directory provided so will not move  (HTML output wil includeGeocode details)
        String fileName="lightroom date year.jpg";
        System.out.println("==========================TEST 11 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 4, startDir + "/Test")) {
            updateTextFile(startDir+"/Test/config.json","<<startdir>>",startDir);
            setFileAttributesForTest(startDir+"/Test/"+"T_"+fileName,"1999-11-23");

            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            IMEMethods.main(new String[]{startDir + "/Test/config.json","overwrite"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("JSON  file found:"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew= readAndUpdateFile(new File(startDir+"/Test/"+"T_"+fileName), null,null, null,true);
            if (fNew != null) {
                assertEquals("2001:01:01 00:00:00",formatter.format( convertToDateViaInstant(fNew.getBestDate())));
                assertEquals("2001",fNew.getIPTCDateCreated());
            }
            else
            {
                fail("Did not find output file");
            }
        } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
    @DisplayName("Test 12")
    @Disabled
    void update12Test() {
        //Test: 7 - HARD CODE DATE USING IPTC INSTRUCTIONS FIELD YEAR AND MONTH
        // Uses TestSource4
        // Six images with YYYY or YYYY-MMM or YYYY-MM-DD provided, should update copyright
        //Json input file, update parameter included, but no directory provided so will not move  (HTML output wil includeGeocode details)
        String fileName="lightroom date year month.jpg";
        System.out.println("==========================TEST 12 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 4, startDir + "/Test")) {
            updateTextFile(startDir+"/Test/config.json","<<startdir>>",startDir);
            setFileAttributesForTest(startDir+"/Test/"+"T_"+fileName,"1999-11-23");
            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            IMEMethods.main(new String[]{startDir + "/Test/config.json","overwrite"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("JSON  file found:"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew= readAndUpdateFile(new File(startDir+"/Test/"+"T_"+fileName), null,null, null,true);
            if (fNew != null) {
                assertEquals("2001:07:01 00:00:00",formatter.format( convertToDateViaInstant(fNew.getBestDate())));
                assertEquals("200107",fNew.getIPTCDateCreated());
            }
            else
            {
                fail("Did not find output file");
            }

        } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
    @DisplayName("Test 13")
    @Disabled
    void update13Test() {
        //Test: 13 - HARD CODE DATE USING IPTC INSTRUCTIONS FIELD YEAR AND MONTH
        // Uses TestSource4
        // Six images with YYYY or YYYY-MMM or YYYY-MM-DD provided, should update copyright
        //Json input file, update parameter included, but no directory provided so will not move  (HTML output wil includeGeocode details)
        String fileName="lightroom date year month day.jpg";
        System.out.println("==========================TEST 13 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 4, startDir + "/Test")) {
            updateTextFile(startDir+"/Test/config.json","<<startdir>>",startDir);
            setFileAttributesForTest(startDir+"/Test/"+"T_"+fileName,"1999-11-23");

            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            IMEMethods.main(new String[]{startDir + "/Test/config.json","overwrite"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("JSON  file found:"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew= readAndUpdateFile(new File(startDir+"/Test/"+"T_"+fileName), null,null, null,true);
            if (fNew != null) {
                assertEquals("2001:07:14 00:00:00",formatter.format( convertToDateViaInstant(fNew.getBestDate())));
                assertEquals("20010714",fNew.getIPTCDateCreated());
            }
            else
            {
                fail("Did not find output file");
            }

        } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
    @Disabled
    @DisplayName("Test 14")
    void update14Test() {
        // Test 14 :  HARD CODE EVENT WITH A POSTCODE
        // Uses TestSource5
        // 11 images where events are added or found via the date of the photo
        //Json input file, update and overwrite parameters included, directory provided so will move  (HTML output wil includeGeocode details)
        System.out.println("==========================TEST 14 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 5, startDir + "/Test")) {
            updateTextFile(startDir+"/Test/config.json","<<startdir>>",startDir);
            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            IMEMethods.main(new String[]{startDir + "/Test/config.json"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("JSON  file found:"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew;
            fNew= readAndUpdateFile(new File(startDir+"/TestNewDir/2019/4/"+"T_"+"Added event with postcode.jpg"), null,null, null,true);
            if (fNew != null) {
                assertEquals("2019:04:14 12:30:00",formatter.format( convertToDateViaInstant(fNew.getBestDate())));
                assertEquals(	"London",fNew.getCity());
            }
            else
            {
                fail("Did not find output file");
            }


        } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
    @Disabled
    @DisplayName("Test 15")
    void update15Test() {
        // Test 15 - HARD CODE EVENT WITH PLACE
        // Uses TestSource5
        // 11 images where events are added or found via the date of the photo
        //Json input file, update and overwrite parameters included, directory provided so will move  (HTML output wil includeGeocode details)
        System.out.println("==========================TEST 15 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 5, startDir + "/Test")) {
            updateTextFile(startDir+"/Test/config.json","<<startdir>>",startDir);
            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            IMEMethods.main(new String[]{startDir + "/Test/config.json"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("JSON  file found:"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew;
            fNew= readAndUpdateFile(new File(startDir+"/TestNewDir/2019/7/"+"T_"+"Added event with place.jpg"), null,null, null,true);
            if (fNew != null) {
                assertEquals("2019:07:21 00:00:00",formatter.format( convertToDateViaInstant(fNew.getBestDate())));
                assertEquals(	"Larkhill",fNew.getCity());
            }
            else
            {
                fail("Did not find output file");
            }


          } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
    @Disabled
    @DisplayName("Test 16")
    void update16Test() {
        // Test 16 - FIND EVENT WITH PLACE
        // Uses TestSource5
        // 11 images where events are added or found via the date of the photo
        //Json input file, update and overwrite parameters included, directory provided so will move  (HTML output wil includeGeocode details)
        System.out.println("==========================TEST 16 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 5, startDir + "/Test")) {
            updateTextFile(startDir+"/Test/config.json","<<startdir>>",startDir);
            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            IMEMethods.main(new String[]{startDir + "/Test/config.json"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("JSON  file found:"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew;
            fNew= readAndUpdateFile(new File(startDir+"/TestNewDir/2019/7/"+"T_"+"Find event date place.jpg"), null,null, null,true);
            if (fNew != null) {
                assertEquals("2019:07:21 21:01:01",formatter.format( convertToDateViaInstant(fNew.getBestDate())));
                assertEquals(	"Larkhill",fNew.getCity());
            }
            else
            {
                fail("Did not find output file");
            }


        } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
     @Disabled
    @DisplayName("Test 17")
    void update17Test() {
        // Test 17 - FIND EVENT WITH NO PLACE
        // Uses TestSource5
        // 11 images where events are added or found via the date of the photo
        //Json input file, update and overwrite parameters included, directory provided so will move  (HTML output wil includeGeocode details)
        System.out.println("==========================TEST 17 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 5, startDir + "/Test")) {
            updateTextFile(startDir+"/Test/config.json","<<startdir>>",startDir);
            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            IMEMethods.main(new String[]{startDir + "/Test/config.json"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("JSON  file found:"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew;
            fNew= readAndUpdateFile(new File(startDir+"/TestNewDir/2020/6/"+"T_"+"Find event date time no place.jpg"), null,null, null,true);
            if(fNew!=null) {
                assertEquals("2020:06:23 21:01:01", formatter.format(convertToDateViaInstant(fNew.getBestDate())));
            }
             else
                {
                    fail("Did not find output file");
                }
        } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
    @Disabled
    @DisplayName("Test 18")
    void update18Test() {
        // Test 18 - FIND EVENT WITH POSTCODE
        // Uses TestSource5
        // 11 images where events are added or found via the date of the photo
        //Json input file, update and overwrite parameters included, directory provided so will move  (HTML output wil includeGeocode details)
        System.out.println("==========================TEST 18 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 5, startDir + "/Test")) {
            updateTextFile(startDir+"/Test/config.json","<<startdir>>",startDir);
            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            IMEMethods.main(new String[]{startDir + "/Test/config.json"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("JSON  file found:"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew;


            fNew= readAndUpdateFile(new File(startDir+"/TestNewDir/2021/7/"+"T_"+"Find event date postcode.jpg"), null,null, null,true);
            if(fNew!=null) {
                assertEquals("2021:07:21 21:01:01", formatter.format(convertToDateViaInstant(fNew.getBestDate())));
                assertEquals("London", fNew.getCity());
            }
            else
            {
                fail("Did not find output file");
            }
         } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
     @Disabled
    @DisplayName("Test 19")
    void update19Test() {
        // Test 19 - ADD EVENT WITH LAT, LON
        // Uses TestSource5
        // 11 images where events are added or found via the date of the photo
        //Json input file, update and overwrite parameters included, directory provided so will move  (HTML output wil includeGeocode details)
        System.out.println("==========================TEST 19 =================================");

        if (copyToTestArea(startDir + "/TestSource" + 5, startDir + "/Test")) {
            updateTextFile(startDir + "/Test/config.json", "<<startdir>>", startDir);
            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            IMEMethods.main(new String[]{startDir + "/Test/config.json"});
            String jsonFile = findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("JSON  file found:" + jsonFile);
            assertNotEquals(jsonFile.length(), 0);
            //
            FileObject fNew;
            fNew = readAndUpdateFile(new File(startDir + "/TestNewDir/2021/8/" + "T_" + "Added event withlatlon.jpg"), null, null, null, true);
            if (fNew != null) {
                assertEquals("2021:08:01 00:00:00", formatter.format(convertToDateViaInstant(fNew.getBestDate())));
                assertEquals("", fNew.getCity());
            } else {
                fail("Did not find output file");
            }
        }else
        {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
     @Disabled
    @DisplayName("Test 20")
    void update20Test() {
        // Test 20 - FIND EVENT WITH LAT, LON
        // Uses TestSource5
        // 11 images where events are added or found via the date of the photo
        //Json input file, update and overwrite parameters included, directory provided so will move  (HTML output wil includeGeocode details)
        System.out.println("==========================TEST 20 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 5, startDir + "/Test")) {
            updateTextFile(startDir+"/Test/config.json","<<startdir>>",startDir);
            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            IMEMethods.main(new String[]{startDir + "/Test/config.json"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("JSON  file found:"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew;
              //
            fNew= readAndUpdateFile(new File(startDir+"/TestNewDir/2021/8/"+"T_"+"Find event date latlon.jpg"), null,null, null,true);
            if(fNew!=null) {
                assertEquals("2021:08:02 21:01:01", formatter.format(convertToDateViaInstant(fNew.getBestDate())));
                assertEquals("", fNew.getCity());
            } else {
                fail("Did not find output file");
            }

        } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
     @Disabled
    @DisplayName("Test 21")
    void update21Test() {
        // Test 21 - FIND EVENT CALENDAR - NO PLACE
        // Uses TestSource5
        // 11 images where events are added or found via the date of the photo
        //Json input file, update and overwrite parameters included, directory provided so will move  (HTML output wil includeGeocode details)
        System.out.println("==========================TEST 21 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 5, startDir + "/Test")) {
            updateTextFile(startDir+"/Test/config.json","<<startdir>>",startDir);
            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            IMEMethods.main(new String[]{startDir + "/Test/config.json"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("JSON  file found:"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew;
            fNew= readAndUpdateFile(new File(startDir+"/TestNewDir/2021/9/"+"T_"+"find eventcalendar no place.jpg"), null,null, null,true);
            if(fNew!=null)
            {
            assertEquals("2021:09:12 21:01:01",formatter.format( convertToDateViaInstant(fNew.getBestDate())));
            } else {
                fail("Did not find output file");
            }

        } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
     @Disabled
    @DisplayName("Test 22")
    void update22Test() {
        // Test 22 -HARD CODE LAT,LON
        // Uses TestSource5
        // 11 images where events are added or found via the date of the photo
        //Json input file, update and overwrite parameters included, directory provided so will move  (HTML output wil includeGeocode details)
        System.out.println("==========================TEST 22 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 5, startDir + "/Test")) {
            updateTextFile(startDir+"/Test/config.json","<<startdir>>",startDir);
            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            IMEMethods.main(new String[]{startDir + "/Test/config.json"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("JSON  file found:"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew;
            fNew= readAndUpdateFile(new File(startDir+"/TestNewDir/2021/10/"+"T_"+"Added latlon.jpg"), null,null, null,true);
            if(fNew!=null)
            {
                assertEquals("2021:10:20 21:01:01",formatter.format( convertToDateViaInstant(fNew.getBestDate())));
                assertEquals(	"London",fNew.getCity());
            } else {
                fail("Did not find output file");
            }

        } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
     @Disabled
    @DisplayName("Test 23")
    void update23Test() {
        // Test 23 -HARD CODE PLACE
        // Uses TestSource5
        // 11 images where events are added or found via the date of the photo
        //Json input file, update and overwrite parameters included, directory provided so will move  (HTML output wil includeGeocode details)
        System.out.println("==========================TEST 23 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 5, startDir + "/Test")) {
            updateTextFile(startDir+"/Test/config.json","<<startdir>>",startDir);
            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            IMEMethods.main(new String[]{startDir + "/Test/config.json"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("JSON  file found:"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew;
            fNew= readAndUpdateFile(new File(startDir+"/TestNewDir/2021/10/"+"T_"+"Added place.jpg"), null,null, null,true);
            if(fNew!=null)
            {
            assertEquals("2021:10:20 21:01:01",formatter.format( convertToDateViaInstant(fNew.getBestDate())));
            assertEquals(	"Larkhill",fNew.getCity());
            } else {
                fail("Did not find output file");
            }

        } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
     @Disabled
    @DisplayName("Test 24")
    void update24Test() {
        // Test 24 -HARD CODE POST CODE
        // Uses TestSource5
        // 11 images where events are added or found via the date of the photo
        //Json input file, update and overwrite parameters included, directory provided so will move  (HTML output wil includeGeocode details)
        System.out.println("==========================TEST 24 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 5, startDir + "/Test")) {
            updateTextFile(startDir+"/Test/config.json","<<startdir>>",startDir);
            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            IMEMethods.main(new String[]{startDir + "/Test/config.json"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("JSON  file found:"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew;
            fNew= readAndUpdateFile(new File(startDir+"/TestNewDir/2021/10/"+"T_"+"Addedpostcode.jpg"), null,null, null,true);
            if(fNew!=null) {
                assertEquals("2021:10:20 21:01:01", formatter.format(convertToDateViaInstant(fNew.getBestDate())));
                assertEquals("London", fNew.getCity());
            } else {
                fail("Did not find output file");
            }
        } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
    @DisplayName("Test 25")
    @Disabled
    void update25Test() {
        // Test: 25 - MOVING DUPLICATE FILES AND RENAMING
        // Uses TestSource6
        // checking duplicate images - two are the same in different folders, other two are different with same name
        // No Json input file, but update parameter added and New Directory provided, so will copy to TestNewDir
        // File is in a sub-directory so the old directory name is added as keywords (one for each word in directory name)
        // Duplicate file should not be moved...
        //Duplicate file name should be moved but with different name _001 added to file root.
        System.out.println("==========================TEST 25 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 6, startDir + "/Test")) {
            IMEMethods.main(new String[]{startDir + "/Test", startDir + "/TestRESULTS", startDir + "/TestNewDir","update"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("JSON  file found:"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            File f_notmoved= new File(startDir + "/Test/T_WP_20150611_001.jpg");
            File f_moved=new File(startDir + "/TestNewDir/2015/6/T_WP_20150611_001.jpg");
            File f_moved2=new File(startDir + "/TestNewDir/2015/6/T_WP_20150611_004.jpg");
            File f_moved3=new File(startDir + "/TestNewDir/2015/6/T_WP_20150611_004_001.jpg");
            assertTrue(f_notmoved.exists());
            assertTrue(f_moved.exists());
            assertTrue(f_moved2.exists());
            assertTrue(f_moved3.exists());
        } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
    @DisplayName("Test 26 - Checks that metadata written to Config Output file")
    @Disabled
    void update26Test() {
        //Test: 26 - CHECKS THAT METADATA FIELDS ARE ALL READ
        // Uses TestSource4
        // Six images with YYYY or YYYY-MMM or YYYY-MM-DD provided, should update copyright
        //Json input file, update parameter included, but no directory provided so will not move  (HTML output wil includeGeocode details)
        String fileName="lightroom date year month day.jpg";
        System.out.println("==========================TEST 26 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 4, startDir + "/Test")) {
            IMEMethods.main(new String[]{startDir + "/Test", startDir + "/TestRESULTS", startDir + "/TestNewDir"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("JSON  file found:"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew= readAndUpdateFile(new File(startDir+"/Test/"+"T_"+fileName), null,null, null,true);
            if (fNew != null) {

                //
                ConfigObject c = readConfig(startDir + "/TestRESULTS/"+jsonFile);
                assertEquals("The Title (ref2021.1)",c.getPhotos().get(0).getIPTCObjectName());
                assertEquals("The description aka caption (ref2021.1)", c.getPhotos().get(0).getIPTCCaptionAbstract());


            }
            else
            {
                fail("Did not find output file");
            }

        } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
    @DisplayName("Test 27 - Checks all metadata fields are read")
    @Disabled
    void update27Test() {
        //Test: 26 - CHECKS THAT METADATA FIELDS ARE ALL READ
        // Uses TestSource4
        // Six images with YYYY or YYYY-MMM or YYYY-MM-DD provided, should update copyright
        //Json input file, update parameter included, but no directory provided so will not move  (HTML output wil includeGeocode details)
        String fileName="lightroom date year month day.jpg";
        System.out.println("==========================TEST 27 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 4, startDir + "/Test")) {
            //
            FileObject fNew= readAndUpdateFile(new File(startDir+"/Test/"+"T_"+fileName), null,null, null,true);
            if (fNew != null) {
                assertEquals("20211020",fNew.getIPTCDateCreated());
                assertEquals(3,fNew.getIPTCKeywordsArray().size());
                assertEquals("The Title (ref2021.1)",fNew.getIPTCObjectName());
                assertEquals("The description aka caption (ref2021.1)",fNew.getIPTCCaptionAbstract());
            }
            else
            {
                fail("Did not find output file");
            }

        } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
    @DisplayName("Test 28 - Checks that XP Keywords and IPTC Keywords are written")
    @Disabled
    void update28Test() {
        // Test: 1 - SIMPLE GEOCODING
        // Uses TestSource1
        // One image with no IPTC metadata and with lat and lon, so should geocode
        // No Json input file, but update parameter added and New Directory provided, so will copy to TestNewDir
        // File is in a sub-directory so the old directory name is added as keywords (one for each word in directory name)
        System.out.println("==========================TEST 28 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 1, startDir + "/Test")) {
            IMEMethods.main(new String[]{startDir + "/Test", startDir + "/TestRESULTS", startDir + "/TestNewDir","update","addxpkeywords","addiptckeywords"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("JSON  file found:"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            String fileName="T_"+"nodescriptivemetadata_haslonlat.jpg";
            FileObject fNew= readAndUpdateFile(new File(startDir+"/TestNewDir/2021/8/"+fileName), null,null, null,true);
            if(fNew!=null) {
                              //other metadata
                assertEquals("DirKeyword1;DirKeyword2;IPTCkey1;IPTCkey2;GB;United Kingdom;Dorset;BH20 5DY;England;South West England;Corfe Castle", fNew.getIPTCKeywords());
                assertEquals("GB;United Kingdom;Dorset;BH20 5DY;England;South West England;Corfe Castle", fNew.getWindowsKeywords());
                ConfigObject c = readConfig(startDir + "/TestRESULTS/"+jsonFile);
                assertEquals("DirKeyword1;DirKeyword2;IPTCkey1;IPTCkey2;GB;United Kingdom;Dorset;BH20 5DY;England;South West England;Corfe Castle",c.getPhotos().get(0).getIPTCKeywords());
                assertEquals("GB;United Kingdom;Dorset;BH20 5DY;England;South West England;Corfe Castle",c.getPhotos().get(0).getWindowsKeywords());
            }
            else
            {
                fail("Could not find file");
            }
        } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
    @DisplayName("Test 29 - Checks that Processed file not reprocessed - geocoded")
    @Disabled
    void update29Test() {
        // Test: 1 - SIMPLE GEOCODING
        // Uses TestSource1
        // One image with no IPTC metadata and with lat and lon, so should geocode
        // No Json input file, but update parameter added and New Directory provided, so will copy to TestNewDir
        // File is in a sub-directory so the old directory name is added as keywords (one for each word in directory name)
        System.out.println("==========================TEST 29 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 9, startDir + "/Test")) {
            IMEMethods.main(new String[]{startDir + "/Test",startDir + "/TestRESULTS","update"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("JSON  file found:"+jsonFile);
            assertEquals(countDriveALREADYPROCESSED,1);
            assertEquals(countDriveUPDATED,0);
            String fileName="T_"+"already_geocoded.jpg";
            FileObject fNew= readAndUpdateFile(new File(startDir+"/Test/"+fileName), null,null, null,true);
            if(fNew!=null) {
                //other metadata
                assertEquals("#geocodeDONE:50.655271666666664,-2.0567166666666665:1#processedDONE:2022:05:05 18:01:00#movedfileDONE:R:/ICTEST/Test/DirKeyword1 DirKeyword2/", fNew.getWindowsComments());
                assertEquals(3,fNew.getComments().size());
            }
            else
            {
                fail("Could not find file");
            }
        } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
    @DisplayName("Test 30 - Checks that Processed file not reprocessed - Date")
    @Disabled
    void update30Test() {
        // Test: 1 - SIMPLE GEOCODING
        // Uses TestSource1
        // One image with no IPTC metadata and with lat and lon, so should geocode
        // No Json input file, but update parameter added and New Directory provided, so will copy to TestNewDir
        // File is in a sub-directory so the old directory name is added as keywords (one for each word in directory name)
        System.out.println("==========================TEST 30 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 8, startDir + "/Test")) {
            IMEMethods.main(new String[]{startDir + "/Test", startDir + "/TestRESULTS","update"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("JSON  file found:"+jsonFile);
            assertEquals(countDriveALREADYPROCESSED,1);
            assertEquals(countDriveUPDATED,0);
            String fileName="T_"+"IPTC-already processed.jpg";
            FileObject fNew= readAndUpdateFile(new File(startDir+"/Test/"+fileName), null,null, null,true);
            if(fNew!=null) {
                //other metadata
                assertEquals("#dateDONE:1985#processedDONE:2022:05:05 17:39:25", fNew.getWindowsComments());
                assertEquals(4,fNew.getComments().size());
            }
            else
            {
                fail("Could not find file");
            }
        } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
    @DisplayName("Test 31 - Checks that Processed file reprocessed with REDO - geocoded")
    @Disabled
    void update31Test() {
        // Test: 1 - SIMPLE GEOCODING
        // Uses TestSource1
        // One image already processed, so should redo
        // No Json input file, but update parameter added and New Directory provided, so will copy to TestNewDir
        // File is in a sub-directory so the old directory name is added as keywords (one for each word in directory name)
        System.out.println("==========================TEST 31 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 9, startDir + "/Test")) {
            IMEMethods.main(new String[]{startDir + "/Test",startDir + "/TestRESULTS","update", "redo"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("JSON  file found:"+jsonFile);
            assertEquals(countDriveALREADYPROCESSED,1);
            assertEquals(countDriveUPDATED,1);
            String fileName="T_"+"already_geocoded.jpg";
            FileObject fNew= readAndUpdateFile(new File(startDir+"/Test/"+fileName), null,null, null,true);
            if(fNew!=null) {
                //Should be original 3 commments + 2 more for redo of geocode and processed
                assertEquals(5,fNew.getComments().size());
            }
            else
            {
                fail("Could not find file");
            }
        } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
    @DisplayName("Test 32 - Checks CLEAR option")
     @Disabled
    void update32Test() {
        // Test: 1 - CLEAR option
        // Uses TestSource1
        // One image with no IPTC metadata and with lat and lon, so should geocode
        // No Json input file, but update parameter added and New Directory provided, so will copy to TestNewDir
        // File is in a sub-directory so the old directory name is added as keywords (one for each word in directory name)
        System.out.println("==========================TEST 32 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 9, startDir + "/Test")) {
            IMEMethods.main(new String[]{startDir + "/Test",startDir + "/TestRESULTS","update","clear"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("JSON  file found:"+jsonFile);
            assertEquals(countDriveALREADYPROCESSED,1);
            assertEquals(countDriveUPDATED,1);
            String fileName="T_"+"already_geocoded.jpg";
            FileObject fNew= readAndUpdateFile(new File(startDir+"/Test/"+fileName), null,null, null,true);
            if(fNew!=null) {
                //other metadata
                  assertEquals(0, fNew.getComments().size());
            }
            else
            {
                fail("Could not find file");
            }
        } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
    @DisplayName("Test 33 - Check files are created")
        //@Disabled
    void update33Test() {
        // Test: 33 - SIMPLE GEOCODING - CHECKS FILES ARE created
        // Uses TestSource1
        // One image with no IPTC metadata and with lat and lon, so should geocode
        // No Json input file, but update parameter added and New Directory provided, so will copy to TestNewDir
        // File is in a sub-directory so the old directory name is added as keywords (one for each word in directory name)
        System.out.println("==========================TEST 1 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 1, startDir + "/Test")) {
            IMEMethods.main(new String[]{startDir + "/Test", startDir + "/TestRESULTS", startDir + "/TestNewDir","update"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("JSON  file found:"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            File f = new File(startDir + "/TestRESULTS/photosbydate.html");
            assertTrue(f.exists());
            f=new File(startDir + "/TestRESULTS/cameras.html");
            assertTrue(f.exists());
            f=new File(startDir + "/TestRESULTS/errors.html");
            assertTrue(f.exists());
            f=new File(startDir + "/TestRESULTS/duplicates.html");
            assertTrue(f.exists());
            f=new File(startDir + "/TestRESULTS/events.html");
            assertTrue(f.exists());
            f=new File(startDir + "/TestRESULTS/places.html");
            assertTrue(f.exists());
            f=new File(startDir + "/TestRESULTS/tracks.html");
            assertTrue(f.exists());
            f=new File(startDir + "/TestRESULTS/point.kml");
            assertTrue(f.exists());
            f=new File(startDir + "/TestRESULTS/track.kml");
            assertTrue(f.exists());
            
            
        } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
}