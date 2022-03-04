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
import org.junit.jupiter.api.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import static IC.ImageCatalogue.*;
import static org.junit.jupiter.api.Assertions.*;
class ImageCatalogueTest {
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
    @DisplayName("Test 1")

    void update1Test() {
        // Test: 1
        // One image with no IPTC metadata but with lat and lon, so should geocode
        // No Json input file, but update parameter added and New Directory provided, so will copy to TestNewDir
        // File is in a sub-directory so the old directory name is added as keywords (one for each word in directory name)
        System.out.println("==========================TEST 1 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 1, startDir + "/Test")) {
            ImageCatalogue.main(new String[]{startDir + "/Test", startDir + "/TestRESULTS", startDir + "/TestNewDir","update"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("json file found"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            String fileName="T_"+"nodescriptivemetadata_haslonlat.jpg";
            FileObject fNew=processFile(new File(startDir+"/TestNewDir/2021/8/"+fileName), null,null, null,true);
            if(fNew!=null) {
                assertEquals("Corfe Castle", fNew.getCity());
                assertEquals("GB", fNew.getCountry_code());
                assertEquals("United Kingdom", fNew.getCountry_name());
                assertEquals("Dorset, South West England", fNew.getStateProvince());
                assertEquals("", fNew.getSubLocation());
                assertEquals("DirKeyword1;DirKeyword2", fNew.getIPTCKeywords());
                //
                assertEquals(0,fNew.getWindowsComments().indexOf("#geocodeDONE:"));
                assertEquals(0,fNew.getIPTCInstructions().indexOf("#geocodeDONE:"));
                assertTrue(checkIPTCComments(fNew.getComments(),"#geocodeDONE:"));

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
    @DisplayName("Test 2")
    @Disabled
    void update2Test() {
        //Test 2
        // One image, with current ITPC data, so it should not be overwritten
        //No Json input file, but update parameter added and New Directory provided, so will copy to TestNewDir
        System.out.println("==========================TEST 2 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 2, startDir + "/Test")) {
            ImageCatalogue.main(new String[]{startDir + "/Test", startDir + "/TestRESULTS", startDir + "/TestNewDir","update"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("json file found"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            String fileName="T_"+"no metadata IPTC location filled in.jpg";
            FileObject fNew=processFile(new File(startDir+"/TestNewDir/2021/8/"+fileName), null,null, null,true);
            if(fNew!=null) {
                assertEquals("Filled in city", fNew.getCity());
                assertEquals("Filled in country", fNew.getCountry_name());
                assertEquals("Filled in province state", fNew.getStateProvince());
                assertEquals("Filled in sublocation", fNew.getSubLocation());
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
    @DisplayName("Test 3 ")
    @Disabled
    void update3Test() {
        //Test: 3
        // One image with IPTC metadata and with lat and lon, so should geocode and overwrite existing IPTC values
        //No Json input file, but update parameter added and New Directory provided, so will copy to TestNewDir, overwriteValues parameter set
        System.out.println("==========================TEST 3 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 2, startDir + "/Test")) {
            ImageCatalogue.main(new String[]{startDir + "/Test", startDir + "/TestRESULTS", startDir + "/TestNewDir","update","overwriteValues"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("json file found"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            String fileName="T_"+"no metadata IPTC location filled in.jpg";
            FileObject fNew=processFile(new File(startDir+"/TestNewDir/2021/8/"+fileName), null,null, null,true);
            if(fNew!=null) {
                assertEquals("Corfe Castle", fNew.getCity());
                assertEquals("GB", fNew.getCountry_code());
                assertEquals("United Kingdom", fNew.getCountry_name());
                assertEquals("Dorset, South West England", fNew.getStateProvince());
                assertEquals("Filled in sublocation", fNew.getSubLocation());
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
    @DisplayName("Read1 - 1 image (rotated| has descriptive metadata      | has lat and lon      | update | Move       | no JSON                ")
    void update4Test() {
        //Test: 4
        // One image - Checking thumbnail is rotated correctly based on orientation - we check the width of thumbnail
        // No Json input file, but update parameter added and New Directory provided, so will copy to TestNewDir
        System.out.println("==========================TEST 4 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 3, startDir + "/Test")) {
            ImageCatalogue.main(new String[]{startDir + "/Test", startDir + "/TestRESULTS", startDir + "/TestNewDir","update"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("json file found"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            String fileName="T_"+"IPTC metadata not origin lat lon rotated.jpg";
            String thumbName=makeThumbName(new File(new StringBuilder().append(startDir).append("/TestNewDir/2021/8/").append(fileName).toString()));
            try {
                BufferedImage bimg = ImageIO.read(new File(startDir + "/TestRESULTS/"+thumbName));
                assertEquals(240,bimg.getWidth());
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
    @DisplayName("Test 5")
    @Disabled
    void update5Test() {
        //Test: 5
        // One image with no descriptive metadata but with lat and lon, so should geocode
        //No Json input file, read only so file will not be updated or moved (HTML output wil includeGeocode details)
        System.out.println("==========================TEST 5 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 1, startDir + "/Test")) {
            ImageCatalogue.main(new String[]{startDir + "/Test", startDir + "/TestRESULTS"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("json file found"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            String fileName="T_"+"nodescriptivemetadata_haslonlat.jpg";
            FileObject fNew=processFile(new File(startDir+"/Test/"+fileName), null,null, null,true);
            if(fNew!=null)
            {
                assertEquals("Corfe Castle",fNew.getCity());
                assertEquals("GB",fNew.getCountry_code());
                assertEquals("United Kingdom",fNew.getCountry_name());
                assertEquals("Dorset, South West England",fNew.getStateProvince());
                assertEquals("Filled in sublocation",fNew.getSubLocation());
            } else {
                fail("Did not find output file");
            }
        } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
    @DisplayName("Test 6")
    @Disabled
    void update6Test() {
        //Test: 6
        // One image with no descriptive metadata but with lat and lon, so should geocode
        //No Json input file, update parameter included, but no directory provided so will not move  (HTML output wil includeGeocode details)
        System.out.println("==========================TEST 6 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 1, startDir + "/Test")) {
            ImageCatalogue.main(new String[]{startDir + "/Test", startDir + "/TestRESULTS","update"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("json file found"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            String fileName="T_"+"nodescriptivemetadata_haslonlat.jpg";
            FileObject fNew=processFile(new File(startDir+"/Test/"+fileName), null,null, null,true);
            if(fNew!=null) {
                assertEquals("Corfe Castle", fNew.getCity());
                assertEquals("GB", fNew.getCountry_code());
                assertEquals("United Kingdom", fNew.getCountry_name());
                assertEquals("Dorset, South West England", fNew.getStateProvince());
                assertEquals("", fNew.getSubLocation());
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
        //Test: 7
        // Six images with no descriptive metadata but with lat and lon, so should update copyright
        //Json input file, update parameter included, but no directory provided so will not move  (HTML output wil includeGeocode details)
        System.out.println("==========================TEST 7 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 4, startDir + "/Test")) {


            ImageCatalogue.main(new String[]{startDir + "/Test/config.json","overwrite"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("json file found"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
           //
            FileObject fNew=processFile(new File(startDir+"/Test/"+"T_"+"IPTC-win date year.jpg"), null,null, null,true);
            if (fNew != null) {
                assertEquals("Modified, Copyright;Copyright (Notice) 2021.1 IPTC - www.iptc.org  (ref2021.1)",fNew.getIPTCCopyright());
                assertEquals("IME1;IME2;Keyword1ref2021.1;Keyword2ref2021.1;Keyword3ref2021.1",fNew.getIPTCKeywords());
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
    @DisplayName("Test 8")
    @Disabled
    void update8Test() {
        //Test: 8
        // Six images with no descriptive metadata but with lat and lon, so should geocode
        //Json input file, update parameter included, but no directory provided so will not move  (HTML output wil includeGeocode details)
        System.out.println("==========================TEST 8 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 4, startDir + "/Test")) {
            setFileAttributesForTest(startDir+"/Test/"+"T_"+"IPTC-win date year.jpg","1999-11-23");


            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            ImageCatalogue.main(new String[]{startDir + "/Test/config.json"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("json file found"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew=processFile(new File(startDir+"/Test/"+"T_"+"IPTC-win date year.jpg"), null,null, null,true);
            if (fNew != null) {
                assertEquals("1985:01:01 00:00:00",formatter.format( convertToDateViaInstant(fNew.getBestDate())));
                assertEquals("1999:11:23 00:00:00",formatter.format( convertToDateViaInstant(fNew.getFileModified())));
                assertEquals("1999:11:23 00:00:00",formatter.format( convertToDateViaInstant(fNew.getFileCreated())));
                assertEquals("1999:11:23 00:00:00",formatter.format( convertToDateViaInstant(fNew.getFileAccessed())));
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
        //Test: 9
        // Six images with no descriptive metadata but with lat and lon, so should geocode
        //Json input file, update parameter included, but no directory provided so will not move  (HTML output wil includeGeocode details)
        System.out.println("==========================TEST 9 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 4, startDir + "/Test")) {
            setFileAttributesForTest(startDir+"/Test/"+"T_"+"IPTC-win date year month.jpg","1999-11-23");
            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            ImageCatalogue.main(new String[]{startDir + "/Test/config.json"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("json file found"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew=processFile(new File(startDir+"/Test/"+"T_"+"IPTC-win date year month.jpg"), null,null, null,true);
            if (fNew != null) {
                assertEquals("1985:07:01 00:00:00", formatter.format(convertToDateViaInstant(fNew.getBestDate())));
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
    @DisplayName("Test 10")
    @Disabled
    void update10Test() {
        //Test: 10
        // Six images with no descriptive metadata but with lat and lon, so should geocode
        //Json input file, update parameter included, but no directory provided so will not move  (HTML output wil includeGeocode details)
        System.out.println("==========================TEST 10 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 4, startDir + "/Test")) {

            setFileAttributesForTest(startDir+"/Test/"+"T_"+"IPTC-win date year month day.jpg","1999-11-23");

            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            ImageCatalogue.main(new String[]{startDir + "/Test/config.json"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("json file found"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew=processFile(new File(startDir+"/Test/"+"T_"+"IPTC-win date year month day.jpg"), null,null, null,true);
            if (fNew != null) {
                assertEquals("1985:07:14 00:00:00", formatter.format(convertToDateViaInstant(fNew.getBestDate())));
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
    @DisplayName("Test 11")
    @Disabled
    void update11Test() {
        //Test: 11
        // Six images with no descriptive metadata but with lat and lon, so should geocode
        //Json input file, update parameter included, but no directory provided so will not move  (HTML output wil includeGeocode details)
        System.out.println("==========================TEST 11 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 4, startDir + "/Test")) {
            setFileAttributesForTest(startDir+"/Test/"+"T_"+"lightroom date year.jpg","1999-11-23");

            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            ImageCatalogue.main(new String[]{startDir + "/Test/config.json"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("json file found"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew=processFile(new File(startDir+"/Test/"+"T_"+"lightroom date year.jpg"), null,null, null,true);
            if (fNew != null) {
                assertEquals("2001:01:01 00:00:00",formatter.format( convertToDateViaInstant(fNew.getBestDate())));
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
        //Test: 7
        // Six images with no descriptive metadata but with lat and lon, so should geocode
        //Json input file, update parameter included, but no directory provided so will not move  (HTML output wil includeGeocode details)
        System.out.println("==========================TEST 12 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 4, startDir + "/Test")) {
            setFileAttributesForTest(startDir+"/Test/"+"T_"+"lightroom date year month.jpg","1999-11-23");
            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            ImageCatalogue.main(new String[]{startDir + "/Test/config.json"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("json file found"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew=processFile(new File(startDir+"/Test/"+"T_"+"lightroom date year month.jpg"), null,null, null,true);
            if (fNew != null) {
                assertEquals("2001:07:01 00:00:00",formatter.format( convertToDateViaInstant(fNew.getBestDate())));
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
        //Test: 13
        // Six images with no descriptive metadata but with lat and lon, so should geocode
        //Json input file, update parameter included, but no directory provided so will not move  (HTML output wil includeGeocode details)
        System.out.println("==========================TEST 13 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 4, startDir + "/Test")) {
            setFileAttributesForTest(startDir+"/Test/"+"T_"+"lightroom date year month day.jpg","1999-11-23");

            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            ImageCatalogue.main(new String[]{startDir + "/Test/config.json"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("json file found"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew=processFile(new File(startDir+"/Test/"+"T_"+"lightroom date year month day.jpg"), null,null, null,true);
            if (fNew != null) {
                assertEquals("2001:07:14 00:00:00",formatter.format( convertToDateViaInstant(fNew.getBestDate())));
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
        // 11 images where events are added or found via the date of the photo
        //Json input file, update and overwrite parameters included, directory provided so will move  (HTML output wil includeGeocode details)
        System.out.println("==========================TEST 14 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 5, startDir + "/Test")) {
            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            ImageCatalogue.main(new String[]{startDir + "/Test/config.json"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("json file found"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew;
            fNew=processFile(new File(startDir+"/TestNewDir/2019/4/"+"T_"+"Added event with postcode.jpg"), null,null, null,true);
            if (fNew != null) {
                assertEquals("2019:04:14 12:30:00",formatter.format( convertToDateViaInstant(fNew.getBestDate())));
                assertEquals(	"City of Westminster",fNew.getCity());
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
        // 11 images where events are added or found via the date of the photo
        //Json input file, update and overwrite parameters included, directory provided so will move  (HTML output wil includeGeocode details)
        System.out.println("==========================TEST 15 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 5, startDir + "/Test")) {
            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            ImageCatalogue.main(new String[]{startDir + "/Test/config.json"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("json file found"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew;
            fNew=processFile(new File(startDir+"/TestNewDir/2019/7/"+"T_"+"Added event with place.jpg"), null,null, null,true);
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
        // 11 images where events are added or found via the date of the photo
        //Json input file, update and overwrite parameters included, directory provided so will move  (HTML output wil includeGeocode details)
        System.out.println("==========================TEST 16 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 5, startDir + "/Test")) {
            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            ImageCatalogue.main(new String[]{startDir + "/Test/config.json"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("json file found"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew;
            fNew=processFile(new File(startDir+"/TestNewDir/2019/7/"+"T_"+"Find event date place.jpg"), null,null, null,true);
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
        // 11 images where events are added or found via the date of the photo
        //Json input file, update and overwrite parameters included, directory provided so will move  (HTML output wil includeGeocode details)
        System.out.println("==========================TEST 17 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 5, startDir + "/Test")) {
            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            ImageCatalogue.main(new String[]{startDir + "/Test/config.json"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("json file found"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew;
            fNew=processFile(new File(startDir+"/TestNewDir/2020/6/"+"T_"+"Find event date time no place.jpg"), null,null, null,true);
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
        // 11 images where events are added or found via the date of the photo
        //Json input file, update and overwrite parameters included, directory provided so will move  (HTML output wil includeGeocode details)
        System.out.println("==========================TEST 18 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 5, startDir + "/Test")) {
            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            ImageCatalogue.main(new String[]{startDir + "/Test/config.json"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("json file found"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew;


            fNew=processFile(new File(startDir+"/TestNewDir/2021/7/"+"T_"+"Find event date postcode.jpg"), null,null, null,true);
            if(fNew!=null) {
                assertEquals("2021:07:21 21:01:01", formatter.format(convertToDateViaInstant(fNew.getBestDate())));
                assertEquals("City of Westminster", fNew.getCity());
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
        // 11 images where events are added or found via the date of the photo
        //Json input file, update and overwrite parameters included, directory provided so will move  (HTML output wil includeGeocode details)
        System.out.println("==========================TEST 19 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 5, startDir + "/Test")) {
            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            ImageCatalogue.main(new String[]{startDir + "/Test/config.json"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("json file found"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew;
            fNew=processFile(new File(startDir+"/TestNewDir/2021/8/"+"T_"+"Added event withlatlon.jpg"), null,null, null,true);
            if(fNew!=null) {
            assertEquals("2021:08:01 00:00:00",formatter.format( convertToDateViaInstant(fNew.getBestDate())));
            assertEquals(	"",fNew.getCity());

            } else {
                fail("Did not find output file");
            }
            fail("Setup Copy files to Test Area could not complete");
        }
    }
    @Test
    @Disabled
    @DisplayName("Test 20")
    void update20Test() {
        // 11 images where events are added or found via the date of the photo
        //Json input file, update and overwrite parameters included, directory provided so will move  (HTML output wil includeGeocode details)
        System.out.println("==========================TEST 20 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 5, startDir + "/Test")) {
            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            ImageCatalogue.main(new String[]{startDir + "/Test/config.json"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("json file found"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew;
              //
            fNew=processFile(new File(startDir+"/TestNewDir/2021/8/"+"T_"+"Find event date latlon.jpg"), null,null, null,true);
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
        // 11 images where events are added or found via the date of the photo
        //Json input file, update and overwrite parameters included, directory provided so will move  (HTML output wil includeGeocode details)
        System.out.println("==========================TEST 21 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 5, startDir + "/Test")) {
            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            ImageCatalogue.main(new String[]{startDir + "/Test/config.json"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("json file found"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew;
            fNew=processFile(new File(startDir+"/TestNewDir/2021/9/"+"T_"+"find eventcalendar no place.jpg"), null,null, null,true);
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
        // 11 images where events are added or found via the date of the photo
        //Json input file, update and overwrite parameters included, directory provided so will move  (HTML output wil includeGeocode details)
        System.out.println("==========================TEST 22 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 5, startDir + "/Test")) {
            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            ImageCatalogue.main(new String[]{startDir + "/Test/config.json"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("json file found"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew;
            fNew=processFile(new File(startDir+"/TestNewDir/2021/10/"+"T_"+"Added latlon.jpg"), null,null, null,true);
            if(fNew!=null)
            {
                assertEquals("2021:10:20 21:01:01",formatter.format( convertToDateViaInstant(fNew.getBestDate())));
                assertEquals(	"City of Westminster",fNew.getCity());
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
        // 11 images where events are added or found via the date of the photo
        //Json input file, update and overwrite parameters included, directory provided so will move  (HTML output wil includeGeocode details)
        System.out.println("==========================TEST 23 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 5, startDir + "/Test")) {
            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            ImageCatalogue.main(new String[]{startDir + "/Test/config.json"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("json file found"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew;
            fNew=processFile(new File(startDir+"/TestNewDir/2021/10/"+"T_"+"Added place.jpg"), null,null, null,true);
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
        // 11 images where events are added or found via the date of the photo
        //Json input file, update and overwrite parameters included, directory provided so will move  (HTML output wil includeGeocode details)
        System.out.println("==========================TEST 24 =================================");
        if (copyToTestArea(startDir + "/TestSource" + 5, startDir + "/Test")) {
            DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            ImageCatalogue.main(new String[]{startDir + "/Test/config.json"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("json file found"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            //
            FileObject fNew;
            fNew=processFile(new File(startDir+"/TestNewDir/2021/10/"+"T_"+"Addedpostcode.jpg"), null,null, null,true);
            if(fNew!=null) {
                assertEquals("2021:10:20 21:01:01", formatter.format(convertToDateViaInstant(fNew.getBestDate())));
                assertEquals("City of Westminster", fNew.getCity());
            } else {
                fail("Did not find output file");
            }
        } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }

}