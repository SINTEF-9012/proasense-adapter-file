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

import eu.proasense.internal.ComplexValue;
import eu.proasense.internal.SimpleEvent;
import eu.proasense.internal.VariableType;
import net.modelbased.proasense.adapter.file.AbstractFileSimulator;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class MontracFileSimulator extends AbstractFileSimulator {
    public final static Logger logger = Logger.getLogger(MontracFileSimulator.class);


    public MontracFileSimulator(String folderPath) {
        super(folderPath);
    }


    public void convertToSimpleEvent(String filePath) throws FileNotFoundException {
        logger.debug("Processing file = " + filePath);
        File file = new File(filePath);
        Scanner scanner = new Scanner(file);
        Map<String, ComplexValue> properties = new HashMap<String, ComplexValue>();

        String date = scanner.next() + " " + scanner.next();
        String removeWhitespace = scanner.nextLine().replace(" ", "");

        String values[] = removeWhitespace.split(",");

        if (values.length != 5) {
            warningMessage(filePath);
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date parsedDate = null;

        try {
            parsedDate = dateFormat.parse(date);
        }
        catch (ParseException e) {
            e.printStackTrace();
            System.out.println("Please check the dateformat for the file below " + e);
            warningMessage(filePath);
        }

        long timestamp = parsedDate.getTime();

        logger.debug("timestamp = " + timestamp);
        logger.debug("sensorId = " + sensorId);

        ComplexValue complexValue = new ComplexValue();
        complexValue.setValue(values[0]);
        complexValue.setType(VariableType.STRING);
        properties.put("location", complexValue);
        logger.debug("location = " + values[0]);

        complexValue = new ComplexValue();
        complexValue.setValue(values[1]);
        complexValue.setType(VariableType.STRING);
        properties.put("event", complexValue);
        logger.debug("event = " + values[1]);

        complexValue = new ComplexValue();
        complexValue.setValue(values[2]);
        complexValue.setType(VariableType.LONG);
        properties.put("shuttle", complexValue);
        logger.debug("shuttle = " + values[2]);

        String leftPiece[] = values[3].split("=");

        complexValue = new ComplexValue();
        complexValue.setValue(leftPiece[1]);
        complexValue.setType(VariableType.BOOLEAN);
        properties.put("leftPiece", complexValue);
        logger.debug("leftPiece = " + leftPiece[1]);

        String rightPiece[] = values[4].split("=");

        complexValue = new ComplexValue();
        complexValue.setValue(rightPiece[1]);
        complexValue.setType(VariableType.BOOLEAN);
        properties.put("rightPiece", complexValue);
        logger.debug("rightPiece = " + rightPiece[1]);

        SimpleEvent event = this.outputPort.createSimpleEvent(sensorId, timestamp, properties);
        this.outputPort.publishSimpleEvent(event);
        logger.debug("SimpleEvent = " + event.toString());

        scanner.close();
    }


    // Start method
    public static void main(String[] args) {
        if (args.length != 1){
            System.out.println("Usage: MontracFileSimulator <Montrac data folder>");
        }
        else {
            new MontracFileSimulator(args[0]);
        }
    }


    // Stop method
    public static void stop(String[] args) {
        System.exit(0);
    }
}
