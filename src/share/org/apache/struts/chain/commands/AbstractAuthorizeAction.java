/*
 * Copyright 2003,2004 The Apache Software Foundation.
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

package org.apache.struts.chain.commands;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.chain.contexts.ActionContext;
import org.apache.struts.config.ActionConfig;


/**
 * <p>Determine whether the requested action is authorized for the current
 * user.  If not, abort chain processing and perferably, return an error
 * message of some kind.</p>
 *
 * @author Don Brown
 * @version $Rev$ $Date$
 */

public abstract class AbstractAuthorizeAction extends ActionCommandBase {


    // ------------------------------------------------------ Instance Variables
    private static final Log log =
        LogFactory.getLog(AbstractAuthorizeAction.class);


    // ---------------------------------------------------------- Public Methods


    /**
     * <p>Determine whether the requested action is authorized for the current
     * user.  If not, abort chain processing and perferably, return an error
     * message of some kind.</p>
     *
     * @param context The <code>Context</code> for the current request
     *
     * @return <code>false</code> if the user is authorized for the selected
     * action, else <code>true</code> to abort processing.
     */
    public boolean execute(ActionContext actionCtx) throws Exception {

        // Retrieve ActionConfig
        ActionConfig actionConfig = actionCtx.getActionConfig();
            
        // Is this action protected by role requirements?
        String roles[] = actionConfig.getRoleNames();
        if ((roles == null) || (roles.length < 1)) {
            return (false);
        }
        
        boolean throwEx = false;
        try {
            throwEx = !(isAuthorized(actionCtx, roles, actionConfig));
        }
        catch (Exception ex) {
            throwEx = true;
            log.error("Unable to complete authorization process", ex);
        }
        
        if (throwEx) {
            
            // The current user is not authorized for this action
            throw new UnauthorizedActionException(getErrorMessage(actionCtx, actionConfig));
        } else {
            return (false);
        }
        
    }
    
    
    // ------------------------------------------------------- Protected Methods
    
    
    /**
     * <p>Determine if the action is authorized for the given roles.</p>
     *
     * @param context        The <code>Context</code> for the current request
     * @param roles          An array of valid roles for this request
     * @param actionConfig   The current action mapping
     *
     * @return <code>true</code> if the request is authorized, else 
     * <code>false</code>
     * @exception Exception If the action cannot be tested for authorization
     */
    protected abstract boolean isAuthorized(ActionContext context, String[] roles,    
                                            ActionConfig actionConfig)
              throws Exception;

    
    protected abstract String getErrorMessage(ActionContext context, ActionConfig actionConfig);

}
