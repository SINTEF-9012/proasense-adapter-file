package net.modelbased.proasense.adapter.montrac;

import eu.proasense.internal.ComplexValue;
import eu.proasense.internal.VariableType;
import net.modelbased.proasense.adapter.abstractFile.WatchDirectory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by shahzad on 18.07.15.
 */
public class PathClass extends WatchDirectory {

    public static void main(String[] args) throws IOException, InterruptedException {

        new PathClass(args[1]);
    }

    PathClass(String path) throws IOException, InterruptedException {

        scanDirectory(path);
    }

    int i = 0;
    public void convertToSimpleEvent(String filePath) throws FileNotFoundException {
        System.out.println("er i readFiles(); " + filePath);
        File file = new File(filePath);
        Scanner scanner = new Scanner(file);
        Map<String, ComplexValue> properties = new HashMap<String, ComplexValue>();
//        SimpleEvent se = new SimpleEvent();
        ComplexValue complexValue = new ComplexValue();

        String values[] = scanner.nextLine().split(",");

        long timestamp = new Timestamp(System.currentTimeMillis()).getTime();
        System.out.println("timestamp er "+timestamp);

        String sensorId = values[1];

        System.out.println("sensorId er "+sensorId);

        complexValue.setValue(values[2]);
        complexValue.setType(VariableType.STRING);
        properties.put("event", complexValue);
        System.out.println("event = "+values[2]);

        complexValue.setValue(values[3]);
        complexValue.setType(VariableType.LONG);
        properties.put("shuttle", complexValue);
        System.out.println("shuttle er = "+values[3]);

        String leftPiece[] = values[4].split("=");

        complexValue.setValue(leftPiece[1]);
        complexValue.setType(VariableType.STRING);
        properties.put("leftPiece", complexValue);
        System.out.println("leftpiece er = "+leftPiece[1]);

        String rightPiece[] = values[5].split("=");

        complexValue.setValue(rightPiece[1]);
        complexValue.setType(VariableType.STRING);
        properties.put("rightPiece", complexValue);
        System.out.println("rightPiece er = "+rightPiece[1]);

// fluytt over pom filen til twitter-maven., se om complexValue da fungerer.

    }
}
