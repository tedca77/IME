package IC.openmaps;
import IC.ConfigObject;
import IC.Rest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.util.WebUtils;

import static IC.ImageCatalogue.assembleLocation;
import static java.net.URLEncoder.encode;


public class OpenMaps {

    /**
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
    /*
      returns the Grid location for a Place Name - this is compatible with Google and OpenStreetMap coordinates - slightly different from Grid Reference as uses decimal for minutes and seconds
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
                System.out.println("result:"+dResult.getBbox().get(0)+","+dResult.getBbox().get(1));
                return dResult.getBbox().get(1)+","+dResult.getBbox().get(0);
            }
            else
            {
                return "";
            }

        }
        catch(Exception e)
        {
            System.out.println("Error in getPlace:"+e);
            return null;
        }
    }
}
