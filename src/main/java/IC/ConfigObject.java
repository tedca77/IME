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
import IC.openmaps.Place;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.ArrayList;
@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class ConfigObject {
    private String tempdir;
    private String newdir;
    private Long minfilesize;
    private Boolean update;
    private Boolean showmetadata;
    private Boolean overwrite;
    private Boolean redo;
    private Boolean append;
    private Boolean clear;
    private Boolean addxpkeywords;
    private Boolean addiptckeywords;
    private String thumbsize;
    private Integer cacheDistance;
    private Integer pauseSeconds;
    private String timeZone;
    private ArrayList<DriveObject> drives;
    private ArrayList<CameraObject> cameras;
    private ArrayList<Place> places;
    private ArrayList<EventObject> events;
    private ArrayList<FileObject> photos;
    private ArrayList<TrackObject> tracks;
    private ArrayList<String> isocountrycode;
    private ArrayList<String> country;
    private ArrayList<String> stateprovince;
    private ArrayList<String> city;
    private ArrayList<String> sublocation;
    private ArrayList<String> newfileNames;
    private String imageextensions;
    private String openAPIKey;
    public Integer getWidth()
    {
        Integer width=600;
        String[] values = thumbsize.split("x",-1);
        if(values.length!=2)
        {
            return width;
        }
        else
        {
            try {
                return Integer.parseInt(values[0]);
              }
            catch(Exception e)
            {
                return width;
            }
        }
     }
    public Integer getHeight()
    {
        Integer height=400;
        String[] values = thumbsize.split("x",-1);
        if(values.length!=2)
        {
            return height;
        }
        else
        {
            try {
                return Integer.parseInt(values[1]);
            }
            catch(Exception e)
            {
                return height;
            }
        }
    }


}
