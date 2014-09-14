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

import org.webcat.core.CourseOffering;
import org.webcat.grader.AssignmentOffering;
import org.webcat.grader.Submission;
import org.webcat.grader.UserSubmissionPair;
import com.webobjects.foundation.NSArray;

// -------------------------------------------------------------------------
/**
 * TODO: place a real description here.
 *
 * @author
 * @author  Last changed by: $Author$
 * @version $Revision$, $Date$
 */
public class EmailAlertForAssignmentOffering
    extends _EmailAlertForAssignmentOffering
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new EmailAlertForAssignmentOffering object.
     */
    public EmailAlertForAssignmentOffering()
    {
        super();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public void send()
    {
        CourseOffering offering = courseOffering();
        AssignmentOffering ao = AssignmentOffering
            .firstObjectMatchingQualifier(
                editingContext(),
                AssignmentOffering.courseOffering.is(offering).and(
                AssignmentOffering.assignment.is(
                    alertForAssignment().alertGroup().assignment())),
                null);

        NSArray<UserSubmissionPair> subs =
            Submission.submissionsForGrading(
                    editingContext(),
                    ao,
                    true,  // omitPartners
                    true,
                    null);

        for (UserSubmissionPair pair : subs)
        {
            EmailAlertForStudent.send(editingContext(),
                this, ao, pair.user(), pair.submission());
        }

        setSent(true);
        editingContext().saveChanges();
    }
}
