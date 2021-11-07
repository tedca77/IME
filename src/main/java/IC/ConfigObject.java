package IC;
import IC.openmaps.Place;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class ConfigObject {
    private String tempdir;
    private Long minfilesize;
    private Boolean update;
    private Boolean showmetadata;
    private Boolean overwrite;
    private Boolean redoGeocode;
    private String thumbsize;
    private Integer cacheDistance;
    private Integer pauseSeconds;
    private String timeZone;
    private ArrayList<DriveObject> drives;
    private ArrayList<CameraObject> cameras;
    private ArrayList<Place> places;
    private ArrayList<EventObject> events;
    private ArrayList<FileObject> photos;
    private ArrayList<TrackObject> tracks;
    private ArrayList<String> isocountrycode;
    private ArrayList<String> country;
    private ArrayList<String> stateprovince;
    private ArrayList<String> city;
    private ArrayList<String> sublocation;
    private String imageextensions;
    private String videoextensions;
    private String openAPIKey;
    public Integer getWidth()
    {
        Integer width=600;
        String[] values = thumbsize.split("x",-1);
        if(values.length!=2)
        {
            return width;
        }
        else
        {
            try {
                return Integer.parseInt(values[0]);
              }
            catch(Exception e)
            {
                return width;
            }
        }
     }
    public Integer getHeight()
    {
        Integer height=400;
        String[] values = thumbsize.split("x",-1);
        if(values.length!=2)
        {
            return height;
        }
        else
        {
            try {
                return Integer.parseInt(values[1]);
            }
            catch(Exception e)
            {
                return height;
            }
        }
    }


}
