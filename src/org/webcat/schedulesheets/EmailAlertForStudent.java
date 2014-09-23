/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2012 Virginia Tech
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

import org.apache.log4j.Logger;
import org.webcat.core.Application;
import org.webcat.core.User;
import org.webcat.grader.Assignment;
import org.webcat.grader.AssignmentOffering;
import org.webcat.grader.Step;
import org.webcat.grader.Submission;
import org.webcat.grader.SubmissionFileStats;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSMutableArray;

// -------------------------------------------------------------------------
/**
 * TODO: place a real description here.
 *
 * @author
 * @author  Last changed by: $Author$
 * @version $Revision$, $Date$
 */
public class EmailAlertForStudent
    extends _EmailAlertForStudent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new EmailAlertForStudent object.
     */
    public EmailAlertForStudent()
    {
        super();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public Assignment assignment()
    {
        return alertOffering().alertForAssignment().alertGroup().assignment();
    }


    // ----------------------------------------------------------
    public Rating codingRating()
    {
        return Rating.valueOf(byteCodingRating());
    }


    // ----------------------------------------------------------
    public void setCodingRating(Rating value)
    {
        if (value == null)
        {
            super.setByteCodingRatingRaw(null);
        }
        else
        {
            super.setByteCodingRating(value.byteValue());
        }
    }


    // ----------------------------------------------------------
    public Rating correctnessRating()
    {
        return Rating.valueOf(byteCorrectnessRating());
    }


    // ----------------------------------------------------------
    public void setCorrectnessRating(Rating value)
    {
        if (value == null)
        {
            super.setByteCorrectnessRatingRaw(null);
        }
        else
        {
            super.setByteCorrectnessRating(value.byteValue());
        }
    }


    // ----------------------------------------------------------
    public Rating styleRating()
    {
        return Rating.valueOf(byteStyleRating());
    }


    // ----------------------------------------------------------
    public void setStyleRating(Rating value)
    {
        if (value == null)
        {
            super.setByteStyleRatingRaw(null);
        }
        else
        {
            super.setByteStyleRating(value.byteValue());
        }
    }


    // ----------------------------------------------------------
    public Rating testingRating()
    {
        return Rating.valueOf(byteTestingRating());
    }


    // ----------------------------------------------------------
    public void setTestingRating(Rating value)
    {
        if (value == null)
        {
            super.setByteTestingRatingRaw(null);
        }
        else
        {
            super.setByteTestingRating(value.byteValue());
        }
    }


    // ----------------------------------------------------------
    public static void send(EOEditingContext editingContext,
        EmailAlertForAssignmentOffering offering,
        AssignmentOffering assignment,
        User aUser,
        Submission aSubmission)
    {
        EmailAlertForStudent alert = EmailAlertForStudent
            .firstObjectMatchingQualifier(editingContext,
                EmailAlertForStudent.alertOffering.is(offering).and(
                EmailAlertForStudent.user.is(aUser)),
                null);
        // If one has already been sent
        if (alert != null)
        {
            log.info("Call to send() for " + aUser + " on " + assignment
                + ", but an e-mail alert was already sent");
            return;
        }

        // Create a new one
        alert = create(editingContext, false, offering, aUser);

        alert.setSubmissionRelationship(aSubmission);
        int daysLeft = alert.calculateDayOffset(assignment);
        alert.analyze(daysLeft);
        alert.send(daysLeft);

        editingContext.saveChanges();
    }


    // ----------------------------------------------------------
    private int calculateDayOffset(AssignmentOffering assignment)
    {
        long due = assignment.dueDate().getTime();
        long send = alertOffering().sendTime().getTime();
        long diff = due - send;

        if (diff <= 0)
        {
            return 0;
        }

        long days = diff / DAY;
        long rem = diff - days * DAY;
        if (rem > 3 * HOUR)
        {
            days++;
        }
        return (int)days;
    }


    // ----------------------------------------------------------
    private void analyze(int daysLeft)
    {
        analyzeCoding(daysLeft);
        analyzeCorrectness(daysLeft);
        analyzeStyle(daysLeft);
        analyzeTesting(daysLeft);
    }


    // ----------------------------------------------------------
    private void analyzeCoding(int daysLeft)
    {
        if (submission() != null
            && submission().resultIsReady())
        {
            int size =  accumulate(Stat.NCLOC, true, false);
//            double pct = ((double)size) / ((double)SIZE);

            int target = SIZE - daysLeft * PER_DAY;

            if (size < 10 || size < target / 2)
            {
                setCodingRating(Rating.Bad);
            }
            else if (size > target)
            {
                setCodingRating(Rating.Good);
            }
            else
            {
                setCodingRating(Rating.Neutral);
            }
        }
    }


    // ----------------------------------------------------------
    private void analyzeCorrectness(int daysLeft)
    {
        if (submission() != null
            && submission().resultIsReady())
        {
            double score = submission().result().correctnessScore();
            if (usesTesting())
            {
                int elements = accumulate(Stat.ELEMENTS, true, false);
                int covered = accumulate(Stat.COVERED, true, false);
                score /= ((double)covered)/((double)elements);
            }
            double goal = 1.0 - daysLeft * 0.2;
            if (score < 0.1 || score < 0.5 * goal)
            {
                setCorrectnessRating(Rating.Bad);
            }
            else if (score >= goal)
            {
                setCorrectnessRating(Rating.Good);
            }
            else
            {
                setCorrectnessRating(Rating.Neutral);
            }
        }
    }


    // ----------------------------------------------------------
    private void analyzeStyle(int daysLeft)
    {
        if (submission() != null
            && submission().resultIsReady())
        {
            double pct = submission().result().toolScore() /
                assignment().submissionProfile().toolPoints();
            if (pct < 0.25)
            {
                setStyleRating(Rating.Bad);
            }
            else if (pct < 0.75)
            {
                setStyleRating(Rating.Neutral);
            }
            else
            {
                setStyleRating(Rating.Good);
            }
        }
    }


    // ----------------------------------------------------------
    private void analyzeTesting(int daysLeft)
    {
        if (submission() != null
            && submission().resultIsReady())
        {
            int elements = accumulate(Stat.ELEMENTS, true, false);
            int covered = accumulate(Stat.COVERED, true, false);
            int size =  accumulate(Stat.NCLOC, false, true);
            double cvg = ((double)covered) / ((double)elements);

            if (size == 0 || cvg < 0.40)
            {
                setTestingRating(Rating.Bad);
            }
            else if (cvg < 0.75)
            {
                setTestingRating(Rating.Neutral);
            }
            else
            {
                setTestingRating(Rating.Good);
            }
        }
    }


    // ----------------------------------------------------------
    private boolean hasRating(Rating value)
    {
        return codingRating() == value
            || correctnessRating() == value
            || styleRating() == value
            || testingRating() == value;
    }


    // ----------------------------------------------------------
    private String subject(int daysLeft)
    {
        String msg = alertOffering().courseOffering().course().deptNumber()
            + ": ";
        if (hasRating(Rating.Bad)
            || (submission() == null && daysLeft <= 4))
        {
            msg += "You are at risk";
        }
        else if (submission() == null || hasRating(Rating.Undefined))
        {
            msg += "You may be at risk";
        }
        else
        {
            msg += "Your progress";
        }
        msg += " on " + assignment().name();
        return msg;
    }


    // ----------------------------------------------------------
    private String body(int daysLeft)
    {
        Assignment assignment = assignment();
        String name = assignment.name();
        String dueString = "due in " + daysLeft + " days.";
        if (daysLeft == 0)
        {
            dueString = "due now.";
        }
        else if (daysLeft < 0)
        {
            dueString = "past due.";
        }
        String body =
            "This notification is to increase your awareness of your current\n"
            + "progress on "
            + name
            + " compared to the rest of the class.\n"
            + name
            + " is "
            + dueString
            + "\n\n";

        if (submission() == null)
        {
            body +=
                "You have not submitted any work to Web-CAT for analysis on\n"
                + name
                + ".\n"
                + "As a result, there is no way to assess your progress "
                + "relative to the\n"
                + "rest of the class, indicating you may be at risk for "
                + "performing\n"
                + "poorly on the assignment.\n"
                + "\n"
                + "Historical data from many programming classes like this "
                + "one indicate\n"
                + "that when a student starts earlier, he or she earns "
                + "higher scores\n"
                + "on average than when the same student starts later.\n"
                + "\n"
                + "If you have not begun work on the project, it is "
                + "important to do\n"
                + "so as soon as possible.  If you have already started "
                + "work, we\n"
                + "encourage you to submit to Web-CAT and self-check your "
                + "work early\n"
                + "and often.  Remember to incrementally test as you write "
                + "code as\n"
                + "well, since leaving testing for later also increases the "
                + "risk of\n"
                + "performing poorly.\n";
        }
        else
        {
            Rating rating = codingRating();
            if (assignment.usesTestingScore()
                && rating.ordinal() > correctnessRating().ordinal())
            {
                rating = correctnessRating();
            }
            switch (rating)
            {
                case Undefined:
                    body +=
                        "While you have submitted work to Web-CAT, analysis "
                        + "of your work\n"
                        + "produced no information.  As a result, there is "
                        + "no way to assess your\n"
                        + "progress relative to the rest of the class, "
                        + "indicating you may be at\n"
                        + "risk for performing poorly on the assignment.\n";
                    break;
                case Bad:
                    body +=
                        "Based on the work you have submitted to Web-CAT, it "
                        + "looks like you\n"
                        + "are behind in your progress on this assignment.  "
                        + "This increases\n"
                        + "your risk of performing poorly.  If you are stuck, "
                        + "you should\n"
                        + "visit a member of the course staff for assistance "
                        + "in getting\n"
                        + "back on track.\n";
                    break;
                case Good:
                    body +=
                        "Based on the work you have submitted to Web-CAT, it "
                        + "looks like you\n"
                        + "are making good progress towards a working solution "
                        + "for this assignment.\n"
                        + "Starting early is associated with a statistically "
                        + "significant increase\n"
                        + "is scores earned, compared to when the same "
                        + "student starts later.\n"
                        + "This increases your chances of success on the "
                        + "assignment.\n";
                    break;
                default:
                    body +=
                        "Based on the work you have submitted to Web-CAT, it "
                        + "looks like you\n"
                        + "are making progress towards a working solution "
                        + "for this assignment.\n"
                        + "Starting early is associated with a statistically "
                        + "significant increase\n"
                        + "is scores earned, compared to when the same "
                        + "student starts later.\n"
                        + "This increases your chances of success on the "
                        + "assignment.\n";
                    break;
            }
            if (usesTesting())
            {
                body += "\n";
                switch (testingRating())
                {
                    case Undefined:
                        body +=
                            "Based on the work you have submitted, it "
                            + "appears that you do not\n"
                            + "yet have working tests for the code you "
                            + "have developed.\n"
                            + "Typical students earn higher scores when "
                            + "they write tests\n"
                            + "incrementally with their code compared to "
                            + "when the same student\n"
                            + "waits to write tests until the code is "
                            + "substantially complete.\n"
                            + "This increases your risk of performing "
                            + "poorly on the assignment.\n";
                        break;
                    case Good:
                        body +=
                            "Based on the tests you have submitted, it "
                            + "appears that you are\n"
                            + "also self-checking some of your work.  This "
                            + "also increases your\n"
                            + "chances of success on the assignment. Typical "
                            + "students earn higher\n"
                            + "scores when they write tests incrementally "
                            + "with their code\n"
                            + "compared to when the same student waits to "
                            + "write tests until\n"
                            + "the code is substantially complete.  Work to "
                            + "improve your incremental\n"
                            + "testing for best results.\n";
                        break;
                    case Bad:
                    default:
                        body +=
                            "Based on the tests you have submitted, it "
                            + "appears that you may be\n"
                            + "waiting until later to test your work, "
                            + "instead of testing it\n"
                            + "incrementally as you develop it.  Typical "
                            + "students earn higher\n"
                            + "scores when they write tests incrementally "
                            + "with their code\n"
                            + "compared to when the same student waits to "
                            + "write tests until\n"
                            + "the code is substantially complete. This "
                            + "increases your risk of\n"
                            + "performing poorly on the assignment.\n";
                        break;
                }
            }
            if (assignment.usesToolCheckScore())
            {
                body += "\n";
                switch (styleRating())
                {
                    case Undefined:
                    case Bad:
                        body +=
                            "Based on the style checks on your assignment, "
                            + "it appears you are\n"
                            + "not formatting your code as expected when "
                            + "you write it, and may\n"
                            + "be intending to go back and correct the "
                            + "formatting later. Adjusting\n"
                            + "your code writing style so that you produce "
                            + "properly formatted and\n"
                            + "documented code as you write will increase "
                            + "your efficiency and\n"
                            + "reduce the time needed to clean up code "
                            + "later.\n";
                        break;
                    case Good:
                        body +=
                            "Based on the style checks on your assignment, "
                            + "it appears you are\n"
                            + "formatting your code as expected when you "
                            + "write it. This increases\n"
                            + "your efficiency and reduces the time needed "
                            + "to clean up code later.\n";
                        break;
                    default:
                        body +=
                            "Based on the style checks on your assignment, "
                            + "it appears you are\n"
                            + "not consistently formatting your code as "
                            + "expected when you write\n"
                            + "it, and may be intending to go back and "
                            + "correct the formatting\n"
                            + "later. Adjusting your code writing style "
                            + "more consistently so that\n"
                            + "you produce properly formatted and "
                            + "documented code as you write\n"
                            + "will increase your efficiency and reduce "
                            + "the time needed to clean\n"
                            + "up code later.\n";
                        break;
                }
            }
        }


        body += "\n"
            + "We wish you the best of luck as you work to complete this "
            + "assignment.\n\n\n"
            + "-- Web-CAT Situational Awareness Service\n";

        return body;
    }


    // ----------------------------------------------------------
    private void send(int daysLeft)
    {
        NSMutableArray<String> to = new NSMutableArray<String>(2);
        to.add(user().email());
        if (submission() != null)
        {
            for (Submission s : submission().partneredSubmissions())
            {
                if (s.user() != user())
                {
                    to.add(s.user().email());
                }
            }
        }
        if (CC_WEBCAT)
        {
            to.add("webcat@vt.edu");
        }
        Application.sendSimpleEmail(to, subject(daysLeft), body(daysLeft));
        setSent(true);
    }


    // ----------------------------------------------------------
    private static enum Stat { NCLOC, COVERED, ELEMENTS }


    // ----------------------------------------------------------
    private int stat(SubmissionFileStats sfs, Stat s)
    {
        switch (s)
        {
            case NCLOC:   return sfs.ncloc();
            case COVERED: return sfs.elementsCovered();
            default:      return sfs.elements();
        }
    }


    // ----------------------------------------------------------
    private boolean isClass(SubmissionFileStats sfs)
    {
        return sfs.className() != null;
    }


    // ----------------------------------------------------------
    private boolean isTest(SubmissionFileStats sfs)
    {
        return isClass(sfs)
            && (sfs.className().endsWith("Test")
            || sfs.className().endsWith("Tests"));
    }


    // ----------------------------------------------------------
    private int accumulate(
        Stat s, boolean includeSolution, boolean includeTests)
    {
        int total = 0;
        for (SubmissionFileStats sfs :
            submission().result().submissionFileStats())
        {
            if (isTest(sfs))
            {
                if (includeTests)
                {
                    total += stat(sfs, s);
                }
            }
            else if (isClass(sfs))
            {
                if (includeSolution)
                {
                    total += stat(sfs, s);
                }
            }
        }
        return total;
    }


    // ----------------------------------------------------------
    private boolean usesTesting()
    {
        boolean usesTesting = assignment().usesTestingScore();
        if (usesTesting)
        {
            usesTesting = false;
            for (Step step : assignment().steps())
            {
                if ((step.config() != null
                    && step.config().configSettings() != null
                    && step.config().configSettings()
                        .containsKey("studentsMustSubmitTests")
                    && step.config().configSettings()
                        .booleanObjectForKey("studentsMustSubmitTests"))
                    || (step.gradingPlugin() != null
                        && step.gradingPlugin().name() != null
                        && step.gradingPlugin().name().toLowerCase()
                            .contains("tdd")))
                {
                    usesTesting = true;
                    break;
                }
            }
        }
        return usesTesting;
    }


    //~ Instance/static variables .............................................

    private static final int  SIZE = 500;
    private static final long HOUR = 1000 * 60 * 60;
    private static final long DAY  = 24 * HOUR;
    private static final int PER_DAY = 75;
    private static final boolean CC_WEBCAT = true;

    static Logger log = Logger.getLogger(EmailAlertForStudent.class);
}
