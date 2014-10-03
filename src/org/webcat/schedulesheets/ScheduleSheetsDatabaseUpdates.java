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


    // ----------------------------------------------------------
    /**
     * Add sheet and component feature ids to sheet feedback items.
     * @throws SQLException on error
     */
    public void updateIncrement1() throws SQLException
    {
        database().executeSQL(
            "alter table SheetFeedbackItem add sheetId INTEGER");
        database().executeSQL(
            "alter table SheetFeedbackItem add componentFeatureId INTEGER");
        createIndexFor("SheetFeedbackItem", "sheetId");
        createIndexFor("SheetFeedbackItem", "componentFeatureId");
    }


    // ----------------------------------------------------------
    /**
     * Add score columns to schedule sheet table.
     * @throws SQLException on error
     */
    public void updateIncrement2() throws SQLException
    {
        database().executeSQL(
            "alter table ScheduleSheet add taScore DOUBLE");
        database().executeSQL(
            "alter table ScheduleSheet add toolScore DOUBLE");
    }


    // ----------------------------------------------------------
    /**
     * Add more columns to sheet entries.
     * @throws SQLException on error
     */
    public void updateIncrement3() throws SQLException
    {
        database().executeSQL(
            "alter table SheetEntry add previousEstimatedRemaining DOUBLE");
        database().executeSQL(
            "alter table SheetEntry add previousTimeInvestedTotal DOUBLE");
        database().executeSQL(
            "alter table SheetEntry add previousWasComplete BIT NOT NULL");

        database().executeSQL(
            "alter table SheetEntry change estimatedRemaining "
            + "newEstimatedRemaining DOUBLE");
        database().executeSQL(
            "alter table SheetEntry change timeInvested "
            + "newTimeInvested DOUBLE");
    }


    // ----------------------------------------------------------
    /**
     * Add score columns to schedule sheet table.
     * @throws SQLException on error
     */
    public void updateIncrement4() throws SQLException
    {
        database().executeSQL(
            "alter table ScheduleSheet add numCheckRounds INTEGER");
        database().executeSQL(
            "alter table SheetFeedbackItem add checkRound INTEGER NOT NULL");
        database().executeSQL(
            "alter table SheetFeedbackItem drop isTransient");
    }


    // ----------------------------------------------------------
    /**
     * Add the ComponentFeatureStudent table.
     * @throws SQLException on error
     */
    public void updateIncrement5() throws SQLException
    {
        createEntryStudentPreviouslyWorkedTable();
        createEntryStudentNewResponsibleTable();
    }


    // ----------------------------------------------------------
    /**
     * Add the e-mail alerts tables.
     * @throws SQLException on error
     */
    public void updateIncrement6() throws SQLException
    {
        createEmailAlertGroupForAssignmentTable();
        createEmailAlertForAssignmentTable();
        createEmailAlertForAssignmentOfferingTable();
        createEmailAlertForStudentTable();
    }


    // ----------------------------------------------------------
    /**
     * Add flags for sent messages.
     * @throws SQLException on error
     */
    public void updateIncrement7() throws SQLException
    {
        database().executeSQL(
            "alter table EmailAlertForStudent add sent BIT NOT NULL");
        database().executeSQL(
            "alter table EmailAlertForAssignmentOffering "
            + "change sent sendTime DATETIME");
        database().executeSQL(
            "alter table EmailAlertForAssignmentOffering "
            + "add sent BIT NOT NULL");
    }


    // ----------------------------------------------------------
    /**
     * Add expected size and min number of components fields to
     * schedule sheet assignments.
     * @throws SQLException on error
     */
    public void updateIncrement8() throws SQLException
    {
        database().executeSQL(
            "alter table ScheduleSheetAssignment add expectedSize INT");
        database().executeSQL(
            "alter table ScheduleSheetAssignment add "
            + "minComponentFeatures INT");
    }


    // ----------------------------------------------------------
    /**
     * Add alertNo to email alerts for assignment.
     * @throws SQLException on error
     */
    public void updateIncrement9() throws SQLException
    {
        database().executeSQL(
            "alter table EmailAlertForAssignment add alertNo INT");
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
            createIndexFor("SheetFeedbackItem", "sheetEntryId");
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the EntryStudentPreviouslyWorked table, if needed.
     * @throws SQLException on error
     */
    private void createEntryStudentPreviouslyWorkedTable() throws SQLException
    {
        if (!database().hasTable("EntryStudentPreviouslyWorked", "id", "1"))
        {
            log.info("creating table EntryStudentPreviouslyWorked");
            database().executeSQL(
                "CREATE TABLE EntryStudentPreviouslyWorked "
                + "(id INT NOT NULL, id1 INT NOT NULL)");
            database().executeSQL(
                "ALTER TABLE EntryStudentPreviouslyWorked ADD PRIMARY KEY "
                + "(id, id1)");
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the EntryStudentNewResponsible table, if needed.
     * @throws SQLException on error
     */
    private void createEntryStudentNewResponsibleTable() throws SQLException
    {
        if (!database().hasTable("EntryStudentNewResponsible", "id", "1"))
        {
            log.info("creating table EntryStudentNewResponsible");
            database().executeSQL(
                "CREATE TABLE EntryStudentNewResponsible "
                + "(id INT NOT NULL, id1 INT NOT NULL)");
            database().executeSQL(
                "ALTER TABLE EntryStudentNewResponsible ADD PRIMARY KEY "
                + "(id, id1)");
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the EmailAlertGroupForAssignment table, if needed.
     * @throws SQLException on error
     */
    private void createEmailAlertGroupForAssignmentTable() throws SQLException
    {
        if (!database().hasTable("EmailAlertGroupForAssignment", "id", "1"))
        {
            log.info("creating table EmailAlertGroupForAssignment");
            database().executeSQL(
                "CREATE TABLE EmailAlertGroupForAssignment ("
                + "assignmentId INTEGER NOT NULL, "
                + "authorId INTEGER, "
                + "OID INTEGER NOT NULL, "
                + "numberOfAlerts INTEGER"
                + ")");
            database().executeSQL(
                "ALTER TABLE EmailAlertGroupForAssignment ADD PRIMARY KEY "
                + "(OID)");
            createIndexFor("EmailAlertGroupForAssignment", "assignmentId");
            createIndexFor("EmailAlertGroupForAssignment", "authorId");
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the EmailAlertForAssignment table, if needed.
     * @throws SQLException on error
     */
    private void createEmailAlertForAssignmentTable() throws SQLException
    {
        if (!database().hasTable("EmailAlertForAssignment", "id", "1"))
        {
            log.info("creating table EmailAlertForAssignment");
            database().executeSQL(
                "CREATE TABLE EmailAlertForAssignment ("
                + "groupId INTEGER NOT NULL, "
                + "OID INTEGER NOT NULL, "
                + "timeBeforeDue BIGINT"
                + ")");
            database().executeSQL(
                "ALTER TABLE EmailAlertForAssignment ADD PRIMARY KEY "
                + "(OID)");
            createIndexFor("EmailAlertForAssignment", "groupId");
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the EmailAlertForAssignmentOffering table, if needed.
     * @throws SQLException on error
     */
    private void createEmailAlertForAssignmentOfferingTable()
        throws SQLException
    {
        if (!database().hasTable("EmailAlertForAssignmentOffering", "id", "1"))
        {
            log.info("creating table EmailAlertForAssignmentOffering");
            database().executeSQL(
                "CREATE TABLE EmailAlertForAssignmentOffering ("
                + "alertForAssignmentId INTEGER NOT NULL, "
                + "courseOfferingId INTEGER NOT NULL, "
                + "OID INTEGER NOT NULL, "
                + "sent DATETIME"
                + ")");
            database().executeSQL(
                "ALTER TABLE EmailAlertForAssignmentOffering ADD PRIMARY KEY "
                + "(OID)");
            createIndexFor("EmailAlertForAssignment", "alertForAssignmentId");
        }
    }


    // ----------------------------------------------------------
    /**
     * Create the EmailAlertForStudent table, if needed.
     * @throws SQLException on error
     */
    private void createEmailAlertForStudentTable() throws SQLException
    {
        if (!database().hasTable("EmailAlertForStudent", "id", "1"))
        {
            log.info("creating table EmailAlertForStudent");
            database().executeSQL(
                "CREATE TABLE EmailAlertForStudent ("
                + "alertOfferingId INTEGER NOT NULL, "
                + "byteCodingRating TINYINT, "
                + "byteCorrectnessRating TINYINT, "
                + "byteStyleRating TINYINT, "
                + "byteTestingRating TINYINT, "
                + "OID INTEGER NOT NULL, "
                + "userId INTEGER NOT NULL, "
                + "submissionId INTEGER"
                + ")");
            database().executeSQL(
                "ALTER TABLE EmailAlertForStudent ADD PRIMARY KEY (OID)");
            createIndexFor("EmailAlertForStudent", "alertOfferingId");
            createIndexFor("EmailAlertForStudent", "userId");
            createIndexFor("EmailAlertForStudent", "submissionId");
        }
    }
}
