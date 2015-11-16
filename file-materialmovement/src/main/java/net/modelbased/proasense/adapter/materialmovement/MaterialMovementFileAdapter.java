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
package net.modelbased.proasense.adapter.materialmovement;

import net.modelbased.proasense.adapter.file.AbstractFileAdapter;

import eu.proasense.internal.ComplexValue;
import eu.proasense.internal.SimpleEvent;
import eu.proasense.internal.VariableType;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class MaterialMovementFileAdapter extends AbstractFileAdapter {
    public final static Logger logger = Logger.getLogger(MaterialMovementFileAdapter.class);


    public MaterialMovementFileAdapter() {
        super();
    }


    public void splitToCSV(String path) throws FileNotFoundException {
        try {
            convertToSimpleEvent(path);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
    }


    public void convertToSimpleEvent(String filePath) throws IOException, ParseException {
        logger.debug("Processing file = " + filePath);

        File file = new File(filePath);
        Scanner scanner = new Scanner(file);

        while (scanner.hasNextLine()) {
            String removeWhitespace = scanner.nextLine().replace(" ", "");
            String values[] = removeWhitespace.split(",");

            if (values.length != 8) {
                warningMessage(filePath);
                return;
            }

            Map<String, ComplexValue> properties = new HashMap<String, ComplexValue>();

            ComplexValue complexValue = new ComplexValue();
            complexValue.setValue(values[0]);
            complexValue.setType(VariableType.STRING);
            properties.put("materialId", complexValue);

            complexValue = new ComplexValue();
            complexValue.setValue(values[1]);
            complexValue.setType(VariableType.STRING);
            properties.put("sourceType", complexValue);

            complexValue = new ComplexValue();
            complexValue.setValue(values[2]);
            complexValue.setType(VariableType.STRING);
            properties.put("sourceBin", complexValue);

            complexValue = new ComplexValue();
            complexValue.setValue(values[3]);
            complexValue.setType(VariableType.STRING);
            properties.put("destinationType", complexValue);

            complexValue = new ComplexValue();
            complexValue.setValue(values[4]);
            complexValue.setType(VariableType.STRING);
            properties.put("destinationBin", complexValue);

            complexValue = new ComplexValue();
            complexValue.setValue(values[5]);
            complexValue.setType(VariableType.LONG);
            properties.put("destinationQuantity", complexValue);

            complexValue = new ComplexValue();
            complexValue.setValue(convertToTimeMillis(values[6], values[7]));
            complexValue.setType(VariableType.LONG);
            properties.put("creationDate", complexValue);

            SimpleEvent event = this.outputPort.createSimpleEvent(this.sensorId, System.currentTimeMillis(), properties);
            this.outputPort.publishSimpleEvent(event);
            logger.debug("SimpleEvent = " + event);
        }
    }


    private String convertToTimeMillis(String time, String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
        String dateString = date + " " + time;
        logger.debug("convertToTimeMillis().dateString = " + dateString);

        long timestamp = 0;
        try {
            timestamp = dateFormat.parse(dateString).getTime();
        }
        catch (ParseException e) {
            e.printStackTrace();
        }

        return new Long(timestamp).toString();
    }


    public static void main(String[] args) {
        new MaterialMovementFileAdapter();
    }
}
