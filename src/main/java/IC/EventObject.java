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
package IC;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

@Data
@JsonIgnoreProperties(value={"exactEndTime", "exactStartTime","imagelinks","keywordsArray"})
public class EventObject {
    String title;
    String description;
    String imagelinks;
    ArrayList<String> keywordsArray;
    String keywords;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate eventdate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate enddate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH.mm")
    LocalTime eventtime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH.mm")
    LocalTime endtime;
    String eventcalendar;
    Integer eventid;
    String location; //either placeKey, lat,long or postcode
    @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
    public LocalDateTime exactStartTime;
    @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
    public LocalDateTime exactEndTime;
}
