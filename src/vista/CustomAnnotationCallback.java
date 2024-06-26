package vista;

import java.io.File;
import java.security.cert.X509Certificate;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;

import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.icepdf.core.pobjects.*;
import org.icepdf.core.pobjects.actions.*;
import org.icepdf.core.pobjects.annotations.Annotation;
import org.icepdf.core.pobjects.annotations.LinkAnnotation;
import org.icepdf.core.pobjects.annotations.SignatureWidgetAnnotation;
import org.icepdf.ri.common.MyAnnotationCallback;
import org.icepdf.ri.common.views.DocumentViewController;
import org.icepdf.ri.util.BareBonesBrowserLaunch;

public class CustomAnnotationCallback extends MyAnnotationCallback {
	private List<Boolean> firmaVerificada;
	private List<byte[]> firma, clavePublica;
	private List<X509Certificate> certificado;
	private List<PDSignature> signatures;
    private static final Logger logger = Logger.getLogger(CustomAnnotationCallback.class.toString());

    public CustomAnnotationCallback(DocumentViewController documentViewController,List<PDSignature> signatures, List<Boolean> verificados, List<byte[]> firmas, List<byte[]> clavesPublicas, List<X509Certificate> certificados) {
    	super(documentViewController);
    	this.signatures = signatures;
    	this.firmaVerificada = verificados;
    	this.firma = firmas;
    	this.clavePublica = clavesPublicas;
    	this.certificado = certificados;
    }

    @Override
    public void processAnnotationAction(Annotation annotation, Action action, int x, int y) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Annotation " + annotation.toString());
            if (annotation.getAction() != null) {
                logger.fine("   Action: " + annotation.getAction().toString());
            }
        }

        // Detectar si la anotación es una firma digital
        if (annotation instanceof SignatureWidgetAnnotation) {
            openCustomFrame();
        } else {
            // Llamar al método padre para manejar otras anotaciones
            super.processAnnotationAction(annotation, action, x, y);
        }
    }

    private void openCustomFrame() {
    	new FrameVerificacion(signatures,firmaVerificada, firma,clavePublica, certificado);
    }
}
