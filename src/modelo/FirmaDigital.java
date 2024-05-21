package modelo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.DefaultSignedAttributeTableGenerator;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.cms.jcajce.JcaSignerInfoVerifierBuilder;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.pqc.crypto.crystals.dilithium.*;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.bouncycastle.pqc.jcajce.spec.DilithiumParameterSpec;
import org.bouncycastle.util.Store;

import org.bouncycastle.cert.jcajce.JcaCertStore;

@SuppressWarnings("static-access")
/**
 * FirmaDigital es la clase que incluye la lógica de la firma de Dilithium. Para
 * ello hace uso de la librería criptográfica Bouncy Castle.
 * 
 * Al instanciar la clase, se recibe como parámetro el nivel de seguridad
 * deseado para Dilithium.
 * 
 * El propio constructor genera un par de claves y la firma digital a partir de
 * un mensaje, aunque estos métodos sirven para testear el funcionamiento del
 * algoritmo.
 * 
 * El método encargado de generar la Firma Digital basada en el estándar CMS es
 * codigoFirma()
 */
public class FirmaDigital {

	static {
		Security.addProvider(new BouncyCastleProvider());
	}
	private AsymmetricCipherKeyPair parClaves; // keypair compatible con Dilithium, no con Java
	private int dilithiumMode;
	private byte[] mensaje;
	private byte[] firma;
	private static PrivateKey sk;
	private static PublicKey pk;
	private static X509Certificate certificado;
	private String algoritmoFirma; // Detecta el algoritmo de firma

	/**
	 * Para crear un nuevo objeto, se le pasa el nivel de seguridad de Dilithium
	 * para iniciar los parámetros
	 * 
	 * @param dilithiumMode El nivel de seguridad con el que se inicializará el
	 *                      algoritmo Sus posibles valores son 2, 3 y 5, siendo el 2
	 *                      el valor por defecto
	 */
	public FirmaDigital(int dilithiumMode) {

		this.dilithiumMode = dilithiumMode;

		if (dilithiumMode == 3) {
			new DilithiumKeyGenerationParameters(new SecureRandom(), DilithiumParameters.dilithium3);
		} else if (dilithiumMode == 5) {
			new DilithiumKeyGenerationParameters(new SecureRandom(), DilithiumParameters.dilithium5);
		} else {
			new DilithiumKeyGenerationParameters(new SecureRandom(), DilithiumParameters.dilithium2);
		}
		mensaje = "¡Dilithium!".getBytes();
	}

	/**
	 * Constructor vacío para la verificacion
	 */
	public FirmaDigital() {

	}

	/**
	 * Devuelve un array de bytes correspondientes a la firma digital IMPORTANTE: El
	 * array de firma que devuelve es producto de la ejecución del método firmar(),
	 * es decir, se corresponde con la firma creada para la comprobación del
	 * funcionamiento del algoritmo
	 */
	public byte[] getFirma() {
		return firma;
	}

	public PublicKey getClavePublica() {
		return this.pk;
	}

	public X509Certificate getCertificado() {
		return certificado;
	}

	public int getDilithiumMode() {
		return dilithiumMode;
	}

	public String getAlgoritmoFirma() {
		return this.algoritmoFirma;
	}

	public void setAlgoritmoFirma(String algoritmo) {
		this.algoritmoFirma = algoritmo;
	}

	/**
	 * Metodo que genera un certificado con formato X509. Para ello genera un par de
	 * claves de Dilithium compatibles con las librerías de seguridad de Java
	 * 
	 * @param dilithiumMode El nivel de seguridad indicado al instanciar la clase
	 * @return cert el certificado creado
	 * @throws OperatorCreationException          Error al inicializar la variable
	 *                                            signer (en el metodo build, por la
	 *                                            clave privada)
	 * @throws CertificateException               Error en la creación de la
	 *                                            variable cert
	 * @throws NoSuchAlgorithmException           Si no encuentra Dilithium; debe
	 *                                            añadirse Bouncy Castle como
	 *                                            provider de Java.Security
	 * @throws NoSuchProviderException            Si no encuentra Bouncy Castle
	 *                                            Provider
	 * @throws InvalidAlgorithmParameterException Error al inicializar el
	 *                                            keyPairGenerator (parametro
	 *                                            incorrecto)
	 * @throws CertIOException
	 */
	private static X509Certificate generarCertificado(int dilithiumMode)
			throws OperatorCreationException, CertificateException, NoSuchAlgorithmException, NoSuchProviderException,
			InvalidAlgorithmParameterException, CertIOException {

		KeyPairGenerator keyPairGenerator;
		if (dilithiumMode == 5) {
			keyPairGenerator = KeyPairGenerator.getInstance("DILITHIUM5", "BC");
			keyPairGenerator.initialize(DilithiumParameterSpec.dilithium5);
		} else if (dilithiumMode == 3) {
			keyPairGenerator = KeyPairGenerator.getInstance("DILITHIUM3", "BC");
			keyPairGenerator.initialize(DilithiumParameterSpec.dilithium3);
		} else {
			keyPairGenerator = KeyPairGenerator.getInstance("DILITHIUM2", "BC");
			keyPairGenerator.initialize(DilithiumParameterSpec.dilithium2);
		}
		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		pk = keyPair.getPublic();
		sk = keyPair.getPrivate();
		SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());

		X500Name issuerName = new X500Name("CN=David García Diez");
		X500Name subjectName = new X500Name("CN=Dgarcd06");

		Date notBefore = new Date(System.currentTimeMillis());
		Date notAfter = new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000); // 1 año de validez

		X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(issuerName,
				BigInteger.valueOf(System.currentTimeMillis()), notBefore, notAfter, subjectName, publicKeyInfo);

// Key Usage extension
		certBuilder.addExtension(Extension.keyUsage, true,
				new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));

// Extended Key Usage extension
		certBuilder.addExtension(Extension.extendedKeyUsage, false,
				new ExtendedKeyUsage(KeyPurposeId.id_kp_emailProtection));

		ContentSigner signer;
		if (dilithiumMode == 5) {
			signer = new JcaContentSignerBuilder("DILITHIUM5").setProvider("BC").build(sk);
		} else if (dilithiumMode == 3) {
			signer = new JcaContentSignerBuilder("DILITHIUM3").setProvider("BC").build(sk);
		} else {
			signer = new JcaContentSignerBuilder("DILITHIUM2").setProvider("BC").build(sk);
		}

		X509CertificateHolder certHolder = certBuilder.build(signer);
		certificado = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certHolder);
		return certificado;
	}

	/**
	 * Genera la Firma Digital siguiendo el estándar CMS. CMS: En primer lugar se
	 * genera un hash del mensaje con el digest algorithm. después, ese hash se
	 * firma con el ContentSigner, que en este caso será Dilithium. Además, genera
	 * un certificado con el método generarCertificado, que irá incluido. El digest
	 * algorithm es SHAKE-256 y el algoritmo de Firma Digital es Dilithium. Genera
	 * un objeto CMSSignedData, que es la firma.
	 * 
	 * @param dilithiumMode El nivel de seguridad elegido al instanciar la clase
	 * @return signedData.getEncoded() el array de bytes con la firma digital
	 * @throws IOException Si la generación del objeto CMSSignedData no es correcta
	 *                     esta excepción podría salir al intentar hacer
	 *                     getEncoded() en el return.
	 */
	public byte[] codigoFirma(int dilithiumMode) throws IOException {
		try {
			CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
			X509Certificate cert = generarCertificado(dilithiumMode);
			ContentSigner dilithiumSigner;

			String algorithm;
			if (dilithiumMode == 5) {
				algorithm = "Dilithium5";
			} else if (dilithiumMode == 3) {
				algorithm = "Dilithium3";
			} else {
				algorithm = "Dilithium2";
			}

			dilithiumSigner = new JcaContentSignerBuilder(algorithm).setProvider("BC").build(sk);

			// Generar un MessageDigest del contenido a firmar
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] messageDigest = md.digest(mensaje);

			// Añadir atributos de firma
			ASN1EncodableVector signedAttributesVector = new ASN1EncodableVector();
			signedAttributesVector
					.add(new Attribute(CMSAttributes.contentType, new DERSet(PKCSObjectIdentifiers.data)));
			signedAttributesVector
					.add(new Attribute(CMSAttributes.messageDigest, new DERSet(new DEROctetString(messageDigest))));
			AttributeTable signedAttr = new AttributeTable(signedAttributesVector);

			JcaSignerInfoGeneratorBuilder sigGenBuilder = new JcaSignerInfoGeneratorBuilder(
					new JcaDigestCalculatorProviderBuilder().setProvider("BC").build());
			sigGenBuilder.setSignedAttributeGenerator(new DefaultSignedAttributeTableGenerator(signedAttr));
			gen.addSignerInfoGenerator(sigGenBuilder.build(dilithiumSigner, cert));

			gen.addCertificates(new JcaCertStore(Arrays.asList(cert)));
			CMSTypedData msg = new CMSProcessableByteArray(mensaje);
			CMSSignedData signedData = gen.generate(msg, true);
			firma = signedData.getEncoded();
			return signedData.getEncoded();
		} catch (GeneralSecurityException | CMSException | OperatorCreationException e) {
			throw new IOException(e);
		}
	}

	public boolean verificarFirmaDocumento(PDDocument doc, PDSignature firmaDocumento) {
		try {
			byte[] contenidoFirma = firmaDocumento.getContents();
			byte[] datosFirma = firmaDocumento.getSignedContent(new ByteArrayInputStream(contenidoFirma));

			CMSSignedData datosCMS = new CMSSignedData(new ByteArrayInputStream(contenidoFirma));
			Store<?> certStore = datosCMS.getCertificates();
			SignerInformationStore signerInfoStore = datosCMS.getSignerInfos();
			Collection<SignerInformation> signers = signerInfoStore.getSigners();

			for (SignerInformation signer : signers) {
				Collection<?> matches = certStore.getMatches(signer.getSID());
				X509CertificateHolder certHolder = (X509CertificateHolder) matches.iterator().next();
				CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
				certificado = new JcaX509CertificateConverter().getCertificate(certHolder);

				pk = certificado.getPublicKey();
				firma = signer.getSignature();
				SignerInformationVerifier verifier = new JcaSimpleSignerInfoVerifierBuilder()
						.build(certificado.getPublicKey());

				if (signer.verify(verifier)) {
					return true;
				} else {
					return false;
				}
			}
			return false;
		} catch (IOException | CMSException | CertificateException | OperatorCreationException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Método utilizado para evaluar el correcto funcionamiento de Dilithium. Este
	 * método recibe una clave privada y un mensaje y genera una firma. Inicializa
	 * un objeto DilithiumSigner con los parámetros indicados. El resultado se
	 * almacena en un array de bytes llamado firma.
	 * 
	 * @param privateKey objeto de Bouncy Castle (no compatible actualmente con
	 *                   Java) que corresponde con la clave privada
	 * @param message    array de bytes con el mensaje a firmar
	 */
	public void firmar(DilithiumPrivateKeyParameters privateKey, byte[] message) {
		DilithiumSigner signer = new DilithiumSigner(); // Primer parametro de init en true para firmar y en false para
														// verificar
		if (dilithiumMode == 3) {
			signer.init(true,
					new DilithiumPrivateKeyParameters(DilithiumParameters.dilithium3, privateKey.getRho(),
							privateKey.getK(), privateKey.getTr(), privateKey.getS1(), privateKey.getS2(),
							privateKey.getT0(), privateKey.getT1()));
		} else if (dilithiumMode == 5) {
			signer.init(true,
					new DilithiumPrivateKeyParameters(DilithiumParameters.dilithium5, privateKey.getRho(),
							privateKey.getK(), privateKey.getTr(), privateKey.getS1(), privateKey.getS2(),
							privateKey.getT0(), privateKey.getT1()));
		} else {
			signer.init(true,
					new DilithiumPrivateKeyParameters(DilithiumParameters.dilithium2, privateKey.getRho(),
							privateKey.getK(), privateKey.getTr(), privateKey.getS1(), privateKey.getS2(),
							privateKey.getT0(), privateKey.getT1()));
		}
		this.firma = signer.generateSignature(this.mensaje);
	}

	/**
	 * Metodo utilizado para evaluar el correcto funcionamiento de Dilithium. Genera
	 * el par de claves en un objeto de Bouncy Castle. Se pueden obtener la clave
	 * publica con getPublic() y la privada con getPrivate(). Las claves no son
	 * compatibles con las librerías nativas de seguridad de Java. Para generar
	 * claves compatibles, habría que usar java.security.KeyPairGenerator e
	 * instanciar el algoritmo Dilithium con el provider Bouncy Castle.
	 * 
	 * @param dilithiumMode El nivel de seguridad de Dilithium seleccionado al
	 *                      instanciar un objeto
	 * @return keyPair el par de claves de tipo
	 *         org.bouncycastle.crypto.AsymmetricCipherKeyPair
	 */
	public static AsymmetricCipherKeyPair generarParClaves(int dilithiumMode) {
		DilithiumKeyPairGenerator generator = new DilithiumKeyPairGenerator();
		if (dilithiumMode == 5) {
			generator.init(new DilithiumKeyGenerationParameters(new SecureRandom(), DilithiumParameters.dilithium5));
		} else if (dilithiumMode == 3) {
			generator.init(new DilithiumKeyGenerationParameters(new SecureRandom(), DilithiumParameters.dilithium3));
		} else {
			generator.init(new DilithiumKeyGenerationParameters(new SecureRandom(), DilithiumParameters.dilithium2));
		}

		AsymmetricCipherKeyPair keyPair = generator.generateKeyPair();

		return keyPair;
	}

	/**
	 * Metodo utilizado para evaluar el correcto funcionamiento de Dilithium.
	 * Verifica la autenticidad de la firma a partir de los parámetros.
	 * 
	 * @param publicKey la clave pública del firmante
	 * @param mensaje   el mensaje utilizado para firmar
	 * @param firma     la firma digital obtenida a partir del mensaje y la clave
	 *                  privada del firmante
	 * @return true si se verifica la firma con los parámetros, false si no se
	 *         verifica
	 */
	public boolean verificar(DilithiumPublicKeyParameters publicKey, byte[] mensaje, byte[] firma) {
		DilithiumSigner verifier = new DilithiumSigner(); // Primer parametro de init en true para firmar y en false
															// para verificar
		if (dilithiumMode == 3) {
			verifier.init(false,
					new DilithiumPublicKeyParameters(DilithiumParameters.dilithium3, publicKey.getEncoded()));
		} else if (dilithiumMode == 5) {
			verifier.init(false,
					new DilithiumPublicKeyParameters(DilithiumParameters.dilithium5, publicKey.getEncoded()));
		} else {
			verifier.init(false,
					new DilithiumPublicKeyParameters(DilithiumParameters.dilithium2, publicKey.getEncoded()));
		}
		return verifier.verifySignature(this.mensaje, this.firma);
	}
}