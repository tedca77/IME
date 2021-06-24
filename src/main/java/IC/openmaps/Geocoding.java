package IC.openmaps;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class Geocoding {
    private String version;
    private String attribution;
    private Query query;
}
