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
package IME;

public class Enums {

        public enum prog {temp}

        public enum argOptions {
            overwrite,update,showmetadata,redo,append,addxpkeywords,addiptckeywords,clear
        }
        /*
           update - if false, no updates will take place to records ...default false
           showmetadata - metadata will be shown before and after, if update is true .. default false
           redoGeocoding - will do geocoding even if the metadata says that geocoding has been done before ... default false
           overwriteValues - if the subLocation, country etc. are filled in, then they will not be replaced...default false
           append - will update photos provided on the input JSON - otherwise will clear out photos array
           addxpkeywords - add location to XP Keywords / tags as well as IPTC data
           addiptckeywords - add location to IPTC Keywords / tags as well as IPTC data
          */
       public enum processMode {
           postcode,geocode,place,latlon,event,date
        }
        public enum statusValues {
            processed,movedfile,renamedfile
        }
        public enum doneValues {
           DONE,FAILED
        }

}
