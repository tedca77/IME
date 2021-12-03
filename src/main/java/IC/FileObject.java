package IC;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

@Data
public class FileObject {
    String fileName;
    Integer fileKey;
    BigDecimal fileSize;
    LocalDateTime bestDate;
    LocalDateTime exifOriginal;
    LocalDateTime exifDigitised;
    LocalDateTime tiffDate;
    LocalDateTime fileCreated;
    LocalDateTime fileModified;
    LocalDateTime fileAccessed;
    String directory;
    String metaData;
    Integer width;
    Integer height;
    String dimensions;
    String cameraMaker;
    String cameraModel;
    String cameraName;
    Integer cameraKey;
    Integer placeKey;
    String eventKeys;
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
    String thumbnail;
    String displayName;
    Integer orientation;
    String IPTCCopyright;
    String IPTCKeywords;
    String IPTCInstructions;
    String windowsComments;
    String windowsTitle;
    String windowsSubject;

}
