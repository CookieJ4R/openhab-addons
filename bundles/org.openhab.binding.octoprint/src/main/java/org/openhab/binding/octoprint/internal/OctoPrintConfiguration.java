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

/**
 * The {@link OctoPrintConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Kevin Binder, Dario Pläschke, Florian Silber, Nour El-Dien Kamel - Initial contribution
 */
public class OctoPrintConfiguration {

    public String hostname;
    public int port;
    public String apikey;
}
