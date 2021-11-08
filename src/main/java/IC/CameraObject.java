package IC;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
@Data
public class CameraObject {
    String friendlyname;
    String cameramaker;
    String cameramodel;
    String programname;
    LocalDateTime startdate;
    LocalDateTime enddate;
    Integer camerakey;
    Integer cameracount=0;
}
