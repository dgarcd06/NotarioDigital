package modelo;

import java.security.Security;

import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;

public class Main {

	public static void main(String[] args) {
		Security.addProvider(new BouncyCastlePQCProvider());
		NotarioDigital n = new NotarioDigital();
		n.setVisible(true);
	}

}
