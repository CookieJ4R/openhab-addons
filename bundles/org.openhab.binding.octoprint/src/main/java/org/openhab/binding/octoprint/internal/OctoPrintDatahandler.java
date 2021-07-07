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

import static org.openhab.core.model.script.actions.HTTP.sendHttpGetRequest;
import static org.openhab.core.model.script.actions.HTTP.sendHttpPostRequest;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.octoprint.internal.model.ItemTemperatureModel;
import org.openhab.binding.octoprint.internal.model.JobStatusModel;
import org.openhab.binding.octoprint.internal.model.PrinterModel;

import com.google.gson.Gson;

/**
 * The {@link OctoPrintDatahandler} handles requests to the OctoPrint server and the json-parsing of the result.
 *
 * @author Kevin Binder, Dario Pläschke, Florian Silber, Nour El-Dien Kamel - Initial contribution
 */
public class OctoPrintDatahandler {

    private OctoPrintConfiguration config;
    private Gson gson = new Gson();

    public OctoPrintDatahandler(OctoPrintConfiguration config) {
        this.config = config;
    }

    /***
     * This method queries the OctoPrint server for the current job information and parses the returned JSON to a
     * {@link JobStatusModel}.
     * 
     * @return The JobStatusModel parsed from the request result
     */
    @Nullable
    public JobStatusModel getJobStatus() {
        String url = "http://" + config.hostname + ":" + config.port + "/api/job?apikey=" + config.apikey;
        String result = sendHttpGetRequest(url, 5000);
        if (result == null)
            return null;
        JobStatusModel job = gson.fromJson(result, JobStatusModel.class);
        return job;
    }

    /***
     * This method queries the OctoPrint server for the current temperature data and parses the returned JSON to a
     * {@link ItemTemperatureModel}
     * 
     * @return The ItemTemperatureModel parsed from the request result
     */
    @Nullable
    public ItemTemperatureModel getTemperatureData() {
        String url = "http://" + config.hostname + ":" + config.port + "/api/printer?apikey=" + config.apikey;
        String result = sendHttpGetRequest(url, 5000);
        if (result == null)
            return null;
        ItemTemperatureModel job = gson.fromJson(result, PrinterModel.class).temperature;
        return job;
    }

    /***
     * This method sends a post request to the OctoPrint server to control the printer (start, pause, etc.)
     * 
     * @param content The content the post request should contain in json notation
     *            (example: "{\"command\":\"pause\",\"action\":\"resume\"}")
     */
    public void sendJobRequest(String content) {
        String url = "http://" + config.hostname + ":" + config.port + "/api/job?apikey=" + config.apikey;
        sendHttpPostRequest(url, "application/json", content, 5000);
    }
}
