/*
 * Copyright 2004 The Apache Software Foundation.
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
package org.apache.struts.validator;

import java.util.List;
import java.util.Map;
import java.lang.reflect.Array;
import java.io.Serializable;
import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionMapping;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.WrapDynaBean;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Struts <i>validator</i> <code>ActionForm</code> backed by either a <code>DynaBean</code> or POJO JavaBean.</p>
 *
 * <p>Passing a POJO JavaBean to the constructor will automatically create an associated
 *    <code>WrapDynaBean</code>. One use for this would be to migrate <i>view</i>
 *    objects from an existing system which, for the usual reasons, can't be changed to extend
 *    <ActionForm</code>.</p>
 *
 * <p>This form is based on the standard struts <code>ValidatorForm</code> for use with the
 *    <i>Validator</i> framework and validates using the <i>name</i> from the Struts
 *    <code>ActionMapping</code>.</p>
 */
public class BeanValidatorForm extends ValidatorForm implements DynaBean, Serializable {

   /**
    * Commons Logging
    */
    protected static Log logger = LogFactory.getLog(BeanValidatorForm.class);

    /**
     * The <code>DynaBean</code> that this ActionForm is backed by.
     */
    protected DynaBean dynaBean;

    /**
     * Indicates whether the ActionMapping's path should be used for the
     * validation key.
     */
    protected boolean pathValidation = false;

    // ------------------- Constructor ----------------------------------

    /**
     * Construct a new <code>BeanValidatorForm</code> with the specified bean.
     */
    public BeanValidatorForm(Object bean) {

        if (bean instanceof DynaBean) {

            dynaBean = (DynaBean)bean;

        } else {

            dynaBean = new WrapDynaBean(bean);

        }
    }

    // ------------------- Public Methods ----------------------------------

   /**
    * <p>Set whether this form should validate based on the <code>ActionMapping</code>'s path.</p>
    */
    public void setPathValidation(boolean pathValidation) {
        this.pathValidation = pathValidation;
    }

   /**
    * <p>Indicates whether this form should validate based on the <code>ActionMapping</code>'s path.</p>
    */
    public boolean isPathValidation() {
        return pathValidation;
    }

   /**
    * <p>Return the <code>DynaBean</code> that this <code>ActionForm</code> is backed by.</p>
    */
    public DynaBean getDynaBean() {
        return dynaBean;
    }

   /**
    * <p>Return the <code>Bean</code> that this <code>ActionForm</code> is backed by.</p>
    *
    * <p>If the <code>DynaBean</code> is a <code>LazyDynaMap</code> type then this method
    * returns the <code>Map</code> associated with it. If you require the actual <code>LazyDynaMap</code>
    * then use the <code>getDynaBean()</code> method.</p>
    *
    * <p>If the <code>DynaBean</code> is a <code>WrapDynaBean</code> type then this method
    * returns the 'Wrapped' POJO bean associated with it. If you require the actual <code>WrapDynaBean</code>
    * then use the <code>getDynaBean()</code> method.</p>
    */
    public Object getInstance() {

        // *** ------------------------------------------------------------ ***
        // *** WrapDynaBean.getInstance() requires BeanUtils 1.7.0 or later ***
        // *** Will implement this later.                                   ***
        // *** ------------------------------------------------------------ ***
        if (dynaBean instanceof WrapDynaBean) {
            return dynaBean;  // FIXME - return DynaBean until BeanUtils 1.7.0
            //  return ((WrapDynaBean)dynaBean).getInstance();
        }

        return dynaBean;

    }

   /**
    * <p>Return the size of an indexed or mapped property.</p>
    */
    public int size(String name) {

        Object value = dynaBean.get(name);
        if (value == null) {
            return 0;
        }

        if (value instanceof Map) {
            return ((Map)value).size();
        }

        if (value instanceof List) {
            return ((List)value).size();
        }

        if ((value.getClass().isArray())) {
            return Array.getLength(value);
        }

        return 0;

    }

    // ------------------- ValidatorForm Methods ----------------------------------

    /**
     * Returns the Validation key
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     * @return validation key to use
     */
    public String getValidationKey(ActionMapping mapping,
                                   HttpServletRequest request) {

        String validationKey = null;

        if (isPathValidation()) {

            // Get the path replacing any slashes by underscore
            validationKey = mapping.getPath();

            // Remove any leading slash
            if (validationKey.charAt(0) == '/') {
                validationKey = validationKey.substring(1);
            }

            // Replace any slashes by underscore
            if (validationKey.indexOf("/") > 0) {
                validationKey = validationKey.replace('/', '_');
            }

        } else {

            validationKey = mapping.getName();

        }

        if (logger.isDebugEnabled()) {
            logger.debug("Validating ActionForm '" + mapping.getName()+"' using key '"+validationKey+"' for mapping '"+mapping.getPath()+"'");
        }


        return validationKey;

    }

    // ------------------- DynaBean Methods ----------------------------------

   /**
    * Return the associated <code>DynaClass</code> for this <code>DynaBean</code>
    */
    public DynaClass getDynaClass() {
        return dynaBean.getDynaClass();
    }

   /**
    * Return a property value.
    */
    public Object get(String name) {
        return dynaBean.get(name);
    }

   /**
    * Return an indexed property value.
    */
    public Object get(String name, int index) {
        return dynaBean.get(name, index);
    }

   /**
    * Return a mapped property value.
    */
    public Object get(String name, String key) {
        return dynaBean.get(name, key);
    }

   /**
    * Set a property value.
    */
    public void set(String name, Object value) {

        // Set the page number (for validator)
        if ("page".equals(name)) {

            if (value == null) {
                page = 0;
            } else  if (value instanceof Integer) {
                page = ((Integer)value).intValue();
            } else {
                try {
                    page = ((Integer)ConvertUtils.convert(value.toString(), Integer.class)).intValue();
                }
                catch (Exception ignore) {
                    page = 0;
                }
            }
        }

        dynaBean.set(name, value);

    }

   /**
    * Set an indexed property value.
    */
    public void set(String name, int index, Object value) {
        dynaBean.set(name, index, value);
    }

   /**
    * Set a mapped property value.
    */
    public void set(String name, String key, Object value) {
        dynaBean.set(name, key, value);
    }

   /**
    * Indicates whether a mapped property contains a specified key.
    */
    public boolean contains(String name, String key) {
        return dynaBean.contains(name, key);
    }

   /**
    * Remove a mapped property value.
    */
    public void remove(String name, String key) {
        dynaBean.remove(name, key);
    }
}