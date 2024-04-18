package controlador;
/*
 * La clase Controlador será la encargada de conectar los métodos de Dilithium con la aplicación Notario Digital
 */
public class Controlador {

	private long clave_publica, firma;
	
	public Controlador() {
		
	}
	
	
	public void coordenadasRaton(double x, double y, double width, double height) {
		
	}
	
	public long getClavePublica() {
		return this.clave_publica;
	}

	public long getFirma() {
		return this.firma;
	}
	
	//ESTOS METODOS NO DEBEN EXISTIR; SUSTITUIR POR LA LLAMADA A LAS FUNCIONES DE DILITHIUM
	public void setClavePublica(long clave_publica) {
		this.clave_publica = clave_publica;
	}
	
	
	public void setFirma(long firma) {
		this.firma = firma;
		
	}
}
