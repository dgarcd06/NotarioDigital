package controlador;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;

/**
 *  * Proyecto realizado como parte del desarrollo del Trabajo de Fin de Grado:
 * "Análisis de algoritmos resistentes a ataques cuánticos: Notario Digital"
 * por la Universidad de León.
 * Las librerías externas empleadas en el código (Apache PDFBox, Bouncy Castle y icePDF)
 * contienen una licencia de uso libre, siendo usadas en su formato original, exceptuando
 * icePDF, de las cuales se ha modificado el código para personalizar la experiencia del usuario
 * (por ello en el paquete vista se incluyen las clases DefaultIconPack.java, IconPack.java,
 * Images.java, SwingController.java y SwingViewBuilder.java).
 * @author David García Diez
 */
public class Main {

	/**
	 * Java, en su librería Security incluye la función addProvider, la cual sirve para
	 * añadir a un proveedor de servicios de seguridad. Esta llamada se ejecuta para que las 
	 * librerías nativas de Java detecten los métodos y algoritmos que define Bouncy Castle,
	 * es decir, en este caso para trabajar con Dilithium.
	 */
	static {
        Security.addProvider(new BouncyCastleProvider());
        Security.addProvider(new BouncyCastlePQCProvider());
    }
	
	public static void main(String[] args) {
		NotarioDigital n = new NotarioDigital();
		n.setVisible(true);
	}
}
