/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.octoprint.internal;

import static org.openhab.binding.octoprint.internal.OctoPrintBindingConstants.*;
import static org.openhab.core.model.script.actions.HTTP.sendHttpGetRequest;
import static org.openhab.core.model.script.actions.HTTP.sendHttpPostRequest;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.octoprint.internal.model.JobStatusModel;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link OctoPrintThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Kevin Binder, Dario Pläschke, Florian Silber, Nour El-Dien Kamel - Initial contribution
 */
@NonNullByDefault
public class OctoPrintThingHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(OctoPrintThingHandler.class);

    private @Nullable OctoPrintConfiguration config;

    private Gson gson = new Gson();

    public OctoPrintThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {} on channel {}", command.toFullString(), channelUID.getId());
        if (JOB_COMMANDS_CHANNEL.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                // TODO: handle data refresh
                logger.debug("Refresh called jobs");
            }
            if (command.toString().equals("version")) {
                logger.debug("GETTING SERVER VERSION...");
                String url = "http://" + config.hostname + ":" + config.port + "/api/version?apikey=" + config.apikey;
                logger.debug("{}", url);
                String version = sendHttpGetRequest(url, 1000);
                logger.debug("{}", version);
            } else if (command.toString().equals("resume")) {
                String url = "http://" + config.hostname + ":" + config.port + "/api/job?apikey=" + config.apikey;
                String content = "{\"command\":\"pause\", \"action\":\"resume\"}";
                String result = sendHttpPostRequest(url, "application/json", content, 5000);
                updateState(JOB_COMMANDS_CHANNEL, new StringType(getJobStatus().state));
                refresh();
                logger.debug("{}", result);
            } else if (command.toString().equals("toggle")) {
                String url = "http://" + config.hostname + ":" + config.port + "/api/job?apikey=" + config.apikey;
                String content = "{\"command\":\"pause\", \"action\":\"toggle\"}";
                String result = sendHttpPostRequest(url, "application/json", content, 5000);
                logger.debug("{}", result);
                updateState(JOB_COMMANDS_CHANNEL, new StringType(getJobStatus().state));
                refresh();
            } else if (command.toString().equals("pause")) {
                String url = "http://" + config.hostname + ":" + config.port + "/api/job?apikey=" + config.apikey;
                String content = "{\"command\":\"pause\", \"action\":\"pause\"}";
                String result = sendHttpPostRequest(url, "application/json", content, 5000);
                updateState(JOB_COMMANDS_CHANNEL, new StringType(getJobStatus().state));
                logger.debug("{}", result);
                refresh();
            } else if (command.toString().equals("start")) {
                String url = "http://" + config.hostname + ":" + config.port + "/api/job?apikey=" + config.apikey;
                String content = "{\"command\":\"start\"}";
                String result = sendHttpPostRequest(url, "application/json", content, 5000);
                updateState(JOB_COMMANDS_CHANNEL, new StringType(getJobStatus().state));
                logger.debug("{}", result);
                refresh();
            } else if (command.toString().equals("cancel")) {
                String url = "http://" + config.hostname + ":" + config.port + "/api/job?apikey=" + config.apikey;
                String content = "{\"command\":\"cancel\"}";
                String result = sendHttpPostRequest(url, "application/json", content, 5000);
                updateState(JOB_COMMANDS_CHANNEL, new StringType(getJobStatus().state));
                logger.debug("{}", result);
                refresh();
            } else if (command.toString().equals("restart")) {
                String url = "http://" + config.hostname + ":" + config.port + "/api/job?apikey=" + config.apikey;
                String content = "{\"command\":\"restart\"}";
                String result = sendHttpPostRequest(url, "application/json", content, 5000);
                logger.debug("{}", result);
                updateState(JOB_COMMANDS_CHANNEL, new StringType(getJobStatus().state));
                refresh();
            }
            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        } else if (TEMPERATURE_CHANNEL.equals(channelUID.getId())) {
            if (command instanceof RefreshType || command.toString().equals("REFRESH")) {
                logger.debug("Refresh called temp");
            }
        } else if (JOB_COMPLETION_CHANNEL.equals(channelUID.getId())) {
            if (command instanceof RefreshType || command.toString().equals("REFRESH")) {
                JobStatusModel job = getJobStatus();
                updateState(JOB_COMPLETION_CHANNEL, new StringType(String.format("%.2f", job.progress.completion)));
            }
        } else if (JOB_RUNTIME_CHANNEL.equals(channelUID.getId())) {
            if (command instanceof RefreshType || command.toString().equals("REFRESH")) {
                JobStatusModel job = getJobStatus();
                updateState(JOB_RUNTIME_CHANNEL, new DecimalType(job.progress.printTime));
            }
        } else if (JOB_RUNTIME_LEFT_CHANNEL.equals(channelUID.getId())) {
            if (command instanceof RefreshType || command.toString().equals("REFRESH")) {
                JobStatusModel job = getJobStatus();
                updateState(JOB_RUNTIME_LEFT_CHANNEL, new DecimalType(job.progress.printTimeLeft));
            }
        } else if (JOB_FILENAME_CHANNEL.equals(channelUID.getId())) {
            if (command instanceof RefreshType || command.toString().equals("REFRESH")) {
                JobStatusModel jobStatus = getJobStatus();
                updateState(JOB_FILENAME_CHANNEL, new StringType(jobStatus.job.file.name));
            }
        }
    }

    public void refresh() {
        JobStatusModel jobStatus = getJobStatus();
        updateState(JOB_FILENAME_CHANNEL, new StringType(jobStatus.job.file.name));
        updateState(JOB_COMPLETION_CHANNEL, new StringType(String.format("%.2f", jobStatus.progress.completion)));
        updateState(JOB_RUNTIME_CHANNEL, new DecimalType(jobStatus.progress.printTime));
        updateState(JOB_RUNTIME_LEFT_CHANNEL, new DecimalType(jobStatus.progress.printTimeLeft));
    }

    public JobStatusModel getJobStatus() {
        String url = "http://" + config.hostname + ":" + config.port + "/api/job?apikey=" + config.apikey;
        String result = sendHttpGetRequest(url, 5000);
        JobStatusModel job = gson.fromJson(result, JobStatusModel.class);
        if (job == null)
            return new JobStatusModel();
        return job;
    }

    @Override
    public void initialize() {
        config = getConfigAs(OctoPrintConfiguration.class);
        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly. Also, before leaving this method a thing
        // status from one of ONLINE, OFFLINE or UNKNOWN must be set. This might already be the real thing status in
        // case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        // Example for background initialization:
        scheduler.execute(() -> {
            boolean thingReachable = true; // <background task with long running initialization here>
            // when done do:
            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });

        // These logging types should be primarily used by bindings
        // logger.trace("Example trace message");
        // logger.debug("Example debug message");
        // logger.warn("Example warn message");

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }
}
