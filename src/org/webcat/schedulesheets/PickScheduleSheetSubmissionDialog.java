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

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSMutableDictionary;
import er.extensions.appserver.ERXDisplayGroup;
import org.webcat.core.WCComponentWithErrorMessages;
import org.webcat.grader.GraderComponent;

//-------------------------------------------------------------------------
/**
 * Allows the user to choose a different submission to view than the one that
 * is displayed on the current page.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class PickScheduleSheetSubmissionDialog
    extends GraderComponent
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new object.
     *
     * @param context The context to use
     */
    public PickScheduleSheetSubmissionDialog(WOContext context)
    {
        super(context);
    }


    //~ KVC Attributes (must be public) .......................................

    public String targetPageName;
    public ScheduleSheetSubmission currentSubmission;
    public ERXDisplayGroup<ScheduleSheetSubmission> submissions;
    public ScheduleSheetSubmission aSubmission;
    public int extraColumnCount;

    public NSMutableDictionary<String, Object> pass =
        new NSMutableDictionary<String, Object>();


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    @Override
    protected void beforeAppendToResponse(WOResponse response, WOContext context)
    {
        if (currentSubmission != null)
        {
            submissions.setObjectArray(currentSubmission.assignmentOffering()
                .allSubsFor(currentSubmission.user()));
            extraColumnCount = 0;
            ScheduleSheetAssignment a =
                currentSubmission.assignmentOffering().assignment();
            if (a.usesTAScore())
            {
                extraColumnCount++;
            }
            if (a.usesToolCheckScore())
            {
                extraColumnCount++;
            }
            if (a.usesBonusesOrPenalties())
            {
                extraColumnCount++;
            }
        }
        super.beforeAppendToResponse(response, context);
    }


    // ----------------------------------------------------------
    public WOComponent viewSubmission()
    {
        ScheduleSheetSubmission selectedSub = submissions.selectedObject();

         if (selectedSub == null)
        {
            selectedSub = currentSubmission;
        }

        if (selectedSub == null)
        {
            return errorMessageOnParent("Please choose a submission.");
        }
        else
        {
            WOComponent targetPage = pageWithName(targetPageName);
            targetPage.takeValueForKey(selectedSub, "submission");
            for (String key : pass.allKeys())
            {
                targetPage.takeValueForKey(pass.valueForKey(key), key);
            }
            return targetPage;
        }
    }


    // ----------------------------------------------------------
    private WCComponentWithErrorMessages errorMessageOnParent(String message)
    {
        WCComponentWithErrorMessages owner = null;
        WOComponent container = parent();
        while (container != null)
        {
            if (container instanceof WCComponentWithErrorMessages)
            {
                owner = (WCComponentWithErrorMessages)container;
            }
            container = container.parent();
        }
        if (owner != null && message != null)
        {
            owner.error(message);
        }
        return owner;
    }


    // ----------------------------------------------------------
    @Override
    public void takeValueForKey(Object value, String key)
    {
        if (key.startsWith("pass."))
        {
            pass.takeValueForKey(value, key.substring("pass.".length()));
        }
        else
        {
            super.takeValueForKey(value, key);
        }
    }


    // ----------------------------------------------------------
    @Override
    public Object valueForKey(String key)
    {
        if (key.startsWith("pass."))
        {
            return pass.valueForKey(key.substring("pass.".length()));
        }
        else
        {
            return super.valueForKey(key);
        }
    }


    //~ Instance/static fields ................................................
}