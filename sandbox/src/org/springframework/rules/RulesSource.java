/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.rules;

import org.springframework.rules.predicates.beans.*;

/**
 * Rules sources provide a mechanism for accessing rules associated with a bean
 * class and specific bean properties. A rules source is effectively a "Rules"
 * data access object.
 * 
 * @author Keith Donald
 * @TODO move to validation package at some point...
 */
public interface RulesSource {

    /**
     * Return the validation rules for the provided bean.
     * 
     * @param beanClass
     *            the bean class
     * @return The validation rules, or <code>null</code> if none exist.
     */
    public Rules getRules(Class bean);

    /**
     * Return the validation rules for the provided bean property.
     * 
     * @param beanClass
     *            the bean class
     * @param propertyName
     *            the bean propertyName
     * @return The validation rules, or <code>null</code> if none exist.
     */
    public BeanPropertyExpression getRules(Class beanClass, String propertyName);

    //@TODO public Rules getRules(String context, Class bean);

}