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

//-------------------------------------------------------------------------
/**
 * Represents the result produced by completing a schedule, together with
 * all feedback and scoring associated with it.  It includes a set of
 * SheetEntry objects representing the data corresponding to each
 * ComponentFeature in the schedule.  Analogous to the SubmissionResult
 * for a regular program assignment.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class ScheduleSheet
    extends _ScheduleSheet
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new ScheduleSheet object.
     */
    public ScheduleSheet()
    {
        super();
    }


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    public void setupFrom(ScheduleSheet other, boolean newAssignment)
    {
        for (ComponentFeature cf : other.componentFeatures())
        {
            ComponentFeature myCf = ComponentFeature.create(editingContext());
            myCf.setupFrom(cf, newAssignment);
            addToComponentFeaturesRelationship(myCf);
        }
    }


    // ----------------------------------------------------------
    public NSArray<SheetFeedbackItem> nontransientFeedback()
    {
        return SheetFeedbackItem.objectsMatchingQualifier(
            editingContext(),
            SheetFeedbackItem.sheetEntry.dot(SheetEntry.componentFeature)
                .dot(ComponentFeature.sheet).is(this)
            .and(SheetFeedbackItem.isTransient.isFalse()),
            SheetFeedbackItem.sheetEntry.dot(SheetEntry.componentFeature)
                .dot(ComponentFeature.order).asc()
            .then(SheetFeedbackItem.sheetEntry.dot(SheetEntry.activity).asc()));
    }


    // ----------------------------------------------------------
    public void moveNonTransientToTransient()
    {
        for (SheetFeedbackItem i : nontransientFeedback())
        {
            i.setIsTransient(true);
        }
    }


    //~ Instance/static fields ................................................

}
