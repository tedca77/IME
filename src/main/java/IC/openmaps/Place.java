package IC.openmaps;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;



@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class Place {
    private String display_name;
    private Long place_id;
    private String licence;
    private String osm_type;
    private Long osm_id;
    private String lat;
    private String lon;
    private AddressObject address;
    private String IPTCCountryCode;
    private String IPTCCountry;
    private String IPTCStateProvince;
    private String IPTCCity;
    private String IPTCSublocation;
    private String friendlyname;
    private Integer internalKey;
    private Integer countPlace;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;
    private Integer trackKey;
    private String imagelinks;
    private String cdata;
}
