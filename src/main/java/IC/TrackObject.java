package IC;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
@Data
public class TrackObject {
    String friendlyName;
    Date startDate;
    Date endDate;
    Date trackDate;
    Integer trackKey;
    Integer placeCount=0;
    ArrayList<Integer> points = new ArrayList<>();
}