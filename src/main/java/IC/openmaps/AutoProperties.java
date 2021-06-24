package IC.openmaps;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class AutoProperties {
    private String name;
    private String county;
    private String locality;
    private String region;
}
