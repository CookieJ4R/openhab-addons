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

import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.octoprint.internal.model.ItemTemperatureModel;
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

    private @Nullable OctoPrintDatahandler datahandler;

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
        //Get printer data
        JobStatusModel jobStatus = datahandler.getJobStatus();
        ItemTemperatureModel temperatureStatus = datahandler.getTemperatureData();
        //update ThingStatus if no result was returned for one of the requests
        if (jobStatus == null || temperatureStatus == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not reach OctoPrint-Server");
            return;
        }
        //handle channel and command
        switch (channelUID.getId()) {
            case JOB_COMMANDS_CHANNEL:
                if (command instanceof RefreshType) {
                    updateState(JOB_COMMANDS_CHANNEL, new StringType(jobStatus.state));
                }
                switch (command.toString()) {
                    case "resume":
                        datahandler.sendJobRequest("{\"command\":\"pause\", \"action\":\"resume\"}");
                        updateState(JOB_COMMANDS_CHANNEL, new StringType(datahandler.getJobStatus().state));
                        break;
                    case "toggle":
                        datahandler.sendJobRequest("{\"command\":\"pause\", \"action\":\"toggle\"}");
                        updateState(JOB_COMMANDS_CHANNEL, new StringType(datahandler.getJobStatus().state));
                        break;
                    case "pause":
                        datahandler.sendJobRequest("{\"command\":\"pause\", \"action\":\"pause\"}");
                        updateState(JOB_COMMANDS_CHANNEL, new StringType(datahandler.getJobStatus().state));
                        break;
                    case "start":
                        datahandler.sendJobRequest("{\"command\":\"start\"}");
                        updateState(JOB_COMMANDS_CHANNEL, new StringType(datahandler.getJobStatus().state));
                        break;
                    case "cancel":
                        datahandler.sendJobRequest("{\"command\":\"cancel\"}");
                        updateState(JOB_COMMANDS_CHANNEL, new StringType(datahandler.getJobStatus().state));
                        break;
                    case "restart":
                        datahandler.sendJobRequest("{\"command\":\"restart\"}");
                        updateState(JOB_COMMANDS_CHANNEL, new StringType(datahandler.getJobStatus().state));
                        break;
                }
                break;
            case JOB_COMPLETION_CHANNEL:
                if (command instanceof RefreshType) {
                    updateState(JOB_COMPLETION_CHANNEL,
                            new StringType(String.format("%.2f", jobStatus.progress.completion)));
                }
                break;
            case JOB_RUNTIME_CHANNEL:
                if (command instanceof RefreshType) {
                    updateState(JOB_RUNTIME_CHANNEL, new DecimalType(jobStatus.progress.printTime));
                }
                break;
            case JOB_RUNTIME_LEFT_CHANNEL:
                if (command instanceof RefreshType) {
                    updateState(JOB_RUNTIME_LEFT_CHANNEL, new DecimalType(jobStatus.progress.printTimeLeft));
                }
                break;
            case JOB_FILENAME_CHANNEL:
                if (command instanceof RefreshType) {
                    updateState(JOB_FILENAME_CHANNEL, new StringType(jobStatus.job.file.name));
                }
                break;
            case TEMPERATURE_TOOL_ONE_CHANNEL:
                if (command instanceof RefreshType) {
                    updateState(TEMPERATURE_TOOL_ONE_CHANNEL, new DecimalType(temperatureStatus.tool0.actual));
                }
                break;
            case TEMPERATURE_TOOL_TWO_CHANNEL:
                if (command instanceof RefreshType) {
                    updateState(TEMPERATURE_TOOL_TWO_CHANNEL, new DecimalType(temperatureStatus.tool1.actual));
                }
                break;
            case TEMPERATURE_BED_CHANNEL:
                if (command instanceof RefreshType) {
                    updateState(TEMPERATURE_BED_CHANNEL, new DecimalType(temperatureStatus.bed.actual));
                }
                break;
        }
    }

    /***
     * The refresh method gets called every {@link OctoPrintConfiguration#refreshrate) seconds.
     * It polls the OctoPrint server for the current state and updates all channels with the corresponding state.
     * If the server is not reachable it sets the ThingStatus to offline
     */
    public void refresh() {
        //Get data
        JobStatusModel jobStatus = datahandler.getJobStatus();
        ItemTemperatureModel temperatureStatus = datahandler.getTemperatureData();

        //Check if the server was reachable
        if (jobStatus == null || temperatureStatus == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not reach OctoPrint-Server");
            return;
        }

        //Update channel states
        updateState(JOB_COMMANDS_CHANNEL, new StringType(jobStatus.state));
        updateState(JOB_FILENAME_CHANNEL, new StringType(jobStatus.job.file.name));
        updateState(JOB_COMPLETION_CHANNEL, new DecimalType(jobStatus.progress.completion));
        updateState(JOB_RUNTIME_CHANNEL, new DecimalType(jobStatus.progress.printTime));
        updateState(JOB_RUNTIME_LEFT_CHANNEL, new DecimalType(jobStatus.progress.printTimeLeft));
        updateState(TEMPERATURE_TOOL_ONE_CHANNEL, new DecimalType(temperatureStatus.tool0.actual));
        updateState(TEMPERATURE_TOOL_TWO_CHANNEL, new DecimalType(temperatureStatus.tool1.actual));
        updateState(TEMPERATURE_BED_CHANNEL, new DecimalType(temperatureStatus.bed.actual));
    }

    @Override
    public void initialize() {
        config = getConfigAs(OctoPrintConfiguration.class);
        datahandler = new OctoPrintDatahandler(config);
        updateStatus(ThingStatus.UNKNOWN);

        //Execute connection check on a different thread to leave init method as fast as possible
        scheduler.execute(() -> {
            String url = "http://" + config.hostname + ":" + config.port + "/api/version?apikey=" + config.apikey;
            String result = sendHttpGetRequest(url, 2000);

            //request timeout => not reachable (OFFLINE)
            if (result == null)
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Can not access OctoPrint-Server. This could be caused by a wrong hostname, port, api-key or simply by the OctoPrint-Server being offline");
            //received expected answer => reachable (ONLINE)
            else if (result.contains("OctoPrint")) {
                updateStatus(ThingStatus.ONLINE);
                scheduler.scheduleWithFixedDelay(updateTask, 0, config.refreshrate, TimeUnit.SECONDS);
            }
            //received unexpected answer => reachable but not an OctoPrint server (OFFLINE)
            else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Got an unexpected answer from server. This could be caused by a different service running on the configured host and port.");
            }
        });
    }

    @Override
    public void dispose() {
        //Cancel data polling refresh cycle on removal
        updateTask.cancel();
    }
}
