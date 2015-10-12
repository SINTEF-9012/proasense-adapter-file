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
package net.modelbased.proasense.adapter.productionplan;

import eu.proasense.internal.ComplexValue;
import eu.proasense.internal.SimpleEvent;
import eu.proasense.internal.VariableType;
import net.modelbased.proasense.adapter.file.AbstractFileAdapter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;

public class ProductionPlanFileAdapter extends AbstractFileAdapter {


    public ProductionPlanFileAdapter() {
        super();
    }


    public void convertToSimpleEvent(String filePath) throws IOException, ParseException {
        checkExcelRows(filePath);
    }

    public void splitToCSV(String path) throws FileNotFoundException, ParseException {

        FileReader file = new FileReader(path);
        Scanner sc = new Scanner(file);

        while(sc.hasNext()){
            String firstElement = splitLine(sc.next());
            String receipt = sc.next();
            String quantety = sc.next();
            String companyCode = sc.next();
           // String scheduledFinish = sc.next();

            String csvString = firstElement+","+receipt+","+quantety+","+companyCode;
            splitAndPublishEvents(csvString);
        }
    }

    String splitLine(String line){
        char[] lineToArray = line.toCharArray();
        String MRPElement = lineToArray[0]+""+lineToArray[1];
        String plant = lineToArray[2]+""+lineToArray[3]+""+lineToArray[4]+""+lineToArray[5];
        String materials = "";

        for(int i = 6; i < lineToArray.length; i++){
            materials += lineToArray[i];
        }

        String doneString = MRPElement+","+plant+","+materials;
        return doneString;
    }

    public void checkExcelRows(String filePath) throws IOException, ParseException {
        FileInputStream inputStream = new FileInputStream(new File(filePath));

        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet firstSheet = workbook.getSheetAt(0);
        Iterator<Row> iterator = firstSheet.iterator();
        int i = 1;
        while (iterator.hasNext()) {
            Row nextRow = iterator.next();
            Iterator<Cell> cellIterator = nextRow.cellIterator();
            // String row = ""+i+",";
            String row = "";
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();

                switch (cell.getCellType()) {
                    case Cell.CELL_TYPE_STRING:
                        row += cell.getStringCellValue();
                        break;
                    case Cell.CELL_TYPE_NUMERIC:
                        row += String.valueOf(cell.getNumericCellValue());
                        //String x = cell.getStringCellValue();
                        break;
                }

                if (cellIterator.hasNext()) {
                    row += ",";
                }

            }
            i++;
            if (row.startsWith("MRP element")) {
                continue;
            } else {
                if (cnt == 0) {
                    cnt++;
                    return;
                }
                splitAndPublishEvents(row);
            }
        }
        workbook.close();
        inputStream.close();
    }


    int cnt = 0;
    void splitAndPublishEvents(String rows) throws ParseException {

        String[] rowValue = rows.split(",");
        String MRAP_Element = rowValue[0];
        String plant = rowValue[1];
        String materials = rowValue[2];

        Date date = new Date();
        String receipt_requirement_date = rowValue[3];
        String modifiedDate = convertDate(receipt_requirement_date);

        String longDate = "";

        if (modifiedDate.equals("0")) {
            longDate = modifiedDate;
        } else {
            DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
            Date date1 = format.parse(modifiedDate);
            longDate = String.valueOf(date1.getTime());
        }

        String quantity = rowValue[4];
        String company_code = rowValue[5];
//        String Scheduled_finish = rowValue[6];


        SimpleEvent simpleEvent = new SimpleEvent();

        simpleEvent.setSensorId(sensorId);
        simpleEvent.setTimestamp(date.getTime());

        ComplexValue complexValue = new ComplexValue();
        complexValue.setValue(MRAP_Element);
        complexValue.setType(VariableType.STRING);
        simpleEvent.putToEventProperties("mrpElement", complexValue);

        complexValue = new ComplexValue();
        complexValue.setValue(materials);
        complexValue.setType(VariableType.STRING);
        simpleEvent.putToEventProperties("materials", complexValue);

        complexValue = new ComplexValue();
        complexValue.setValue(longDate);
        complexValue.setType(VariableType.LONG);
        simpleEvent.putToEventProperties("plannedDate", complexValue);

        complexValue = new ComplexValue();
        complexValue.setValue(quantity);
        complexValue.setType(VariableType.LONG);
        simpleEvent.putToEventProperties("quantity", complexValue);

        outputPort.publishSimpleEvent(simpleEvent);
        logger.debug(simpleEvent.toString());
    }


    String convertDate(String date) {
        String[] dateSplit = date.split("\\.");

        if (date.equals("0.0") || date.equals("00000000")){
            return "0";
        }else if(dateSplit.length == 1) {
            String changeDateFormat = date.substring(0,4)+"/"+date.substring(4,6)+"/"+date.substring(6,8);
            logger.debug("formatted date is "+changeDateFormat);
            return changeDateFormat;
        }else{
            String newFormat = dateSplit[0] + "" + dateSplit[1].substring(0, 7);
            char[] modifyDate = newFormat.toCharArray();
            String finalDate = modifyDate[0] + "" + modifyDate[1] + "" + modifyDate[2] + "" + modifyDate[3] + "/" + modifyDate[4] + "" + modifyDate[5] + ""
                    + "/" + modifyDate[6] + "" + modifyDate[7];

            return finalDate;
        }
    }


    public static void main(String[] args) {
        new ProductionPlanFileAdapter();
    }

}
