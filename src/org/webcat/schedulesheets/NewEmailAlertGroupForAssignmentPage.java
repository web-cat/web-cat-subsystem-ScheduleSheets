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
import org.apache.log4j.Logger;
import org.webcat.core.Course;
import org.webcat.core.CourseOffering;
import org.webcat.core.Semester;
import org.webcat.grader.Assignment;
import org.webcat.grader.AssignmentOffering;
import org.webcat.grader.GraderAssignmentsComponent;

//-------------------------------------------------------------------------
/**
 * Page to create a new group of e-mail alerts for an assignment.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class NewEmailAlertGroupForAssignmentPage
    extends GraderAssignmentsComponent
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new object.
     *
     * @param context The context to use
     */
    public NewEmailAlertGroupForAssignmentPage(WOContext context)
    {
        super(context);
    }


    //~ KVC Attributes (must be public) .......................................

    public byte numberOfAlerts = 3;
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
        if (prefs().assignment() != null)
        {
            assignment = prefs().assignment();
        }
        else if (prefs().assignmentOffering() != null)
        {
            assignment = prefs().assignmentOffering().assignment();
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
        if (numberOfAlerts < 1)
        {
            error("Please enter a positive number of e-mail alerts for "
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

        log.debug("creating new e-mail alert group");
        EmailAlertGroupForAssignment group = EmailAlertGroupForAssignment
            .create(localContext(), assignment);
        group.setAuthorRelationship(user());
        group.setNumberOfAlerts(numberOfAlerts);

        long time = 1000 * 60 * 60 * 24;   // 1 days
        for (int i = 0; i < numberOfAlerts; i++)
        {
            EmailAlertForAssignment eafa =
                EmailAlertForAssignment.create(localContext(), group);
            eafa.setAlertNo(numberOfAlerts - 1 - i);
            time *= 2;
            eafa.setTimeBeforeDue(time);
            for (CourseOffering co : offerings)
            {
                EmailAlertForAssignmentOffering eafao =
                    EmailAlertForAssignmentOffering
                    .create(localContext(), false, eafa, co);
                AssignmentOffering ao = AssignmentOffering
                    .firstObjectMatchingQualifier(localContext(),
                        AssignmentOffering.assignment.is(assignment).and(
                        AssignmentOffering.courseOffering.is(co)), null);
                eafao.setSendTime(
                    new NSTimestamp(ao.dueDate().getTime() - time));
            }
        }

        applyLocalChanges();

        EditEmailAlertGroupForAssignmentPage page =
            pageWithName(EditEmailAlertGroupForAssignmentPage.class);
        page.group = group;
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

    static Logger log =
        Logger.getLogger(NewEmailAlertGroupForAssignmentPage.class);
}