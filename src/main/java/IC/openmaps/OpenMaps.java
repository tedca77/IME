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
import IC.Rest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.util.WebUtils;

import static IC.ImageCatalogue.assembleKeywords;
import static IC.ImageCatalogue.assembleLocation;
import static java.net.URLEncoder.encode;

public class OpenMaps {

    /**
     * Converts a lat,lon pair to a place - and creates the 5 IPTC fields which are added to a Place object
     * @param lat - latitude
     * @param lon - longtitude
     * @param config - config object
     * @return - returns Place Object
     */
    public static Place reverseGeocode(String lat, String lon, ConfigObject config)
    {
        try {
            Rest r = new Rest();
            String s="https://nominatim.openstreetmap.org/reverse?lat="+lat+"&lon="+lon+"&format=json";
            String result = r.doGetString(s);
            ObjectMapper mapper = new ObjectMapper();
            Place dResult = mapper.readValue(result, Place.class);
            dResult.setIPTCCity(assembleLocation(dResult,config.getCity()));
            dResult.setIPTCCountryCode(assembleLocation(dResult,config.getIsocountrycode()));
            dResult.setIPTCCountry(assembleLocation(dResult,config.getCountry()));
            dResult.setIPTCStateProvince(assembleLocation(dResult,config.getStateprovince()));
            dResult.setIPTCSublocation(assembleLocation(dResult,config.getSublocation()));
            dResult.setKeywords(assembleKeywords(dResult));
            // we set lat and long to the photo in case there is a difference in OpenMaps
            dResult.setLat(lat);
            dResult.setLon(lon);
            dResult.setCountPlace(1);
            return dResult;
        }
        catch(Exception e)
        {
            return null;
        }
    }

    /**
     *  returns the Grid Ref (lat, lon) for a Postcode - this is compatible with Google and OpenStreetMap coordinates -
     *  slightly different from Grid Reference as uses decimal for minutes and seconds
     *
     * @param query -postcode to check
     * @param apiKey - API key for OpenStreetMap
     * @param countryString - Country String used in the search e.g. GBR
     * @return
     */
    public static String checkPostCode(String query,String apiKey,String countryString)

    {
        Rest r = new Rest();
        String enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        if(apiKey==null)
        {
            return "NOAPIKEY";
        }
        try {
            String s="https://api.openrouteservice.org/geocode/search?api_key="+apiKey+"&boundary.country="+countryString+"&text="+ encode(query,enc);
            System.out.println("get Place:"+s);
            String result = r.doGetString(s);
            ObjectMapper mapper = new ObjectMapper();
            OpenMapPlace dResult = mapper.readValue(result, OpenMapPlace.class);
            if(dResult.getFeatures().size()==1)
            {
              //  System.out.println("result:"+dResult.getBbox().get(0)+","+dResult.getBbox().get(1));
                return dResult.getBbox().get(1)+","+dResult.getBbox().get(0);
            }
            else
            {

                return "";
            }

        }
        catch(Exception e)
        {
            System.out.println("Error in checkPostCode:"+e);
            return null;
        }
    }
}
