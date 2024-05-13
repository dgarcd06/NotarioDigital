package modelo;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.examples.signature.SigUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSigProperties;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSignDesigner;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.pqc.crypto.crystals.dilithium.*;
import org.bouncycastle.pqc.jcajce.spec.DilithiumParameterSpec;

import vista.ImagenFirma;
import vista.VisorPDF;

import org.bouncycastle.cert.jcajce.JcaCertStore;

/*
 * La clase Controlador será la encargada de conectar los métodos de Dilithium con la aplicación Notario Digital
 */
@SuppressWarnings("static-access")
public class FirmaDigital {

	private  AsymmetricCipherKeyPair parClaves;
	private static int dilithiumMode;
	private static byte[] mensaje;
	private static byte[] firma;
	private static SignatureOptions signatureOptions;
	private static PDVisibleSignDesigner visibleSignDesigner;
	private final static PDVisibleSigProperties visibleSignatureProperties = new PDVisibleSigProperties();
	private static PDDocument doc;
	private static PrivateKey sk;
	private static PublicKey pk;
	private static X509Certificate certificado;
	private final static String dir = System.getProperty("user.dir");
	private static String archivo_output;
	private static VisorPDF visorPDF;
	private static PDSignature datosFirma;
	/**
	 * Para crear el objeto, se le pasa el archivo de PDF ¡¡¡¡REVISAR!!!! y el nivel
	 * de seguridad de Dilithium para iniciar los parámetros
	 * 
	 * @param archivo_pdf
	 * @param dilithiumMode
	 */
	
	public FirmaDigital(PDDocument documento, int dilithiumMode, String rutaPDF, VisorPDF visor) {
		
		visorPDF = visor;
		archivo_output = rutaPDF.substring(0,rutaPDF.lastIndexOf("."));
		archivo_output = archivo_output + "_firmado.pdf";
		doc = documento;
		this.dilithiumMode = dilithiumMode;
		new ImagenFirma("David García Diez",dilithiumMode,370,150);
		if (dilithiumMode == 3) {
			new DilithiumKeyGenerationParameters(new SecureRandom(),
					DilithiumParameters.dilithium3);
		} else if (dilithiumMode == 5) {
			new DilithiumKeyGenerationParameters(new SecureRandom(),
					DilithiumParameters.dilithium5);
		} else {
			dilithiumMode = 2;
			new DilithiumKeyGenerationParameters(new SecureRandom(),
					DilithiumParameters.dilithium2);
		}
		parClaves = generarParClaves();
		mensaje = "¡Dilithium!".getBytes();
		try {
			firmar((DilithiumPrivateKeyParameters) parClaves.getPrivate(), mensaje);
		} catch (OperatorCreationException | CertificateException | IOException e) {
			e.printStackTrace();
		}
	}

	public String getOS() {
		return System.getProperty("os.name");
	}

	public PDDocument getPDDocument() {
		return doc;
	}

	public byte[] getFirma() {
		return firma;
	}
	public AsymmetricKeyParameter getClavePublica() {
		return this.parClaves.getPublic();
	}

	public X509Certificate getCertificado() {
		return certificado;
	}

	public PrivateKey getPrivateKey() {
		return sk;
	}

	public SignatureOptions getSignatureOptions() {
		return signatureOptions;
	}

	/**
	 * Ejecuta la firma de Dilithium. Después almacena los datos de la firma para
	 * después utilizarlos en la escritura del PDF.
	 * 
	 * @return 0 si todo está correcto. 1 si hay algún error
	 * @throws IOException
	 * @throws CertificateException
	 * @throws OperatorCreationException
	 * @throws CertificateEncodingException
	 */
	public void firmar(DilithiumPrivateKeyParameters privateKey, byte[] message)
			throws CertificateEncodingException, OperatorCreationException, CertificateException, IOException {
		DilithiumSigner signer = new DilithiumSigner();
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
		firmarDocumento();
	}

	public boolean verificar() {
		if(datosFirma != null){
			DilithiumSigner verifier = new DilithiumSigner();
			DilithiumPublicKeyParameters publicKey = (DilithiumPublicKeyParameters) parClaves.getPublic();
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
		}else {
			return false;
		}
	}

	public static AsymmetricCipherKeyPair generarParClaves() {
		DilithiumKeyPairGenerator generator = new DilithiumKeyPairGenerator();
		if(dilithiumMode == 5) {
			generator.init(new DilithiumKeyGenerationParameters(new SecureRandom(), DilithiumParameters.dilithium5));
		}else if(dilithiumMode == 3) {
			generator.init(new DilithiumKeyGenerationParameters(new SecureRandom(), DilithiumParameters.dilithium3));
		}else {
			generator.init(new DilithiumKeyGenerationParameters(new SecureRandom(), DilithiumParameters.dilithium2));
		}
		

		AsymmetricCipherKeyPair keyPair = generator.generateKeyPair();

		return keyPair;
	}

	public static void firmarDocumento() throws CertificateEncodingException, OperatorCreationException, CertificateException, IOException {
		try(FileOutputStream archivoOutput = new FileOutputStream(archivo_output)){
			byte[] codigoFirma = codigoFirma();
			// Creamos el elemento visual de firma
			visibleSignDesigner = new PDVisibleSignDesigner(doc, new FileInputStream(dir + "\\recursos\\firma.png"), visorPDF.getController().getCurrentPageNumber() + 1);
			visibleSignDesigner.xAxis(100).yAxis(100).zoom(0).adjustForRotation();
			// Propiedades de la firma
			visibleSignatureProperties.signerName("David García Diez").signerLocation("Universidad de León")
					.signatureReason("Firma con Dilithium").preferredSize(50).page(visorPDF.getController().getCurrentPageNumber()).visualSignEnabled(true)
					.setPdVisibleSignature(visibleSignDesigner);

			int accessPermissions = SigUtils.getMDPPermission(doc);
			if (accessPermissions == 1) {
				throw new IllegalStateException(
						"No changes to the document are permitted due to DocMDP transform parameters dictionary");
			}
			PDSignature signature = new PDSignature();
			if (doc.getVersion() >= 1.5f && accessPermissions == 0) {
				SigUtils.setMDPPermission(doc, signature, 2);
			}
			PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm(null);
			if (acroForm != null && acroForm.getNeedAppearances()) {
				if (acroForm.getFields().isEmpty()) {
					acroForm.getCOSObject().removeItem(COSName.NEED_APPEARANCES);
				} else {
					System.out.println("/NeedAppearances is set, signature may be ignored by Adobe Reader");
				}
			}

			signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
			signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
			visibleSignatureProperties.buildSignature();
			signature.setName("David García Diez");
			signature.setLocation("Universidad de León");
			signature.setReason("Análisis de Algoritmos Post-Cuánticos");
			signature.setSignDate(Calendar.getInstance());
			SignatureInterface signatureInterface = new SignatureInterface() {

				public byte[] sign(InputStream arg0) throws IOException {
					return firma;
				}
				
			};

			// register signature dictionary and sign interface
			if (visibleSignatureProperties.isVisualSignEnabled()) {
				signatureOptions = new SignatureOptions();
				signatureOptions.setVisualSignature(visibleSignatureProperties.getVisibleSignature());
				signatureOptions.setPage(visibleSignatureProperties.getPage() - 1);
				signatureOptions.setPreferredSignatureSize(13000);
				doc.addSignature(signature, signatureInterface, signatureOptions);
			} else {
				signatureOptions = new SignatureOptions();
				signatureOptions.setPreferredSignatureSize(13000);
				doc.addSignature(signature, signatureInterface);
			}
			ExternalSigningSupport externalSigning = doc.saveIncrementalForExternalSigning(archivoOutput);
			externalSigning.setSignature(codigoFirma);

		}
	}

	private static X509Certificate generarCertificado() throws OperatorCreationException, CertificateException,
			IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {

		// Generar par de claves para Dilithium
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
		SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(pk.getEncoded());
		// Crear el emisor y el sujeto del certificado
		X500Name issuerName = new X500Name("CN=David García Diez");
		X500Name subjectName = new X500Name("CN=Dgarcd06");

		// Crear el generador de certificados
		X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(issuerName,
				BigInteger.valueOf(System.currentTimeMillis()), new Date(System.currentTimeMillis()),
				new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000), // 1 año de validez
				subjectName, publicKeyInfo);

		// Construir el firmante del certificado
		ContentSigner signer;
		if(dilithiumMode == 5) {
			signer = new JcaContentSignerBuilder("DILITHIUM5").setProvider("BC").build(sk);
		}else if(dilithiumMode == 3) {
			signer = new JcaContentSignerBuilder("DILITHIUM3").setProvider("BC").build(sk);
		}else {
			signer = new JcaContentSignerBuilder("DILITHIUM2").setProvider("BC").build(sk);
		}
		

		// Construir el certificado
		X509CertificateHolder certHolder = certBuilder.build(signer);

		// Convertir el certificado a la clase X509Certificate de Java
		X509Certificate cert = new JcaX509CertificateConverter().getCertificate(certHolder);

		// Imprimir el certificado (opcional)
		System.out.println(cert.toString());
		return cert;
	}

	public static byte[] codigoFirma() throws IOException {
		try {
			CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
			X509Certificate cert = generarCertificado();
			ContentSigner dilithiumSigner;
			if (dilithiumMode == 5) {
				dilithiumSigner = new JcaContentSignerBuilder("Dilithium5").build(sk);
			} else if (dilithiumMode == 3) {
				dilithiumSigner = new JcaContentSignerBuilder("Dilithium3").build(sk);
			} else {
				dilithiumSigner = new JcaContentSignerBuilder("Dilithium2").build(sk);
			}

			gen.addSignerInfoGenerator(
					new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().build())
							.build(dilithiumSigner, cert));
			gen.addCertificates(new JcaCertStore(Arrays.asList(cert)));
			CMSTypedData msg = new CMSProcessableByteArray(mensaje);
			CMSSignedData signedData = gen.generate(msg, false);
			
			return signedData.getEncoded();
		} catch (GeneralSecurityException | CMSException | OperatorCreationException e) {
			throw new IOException(e);
		}
	}

}
