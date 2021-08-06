package IC;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
@Data
public class TrackObject {
    private Date startDate;
    private Date endDate;
    private Date trackDate;
    private Integer trackKey;
    private Integer placeCount=0;
    private ArrayList<Integer> points = new ArrayList<>();
    private String imagelinks;
    private String coordinates;
    private String startAndEndPlace;
}