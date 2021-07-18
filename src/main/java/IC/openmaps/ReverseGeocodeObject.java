package IC.openmaps;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Date;


@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class ReverseGeocodeObject {
    private String display_name;
    private Long place_id;
    private String licence;
    private String osm_type;
    private Long osm_id;
    private String lat;
    private String lon;
    private String[] boundingbox;
    private AddressObject address;
    private String IPTCCountryCode;
    private String IPTCCountry;
    private String IPTCStateProvince;
    private String IPTCCity;
    private String IPTCSublocation;
    private String friendlyname;
    private Integer internalKey;
    private Integer countPlace;
    private Date endDate;
    private Date startDate;
    private Integer trackKey;
}
