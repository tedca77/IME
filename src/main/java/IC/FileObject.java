/*
 *    Copyright 2021 E.M.Carroll
 *    ==========================
 *    This file is part of Image Metadata Enhancer (IME).
 *
 *     Image Metadata Enhancer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Image Metadata Enhancer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Image Metadata Enhancer.  If not, see <https://www.gnu.org/licenses/>.
 */
package IC;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
@JsonIgnoreProperties(value={"IPTCKeywordsArray", "windowsKeywordsArray"})
@Data
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
    Integer width;
    Integer height;
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
    String IPTCCategory;
    ArrayList<String> IPTCKeywordsArray;
    String IPTCKeywords;
    String IPTCInstructions;
    String IPTCObjectName;
    String IPTCCaptionAbstract;
    String IPTCDateCreated;
    String windowsComments;
    String windowsTitle;
    String windowsSubject;
    ArrayList<String> windowsKeywordsArray;
    String windowsKeywords;
    ArrayList<String> comments;
    Boolean duplicate;
}
