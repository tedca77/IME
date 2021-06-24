package IC.openmaps;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class Place {
    private String placeName;
    private String placeRegion;
    private String placeCounty;
    private String placeGrid;

    @Override
    public boolean equals (Object object) {
        boolean result = false;
        if (object == null || object.getClass() != getClass()) {
            result = false;
        } else {
            Place  place = (Place) object;
            if (this.placeName.equals(place.getPlaceName())) {
                result = true;
            }
        }
        return result;
    }
}
