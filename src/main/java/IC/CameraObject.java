package IC;
import lombok.Data;
import java.util.Date;
@Data
public class CameraObject {
    String friendlyname;
    String cameramaker;
    String cameramodel;
    String programname;
    Date startdate;
    Date enddate;
    Integer camerakey;
    Integer cameracount=0;
}
