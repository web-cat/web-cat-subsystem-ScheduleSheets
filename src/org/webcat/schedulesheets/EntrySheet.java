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

import org.webcat.core.User;
import org.webcat.grader.GraderComponent;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;
import er.extensions.appserver.ERXDisplayGroup;

//-------------------------------------------------------------------------
/**
 * Data entry page for schedule estimates.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class EntrySheet
    extends GraderComponent
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    /**
     * Create a new object.
     * @param context
     */
    public EntrySheet(WOContext context)
    {
        super(context);
    }


    //~ KVC Attributes (must be public) .......................................

    public ScheduleSheetAssignmentOffering offering;
    public ScheduleSheetSubmission submission;
    public ScheduleSheet sheet;

    public ScheduleSheetSubmission previousSheet;
    public boolean inReportElapsedPhase;

    public ERXDisplayGroup<ComponentFeature> componentFeatures;
    public ERXDisplayGroup<SheetEntry> entries;
    public SheetEntry entry;
    public boolean requireCheck = true;
    public ERXDisplayGroup<SheetFeedbackItem> feedback;
    public SheetFeedbackItem msg;
    public int cfIndex;
    public int aIndex;

    public ERXDisplayGroup<User> students;
    public User student;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    protected void beforeAppendToResponse(
        WOResponse response, WOContext context)
    {
        if (submission == null && offering != null)
        {
            NSArray<ScheduleSheetSubmission> submissions =
                ScheduleSheetSubmission.objectsMatchingQualifier(
                    localContext(),
                    ScheduleSheetSubmission.assignmentOffering.is(offering)
                    .and(ScheduleSheetSubmission.user.is(user())),
                    ScheduleSheetSubmission.submitNumber.ascs());

            submission = ScheduleSheetSubmission.create(
                localContext(), false, false);
            submission.setSubmitNumber(submissions.count() + 1);
            submission.setUserRelationship(user());
            submission.setAssignmentOfferingRelationship(offering);
            submission.setSubmitTime(new NSTimestamp());

            ScheduleSheetSubmission mostRecent = null;
            for (ScheduleSheetAssignmentOffering o :
                offering.assignment().offerings())
            {
                if (o.courseOffering() == offering.courseOffering()
                    && o.order() <= offering.order())
                {
                    ScheduleSheetSubmission sub =
                        o.mostRecentSubFor(user());
                    if (sub != null)
                    {
                        if (mostRecent == null
                            || sub.submitTime().after(mostRecent.submitTime()))
                        {
                            mostRecent = sub;
                        }
                        if (o.order() < offering.order())
                        {
                            if (previousSheet == null
                                || sub.assignmentOffering().order()
                                > previousSheet.assignmentOffering().order())
                            {
                                previousSheet = sub;
                            }
                        }
                    }
                }
            }
            if (sheet != null && submission.sheet() == sheet)
            {
                sheet = null;
            }
            if (sheet == null)
            {
                sheet = ScheduleSheet.create(localContext());
                submission.setSheetRelationship(sheet);
                if (mostRecent != null)
                {
                    sheet.setupFrom(mostRecent.sheet(),
                        mostRecent.assignmentOffering() != offering);
                }
            }
            if (mostRecent != null)
            {
                submission.partnerWith(mostRecent.allPartners());
            }
            componentFeatures.setMasterObject(sheet);
            feedback.setMasterObject(sheet);
            inReportElapsedPhase = !offering.isFirstSheet();
        }
        if (submission != null)
        {
            // Have to calculate this separately, since objects aren't
            // committed to the db yet, so allPartners() and allUsers()
            // won't return meaningful results.
            // students.setMasterObject(submission);
            NSArray<ScheduleSheetSubmission> subs = sheet.submissions();
            NSMutableArray<User> users =
                new NSMutableArray<User>(subs.count());
            for (ScheduleSheetSubmission s : subs)
            {
                users.add(s.user());
            }
            students.setObjectArray(users);
        }
        super.beforeAppendToResponse(response, context);
    }


    // ----------------------------------------------------------
    public int rowIndex()
    {
        return cfIndex + aIndex;
    }


    // ----------------------------------------------------------
    public WOComponent add()
    {
        ComponentFeature cf = ComponentFeature.create(localContext());
        for (Byte activity : SheetEntry.ACTIVITIES)
        {
            SheetEntry e = SheetEntry.create(localContext(), false, false);
            e.setActivity(activity);
            cf.addToEntriesRelationship(e);
        }
        if (sheet.componentFeatures() == null)
        {
            cf.setOrder((byte)0);
        }
        else
        {
            cf.setOrder((byte)sheet.componentFeatures().count());
        }
        sheet.addToComponentFeaturesRelationship(cf);
        componentFeatures.qualifyDataSource();
        return null;
    }


    // ----------------------------------------------------------
    public boolean canSubmit()
    {
        return user().hasAdminPrivileges()
            || offering.courseOffering().isStaff(user())
            || offering.closedOnDate().after(new NSTimestamp());
    }


    // ----------------------------------------------------------
    public WOComponent check()
    {
        if (canSubmit())
        {
            submission.setSubmitTime(new NSTimestamp());
            for (ScheduleSheetSubmission sub :
                submission.partneredSubmissions())
            {
                sub.markBestSubmissionForGrading();
            }
            sheet.runAutomaticChecks(!inReportElapsedPhase);
            applyLocalChanges();
            requireCheck = false;
            feedback.qualifyDataSource();
            return this;
        }
        else
        {
            cancelLocalChanges();
        }
        return null;
    }


    // ----------------------------------------------------------
    public WOComponent next()
    {
        WOComponent result = check();
        if (result != null && !hasMessages())
        {
            if (inReportElapsedPhase && !offering.isLastSheet())
            {
                inReportElapsedPhase = false;
                requireCheck = true;
            }
            else
            {
                SheetFeedbackPage page = pageWithName(SheetFeedbackPage.class);
                page.submission = submission;
                result = page;
            }
        }
        return result;
    }


    // ----------------------------------------------------------
    public WOComponent back()
    {
        inReportElapsedPhase = true;
        return check();
    }


    // ----------------------------------------------------------
    @Override
    public WOComponent cancel()
    {
        super.cancel();
        return super.next();
    }


    // ----------------------------------------------------------
    public String title()
    {
        return "Sheet " + (1 + offering.order()) + ": "
            + offering.assignment().name();
    }


    // ----------------------------------------------------------
    public ComponentFeature componentFeature()
    {
        return componentFeature;
    }


    // ----------------------------------------------------------
    public void setComponentFeature(ComponentFeature cf)
    {
        componentFeature = cf;
        entries.setMasterObject(cf);
    }


    // ----------------------------------------------------------
    public boolean studentWorked()
    {
        return entry.workers().contains(student);
    }


    // ----------------------------------------------------------
    public void setStudentWorked(boolean value)
    {
        if (value)
        {
            entry.addToWorkersRelationship(student);
        }
        else
        {
            entry.removeFromWorkersRelationship(student);
        }
    }


    // ----------------------------------------------------------
    public boolean studentResponsible()
    {
        return entry.responsible().contains(student);
    }


    // ----------------------------------------------------------
    public void setStudentResponsible(boolean value)
    {
        if (value)
        {
            entry.addToResponsibleRelationship(student);
        }
        else
        {
            entry.removeFromResponsibleRelationship(student);
        }
    }


    // TODO: list
    // ----------
    // pickOtherSubmission() in GradeScheduleSubmissionsPage
    // add rule checks for partner assignments
    //     one partner too much
    // add rule checks for deadlines too soon
    // add partner imbalance checks
    // add three-vs-one component feature choice + edit
    // add min components to assignment config
    // add target effort to assignment config

    // add component feature move support
    // add component feature delete support
    // save and goto next on grading sheet feedback page

    // summary statistics/graphs/histograms?


    //~ Instance/static fields ................................................

    private ComponentFeature componentFeature;
}
