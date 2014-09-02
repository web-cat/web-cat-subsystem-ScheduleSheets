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

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import er.extensions.appserver.ERXDisplayGroup;
import org.webcat.grader.GraderAssignmentsComponent;

//-------------------------------------------------------------------------
/**
 * Schedule feedback page.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class SheetFeedbackPage
    extends GraderAssignmentsComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Create a new EstimateEntrySheet object.
     * @param context
     */
    public SheetFeedbackPage(WOContext context)
    {
        super(context);
    }


    //~ KVC Attributes (must be public) .......................................

    public ScheduleSheetAssignmentOffering offering;
    public ScheduleSheetSubmission submission;
    public ScheduleSheet sheet;

    public ERXDisplayGroup<ComponentFeature> componentFeatures;
    public ERXDisplayGroup<SheetEntry> entries;
    public SheetEntry entry;
    public int cfIndex;
    public int aIndex;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    protected void beforeAppendToResponse(
        WOResponse response, WOContext context)
    {
        super.beforeAppendToResponse(response, context);
        if (submission == null)
        {
            submission = offering.mostRecentSubFor(user());
            sheet = submission.sheet();
            componentFeatures.setMasterObject(sheet);
        }
    }


    // ----------------------------------------------------------
    public int rowIndex()
    {
        return cfIndex + aIndex;
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


    //~ Instance/static fields ................................................

    private ComponentFeature componentFeature;
}