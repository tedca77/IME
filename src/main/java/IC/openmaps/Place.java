package IC.openmaps;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;



@Data
@JsonIgnoreProperties(ignoreUnknown=true,value={"imagelinks", "osm_type","cdata","lonAsDouble","latAsDouble","trackKey","startDate","endDate","countPlace","osm_id","licence","place_id"})

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
    private Integer placeid;
    private Integer countPlace;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;
    private Integer trackKey;
    private String imagelinks;
    private String cdata;

    public Double getLatAsDouble()
    {
        try
        {
            return Double.parseDouble(lat);
        }
        catch(Exception e){
            return null;
        }
    }
    public Double getLonAsDouble()
    {
        try
        {
            return Double.parseDouble(lon);
        }
        catch(Exception e){
            return null;
        }
    }
}
