/*==========================================================================*\
 |  _EmailAlertForAssignment.java
 |*-------------------------------------------------------------------------*|
 |  Created by eogenerator
 |  DO NOT EDIT.  Make changes to EmailAlertForAssignment.java instead.
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2012 Virginia Tech
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

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.eof.ERXKey;
import org.apache.log4j.Logger;
import org.webcat.core.EOBasedKeyGenerator;
import org.webcat.woextensions.WCFetchSpecification;

// -------------------------------------------------------------------------
/**
 * An automatically generated EOGenericRecord subclass.  DO NOT EDIT.
 * To change, use EOModeler, or make additions in
 * EmailAlertForAssignment.java.
 *
 * @author Generated by eogenerator
 * @version version suppressed to control auto-generation
 */
public abstract class _EmailAlertForAssignment
    extends org.webcat.core.EOBase
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new _EmailAlertForAssignment object.
     */
    public _EmailAlertForAssignment()
    {
        super();
    }


    // ----------------------------------------------------------
    /**
     * A static factory method for creating a new
     * EmailAlertForAssignment object given required
     * attributes and relationships.
     * @param editingContext The context in which the new object will be
     * inserted
     * @param alertGroupValue
     * @return The newly created object
     */
    public static EmailAlertForAssignment create(
        EOEditingContext editingContext,
        org.webcat.schedulesheets.EmailAlertGroupForAssignment alertGroupValue
        )
    {
        EmailAlertForAssignment eoObject = (EmailAlertForAssignment)
            EOUtilities.createAndInsertInstance(
                editingContext,
                _EmailAlertForAssignment.ENTITY_NAME);
        eoObject.setAlertGroupRelationship(alertGroupValue);
        return eoObject;
    }


    // ----------------------------------------------------------
    /**
     * Get a local instance of the given object in another editing context.
     * @param editingContext The target editing context
     * @param eo The object to import
     * @return An instance of the given object in the target editing context
     */
    public static EmailAlertForAssignment localInstance(
        EOEditingContext editingContext, EmailAlertForAssignment eo)
    {
        return (eo == null)
            ? null
            : (EmailAlertForAssignment)EOUtilities.localInstanceOfObject(
                editingContext, eo);
    }


    // ----------------------------------------------------------
    /**
     * Look up an object by id number.  Assumes the editing
     * context is appropriately locked.
     * @param ec The editing context to use
     * @param id The id to look up
     * @return The object, or null if no such id exists
     */
    public static EmailAlertForAssignment forId(
        EOEditingContext ec, int id)
    {
        EmailAlertForAssignment obj = null;
        if (id > 0)
        {
            NSArray<EmailAlertForAssignment> objects =
                objectsMatchingValues(ec, "id", new Integer(id));
            if (objects != null && objects.count() > 0)
            {
                obj = objects.objectAtIndex(0);
            }
        }
        return obj;
    }


    // ----------------------------------------------------------
    /**
     * Look up an object by id number.  Assumes the editing
     * context is appropriately locked.
     * @param ec The editing context to use
     * @param id The id to look up
     * @return The object, or null if no such id exists
     */
    public static EmailAlertForAssignment forId(
        EOEditingContext ec, String id)
    {
        return forId(ec, er.extensions.foundation.ERXValueUtilities.intValue(id));
    }


    //~ Constants (for key names) .............................................

    // Attributes ---
    public static final String ALERT_NO_KEY = "alertNo";
    public static final ERXKey<Integer> alertNo =
        new ERXKey<Integer>(ALERT_NO_KEY);
    public static final String TIME_BEFORE_DUE_KEY = "timeBeforeDue";
    public static final ERXKey<Long> timeBeforeDue =
        new ERXKey<Long>(TIME_BEFORE_DUE_KEY);
    // To-one relationships ---
    public static final String ALERT_GROUP_KEY = "alertGroup";
    public static final ERXKey<org.webcat.schedulesheets.EmailAlertGroupForAssignment> alertGroup =
        new ERXKey<org.webcat.schedulesheets.EmailAlertGroupForAssignment>(ALERT_GROUP_KEY);
    // To-many relationships ---
    public static final String OFFERINGS_KEY = "offerings";
    public static final ERXKey<org.webcat.schedulesheets.EmailAlertForAssignmentOffering> offerings =
        new ERXKey<org.webcat.schedulesheets.EmailAlertForAssignmentOffering>(OFFERINGS_KEY);
    // Fetch specifications ---
    public static final String ENTITY_NAME = "EmailAlertForAssignment";

    public transient final EOBasedKeyGenerator generateKey =
        new EOBasedKeyGenerator(this);


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Get a local instance of this object in another editing context.
     * @param editingContext The target editing context
     * @return An instance of this object in the target editing context
     */
    public EmailAlertForAssignment localInstance(EOEditingContext editingContext)
    {
        return (EmailAlertForAssignment)EOUtilities.localInstanceOfObject(
            editingContext, this);
    }


    // ----------------------------------------------------------
    /**
     * Get a list of changes between this object's current state and the
     * last committed version.
     * @return a dictionary of the changes that have not yet been committed
     */
    @SuppressWarnings("unchecked")
    public NSDictionary<String, Object> changedProperties()
    {
        return changesFromSnapshot(
            editingContext().committedSnapshotForObject(this));
    }


    // ----------------------------------------------------------
    /**
     * Retrieve this object's <code>id</code> value.
     * @return the value of the attribute
     */
    public Number id()
    {
        try
        {
            return (Number)EOUtilities.primaryKeyForObject(
                editingContext() , this).objectForKey("id");
        }
        catch (Exception e)
        {
            return er.extensions.eof.ERXConstant.ZeroInteger;
        }
    }

    // ----------------------------------------------------------
    /**
     * Retrieve this object's <code>alertNo</code> value.
     * @return the value of the attribute
     */
    public int alertNo()
    {
        Integer returnValue =
            (Integer)storedValueForKey( "alertNo" );
        return ( returnValue == null )
            ? 0
            : returnValue.intValue();
    }


    // ----------------------------------------------------------
    /**
     * Change the value of this object's <code>alertNo</code>
     * property.
     *
     * @param value The new value for this property
     */
    public void setAlertNo( int value )
    {
        if (log.isDebugEnabled())
        {
            log.debug( "setAlertNo("
                + value + "): was " + alertNo() );
        }
        Integer actual =
            er.extensions.eof.ERXConstant.integerForInt( value );
            setAlertNoRaw( actual );
    }


    // ----------------------------------------------------------
    /**
     * Retrieve this object's <code>alertNo</code> value.
     * @return the value of the attribute
     */
    public Integer alertNoRaw()
    {
        return (Integer)storedValueForKey( "alertNo" );
    }


    // ----------------------------------------------------------
    /**
     * Change the value of this object's <code>alertNo</code>
     * property.
     *
     * @param value The new value for this property
     */
    public void setAlertNoRaw( Integer value )
    {
        if (log.isDebugEnabled())
        {
            log.debug( "setAlertNoRaw("
                + value + "): was " + alertNoRaw() );
        }
        takeStoredValueForKey( value, "alertNo" );
    }


    // ----------------------------------------------------------
    /**
     * Retrieve this object's <code>timeBeforeDue</code> value.
     * @return the value of the attribute
     */
    public long timeBeforeDue()
    {
        Long returnValue =
            (Long)storedValueForKey( "timeBeforeDue" );
        return ( returnValue == null )
            ? 0L
            : returnValue.longValue();
    }


    // ----------------------------------------------------------
    /**
     * Change the value of this object's <code>timeBeforeDue</code>
     * property.
     *
     * @param value The new value for this property
     */
    public void setTimeBeforeDue( long value )
    {
        if (log.isDebugEnabled())
        {
            log.debug( "setTimeBeforeDue("
                + value + "): was " + timeBeforeDue() );
        }
        Long actual =
            new Long( value );
            setTimeBeforeDueRaw( actual );
    }


    // ----------------------------------------------------------
    /**
     * Retrieve this object's <code>timeBeforeDue</code> value.
     * @return the value of the attribute
     */
    public Long timeBeforeDueRaw()
    {
        return (Long)storedValueForKey( "timeBeforeDue" );
    }


    // ----------------------------------------------------------
    /**
     * Change the value of this object's <code>timeBeforeDue</code>
     * property.
     *
     * @param value The new value for this property
     */
    public void setTimeBeforeDueRaw( Long value )
    {
        if (log.isDebugEnabled())
        {
            log.debug( "setTimeBeforeDueRaw("
                + value + "): was " + timeBeforeDueRaw() );
        }
        takeStoredValueForKey( value, "timeBeforeDue" );
    }


    // ----------------------------------------------------------
    /**
     * Retrieve the entity pointed to by the <code>alertGroup</code>
     * relationship.
     * @return the entity in the relationship
     */
    public org.webcat.schedulesheets.EmailAlertGroupForAssignment alertGroup()
    {
        return (org.webcat.schedulesheets.EmailAlertGroupForAssignment)storedValueForKey( "alertGroup" );
    }


    // ----------------------------------------------------------
    /**
     * Set the entity pointed to by the <code>alertGroup</code>
     * relationship (DO NOT USE--instead, use
     * <code>setAlertGroupRelationship()</code>.
     * This method is provided for WebObjects use.
     *
     * @param value The new entity to relate to
     */
    public void setAlertGroup( org.webcat.schedulesheets.EmailAlertGroupForAssignment value )
    {
        if (log.isDebugEnabled())
        {
            log.debug( "setAlertGroup("
                + value + "): was " + alertGroup() );
        }
        takeStoredValueForKey( value, "alertGroup" );
    }


    // ----------------------------------------------------------
    /**
     * Set the entity pointed to by the <code>alertGroup</code>
     * relationship.  This method is a type-safe version of
     * <code>addObjectToBothSidesOfRelationshipWithKey()</code>.
     *
     * @param value The new entity to relate to
     */
    public void setAlertGroupRelationship(
        org.webcat.schedulesheets.EmailAlertGroupForAssignment value )
    {
        if (log.isDebugEnabled())
        {
            log.debug( "setAlertGroupRelationship("
                + value + "): was " + alertGroup() );
        }
        if ( value == null )
        {
            org.webcat.schedulesheets.EmailAlertGroupForAssignment object = alertGroup();
            if ( object != null )
                removeObjectFromBothSidesOfRelationshipWithKey( object, "alertGroup" );
        }
        else
        {
            addObjectToBothSidesOfRelationshipWithKey( value, "alertGroup" );
        }
    }


    // ----------------------------------------------------------
    /**
     * Retrieve the entities pointed to by the <code>offerings</code>
     * relationship.
     * @return an NSArray of the entities in the relationship
     */
    @SuppressWarnings("unchecked")
    public NSArray<org.webcat.schedulesheets.EmailAlertForAssignmentOffering> offerings()
    {
        return (NSArray<org.webcat.schedulesheets.EmailAlertForAssignmentOffering>)
            storedValueForKey("offerings");
    }


    // ----------------------------------------------------------
    /**
     * Replace the list of entities pointed to by the
     * <code>offerings</code> relationship.
     *
     * @param value The new set of entities to relate to
     */
    public void setOfferings(
        NSMutableArray<org.webcat.schedulesheets.EmailAlertForAssignmentOffering>  value)
    {
        if (log.isDebugEnabled())
        {
            log.debug("setOfferings("
                + value + "): was " + offerings());
        }
        takeStoredValueForKey(value, "offerings");
    }


    // ----------------------------------------------------------
    /**
     * Add a new entity to the <code>offerings</code>
     * relationship (DO NOT USE--instead, use
     * <code>addToOfferingsRelationship()</code>.
     * This method is provided for WebObjects use.
     *
     * @param value The new entity to relate to
     */
    public void addToOfferings( org.webcat.schedulesheets.EmailAlertForAssignmentOffering value )
    {
        if (log.isDebugEnabled())
        {
            log.debug( "addToOfferings("
                + value + "): was " + offerings() );
        }
        NSMutableArray<org.webcat.schedulesheets.EmailAlertForAssignmentOffering> array =
            (NSMutableArray<org.webcat.schedulesheets.EmailAlertForAssignmentOffering>)offerings();
        willChange();
        array.addObject( value );
    }


    // ----------------------------------------------------------
    /**
     * Remove a specific entity from the <code>offerings</code>
     * relationship (DO NOT USE--instead, use
     * <code>removeFromOfferingsRelationship()</code>.
     * This method is provided for WebObjects use.
     *
     * @param value The entity to remove from the relationship
     */
    public void removeFromOfferings( org.webcat.schedulesheets.EmailAlertForAssignmentOffering value )
    {
        if (log.isDebugEnabled())
        {
            log.debug( "RemoveFromOfferings("
                + value + "): was " + offerings() );
        }
        NSMutableArray<org.webcat.schedulesheets.EmailAlertForAssignmentOffering> array =
            (NSMutableArray<org.webcat.schedulesheets.EmailAlertForAssignmentOffering>)offerings();
        willChange();
        array.removeObject( value );
    }


    // ----------------------------------------------------------
    /**
     * Add a new entity to the <code>offerings</code>
     * relationship.
     *
     * @param value The new entity to relate to
     */
    public void addToOfferingsRelationship( org.webcat.schedulesheets.EmailAlertForAssignmentOffering value )
    {
        if (log.isDebugEnabled())
        {
            log.debug( "addToOfferingsRelationship("
                + value + "): was " + offerings() );
        }
        addObjectToBothSidesOfRelationshipWithKey(
            value, "offerings" );
    }


    // ----------------------------------------------------------
    /**
     * Remove a specific entity from the <code>offerings</code>
     * relationship.
     *
     * @param value The entity to remove from the relationship
     */
    public void removeFromOfferingsRelationship( org.webcat.schedulesheets.EmailAlertForAssignmentOffering value )
    {
        if (log.isDebugEnabled())
        {
            log.debug( "removeFromOfferingsRelationship("
                + value + "): was " + offerings() );
        }
        removeObjectFromBothSidesOfRelationshipWithKey(
            value, "offerings" );
    }


    // ----------------------------------------------------------
    /**
     * Create a brand new object that is a member of the
     * <code>offerings</code> relationship.
     *
     * @return The new entity
     */
    public org.webcat.schedulesheets.EmailAlertForAssignmentOffering createOfferingsRelationship()
    {
        if (log.isDebugEnabled())
        {
            log.debug( "createOfferingsRelationship()" );
        }
        EOClassDescription eoClassDesc = EOClassDescription
            .classDescriptionForEntityName( "EmailAlertForAssignmentOffering" );
        EOEnterpriseObject eoObject = eoClassDesc
            .createInstanceWithEditingContext( editingContext(), null );
        editingContext().insertObject( eoObject );
        addObjectToBothSidesOfRelationshipWithKey(
            eoObject, "offerings" );
        return (org.webcat.schedulesheets.EmailAlertForAssignmentOffering)eoObject;
    }


    // ----------------------------------------------------------
    /**
     * Remove and then delete a specific entity that is a member of the
     * <code>offerings</code> relationship.
     *
     * @param value The entity to remove from the relationship and then delete
     */
    public void deleteOfferingsRelationship( org.webcat.schedulesheets.EmailAlertForAssignmentOffering value )
    {
        if (log.isDebugEnabled())
        {
            log.debug( "deleteOfferingsRelationship("
                + value + "): was " + offerings() );
        }
        removeObjectFromBothSidesOfRelationshipWithKey(
            value, "offerings" );
        editingContext().deleteObject( value );
    }


    // ----------------------------------------------------------
    /**
     * Remove (and then delete, if owned) all entities that are members of the
     * <code>offerings</code> relationship.
     */
    public void deleteAllOfferingsRelationships()
    {
        if (log.isDebugEnabled())
        {
            log.debug( "deleteAllOfferingsRelationships(): was "
                + offerings() );
        }
        for (org.webcat.schedulesheets.EmailAlertForAssignmentOffering object : offerings())
        {
            deleteOfferingsRelationship(object);
        }
    }


    // ----------------------------------------------------------
    /**
     * Retrieve objects using a fetch specification.
     *
     * @param context The editing context to use
     * @param fspec The fetch specification to use
     *
     * @return an NSArray of the entities retrieved
     */
    @SuppressWarnings("unchecked")
    public static NSArray<EmailAlertForAssignment> objectsWithFetchSpecification(
        EOEditingContext context,
        EOFetchSpecification fspec)
    {
        return context.objectsWithFetchSpecification(fspec);
    }


    // ----------------------------------------------------------
    /**
     * Retrieve all objects of this type.
     *
     * @param context The editing context to use
     *
     * @return an NSArray of the entities retrieved
     */
    public static NSArray<EmailAlertForAssignment> allObjects(
        EOEditingContext context)
    {
        return objectsMatchingQualifier(context, null, null);
    }


    // ----------------------------------------------------------
    /**
     * Retrieve objects using a qualifier.
     *
     * @param context The editing context to use
     * @param qualifier The qualifier to use
     *
     * @return an NSArray of the entities retrieved
     */
    public static NSArray<EmailAlertForAssignment> objectsMatchingQualifier(
        EOEditingContext context,
        EOQualifier qualifier)
    {
        return objectsMatchingQualifier(context, qualifier, null);
    }


    // ----------------------------------------------------------
    /**
     * Retrieve objects using a qualifier and sort orderings.
     *
     * @param context The editing context to use
     * @param qualifier The qualifier to use
     * @param sortOrderings The sort orderings to use
     *
     * @return an NSArray of the entities retrieved
     */
    public static NSArray<EmailAlertForAssignment> objectsMatchingQualifier(
        EOEditingContext context,
        EOQualifier qualifier,
        NSArray<EOSortOrdering> sortOrderings)
    {
        WCFetchSpecification<EmailAlertForAssignment> fspec =
            new WCFetchSpecification<EmailAlertForAssignment>(
                ENTITY_NAME, qualifier, sortOrderings);
        fspec.setUsesDistinct(true);
        return objectsWithFetchSpecification(context, fspec);
    }


    // ----------------------------------------------------------
    /**
     * Retrieve the first object that matches a qualifier, when
     * sorted with the specified sort orderings.
     *
     * @param context The editing context to use
     * @param qualifier The qualifier to use
     * @param sortOrderings the sort orderings
     *
     * @return the first entity that was retrieved, or null if there was none
     */
    public static EmailAlertForAssignment firstObjectMatchingQualifier(
        EOEditingContext context,
        EOQualifier qualifier,
        NSArray<EOSortOrdering> sortOrderings)
    {
        WCFetchSpecification<EmailAlertForAssignment> fspec =
            new WCFetchSpecification<EmailAlertForAssignment>(
                ENTITY_NAME, qualifier, sortOrderings);
        fspec.setUsesDistinct(true);
        fspec.setFetchLimit(1);
        NSArray<EmailAlertForAssignment> objects =
            objectsWithFetchSpecification(context, fspec);
        return (objects.size() > 0)
            ? objects.get(0)
            : null;
    }


    // ----------------------------------------------------------
    /**
     * Retrieve a single object using a list of keys and values to match.
     *
     * @param context The editing context to use
     * @param qualifier The qualifier to use
     *
     * @return the single entity that was retrieved
     *
     * @throws EOUtilities.MoreThanOneException
     *     if there is more than one matching object
     */
    public static EmailAlertForAssignment uniqueObjectMatchingQualifier(
        EOEditingContext context,
        EOQualifier qualifier) throws EOUtilities.MoreThanOneException
    {
        NSArray<EmailAlertForAssignment> objects =
            objectsMatchingQualifier(context, qualifier);
        if (objects.size() > 1)
        {
            String msg = "fetching EmailAlertForAssignment";
            try
            {
                if (qualifier != null)
                {
                    msg += " where " + qualifier;
                }
                msg += ", result = " + objects;
            }
            catch (Exception e)
            {
                log.error("Exception building MoreThanOneException message, "
                    + "contents so far: " + msg, e);
            }
            throw new EOUtilities.MoreThanOneException(msg);
        }
        return (objects.size() > 0)
            ? objects.get(0)
            : null;
    }


    // ----------------------------------------------------------
    /**
     * Retrieve objects using a list of keys and values to match.
     *
     * @param context The editing context to use
     * @param keysAndValues a list of keys and values to match, alternating
     *     "key", "value", "key", "value"...
     *
     * @return an NSArray of the entities retrieved
     */
    public static NSArray<EmailAlertForAssignment> objectsMatchingValues(
        EOEditingContext context,
        Object... keysAndValues)
    {
        if (keysAndValues.length % 2 != 0)
        {
            throw new IllegalArgumentException("There should a value "
                + "corresponding to every key that was passed. Args = "
                + java.util.Arrays.toString(keysAndValues));
        }

        NSMutableDictionary<String, Object> valueDictionary =
            new NSMutableDictionary<String, Object>();

        for (int i = 0; i < keysAndValues.length; i += 2)
        {
            Object key = keysAndValues[i];
            Object value = keysAndValues[i + 1];

            if (key == null)
            {
                throw new IllegalArgumentException(
                    "Found null where a String key was expected, arguments = "
                    + java.util.Arrays.toString(keysAndValues));
            }
            else if (!(key instanceof String))
            {
                throw new IllegalArgumentException(
                    "Found a " + key.getClass().getName() + " [" + key + "]"
                    + " where a String key was expected, arguments = "
                    + java.util.Arrays.toString(keysAndValues));
            }

            valueDictionary.setObjectForKey(value, (String)key);
        }

        return objectsMatchingValues(context, valueDictionary);
    }


    // ----------------------------------------------------------
    /**
     * Retrieve objects using a dictionary of keys and values to match.
     *
     * @param context The editing context to use
     * @param keysAndValues a dictionary of keys and values to match
     *
     * @return an NSArray of the entities retrieved
     */
    @SuppressWarnings("unchecked")
    public static NSArray<EmailAlertForAssignment> objectsMatchingValues(
        EOEditingContext context,
        NSDictionary<String, Object> keysAndValues)
    {
        return EOUtilities.objectsMatchingValues(context, ENTITY_NAME,
            keysAndValues);
    }


    // ----------------------------------------------------------
    /**
     * Retrieve the first object that matches a set of keys and values, when
     * sorted with the specified sort orderings.
     *
     * @param context The editing context to use
     * @param sortOrderings the sort orderings
     * @param keysAndValues a list of keys and values to match, alternating
     *     "key", "value", "key", "value"...
     *
     * @return the first entity that was retrieved, or null if there was none
     */
    public static EmailAlertForAssignment firstObjectMatchingValues(
        EOEditingContext context,
        NSArray<EOSortOrdering> sortOrderings,
        Object... keysAndValues)
    {
        if (keysAndValues.length % 2 != 0)
        {
            throw new IllegalArgumentException("There should a value "
                + "corresponding to every key that was passed. Args = "
                + java.util.Arrays.toString(keysAndValues));
        }

        NSMutableDictionary<String, Object> valueDictionary =
            new NSMutableDictionary<String, Object>();

        for (int i = 0; i < keysAndValues.length; i += 2)
        {
            Object key = keysAndValues[i];
            Object value = keysAndValues[i + 1];

            if (key == null)
            {
                throw new IllegalArgumentException(
                    "Found null where a String key was expected, arguments = "
                    + java.util.Arrays.toString(keysAndValues));
            }
            else if (!(key instanceof String))
            {
                throw new IllegalArgumentException(
                    "Found a " + key.getClass().getName() + " [" + key + "]"
                    + " where a String key was expected, arguments = "
                    + java.util.Arrays.toString(keysAndValues));
            }

            valueDictionary.setObjectForKey(value, (String)key);
        }

        return firstObjectMatchingValues(
            context, sortOrderings, valueDictionary);
    }


    // ----------------------------------------------------------
    /**
     * Retrieves the first object that matches a set of keys and values, when
     * sorted with the specified sort orderings.
     *
     * @param context The editing context to use
     * @param sortOrderings the sort orderings
     * @param keysAndValues a dictionary of keys and values to match
     *
     * @return the first entity that was retrieved, or null if there was none
     */
    public static EmailAlertForAssignment firstObjectMatchingValues(
        EOEditingContext context,
        NSArray<EOSortOrdering> sortOrderings,
        NSDictionary<String, Object> keysAndValues)
    {
        WCFetchSpecification<EmailAlertForAssignment> fspec =
            new WCFetchSpecification<EmailAlertForAssignment>(
                ENTITY_NAME,
                EOQualifier.qualifierToMatchAllValues(keysAndValues),
                sortOrderings);
        fspec.setFetchLimit(1);

        NSArray<EmailAlertForAssignment> objects =
            objectsWithFetchSpecification( context, fspec );

        if ( objects.count() == 0 )
        {
            return null;
        }
        else
        {
            return objects.objectAtIndex(0);
        }
    }


    // ----------------------------------------------------------
    /**
     * Retrieve a single object using a list of keys and values to match.
     *
     * @param context The editing context to use
     * @param keysAndValues a list of keys and values to match, alternating
     *     "key", "value", "key", "value"...
     *
     * @return the single entity that was retrieved, or null if there was none
     *
     * @throws EOUtilities.MoreThanOneException
     *     if there is more than one matching object
     */
    public static EmailAlertForAssignment uniqueObjectMatchingValues(
        EOEditingContext context,
        Object... keysAndValues) throws EOUtilities.MoreThanOneException
    {
        if (keysAndValues.length % 2 != 0)
        {
            throw new IllegalArgumentException("There should a value "
                + "corresponding to every key that was passed. Args = "
                + java.util.Arrays.toString(keysAndValues));
        }

        NSMutableDictionary<String, Object> valueDictionary =
            new NSMutableDictionary<String, Object>();

        for (int i = 0; i < keysAndValues.length; i += 2)
        {
            Object key = keysAndValues[i];
            Object value = keysAndValues[i + 1];

            if (key == null)
            {
                throw new IllegalArgumentException(
                    "Found null where a String key was expected, arguments = "
                    + java.util.Arrays.toString(keysAndValues));
            }
            else if (!(key instanceof String))
            {
                throw new IllegalArgumentException(
                    "Found a " + key.getClass().getName() + " [" + key + "]"
                    + " where a String key was expected, arguments = "
                    + java.util.Arrays.toString(keysAndValues));
            }

            valueDictionary.setObjectForKey(value, (String)key);
        }

        return uniqueObjectMatchingValues(context, valueDictionary);
    }


    // ----------------------------------------------------------
    /**
     * Retrieve an object using a dictionary of keys and values to match.
     *
     * @param context The editing context to use
     * @param keysAndValues a dictionary of keys and values to match
     *
     * @return the single entity that was retrieved, or null if there was none
     *
     * @throws EOUtilities.MoreThanOneException
     *     if there is more than one matching object
     */
    public static EmailAlertForAssignment uniqueObjectMatchingValues(
        EOEditingContext context,
        NSDictionary<String, Object> keysAndValues)
        throws EOUtilities.MoreThanOneException
    {
        try
        {
            return (EmailAlertForAssignment)EOUtilities.objectMatchingValues(
                context, ENTITY_NAME, keysAndValues);
        }
        catch (EOObjectNotAvailableException e)
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    /**
     * Retrieve the count of all objects of this type.
     *
     * @param context The editing context to use
     *
     * @return the count of all objects
     */
    public static int countOfAllObjects(EOEditingContext context)
    {
        return countOfObjectsMatchingQualifier(context, null);
    }


    // ----------------------------------------------------------
    /**
     * Retrieve the count of objects that match a qualifier.
     *
     * @param context The editing context to use
     * @param qualifier The qualifier to use
     *
     * @return the count of objects matching the qualifier
     */
    public static int countOfObjectsMatchingQualifier(
        EOEditingContext context, EOQualifier qualifier)
    {
        return ERXEOControlUtilities.objectCountWithQualifier(
                context, ENTITY_NAME, qualifier);
    }


    // ----------------------------------------------------------
    /**
     * Retrieve the count of objects using a list of keys and values to match.
     *
     * @param context The editing context to use
     * @param keysAndValues a list of keys and values to match, alternating
     *     "key", "value", "key", "value"...
     *
     * @return the count of objects that match the specified values
     */
    public static int countOfObjectsMatchingValues(
        EOEditingContext context,
        Object... keysAndValues)
    {
        if (keysAndValues.length % 2 != 0)
        {
            throw new IllegalArgumentException("There should a value "
                + "corresponding to every key that was passed. Args = "
                + java.util.Arrays.toString(keysAndValues));
        }

        NSMutableDictionary<String, Object> valueDictionary =
            new NSMutableDictionary<String, Object>();

        for (int i = 0; i < keysAndValues.length; i += 2)
        {
            Object key = keysAndValues[i];
            Object value = keysAndValues[i + 1];

            if (key == null)
            {
                throw new IllegalArgumentException(
                    "Found null where a String key was expected, arguments = "
                    + java.util.Arrays.toString(keysAndValues));
            }
            else if (!(key instanceof String))
            {
                throw new IllegalArgumentException(
                    "Found a " + key.getClass().getName() + " [" + key + "]"
                    + " where a String key was expected, arguments = "
                    + java.util.Arrays.toString(keysAndValues));
            }

            valueDictionary.setObjectForKey(value, (String)key);
        }

        return countOfObjectsMatchingValues(context, valueDictionary);
    }


    // ----------------------------------------------------------
    /**
     * Retrieve the count of objects using a dictionary of keys and values to
     * match.
     *
     * @param context The editing context to use
     * @param keysAndValues a dictionary of keys and values to match
     *
     * @return the count of objects that matched the specified values
     */
    public static int countOfObjectsMatchingValues(
        EOEditingContext context,
        NSDictionary<String, Object> keysAndValues)
    {
        return countOfObjectsMatchingQualifier(context,
                EOQualifier.qualifierToMatchAllValues(keysAndValues));
    }


    // ----------------------------------------------------------
    /**
     * Produce a string representation of this object.  This implementation
     * calls UserPresentableDescription(), which uses WebObjects' internal
     * mechanism to print out the visible fields of this object.  Normally,
     * subclasses would override userPresentableDescription() to change
     * the way the object is printed.
     *
     * @return A string representation of the object's value
     */
    public String toString()
    {
        return userPresentableDescription();
    }


    //~ Instance/static variables .............................................

    static Logger log = Logger.getLogger(EmailAlertForAssignment.class);
}
