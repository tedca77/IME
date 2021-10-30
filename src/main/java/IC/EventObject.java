package IC;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;
@Data
public class EventObject {
    String title;
    String description;
    String keywords;
    String eventType; //annual  oneoff
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "GMT")
    Date startdate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "GMT")
    Date enddate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH.mm", timezone = "GMT")
    Date starttime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH.mm", timezone = "GMT")
    Date endtime;
    Integer eventid;
    String location; //either placeKey, lat,long or postcode
}
