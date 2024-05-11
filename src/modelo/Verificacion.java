package modelo;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.pqc.crypto.crystals.dilithium.DilithiumParameters;
import org.bouncycastle.pqc.crypto.crystals.dilithium.DilithiumPublicKeyParameters;
import org.bouncycastle.pqc.crypto.crystals.dilithium.DilithiumSigner;

public class Verificacion {
	private int dilithiumMode;
	private DilithiumPublicKeyParameters pk;
	private final byte[] mensaje = "¡Dilithium!".getBytes();
	private byte[] firma;
	public Verificacion(PDDocument doc) {
		try {
			for(PDSignature signature:doc.getSignatureDictionaries()) {
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public boolean verificar() {
			DilithiumSigner verifier = new DilithiumSigner();
			if (this.dilithiumMode == 3) {
				verifier.init(false,
						new DilithiumPublicKeyParameters(DilithiumParameters.dilithium3, pk.getEncoded()));
			} else if (this.dilithiumMode == 5) {
				verifier.init(false,
						new DilithiumPublicKeyParameters(DilithiumParameters.dilithium5, pk.getEncoded()));
			} else {
				verifier.init(false,
						new DilithiumPublicKeyParameters(DilithiumParameters.dilithium2, pk.getEncoded()));
			}
			return verifier.verifySignature(mensaje, this.firma);
	}
}
