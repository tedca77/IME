package IC;

import java.math.BigDecimal;
import java.util.Date;

public class FileObject {
    String fileName;
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
    String programName;
    String fStop;
    String latitude;
    String longtitude;
    String altitude;
}
