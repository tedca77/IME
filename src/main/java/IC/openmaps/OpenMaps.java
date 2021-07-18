package IC.openmaps;


import IC.ConfigObject;
import IC.Rest;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.web.util.WebUtils;


import java.util.ArrayList;
import java.util.Date;

import static IC.ImageCatalogue.assembleLocation;

public class OpenMaps {
    private String mapKey ="5b3ce3597851110001cf6248a15496c57f254acbbcb04aaf8e115b50";

    /*
        returns the distance between two points...
     */

    /*
    returns an Array of Place suggestions
     */
    public static ArrayList<Place> getPlaceSuggestions(String query)
    {
        ArrayList<Place> places = new ArrayList<>();
       // https://api.openrouteservice.org/geocode/autocomplete?api_key=5b3ce3597851110001cf6248a15496c57f254acbbcb04aaf8e115b50&boundary.country=GB&text=
        try {
        Rest r = new Rest();
        String enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        String s="https://api.openrouteservice.org/geocode/autocomplete?api_key=5b3ce3597851110001cf6248a15496c57f254acbbcb04aaf8e115b50&boundary.country=GBR&text="+ Base64.getEncoder().encodeToString(query.getBytes(StandardCharsets.UTF_8));
     System.out.println("get place suggestions:"+s);
        String result = r.doGetString(s);


            ObjectMapper mapper = new ObjectMapper();
            Features dResult = mapper.readValue(result, Features.class);
          System.out.println("number of suggestions found is:"+dResult.getFeatures().size());
            for(Feature f : dResult.getFeatures())
            {
                Place p = new Place();


                    if(f.getProperties().getCounty()!=null) {
                        p.setPlaceName(f.getProperties().getName() + " (" +f.getProperties().getCounty()  + ")");
                    }
                    else
                    {
                        p.setPlaceName(f.getProperties().getName() + " (" +f.getProperties().getRegion()  + ")");
                    }
                p.setPlaceRegion(f.getProperties().getRegion());
                p.setPlaceCounty(f.getProperties().getCounty());
                p.setPlaceGrid(f.getGeometry().getCoordinates().get(0)+","+ f.getGeometry().getCoordinates().get(1));
                if (!places.contains(p)) {
                    places.add(p);
                }
              System.out.println("place:"+p.getPlaceName()+p.getPlaceCounty()+p.getPlaceRegion()+p.getPlaceGrid());
            }

        }
        catch(Exception e)
        {
           System.out.println("Error in getPlaceSuggestions:"+e);

        }


            return places;
    }
    /*
      returns the Grid location for a Place Name - this is compatible with Google and OpenStreetMap coordinates - slightly different from Grid Reference as uses decimal for minutes and seconds
     */
    public static String getPlace(String query)
    {

        try {
            Rest r = new Rest();
        String enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        String s="https://api.openrouteservice.org/geocode/search?api_key=5b3ce3597851110001cf6248a15496c57f254acbbcb04aaf8e115b50&boundary.country=GBR&text="+ Base64.getEncoder().encodeToString(query.getBytes(StandardCharsets.UTF_8));
      System.out.println("get Place:"+s);
        String result = r.doGetString(s);


            ObjectMapper mapper = new ObjectMapper();
            OpenMapPlace dResult = mapper.readValue(result, OpenMapPlace.class);
          System.out.println("result:"+dResult.getBbox().get(0)+dResult.getBbox().get(1));
            return ""+dResult.getBbox().get(0)+","+dResult.getBbox().get(1);
        }
        catch(Exception e)
        {
            System.out.println("Error in getPlace:"+e);
            return null;
        }
    }
    /*
      returns the Grid location for a Place Name - this is compatible with Google and OpenStreetMap coordinates - slightly different from Grid Reference as uses decimal for minutes and seconds
     */
    public static Boolean checkPostCode(String query)
    {
        try {
        Rest r = new Rest();
        String enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        String s="https://api.openrouteservice.org/geocode/search?api_key=5b3ce3597851110001cf6248a15496c57f254acbbcb04aaf8e115b50&boundary.country=GBR&text="+ Base64.getEncoder().encodeToString(query.getBytes(StandardCharsets.UTF_8));
       System.out.println("get Place:"+s);
        String result = r.doGetString(s);

            ObjectMapper mapper = new ObjectMapper();
            OpenMapPlace dResult = mapper.readValue(result, OpenMapPlace.class);
            if(dResult.getFeatures().size()==1)
            {
                return true;
            }
            else
            {
                return false;
            }
           //System.out.println("result:"+dResult.getBbox().get(0)+dResult.getBbox().get(1));
           // return ""+dResult.getBbox().get(0)+","+dResult.getBbox().get(1);
        }
        catch(Exception e)
        {
            System.out.println("Error in getPlace:"+e);
            return null;
        }
    }
    /*
      returns the Grid location for a Place Name - this is compatible with Google and OpenStreetMap coordinates - slightly different from Grid Reference as uses decimal for minutes and seconds
     */
    public static ReverseGeocodeObject reverseGeocode(String lat, String lon, ConfigObject config)
    {
        try {
            Rest r = new Rest();
            String enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
            String s="https://nominatim.openstreetmap.org/reverse?lat="+lat+"&lon="+lon+"&format=json";
            System.out.println("get Place:"+s);
            String result = r.doGetString(s);

            ObjectMapper mapper = new ObjectMapper();
            ReverseGeocodeObject dResult = mapper.readValue(result, ReverseGeocodeObject.class);
            dResult.setIPTCCity(assembleLocation(dResult,config.getCity()));
            dResult.setIPTCCountryCode(assembleLocation(dResult,config.getIsocountrycode()));
            dResult.setIPTCCountry(assembleLocation(dResult,config.getCountry()));
            dResult.setIPTCStateProvince(assembleLocation(dResult,config.getStateprovince()));
            dResult.setIPTCSublocation(assembleLocation(dResult,config.getSublocation()));
            // we set lat and long to the photo in case there is a difference in OpenMaps
            dResult.setLat(lat);
            dResult.setLon(lon);
            dResult.setCountPlace(1);

            System.out.println("result:"+dResult.getDisplay_name());
            return dResult;

        }
        catch(Exception e)
        {
            System.out.println("Error in getPlace:"+e);
            return null;
        }
    }
}
