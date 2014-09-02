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

// -------------------------------------------------------------------------
/**
 * Represents an automatically generated feedback message for a
 * time schedule.
 *
 * @author Stephen Edwards
 * @author  Last changed by: $Author$
 * @version $Revision$, $Date$
 */
public class SheetFeedbackItem
    extends _SheetFeedbackItem
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new SheetFeedbackItem object.
     */
    public SheetFeedbackItem()
    {
        super();
    }


    //~ Constants .............................................................

    public static final byte INFORMATION = org.webcat.core.Status.INFORMATION;
    public static final byte WARNING = org.webcat.core.Status.WARNING;
    public static final byte ERROR = org.webcat.core.Status.ERROR;

    public static final int CODE1 = 0;

    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public String categoryString()
    {
        return org.webcat.core.Status.statusCssClass(category());
    }


    // ----------------------------------------------------------
    public String renderedMessage()
    {
        if (message() == null)
        {
            return TEMPLATES[code()];
        }
        else
        {
            return message();
        }
    }


    //~ Instance/static fields ................................................

    private static final String[] TEMPLATES = {
        "hello"
    };
}
