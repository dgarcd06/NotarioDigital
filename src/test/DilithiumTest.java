package test;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.security.Security;
import java.security.cert.X509Certificate;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.pqc.crypto.crystals.dilithium.DilithiumPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.crystals.dilithium.DilithiumPublicKeyParameters;
import org.bouncycastle.pqc.crypto.crystals.dilithium.DilithiumSigner;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import controlador.NotarioDigital;
import modelo.FirmaDigital;


class DilithiumTest {
	private final static String dir = System.getProperty("user.dir");
	
	FirmaDigital firmaDigital2 = new FirmaDigital(2);
	FirmaDigital firmaDigital3 = new FirmaDigital(3);
	FirmaDigital firmaDigital5 = new FirmaDigital(5);
	NotarioDigital n = new NotarioDigital();
	
	
	byte[] mensaje = "¡Test de Dilithium!".getBytes();
	DilithiumSigner signer  = new DilithiumSigner();
	DilithiumSigner verifier  = new DilithiumSigner();
	X509Certificate certificado;
	
	@BeforeEach
	void setup() {
		Security.addProvider(new BouncyCastlePQCProvider());
		assertNotNull(Security.getProvider("BCPQC"));
		
	}
	/**
	 * TESTS DE FIRMA Y VERIFICACION SOBRE EL DOCUMENTO
	 */
	@Test
	void testDilithiumMode() {
		assertEquals(2,firmaDigital2.getDilithiumMode());
		assertEquals(3,firmaDigital3.getDilithiumMode());
		assertEquals(5,firmaDigital5.getDilithiumMode());
	}
	@Test
	void testGetCertificado() throws IOException {
		FirmaDigital verificar = new FirmaDigital();
		assertNull(firmaDigital2.getCertificado());
		assertNull(firmaDigital3.getCertificado());
		assertNull(firmaDigital5.getCertificado());
		
		firmaDigital2.codigoFirma(2);
		firmaDigital3.codigoFirma(3);
		firmaDigital5.codigoFirma(5);
		assertThrows(Exception.class, () -> {
            verificar.codigoFirma(17);
        });
		assertNotNull(firmaDigital2.getFirma());
		assertNotNull(firmaDigital2.getCertificado());
		
		assertNotNull(firmaDigital3.getFirma());
		assertNotNull(firmaDigital3.getCertificado());
		
		assertNotNull(firmaDigital5.getFirma());
		assertNotNull(firmaDigital5.getCertificado());
		
		assertEquals(firmaDigital2.getClavePublica(), firmaDigital2.getCertificado().getPublicKey());
		assertEquals(firmaDigital3.getClavePublica(), firmaDigital3.getCertificado().getPublicKey());
		assertEquals(firmaDigital5.getClavePublica(), firmaDigital5.getCertificado().getPublicKey());
	}
	@Test
	void testVerificarDoc() throws IOException {
		FirmaDigital verificar = new FirmaDigital();
		PDDocument docNoFirmado = PDDocument.load(new File(dir + "\\recursos\\pdf_test.pdf"));
		PDDocument docFirmado = PDDocument.load(new File(dir + "\\recursos\\pdf_test_firmado.pdf"));
		n.abrirArchivoArrastrado(new File(dir + "\\recursos\\pdf_test_firmado.pdf"));
		
		assertTrue(verificar.verificarFirmaDocumento(n.getDoc(), n.buscarFirmaDocumento(n.getDoc())));
		assertFalse(verificar.verificarFirmaDocumento(docFirmado, new PDSignature()));
		assertNull(NotarioDigital.buscarFirmaDocumento(docNoFirmado));
		
	}
	
	/**
	 * TEST SOBRE LA FUNCIONALIDAD DE DILITHIUM (EXPERIMENTALES)
	 */
	@Test
	void testFirmaDilithium() {
		AsymmetricCipherKeyPair parClaves2 = firmaDigital2.generarParClaves(2);
		AsymmetricCipherKeyPair parClaves3 = firmaDigital2.generarParClaves(3);
		AsymmetricCipherKeyPair parClaves5 = firmaDigital2.generarParClaves(5);
		
		firmaDigital2.firmar((DilithiumPrivateKeyParameters) parClaves2.getPrivate(), "¡Test 2!".getBytes());
		firmaDigital3.firmar((DilithiumPrivateKeyParameters) parClaves3.getPrivate(), "¡Test 3!".getBytes());
		firmaDigital5.firmar((DilithiumPrivateKeyParameters) parClaves5.getPrivate(), "¡Test 5!".getBytes());
		
		assertTrue(firmaDigital2.verificar((DilithiumPublicKeyParameters) parClaves2.getPublic(), "¡Test 2!".getBytes(), firmaDigital2.getFirma()));
		assertTrue(firmaDigital3.verificar((DilithiumPublicKeyParameters) parClaves3.getPublic(), "¡Test 3!".getBytes(), firmaDigital3.getFirma()));
		assertTrue(firmaDigital5.verificar((DilithiumPublicKeyParameters) parClaves5.getPublic(), "¡Test 5!".getBytes(), firmaDigital5.getFirma()));
	}
}
