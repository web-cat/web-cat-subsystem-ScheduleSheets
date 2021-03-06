/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2014 Virginia Tech
 |
 |  This file is part of Web-CAT.
 |
 |  Web-CAT is free software; you can redistribute it and/or modify
 |  it under the terms of the GNU Affero General Public License as published
 |  by the Free Software Foundation; either version 3 of the License, or
 |  (at your option) any later version.
 |
 |  Web-CAT is distributed in the hope that it will be useful,
 |  but WITHOUT ANY WARRANTY; without even the implied warranty of
 |  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 |  GNU General Public License for more details.
 |
 |  You should have received a copy of the GNU Affero General Public License
 |  along with Web-CAT; if not, see <http://www.gnu.org/licenses/>.
\*==========================================================================*/

package org.webcat.schedulesheets;

//-------------------------------------------------------------------------
/**
 * Represents a 3-valued rating (+ undefined) for some aspect of an
 * assignment.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public enum Rating
{
    Undefined, Bad, Neutral, Good;


    // ----------------------------------------------------------
    /**
     * Get the byte-value equivalent of this rating
     */
    public byte byteValue()
    {
        return (byte)ordinal();
    }


    // ----------------------------------------------------------
    /**
     * Get the Rating equivalent of a given byte value.
     */
    public static Rating valueOf(byte value)
    {
        return values()[value];
    }
}
