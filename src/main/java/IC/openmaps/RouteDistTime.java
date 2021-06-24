package IC.openmaps;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RouteDistTime {
    private Place fromPlace;
    private Place toPlace;
    private BigDecimal duration;
    private BigDecimal distance;

}
