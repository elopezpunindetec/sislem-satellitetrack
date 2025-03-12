package main;

//import java.text.SimpleDateFormat;
import java.util.Date;

public class PosSismep {
	
	private String id;
	private float latitud;
	private float longitud;
	private Date fecha;
	private boolean enEmergencia;
	private boolean enPuerto;
	private boolean enZonaPescaProhibida;
	private boolean enAreaProhibida;

	public PosSismep(String id, float lat, float lng, boolean emergencia) {
		//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		this.id = id.trim();
		latitud = lat;
		longitud = lng;
		fecha = new Date();
		enEmergencia = emergencia;
		enPuerto = Math.random() < 0.5;
		enZonaPescaProhibida = Math.random() < 0.5;
		enAreaProhibida = Math.random() < 0.5;
	}
	
	public PosSismep(String id, float lat, float lng, Date fecha, boolean emergencia) {
		this.id = id.trim();
		latitud = lat;
		longitud = lng;
		this.fecha = fecha;
		enEmergencia = emergencia;
		enPuerto = false;
		enZonaPescaProhibida = false;
		enAreaProhibida = false;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public float getLatitud() {
		return latitud;
	}

	public void setLatitud(float latitud) {
		this.latitud = latitud;
	}

	public float getLongitud() {
		return longitud;
	}

	public void setLongitud(float longitud) {
		this.longitud = longitud;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public boolean isEnEmergencia() {
		return enEmergencia;
	}

	public void setEnEmergencia(boolean enEmergencia) {
		this.enEmergencia = enEmergencia;
	}

	public boolean isEnPuerto() {
		return enPuerto;
	}

	public void setEnPuerto(boolean enPuerto) {
		this.enPuerto = enPuerto;
	}

	public boolean isEnZonaPescaProhibida() {
		return enZonaPescaProhibida;
	}

	public void setEnZonaPescaProhibida(boolean enZonaPescaProh) {
		this.enZonaPescaProhibida = enZonaPescaProh;
	}

	public boolean isEnAreaProhibida() {
		return enAreaProhibida;
	}

	public void setEnAreaProhibida(boolean enAreaProh) {
		this.enAreaProhibida = enAreaProh;
	}
}
