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

import org.webcat.core.WCProperties;
import com.webobjects.eocontrol.EOEditingContext;

// -------------------------------------------------------------------------
/**
 * Represents an automatically generated feedback message for a
 * time schedule.
 *
 * @author Stephen Edwards
 * @author  Last changed by: $Author$
 * @version $Revision$, $Date$
 */
public class SheetFeedbackItem
    extends _SheetFeedbackItem
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new SheetFeedbackItem object.
     */
    public SheetFeedbackItem()
    {
        super();
    }


    //~ Constants .............................................................

    public static final byte INFORMATION = org.webcat.core.Status.INFORMATION;
    public static final byte WARNING = org.webcat.core.Status.WARNING;
    public static final byte ERROR = org.webcat.core.Status.ERROR;

    public static final int OK = 0;
    public static final int ENTRY_MISSING_ESTIMATED_REMAINING = 1;
    public static final int ENTRY_MISSING_NEW_DEADLINE = 2;
    public static final int ENTRY_IS_OVERDUE = 3;

    public static final int CF_INSUFFICIENT_DESIGN = 4;
    public static final int CF_INSUFFICIENT_TEST = 5;
    public static final int CF_INSUFFICIENT_TEST2 = 6;
    public static final int CF_DESIGN_AFTER_CODE = 7;
    public static final int CF_CODE_AFTER_TEST = 8;

    public static final int SHEET_ONLY_ONE_FEATURE = 9;
    public static final int CF_TOO_LARGE = 10;

    public static final int ENTRY_IS_DUE_TODAY = 11;
    public static final int ENTRY_IS_DUE_TOMORROW = 12;
    public static final int ENTRY_NO_WORKERS = 13;
    public static final int ENTRY_NO_RESPONSIBLES = 14;
    public static final int ENTRY_NOT_COMPLETE = 15;

    public static final int SHEET_TOO_FEW_CFS = 16;
    public static final int SHEET_TOO_FEW_ESTIMATED_HOURS = 17;
    public static final int SHEET_TOO_MANY_ESTIMATED_HOURS = 18;

    public static final int ENTRY_NOT_ENOUGH_TIME = 19;
    public static final int ENTRY_TIME_TOO_TIGHT = 20;
    public static final int CF_NOT_ENOUGH_TIME = 21;
    public static final int CF_TIME_TOO_TIGHT = 22;
    public static final int SHEET_NOT_ENOUGH_TIME = 23;
    public static final int SHEET_TIME_TOO_TIGHT = 24;


    //~ Factory Methods .......................................................

    // ----------------------------------------------------------
    /**
     * A static factory method for creating a new
     * SheetFeedbackItem object given required
     * attributes and relationships.
     * @param editingContext The context in which the new object will be
     * inserted
     * @param round
     * @param codeValue
     * @param aSheet
     * @return The newly created object
     */
    public static SheetFeedbackItem create(
        EOEditingContext editingContext,
        int round,
        int codeValue,
        ScheduleSheet aSheet)
    {
        SheetFeedbackItem item = create(
            editingContext,
            TEMPLATES[codeValue].category,
            round,
            codeValue);
        item.setSheetRelationship(aSheet);
        return item;
    }


    // ----------------------------------------------------------
    /**
     * A static factory method for creating a new
     * SheetFeedbackItem object given required
     * attributes and relationships.
     * @param editingContext The context in which the new object will be
     * inserted
     * @param round
     * @param codeValue
     * @param aComponentFeature
     * @return The newly created object
     */
    public static SheetFeedbackItem create(
        EOEditingContext editingContext,
        int round,
        int codeValue,
        ComponentFeature aComponentFeature)
    {
        SheetFeedbackItem item = create(
            editingContext,
            round,
            codeValue,
            aComponentFeature.sheet());
        item.setComponentFeatureRelationship(aComponentFeature);
        return item;
    }


    // ----------------------------------------------------------
    /**
     * A static factory method for creating a new
     * SheetFeedbackItem object given required
     * attributes and relationships.
     * @param editingContext The context in which the new object will be
     * inserted
     * @param categoryValue
     * @param round
     * @param codeValue
     * @param isTransientValue
     * @return The newly created object
     */
    public static SheetFeedbackItem create(
        EOEditingContext editingContext,
        int round,
        int codeValue,
        SheetEntry entry)
    {
        SheetFeedbackItem item = create(
            editingContext,
            round,
            codeValue,
            entry.componentFeature());
        item.setSheetEntryRelationship(entry);
        return item;
    }


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    public String categoryString()
    {
        return cssClass();
    }


    // ----------------------------------------------------------
    public String text()
    {
        String msg = message();
        if (msg == null)
        {
            msg = TEMPLATES[code()].body;
        }
        return formatMessage(msg);
    }


    // ----------------------------------------------------------
    public Message renderedMessage()
    {
        return new Message(category(), text());
    }


    // ----------------------------------------------------------
    public String cssClass()
    {
        return org.webcat.core.Status.statusCssClass(category());
    }


    // ----------------------------------------------------------
    public static class Message
    {
        public final int category;
        public final String body;
        public Message(int category, String body)
        {
            this.category = category;
            this.body = body;
        }
    }


    //~ Private Methods .......................................................

    // ----------------------------------------------------------
    private WCProperties properties()
    {
        WCProperties props = new WCProperties();
        if (componentFeature() != null)
        {
            props.takeValueForKey(
                componentFeature().name(), "componentFeature");
        }
        if (sheetEntry() != null)
        {
            props.takeValueForKey(sheetEntry().activityName(), "activity");
        }
        return props;
    }


    // ----------------------------------------------------------
    private String formatMessage(String template)
    {
        return properties().substitutePropertyReferences(template);
    }


    //~ Instance/static fields ................................................

    private static final Message[] TEMPLATES = {
        // OK
        new Message(INFORMATION,
            "Your entries do not contain any obvious inconsistencies."),
        // ENTRY_MISSING_ESTIMATED_REMAINING
        new Message(ERROR,
            "The ${activity} activity for ${componentFeature} is not "
            + "complete, but no estimated time to complete it has been "
            + "entered."),
        // ENTRY_MISSING_NEW_DEADLINE
        new Message(ERROR,
            "The ${activity} activity for ${componentFeature} is not "
            + "complete, but new personal deadline has been "
            + "entered."),
        // ENTRY_IS_OVERDUE
        new Message(ERROR,
            "The ${activity} activity for ${componentFeature} is not "
            + "complete, but your personal deadline has passed."),

        // CF_INSUFFICIENT_DESIGN
        new Message(WARNING,
            "For ${componentFeature}, you have estimated a fair amount "
            + "of coding time, but have not allocated any time for design."),
        // CF_INSUFFICIENT_TEST
        new Message(ERROR,
            "For ${componentFeature}, you have estimated time to write "
            + "code, but have not allocated any time for testing it."),
        // CF_INSUFFICIENT_TEST2
        new Message(ERROR,
            "For ${componentFeature}, based on the amount of time you intend "
            + " to write code, it does not appear that you have allocated "
            + "sufficient time to testing it thoroughly."),
        // CF_DESIGN_AFTER_CODE
        new Message(WARNING,
            "For ${componentFeature}, your personal deadline for completing "
            + "the design comes after your personal deadline for coding, "
            + "which suggests you might be working on more (unimplemented) "
            + "design after coding is finished."),
        // CF_CODE_AFTER_TEST
        new Message(WARNING,
            "For ${componentFeature}, your personal deadline for completing "
            + "the code comes after your personal deadline for testing it, "
            + "which suggests you might be writing more (untested) code after "
            + "testing is finished."),

        // SHEET_ONLY_ONE_FEATURE
        new Message(ERROR,
            "You only have one component or feature listed in your "
            + "schedule.  Break up your schedule into a more appropriate "
            + "number of smaller pieces that can be managed more effectively."),
        // CF_TOO_LARGE
        new Message(ERROR,
            "The ${componentFeature} component of your schedule is too large. "
            + "Break it into smaller pieces that can be managed more "
            + "effectively."),
        // ENTRY_IS_DUE_TODAY
        new Message(WARNING,
            "The ${activity} activity for ${componentFeature} is due today."),
        // ENTRY_IS_DUE_TOMORROW
        new Message(INFORMATION,
            "The ${activity} activity for ${componentFeature} is due "
            + "tomorrow."),
        // ENTRY_NO_WORKERS
        new Message(ERROR,
            "No student has been identified who worked on the ${activity} "
            + "activity for ${componentFeature}."),
        // ENTRY_NO_RESPONSIBLES
        new Message(ERROR,
            "No student has been identified who is responsible for working "
            + "on the ${activity} activity for ${componentFeature}."),
        // ENTRY_NOT_COMPLETE
        new Message(ERROR,
            "The ${activity} activity for ${componentFeature} is not "
            + "complete.  Please resubmit your schedule once you "
            + "have completed everything."),
        // SHEET_TOO_FEW_CFS
        new Message(ERROR,
            "For a project of this size, you have not broken your plan down "
            + "into enough components or features.  Reconsider your plan and "
            + "divide your tasks into smaller, more manageable units."),
        // SHEET_TOO_FEW_ESTIMATED_HOURS
        new Message(ERROR,
            "For a project of this size, you have not estimated enough "
            + "hours to complete the work.  Reconsider your plan and "
            + "consider how much time it will take to complete each "
            + "component or feature."),
        // SHEET_TOO_MANY_ESTIMATED_HOURS
        new Message(WARNING,
            "For a project of this size, you have estimated a very large "
            + "number of hours to complete the work.  You may wish to "
            + "reconsider whether the tasks you have defined will take as "
            + "much time as you have estimated."),
        // ENTRY_NOT_ENOUGH_TIME
        new Message(ERROR,
            "The ${activity} activity for ${componentFeature} has a deadline "
            + "that is too soon to realistically complete the number of hours "
            + "you have estimated."),
        // ENTRY_TIME_TOO_TIGHT
        new Message(WARNING,
            "The ${activity} activity for ${componentFeature} has a deadline "
            + "that is too soon to complete the number of hours "
            + "you have estimated without serious overtime."),
        // CF_NOT_ENOUGH_TIME
        new Message(ERROR,
            "The ${componentFeature} has deadline(s) that are too soon to "
            + "realistically complete the number of hours you have estimated."),
        // CF_TIME_TOO_TIGHT
        new Message(WARNING,
            "The ${componentFeature} has deadline(s) that are too soon to "
            + "complete the number of hours "
            + "you have estimated without serious overtime."),
        // SHEET_NOT_ENOUGH_TIME
        new Message(ERROR,
            "This schedule has deadline(s) that are too soon to "
            + "realistically complete the number of hours you have estimated."),
        // SHEET_TIME_TOO_TIGHT
        new Message(WARNING,
            "This schedule has deadline(s) that are too soon to "
            + "complete the number of hours "
            + "you have estimated without serious overtime.")
    };
}
