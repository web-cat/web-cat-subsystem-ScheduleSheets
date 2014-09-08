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

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;
import er.extensions.appserver.ERXDisplayGroup;
import org.apache.log4j.Logger;
import org.webcat.core.CourseOffering;
import org.webcat.grader.GraderCourseComponent;

//-------------------------------------------------------------------------
/**
 * Pick which schedule sheet to enter.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class PickSchedulePage
    extends GraderCourseComponent
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new object.
     *
     * @param context The context to use
     */
    public PickSchedulePage(WOContext context)
    {
        super(context);
    }


    //~ KVC Attributes (must be public) .......................................

    public CourseOffering          course;
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

        course = coreSelections().courseOffering();
        if (course == null)
        {
            return;
        }

        if (course.isStaff(user()) || user().hasAdminPrivileges())
        {
            allOfferings =
                ScheduleSheetAssignmentOffering.objectsMatchingQualifier(
                    localContext(),
                    ScheduleSheetAssignmentOffering.courseOffering.is(course),
                    ScheduleSheetAssignmentOffering.order.asc()
                        .then(ScheduleSheetAssignmentOffering.dueDate.asc()));
        }
        else
        {
            allOfferings =
                ScheduleSheetAssignmentOffering.objectsMatchingQualifier(
                    localContext(),
                    ScheduleSheetAssignmentOffering.courseOffering.is(course)
                        .and(ScheduleSheetAssignmentOffering.publish.isTrue()),
                    ScheduleSheetAssignmentOffering.order.asc()
                        .then(ScheduleSheetAssignmentOffering.dueDate.asc()));
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
        coreSelections().setCourseOfferingRelationship(sheet.courseOffering());
        EntrySheet entrySheet = pageWithName(EntrySheet.class);
        entrySheet.offering = sheet;
        entrySheet.nextPage = this;
        return entrySheet;
    }


    // ----------------------------------------------------------
    public WOComponent view()
    {
        coreSelections().setCourseOfferingRelationship(sheet.courseOffering());
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
        sheets.queryMatch().takeValueForKey(newValue,
            ScheduleSheetAssignmentOffering.ASSIGNMENT_KEY);
        sheets.qualifyDisplayGroup();
    }


    //~ Instance/static fields ................................................

    private NSArray<ScheduleSheetAssignmentOffering> allOfferings;
    private NSMutableArray<ScheduleSheetAssignment> allAssignments;
    private ScheduleSheetAssignment assignment;

    static Logger log = Logger.getLogger(PickSchedulePage.class);
}
