package IC;
import lombok.Data;
import java.util.Date;
@Data
public class EventObject {
    String title;
    String description;
    String keywords;
    String eventType;
    Date startdate;
    Date enddate;
    Integer eventKey;
    Integer placeKey;
    String placeDescription; //either placeKey, lat,long or postcode
}
