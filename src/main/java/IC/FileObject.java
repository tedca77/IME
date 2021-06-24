package IC;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
public class FileObject {
    String fileName;
    Integer fileKey;
    BigDecimal fileSize;
    BigDecimal fileSizeOnDisk;
    Date fileCreated;
    Date fileModified;
    Date fileAccessed;
    String directory;
    String gridReference;
    String googleReference;
    String metaData;
    Boolean readOnlyAttribute;
    Boolean hiddenAttribute;
    Integer width;
    Integer height;
    String dimensions;
    Integer horResolution;
    Integer verResolution;
    String cameraMaker;
    String cameraModel;
    Integer cameraKey;
    String programName;
    String fStop;
    Double latitude;
    Double longitude;
    String altitude;
}
