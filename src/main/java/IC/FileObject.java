package IC;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(value={"orientation", "thumbnail","dimension"})

public class FileObject {
    String fileName;
    Integer fileKey;
    BigDecimal fileSize;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime bestDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime exifOriginal;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime exifDigitised;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime tiffDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime fileCreated;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime fileModified;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime fileAccessed;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
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
    String IPTCDateCreated;
    String windowsComments;
    String windowsTitle;
    String windowsSubject;

}
