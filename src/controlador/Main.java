package controlador;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;

public class Main {

	static {
        Security.addProvider(new BouncyCastleProvider());
        Security.addProvider(new BouncyCastlePQCProvider());
    }
	
	public static void main(String[] args) {
		NotarioDigital n = new NotarioDigital();
		n.setVisible(true);
	}
}
