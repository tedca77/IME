package IC;
import lombok.Data;
import java.util.Date;
@Data
public class CameraObject {
    String cameraMaker;
    String cameraModel;
    String programName;
    Date startDate;
    Date endDate;
    Integer cameraKey;
    Integer cameraCount=0;
}
