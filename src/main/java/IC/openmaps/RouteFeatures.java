package IC.openmaps;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class RouteFeatures {
    private RouteProperties properties;
}
