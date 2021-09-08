package IC;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class DriveObject {
   public String startdir;
   private ExcludeSpec excludespec;
   private String IPTCCopyright;
   private String IPTCCategory;
   private String IPTCKeywords;

}
