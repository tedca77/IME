package IC.openmaps;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class OpenMapPlace {

    private Geocoding geocoding;
    private ArrayList<String> bbox;
    private ArrayList<Feature> features;
}
