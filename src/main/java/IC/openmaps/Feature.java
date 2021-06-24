package IC.openmaps;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class Feature {
    private AutoProperties properties;
    private AutoGeometry geometry;
}
