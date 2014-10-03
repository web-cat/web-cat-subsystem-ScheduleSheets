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

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;
import er.extensions.eof.ERXQ;
import er.extensions.foundation.ERXArrayUtilities;
import org.webcat.core.CourseOffering;
import org.webcat.core.User;
import org.webcat.grader.GraderComponent;

//-------------------------------------------------------------------------
/**
 *  Renders a descriptive table containing a submitted schedule sheet's basic
 *  identifying information.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class ScheduleSheetSummaryBlock
    extends GraderComponent
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new object.
     */
    public ScheduleSheetSummaryBlock(WOContext context)
    {
        super(context);
    }


    //~ KVC Attributes (must be public) .......................................

    public ScheduleSheetSubmission submission;
    public boolean allowPartnerEdit = false;
    public NSArray<User> originalPartners;
    public NSArray<User> partnersForEditing;
    public User aPartner;
    public int  rowNumber;


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    protected void beforeAppendToResponse(
        WOResponse response, WOContext context)
    {
        rowNumber = 0;

        if (submission != null)
        {
            originalPartners = submission.allPartners();
            partnersForEditing = originalPartners.mutableClone();
        }

        super.beforeAppendToResponse(response, context);
    }


    // ----------------------------------------------------------
    public void setPartnersForEditing(NSArray<User> users)
    {
        NSTimestamp now = new NSTimestamp();
        if (!submission.assignmentOffering().userCanSubmit(user()))
        {
            // Submission is now closed for the current user, so let
            // the enclosing page handle it.
            return;
        }

        partnersForEditing = users;
        NSArray<User> partnersWithoutPrincipal = users;
        if (!partnersForEditing.contains(submission.user()))
        {
            NSMutableArray<User> p = partnersForEditing.mutableClone();
            p.addObject(submission.user());
            partnersForEditing = p;
        }
        else
        {
            partnersForEditing = partnersForEditing.mutableClone();
            NSMutableArray<User> p = partnersWithoutPrincipal.mutableClone();
            p.removeObject(submission.user());
            partnersWithoutPrincipal = p;
        }

        // TODO: This now changes partnering for ALL submissions in a chain,
        // but there should probably be an option in the partner editing
        // dialog that allows the user to choose whether to change all, or
        // just the current one.
        for (ScheduleSheetSubmission s : submission.assignmentOffering()
            .allSubsFor(submission.user()))
        {
            NSArray<User> originals = s.allPartners();
            @SuppressWarnings("unchecked")
            NSArray<User> partnersToRemove = ERXArrayUtilities.arrayMinusArray(
                originals, partnersWithoutPrincipal);
            s.unpartnerFrom(partnersToRemove);

            @SuppressWarnings("unchecked")
            NSArray<User> partnersToAdd = ERXArrayUtilities.arrayMinusArray(
                partnersWithoutPrincipal, originals);
            s.partnerWith(partnersToAdd);
        }

        if (user() == submission.user())
        {
            submission.setSubmitTime(now);
        }

        originalPartners = partnersWithoutPrincipal;
        partnersForEditing = partnersWithoutPrincipal;
    }


    // ----------------------------------------------------------
    public EOQualifier qualifierForStudentsInCourse()
    {
        CourseOffering courseOffering =
            submission.assignmentOffering().courseOffering();
        NSArray<CourseOffering> offerings =
            CourseOffering.offeringsForSemesterAndCourse(localContext(),
                courseOffering.course(),
                courseOffering.semester());

        EOQualifier[] enrollmentQuals = new EOQualifier[offerings.count()];
        int i = 0;
        for (CourseOffering offering : offerings)
        {
            enrollmentQuals[i++] = User.enrolledIn.is(offering);
        }

        return ERXQ.or(enrollmentQuals);
    }
}