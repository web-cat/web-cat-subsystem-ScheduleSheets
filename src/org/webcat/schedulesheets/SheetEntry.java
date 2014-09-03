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

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;
import er.extensions.foundation.ERXArrayUtilities;

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
        setIsComplete(other.isComplete());
        if (newAssignment)
        {
            setPreviousEstimatedTotal(other.previousEstimatedTotal()
                + other.estimatedRemaining());
            setPreviousDeadline(other.newDeadline());
            setNewDeadline(other.newDeadline());
        }
        else
        {
            // resubmission
            setPreviousEstimatedTotal(other.previousEstimatedTotal());
            setPreviousDeadline(other.previousDeadline());
            setTimeInvested(other.timeInvested());
            setEstimatedRemaining(other.estimatedRemaining());
            setNewDeadline(other.newDeadline());
        }
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
    @SuppressWarnings("unchecked")
    public NSArray<SheetFeedbackItem> nontransientFeedback()
    {
        return ERXArrayUtilities.filteredArrayWithQualifierEvaluation(
            feedbackItems(),
            SheetFeedbackItem.isTransient.isFalse());
    }


    // ----------------------------------------------------------
    public void moveNonTransientToTransient()
    {
        for (SheetFeedbackItem i : nontransientFeedback())
        {
            i.setIsTransient(true);
        }
    }


    // ----------------------------------------------------------
    public boolean isOverdue()
    {
        NSTimestamp submitTime =
            componentFeature().sheet().submission().submitTime();
        if (newDeadline() != null)
        {
            return submitTime.after(newDeadline());
        }
        else if (previousDeadline() != null)
        {
            return submitTime.after(previousDeadline());
        }
        else
        {
            return false;
        }
    }


    // ----------------------------------------------------------
    public double newEstimatedTotal()
    {
        return previousEstimatedTotal() + estimatedRemaining();
    }


    //~ Instance/static fields ................................................

    public static final byte DESIGN = 0;
    public static final byte CODE   = 1;
    public static final byte TEST   = 2;

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
