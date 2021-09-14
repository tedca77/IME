package IC.openmaps;
import IC.ConfigObject;
import IC.Rest;
import com.fasterxml.jackson.databind.ObjectMapper;

import static IC.ImageCatalogue.assembleLocation;


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
}
