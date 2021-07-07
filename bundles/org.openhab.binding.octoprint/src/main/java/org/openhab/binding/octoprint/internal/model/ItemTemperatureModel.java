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
package org.openhab.binding.octoprint.internal.model;

/**
 * The {@link ItemTemperatureModel} contains a {@link TemperatureModel} for each of the tools and the bed of the printer
 *
 * @author Kevin Binder, Dario Pläschke, Florian Silber, Nour El-Dien Kamel - Initial contribution
 */
public class ItemTemperatureModel {

    public TemperatureModel bed;
    public TemperatureModel tool0;
    public TemperatureModel tool1;
}
