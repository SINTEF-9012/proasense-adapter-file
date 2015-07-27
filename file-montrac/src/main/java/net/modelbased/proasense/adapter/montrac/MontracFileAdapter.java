/**
 * Copyright (C) 2014-2015 SINTEF
 *
 *     Brian Elves�ter <brian.elvesater@sintef.no>
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

import eu.proasense.internal.ComplexValue;
import eu.proasense.internal.SimpleEvent;
import eu.proasense.internal.VariableType;
import net.modelbased.proasense.adapter.file.AbstractFileAdapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class MontracFileAdapter extends AbstractFileAdapter {
   // final static Logger logger = Logger.getLogger(MontracFileAdapter.class);


    public MontracFileAdapter() throws IOException, InterruptedException {
        scanDirectory(rootDirectoryPath, delayValue);
    }


    public void convertToSimpleEvent(String filePath) throws FileNotFoundException {
        logger.debug("er i readFiles(); " + filePath);
        File file = new File(filePath);
        Scanner scanner = new Scanner(file);
        Map<String, ComplexValue> properties = new HashMap<String, ComplexValue>();

        String date = scanner.next() + " " +scanner.next();
        String removeWhtespace = scanner.nextLine().replace(" ","");

        String values[] = removeWhtespace.split(",");

        // Convert the time from the event file to a long timestamp
        // Read it as a Date object
        // Convert to long

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date parsedDate = null;

        try {
            parsedDate = dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long timestamp = parsedDate.getTime();

        String sensorId = values[0];

        logger.debug("timestamp er  " + timestamp);
        logger.debug("sensorId er  " + sensorId);

        ComplexValue complexValue = new ComplexValue();
        complexValue.setValue(values[1]);
        complexValue.setType(VariableType.STRING);
        properties.put("event", complexValue);
        logger.debug("event = " + values[1]);


        complexValue = new ComplexValue();
        complexValue.setValue(values[2]);
        complexValue.setType(VariableType.LONG);
        properties.put("shuttle", complexValue);
        logger.debug("shuttle er = " + values[2]);

        String leftPiece[] = values[3].split("=");

        complexValue = new ComplexValue();
        complexValue.setValue(leftPiece[1]);
        complexValue.setType(VariableType.BOOLEAN);
        properties.put("leftPiece", complexValue);
        logger.debug("leftpiece er = " + leftPiece[0]);

        String rightPiece[] = values[4].split("=");

        complexValue = new ComplexValue();
        complexValue.setValue(rightPiece[1]);
        complexValue.setType(VariableType.BOOLEAN);
        properties.put("rightPiece", complexValue);
        logger.debug("rightPiece er = " + rightPiece[0]);

        SimpleEvent event = this.outputPort.createSimpleEvent(sensorId, timestamp, properties);
        logger.debug("SimpleEvent = " + event.toString());
        this.outputPort.publishSimpleEvent(event);
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        new MontracFileAdapter();
    }
}
