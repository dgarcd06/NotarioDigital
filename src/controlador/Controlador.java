package controlador;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;

/*
 * La clase Controlador será la encargada de conectar los métodos de Dilithium con la aplicación Notario Digital
 */
public class Controlador {

	private String clave_publica, firma;
	private File archivo_pdf, archivo_firma;
	private double x, y, width, height;
	private final String dir = System.getProperty("user.dir");

	public Controlador(File archivo_pdf) {
		this.archivo_pdf = archivo_pdf;
		this.archivo_firma = new File("datos_firma.txt");
	}

	public void coordenadasRaton(double x, double y, double width, double height) {

	}

	public String getOS() {
		return System.getProperty("os.name");
	}

	public File getArchivoPDF() {
		return this.archivo_pdf;
	}

	
	public String getClavePublica() {
		return this.clave_publica;
	}

	public String getFirma() {
		return this.firma;
	}
	
	public void setClavePublica(String pk) {
		this.clave_publica = pk;
	}
	public void setFirma(String sign) {
		this.firma = sign;
	}

	/**
	 * Ejecuta la firma de Dilithium. Después almacena los datos de la firma para
	 * después utilizarlos en la escritura del PDF.
	 * 
	 * @return 0 si todo está correcto. 1 si hay algún error
	 */
	public int firmar() {
		// Si el sistema es Windows, ejecutar el .exe, sino, ejecutar el otro archivo
		if (this.getOS().equals("Windows 10")) {
			// Ejecutar el .exe
			try {
				File ruta = new File(dir + "\\src\\controlador\\dilithium\\firma.exe");
				String comando[] = { "cmd", "/c", "start cmd.exe /K" };
				// Abre una terminal (cmd en Windows) en el directorio especificado
				Runtime r = Runtime.getRuntime();
				Process p = r.exec(comando);
				
				OutputStreamWriter writer = new OutputStreamWriter(p.getOutputStream());

	            // Escribe el comando para ejecutar firma.exe y cierra la CMD después de ejecutarlo
	            writer.write("firma.exe\nexit\n");
	            writer.flush();
				return p.waitFor();
			} catch (Exception e) {
				System.out.println("Error al firmar: " + e);
				return -1;
			}

		} else {
			// Ejecutar el archivo de UNIX
			try {
				String[] cmd = { "cmd.exe", "/c", "start", "cmd.exe", "/k", "cd",
						dir + "\\src\\controlador\\dilithium", "&&", "exit" };
				String[] cmd2 = {"cmd.exe", "/c"};
				
				Process process = Runtime.getRuntime().exec(cmd2);
				 OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream());
				 writer.write("cd \\src\\controlador\\dilithium");
				 writer.flush();
				int exitCode = process.waitFor();

				return exitCode;
			} catch (Exception e) {
				System.out.println("Error al firmar: " + e);
				return -1;
			}
		}
	}

	public int verificar() {
		// Si el sistema es Windows, ejecutar el .exe, sino, ejecutar el otro archivo
		if (this.getOS().equals("Windows 10")) {
			// Ejecutar el .exe
			try {
				Runtime.getRuntime().exec(dir + "\\src\\controlador\\dilithium\\verificacion.exe");
				return 0;
			} catch (Exception e) {
				System.out.println("Error al verificar: " + e);
				return 1;
			}
		} else {
			// Ejecutar el archivo de UNIX
			try {
				Runtime.getRuntime().exec(dir + "\\src\\controlador\\dilithium\\verificacion");
				return 0;
			} catch (Exception e) {
				System.out.println("Error al firmar: " + e);
				return 1;
			}
		}

	}

	public void leerArchivoFirma() {
		 try (BufferedReader br = new BufferedReader(new FileReader(this.archivo_firma))) {
	            String linea;

	            // Leer cada línea del archivo
	            while ((linea = br.readLine()) != null) {
	                // Dividir la línea en campos usando el signo "=" como separador
	                String[] partes = linea.split("=");

	                // Verificar si la línea contiene un campo válido
	                if (partes.length == 2) {
	                    String campo = partes[0].trim();
	                    String valor = partes[1].trim();

	                    // Asignar el valor a la variable correspondiente
	                    if (campo.equals("pk")) {
	                        setClavePublica(valor);
	                    } else if (campo.equals("sm")) {
	                        setFirma(valor);
	                    }
	                    // Puedes agregar más condiciones para otros campos si es necesario
	                }
	            }
	        } catch (IOException e) {
	            System.err.println("Error al leer el archivo: " + e.getMessage());
	        }
	}
}
