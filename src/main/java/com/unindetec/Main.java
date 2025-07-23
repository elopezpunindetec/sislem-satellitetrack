package com.unindetec;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        logger.info("Iniciando sislem subscriber");

        // Load config values
        final String url = Config.get("db.url");
        final String user = Config.get("db.user");
        final String pwd = Config.get("db.password");
        final String kafkaBootstrap = Config.get("kafka.bootstrap.servers");
        final String kafkaGroupId = Config.get("kafka.group.id");
        final String kafkaTopic = Config.get("kafka.topic");
        final String catalogFileName = Config.get("catalog.file");

        Properties kafkaProps = new Properties();
        kafkaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrap);
        kafkaProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        kafkaProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        kafkaProps.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaGroupId);

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(kafkaProps);

        Queue<CatSismep> buques;
        List<CatSismep> nuevos = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        List<String> matriculas = new ArrayList<>();
        List<PosSismep> posNuevos = new ArrayList<>();

        try {
            // Load embarcaciones catalog from resources
            ClassLoader classLoader = Main.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(catalogFileName);
            if (inputStream == null) throw new FileNotFoundException(catalogFileName + " not found");

            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String linea;
            String[] valores;
            CatSismep cat;
            float tmpf;
            while ((linea = br.readLine()) != null) {
                valores = linea.trim().split("\\|");
                tmpf = Float.parseFloat(valores[12]);
                cat = new CatSismep("", valores[8], valores[7], valores[5], valores[6], "", tmpf);
                nuevos.add(cat);
            }
            br.close();
            logger.info("En el archivo {}", nuevos.size());

            // Load existing embarcaciones from DB
            try (Connection cnx = DriverManager.getConnection(url, user, pwd);
                 Statement st = cnx.createStatement();
                 ResultSet rs = st.executeQuery("SELECT id, matricula FROM embarcacion")) {

                while (rs.next()) {
                    ids.add(rs.getString(1));
                    matriculas.add(rs.getString(2));
                }
            }
            logger.info("En la bd {}", matriculas.size());

            // Filter new embarcaciones
            nuevos = nuevos.stream()
                    .filter(b -> !matriculas.contains(b.getMatricula()))
                    .collect(Collectors.toList());
            buques = new LinkedList<>(nuevos);
            logger.info("Diferencia {}", buques.size());

            // Kafka subscription
            consumer.subscribe(Collections.singletonList(kafkaTopic));

            int c = 1;
            nuevos.clear();
            ConsumerRecords<String, String> records;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
            Document doc;
            NodeList nodos;
            Element elem;
            String temp, id, msj;
            float lat, lng;
            Date d;
            ShipInserter insCat;
            PositionInserter insPos;

            while (true) {
                records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    msj = record.value();
                    if (msj.contains("kml xmlns")) {
                        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                                .parse(new ByteArrayInputStream(msj.getBytes()));
                        doc.getDocumentElement().normalize();

                        nodos = doc.getElementsByTagName("Data");
                        elem = (Element) nodos.item(1);
                        temp = elem.getElementsByTagName("value").item(0).getTextContent();
                        d = sdf.parse(temp);

                        nodos = doc.getElementsByTagName("Placemark");
                        elem = (Element) nodos.item(0);
                        id = elem.getElementsByTagName("name").item(0).getTextContent();

                        if (!ids.contains(id)) {
                            cat = buques.poll();
                            if (cat != null) {
                                cat.setId(id);
                                ids.add(id);
                                nuevos.add(cat);
                            }
                            if (c == 3) {
                                logger.info("Enviando a insertar catálogos");
                                insCat = new ShipInserter(nuevos);
                                Thread thread = new Thread(insCat);
                                thread.start();
                                nuevos.clear();
                                c = 0;
                            }
                            c++;
                        }

                        nodos = doc.getElementsByTagName("Point");
                        elem = (Element) nodos.item(0);
                        temp = elem.getElementsByTagName("coordinates").item(0).getTextContent();
                        valores = temp.split(",");
                        lat = Float.parseFloat(valores[1]);
                        lng = Float.parseFloat(valores[0]);
                        posNuevos.add(new PosSismep(id, lat, lng, d, false));

                        if (posNuevos.size() == 1) {
                            logger.info("Enviando a insertar posiciones");
                            insPos = new PositionInserter(posNuevos);
                            Thread thread = new Thread(insPos);
                            thread.start();
                            posNuevos.clear();
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error en sislem subscriber", e);
        } finally {
            consumer.close();
        }
    }
}