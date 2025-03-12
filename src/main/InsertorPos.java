package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;

public class InsertorPos extends Thread{
	private final String url = "jdbc:postgresql://1.2.130.96:5432/db_gateway";
	private final String user = "kernel";
	private final String pwd = "1234";
	private final String sql = "insert into posiciones (id,latitud,longitud,fecha) values(?,?,?,?) "
			+ "on conflict(id) do update set latitud=excluded.latitud,longitud=excluded.longitud,fecha=excluded.fecha";
	private List<PosSismep> lista;
	
	public InsertorPos(List<PosSismep> lista) {
		this.lista = lista;
	}

	public void run() {
		try {
			Connection cnx = DriverManager.getConnection(url, user, pwd);
			PreparedStatement ps1 = cnx.prepareStatement(sql);
			java.sql.Timestamp fecha;
			java.util.Date fec;
			for(PosSismep pos : lista) {
				ps1.setString(1, pos.getId());
				ps1.setFloat(2, pos.getLatitud());
				ps1.setFloat(3, pos.getLongitud());
				fec = pos.getFecha();
				fecha = new java.sql.Timestamp(fec.getTime());
				ps1.setTimestamp(4, fecha);
				ps1.addBatch();
			}
			ps1.executeBatch();
			System.out.println("Pos. Insertados");
			cnx.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
