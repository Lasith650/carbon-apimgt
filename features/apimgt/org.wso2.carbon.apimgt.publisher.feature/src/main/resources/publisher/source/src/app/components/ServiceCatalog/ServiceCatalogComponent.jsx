/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import React from 'react';
import { Route, Switch } from 'react-router-dom';
import Listing from 'AppComponents/ServiceCatalog/Listing/Listing';
import Overview from 'AppComponents/ServiceCatalog/Listing/Overview';

/**
 * Have used key={Date.now()} for `Route` element in `/service-catalog`
 */
const ServiceCatalogComponent = () => {
    return (
        <Switch>
            <Route
                exact
                path='/service-catalog'
                key={Date.now()}
                component={(props) => <Listing {...props} />}
            />
            <Route
                path='/service-catalog/:service_uuid/overview'
                key='/service-catalog/:service_uuid/overview'
                // eslint-disable-next-line react/jsx-props-no-spreading
                component={(props) => <Overview {...props} />}
            />
        </Switch>
    );
};

export default ServiceCatalogComponent;