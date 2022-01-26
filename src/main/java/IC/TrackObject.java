package IC;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Data
@JsonIgnoreProperties(value={"imagelinks"})
public class TrackObject {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate trackDate;
    private Integer trackKey;
    private Integer placeCount=0;
    private ArrayList<Integer> points = new ArrayList<>();
    private String imageLinks;
    private String coordinates;
    private String startAndEndPlace;
}