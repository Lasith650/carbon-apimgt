/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.lifecycle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the LifeCycle
 */
public class LCManager {

    private static final Log log = LogFactory.getLog(LCManager.class);
    private static final String STATE_ID_PROTOTYPED = "Prototyped";
    private static final String STATE_ID_PUBLISHED = "Published";
    private static final String TRANSITION_TARGET_PROTOTYPED = "Prototyped";
    private static final String TRANSITION_TARGET_PUBLISHED = "Published";
    private static final String LIFECYCLE_KEY = "LifeCycle";
    private static final String STATES_KEY = "States";
    private static final String STATE_KEY = "State";
    private static final String TRANSITIONS_KEY = "Transitions";
    private static final String CHECK_ITEMS_KEY = "CheckItems";
    private static final String API_LIFECYCLE_PATH = "lifecycle/APILifeCycle.json";
    private static final String UTF_8 = "UTF-8";
    private static final String EVENT_KEY = "Event";
    private static final String TARGET_KEY = "Target";
    private Map<String, String> stateTransitionMap = new HashMap<String, String>();
    private Map<String, StateInfo> stateInfoMap = new HashMap<String, StateInfo>();
    private HashMap<String, LifeCycleTransition> stateHashMap = new HashMap<String, LifeCycleTransition>();
    private String tenantDomain;
    private static JSONObject defaultLCObj;
    private LCManager instance;

    static {
        if (defaultLCObj == null) {

            try {
                defaultLCObj = getDefaultLCConfigJSON();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }
    }

    /**
     * Initialize LCManager Class
     * @param tenantDomain
     */
    public LCManager(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    /**
     * Process the lifecycle object into the states
     *
     * @throws APIManagementException
     */
    private void processLifeCycle() throws APIManagementException {

        JSONObject tenantConfig = APIUtil.getTenantConfig(tenantDomain);
        JSONArray states;

        //Checking whether the lifecycle exists in the tenantConfig
        if (tenantConfig.containsKey(LIFECYCLE_KEY)) {
            JSONObject LCObj = (JSONObject) tenantConfig.get(LIFECYCLE_KEY);
            states = (JSONArray) LCObj.get(STATES_KEY);
        } else {
            JSONObject jsonObject = defaultLCObj;
            states = (JSONArray) jsonObject.get(STATES_KEY);
        }

        for (Object state : states) {
            JSONObject stateObj = (JSONObject) state;
            String stateId = (String) stateObj.get(STATE_KEY);
            LifeCycleTransition lifeCycleTransition = new LifeCycleTransition();
            List<String> actions = new ArrayList<String>();
            List<String> checklistItems = new ArrayList<String>();
            if (stateObj.containsKey(TRANSITIONS_KEY)) {
                JSONArray transitions = (JSONArray) stateObj.get(TRANSITIONS_KEY);
                for (Object transition : transitions) {
                    JSONObject transitionObj = (JSONObject) transition;
                    String action = (String) transitionObj.get(EVENT_KEY);
                    String target = (String) transitionObj.get(TARGET_KEY);
                    if (stateId.equals(STATE_ID_PROTOTYPED)
                            && (target.equals(TRANSITION_TARGET_PROTOTYPED)
                    )) {
                        // skip adding "Publish" and "Deploy as a Prototype" transitions as having those transitions
                        // in Prototyped state is invalid
                    } else if (stateId.equals(STATE_ID_PUBLISHED)
                            && target.equals(TRANSITION_TARGET_PUBLISHED)) {
                        // skip adding "Publish" transition as having this transition in Published state is invalid
                    } else {
                        if (target != null && action != null) {
                            lifeCycleTransition.addTransition(target.toUpperCase(),
                                    action);
                            stateTransitionMap.put(action, target.toUpperCase());
                            actions.add(action);
                        }
                    }
                }
            }

            if (stateObj.containsKey(CHECK_ITEMS_KEY)) {
                JSONArray checkItems = (JSONArray) stateObj.get(CHECK_ITEMS_KEY);
                for (Object checkItem : checkItems) {
                    checklistItems.add(checkItem.toString());
                }
            }

            stateHashMap.put(stateId.toUpperCase(), lifeCycleTransition);
            StateInfo stateInfo = new StateInfo();
            stateInfo.setCheckListItems(checklistItems);
            stateInfo.setTransitions(actions);
            stateInfoMap.put(stateId.toUpperCase(), stateInfo);
        }
    }

    /**
     * Reading the default API Lifecycle
     *
     * @return
     * @throws IOException
     * @throws ParseException
     * @throws URISyntaxException
     * @throws APIManagementException
     */
    public static JSONObject getDefaultLCConfigJSON() throws IOException, ParseException {

        InputStream lcStream = LCManager.class.getClassLoader().getResourceAsStream(API_LIFECYCLE_PATH);
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(
                new InputStreamReader(lcStream, UTF_8));

        return jsonObject;
    }

    /**
     * Returning the Action for the current and the target state
     *
     * @param currentState
     * @param targetState
     * @return
     * @throws APIManagementException
     * @throws IOException
     * @throws ParseException
     */
    public String getTransitionAction(String currentState, String targetState) throws APIManagementException, IOException, ParseException {
        processLifeCycle();
        if (stateHashMap.containsKey(currentState)) {
            LifeCycleTransition transition = stateHashMap.get(currentState);
            return transition.getAction(targetState);
        }
        return null;
    }

    /**
     * Get State Transition for the action
     *
     * @param action
     * @return
     * @throws APIManagementException
     */
    public String getStateForTransition(String action) throws APIManagementException{
        processLifeCycle();
        return stateTransitionMap.get(action);
    }

    static class LifeCycleTransition {
        private HashMap<String, String> transitions;

        /**
         * Initialize class
         */
        public LifeCycleTransition() {
            this.transitions = new HashMap<>();
        }

        /**
         * Returns action required to transit to state.
         *
         * @param state State to get action
         * @return lifecycle action associated or null if not found
         */
        public String getAction(String state) {
            if (!transitions.containsKey(state)) {
                return null;
            }
            return transitions.get(state);
        }

        /**
         * Adds a transition.
         *
         * @param targetStatus target status
         * @param action action associated with target
         */
        public void addTransition(String targetStatus, String action) {
            transitions.put(targetStatus, action);
        }
    }

    static class StateInfo {
        private String state;
        private List<String> transitions = new ArrayList<String>();
        private List<String> checkListItems = new ArrayList<String>();

        public List<String> getCheckListItems() {
            return checkListItems;
        }
        public void setCheckListItems(List<String> checkListItems) {
            this.checkListItems = checkListItems;
        }

        public List<String> getTransitions() {
            return transitions;
        }

        public void setTransitions(List<String> transitions) {
            this.transitions = transitions;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }
    }

    /**
     * Get check list items for the state
     *
     * @param state
     * @return
     * @throws APIManagementException
     * @throws IOException
     * @throws ParseException
     */
    public List<String> getCheckListItemsForState(String state) throws APIManagementException, IOException, ParseException {
        processLifeCycle();
        if (stateInfoMap.containsKey(state)) {
            return stateInfoMap.get(state).getCheckListItems();
        }
        return null;
    }

    /**
     * Get allowed actions for the state
     *
     * @param state
     * @return
     * @throws APIManagementException
     * @throws IOException
     * @throws ParseException
     */
    public List<String> getAllowedActionsForState(String state) throws APIManagementException, IOException, ParseException {
        processLifeCycle();
        if (stateInfoMap.containsKey(state)) {
            return stateInfoMap.get(state).getTransitions();
        }
        return null;
    }

}