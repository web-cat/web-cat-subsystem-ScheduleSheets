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

import org.webcat.dbupdate.UpdateSet;
import java.sql.SQLException;

//-------------------------------------------------------------------------
/**
 * This class captures the SQL database schema for the database tables
 * underlying the ScheduleSheets subsystem and the ScheduleSheets.eomodeld.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class ScheduleSheetsDatabaseUpdates
    extends UpdateSet
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * The default constructor uses the name "schedulesheets" as the unique
     * identifier for this subsystem and EOModel.
     */
    public ScheduleSheetsDatabaseUpdates()
    {
        super("schedulesheets");
    }


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    /**
     * Creates all tables in their baseline configuration, as needed.
     * @throws SQLException on error
     */
    public void updateIncrement0() throws SQLException
    {
        createComponentFeatureTable();
        createScheduleSheetTable();
        createScheduleSheetAssignmentTable();
        createScheduleSheetAssignmentOfferingTable();
        createScheduleSheetSubmissionTable();
        createSheetEntryTable();
        createSheetFeedbackItemTable();
    }


    //~ Private Methods .......................................................

    // ----------------------------------------------------------
    /**
     * Create the ComponentFeature table, if needed.
     * @throws SQLException on error
     */
    private void createComponentFeatureTable() throws SQLException
    {
        if (!database().hasTable("ComponentFeature"))
        {
            log.info("creating table ComponentFeature");
            database().executeSQL(
                "create table ComponentFeature ("
                + "OID INTEGER NOT NULL, "
                + "name TINYTEXT, "
                + "Corder TINYINT, "
                + "sheetId INTEGER"
                + ")");
            database().executeSQL(
                "ALTER TABLE ComponentFeature ADD PRIMARY KEY (OID)");
            createIndexFor("ComponentFeature", "sheetId");
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the ScheduleSheet table, if needed.
     * @throws SQLException on error
     */
    private void createScheduleSheetTable() throws SQLException
    {
        if (!database().hasTable("ScheduleSheet"))
        {
            log.info("creating table ScheduleSheet");
            database().executeSQL(
                "create table ScheduleSheet ("
                + "commentFormat TINYINT, "
                + "comments MEDIUMTEXT, "
                + "score DOUBLE, "
                + "OID INTEGER NOT NULL, "
                + "lastUpdated DATETIME, "
                + "status TINYINT"
                + ")"
            );
            database().executeSQL(
                "ALTER TABLE ScheduleSheet ADD PRIMARY KEY (OID)");
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the ScheduleSheetAssignment table, if needed.
     * @throws SQLException on error
     */
    private void createScheduleSheetAssignmentTable() throws SQLException
    {
        if (!database().hasTable("ScheduleSheetAssignment"))
        {
            log.info("creating table ScheduleSheetAssignment");
            database().executeSQL(
                "create table ScheduleSheetAssignment ("
                + "assignmentId INTEGER, "
                + "authorId INTEGER, "
                + "OID INTEGER NOT NULL, "
                + "name TINYTEXT, "
                + "numberOfSheets TINYINT, "
                + "shortDescription TINYTEXT, "
                + "submissionProfileId INTEGER, "
                + "url TINYTEXT"
                + ")"
            );
            database().executeSQL(
                "ALTER TABLE ScheduleSheetAssignment ADD PRIMARY KEY (OID)");
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the ScheduleSheetAssignmentOffering table, if needed.
     * @throws SQLException on error
     */
    private void createScheduleSheetAssignmentOfferingTable()
        throws SQLException
    {
        if (!database().hasTable("ScheduleSheetAssignmentOffering"))
        {
            log.info("creating table ScheduleSheetAssignmentOffering");
            database().executeSQL(
                "create table ScheduleSheetAssignmentOffering ("
                + "assignmentId INTEGER, "
                + "closedOnDate DATETIME, "
                + "courseOfferingId INTEGER, "
                + "dueDate DATETIME, "
                + "OID INTEGER NOT NULL, "
                + "Corder TINYINT, "
                + "publish BIT NOT NULL, "
                + "shortDescription TINYTEXT, "
                + "submissionProfileId INTEGER, "
                + "url TINYTEXT"
                + ")"
            );
            database().executeSQL(
                "ALTER TABLE ScheduleSheetAssignmentOffering "
                + "ADD PRIMARY KEY (OID)");
            createIndexFor("ScheduleSheetAssignmentOffering", "assignmentId");
            createIndexFor(
                "ScheduleSheetAssignmentOffering", "courseOfferingId");
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the ScheduleSheetSubmission table, if needed.
     * @throws SQLException on error
     */
    private void createScheduleSheetSubmissionTable() throws SQLException
    {
        if (!database().hasTable("ScheduleSheetSubmission"))
        {
            log.info("creating table ScheduleSheetSubmission");
            database().executeSQL(
                "create table ScheduleSheetSubmission ("
                + "assignmentOfferingId INTEGER, "
                + "OID INTEGER NOT NULL, "
                + "isSubmissionForGrading BIT NOT NULL, "
                + "partnerLink BIT NOT NULL, "
                + "primarySubmissionId INTEGER, "
                + "sheetId INTEGER, "
                + "submitNumber INTEGER, "
                + "submitTime DATETIME, "
                + "userId INTEGER"
                + ")"
            );
            database().executeSQL(
                "ALTER TABLE ScheduleSheetSubmission ADD PRIMARY KEY (OID)");
            createIndexFor("ScheduleSheetSubmission", "assignmentOfferingId");
            createIndexFor("ScheduleSheetSubmission", "primarySubmissionId");
            createIndexFor("ScheduleSheetSubmission", "sheetId");
            createIndexFor("ScheduleSheetSubmission", "userId");
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the SheetEntry table, if needed.
     * @throws SQLException on error
     */
    private void createSheetEntryTable() throws SQLException
    {
        if (!database().hasTable("SheetEntry"))
        {
            log.info("creating table SheetEntry");
            database().executeSQL(
                "create table SheetEntry ("
                + "activity TINYINT, "
                + "componentFeatureId INTEGER, "
                + "estimatedRemaining DOUBLE, "
                + "OID INTEGER NOT NULL, "
                + "isComplete BIT NOT NULL, "
                + "newDeadline DATE, "
                + "previousDeadline DATE, "
                + "previousEstimatedTotal DOUBLE, "
                + "timeInvested DOUBLE"
                + ")"
            );
            database().executeSQL(
                "ALTER TABLE SheetEntry ADD PRIMARY KEY (OID)");
            createIndexFor("SheetEntry", "componentFeatureId");
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the SheetFeedbackItem table, if needed.
     * @throws SQLException on error
     */
    private void createSheetFeedbackItemTable() throws SQLException
    {
        if (!database().hasTable("SheetFeedbackItem"))
        {
            log.info("creating table SheetFeedbackItem");
            database().executeSQL(
                "create table SheetFeedbackItem ("
                + "category TINYINT NOT NULL, "
                + "code INTEGER NOT NULL, "
                + "sheetEntryId INTEGER, "
                + "isTransient BIT NOT NULL, "
                + "OID INTEGER NOT NULL, "
                + "message MEDIUMTEXT"
                + ")"
            );
            database().executeSQL(
                "ALTER TABLE SheetFeedbackItem ADD PRIMARY KEY (OID)");
            createIndexFor("SheetFeedbackItem", "componentFeatureId");
        }
    }
}
