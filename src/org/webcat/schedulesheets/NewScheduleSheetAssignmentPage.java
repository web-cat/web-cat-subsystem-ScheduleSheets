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
import org.webcat.core.Course;
import org.webcat.core.CourseOffering;
import org.webcat.core.Semester;
import org.webcat.grader.Assignment;
import org.webcat.grader.GraderAssignmentsComponent;
import org.webcat.grader.SubmissionProfile;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;

//-------------------------------------------------------------------------
/**
 * Page to create a new schedule sheet assignment.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class NewScheduleSheetAssignmentPage
    extends GraderAssignmentsComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new object.
     *
     * @param context The context to use
     */
    public NewScheduleSheetAssignmentPage(WOContext context)
    {
        super(context);
    }


    //~ KVC Attributes (must be public) .......................................

    public String aName;
    public boolean bindToAssignment = true;
    public byte numberOfSheets = 3;
    public String title;
    public boolean forAllSections = true;
    public Assignment assignment;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Adds to the response of the page
     *
     * @param response The response being built
     * @param context  The context of the request
     */
    protected void beforeAppendToResponse(
        WOResponse response, WOContext context)
    {
        if (bindToAssignment && (aName == null || aName.isEmpty()))
        {
            if (prefs().assignment() != null)
            {
                assignment = prefs().assignment();
                aName = assignment.name();
            }
            else if (prefs().assignmentOffering() != null)
            {
                assignment = prefs().assignmentOffering().assignment();
                aName = assignment.name();
            }
            aName = "Schedule for " + aName;
        }
        if (coreSelections().course() == null
            && coreSelections().courseOffering() == null)
        {
            if (prefs().assignmentOffering() != null)
            {
                coreSelections().setCourseRelationship(
                    prefs().assignmentOffering().courseOffering().course());
            }
            else if (prefs().assignment() != null
                && prefs().assignment().offerings().count() > 0)
            {
                coreSelections().setCourseRelationship(
                    prefs().assignment().offerings().objectAtIndex(0)
                        .courseOffering().course());
            }
        }
    }


    // ----------------------------------------------------------
    @Override
    public WOComponent next()
    {
        if (aName == null || aName.length() == 0)
        {
            error("Please enter a name for your assignment.");
            return null;
        }
        if (numberOfSheets < 1)
        {
            error("Please enter a positive number of schedule sheets for "
                + "this assignment.");
            return null;
        }

        Semester semester = coreSelections().semester();
        Course course = coreSelections().course();
        NSArray<CourseOffering> offerings = null;
        if (course == null
            && forAllSections
            && coreSelections().courseOffering() != null)
        {
            course = coreSelections().courseOffering().course();
            semester = coreSelections().courseOffering().semester();
        }
        if (course != null)
        {
            offerings = course.offerings();
            if (semester != null)
            {
                NSMutableArray<CourseOffering> fullOfferings =
                    offerings.mutableClone();
                for (int i = 0; i < fullOfferings.count(); i++)
                {
                    if (fullOfferings.objectAtIndex(i).semester() != semester)
                    {
                        fullOfferings.remove(i);
                        i--;
                    }
                }
                offerings = fullOfferings;
            }
        }
        else
        {
            offerings = new NSArray<CourseOffering>(
                coreSelections().courseOffering());
        }

        if (offerings == null || offerings.count() == 0)
        {
            error("Please select a course in which to create the assignment.");
            return null;
        }

        log.debug("creating new schedule sheet assignment");
        ScheduleSheetAssignment newAssignment = ScheduleSheetAssignment
            .create(localContext());
        newAssignment.setName(aName);
        newAssignment.setShortDescription(title);
        newAssignment.setAuthorRelationship(user());
        newAssignment.setNumberOfSheets(numberOfSheets);
        if (bindToAssignment && assignment != null)
        {
            newAssignment.setAssignmentRelationship(assignment);
        }

        SubmissionProfile profile = SubmissionProfile.create(
            localContext(), false,false, false, false, false);
        profile.setDeadTimeDelta(
            SubmissionProfile.timeUnits[2].factor() * 2);
        profile.setAvailableTimeDelta(
            SubmissionProfile.timeUnits[2].factor() * 2);
        profile.setToolPoints(100);
        profile.setAvailablePoints(profile.toolPoints());

        profile.setAllowPartners(false);
        profile.setAutoAssignPartners(false);
        profile.setAwardEarlyBonus(false);
        profile.setDeductExcessSubmissionPenalty(false);
        profile.setDeductLatePenalty(false);

        profile.setEarlyBonusMaxPtsRaw(null);
        profile.setEarlyBonusUnitPtsRaw(null);
        profile.setEarlyBonusUnitTimeRaw(null);
        profile.setExcessSubmissionsMaxPtsRaw(null);
        profile.setExcessSubmissionsThresholdRaw(null);
        profile.setExcessSubmissionsUnitPtsRaw(null);
        profile.setExcessSubmissionsUnitSizeRaw(null);
        profile.setLatePenaltyMaxPtsRaw(null);
        profile.setLatePenaltyUnitPtsRaw(null);
        profile.setLatePenaltyUnitTimeRaw(null);

        newAssignment.setSubmissionProfileRelationship(profile);


        NSTimestamp due = new NSTimestamp();
        for (byte order = 0; order < numberOfSheets; order++)
        {
            due = due.timestampByAddingGregorianUnits(0, 0, 7, 0, 0, 0);
            for (CourseOffering offering: offerings)
            {
                log.debug("creating new schedule sheet assignment offering "
                    + order + "for " + offering);
                ScheduleSheetAssignmentOffering newOffering =
                    ScheduleSheetAssignmentOffering
                    .create(localContext(), false);
                newOffering.setAssignmentRelationship(newAssignment);
                newOffering.setCourseOfferingRelationship(offering);
                newOffering.setDueDate(due);
                newOffering.setClosedOnDate(due);
                newOffering.setOrder(order);
            }
        }

        try
        {
            localContext().saveChanges();
            profile.refetchObjectFromDBinEditingContext(localContext());
        }
        catch (Exception e)
        {
            return null;
        }
        EditScheduleSheetAssignmentPage page =
            pageWithName(EditScheduleSheetAssignmentPage.class);
        page.assignment = newAssignment;
        return page;
    }


    // ----------------------------------------------------------
    public boolean hasMultipleSections()
    {
        NSArray<CourseOffering> offerings = null;
        Course course = coreSelections().course();
        if (course == null && coreSelections().courseOffering() != null)
        {
            course = coreSelections().courseOffering().course();
        }
        Semester semester = coreSelections().semester();
        if (semester == null && coreSelections().courseOffering() != null)
        {
            semester = coreSelections().courseOffering().semester();
        }
        if (course != null && semester != null)
        {
            offerings = CourseOffering.offeringsForSemesterAndCourse(
                localContext(), course, semester);
        }
        return offerings != null && offerings.count() > 1;
    }


    //~ Instance/static fields ................................................

    static Logger log = Logger.getLogger(NewScheduleSheetAssignmentPage.class);
}
