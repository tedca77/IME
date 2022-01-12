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

import java.io.File;

import static IC.ImageCatalogue.*;
import static org.junit.jupiter.api.Assertions.*;

class ImageCatalogueTest {
    String startDir = "R:/ICTEST";
    @BeforeAll
    static void initAll() {

    }
    @BeforeEach
    void init() {

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
    @DisplayName("Read1 - 1 image | no descriptive metadata      | has lat and lon      | update      | No Move       | no JSON                ")
    //nodescriptivemetadata_haslonlat.jpg
    void read1Test() {
        if (!clearTestArea(startDir)) {
            fail("Setup Clear Test Area could not complete");
        }
        if (copyToTestArea(startDir + "/TestSource" + 1, startDir + "/Test")) {
            ImageCatalogue.main(new String[]{startDir + "/Test", startDir + "/TestRESULTS", startDir + "/TestNewDir","update"});
            String jsonFile=findJSONFile(new File(startDir + "/TestRESULTS"));
            System.out.println("json file found"+jsonFile);
            assertNotEquals(jsonFile.length(),0);
            String fileName="T_"+"nodescriptivemetadata_haslonlat.jpg";
            String thumbName=makeThumbName(new File(fileName));
            FileObject fNew=processFile(new File(startDir+"/TestNewDir/2021/8/"+fileName), null,null, null,true);
            assertEquals(fNew.getCity(),"Corfe Castle");
            assertEquals(fNew.getCountry_code(),"GB");
            assertEquals(fNew.getCountry_name(),"United Kingdom");
            assertEquals(fNew.getStateProvince(),"Dorset, South West England");
            assertEquals(fNew.getSubLocation(),"");
         } else {
            fail("Setup Copy files to Test Area could not complete");
        }
    }
}