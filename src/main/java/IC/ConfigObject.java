package IC;
import lombok.Data;

@Data
public class ConfigObject {
    private String tempdir;
    private String cryptokey;
    private String openmapkey;
    private String minfilesize;
    private String thumbsize;
    private IncludeSpec includespec;
    private ExcludeSpec excludespec;


}
