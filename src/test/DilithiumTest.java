package test;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.security.Security;
import java.security.cert.X509Certificate;

import org.bouncycastle.pqc.crypto.crystals.dilithium.DilithiumSigner;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import modelo.FirmaDigital;


class DilithiumTest {
	FirmaDigital firmaDigital2 = new FirmaDigital(2);
	FirmaDigital firmaDigital3 = new FirmaDigital(3);
	FirmaDigital firmaDigital5 = new FirmaDigital(5);
	
	byte[] mensaje = "¡Test de Dilithium!".getBytes();
	DilithiumSigner signer  = new DilithiumSigner();
	DilithiumSigner verifier  = new DilithiumSigner();
	X509Certificate certificado;
	
	@BeforeEach
	void setup() {
		Security.addProvider(new BouncyCastlePQCProvider());
		assertNotNull(Security.getProvider("BCPQC"));
	}
	@Test
	void testDilithiumMode() {
		assertEquals(2,firmaDigital2.getDilithiumMode());
		assertEquals(3,firmaDigital3.getDilithiumMode());
		assertEquals(5,firmaDigital5.getDilithiumMode());
	}
	@Test
	void testGetCertificado() throws IOException {
		assertSame(certificado, firmaDigital2.getCertificado());
		assertSame(certificado, firmaDigital3.getCertificado());
		assertSame(certificado, firmaDigital5.getCertificado());
		firmaDigital2.codigoFirma(2);
		assertNotNull(firmaDigital2.getFirma());
		assertEquals(firmaDigital2.getClavePublica(), firmaDigital2.getCertificado().getPublicKey());
	}
}
