package IC;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;
@JsonIgnoreProperties(value={"cameracount"})
@Data
public class CameraObject {
    String friendlyname;
    String cameramaker;
    String cameramodel;
    String programname;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime startdate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime enddate;
    Integer cameraid;
    Integer cameracount=0;
}
