/**
 * Copyright (C) 2014-2015 SINTEF
 *
 *     Brian Elvesæter <brian.elvesater@sintef.no>
 *     Shahzad Karamat <shazad.karamat@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.modelbased.proasense.adapter.montrac;

import net.modelbased.proasense.adapter.file.AbstractFileAdapter;

import eu.proasense.internal.ComplexValue;
import eu.proasense.internal.SimpleEvent;
import eu.proasense.internal.VariableType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.log4j.Logger;


public class MontracFileAdapter extends AbstractFileAdapter {
    final static Logger logger = Logger.getLogger(MontracFileAdapter.class);


    public MontracFileAdapter() throws IOException, InterruptedException {
        scanDirectory(rootDirectoryPath, delayValue);
    }


    public void convertToSimpleEvent(String filePath) throws FileNotFoundException {
        System.out.println("er i readFiles(); " + filePath);
        File file = new File(filePath);
        Scanner scanner = new Scanner(file);
        Map<String, ComplexValue> properties = new HashMap<String, ComplexValue>();

        String values[] = scanner.nextLine().split(",");

        // Convert the time from the event file to a long timestamp
        // Read it as a Date object
        // Convert to long

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date parsedDate = null;

        try {
            parsedDate = dateFormat.parse(values[0]);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long timestamp = parsedDate.getTime();

        String sensorId = values[1];

        System.out.println("timestamp er  " + timestamp);
        System.out.println("sensorId er  " + sensorId);

        ComplexValue complexValue = new ComplexValue();
        complexValue.setValue(values[2]);
        complexValue.setType(VariableType.STRING);
        properties.put("event", complexValue);
        System.out.println("event = " + values[2]);

        complexValue = new ComplexValue();
        complexValue.setValue(values[3]);
        complexValue.setType(VariableType.LONG);
        properties.put("shuttle", complexValue);
        System.out.println("shuttle er = " + values[3]);

        String leftPiece[] = values[4].split("=");

        complexValue = new ComplexValue();
        complexValue.setValue(leftPiece[1]);
        complexValue.setType(VariableType.STRING);
        properties.put("leftPiece", complexValue);
        System.out.println("leftpiece er = " + leftPiece[1]);

        String rightPiece[] = values[5].split("=");

        complexValue = new ComplexValue();
        complexValue.setValue(rightPiece[1]);
        complexValue.setType(VariableType.STRING);
        properties.put("rightPiece", complexValue);
        System.out.println("rightPiece er = " + rightPiece[1]);
        logger.debug("rightPiece er = " + rightPiece[1]);

        SimpleEvent event = this.outputPort.createSimpleEvent(sensorId, timestamp, properties);
        logger.debug("SimpleEvent = " + event.toString());
        this.outputPort.publishSimpleEvent(event);
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        new MontracFileAdapter();
    }
}
