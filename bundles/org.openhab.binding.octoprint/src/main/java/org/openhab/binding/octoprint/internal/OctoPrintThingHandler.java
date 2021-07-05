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

import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.octoprint.internal.model.JobStatusModel;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link OctoPrintThingHandler} is responsible for initialization, handling commands, scheduling updates
 * and de-initialization
 *
 * @author Kevin Binder, Dario Pläschke, Florian Silber, Nour El-Dien Kamel - Initial contribution
 */
@NonNullByDefault
public class OctoPrintThingHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(OctoPrintThingHandler.class);

    private @Nullable OctoPrintConfiguration config;

    private Gson gson = new Gson();

    private TimerTask updateTask = new TimerTask() {
        @Override
        public void run() {
            refresh();
        }
    };

    public OctoPrintThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {} on channel {}", command.toFullString(), channelUID.getId());
        if (JOB_COMMANDS_CHANNEL.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                JobStatusModel jobStatus = getJobStatus();
                updateState(JOB_COMMANDS_CHANNEL, new StringType(jobStatus.state));
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
        if (jobStatus == null)
            return;
        updateState(JOB_COMMANDS_CHANNEL, new StringType(jobStatus.state));
        updateState(JOB_FILENAME_CHANNEL, new StringType(jobStatus.job.file.name));
        updateState(JOB_COMPLETION_CHANNEL, new StringType(String.format("%.2f", jobStatus.progress.completion)));
        updateState(JOB_RUNTIME_CHANNEL, new DecimalType(jobStatus.progress.printTime));
        updateState(JOB_RUNTIME_LEFT_CHANNEL, new DecimalType(jobStatus.progress.printTimeLeft));
    }

    @Nullable
    public JobStatusModel getJobStatus() {
        String url = "http://" + config.hostname + ":" + config.port + "/api/job?apikey=" + config.apikey;
        String result = sendHttpGetRequest(url, 5000);
        if (result == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not reach OctoPrint-Server");
            return null;
        }
        JobStatusModel job = gson.fromJson(result, JobStatusModel.class);
        return job;
    }

    @Override
    public void initialize() {
        config = getConfigAs(OctoPrintConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            String url = "http://" + config.hostname + ":" + config.port + "/api/version?apikey=" + config.apikey;
            String result = sendHttpGetRequest(url, 2000);
            if (result == null)
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Can not access OctoPrint-Server. This could be caused by a wrong hostname, port, api-key or simply by the OctoPrint-Server being offline");
            else if (result.contains("OctoPrint")) {
                updateStatus(ThingStatus.ONLINE);
                scheduler.scheduleWithFixedDelay(updateTask, 0, 1, TimeUnit.SECONDS);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Got an unexpected answer from server. This could be caused by a different service running on the configured host and port.");
            }
        });
    }

    @Override
    public void dispose() {
        updateTask.cancel();
    }
}
