
package com.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PerformanceTest {

    // --- 1. 테스트용 데이터 모델 클래스 ---
    static class SampleData {

        int id;
        String name;
        String email;
        boolean isActive;
        List<String> permissions;
        Map<String, Object> nestedData; // 중첩 구조를 위한 Map

        public SampleData(int id, String name, String email, boolean isActive) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.isActive = isActive;
            this.permissions = new ArrayList<>(List.of("read", "write"));
            this.nestedData = new LinkedHashMap<>();
        }
    }

    public static void main(String[] args) throws Exception {
//        // 시나리오 1: 비교적 간단한 데이터 구조
//        runPerformanceAnalysis(1000, 1);
//
//        // 시나리오 2: 깊고 복잡한 데이터 구조
//        runPerformanceAnalysis(1000, 5);

        runPerformanceAnalysis(500, 10);

        runPerformanceAnalysis(1000, 10);
    }

    // --- 2. 테스트 데이터 생성 ---
    public static List<SampleData> generateSampleData(int numRecords, int depth) {
        List<SampleData> records = new ArrayList<>();
        for (int i = 0; i < numRecords; i++) {
            SampleData record = new SampleData(i, "user_" + i, "user_" + i + "@example.com", true);

            // 지정된 깊이만큼 Map을 중첩하여 복잡성 증가
            Map<String, Object> currentLevel = record.nestedData;
            for (int d = 0; d < depth; d++) {
                Map<String, Object> nextLevel = new LinkedHashMap<>();
                currentLevel.put("nested_level_" + (d + 1), nextLevel);
                currentLevel = nextLevel;
                currentLevel.put("data", "some_data_at_depth_" + (d + 1));
            }
            records.add(record);
        }
        return records;
    }

    // --- 3. 성능 분석 실행 ---
    public static void runPerformanceAnalysis(int numRecords, int depth) throws Exception {
        System.out.println(" ============================================================");
        System.out.printf(" 분석 시작: 레코드 %d개, 중첩 깊이 %d \n", numRecords, depth);
        System.out.println("============================================================");

        List<SampleData> sampleData = generateSampleData(numRecords, depth);

        // --- JSON 성능 분석 ---
        System.out.println("--- JSON 성능 분석 ---");
        Gson gson = new GsonBuilder().setPrettyPrinting().create(); // 가독성을 위해 PrettyPrinting 사용

        // 직렬화 (Java Object -> JSON String)
        long startTime = System.nanoTime();
        String jsonString = gson.toJson(sampleData);
        long jsonSerializationTime = (System.nanoTime() - startTime) / 1_000_000; // ms
        long jsonSize = jsonString.getBytes(StandardCharsets.UTF_8).length;

        // 역직렬화 (JSON String -> Java Object)
        startTime = System.nanoTime();
        SampleData[] dataFromJason = gson.fromJson(jsonString, SampleData[].class);
        long jsonDeserializationTime = (System.nanoTime() - startTime) / 1_000_000; // ms

        System.out.printf("직렬화 (객체->문자열) 시간: %d ms \n", jsonSerializationTime);
        System.out.printf("역직렬화 (문자열->객체) 시간: %d ms \n", jsonDeserializationTime);
        System.out.printf("데이터 크기: %,d 바이트 \n", jsonSize);

        // --- XML 성능 분석 ---
        System.out.println(" -- - XML 성능 분석-- - ");
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // 직렬화 (Java Object -> XML String)
        startTime = System.nanoTime();
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("root");
        doc.appendChild(rootElement);

        for (SampleData data : sampleData) {
            Element recordElem = doc.createElement("record");
            rootElement.appendChild(recordElem);

            // 재귀 함수를 사용하지 않고 간단하게 구현
            createElement(doc, recordElem, "id", String.valueOf(data.id));
            createElement(doc, recordElem, "name", data.name);
            createElement(doc, recordElem, "email", data.email);
            createElement(doc, recordElem, "isActive", String.valueOf(data.isActive));

            Element permsElem = doc.createElement("permissions");
            for (String p : data.permissions) {
                createElement(doc, permsElem, "permission", p);
            }
            recordElem.appendChild(permsElem);

            // 중첩된 Map을 XML로 변환
            Element nestedElem = doc.createElement("nestedData");
            mapToXml(doc, nestedElem, data.nestedData);
            recordElem.appendChild(nestedElem);
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // 가독성 확보
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        String xmlString = writer.toString();
        long xmlSerializationTime = (System.nanoTime() - startTime) / 1_000_000;
        long xmlSize = xmlString.getBytes(StandardCharsets.UTF_8).length;

        // 역직렬화 (XML String -> Java Object)
        startTime = System.nanoTime();
        Document parsedDoc = docBuilder.parse(
            new org.xml.sax.InputSource(new StringReader(xmlString)));
        NodeList recordNodes = parsedDoc.getElementsByTagName("record");
        List<SampleData> dataFromXml = new ArrayList<>();
        for (int i = 0; i < recordNodes.getLength(); i++) {
            // XML을 다시 객체로 만드는 과정은 복잡하므로 여기서는 파싱 자체의 시간만 측정
        }
        long xmlDeserializationTime = (System.nanoTime() - startTime) / 1_000_000;

        System.out.printf("직렬화 (객체->문자열) 시간: %d ms \n", xmlSerializationTime);
        System.out.printf("역직렬화 (문자열->DOM) 시간: %d ms \n", xmlDeserializationTime);
        System.out.printf("데이터 크기: %,d 바이트 \n", xmlSize);

        // --- 최종 비교 ---
        System.out.println(" -- - 최종 비교-- - ");
        System.out.printf("데이터 크기 비교: XML이 JSON보다 %.2f배 더 큼 \n", (double) xmlSize / jsonSize);
        System.out.printf("직렬화 속도 비교: XML이 JSON보다 %.2f배 더 느림 \n",
            (double) xmlSerializationTime / jsonSerializationTime);
        System.out.printf("역직렬화(파싱) 속도 비교: XML이 JSON보다 %.2f배 더 느림 \n",
            (double) xmlDeserializationTime / jsonDeserializationTime);
    }

    // Helper to create element
    private static void createElement(Document doc, Element parent, String tagName,
        String textContent) {
        Element element = doc.createElement(tagName);
        element.appendChild(doc.createTextNode(textContent));
        parent.appendChild(element);
    }

    // Helper to convert Map to XML
    private static void mapToXml(Document doc, Element parent, Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            Element elem = doc.createElement(key);
            if (value instanceof Map) {
                mapToXml(doc, elem, (Map<String, Object>) value);
            } else {
                elem.setTextContent(value.toString());
            }
            parent.appendChild(elem);
        }
    }
}
