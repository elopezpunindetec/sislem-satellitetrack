package main;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

public class SuscriptorInsertor {

	public static void main(String[] args) {
		Queue<CatSismep> buques = new LinkedList<CatSismep>();
		List<CatSismep> nuevos = new ArrayList<CatSismep>();
		List<String> ids = new ArrayList<String>();
		List<String> matriculas = new ArrayList<String>();
		List<PosSismep> posNuevos = new ArrayList<PosSismep>();
		final String url = "jdbc:postgresql://1.2.130.96:5432/db_gateway";
		final String user = "kernel";
		final String pwd = "1234";
		
		try {
			String linea; /* Línea leída */
			FileReader fr = new FileReader(new File("Embarcaciones.txt"));
			BufferedReader br = new BufferedReader(fr); 
			String valores[], temp, id;
			CatSismep cat;
			InsertorCat insCat;
			InsertorPos insPos;
			float tmpf, lat, lng;
			int c;
			Document doc; 
			NodeList nodos; 
			Element elem; 
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S"); 
			Date d;
			Connection cnx = DriverManager.getConnection(url, user, pwd);
			Statement st = cnx.createStatement();
			ResultSet rs = st.executeQuery("select id,matricula from embarcacion");
			
			while(rs.next()) {				
				ids.add(rs.getString(1));
				matriculas.add(rs.getString(2));
			}			
			cnx.close();
			System.out.println("En la bd " + matriculas.size());
			while ((linea = br.readLine()) != null) {
				valores = linea.trim().split("\\|");
				tmpf = Float.parseFloat(valores[12]);
				cat = new CatSismep("",valores[8],valores[7],valores[5],valores[6],"",tmpf);
				nuevos.add(cat);
			}
			br.close();
			System.out.println("En el archivo " + nuevos.size());
			nuevos = nuevos.stream()
					.filter(b -> !matriculas.contains(b.getMatricula()))
					.collect(Collectors.toList());
			buques = new LinkedList<CatSismep>(nuevos);
			System.out.println("Diferencia " + buques.size());
			
			Context ctx=ZMQ.context(1);
			Socket subs=ctx.socket(ZMQ.SUB);
			String msj;
			
			subs.connect("tcp://5.1.7.38:4023");
			subs.subscribe("".getBytes());
			c = 1;
			nuevos.clear();
			while(!Thread.currentThread().isInterrupted()){
				msj = subs.recvStr(0);
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
						cat.setId(id);
						ids.add(id);
						nuevos.add(cat);
						if(c == 3) {
							//insertar
							System.out.println("Enviando a insertar catálogos");
							insCat = new InsertorCat(nuevos);
							insCat.run();
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
					if(posNuevos.size() == 25) {
						System.out.println("Enviando a insertar posiciones");
						insPos = new InsertorPos(posNuevos);
						insPos.run();
						posNuevos.clear();
					}
				}
			}
			subs.close();
			c = 0;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
