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
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import static IME.IMEMethods.message;
import static IME.IMEMethods.truncate;

@Data
public class CounterObject {

    private int countFiles = 0;   //total number of images found
    private int countImages = 0;   //total number of images found
    private int countTooSmall = 0; // images too small (images processed is countImages-countTooSmall)
    private int countProcessed = 0; // images processed (images processed is countImages-countTooSmall)
    private int countLATLONG = 0;  // images with lat/long info
    private int countALREADYPROCESSED = 0; // images already processed
    private int countGEOCODED = 0; // images which were successfully geocoded
    private int countNOTGEOCODED = 0; // images which were not successfully geocoded (but with lat/long)
    private int countDateUpdate = 0;   // images where the date was updated.
    private int countEventsFound = 0;   // images where events were found through date checks...
    private int countAddedPlace = 0;   // images where a place has been specified
    private int countAddedLATLONG = 0;   // images where a LatLon has been added (either directly or via an event)
    private int countAddedPostcode = 0;   // images where a postcode has been used to determine LatLon (either directly or via an event)
    private int countAddedEvent = 0;   // images where an event has been added  manually
    private int countUPDATED = 0; // images which were not successfully updated
    private int countErrors = 0;   // images where there were errors (e.g. failure of geocode or others)....
    private int countMoved = 0;    // images which have been moved
    private int countDuplicates = 0;  // images which are duplicates

    public void addCountDuplicates()
    {
        countDuplicates++;
    }
    public void addCountMoved()
    {
        countMoved++;
    }
    public void addCountErrors()
    {
        countErrors++;
    }
    public void addCountUPDATED()
    {
        countUPDATED++;
    }
    public void addCountAddedEvent()
    {
        countAddedEvent++;
    }
    public void addCountAddedPostcode()
    {
        countAddedPostcode++;
    }
    public void addCountAddedLATLONG()
    {
        countAddedLATLONG++;
    }
    public void addCountAddedPlace()
    {
        countAddedPlace++;
    }
    public void addCountEventsFound()
    {
        countEventsFound++;
    }
    public void addCountDateUpdate()
    {
        countDateUpdate++;
    }
    public void addCountNOTGEOCODED()
    {
        countNOTGEOCODED++;
    }
    public void addCountGEOCODED()
    {
        countGEOCODED++;
    }
    public void addCountALREADYPROCESSED()
    {
        countALREADYPROCESSED++;
    }
    public void addCountLATLONG()
    {
        countLATLONG++;
    }
    public void addCountProcessed()
    {
        countProcessed++;
    }
    public void addCountTooSmall()
    {
        countTooSmall++;
    }
    public void addCountImages()
    {
        countImages++;
    }
    public void addCountFiles()
    {
        countFiles++;
    }
    public void printResults(String s)
    {
        String ex = truncate(s,40);
        message("Files found                        "+ex +":"+ countFiles);
        message("Photos found                       "+ex +":"+ countImages);
        message("Photos too small                   "+ex +":"+ countTooSmall);
        message("Photos to be processed             "+ex +":"+ countProcessed);
        message("Photos already Processed           "+ex +":"+ countALREADYPROCESSED);
        message("Photos updated                     "+ex +":"+ countUPDATED);
        message("Photos with metadata errors        "+ex +":"+ countErrors);
        message("Photos which have been moved       "+ex +":"+ countMoved);
        message("Photos which are duplicates        "+ex +":"+ countDuplicates);
        message("-----------------------------------");
        message("Photos with Lat Lon                "+ex +":"+ countLATLONG);
        message("Photos Geocoded                    "+ex +":"+ countGEOCODED);
        message("Photos with failed Geocoding       "+ex +":"+ countNOTGEOCODED);
        message("Photos where Date added            "+ex +":"+ countDateUpdate);
        message("Photos where Events found from Date"+ex +":"+ countEventsFound);
        message("Photos with Place added            "+ex +":"+ countAddedPlace);
        message("Photos with Lat Lon added          "+ex +":"+ countAddedLATLONG);
        message("Photos with Postcode added         "+ex +":"+ countAddedPostcode);
        message("Photos with Event added            "+ex +":"+ countAddedEvent);
    }

}
