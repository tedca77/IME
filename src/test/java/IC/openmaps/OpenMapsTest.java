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

package IC.openmaps;

import IC.ConfigObject;
import static IC.ImageCatalogue.setDefaults;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.util.Objects;

import static IC.openmaps.OpenMaps.checkPostCode;
import static IC.openmaps.OpenMaps.reverseGeocode;
import static org.junit.jupiter.api.Assertions.*;

class OpenMapsTest {
    String apiKey="5b3ce3597851110001cf6248a15496c57f254acbbcb04aaf8e115b50";
    String countryCode="GBR";
    @Test
    @DisplayName("Postcode check - correct")
    void checkPostCodeTest()
    {
        String result=checkPostCode("SW1A 1AA",apiKey,countryCode);
        String[] values2 = result.split(",", -1);
        Double lat = Double.valueOf(values2[0]);
        Double lon = Double.valueOf(values2[1]);
        assertEquals( lat, 51.501009d, 0.001 );
        assertEquals( lon,-0.141588d, 0.001 );
    }
    @Test
    @DisplayName("Postcode check - invalid data")
    void checkPostCodeTestError()
    {
        String result=checkPostCode("ABCD 1AA",apiKey,countryCode);
        assertEquals( result,"");
    }
    @Test
    @DisplayName("Reverse Geocode - correct")
    @Disabled
    void reverseGeocodeTest() {
        ZoneId z= ZoneId.systemDefault();
        ConfigObject config= new ConfigObject();
        setDefaults(Objects.requireNonNull(config),z);
        Place p =reverseGeocode("51.501009","-0.141588", config);
        assertEquals(p.getIPTCCity(),"City of Westminster");
        assertEquals(p.getIPTCCountry(),"United Kingdom");
        assertEquals(p.getIPTCStateProvince(),"Greater London");
        assertEquals(p.getIPTCSublocation(),"Ambassador's Court, Victoria");
        assertEquals(p.getIPTCCountryCode(),"GB");
    }
    @Test
    @DisplayName("Reverse Geocode - incorrect data")
    void reverseGeocodeTestError() {
        ZoneId z= ZoneId.systemDefault();
        ConfigObject config= new ConfigObject();
        setDefaults(Objects.requireNonNull(config),z);
        Place p =reverseGeocode("ww.501009","-200.141588", config);
        assertNull(p);
    }
}