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

import java.text.DecimalFormat;
import java.text.Format;
import java.util.TimeZone;
import org.webcat.core.Course;
import org.webcat.core.CourseOffering;
import org.webcat.grader.Assignment;
import org.webcat.grader.GraderAssignmentsComponent;
import org.webcat.grader.SubmissionProfile;
import org.webcat.ui.generators.JavascriptGenerator;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;
import er.extensions.appserver.ERXDisplayGroup;

//-------------------------------------------------------------------------
/**
 * Page to edit the properties of a schedule sheet assignment and its
 * offerings.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class EditScheduleSheetAssignmentPage
    extends GraderAssignmentsComponent
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new object.
     *
     * @param context The context to use
     */
    public EditScheduleSheetAssignmentPage(WOContext context)
    {
        super(context);
    }


    //~ KVC Attributes (must be public) .......................................

    public ScheduleSheetAssignment assignment;
    public ScheduleSheetAssignmentOffering offering;
    public NSMutableArray<Integer> sheetNumbers;
    public ERXDisplayGroup<ScheduleSheetAssignmentOffering> offerings;
    public int offeringIndex;

    public Long availableTimeDelta; // null for no limit
    public Long deadTimeDelta;      // null for no late submissions
    public Long earlyBonusUnitTime;
    public Long latePenaltyUnitTime;
    public SubmissionProfile.TimeUnit unit;
    public SubmissionProfile.TimeUnit availableTimeDeltaUnit;
    public SubmissionProfile.TimeUnit deadTimeDeltaUnit;
    public SubmissionProfile.TimeUnit earlyUnitTimeUnit;
    public SubmissionProfile.TimeUnit lateUnitTimeUnit;

    public final Format doubleFormatter = new DecimalFormat("0.######");


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    @Override
    protected void beforeAppendToResponse(
        WOResponse response, WOContext context)
    {
        if (assignment == null)
        {
            Assignment progrAssgn = prefs().assignment();
            if (progrAssgn == null)
            {
                progrAssgn = prefs().assignmentOffering().assignment();
            }

            if (coreSelections().course() == null)
            {
                CourseOffering courseOffering =
                    coreSelections().courseOffering();
                NSArray<ScheduleSheetAssignmentOffering> candidates =
                    ScheduleSheetAssignmentOffering.objectsMatchingQualifier(
                        localContext(),
                        ScheduleSheetAssignmentOffering.assignment
                        .dot(ScheduleSheetAssignment.assignment).is(progrAssgn)
                        .and(ScheduleSheetAssignmentOffering.courseOffering
                            .is(courseOffering)),
                    ScheduleSheetAssignmentOffering.order.ascs());
                if (candidates.count() > 0)
                {
                    assignment = candidates.get(0).assignment();
                }
            }
            else
            {
                Course course = coreSelections().course();
                NSArray<ScheduleSheetAssignmentOffering> candidates =
                    ScheduleSheetAssignmentOffering.objectsMatchingQualifier(
                        localContext(),
                        ScheduleSheetAssignmentOffering.assignment
                        .dot(ScheduleSheetAssignment.assignment).is(progrAssgn)
                        .and(ScheduleSheetAssignmentOffering.courseOffering
                            .dot(CourseOffering.course).is(course)),
                    ScheduleSheetAssignmentOffering.order.ascs());
                if (candidates.count() > 0)
                {
                    assignment = candidates.get(0).assignment();
                }
            }
        }

        if (assignment != null && sheetNumbers == null)
        {
            offerings.setMasterObject(assignment);
            int max = 0;
            for (ScheduleSheetAssignmentOffering o : assignment.offerings())
            {
                max = Math.max(max, o.order());
            }
            sheetNumbers = new NSMutableArray<Integer>(max);
            for (int i = 0; i <= max; i++)
            {
                sheetNumbers.add(i);
            }

            initializeTimeFields();
        }
        super.beforeAppendToResponse(response, context);
    }


    // ----------------------------------------------------------
    public boolean finishEnabled()
    {
        return applyEnabled();
    }


    // ----------------------------------------------------------
    public TimeZone timeZone()
    {
        return TimeZone.getTimeZone(user().timeZoneName());
    }


    // ----------------------------------------------------------
    public Integer sheetNumber()
    {
        return sheetNumber;
    }


    // ----------------------------------------------------------
    public void setSheetNumber(Integer newValue)
    {
        if (newValue != null)
        {
            if (sheetNumber == null
                || sheetNumber.intValue() != newValue.intValue())
            {
                offerings.queryMatch().takeValueForKey(
                    newValue, ScheduleSheetAssignmentOffering.ORDER_KEY);
                offerings.qualifyDisplayGroup();
                areDueDatesLocked = areDueDatesSame();
            }
        }
        else
        {
            offerings.queryMatch().remove(
                ScheduleSheetAssignmentOffering.ORDER_KEY);
            offerings.qualifyDisplayGroup();
            areDueDatesLocked = areDueDatesSame();
        }
        sheetNumber = newValue;
    }


    // ----------------------------------------------------------
    public NSTimestamp dueDate()
    {
        return offering.dueDate();
    }


    // ----------------------------------------------------------
    public WOActionResults togglePublished()
    {
        boolean value = !offering.publish();
        if (areDueDatesLocked)
        {
            for (ScheduleSheetAssignmentOffering o :
                offerings.displayedObjects())
            {
                o.setPublish(value);
            }
        }
        else
        {
            offering.setPublish(value);
        }
        applyLocalChanges();
        return new JavascriptGenerator()
            .refresh("offerings" + sheetNumber, "error-panel");
    }


    // ----------------------------------------------------------
    public void setDueDate(NSTimestamp value)
    {
        if (areDueDatesLocked)
        {
            for (ScheduleSheetAssignmentOffering o :
                offerings.displayedObjects())
            {
                o.setDueDate(value);
            }
        }
        else
        {
            offering.setDueDate(value);
        }
    }


    // ----------------------------------------------------------
    public boolean shouldShowDueDatePicker()
    {
        return !areDueDatesLocked
            || (offerings.displayedObjects().count() > 0
                && offering.equals(
                        offerings.displayedObjects().objectAtIndex(0)));
    }


    // ----------------------------------------------------------
    public boolean areDueDatesLocked()
    {
        return areDueDatesLocked;
    }


    // ----------------------------------------------------------
    public boolean areDueDatesSame()
    {
        NSTimestamp exemplar = null;

        for (ScheduleSheetAssignmentOffering o :
            offerings.displayedObjects())
        {
            if (exemplar == null)
            {
                exemplar = o.dueDate();
            }
            else
            {
                if (o.dueDate() == null
                        || !exemplar.equals(o.dueDate()))
                {
                    return false;
                }
            }
        }

        return true;
    }


    // ----------------------------------------------------------
    public WOActionResults lockDueDates()
    {
        if (saveAndCanProceed())
        {
            areDueDatesLocked = true;

            NSTimestamp exemplar = null;

            for (ScheduleSheetAssignmentOffering o :
                offerings.displayedObjects())
            {
                if (exemplar == null)
                {
                    exemplar = o.dueDate();
                }
                else
                {
                    o.setDueDate(exemplar);
                }
            }

            applyLocalChanges();
        }

        return new JavascriptGenerator()
            .refresh("offerings" + sheetNumber, "error-panel");
    }


    // ----------------------------------------------------------
    public WOActionResults unlockDueDates()
    {
        if (saveAndCanProceed())
        {
            areDueDatesLocked = false;
        }

        return new JavascriptGenerator()
            .refresh("offerings" + sheetNumber, "error-panel");
    }


    // ----------------------------------------------------------
    public boolean saveAndCanProceed()
    {
        return !hasMessages() && applyLocalChanges();
    }


    // ----------------------------------------------------------
    public String inlineJavaScript()
    {
        return INLINE_JAVASCRIPT;
    }


    // ----------------------------------------------------------
    public WOComponent cancel()
    {
        cancelLocalChanges();
        return null;
    }


    // ----------------------------------------------------------
    @Override
    public WOComponent apply()
    {
        applyLocalChanges();
        return super.apply();
    }


    // ----------------------------------------------------------
    public void initializeTimeFields()
    {
        SubmissionProfile submissionProfile = assignment.submissionProfile();

        // First, fill availableTimeDelta data members
        if (submissionProfile.availableTimeDeltaRaw() == null)
        {
            availableTimeDelta     = null;
            availableTimeDeltaUnit = SubmissionProfile.timeUnits[2];  // Days
        }
        else
        {
            long storedAvailableTimeDelta =
                submissionProfile.availableTimeDelta();
            for (int i = SubmissionProfile.timeUnits.length - 1; i >= 0; i--)
            {
                availableTimeDeltaUnit = SubmissionProfile.timeUnits[i];
                if (availableTimeDeltaUnit.isUnitFor(storedAvailableTimeDelta)
                    || i == 0)
                {
                    availableTimeDelta = availableTimeDeltaUnit
                        .unitsFromRaw(storedAvailableTimeDelta);
                    break;
                }
            }
        }
        // Next, fill deadTimeDelta data members
        if (submissionProfile.deadTimeDeltaRaw() == null)
        {
            deadTimeDelta     = 0L;
            deadTimeDeltaUnit = SubmissionProfile.timeUnits[2];  // Days
        }
        else
        {
            long storedDeadTimeDelta = submissionProfile.deadTimeDelta();
            for (int i = SubmissionProfile.timeUnits.length - 1; i >= 0; i--)
            {
                deadTimeDeltaUnit = SubmissionProfile.timeUnits[i];
                if (deadTimeDeltaUnit.isUnitFor(storedDeadTimeDelta)
                    || i == 0)
                {
                    deadTimeDelta = deadTimeDeltaUnit
                        .unitsFromRaw(storedDeadTimeDelta);
                    break;
                }
            }
        }
        // Next, fill earlyBonusTimeUnit data members
        if (submissionProfile.earlyBonusUnitTimeRaw() == null)
        {
            earlyBonusUnitTime = 0L;
            earlyUnitTimeUnit  = SubmissionProfile.timeUnits[2];  // Days
        }
        else
        {
            long storedEarlyBonusUnitTime =
                submissionProfile.earlyBonusUnitTime();
            for (int i = SubmissionProfile.timeUnits.length - 1; i >= 0; i--)
            {
                earlyUnitTimeUnit = SubmissionProfile.timeUnits[i];
                if (earlyUnitTimeUnit.isUnitFor(storedEarlyBonusUnitTime)
                    || i == 0)
                {
                    earlyBonusUnitTime = earlyUnitTimeUnit
                        .unitsFromRaw(storedEarlyBonusUnitTime);
                    break;
                }
            }
        }
        // Finally, fill latePenaltyTimeUnit data members
        if (submissionProfile.latePenaltyUnitTimeRaw() == null)
        {
            latePenaltyUnitTime = 0L;
            lateUnitTimeUnit    = SubmissionProfile.timeUnits[2];  // Days
        }
        else
        {
            long storedLatePenaltyUnitTime =
                submissionProfile.latePenaltyUnitTime();
            for (int i = SubmissionProfile.timeUnits.length - 1; i >= 0; i--)
            {
                lateUnitTimeUnit = SubmissionProfile.timeUnits[i];
                if (lateUnitTimeUnit.isUnitFor(storedLatePenaltyUnitTime)
                    || i == 0)
                {
                    latePenaltyUnitTime = lateUnitTimeUnit
                        .unitsFromRaw(storedLatePenaltyUnitTime);
                    break;
                }
            }
        }
    }


    // ----------------------------------------------------------
    public void saveTimeFields()
    {
        assignment.submissionProfile().setAvailableTimeDeltaRaw(
            availableTimeDeltaUnit.rawFromUnits(availableTimeDelta));
        assignment.submissionProfile().setDeadTimeDeltaRaw(
            deadTimeDeltaUnit.rawFromUnits(deadTimeDelta));
        assignment.submissionProfile().setEarlyBonusUnitTimeRaw(
            earlyUnitTimeUnit.rawFromUnits(earlyBonusUnitTime));
        assignment.submissionProfile().setLatePenaltyUnitTimeRaw(
            lateUnitTimeUnit.rawFromUnits(latePenaltyUnitTime));
    }


    // ----------------------------------------------------------
    public boolean applyLocalChanges()
    {
        saveTimeFields();
        SubmissionProfile master = assignment.submissionProfile();
        for (ScheduleSheetAssignmentOffering o : assignment.offerings())
        {
            SubmissionProfile override = o.overrideProfile();
            if (override != null)
            {
                double availablePoints = o.availablePoints();

                // Copy this profile over top of overrides
                for (String key :
                    assignment.submissionProfile().allPropertyKeys())
                {
                    override.takeValueForKey(master.valueForKey(key), key);
                }

                if (availablePoints != master.availablePoints())
                {
                    override.setAvailablePoints(availablePoints);
                    override.setToolPoints(master.toolPoints() *
                        availablePoints / master.availablePoints());
                    override.setTaPoints(
                        availablePoints - override.toolPoints());
                }
            }
        }
        boolean result = super.applyLocalChanges();
        return result;
    }


    // ----------------------------------------------------------
    public Double availablePointsOverride()
    {
        if (offering == null || offering.overrideProfile() == null)
        {
            return null;
        }
        else
        {
            return offering.availablePoints();
        }
    }


    // ----------------------------------------------------------
    public void setAvailablePointsOverride(Double value)
    {
        if (offering == null)
        {
            return;
        }

        if (value == null
            || value == assignment.submissionProfile().availablePoints())
        {
            if (offering.overrideProfile() != null)
            {
                offering.overrideProfile().delete();
                offering.setOverrideProfileRelationship(null);
            }
        }
        else
        {
            offering.setAvailablePoints(value);
        }
    }


    // ----------------------------------------------------------
    @Override
    public void takeValuesFromRequest(WORequest request, WOContext context)
    {
        super.takeValuesFromRequest(request, context);
        SubmissionProfile profile = assignment.submissionProfile();
        if (profile != null)
        {
            profile.setAvailablePoints(
                + profile.taPoints() + profile.toolPoints());
        }
    }


    //~ Instance/static fields ................................................

    private static final String INLINE_JAVASCRIPT =
        "<script type=\"text/javascript\">\n"
        + "<!-- Begin\n"
        + "function startCalc(){\n"
        + "  interval = setInterval(\"calc()\",1);\n"
        + "}\n"
        + "function calc(){\n"
        + "  document.assignmentForm.total.value =\n"
        + "      ( document.assignmentForm.taPoints.value * 1 )\n"
        + "      + ( document.assignmentForm.toolPoints.value * 1 );\n"
        + "}\n"
        + "function stopCalc(){\n"
        + "  clearInterval(interval);\n"
        + "}\n"
        + "// End -->\n"
        + "</script>";

    private Integer sheetNumber;
    private boolean areDueDatesLocked;
}