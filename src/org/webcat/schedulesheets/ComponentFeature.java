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

import com.webobjects.foundation.NSTimestamp;

//-------------------------------------------------------------------------
/**
 * Represents one work unit (component, feature, etc.) in a schedule.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class ComponentFeature
    extends _ComponentFeature
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new ComponentFeature object.
     */
    public ComponentFeature()
    {
        super();
    }


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    public void setupFrom(ComponentFeature other, boolean newAssignment)
    {
        setName(other.name());
        setOrder(other.order());
        for (SheetEntry entry : other.entries())
        {
            SheetEntry myEntry = SheetEntry.create(editingContext(),
                entry.isComplete(), entry.previousWasComplete());
            addToEntriesRelationship(myEntry);
            myEntry.setupFrom(entry, newAssignment);
        }
    }


    // ----------------------------------------------------------
    public SheetEntry entryFor(byte activity)
    {
        for (SheetEntry entry : entries())
        {
            if (entry.activity() == activity)
            {
                return entry;
            }
        }
        return null;
    }


    // ----------------------------------------------------------
    public boolean isOverdue()
    {
        for (SheetEntry entry : entries())
        {
            if (entry.isOverdue())
            {
                return true;
            }
        }
        return false;
    }


    // ----------------------------------------------------------
    public double newEstimatedRemaining()
    {
        double result = 0.0;
        for (SheetEntry entry : entries())
        {
            result += entry.newEstimatedRemaining();
        }
        return result;
    }


    // ----------------------------------------------------------
    public double previousEstimatedRemaining()
    {
        double result = 0.0;
        for (SheetEntry entry : entries())
        {
            result += entry.previousEstimatedRemaining();
        }
        return result;
    }


    // ----------------------------------------------------------
    public double newEstimatedTotal()
    {
        double result = 0.0;
        for (SheetEntry entry : entries())
        {
            result += entry.newEstimatedTotal();
        }
        return result;
    }


    // ----------------------------------------------------------
    public double previousEstimatedTotal()
    {
        double result = 0.0;
        for (SheetEntry entry : entries())
        {
            result += entry.previousEstimatedTotal();
        }
        return result;
    }


    // ----------------------------------------------------------
    public double newTimeInvested()
    {
        double result = 0.0;
        for (SheetEntry entry : entries())
        {
            result += entry.newTimeInvested();
        }
        return result;
    }


    // ----------------------------------------------------------
    public double newTimeInvestedTotal()
    {
        double result = 0.0;
        for (SheetEntry entry : entries())
        {
            result += entry.newTimeInvestedTotal();
        }
        return result;
    }


    // ----------------------------------------------------------
    public double previousTimeInvestedTotal()
    {
        double result = 0.0;
        for (SheetEntry entry : entries())
        {
            result += entry.previousTimeInvestedTotal();
        }
        return result;
    }


    // ----------------------------------------------------------
    public boolean isComplete()
    {
        for (SheetEntry entry : entries())
        {
            if (!entry.isComplete())
            {
                return false;
            }
        }
        return true;
    }


    // ----------------------------------------------------------
    public boolean previousWasComplete()
    {
        for (SheetEntry entry : entries())
        {
            if (!entry.previousWasComplete())
            {
                return false;
            }
        }
        return true;
    }


    // ----------------------------------------------------------
    public NSTimestamp newDeadline()
    {
        NSTimestamp result = null;
        for (SheetEntry entry : entries())
        {
            if (result == null
                || (entry.newDeadline() != null
                    && entry.newDeadline().after(result)))
            {
                result = entry.newDeadline();
            }
        }
        return result;
    }


    // ----------------------------------------------------------
    public NSTimestamp previousDeadline()
    {
        NSTimestamp result = null;
        for (SheetEntry entry : entries())
        {
            if (result == null
                || (entry.previousDeadline() != null
                    && entry.previousDeadline().after(result)))
            {
                result = entry.previousDeadline();
            }
        }
        return result;
    }


    // ----------------------------------------------------------
    public boolean isEmpty()
    {
        if (name() != null && !name().isEmpty())
        {
            return false;
        }
        for (SheetEntry entry : entries())
        {
            if (!entry.isEmpty())
            {
                return false;
            }
        }
        return true;
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
    public void runAutomaticChecks(int round, boolean checkEstimatePhase)
    {
        for (SheetEntry entry : entries())
        {
            entry.runAutomaticChecks(round, checkEstimatePhase);
        }

        // Component-level checks
        SheetEntry design = entryFor(SheetEntry.DESIGN);
        SheetEntry code = entryFor(SheetEntry.CODE);
        SheetEntry test = entryFor(SheetEntry.TEST);

        // Component-level checks for entry phase

        // Component-level checks for estimate phase
        if (checkEstimatePhase)
        {
            // No design time estimated
            if (!code.isComplete()
                && code.newEstimatedRemaining() > 4
                && design.newTimeInvestedTotal() == 0
                && design.newEstimatedRemaining() == 0)
            {
                SheetFeedbackItem.create(editingContext(), round,
                    SheetFeedbackItem.CF_INSUFFICIENT_DESIGN, this);
            }

            // No test time estimated
            if (!code.isComplete()
                && code.newEstimatedRemaining() > 0)
            {
                if (test.newEstimatedRemaining() == 0)
                {
                    SheetFeedbackItem.create(editingContext(), round,
                        SheetFeedbackItem.CF_INSUFFICIENT_TEST, this);
                }
                else if (test.newEstimatedRemaining()
                    < code.newEstimatedRemaining() / 5)
                {
                    SheetFeedbackItem.create(editingContext(), round,
                        SheetFeedbackItem.CF_INSUFFICIENT_TEST2, this);
                }
            }

            // Design deadline falls after code deadline
            if (!design.isComplete()
                && design.newEstimatedRemaining() > 0
                && !code.isComplete()
                && code.newEstimatedRemaining() > 0
                && code.newDeadline() != null
                && design.newDeadline() != null
                && design.newDeadline().after(code.newDeadline()))
            {
                SheetFeedbackItem.create(editingContext(), round,
                    SheetFeedbackItem.CF_DESIGN_AFTER_CODE, this);
            }

            // Code deadline falls after test deadline
            if (!code.isComplete()
                && code.newEstimatedRemaining() > 0
                && !test.isComplete()
                && test.newEstimatedRemaining() > 0
                && test.newDeadline() != null
                && code.newDeadline() != null
                && code.newDeadline().after(test.newDeadline()))
            {
                SheetFeedbackItem.create(editingContext(), round,
                    SheetFeedbackItem.CF_CODE_AFTER_TEST, this);
            }

            double hrsRemaining = sheet().calculateHoursAvailableBefore(
                currentDeadline());
            if (hrsRemaining / 2 < newEstimatedRemaining())
            {
                SheetFeedbackItem.create(editingContext(), round,
                    SheetFeedbackItem.CF_NOT_ENOUGH_TIME, this);
            }
            else if (hrsRemaining / 4 < newEstimatedRemaining())
            {
                SheetFeedbackItem.create(editingContext(), round,
                    SheetFeedbackItem.CF_TIME_TOO_TIGHT, this);
            }
        }
    }

    //~ Instance/static fields ................................................
}
