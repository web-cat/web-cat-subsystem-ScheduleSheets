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

import org.webcat.core.CourseOffering;
import org.webcat.core.Semester;
import org.webcat.core.User;
import org.webcat.grader.AssignmentOffering;
import org.webcat.grader.SubmissionProfile;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;
import er.extensions.eof.ERXKey;

//-------------------------------------------------------------------------
/**
 * Represents a single schedule sheet deadline for a single course offering.
 * It is part of a set of zero or more schedule sheet offerings for a
 * group of one or more course offerings, probably all associated with a
 * single programming assignment or project.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class ScheduleSheetAssignmentOffering
    extends _ScheduleSheetAssignmentOffering
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new ComponentFeature object.
     */
    public ScheduleSheetAssignmentOffering()
    {
        super();
    }


    //~ Constants (for key names) .............................................

    // Derived Attributes ---
    public static final String AVAILABLE_FROM_KEY = "availableFrom";
    public static final String LATE_DEADLINE_KEY  = "lateDeadline";

    public static final ERXKey<NSTimestamp> availableFrom =
        new ERXKey<NSTimestamp>(AVAILABLE_FROM_KEY);
    public static final ERXKey<NSTimestamp> lateDeadline =
        new ERXKey<NSTimestamp>(LATE_DEADLINE_KEY);


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    public AssignmentOffering assignmentOffering()
    {
        if (assignmentOffering == null
            && assignment() != null
            && assignment().assignment() != null)
        {
            NSArray<AssignmentOffering> offerings = AssignmentOffering
                .objectsMatchingQualifier(editingContext(),
                    AssignmentOffering.courseOffering.is(courseOffering())
                    .and(AssignmentOffering.assignment.is(
                        assignment().assignment())));
            if (offerings.count() > 0)
            {
                if (offerings.count() > 1)
                {
                    log.error("Found multiple assignment offerings for "
                        + assignment().assignment()
                        + " in "
                        + courseOffering()
                        + ":\n"
                        + offerings);
                }
                assignmentOffering = offerings.get(0);
            }
        }
        return assignmentOffering;
    }


    // ----------------------------------------------------------
    @Override
    public void setCourseOffering(CourseOffering value)
    {
        assignmentOffering = null;
        super.setCourseOffering(value);
    }


    // ----------------------------------------------------------
    /**
     * Determine the time when this assignment begins accepting
     * submissions.
     * @return the start time for the acceptance window.
     */
    public NSTimestamp availableFrom()
    {
        NSTimestamp myDueDate = dueDate();
        NSTimestamp openingDate = null;
        if (myDueDate != null)
        {
            ScheduleSheetAssignment myAssignment = assignment();
            if (myAssignment != null)
            {
                SubmissionProfile submissionProfile =
                    myAssignment.submissionProfile();
                if (submissionProfile != null
                   && submissionProfile.availableTimeDeltaRaw() != null)
                {
                    openingDate = new NSTimestamp(
                        - submissionProfile.availableTimeDelta(),
                        myDueDate);
                }
            }
        }
        log.debug("availableFrom() = " + openingDate);
        return (openingDate == null)
            ? new NSTimestamp(0L)
            : openingDate;
    }


    // ----------------------------------------------------------
    /**
     * Determine the latest time when assignments are accepted.
     * @return the final deadline as a timestamp
     */
    public NSTimestamp lateDeadline()
    {
        NSTimestamp myDueDate = dueDate();
        if (myDueDate != null)
        {
            ScheduleSheetAssignment myAssignment = assignment();
            if (myAssignment != null)
            {
                SubmissionProfile submissionProfile =
                    myAssignment.submissionProfile();
                if (submissionProfile != null)
                {
                    myDueDate = new NSTimestamp(
                        submissionProfile.deadTimeDelta(),
                        myDueDate);
                }
            }
        }
        log.debug("lateDeadline() = " + myDueDate);
        return myDueDate;
    }


    // ----------------------------------------------------------
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accessibleByUser(User user)
    {
        return courseOffering() != null
            && courseOffering().accessibleByUser(user);
    }


    // ----------------------------------------------------------
    /**
     * Determine the latest time when assignments are accepted.
     * @param user The user to check for
     * @return The final deadline as a timestamp
     */
    public boolean userCanSubmit(User user)
    {
        boolean result = false;
        if (courseOffering().instructors().containsObject(user)
             || courseOffering().graders().containsObject(user))
        {
            result = true;
        }
        else if (publish())
        {
            NSTimestamp now = new NSTimestamp();
            result = availableFrom().before(now)
                && lateDeadline().after(now);
        }
        return result;
    }


    // ----------------------------------------------------------
    public String titleString(Semester semester)
    {
        CourseOffering course = courseOffering();
        ScheduleSheetAssignment myAssignment = assignment();
        String result = "";
        if (course != null)
        {
            result += course.compactName();
            if (course.semester() != semester)
            {
                result += "[" + course.semester() + "]";
            }
            result += " ";
        }
        if (myAssignment != null)
        {
            result += myAssignment.titleString();
            if (myAssignment.numberOfSheets() > 1)
            {
                result += ", Sheet " + (order() + 1);
            }
        }
        return result;
    }


    // ----------------------------------------------------------
    public String titleString()
    {
        CourseOffering course = courseOffering();
        return (course == null)
            ? titleString(null)
            : titleString(course.semester());
    }


    // ----------------------------------------------------------
    /**
     * Return the highest-numbered submission for this assignment offering
     * made by the given user.
     * @param user the user to look up
     * @return the most recent Submission object for the given user, or
     *         null if there is none
     */
    public ScheduleSheetSubmission mostRecentSubFor(User user)
    {
        ScheduleSheetSubmission mostRecent = null;
        NSArray<ScheduleSheetSubmission> subs = ScheduleSheetSubmission
            .objectsMatchingQualifier(
                editingContext(),
                ScheduleSheetSubmission.assignmentOffering.is(this)
                    .and(ScheduleSheetSubmission.user.is(user)),
                ScheduleSheetSubmission.submitNumber.descs());
        if (subs != null && subs.count() > 0)
        {
            mostRecent = subs.objectAtIndex(0);
        }
        return mostRecent;
    }


    // ----------------------------------------------------------
    /**
     * Return a list consisting of all the submissions for the given
     * student, arranged first to last.
     * @param user the user to look up
     * @return an NSArray of Submission objects
     */
    public NSArray<ScheduleSheetSubmission> allSubsFor(User user)
    {
        return ScheduleSheetSubmission.objectsMatchingQualifier(
            editingContext(),
            ScheduleSheetSubmission.assignmentOffering.is(this)
                .and(ScheduleSheetSubmission.user.is(user)),
            ScheduleSheetSubmission.submitNumber.ascs());
    }


    // ----------------------------------------------------------
    /**
     * Return a list consisting of the most recent submission for all
     * students who have submitted to this assignment offering.
     * @return an NSArray of Submission objects
     */
    public NSArray<ScheduleSheetSubmission> mostRecentSubsForAll()
    {
        NSMutableArray<ScheduleSheetSubmission> recentSubs =
            new NSMutableArray<ScheduleSheetSubmission>();
        NSMutableArray<User> students =
            courseOffering().students().mutableClone();
        NSArray<User> staff = courseOffering().instructors();
        students.removeObjectsInArray(staff);
        students.addObjectsFromArray(staff);
        staff = courseOffering().graders();
        students.removeObjectsInArray(staff);
        students.addObjectsFromArray(staff);
        for (User user : students)
        {
            ScheduleSheetSubmission s = mostRecentSubFor(user);
            if (s != null)
            {
                recentSubs.addObject(s);
            }
        }
        return recentSubs;
    }


    // ----------------------------------------------------------
    /**
     * Return the most recent processed submission result for this assignment
     * offering made by the given user.
     * @param user the user to look up
     * @return the most recent SubmissionResult object for the given user, or
     *         null if there is none
     */
    public ScheduleSheet mostRecentScheduleSheetFor(User user)
    {
        ScheduleSheet newest = null;
        NSArray<ScheduleSheet> subs =
            ScheduleSheet.objectsMatchingQualifier(editingContext(),
                ScheduleSheet.submissions
                    .dot(ScheduleSheetSubmission.assignmentOffering).is(this)
                .and(ScheduleSheet.submissions
                    .dot(ScheduleSheetSubmission.user).is(user)),
            ScheduleSheet.submissions
                .dot(ScheduleSheetSubmission.submitTime).descs());
        if (subs.count() > 0)
        {
            newest = subs.objectAtIndex(0);
        }
        return newest;
    }


    // ----------------------------------------------------------
    public boolean isFirstSheet()
    {
        return order() == 0;
    }


    // ----------------------------------------------------------
    public boolean isLastSheet()
    {
        return order() == assignment().numberOfSheets() - 1;
    }


    // ----------------------------------------------------------
    public double availablePoints()
    {
        return submissionProfile().availablePoints();
    }


    // ----------------------------------------------------------
    public void setAvailablePoints(double value)
    {
        if (value != availablePoints())
        {
            if (overrideProfile() == null)
            {
                SubmissionProfile master = assignment().submissionProfile();
                SubmissionProfile override = SubmissionProfile.create(
                    editingContext(),
                    master.allowPartners(),
                    master.autoAssignPartners(),
                    master.awardEarlyBonus(),
                    master.deductExcessSubmissionPenalty(),
                    master.deductLatePenalty());
                // Copy all the original's fields
                for (String key : master.allPropertyKeys())
                {
                    override.takeValueForKey(master.valueForKey(key), key);
                }
            }
            overrideProfile().setAvailablePoints(value);
        }
    }


    // ----------------------------------------------------------
    public SubmissionProfile submissionProfile()
    {
        SubmissionProfile result = overrideProfile();
        if (result == null)
        {
            result = assignment().submissionProfile();
        }
        return result;
    }


    // ----------------------------------------------------------
    @Override
    public NSTimestamp closedOnDate()
    {
        return new NSTimestamp(dueDate().getTime()
            + submissionProfile().deadTimeDelta());
    }


    //~ Instance/static fields ................................................

    private AssignmentOffering assignmentOffering;
}
