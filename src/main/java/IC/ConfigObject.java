package IC;
import IC.openmaps.ReverseGeocodeObject;
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
    private ArrayList<DriveObject> drives;
    private ArrayList<CameraObject> cameras;
    private ArrayList<ReverseGeocodeObject> places;
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

}
