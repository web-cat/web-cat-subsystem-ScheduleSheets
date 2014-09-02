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

import org.apache.log4j.Logger;
import org.webcat.grader.GraderComponent;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;
import er.extensions.appserver.ERXDisplayGroup;

//-------------------------------------------------------------------------
/**
 * Home page info for schedule sheets.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class ScheduleSheetsHomeStatus
    extends GraderComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new object.
     *
     * @param context The context to use
     */
    public ScheduleSheetsHomeStatus(WOContext context)
    {
        super(context);
    }


    //~ KVC Attributes (must be public) .......................................

    public ERXDisplayGroup<ScheduleSheetAssignment> assignments;
    public ERXDisplayGroup<ScheduleSheetAssignmentOffering> sheets;
    public ScheduleSheetAssignmentOffering sheet;

    public int                     index;
    public NSTimestamp             now;


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    @Override
    protected void beforeAppendToResponse(
        WOResponse response, WOContext context)
    {
        super.beforeAppendToResponse(response, context);

        allOfferings =
            ScheduleSheetAssignmentOffering.objectsMatchingQualifier(
                localContext(),
                ScheduleSheetAssignmentOffering.courseOffering.isNotNull(),
                ScheduleSheetAssignmentOffering.order.asc()
                    .then(ScheduleSheetAssignmentOffering.dueDate.asc()))
            .mutableClone();

        for (int i = 0; i < allOfferings.count(); i++)
        {
            if (!user().hasAdminPrivileges())
            {
                ScheduleSheetAssignmentOffering o = allOfferings.get(i);
                if (!o.courseOffering().isStaff(user()))
                {
                    if (!o.publish()
                        || !o.courseOffering().students().contains(user()))
                    {
                        allOfferings.remove(i);
                        i--;
                    }
                }
            }
        }

        allAssignments = new NSMutableArray<ScheduleSheetAssignment>();
        for (ScheduleSheetAssignmentOffering o : allOfferings)
        {
            if (!allAssignments.contains(o.assignment()))
            {
                allAssignments.add(o.assignment());
            }
        }
        assignments.setObjectArray(allAssignments);
        sheets.setObjectArray(allOfferings);

        now = new NSTimestamp();
    }


    // ----------------------------------------------------------
    public WOComponent submit()
    {
        EntrySheet entrySheet = pageWithName(EntrySheet.class);
        entrySheet.offering = sheet;
        entrySheet.nextPage = this;
        return entrySheet;
    }


    // ----------------------------------------------------------
    public WOComponent view()
    {
        SheetFeedbackPage feedback = pageWithName(SheetFeedbackPage.class);
        feedback.offering = sheet;
        feedback.nextPage = this;
        return feedback;
    }


    // ----------------------------------------------------------
    public ScheduleSheetAssignment assignment()
    {
        return assignment;
    }


    // ----------------------------------------------------------
    public void setAssignment(ScheduleSheetAssignment newValue)
    {
        assignment = newValue;
        sheets.queryBindings().takeValueForKey(newValue,
            ScheduleSheetAssignmentOffering.ASSIGNMENT_KEY);
        sheets.qualifyDisplayGroup();
    }


    //~ Instance/static fields ................................................

    private NSMutableArray<ScheduleSheetAssignmentOffering> allOfferings;
    private NSMutableArray<ScheduleSheetAssignment> allAssignments;
    private ScheduleSheetAssignment assignment;

    static Logger log = Logger.getLogger(ScheduleSheetsHomeStatus.class);
}