package controlador;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.SecureRandom;

import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.pqc.crypto.crystals.dilithium.*;



/*
 * La clase Controlador será la encargada de conectar los métodos de Dilithium con la aplicación Notario Digital
 */
public class Controlador {

	private DilithiumKeyPairGenerator keypair;
	private DilithiumKeyGenerationParameters generadorClaves;
	private DilithiumPrivateKeyParameters clavePrivada;
	private DilithiumPublicKeyParameters clavePublica;
	private AsymmetricCipherKeyPair parClaves;
	private DilithiumKeyParameters params;
	private File archivo_pdf, archivo_firma;
	private int dilithiumMode;
	private byte[] mensaje;
	private PDSignature signature;

	/**
	 * Para crear el objeto, se le pasa el archivo de PDF ¡¡¡¡REVISAR!!!! y el nivel de seguridad de Dilithium para iniciar los parámetros
	 * @param archivo_pdf
	 * @param dilithiumMode
	 */
	public Controlador(File archivo_pdf, int dilithiumMode) {
		this.archivo_pdf = archivo_pdf;
		this.dilithiumMode = dilithiumMode;
		if(dilithiumMode == 3) {
			this.generadorClaves = new DilithiumKeyGenerationParameters(new SecureRandom(), DilithiumParameters.dilithium3);
		}else if(dilithiumMode == 5) {
			this.generadorClaves = new DilithiumKeyGenerationParameters(new SecureRandom(), DilithiumParameters.dilithium5);
		}else {
			this.dilithiumMode = 2;
			this.generadorClaves = new DilithiumKeyGenerationParameters(new SecureRandom(), DilithiumParameters.dilithium2);
		}
		this.keypair = new DilithiumKeyPairGenerator();
	}


	public String getOS() {
		return System.getProperty("os.name");
	}

	public File getArchivoPDF() {
		return this.archivo_pdf;
	}
	public void setMensaje(String datos) {
		
	}

	public void generarClaves() {

		if(this.dilithiumMode == 3) {
			keypair.init(new DilithiumKeyGenerationParameters(
			        new SecureRandom(),
			        DilithiumParameters.dilithium3
			        ));
		}else if(this.dilithiumMode == 5) {
			keypair.init(new DilithiumKeyGenerationParameters(
			        new SecureRandom(),
			        DilithiumParameters.dilithium5
			        ));
		}else {
			keypair.init(new DilithiumKeyGenerationParameters(
			        new SecureRandom(),
			        DilithiumParameters.dilithium2
			        ));
		}
		
		parClaves = keypair.generateKeyPair();
		this.clavePrivada = (DilithiumPrivateKeyParameters) parClaves.getPrivate();
		this.clavePublica = (DilithiumPublicKeyParameters) parClaves.getPublic();
		}
	/**
	 * Ejecuta la firma de Dilithium. Después almacena los datos de la firma para
	 * después utilizarlos en la escritura del PDF.
	 * 
	 * @return 0 si todo está correcto. 1 si hay algún error
	 */
	public int firmar() {
		DilithiumSigner signer = new DilithiumSigner();
		if(this.dilithiumMode == 3) {
			signer.init(true,
					new DilithiumPrivateKeyParameters(DilithiumParameters.dilithium3,
			                this.clavePrivada.getRho(),
			                this.clavePrivada.K,
			                this.clavePrivada.Tr,
			                this.clavePrivada.S1,
			                this.clavePrivada.S2,
			                this.clavePrivada.T0,
			                this.clavePrivada.T1)
			        );
		}else if(this.dilithiumMode == 5) {
			signer.init(true,
					new DilithiumPrivateKeyParameters(DilithiumParameters.dilithium3,
			                this.clavePrivada.getRho(),
			                this.clavePrivada.K,
			                this.clavePrivada.Tr,
			                this.clavePrivada.S1,
			                this.clavePrivada.S2,
			                this.clavePrivada.T0,
			                this.clavePrivada.T1)
			        );
		}else {
			signer.init(true,
					new DilithiumPrivateKeyParameters(DilithiumParameters.dilithium3,
			                this.clavePrivada.getRho(),
			                this.clavePrivada.K,
			                this.clavePrivada.Tr,
			                this.clavePrivada.S1,
			                this.clavePrivada.S2,
			                this.clavePrivada.T0,
			                this.clavePrivada.T1)
			        );
		}
		
		return 0;
	}

	public int verificar() {
		DilithiumSigner verifier = new DilithiumSigner();
		if(this.dilithiumMode == 3) {
			verifier.init(true,
					new DilithiumPrivateKeyParameters(DilithiumParameters.dilithium3,
			                this.clavePublica.getRho(),
			                this.clavePublica.K,
			                this.clavePublica.Tr,
			                this.clavePublica.S1,
			                this.clavePublica.S2,
			                this.clavePublica.T0,
			                this.clavePublica.T1)
			        );
		}else if(this.dilithiumMode == 5) {
			verifier.init(true,
					new DilithiumPrivateKeyParameters(DilithiumParameters.dilithium3,
			                this.clavePublica.getRho(),
			                this.clavePublica.K,
			                this.clavePublica.Tr,
			                this.clavePublica.S1,
			                this.clavePublica.S2,
			                this.clavePublica.T0,
			                this.clavePublica.T1)
			        );
		}else {
			verifier.init(true,
					new DilithiumPrivateKeyParameters(DilithiumParameters.dilithium2,
							this.clavePublica.getRho(),
			                this.clavePublica.K,
			                this.clavePublica.Tr,
			                this.clavePublica.S1,
			                this.clavePublica.S2,
			                this.clavePublica.T0,
			                this.clavePublica.T1))
			        );
		}

	}
}
