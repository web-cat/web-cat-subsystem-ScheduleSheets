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

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;
import er.extensions.appserver.ERXDisplayGroup;
import org.apache.log4j.Logger;
import org.webcat.core.Status;
import org.webcat.grader.GraderComponent;
import org.webcat.ui.generators.JavascriptGenerator;

//-------------------------------------------------------------------------
/**
 * Schedule feedback page.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class SheetFeedbackPage
    extends GraderComponent
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
    public ERXDisplayGroup<SheetFeedbackItem> feedback;
    public SheetFeedbackItem msg;
    public int cfIndex;
    public int aIndex;

    public boolean edit = false;

    public NSArray<Byte> formats = ScheduleSheet.FORMATS;
    public byte aFormat;

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
        }
        else if (offering == null)
        {
            offering = submission.assignmentOffering();
        }
        sheet = submission.sheet();
        priorComments = sheet.comments();
        componentFeatures.setMasterObject(sheet);
        feedback.setMasterObject(sheet);
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


    // ----------------------------------------------------------
    public boolean studentCanSeeResults()
    {
        return sheet == null
            || sheet.status() == org.webcat.core.Status.CHECK;
    }


    // ----------------------------------------------------------
    public WOActionResults pickOtherSubmission()
    {
//        saveGrading();

        JavascriptGenerator js = new JavascriptGenerator();
        js.dijit("pickSubmissionDialog").call("show");
        return js;
    }


    // ----------------------------------------------------------
    public String formatLabel()
    {
        return ScheduleSheet.FORMAT_STRINGS.objectAtIndex(aFormat);
    }


    // ----------------------------------------------------------
    public boolean gradingDone()
    {
        return sheet.status() == Status.CHECK;
    }


    // ----------------------------------------------------------
    public void setGradingDone(boolean done)
    {
        if (done)
        {
            sheet.setStatus(Status.CHECK);
//            for (ScheduleSheetSubmission sub : sheet.submissions())
//            {
//                sub.emailNotificationToStudent(
//                    "has been updated by the course staff");
//            }
        }
    }


    // ----------------------------------------------------------
    @Override
    public WOComponent next()
    {
        if (edit)
        {
            if (!applyLocalChanges())
            {
                return null;
            }
        }
        return super.next();
    }


    // ----------------------------------------------------------
    public void saveGrading()
    {
        sheet.addCommentByLineFor(user(), priorComments);
    }


    // ----------------------------------------------------------
    public boolean applyLocalChanges()
    {
        NSTimestamp now = new NSTimestamp();
        saveGrading();
        if (sheet.status() == Status.TO_DO)
        {
            if (sheet.taScoreRaw() != null
                && sheet.taScore() != submission
                    .assignmentOffering().assignment()
                    .submissionProfile().taPoints())
            {
                sheet.setStatus(Status.UNFINISHED);
            }
        }
        if (sheet.changedProperties().size() > 0)
        {
            sheet.setLastUpdated(now);
        }
        log.debug("Before commiting, result = " + sheet.snapshot());
        return super.applyLocalChanges();
    }


    // ----------------------------------------------------------
    public WOComponent regrade()
    {
        sheet.runAutomaticChecks();
        applyLocalChanges();
        return null;
    }


    // ----------------------------------------------------------
    @Override
    public WOComponent cancel()
    {
        WOComponent result = super.cancel();
        if (nextPage != null)
        {
            result = nextPage;
        }
        return result;
    }


    //~ Instance/static fields ................................................

    private ComponentFeature componentFeature;
    private String priorComments;
    static Logger log = Logger.getLogger(SheetFeedbackPage.class);
}