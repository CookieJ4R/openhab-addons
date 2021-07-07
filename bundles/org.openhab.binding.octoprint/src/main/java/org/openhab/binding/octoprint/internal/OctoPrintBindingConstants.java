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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link OctoPrintBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Kevin Binder, Dario Pläschke, Florian Silber, Nour El-Dien Kamel - Initial contribution
 */
@NonNullByDefault
public class OctoPrintBindingConstants {

    private static final String BINDING_ID = "octoprint";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_OCTOPRINT_SERVER = new ThingTypeUID(BINDING_ID, "OctoPrintServer");

    // List of all Channel ids
    public static final String JOB_COMMANDS_CHANNEL = "job_commands";

    public static final String JOB_RUNTIME_CHANNEL = "job_runtime";
    public static final String JOB_RUNTIME_LEFT_CHANNEL = "job_runtime_left";
    public static final String JOB_COMPLETION_CHANNEL = "job_completion";
    public static final String JOB_FILENAME_CHANNEL = "job_filename";

    public static final String TEMPERATURE_TOOL_ONE_CHANNEL = "tool_1_temperature";
    public static final String TEMPERATURE_TOOL_TWO_CHANNEL = "tool_2_temperature";
    public static final String TEMPERATURE_BED_CHANNEL = "bed_temperature";
}
