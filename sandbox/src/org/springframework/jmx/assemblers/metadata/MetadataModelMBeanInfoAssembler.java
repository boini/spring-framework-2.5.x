/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.jmx.assemblers.metadata;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import javax.management.Descriptor;

import org.springframework.jmx.assemblers.AbstractReflectionBasedModelMBeanInfoAssembler;
import org.springframework.jmx.assemblers.AutodetectCapableModelMBeanInfoAssembler;
import org.springframework.jmx.metadata.support.JmxAttributeSource;
import org.springframework.jmx.metadata.support.ManagedAttribute;
import org.springframework.jmx.metadata.support.ManagedOperation;
import org.springframework.jmx.metadata.support.ManagedResource;
import org.springframework.jmx.metadata.support.commons.CommonsAttributesJmxAttributeSource;
import org.springframework.jmx.util.JmxUtils;

/**
 * Implementation of <tt>ModelMBeanInfoAssembler</tt> that reads the
 * management interface information from source level metadata. Uses Spring's
 * metadata abstraction layer so that metadata can be read using any supported
 * implementation.
 * 
 * @author Rob Harrop
 */
public class MetadataModelMBeanInfoAssembler extends
        AbstractReflectionBasedModelMBeanInfoAssembler implements
        AutodetectCapableModelMBeanInfoAssembler {

    private static final String LOG = "log";

    private static final String LOG_FILE = "logFile";

    private static final String CURRENCY_TIME_LIMIT = "currencyTimeLimit";
    
    private static final String DEFAULT = "default";
    
    private static final String PERSIST_POLICY = "persistPolicy";
    
    private static final String PERSIST_PERIOD = "persistPeriod";
    
    private static final String PERSIST_LOCATION = "persistLocation";
    
    private static final String PERSIST_NAME = "persistName";

  
    private JmxAttributeSource attributeSource = new CommonsAttributesJmxAttributeSource();


    public void setAttributeSource(JmxAttributeSource attributeSource) {
        this.attributeSource = attributeSource;
    }

    protected boolean includeReadAttribute(Method method) {
        return hasManagedAttribute(method);
    }

    protected boolean includeWriteAttribute(Method method) {
        return hasManagedAttribute(method);
    }

    protected boolean includeOperation(Method method) {
        if (JmxUtils.isProperty(method)) {
            return hasManagedAttribute(method);
        } else {
            return hasManagedOperation(method);
        }
    }

    protected String getOperationDescription(Method method) {

        if (JmxUtils.isProperty(method)) {
            ManagedAttribute ma = attributeSource.getManagedAttribute(method);

            if (ma == null) {
                return method.getName();
            } else {
                return ma.getDescription();
            }
        } else {
            ManagedOperation mo = attributeSource.getManagedOperation(method);

            if (mo == null) {
                return method.getName();
            } else {
                return mo.getDescription();
            }
        }
    }

    /**
     * Creates a description for the attribute corresponding to this property
     * descriptor. Attempts to create the description using metadata from either
     * the getter or setter attributes, otherwise uses the property name.
     * 
     * @param propertyDescriptor
     * @return
     */
    protected String getAttributeDescription(
            PropertyDescriptor propertyDescriptor) {

        Method readMethod = propertyDescriptor.getReadMethod();
        Method writeMethod = propertyDescriptor.getWriteMethod();

        ManagedAttribute getter = (readMethod != null) ? attributeSource.getManagedAttribute(readMethod) : null;
        ManagedAttribute setter = (writeMethod != null) ? attributeSource.getManagedAttribute(writeMethod) : null;

        StringBuffer sb = new StringBuffer();

        if ((getter != null) && (getter.getDescription() != null)
                && (getter.getDescription().length() > 0)) {
            return getter.getDescription();
        } else if ((setter != null) && (setter.getDescription() != null)
                && (setter.getDescription().length() > 0)) {
            return setter.getDescription();
        } else {
            return propertyDescriptor.getDisplayName();
        }
    }

    /**
     * Attempts to read managed resource description from the source level
     * metdata. Returns an empty <code>String</code> if no description can be
     * found.
     */
    protected String getDescription(String beanKey, Class beanClass) {
        ManagedResource mr = attributeSource.getManagedResource(beanClass);

        if (mr == null) {
            return "";
        } else {
            return mr.getDescription();
        }
    }

    protected void populateMBeanDescriptor(Descriptor mbeanDescriptor, String beanKey, Class beanClass) {
        ManagedResource mr = attributeSource.getManagedResource(beanClass);

        mbeanDescriptor.setField(LOG, mr.isLog() ? "true" : "false");

        if (mr.getLogFile() != null) {
            mbeanDescriptor.setField(LOG_FILE, mr.getLogFile());
        }

        mbeanDescriptor.setField(CURRENCY_TIME_LIMIT, new Integer(mr
                .getCurrencyTimeLimit()));
        
        mbeanDescriptor.setField(PERSIST_POLICY, mr.getPersistPolicy());
        
        mbeanDescriptor.setField(PERSIST_PERIOD, new Integer(mr.getPersistPeriod()));
        
        mbeanDescriptor.setField(PERSIST_LOCATION, mr.getPersistLocation());
        
        mbeanDescriptor.setField(PERSIST_NAME, mr.getPersistName());
    }

    protected void populateAttributeDescriptor(Descriptor descriptor,
            Method getter, Method setter) {

        ManagedAttribute gma = (getter == null) ? ManagedAttribute.EMPTY : attributeSource.getManagedAttribute(getter);
        
        ManagedAttribute sma = (setter == null) ? ManagedAttribute.EMPTY : attributeSource.getManagedAttribute(setter);


        int ctl = resolveIntDescriptor(gma.getCurrencyTimeLimit(), sma.getCurrencyTimeLimit());
        descriptor.setField(CURRENCY_TIME_LIMIT, new Integer(ctl));
        
        Object defaultValue = resolveObjectDescriptor(gma.getDefaultValue(), sma.getDefaultValue());
        descriptor.setField(DEFAULT, defaultValue);
        
        String persistPolicy = resolveStringDescriptor(gma.getPersistPolicy(), sma.getPersistPolicy(), "Never");
        descriptor.setField(PERSIST_POLICY, persistPolicy);
        
        int persistPeriod = resolveIntDescriptor(gma.getPersistPeriod(), sma.getPersistPeriod());
        descriptor.setField(PERSIST_PERIOD, new Integer(persistPeriod));
            
    }
    
    /**
     * Adds the <code>currencyTimeLimit</code> field to the supplied
     * <code>Descriptor</code> using the value provided in the metadata
     * for the supplied <code>Method</code>.
     */
    protected void populateOperationDescriptor(Descriptor descriptor,
            Method method) {
        ManagedOperation mo = attributeSource.getManagedOperation(method);
        
        if(mo != null) {
            descriptor.setField(CURRENCY_TIME_LIMIT, new Integer(mo.getCurrencyTimeLimit()));
        }
    }
    
    
    /**
     * Determines which of two <code>int</code> values
     * should be used as the value for an attribute descriptor.
     * In general only the getter or the setter will be have a non-zero
     * value so we use that value. In the event that both values
     * are non-zero we use the greater of the two. This method can be used
     * to resolve any <code>int</code> valued descriptor where there are two
     * possible values.
     * @param getter the <code>int</code> value associated with the getter for this attribute.
     * @param setter the <code>int</code> value associated with the setter for this attribute.
     */
    private int resolveIntDescriptor(int getter, int setter) {
        if(getter == 0 && setter != 0) {
            return setter;
        } else if(setter == 0 && getter != 0) {
            return getter;
        } else {
            return (getter >= setter) ? getter : setter;
        }
    }
    
    /**
     * Locates the value of a descriptor based on values attached
     * to both the getter and setter methods. If both have values
     * supplied then the value attached to the getter is preferred.
     * @param getter the <code>Object</code> value associated with the get method.
     * @param setter the <code>Object</code> value associated with the set method.
     * @return The appropriate <code>Object</code> to use as the value for the descriptor.
     */
    private Object resolveObjectDescriptor(Object getter, Object setter) {
        if(getter != null) {
            return getter;
        } else if (setter != null) {
            return setter;
        } else {
            return null;
        }
    }
    
    /**
     * Locates the value of a descriptor based on values attached
     * to both the getter and setter methods. If both have values
     * supplied then the value attached to the getter is preferred.
     * The supplied default value is used to check to see if the value
     * associated with the getter has changed from the default.
     * @param getter the <code>String</code> value associated with the get method.
     * @param setter the <code>String</code> value associated with the set method.
     * @param defaultValue the <code>String</code> valued default associated with this descriptor.
     * @return The appropriate <code>String</code> to use as the value for the descriptor.
     */
    private String resolveStringDescriptor(String getter, String setter, String defaultValue) {
        if(getter != null && !defaultValue.equals(getter)) {
            return getter;
        } else if (setter != null) {
            return setter;
        } else {
            return null;
        }
    }

    private boolean hasManagedAttribute(Method method) {
        ManagedAttribute ma = attributeSource.getManagedAttribute(method);

        return (ma != null) ? true : false;
    }

    private boolean hasManagedOperation(Method method) {
        ManagedOperation mo = attributeSource.getManagedOperation(method);

        return (mo != null) ? true : false;
    }

    /**
     * Used for auto detection of beans. Checks to see if the bean's class has a
     * ManagedResource attribute. If so it will add it list of included beans
     */
    public boolean includeBean(String beanName, Class beanClass) {
        if (attributeSource.getManagedResource(beanClass) != null) {
            return true;
        } else {
            return false;
        }
    }
}