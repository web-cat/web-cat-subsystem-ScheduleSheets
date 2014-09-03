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
import com.webobjects.eoaccess.EOUtilities;
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


    //~ Factory Methods .......................................................

    // ----------------------------------------------------------
    /**
     * A static factory method for creating a new
     * SheetFeedbackItem object given required
     * attributes and relationships.
     * @param editingContext The context in which the new object will be
     * inserted
     * @param codeValue
     * @param aSheet
     * @return The newly created object
     */
    public static SheetFeedbackItem create(
        EOEditingContext editingContext,
        int codeValue,
        ScheduleSheet aSheet)
    {
        SheetFeedbackItem item = create(
            editingContext,
            TEMPLATES[codeValue].category,
            codeValue,
            false);
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
     * @param codeValue
     * @param aComponentFeature
     * @return The newly created object
     */
    public static SheetFeedbackItem create(
        EOEditingContext editingContext,
        int codeValue,
        ComponentFeature aComponentFeature)
    {
        SheetFeedbackItem item = create(
            editingContext,
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
     * @param codeValue
     * @param isTransientValue
     * @return The newly created object
     */
    public static SheetFeedbackItem create(
        EOEditingContext editingContext,
        int codeValue,
        SheetEntry entry)
    {
        SheetFeedbackItem item = create(
            editingContext,
            codeValue,
            entry.componentFeature());
        item.setSheetEntryRelationship(entry);
        return item;
    }


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    public String categoryString()
    {
        return org.webcat.core.Status.statusCssClass(category());
    }


    // ----------------------------------------------------------
    public Message renderedMessage()
    {
        if (message() == null)
        {
            Message template = TEMPLATES[code()];
            return new Message(category(), formatMessage(template.body));
        }
        else
        {
            return new Message(category(), formatMessage(message()));
        }
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
            "Your entries do not contain any obvious inconsistencies")
    };
}
