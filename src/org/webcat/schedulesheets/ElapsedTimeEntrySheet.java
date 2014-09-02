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

import org.webcat.grader.GraderAssignmentComponent;
import com.webobjects.appserver.WOContext;

//-------------------------------------------------------------------------
/**
 * Data entry page for elapse time schedule reports.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class ElapsedTimeEntrySheet
    extends GraderAssignmentComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Create a new ElapsedTimeEntrySheet object.
     * @param context
     */
    public ElapsedTimeEntrySheet(WOContext context)
    {
        super(context);
    }


    //~ KVC Attributes (must be public) .......................................

    public boolean first = true;
    public boolean rest = false;


    //~ Methods ...............................................................


    //~ Instance/static fields ................................................
}
