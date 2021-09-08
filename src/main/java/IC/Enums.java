package IC;

public class Enums {


        public enum argOptions {
            overwriteValues,update,showmetadata,redoGeocoding
        }
        /*
           update - if false, no updates will take place to records ...default false
           showmetadata - metadata will be shown before and after, if update is true .. default false
           redoGeocoding - will do geocoding even if the metadata says that geocoding has been done before ... default false
           overwriteValues - if the subLocation, country etc. are filled in, then they will not be replaced...default false
          */


}
