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

import java.util.TimeZone;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSTimestamp;
import er.extensions.appserver.ERXDisplayGroup;
import org.webcat.grader.Assignment;
import org.webcat.grader.GraderAssignmentsComponent;
import org.webcat.ui.generators.JavascriptGenerator;

//-------------------------------------------------------------------------
/**
 * Page to edit the properties of a group of e-mail alerts for a
 * programming assignment.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class EditEmailAlertGroupForAssignmentPage
    extends GraderAssignmentsComponent
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new object.
     *
     * @param context The context to use
     */
    public EditEmailAlertGroupForAssignmentPage(WOContext context)
    {
        super(context);
    }


    //~ KVC Attributes (must be public) .......................................

    public EmailAlertGroupForAssignment group;
    public ERXDisplayGroup<EmailAlertForAssignment> batches;
    public int batchNo;
    public ERXDisplayGroup<EmailAlertForAssignmentOffering> offerings;
    public EmailAlertForAssignmentOffering offering;
    public int offeringIndex;


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    @Override
    protected void beforeAppendToResponse(
        WOResponse response, WOContext context)
    {
        if (group == null)
        {
            Assignment progrAssgn = prefs().assignment();
            if (progrAssgn == null)
            {
                progrAssgn = prefs().assignmentOffering().assignment();
            }
            group = EmailAlertGroupForAssignment.firstObjectMatchingQualifier(
                localContext(),
                EmailAlertGroupForAssignment.assignment.is(progrAssgn),
                null);
        }

        if (group != null)
        {
            batches.setMasterObject(group);
        }
    }


    // ----------------------------------------------------------
    public EmailAlertForAssignment batch()
    {
        return batch;
    }


    // ----------------------------------------------------------
    public void setBatch(EmailAlertForAssignment value)
    {
        if (value != null && batches.displayedObjects().count() > 0)
        {
            if (value == batches.displayedObjects().get(0))
            {
                batchNo = 1;
            }
            else
            {
                batchNo++;
            }
        }
        batch = value;
        if (batch != null)
        {
            offerings.setMasterObject(batch);
        }
    }


    // ----------------------------------------------------------
    public NSTimestamp sendTime()
    {
        return offering.sendTime();
    }


    // ----------------------------------------------------------
    public void setSendTime(NSTimestamp value)
    {
        if (areDatesLocked)
        {
            for (EmailAlertForAssignmentOffering o :
                offerings.displayedObjects())
            {
                o.setSendTime(value);
            }
        }
        else
        {
            offering.setSendTime(value);
        }
    }


    // ----------------------------------------------------------
    public boolean shouldShowDatePicker()
    {
        return !areDatesLocked
            || (offerings.displayedObjects().count() > 0
                && offering.equals(
                        offerings.displayedObjects().objectAtIndex(0)));
    }


    // ----------------------------------------------------------
    public boolean areDatesLocked()
    {
        return areDatesLocked;
    }


    // ----------------------------------------------------------
    public boolean areDatesSame()
    {
        NSTimestamp exemplar = null;

        for (EmailAlertForAssignmentOffering o :
            offerings.displayedObjects())
        {
            if (exemplar == null)
            {
                exemplar = o.sendTime();
            }
            else
            {
                if (o.sendTime() == null
                        || !exemplar.equals(o.sendTime()))
                {
                    return false;
                }
            }
        }

        return true;
    }


    // ----------------------------------------------------------
    public WOActionResults lockDates()
    {
        if (saveAndCanProceed())
        {
            areDatesLocked = true;

            NSTimestamp exemplar = null;

            for (EmailAlertForAssignmentOffering o :
                offerings.displayedObjects())
            {
                if (exemplar == null)
                {
                    exemplar = o.sendTime();
                }
                else
                {
                    o.setSendTime(exemplar);
                }
            }

            applyLocalChanges();
        }

        return new JavascriptGenerator()
            .refresh("offerings" + batchNo, "error-panel");
    }


    // ----------------------------------------------------------
    public WOActionResults unlockDates()
    {
        if (saveAndCanProceed())
        {
            areDatesLocked = false;
        }

        return new JavascriptGenerator()
            .refresh("offerings" + batchNo, "error-panel");
    }


    // ----------------------------------------------------------
    public boolean saveAndCanProceed()
    {
        return !hasMessages() && applyLocalChanges();
    }


    // ----------------------------------------------------------
    @Override
    public WOComponent apply()
    {
        if (saveAndCanProceed())
        {
            ScheduleSheets.pingAlerter();
            return super.apply();
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    public TimeZone timeZone()
    {
        return TimeZone.getTimeZone(user().timeZoneName());
    }


    //~ Instance/static fields ................................................

    private EmailAlertForAssignment batch;
    private boolean areDatesLocked;
}