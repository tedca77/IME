package IC;
import IC.openmaps.ReverseGeocodeObject;
import lombok.Data;

import java.util.ArrayList;

@Data
public class ConfigObject {
    private String tempdir;
    private String cryptokey;
    private String openmapkey;
    private String minfilesize;
    private String update;
    private String thumbsize;
    private ArrayList<DriveObject> drives;
    private ArrayList<CameraObject> cameras;
    private ArrayList<ReverseGeocodeObject> places;
    private ArrayList<FileObject> photos;
    private ArrayList<TrackObject> tracks;
    private ArrayList<String> isocountrycode;
    private ArrayList<String> country;
    private ArrayList<String> stateprovince;
    private ArrayList<String> city;
    private ArrayList<String> sublocation;

}
