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
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;

//-------------------------------------------------------------------------
/**
 * Represents a single line of data from a submitted schedule, about
 * one type of activity on a single ComponentFeature.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class SheetEntry
    extends _SheetEntry
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new SheetEntry object.
     */
    public SheetEntry()
    {
        super();
    }


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    public void setupFrom(SheetEntry other, boolean newAssignment)
    {
        setActivity(other.activity());
        if (newAssignment)
        {
            if (other.previousEstimatedTotalRaw() != null)
            {
                setPreviousEstimatedTotal(other.newEstimatedTotal());
            }
            else
            {
                setPreviousEstimatedTotalRaw(other.newEstimatedRemainingRaw());
            }
            if (other.newTimeInvestedRaw() != null)
            {
                setPreviousTimeInvestedTotal(other.newTimeInvestedTotal());
            }
            else
            {
                setPreviousTimeInvestedTotalRaw(
                    other.previousTimeInvestedTotalRaw());
            }
            setPreviousDeadline(other.newDeadline());
            setPreviousEstimatedRemainingRaw(other.newEstimatedRemainingRaw());
            for (User u : other.responsible())
            {
                addToWorkersRelationship(u);
            }
        }
        else
        {
            // resubmission
            setPreviousEstimatedRemainingRaw(
                other.previousEstimatedRemainingRaw());
            setPreviousEstimatedTotalRaw(other.previousEstimatedTotalRaw());
            setPreviousDeadline(other.previousDeadline());
            setPreviousTimeInvestedTotalRaw(
                other.previousTimeInvestedTotalRaw());

            setNewTimeInvestedRaw(other.newTimeInvestedRaw());
            setNewEstimatedRemainingRaw(other.newEstimatedRemainingRaw());
            for (User u : other.workers())
            {
                addToWorkersRelationship(u);
            }
            for (User u : other.responsible())
            {
                addToResponsibleRelationship(u);
            }
        }
        setNewDeadline(other.newDeadline());
        setIsCompleteRaw(other.isCompleteRaw());
        setPreviousWasCompleteRaw(other.previousWasCompleteRaw());
    }


    // ----------------------------------------------------------
    public NSArray<Byte> activities()
    {
        return ACTIVITIES;
    }


    // ----------------------------------------------------------
    public NSArray<String> activityNames()
    {
        return ACTIVITY_NAMES;
    }


    // ----------------------------------------------------------
    public String activityName()
    {
        return ACTIVITY_NAMES.get(activity());
    }


    // ----------------------------------------------------------
    public NSTimestamp currentDeadline()
    {
        NSTimestamp deadline = newDeadline();
        if (deadline == null)
        {
            deadline = previousDeadline();
        }
        return deadline;
    }


    // ----------------------------------------------------------
    private long msAfterDeadline()
    {
        NSTimestamp deadline = currentDeadline();
        if (deadline == null)
        {
            return -DAY - 1L;
        }
        else
        {
            return componentFeature().sheet().submission().submitTime()
                .getTime() - deadline.getTime();
        }
    }


    // ----------------------------------------------------------
    public boolean isOverdue()
    {
        return !isComplete() && msAfterDeadline() > DAY;
    }


    // ----------------------------------------------------------
    public boolean isDueToday()
    {
        long ms = msAfterDeadline();
        return !isComplete() && ms >= 0 && ms < DAY;
    }


    // ----------------------------------------------------------
    public boolean isDueTomorrow()
    {
        long ms = msAfterDeadline();
        return !isComplete() && ms < 0 && ms >= -DAY;
    }


    // ----------------------------------------------------------
    public double newEstimatedTotal()
    {
        return previousEstimatedTotal() + newEstimatedRemaining();
    }


    // ----------------------------------------------------------
    public double newTimeInvestedTotal()
    {
        return previousTimeInvestedTotal() + newTimeInvested();
    }


    // ----------------------------------------------------------
    public boolean isEmpty()
    {
        return !isComplete()
            && newEstimatedRemaining() == 0
            && newDeadline() == null
            && newTimeInvested() == 0
            && previousEstimatedRemaining() == 0
            && previousDeadline() == null
            && previousTimeInvestedTotal() == 0;
    }


    // ----------------------------------------------------------
    public void runAutomaticChecks(int round, boolean checkEstimatePhase)
    {
        // Entry-level checks for entry phase
        boolean isLast = componentFeature().sheet().isLastSheet();

        // An entry is overdue
        if (isLast && !isComplete())
        {
            SheetFeedbackItem.create(editingContext(), round,
                SheetFeedbackItem.ENTRY_NOT_COMPLETE, this);
        }

        if (!isLast && isOverdue())
        {
            SheetFeedbackItem.create(editingContext(), round,
                SheetFeedbackItem.ENTRY_IS_OVERDUE, this);
        }
        else if (isDueToday())
        {
            SheetFeedbackItem.create(editingContext(), round,
                SheetFeedbackItem.ENTRY_IS_DUE_TODAY, this);
        }
        else if (isDueTomorrow())
        {
            SheetFeedbackItem.create(editingContext(), round,
                SheetFeedbackItem.ENTRY_IS_DUE_TOMORROW, this);
        }

        if (newTimeInvested() > 0
            && workers().count() == 0
            && componentFeature().sheet().submissions().count() > 1)
        {
            SheetFeedbackItem.create(editingContext(), round,
                SheetFeedbackItem.ENTRY_NO_WORKERS, this);
        }

        // Entry-level checks for estimate phase
        if (checkEstimatePhase)
        {
            // No estimated time to complete an entry
            if (!isComplete()
                && (previousDeadline() != null
                    || previousEstimatedRemaining() > 0
                    || previousTimeInvestedTotal() > 0)
                    && newEstimatedRemainingRaw() == null)
            {
                SheetFeedbackItem.create(editingContext(), round,
                    SheetFeedbackItem.ENTRY_MISSING_ESTIMATED_REMAINING, this);
            }

            if (!isComplete()
                && newEstimatedRemaining() > 0
                && newDeadline() == null)
            {
                SheetFeedbackItem.create(editingContext(), round,
                    SheetFeedbackItem.ENTRY_MISSING_NEW_DEADLINE, this);
            }

            if (newEstimatedRemaining() > 0
                && responsible().count() == 0
                && componentFeature().sheet().submissions().count() > 1)
            {
                SheetFeedbackItem.create(editingContext(), round,
                    SheetFeedbackItem.ENTRY_NO_RESPONSIBLES, this);
            }

            double hrsRemaining = componentFeature().sheet()
                .calculateHoursAvailableBefore(currentDeadline());
            int students = componentFeature().sheet().submissions().count();
            if (hrsRemaining / 2 * students < newEstimatedRemaining())
            {
                SheetFeedbackItem.create(editingContext(), round,
                    SheetFeedbackItem.ENTRY_NOT_ENOUGH_TIME, this);
            }
            else if (hrsRemaining / 4 * students < newEstimatedRemaining())
            {
                SheetFeedbackItem.create(editingContext(), round,
                    SheetFeedbackItem.ENTRY_TIME_TOO_TIGHT, this);
            }
        }
    }


    //~ Instance/static fields ................................................

    public static final byte DESIGN = 0;
    public static final byte CODE   = 1;
    public static final byte TEST   = 2;
    private static final long DAY = 1000 * 60 * 60 * 24;

    public static final NSArray<Byte> ACTIVITIES = new NSArray<Byte>(
        new Byte[] {
            new Byte(DESIGN),
            new Byte(CODE),
            new Byte(TEST)
        });

    public static final NSArray<String> ACTIVITY_NAMES = new NSArray<String>(
        new String[] {
            "Design",
            "Code",
            "Test"
        });
}
