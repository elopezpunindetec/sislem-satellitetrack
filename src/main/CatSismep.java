package main;

public class CatSismep {
	private String id;
	private String matricula;
	private String nombre;
	private String razonSocial;
	private String rnp;
	private String capitan;
	private float eslora;

	public CatSismep(String id, String matricula, String nombre, String razon, String rnp, String capitan, float eslora) {
		this.id = id.trim();
		this.matricula = matricula.trim();
		this.nombre = nombre.trim();
		razonSocial = razon.trim();
		this.rnp = rnp.trim();
		this.capitan = capitan.trim();
		this.eslora = eslora;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMatricula() {
		return matricula;
	}

	public void setMatricula(String matricula) {
		this.matricula = matricula;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getRazonSocial() {
		return razonSocial;
	}

	public void setRazonSocial(String razon_social) {
		this.razonSocial = razon_social;
	}

	public String getRnp() {
		return rnp;
	}

	public void setRnp(String rnp) {
		this.rnp = rnp;
	}

	public String getCapitan() {
		return capitan;
	}

	public void setCapitan(String capitan) {
		this.capitan = capitan;
	}

	public float getEslora() {
		return eslora;
	}

	public void setEslora(float eslora) {
		this.eslora = eslora;
	}
}
