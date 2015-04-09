/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2012 Virginia Tech
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

import com.webobjects.foundation.NSArray;

// -------------------------------------------------------------------------
/**
 * Represents a single batch of alerts sent all together at the same time
 * to all students in a course.
 *
 * @author Stephen Edwards
 * @author  Last changed by: $Author$
 * @version $Revision$, $Date$
 */
public class EmailAlertForAssignment
    extends _EmailAlertForAssignment
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new EmailAlertForAssignment object.
     */
    public EmailAlertForAssignment()
    {
        super();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public int alertNo()
    {
        if (alertNoRaw() == null && alertGroup() != null)
        {
            NSArray<EmailAlertForAssignment> alerts =
                timeBeforeDue.desc().sorted(alertGroup().alerts());
            setAlertNoRaw(alerts.indexOf(this));
        }
        return super.alertNo();
    }
}
