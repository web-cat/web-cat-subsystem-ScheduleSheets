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
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableSet;

//-------------------------------------------------------------------------
/**
 * Represents the submission of one schedule sheet by a student--always
 * associated with a ScheduleSheet (the result of the submission).  This
 * entity corresponds to the Submission object for a programming assignment
 * submission.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class ScheduleSheetSubmission
    extends _ScheduleSheetSubmission
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new ScheduleSheetSubmission object.
     */
    public ScheduleSheetSubmission()
    {
        super();
    }


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    public void partnerWith(NSArray<User> users)
    {
        // Collect all of the students enrolled in any offering of the course
        // to which the submission is being made.

        NSMutableSet<User> studentsEnrolled = new NSMutableSet<User>();

        EOEditingContext ec = editingContext();
        if (ec == null && assignmentOffering() != null)
        {
            ec = assignmentOffering().editingContext();
        }

        // Find all offerings of the current course in this semester
        NSArray<CourseOffering> offerings =
            CourseOffering.offeringsForSemesterAndCourse(ec,
                assignmentOffering().courseOffering().course(),
                assignmentOffering().courseOffering().semester());
        for (CourseOffering offering : offerings)
        {
            studentsEnrolled.addObjectsFromArray(offering.students());
        }

        for (User partner : users)
        {
            // Only partner a user on a submission if they are enrolled in the
            // same course as the user making the primary submission (but not
            // necessarily in the same offering -- this is a little more
            // flexible).

            if (studentsEnrolled.containsObject(partner))
            {
                partnerWith(partner);
            }
        }
    }


    // ----------------------------------------------------------
    public void partnerWith(User partner)
    {
        // Make sure that a user isn't trying to partner with himself.

        if (partner.equals(user()))
        {
            return;
        }
        if (primarySubmission() != null)
        {
            primarySubmission().partnerWith(partner);
            return;
        }

        EOEditingContext ec = editingContext();
        if (ec == null)
        {
            ec = partner.editingContext();
        }

        int partnerSubmitNumber = 1;

        // Find partner's home courseOffering and its assignment offering
        ScheduleSheetAssignmentOffering partnerOffering = assignmentOffering();
        NSArray<ScheduleSheetAssignmentOffering> partnerOfferings =
            ScheduleSheetAssignmentOffering.objectsMatchingQualifier(ec,
                ScheduleSheetAssignmentOffering.courseOffering
                    .dot(CourseOffering.course)
                    .eq(assignmentOffering().courseOffering().course())
                .and(ScheduleSheetAssignmentOffering.courseOffering
                    .dot(CourseOffering.students).eq(partner))
                .and(ScheduleSheetAssignmentOffering.assignment
                    .eq(assignmentOffering().assignment())));
        if (partnerOfferings.count() > 0)
        {
            partnerOffering = partnerOfferings.get(0);
        }

        ScheduleSheetSubmission highestSubmission =
            partnerOffering.mostRecentSubFor(partner);

        if (highestSubmission != null)
        {
            partnerSubmitNumber = highestSubmission.submitNumber() + 1;
        }

        if (isSubmissionForGrading())
        {
            for (ScheduleSheetSubmission other :
                partnerOffering.allSubsFor(partner))
            {
                other.setIsSubmissionForGrading(false);
            }
        }
        ScheduleSheetSubmission newSubmission = ScheduleSheetSubmission.create(
            ec, isSubmissionForGrading(), true);

        newSubmission.setSubmitNumber(partnerSubmitNumber);
        newSubmission.setSubmitTime(submitTime());
        newSubmission.setAssignmentOfferingRelationship(partnerOffering);
        newSubmission.setSheetRelationship(sheet());
        newSubmission.setUserRelationship(partner);
        newSubmission.setPrimarySubmissionRelationship(this);

        addToPartneredSubmissionsRelationship(newSubmission);
    }


    // ----------------------------------------------------------
    public void unpartnerFrom(NSArray<User> users)
    {
        for (User partner : users)
        {
            unpartnerFrom(partner);
        }
    }


    // ----------------------------------------------------------
    public void unpartnerFrom(User partner)
    {
        EOEditingContext ec = editingContext();
        if (ec == null)
        {
            ec = partner.editingContext();
        }

        for (ScheduleSheetSubmission partneredSubmission :
            ScheduleSheetSubmission.objectsMatchingQualifier(ec,
                ScheduleSheetSubmission.sheet.is(sheet()).and(
                    ScheduleSheetSubmission.user.eq(partner))))
        {
            partneredSubmission.setSheetRelationship(null);
            ec.deleteObject(partneredSubmission);
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets an array containing all the users associated with this submission;
     * that is, the user who submitted it as well as any partners.
     *
     * @return the array of all users associated with the submission
     */
    public NSArray<User> allUsers()
    {
        NSMutableArray<User> users = (NSMutableArray<User>)allPartners();
        users.addObject(user());
        return users;
    }


    // ----------------------------------------------------------
    /**
     * Gets an array containing all the partners associated with this
     * submission, excluding the user who submitted it.
     *
     * @return the array of all partners associated with the submission
     */
    public NSArray<User> allPartners()
    {
        NSMutableArray<User> users = new NSMutableArray<User>();

        for (ScheduleSheetSubmission partnerSub : partneredSubmissions())
        {
            users.addObject(partnerSub.user());
        }

        return users;
    }


    // ----------------------------------------------------------
    /**
     * Gets a string containing the names of the user who made this submission
     * and all of his or her partners, in the form "User 1, User 2, ..., and
     * User N".
     *
     * @return a string containing the names of all of the partners
     */
    public String namesOfAllUsers()
    {
        NSMutableArray<String> names = new NSMutableArray<String>();
        names.addObject(user().nameAndUid());

        for (ScheduleSheetSubmission partnerSub : partneredSubmissions())
        {
            names.addObject(partnerSub.user().nameAndUid());
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append(names.objectAtIndex(0));

        if (names.count() > 1)
        {
            for (int i = 1; i < names.count() - 1; i++)
            {
                buffer.append(", ");
                buffer.append(names.objectAtIndex(i));
            }

            if (names.count() > 2)
            {
                buffer.append(',');
            }

            buffer.append(" and ");
            buffer.append(names.lastObject());
        }

        return buffer.toString();
    }


    // ----------------------------------------------------------
    /**
     * Gets a string containing the names of the user who made this submission
     * and all of his or her partners, in the form "User 1, User 2, ..., and
     * User N".
     *
     * @return a string containing the names of all of the partners
     */
    public String namesOfAllUsers_LF()
    {
        NSMutableArray<String> names = new NSMutableArray<String>();
        names.addObject(user().nameAndUid_LF());

        for (ScheduleSheetSubmission partnerSub : partneredSubmissions())
        {
            names.addObject(partnerSub.user().nameAndUid_LF());
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append(names.objectAtIndex(0));

        if (names.count() > 1)
        {
            for (int i = 1; i < names.count() - 1; i++)
            {
                buffer.append("; ");
                buffer.append(names.objectAtIndex(i));
            }

            if (names.count() > 2)
            {
                buffer.append(';');
            }

            buffer.append(" and ");
            buffer.append(names.lastObject());
        }

        return buffer.toString();
    }


    // ----------------------------------------------------------
    public void markAsSubmissionForGrading()
    {
        for (ScheduleSheetSubmission sub :
            assignmentOffering().allSubsFor(user()))
        {
            sub.setIsSubmissionForGrading(false);
        }
        setIsSubmissionForGrading(true);
    }


    // ----------------------------------------------------------
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accessibleByUser(User aUser)
    {
        return aUser == user()
            || (assignmentOffering() != null
                && assignmentOffering().accessibleByUser(aUser));
    }


    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether or not the submission was late.
     *
     * @return true if the submission was late, otherwise false
     */
    public boolean isLate()
    {
        return submitTime().after(assignmentOffering().dueDate());
    }


    // ----------------------------------------------------------
    /**
     * Computes the difference between the submission time and the
     * due date/time, and renders it in a human-readable string.
     *
     * @return the string representation of how early or late
     */
    public String earlyLateStatus()
    {
        String description = null;
        long time = submitTime().getTime();
        long dueTime = assignmentOffering().dueDate().getTime();
        if (dueTime >= time)
        {
            // Early submission
            description = getStringTimeRepresentation(dueTime - time)
                + " early";
        }
        else
        {
            // Late submission
            description = getStringTimeRepresentation(time - dueTime)
                + " late";
        }
        return description;
    }


    // ----------------------------------------------------------
    /**
     * Converts a time to its human-readable format.  Most useful
     * when the time is "small," like a difference between two
     * other time stamps.
     *
     * @param time The time to convert
     * @return     A human-readable version of the time
     */
    public static String getStringTimeRepresentation(long time)
    {
        return org.webcat.grader.Submission.getStringTimeRepresentation(time);
    }


    //~ Instance/static fields ................................................

}
