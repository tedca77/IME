package IC;

import lombok.Data;

import java.util.ArrayList;

@Data
public class ExcludeSpec {
    public ArrayList<DirectoryObject> directories;
    public ArrayList<DirectoryObject> fileprefixes;
}
