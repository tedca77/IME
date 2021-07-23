package IC;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
public class FileObject {
    String fileName;
    Integer fileKey;
    BigDecimal fileSize;
    Date bestDate;
    Date exifOriginal;
    Date exifDigitised;
    Date tiffDate;
    Date fileCreated;
    Date fileModified;
    Date fileAccessed;
    String directory;
    String metaData;
    Integer width;
    Integer height;
    String dimensions;
    String cameraMaker;
    String cameraModel;
    Integer cameraKey;
    Integer placeKey;
    String programName;
    Double fStop;
    Double latitude;
    Double longitude;
    Double altitude;
    String city;
    String country_name;
    String country_code;
    String subLocation;
    String stateProvince;
    String displayName;

}
