package IC.openmaps;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class AddressObject {
    private String amenity;
    private String suburb;
    private String city;
    private String county;
    private String state_district;
    private String state;
    private String postcode;
    private String country;
    private String country_code;
    private String road;
    private String village;
    private String hamlet;
    private String house_number;
    private String town;
    private String city_district;
    private String leisure;
 }
