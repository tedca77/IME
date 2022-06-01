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

import IME.openmaps.Place;

import java.util.Comparator;

public class PlaceComparator implements Comparator<Place>
{
    public int compare(Place p1, Place p2)
    {
        // Assume no nulls, and simple ordinal comparisons

        // First by Country - stop if this gives a result.
        int countryResult = p1.getIPTCCountry().compareTo(p2.getIPTCCountry());
        if (countryResult != 0)
        {
            return countryResult;
        }

        // Next by StateProvince
        int stateResult = p1.getIPTCStateProvince().compareTo(p2.getIPTCStateProvince());
        if (stateResult != 0)
        {
            return stateResult;
        }
// Next by city
        int cityResult = p1.getIPTCCity().compareTo(p2.getIPTCCity());
        if (cityResult != 0)
        {
            return cityResult;
        }
        // Next by subLocation
        int subResult = p1.getIPTCSublocation().compareTo(p2.getIPTCSublocation());
        if (subResult != 0)
        {
            return subResult;
        }
        // Finally by longitude
        return p1.getLonAsDouble().compareTo(p2.getLonAsDouble());
    }
}
