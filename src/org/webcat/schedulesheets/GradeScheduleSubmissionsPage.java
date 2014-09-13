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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import er.extensions.appserver.ERXDisplayGroup;
import er.extensions.foundation.ERXArrayUtilities;
import org.webcat.core.Status;
import org.webcat.core.User;
import org.webcat.grader.GraderAssignmentsComponent;
import static org.webcat.grader.Submission.CumulativeStats;
import org.webcat.ui.util.ComponentIDGenerator;

//-------------------------------------------------------------------------
/**
 * The main grading page for grading schedule assignments.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class GradeScheduleSubmissionsPage
    extends GraderAssignmentsComponent
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    /**
     * Create a new object.
     * @param context
     */
    public GradeScheduleSubmissionsPage(WOContext context)
    {
        super(context);
    }


    //~ KVC Attributes (must be public) .......................................

    public ScheduleSheetAssignment assignment;
    public NSMutableArray<Integer> sheetNos;
    public Integer aSheetNo;

    public ERXDisplayGroup<ScheduleSheetAssignmentOffering> offerings;
    public ScheduleSheetAssignmentOffering offering;
    public ScheduleSheetSubmission aSubmission;
    public ERXDisplayGroup<ScheduleSheetSubmission> studentNewerSubmissions;
    public ScheduleSheetSubmission aNewerSubmission;
    public ERXDisplayGroup<UserSubmissionPair> staffPairs;
    /** index in the staff worepetition */
    public int staffIndex;

    public ComponentIDGenerator idFor = new ComponentIDGenerator(this);


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    protected void beforeAppendToResponse(
        WOResponse response, WOContext context)
    {
        super.beforeAppendToResponse(response, context);

        subStats =
            new HashMap<ScheduleSheetAssignmentOffering, CumulativeStats>();
        timeInvestedStats =
            new HashMap<ScheduleSheetAssignmentOffering, CumulativeStats>();
        estimatedRemainingStats =
            new HashMap<ScheduleSheetAssignmentOffering, CumulativeStats>();
        componentStats =
            new HashMap<ScheduleSheetAssignmentOffering, CumulativeStats>();

        org.webcat.grader.Assignment target = prefs().assignment();
        if (target == null)
        {
            if (prefs().assignment() != null)
            {
                target = prefs().assignment();
            }
            else if (prefs().assignmentOffering() != null)
            {
                target = prefs().assignmentOffering().assignment();
            }
        }
        if (target != null)
        {
            offerings.setObjectArray(ScheduleSheetAssignmentOffering
                .objectsMatchingQualifier(
                localContext(),
                ScheduleSheetAssignmentOffering.assignment
                .dot(ScheduleSheetAssignment.assignment)
                .is(target)));
            if (offerings.displayedObjects().count() > 0)
            {
                assignment = offerings.displayedObjects().get(0).assignment();
                offerings.queryMatch().takeValueForKey(assignment,
                    ScheduleSheetAssignmentOffering.ASSIGNMENT_KEY);
                offerings.qualifyDisplayGroup();
            }
            else
            {
                target = null;
            }

            sheetNos =
                new NSMutableArray<Integer>(assignment.numberOfSheets());
            staffPairsBySheet.clear();
            if (assignment != null)
            {
                for (int i = 0; i < assignment.numberOfSheets(); i++)
                {
                    sheetNos.add(i + 1);
                    staffPairsBySheet.add(
                        new NSMutableArray<UserSubmissionPair>());
                }
                if (sheetNo < 1 || sheetNo > assignment.numberOfSheets())
                {
                    sheetNo = 1;
                }
            }
        }

        NSArray<User> admins = User.administrators(localContext());
        for (ScheduleSheetAssignmentOffering o : offerings.allObjects())
        {
            offering = o;
            NSArray<UserSubmissionPair> pairs =
                ScheduleSheetSubmission.submissionsForGrading(localContext(),
                    o, true, true, studentStats());
            for (UserSubmissionPair p : pairs)
            {
                if (p.submission() != null)
                {
                    timeInvestedStats().accumulate(p.submission().sheet());
                    estimatedRemainingStats().accumulate(
                        p.submission().sheet());
                    componentStats().accumulate(
                        p.submission().sheet());
                }
            }
            studentSubPairs().setObjectArray(pairs);

            @SuppressWarnings("unchecked")
            NSArray<User> staff = ERXArrayUtilities
                .arrayByAddingObjectsFromArrayWithoutDuplicates(
                    o.courseOffering().staff(),
                    admins);
            pairs = ScheduleSheetSubmission.submissionsForGrading(
                localContext(), o, true, staff, null);

            @SuppressWarnings("unchecked")
            NSArray<UserSubmissionPair> filteredPairs = ERXArrayUtilities
                .filteredArrayWithQualifierEvaluation(
                pairs, UserSubmissionPair.submission.isNotNull());
            staffPairsBySheet.get(o.order()).addAll(filteredPairs);

        }
        setSheetNo(sheetNo);
    }


    // ----------------------------------------------------------
    public ScheduleSheetSubmission submission()
    {
        return submission;
    }


    // ----------------------------------------------------------
    public String tableId()
    {
        return idFor.get("submissionsTable_" + offering.id());
    }


    // ----------------------------------------------------------
    public CumulativeStats studentStats()
    {
        CumulativeStats stats = subStats.get(offering);
        if (stats == null)
        {
            stats = new CumulativeStats();
            subStats.put(offering, stats);
        }
        return stats;
    }


    // ----------------------------------------------------------
    public CumulativeStats timeInvestedStats()
    {
        CumulativeStats stats = timeInvestedStats.get(offering);
        if (stats == null)
        {
            stats = new CumulativeStats("newTimeInvestedTotal");
            timeInvestedStats.put(offering, stats);
        }
        return stats;
    }


    // ----------------------------------------------------------
    public CumulativeStats estimatedRemainingStats()
    {
        CumulativeStats stats = estimatedRemainingStats.get(offering);
        if (stats == null)
        {
            stats = new CumulativeStats("newEstimatedRemaining");
            estimatedRemainingStats.put(offering, stats);
        }
        return stats;
    }


    // ----------------------------------------------------------
    public CumulativeStats componentStats()
    {
        CumulativeStats stats = componentStats.get(offering);
        if (stats == null)
        {
            stats = new CumulativeStats("componentFeatures.count");
            componentStats.put(offering, stats);
        }
        return stats;
    }


    // ----------------------------------------------------------
    public ERXDisplayGroup<UserSubmissionPair> studentSubPairs()
    {
        ERXDisplayGroup<UserSubmissionPair> group =
            studentSubPairs.get(offering);
        if (group == null)
        {
            group = new ERXDisplayGroup<UserSubmissionPair>();
            group.setNumberOfObjectsPerBatch(100);
            group.setSortOrderings(
                UserSubmissionPair.user.dot(User.name_LF).ascInsensitives()
                .then(UserSubmissionPair.user.dot(User.userName)
                    .ascInsensitive()));
            studentSubPairs.put(offering, group);
        }
        return group;
    }


    // ----------------------------------------------------------
    public UserSubmissionPair pair()
    {
        return pair;
    }


    // ----------------------------------------------------------
    public void setPair(UserSubmissionPair value)
    {
        pair = value;
        if (pair != null)
        {
            aSubmission = pair.submission();
            if (aSubmission != null)
            {
                studentNewerSubmissions.setObjectArray(
                    aSubmission.allSubmissions());
                studentNewerSubmissions.queryMin().takeValueForKey(
                    aSubmission.submitNumber() + 1,
                    ScheduleSheetSubmission.SUBMIT_NUMBER_KEY);
                studentNewerSubmissions.qualifyDisplayGroup();
            }
        }
    }


    // ----------------------------------------------------------
    public int sheetNo()
    {
        return sheetNo;
    }


    // ----------------------------------------------------------
    public void setSheetNo(int newValue)
    {
        sheetNo = newValue;
        staffPairs.setObjectArray(staffPairsBySheet.get(sheetNo - 1));
        offerings.queryMatch().takeValueForKey(
            sheetNo - 1, ScheduleSheetAssignmentOffering.ORDER_KEY);
        offerings.qualifyDisplayGroup();
    }


    // ----------------------------------------------------------
    public boolean isMostRecentSubmission()
    {
        return aSubmission == null
            || aSubmission == aSubmission.latestSubmission();
    }


    // ----------------------------------------------------------
    public int mostRecentSubmissionNo()
    {
        return aSubmission.latestSubmission().submitNumber();
    }


    // ----------------------------------------------------------
    public String submitTimeSpanClass()
    {
        if (aSubmission.isLate())
        {
            return "warn";
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    public String newerSubmitTimeSpanClass()
    {
        if (aNewerSubmission.isLate())
        {
            return "warn sm";
        }
        else
        {
            return "sm";
        }
    }


    // ----------------------------------------------------------
    public String newerSubmissionStatus()
    {
        String result = "feedback entered on earlier submission";
        // check date of submission against date of feedback
        if (aSubmission.sheet().lastUpdated() != null
            && aNewerSubmission.submitTime().after(
                    aSubmission.sheet().lastUpdated()))
        {
            result = "newer than feedback";
        }
        return result;
    }


    // ----------------------------------------------------------
    public WOComponent editSubmissionScore()
    {
        return editSubmissionScore(aSubmission);
    }


    // ----------------------------------------------------------
    public WOComponent editNewerSubmissionScore()
    {
        return editSubmissionScore(aNewerSubmission);
    }


    // ----------------------------------------------------------
    public WOComponent reload()
    {
        return null;
    }


    // ----------------------------------------------------------
    private WOComponent editSubmissionScore(ScheduleSheetSubmission sub)
    {
        SheetFeedbackPage page = pageWithName(SheetFeedbackPage.class);
        page.submission = sub;
        page.edit = true;
        page.nextPage = this;
        return page;
    }


    // ----------------------------------------------------------
    public WOComponent regradeSubmissions()
    {
        for (ScheduleSheetAssignmentOffering o : offerings.displayedObjects())
        {
            for (ScheduleSheetSubmission sub : o.submissions())
            {
                sub.sheet().runAutomaticChecks(true);
            }
        }
        applyLocalChanges();
        return null;
    }


    // ----------------------------------------------------------
    public String markCompleteStatusIndicatorId()
    {
        return idFor.get("markCompleteStatusIndicator_"
                + offering.id());
    }


    // ----------------------------------------------------------
    /**
     * Marks all the submissions shown that have been partially graded as
     * being completed, sending e-mail notifications as necessary.
     * @return null to force this page to reload
     */
    public WOComponent markAsComplete()
    {
        int numberNotified = 0;

        for (UserSubmissionPair p : studentSubPairs().allObjects())
        {
            if (p.userHasSubmission())
            {
                ScheduleSheetSubmission sub = p.submission();

                if (sub.result().status() == Status.UNFINISHED
                    || (sub.result().status() != Status.CHECK
                        && !sub.assignmentOffering().assignment()
                            .usesTAScore()))
                {
                    sub.result().setStatus(Status.CHECK);
                    if (applyLocalChanges())
                    {
                        numberNotified++;
//                        sub.emailNotificationToStudent(
//                            "has been updated by the course staff");
                    }
                }
            }
        }

//        return numberNotified;
        return null;
    }


    // TODO: still to implement:
    // pickOtherSubmission()


    //~ Instance/static fields ................................................

    private int sheetNo;
    private UserSubmissionPair pair;
    private Map<ScheduleSheetAssignmentOffering,
        ERXDisplayGroup<UserSubmissionPair>> studentSubPairs =
        new HashMap<ScheduleSheetAssignmentOffering,
            ERXDisplayGroup<UserSubmissionPair>>();
    private Map<ScheduleSheetAssignmentOffering, CumulativeStats> subStats;
    private Map<ScheduleSheetAssignmentOffering, CumulativeStats>
        timeInvestedStats;
    private Map<ScheduleSheetAssignmentOffering, CumulativeStats>
        estimatedRemainingStats;
    private Map<ScheduleSheetAssignmentOffering, CumulativeStats>
        componentStats;
    private List<NSMutableArray<UserSubmissionPair>> staffPairsBySheet =
        new ArrayList<NSMutableArray<UserSubmissionPair>>();
    private ScheduleSheetSubmission submission;
}
