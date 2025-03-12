package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class InsertorCat extends Thread {
	private final String url = "jdbc:postgresql://1.2.130.96:5432/db_gateway";
	private final String user = "kernel";
	private final String pwd = "1234";
	private List<CatSismep> lista = new ArrayList<CatSismep>();
	
	public InsertorCat(List<CatSismep> lista) {
		this.lista = lista;
	}

	public void run() {
		try {
			Connection cnx = DriverManager.getConnection(url, user, pwd);
			//id, matricula, nombre, razon_social, rnp, capitan, eslora
			PreparedStatement ps = cnx.prepareStatement("insert into embarcacion values(?,?,?,?,?,?,?)");
			
			for(CatSismep cs : lista) {
				ps.setString(1, cs.getId());
				ps.setString(2, cs.getMatricula());
				ps.setString(3, cs.getNombre());
				ps.setString(4, cs.getRazonSocial());
				ps.setString(5, cs.getRnp());
				ps.setString(6, cs.getCapitan());
				ps.setFloat(7, cs.getEslora());
				
				ps.execute();
			}
			System.out.println("Cat. Insertados");
			cnx.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
