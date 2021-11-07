package IC;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
@Data
public class EventObject {
    String title;
    String description;
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
    public LocalDateTime exactStartTime;
    public LocalDateTime exactEndTime;
}
