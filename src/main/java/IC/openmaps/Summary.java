package IC.openmaps;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class Summary {
    private BigDecimal distance;
    private BigDecimal duration;
}
