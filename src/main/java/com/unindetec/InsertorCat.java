package com.unindetec;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsertorCat extends Thread {
	private static final Logger logger = LoggerFactory.getLogger(InsertorCat.class);
	private final String url = Config.get("db.url");
	private final String user = Config.get("db.user");
	private final String pwd = Config.get("db.password");
	private List<CatSismep> lista;

	public InsertorCat(List<CatSismep> lista) {
		this.lista = lista;
	}

	public void run() {
		try (Connection cnx = DriverManager.getConnection(url, user, pwd);
			 PreparedStatement ps = cnx.prepareStatement("INSERT INTO embarcacion VALUES(?,?,?,?,?,?,?)")) {
			for (CatSismep cs : lista) {
				ps.setString(1, cs.getId());
				ps.setString(2, cs.getMatricula());
				ps.setString(3, cs.getNombre());
				ps.setString(4, cs.getRazonSocial());
				ps.setString(5, cs.getRnp());
				ps.setString(6, cs.getCapitan());
				ps.setFloat(7, cs.getEslora());
				ps.execute();
			}
			logger.info("Catálogos insertados.");
		} catch (Exception e) {
			logger.error("Error insertando catálogos", e);
		}
	}
}
