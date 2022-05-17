/*
 *    Copyright 2021 E.M.Carroll
 *    ==========================
 *    This file is part of Image Metadata Enhancer (IME).
 *
 *     Image Metadata Enhancer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Image Metadata Enhancer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Image Metadata Enhancer.  If not, see <https://www.gnu.org/licenses/>.
 */
package IME.openmaps;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;

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
    private ArrayList<String> keywords;

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
