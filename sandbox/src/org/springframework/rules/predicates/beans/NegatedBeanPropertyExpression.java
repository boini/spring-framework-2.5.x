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
package org.springframework.rules.predicates.beans;

import org.springframework.rules.predicates.UnaryNot;

/**
 * @author Keith Donald
 */
public class NegatedBeanPropertyExpression extends UnaryNot implements
        BeanPropertyExpression {
    public NegatedBeanPropertyExpression(BeanPropertyExpression e) {
        super(e);
    }
    
    /**
     * @see org.springframework.rules.predicates.beans.BeanPropertyExpression#getPropertyName()
     */
    public String getPropertyName() {
        return ((BeanPropertyExpression)super.getPredicate()).getPropertyName();
    }
}