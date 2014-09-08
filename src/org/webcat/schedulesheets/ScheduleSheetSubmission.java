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
import org.webcat.core.Status;
import org.webcat.core.User;
import org.webcat.grader.Submission.CumulativeStats;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
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
                    .eq(assignmentOffering().assignment()))
                .and(ScheduleSheetAssignmentOffering.order
                    .eq(assignmentOffering().orderRaw())));
        if (partnerOfferings.count() > 0)
        {
            partnerOffering = partnerOfferings.get(0);
        }

        NSArray<ScheduleSheetSubmission> partnerSubs =
            partnerOffering.allSubsFor(partner);
        int partnerSubmitNumber = partnerSubs.count() + 1;

        ScheduleSheetSubmission newSubmission = ScheduleSheetSubmission.create(
            ec, false, true);

        newSubmission.setSubmitNumber(partnerSubmitNumber);
        newSubmission.setSubmitTime(submitTime());
        newSubmission.setAssignmentOfferingRelationship(partnerOffering);
        newSubmission.setSheetRelationship(sheet());
        newSubmission.setUserRelationship(partner);
        newSubmission.setPrimarySubmissionRelationship(this);
        addToPartneredSubmissionsRelationship(newSubmission);
        newSubmission.markBestSubmissionForGrading();
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
    public boolean isPartnerSubmission()
    {
        return primarySubmission() != null;
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
    /**
     * Determine whether this submission, or another it is compared against,
     * is the preferable one to use as the submission for grading.  A
     * submission is preferable for grading if it has any newer feedback,
     * or if neither submission has any feedback, if it was made more
     * recently.
     * @param other The other submission to compare against.
     * @return True if this submission should be used as the submission
     * for grading instead of the other one.
     */
    public boolean isBetterGradingChoiceThan(ScheduleSheetSubmission other)
    {
        if (sheet() == null)
        {
            if (other.sheet() == null)
            {
                // Neither has a result!
                return submitTime().after(other.submitTime());
            }
            // Otherwise, go with the other one
            return false;
        }
        else if (other.sheet() == null)
        {
            return true;
        }
        else if (sheet().lastUpdated() == null)
        {
            // We have no feedback, so check other
            if (other.sheet().lastUpdated() != null)
            {
                // Other has feedback, so go with it
                return false;
            }
            else if (sheet().status() != Status.TO_DO)
            {
                if (other.sheet().status() != Status.TO_DO)
                {
                    // Both have status change but no feedback timestamp,
                    // so go with newest submission
                    return submitTime().after(other.submitTime());
                }
                else
                {
                    // We have a status change but other does not
                    return true;
                }
            }
            else
            {
                // Neither has feedback, so go with newest submission
                return submitTime().after(other.submitTime());
            }
        }
        else if (other.sheet().lastUpdated() == null)
        {
            // We have feedback, but other doesn't
            return true;
        }
        else
        {
            // Both have feedback, so go with most recent feedback
            return sheet().lastUpdated().after(other.sheet().lastUpdated());
        }
    }


    // ----------------------------------------------------------
    public void markBestSubmissionForGrading()
    {
        ScheduleSheetSubmission best = this;
        for (ScheduleSheetSubmission sub :allSubmissions())
        {
            if (sub.isBetterGradingChoiceThan(best))
            {
                if (best.isSubmissionForGrading())
                {
                    best.setIsSubmissionForGrading(false);
                }
                best = sub;
            }
            else if (sub.isSubmissionForGrading())
            {
                sub.setIsSubmissionForGrading(false);
            }
        }
        best.setIsSubmissionForGrading(true);
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


    // ----------------------------------------------------------
    /**
     * Gets the array of all submissions in the submission chain that contains
     * this submission (that is, all submissions for this submission's user and
     * assignment offering). The returned array is sorted by submission time in
     * ascending order.
     *
     * @return an NSArray containing the Submission objects in this submission
     *     chain
     */
    public NSArray<ScheduleSheetSubmission> allSubmissions()
    {
        int newAOSubCount = assignmentOffering().submissions().count();
        if (newAOSubCount != aoSubmissionsCountCache)
        {
            allSubmissionsCache = null;
            aoSubmissionsCountCache = newAOSubCount;
        }

        if (allSubmissionsCache == null)
        {
            if (user() != null && assignmentOffering() != null)
            {
                allSubmissionsCache = assignmentOffering().allSubsFor(user());
            }
        }

        return (allSubmissionsCache != null) ?
                allSubmissionsCache : NO_SUBMISSIONS;
    }


    // ----------------------------------------------------------
    /**
     * Flush any cached data stored by the object in memory.
     */
    @Override
    public void flushCaches()
    {
        // Clear the in-memory cache of the all-submissions chain so that it
        // will be fetched again.

        aoSubmissionsCountCache = 0;
        allSubmissionsCache = null;
    }


    // ----------------------------------------------------------
    /**
     * Gets the submission in this submission chain that represents the
     * "submission for grading". If no manual grading has yet occurred, then
     * this is equivalent to {@link #latestSubmission()}. Otherwise, if a TA
     * has manually graded one or more submissions, then this method returns
     * the latest of those.
     *
     * @return the submission for grading in this submission chain
     */
    public ScheduleSheetSubmission gradedSubmission()
    {
        return submissionForGrading(
            editingContext(), assignmentOffering(), user());
    }


    // ----------------------------------------------------------
    /**
     * Gets the earliest (in other words, first) submission made in this
     * submission chain.
     *
     * @return the earliest submission in the submission chain
     */
    public ScheduleSheetSubmission earliestSubmission()
    {
        if (user() == null || assignmentOffering() == null) return null;

        NSArray<ScheduleSheetSubmission> subs = allSubmissions();

        if (subs != null && subs.count() >= 1)
        {
            return subs.objectAtIndex(0);
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets the latest (in other words, last) submission made in this
     * submission chain. Typically clients should prefer to use the
     * {@link #gradedSubmission()} method over this one, depending on their
     * policy regarding the grading of submissions that are not the most recent
     * one; this method exists for symmetry and to allow clients to distinguish
     * between the graded submission and the last one, if necessary.
     *
     * @return the latest submission in the submission chain
     */
    public ScheduleSheetSubmission latestSubmission()
    {
        if (user() == null || assignmentOffering() == null) return null;

        NSArray<ScheduleSheetSubmission> subs = allSubmissions();

        if (subs != null && subs.count() >= 1)
        {
            return subs.objectAtIndex(subs.count() - 1);
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets the index of the submission with the specified submission number in
     * the allSubmissions array. This function isolates the logic required to
     * handle the rare but possible case where there are gaps in the submission
     * numbers of a student's submissions.
     *
     * @param number the submission number to search for
     *
     * @return the index of that submission in the allSubmissions array
     */
    private int indexOfSubmissionWithSubmitNumber(int number)
    {
        NSArray<ScheduleSheetSubmission> subs = allSubmissions();

        if (subs.isEmpty())
        {
            return -1;
        }

        int index = number - 1;

        if (index < 0)
        {
            index = 0;
        }
        else if (index > subs.count() - 1)
        {
            index = subs.count() - 1;
        }

        while (0 <= index && index < subs.count())
        {
            ScheduleSheetSubmission sub = subs.objectAtIndex(index);

            if (sub.submitNumber() == number)
            {
                return index;
            }
            else if (sub.submitNumber() < number)
            {
                index++;
                if (index < subs.count()
                    && subs.objectAtIndex(index).submitNumber() > number)
                {
                    // oops! not found
                    return -1;
                }
            }
            else if (sub.submitNumber() > number)
            {
                index--;
                if (index >= 0
                    && subs.objectAtIndex(index).submitNumber() < number)
                {
                    // oops! not found
                    return -1;
                }
            }
        }

        return -1;
    }


    // ----------------------------------------------------------
    /**
     * Gets from the submission chain the submission with the specified
     * submission number.
     *
     * @param number the number of the submission to retrieve from the chain
     *
     * @return the specified submission, or null if there was not one with
     *     that number in the chain (or there was some other error)
     */
    public ScheduleSheetSubmission submissionWithSubmitNumber(int number)
    {
        if (user() == null || assignmentOffering() == null)
        {
            return null;
        }

        int index = indexOfSubmissionWithSubmitNumber(number);

        if (index == -1)
        {
            return null;
        }
        else
        {
            return allSubmissions().objectAtIndex(index);
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets the previous submission to this one in the submission chain.
     *
     * @return the previous submission, or null if it is the first one (or
     *     there was an error)
     */
    public ScheduleSheetSubmission previousSubmission()
    {
        if (submitNumberRaw() == null)
        {
            return null;
        }
        else
        {
            NSArray<ScheduleSheetSubmission> subs = allSubmissions();
            int index = indexOfSubmissionWithSubmitNumber(submitNumber());

            if (index <= 0)
            {
                return null;
            }
            else
            {
                return subs.objectAtIndex(index - 1);
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets the next submission following this one in the submission chain.
     *
     * @return the next submission, or null if it is the first one (or
     *     there was an error)
     */
    public ScheduleSheetSubmission nextSubmission()
    {
        if (submitNumberRaw() == null)
        {
            return null;
        }
        else
        {
            NSArray<ScheduleSheetSubmission> subs = allSubmissions();
            int index = indexOfSubmissionWithSubmitNumber(submitNumber());

            if (index == -1 || index == subs.count() - 1)
            {
                return null;
            }
            else
            {
                return subs.objectAtIndex(index + 1);
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Find all submissions that are used for scoring for a given
     * assignment.
     *
     * @param ec                 The editing context
     * @param anAssignmentOffering The offering to search for.
     * @param omitPartners       If false, include submissions from all
     *                           partners working together.  If true,
     *                           include only the primary submitter's
     *                           submission.
     * @param omitStaff          If true, leave out course staff.
     * @param accumulator        If non-null, use this object to accumulate
     *                           descriptive summary statistics about the
     *                           submissions.
     * @return An array of the submissions found.
     */
    public static NSArray<UserSubmissionPair> submissionsForGrading(
        EOEditingContext   ec,
        ScheduleSheetAssignmentOffering anAssignmentOffering,
        boolean            omitPartners,
        boolean            omitStaff,
        CumulativeStats    accumulator)
    {
        CourseOffering courseOffering = anAssignmentOffering.courseOffering();
        NSArray<User> users = omitStaff
            ? courseOffering.studentsWithoutStaff()
            : courseOffering.studentsAndStaff();

        return submissionsForGrading(
            ec, anAssignmentOffering, omitPartners, users, accumulator);
    }


    // ----------------------------------------------------------
    /**
     * Find all submissions that are used for scoring for a given
     * assignment by the specified users.
     *
     * @param ec                 The editing context
     * @param anAssignmentOffering The offering to search for.
     * @param omitPartners       If false, include submissions from all
     *                           partners working together.  If true,
     *                           include only the primary submitter's
     *                           submission.
     * @param users              The list of users to find submissions for.
     * @param accumulator        If non-null, use this object to accumulate
     *                           descriptive summary statistics about the
     *                           submissions.
     * @return An array of the user/submission pairs.
     */
    public static NSArray<UserSubmissionPair> submissionsForGrading(
        EOEditingContext   ec,
        ScheduleSheetAssignmentOffering anAssignmentOffering,
        boolean            omitPartners,
        NSArray<User>      users,
        CumulativeStats    accumulator)
    {
        NSMutableDictionary<User, ScheduleSheetSubmission> submissions =
            new NSMutableDictionary<User, ScheduleSheetSubmission>();
        NSMutableArray<User> realUsers = users.mutableClone();

        for (User student : users)
        {
            log.debug("Scanning submissions for " + student);
            ScheduleSheetSubmission forGrading =
                submissionForGrading(ec, anAssignmentOffering, student);
            if (forGrading != null)
            {
                if (omitPartners
                    && forGrading.isPartnerSubmission()
                    && forGrading.sheet().submission()
                        == forGrading.sheet().submission().gradedSubmission())
                {
                    realUsers.removeObject(student);
                }
                else
                {
                    submissions.setObjectForKey(forGrading, student);
                    if (accumulator != null)
                    {
                        accumulator.accumulate(forGrading.sheet());
                    }
                }
            }
        }

        return generateUserSubmissionPairs(realUsers, submissions);
    }


    // ----------------------------------------------------------
    private static NSArray<UserSubmissionPair> generateUserSubmissionPairs(
            NSArray<User> users,
            NSDictionary<User, ScheduleSheetSubmission> submissions)
    {
        NSMutableArray<UserSubmissionPair> pairs =
            new NSMutableArray<UserSubmissionPair>(users.size());

        for (User aUser : users)
        {
            ScheduleSheetSubmission submission =
                submissions.objectForKey(aUser);
            pairs.addObject(new UserSubmissionPair(aUser, submission));
        }

        return pairs;
    }


    // ----------------------------------------------------------
    /**
     * Retrieve objects according to the <code>submissionsForGrading</code>
     * fetch specification.
     *
     * @param context The editing context to use
     * @param assignmentOfferingBinding fetch spec parameter
     * @param userBinding fetch spec parameter
     * @return an NSArray of the entities retrieved
     */
    public static ScheduleSheetSubmission submissionForGrading(
            EOEditingContext context,
            ScheduleSheetAssignmentOffering assignmentOfferingBinding,
            User userBinding
        )
    {
        NSArray<ScheduleSheetSubmission> subs =
            ScheduleSheetSubmission.objectsMatchingQualifier(
            context,
            ScheduleSheetSubmission.assignmentOffering
            .eq(assignmentOfferingBinding)
            .and(ScheduleSheetSubmission.user.eq(userBinding))
            .and(ScheduleSheetSubmission.sheet.isNotNull()),
            ScheduleSheetSubmission.isSubmissionForGrading.desc()
            .then(ScheduleSheetSubmission.sheet
                .dot(ScheduleSheet.lastUpdated).desc())
            .then(ScheduleSheetSubmission.sheet
                .dot(ScheduleSheet.status).desc())
            .then(ScheduleSheetSubmission.submitNumber.desc()));
        return subs.count() > 0 ? subs.get(0) : null;
    }


    // ----------------------------------------------------------
    public ScheduleSheet result()
    {
        return sheet();
    }


    //~ Instance/static fields ................................................

    private int aoSubmissionsCountCache;
    private NSArray<ScheduleSheetSubmission> allSubmissionsCache;

    private static final NSArray<ScheduleSheetSubmission> NO_SUBMISSIONS =
        new NSArray<ScheduleSheetSubmission>();
}
