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
import java.util.*;

/**
 * Created by shahzad on 18.07.15.
 */
public class MontracFileAdapter extends AbstractFileAdapter {

    public static void main(String[] args) throws IOException, InterruptedException {

        new MontracFileAdapter();
    }

    MontracFileAdapter() throws IOException, InterruptedException {

        scanDirectory(rootDirectoryPath, delayValue);
    }


    public void convertToSimpleEvent(String filePath) throws FileNotFoundException {
        System.out.println("er i readFiles(); " + filePath);
        File file = new File(filePath);
        Scanner scanner = new Scanner(file);
        Map<String, ComplexValue> properties = new HashMap<String, ComplexValue>();

        ComplexValue complexValue = new ComplexValue();

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
        complexValue.setValue(values[2]);
        complexValue.setType(VariableType.STRING);
        properties.put("event", complexValue);
        System.out.println("event = " + values[2]);

        complexValue.setValue(values[3]);
        complexValue.setType(VariableType.LONG);
        properties.put("shuttle", complexValue);
        System.out.println("shuttle er = " + values[3]);

        String leftPiece[] = values[4].split("=");

        complexValue.setValue(leftPiece[1]);
        complexValue.setType(VariableType.STRING);
        properties.put("leftPiece", complexValue);
        System.out.println("leftpiece er = " + leftPiece[1]);

        String rightPiece[] = values[5].split("=");

        complexValue.setValue(rightPiece[1]);
        complexValue.setType(VariableType.STRING);
        properties.put("rightPiece", complexValue);
        System.out.println("rightPiece er = " + rightPiece[1]);

        SimpleEvent event = this.outputPort.createSimpleEvent(sensorId, timestamp, properties);
        this.outputPort.publishSimpleEvent(event);

    }
}
