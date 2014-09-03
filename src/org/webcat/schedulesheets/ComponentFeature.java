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
import er.extensions.foundation.ERXArrayUtilities;

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
            SheetEntry myEntry = SheetEntry.create(editingContext(), false);
            addToEntriesRelationship(myEntry);
            myEntry.setupFrom(entry, newAssignment);
        }
    }


    // ----------------------------------------------------------
    public SheetEntry entryForActivity(byte activity)
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
    public double estimatedRemaining()
    {
        double result = 0.0;
        for (SheetEntry entry : entries())
        {
            result += entry.estimatedRemaining();
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


    //~ Instance/static fields ................................................
}
