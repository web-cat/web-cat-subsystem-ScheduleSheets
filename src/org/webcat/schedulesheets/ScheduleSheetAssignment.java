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
import org.webcat.core.User;
import org.webcat.grader.SubmissionProfile;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSTimestamp;

//-------------------------------------------------------------------------
/**
 * Represents a single set of schedule sheet assignment offerings associated
 * with a group of course offerings for a course within a given semester.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class ScheduleSheetAssignment
    extends _ScheduleSheetAssignment
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new ScheduleSheetAssignment object.
     */
    public ScheduleSheetAssignment()
    {
        super();
    }


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    public String titleString()
    {
        String result = name();
        if (shortDescription() != null)
        {
            result += ": " + shortDescription();
        }
        return result;
    }


    // ----------------------------------------------------------
    public boolean usesToolCheckScore()
    {
        SubmissionProfile profile = submissionProfile();
        return profile != null
            && (profile.toolPointsRaw() != null
                && profile.toolPoints() != 0);
    }


    // ----------------------------------------------------------
    public boolean usesTAScore()
    {
        SubmissionProfile profile = submissionProfile();
        return profile != null
            && (profile.taPoints() > 0.0);
    }


    // ----------------------------------------------------------
    public boolean usesTestingScore()
    {
        return false;
    }


    // ----------------------------------------------------------
    public boolean usesBonusesOrPenalties()
    {
        SubmissionProfile profile = submissionProfile();
        return profile != null
            && (profile.awardEarlyBonus()
                || profile.deductLatePenalty()
                || profile.deductExcessSubmissionPenalty());
    }


    // ----------------------------------------------------------
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accessibleByUser(User user)
    {
        for (ScheduleSheetAssignmentOffering offering : offerings())
        {
            if (offering.accessibleByUser(user))
            {
                return true;
            }
        }
        return false;
    }


    // ----------------------------------------------------------
    public NSTimestamp commonOfferingsDueDate()
    {
        NSTimestamp common = null;
        NSArray<ScheduleSheetAssignmentOffering> myOfferings = offerings();
        if (myOfferings.count() > 1)
        {
            for (ScheduleSheetAssignmentOffering ao : myOfferings)
            {
                if (common == null)
                {
                    common = ao.dueDate();
                }
                else if (common.compare(ao.dueDate()) !=
                         NSComparator.OrderedSame)
                {
                    common = null;
                    break;
                }
            }
        }
        return common;
    }


    // ----------------------------------------------------------
    public NSArray<ScheduleSheetAssignmentOffering> offeringsForCourseOffering(
        CourseOffering aCourseOffering)
    {
        return ScheduleSheetAssignmentOffering.objectsMatchingQualifier(
            editingContext(),
            ScheduleSheetAssignmentOffering.assignment.is(this)
            .and(ScheduleSheetAssignmentOffering.courseOffering
                .is(aCourseOffering)),
            ScheduleSheetAssignmentOffering.order.ascs());
    }


    // ----------------------------------------------------------
    /**
     * Return the highest-numbered submission for this assignment offering
     * made by the given user.
     * @param user the user to look up
     * @return the most recent Submission object for the given user, or
     *         null if there is none
     */
    public ScheduleSheetSubmission[] mostRecentSubsFor(
        User user, CourseOffering aCourseOffering)
    {
        NSArray<ScheduleSheetAssignmentOffering> sheets =
            offeringsForCourseOffering(aCourseOffering);
        ScheduleSheetSubmission[] result =
            new ScheduleSheetSubmission[sheets.count()];
        for (ScheduleSheetAssignmentOffering o : sheets)
        {
            result[o.order()] = o.mostRecentSubFor(user);
        }
        return result;
    }


    // ----------------------------------------------------------
//    public ScheduleSheetAssignmentOffering offeringForUser(User user)
//    {
//        ScheduleSheetAssignmentOffering offering = null;
//
//        // First, check to see if the user is a student in any of the
//        // course offerings associated with the available assignment offerings
//        NSMutableArray<ScheduleSheetAssignmentOffering> results =
//            new NSMutableArray<ScheduleSheetAssignmentOffering>();
//        for (ScheduleSheetAssignmentOffering o : offerings())
//        {
//            if (o.courseOffering().students().contains(user))
//            {
//                results.add(o);
//            }
//        }
//        if (results.count() == 0)
//        {
//            // if the user is not found as a student, check for staff instead
//            for (ScheduleSheetAssignmentOffering o : offerings())
//            {
//                if (o.courseOffering().isStaff(user))
//                {
//                    results.add(o);
//                }
//            }
//        }
//        if (results.count() > 0)
//        {
//            offering = results.objectAtIndex(0);
//        }
//        return offering;
//    }


    //~ Instance/static fields ................................................

}
