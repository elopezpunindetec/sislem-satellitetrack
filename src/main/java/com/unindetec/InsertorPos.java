package com.unindetec;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsertorPos extends Thread {
	private static final Logger logger = LoggerFactory.getLogger(InsertorPos.class);
	private final String url = Config.get("db.url");
	private final String user = Config.get("db.user");
	private final String pwd = Config.get("db.password");
	private final String sql = "INSERT INTO posiciones (id, latitud, longitud, fecha) VALUES (?, ?, ?, ?) " +
			"ON CONFLICT(id) DO UPDATE SET latitud=excluded.latitud, longitud=excluded.longitud, fecha=excluded.fecha";
	private List<PosSismep> lista;

	public InsertorPos(List<PosSismep> lista) {
		this.lista = lista;
	}

	public void run() {
		try (Connection cnx = DriverManager.getConnection(url, user, pwd);
			 PreparedStatement ps1 = cnx.prepareStatement(sql)) {
			for (PosSismep pos : lista) {
				ps1.setString(1, pos.getId());
				ps1.setFloat(2, pos.getLatitud());
				ps1.setFloat(3, pos.getLongitud());
				ps1.setTimestamp(4, new java.sql.Timestamp(pos.getFecha().getTime()));
				ps1.addBatch();
			}
			ps1.executeBatch();
			logger.info("Posiciones insertadas.");
		} catch (Exception e) {
			logger.error("Error insertando posiciones", e);
		}
	}
}
