/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React, { Component } from 'react'

import { Link } from 'react-router-dom'
import Grid from 'material-ui/Grid';
import Paper from 'material-ui/Paper';
import Typography from 'material-ui/Typography';
import Table, { TableBody, TableCell, TableHead, TableRow } from 'material-ui/Table';
import AppBar from 'material-ui/AppBar';
import Toolbar from 'material-ui/Toolbar';
import IconButton from 'material-ui/IconButton';
import Divider from 'material-ui/Divider';
import Button from 'material-ui/Button';
import MenuIcon from '@material-ui/icons/Menu';
import TextField from 'material-ui/TextField';
import { withStyles } from 'material-ui/styles';
import Switch from 'material-ui/Switch';

import API from '../../data/api'
import Message from '../Shared/Message'
import Confirm from '../Shared/Confirm'
import Alert from '../Shared/Alert'

const messages = {
    success: 'Deleted black list policy successfully',
    failure: 'Error while deleting black list policy',
    retrieveError: 'Error while retrieving black list policies'
};

const styles = ({
    divider: {
        marginBottom: 20,
    },
    createButton: {
        textDecoration: 'none',
        display: 'inline-block',
        marginLeft: 20,
        alignSelf: 'flex-start',
    },
    titleWrapper: {
        display: 'flex',
    }
});

class BlackLists extends Component {
    constructor(props) {
        super(props);
        this.state = {
            policies: null,
            selectedRowKeys: [],
            open: false,
            message: ''
        };
        this.handlePolicyChange = this.handlePolicyChange.bind(this);
        this.deleteBlackListPolicy = this.deleteBlackListPolicy.bind(this);

    }

    deleteBlackListPolicy(event) {
        const api = new API();
        const id = event.currentTarget.id;
        const promisedPolicies = api.deleteBlackListPolicy(id);
        promisedPolicies.then(
            response => {
                Alert.info(messages.success);
                var data = this.state.policies.filter(obj => {
                    return obj.conditionId !== id;
                });
                this.setState({ policies: data });
            }
        ).catch(
            error => {
                Alert.error(messages.failure);
                console.error(error);
            }
        );
    }

    componentDidMount() {
        const api = new API();

        const promised_policies = api.getBlockListPolicies();
        promised_policies.then(
            response => {
                this.setState({ policies: response.obj.list });
            }
        ).catch(
            error => {
                Alert.error(messages.retrieveError);
                console.error(error);
            }
        );
    }

    handlePolicyChange(event) {
        const api = new API();
        const status = event.target.checked;
        const id = event.target.id;

        const promisedPolicies = api.updateBlackListPolicy(id, { status: status });
        promisedPolicies.then(
            response => {
                const policies = this.state.policies;
                policies.forEach(element => {
                    if (element.conditionId === id) {
                        element.status = status;
                    }
                });
                this.setState({ policies: policies });
            }
        ).catch(
            error => {
                Alert.error(messages.retrieveError);
                console.error(error);
            }
        );
    }

    render() {
        /*TODO implement search and pagination*/
        const tiers = this.state.policies;
        const { classes } = this.props;
        let data = [];
        if (tiers) {
            data = tiers;
        }

        return (
            <div>
                <Grid container justify="center" alignItems="center">
                    <Grid item xs={12}>

                        <div className={classes.titleWrapper}>
                            <Typography variant="display1" gutterBottom >
                                Black List Policies
                            </Typography>
                            <Link to={"/policies/black_list_policies/create"} className={classes.createButton}>
                                <Button variant="raised" color="primary" className={classes.button}>
                                    Add Black List Policy
                                </Button>
                            </Link>
                        </div>
                        <Divider className={classes.divider} />

                    </Grid>
                    <Grid item xs={12} className="page-content">
                        <Paper>
                            <Table>
                                <TableHead>
                                    <TableRow>
                                        <TableCell>Condition Id</TableCell>
                                        <TableCell>Type</TableCell>
                                        <TableCell>Value</TableCell>
                                        <TableCell>Status</TableCell>
                                        <TableCell></TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {data.map(n => {
                                        return (
                                            <TableRow key={n.conditionId}>
                                                <TableCell>{n.conditionId}</TableCell>
                                                <TableCell>{n.conditionType}</TableCell>
                                                <TableCell>{n.conditionValue}</TableCell>
                                                <TableCell><Switch id={n.conditionId}
                                                    checked={n.status}
                                                    onChange={this.handlePolicyChange}
                                                    value={n.conditionId}
                                                    color="primary"
                                                /></TableCell>
                                                <TableCell>
                                                    <span>
                                                        <Button id={n.conditionId} color="default"
                                                            onClick={this.deleteBlackListPolicy} >Delete</Button>
                                                    </span>
                                                </TableCell>
                                            </TableRow>
                                        );
                                    })}
                                </TableBody>
                            </Table>
                        </Paper>
                    </Grid>
                </Grid>
            </div>
        );
    }
}
export default withStyles(styles)(BlackLists);
