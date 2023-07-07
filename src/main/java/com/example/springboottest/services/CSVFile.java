package com.example.springboottest.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CSVFile {
    private List<List<String>> csvData = new ArrayList<>();
    private static final String CSV_URL_COLUMN_NAME = "spwUrl";
    private static final String VALUE_DELIMITER = ",";
    private static final String DATA_DELIMITER = "\",\"";
    private static final String DATA_SURROUNDINGS = "\"";
    private static final String CSV_SEPARATOR = "sep=,";
    private static final String ROW_SEPARATOR = "\n";
    private final int URL_COLUMN_INDEX;
    private static final int STATUS_ROW_INDEX = 0;

    public CSVFile(List<String> csvFileRowList) {
        csvData = getCsvDataFromRowList(csvFileRowList);
        URL_COLUMN_INDEX = getUrlIndex(csvData.get(0));
    }

    public int size() {
        return csvData.size();
    }

    private List<List<String>> getCsvDataFromRowList(List<String> rowList) {
        List<List<String>> dataAsList = new ArrayList<>();
        List<String> data = new ArrayList<>(List.of(rowList.get(1).split(VALUE_DELIMITER)));
        dataAsList.add(data);
        for (int i = 2; i < rowList.size(); i++) {
            data = getValuesInQuotes(rowList.get(i));
            dataAsList.add(data);
        }
        return dataAsList;
    }

    public List<String> getAllPageUrlAsList() {
        List<String> pageUrlList = new ArrayList<>();
        for (int i = 1; i < size(); i++) {
            pageUrlList.add(getUrlByIndex(i));
        }
        return pageUrlList;
    }

    public void writeCSVFile(String filePath) throws IOException {
        File file = new File(filePath);
        FileWriter csvWriter = new FileWriter(file);
        String dataAsString = getDataAsString();
        csvWriter.append(dataAsString);
        csvWriter.close();
    }

    public String getDataAsString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(CSV_SEPARATOR).append(ROW_SEPARATOR);
        stringBuilder.append(String.join(VALUE_DELIMITER, csvData.get(STATUS_ROW_INDEX))).append(ROW_SEPARATOR);
        for (int i = 1; i < csvData.size(); i++) {
            stringBuilder.append(DATA_SURROUNDINGS).append(String.join(DATA_DELIMITER, csvData.get(i))).append(DATA_SURROUNDINGS).append(ROW_SEPARATOR);
        }
        return stringBuilder.toString();
    }

    public byte[] getDataAsByteArray() {
        StringBuilder stringBuilder = new StringBuilder();
        for (List<String> dataRow : csvData) {
            stringBuilder.append(Arrays.toString(convertListToByteArray(dataRow)));
        }
        return stringBuilder.toString().getBytes();
    }

    public byte[] convertListToByteArray(List<String> stringList) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String str : stringList) {
            stringBuilder.append(str);
        }
        String concatenatedString = stringBuilder.toString();

        return concatenatedString.getBytes();
    }


    public List<String> getValuesInQuotes(String dataRow) {
        List<String> values = new ArrayList<>();

        Pattern pattern = Pattern.compile(DATA_SURROUNDINGS + "([^" + DATA_SURROUNDINGS + "]*)" + DATA_SURROUNDINGS);
        Matcher matcher = pattern.matcher(dataRow);

        while (matcher.find()) {
            String value = matcher.group(1);
            values.add(value);
        }

        return values;
    }

    public List<String> getDataRowByIndex(int rowIndex) {
        return csvData.get(rowIndex);
    }

    public void appendStatus(Map<String, String> statusMap, String webImage) {
        int colIndex;
        int dataRowIndex = -1;
        for (int i = 1; i < csvData.size(); i++) {
            if (csvData.get(i).get(URL_COLUMN_INDEX).equals(webImage)) {
                dataRowIndex = i;
                break;
            }
        }

        for (Map.Entry<String, String> entry : statusMap.entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue();
            if (!csvData.get(0).contains(k)) {
                csvData.get(0).add(k);
                colIndex = getColumnIndex(k);
                for (int i = 1; i < csvData.size(); i++) {
                    csvData.get(i).add(colIndex, "");
                }
            }
            colIndex = getColumnIndex(k);
            csvData.get(dataRowIndex).set(colIndex, v);
        }
    }

    private int getColumnIndex(String value) {
        List<String> valueRow = csvData.get(0);
        for (int i = 0; i < valueRow.size(); i++) {
            if (valueRow.get(i).equals(value)) {
                return i;
            }
        }
        return -1;
    }

    public void readResultLog() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("src/main/resources/config/config.properties"));
        String logFilePath = properties.getProperty("logFilePath");
        try (Scanner scanner = new Scanner(new File(logFilePath))) {
            scanner.nextLine();
            String resultRow = scanner.nextLine();
            while (scanner.hasNext()) {
                resultRow = scanner.nextLine();
                if (scanner.hasNext()) {
                    appendResult(resultRow);
                }
            }
        }

    }

    public void appendResult(String resultRow) {
        Map<String, String> websiteStatusMap = new HashMap<>();
        List<String> listOfResult = Stream.of(resultRow.split(";")).map(String::trim).collect(Collectors.toList());

        if (!(listOfResult.size() < 6)) {
            listOfResult = listOfResult.stream().map(String::trim).collect(Collectors.toList());
            websiteStatusMap.put("website status", listOfResult.get(2));
            websiteStatusMap.put("image count", listOfResult.get(3));
            websiteStatusMap.put("image opened", listOfResult.get(4));
            websiteStatusMap.put("time elapsed", listOfResult.get(5));
            appendStatus(websiteStatusMap, listOfResult.get(1).substring(8));
        }
    }

    public String getUrlByIndex(int index) {
        return csvData.get(index).get(URL_COLUMN_INDEX);
    }

    public static int getUrlIndex(List<String> data) {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).equals(CSV_URL_COLUMN_NAME)) return i;
        }
        return -1;
    }
}
