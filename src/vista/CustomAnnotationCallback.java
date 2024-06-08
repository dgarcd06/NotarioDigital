package vista;

import java.io.File;
import java.security.cert.X509Certificate;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.icepdf.core.pobjects.*;
import org.icepdf.core.pobjects.actions.*;
import org.icepdf.core.pobjects.annotations.Annotation;
import org.icepdf.core.pobjects.annotations.LinkAnnotation;
import org.icepdf.core.pobjects.annotations.SignatureWidgetAnnotation;
import org.icepdf.ri.common.MyAnnotationCallback;
import org.icepdf.ri.common.views.DocumentViewController;
import org.icepdf.ri.util.BareBonesBrowserLaunch;

public class CustomAnnotationCallback extends MyAnnotationCallback {
	private Boolean firmaVerificada;
	private byte[] firma, clavePublica;
	X509Certificate certificado;
    private static final Logger logger = Logger.getLogger(CustomAnnotationCallback.class.toString());

    public CustomAnnotationCallback(DocumentViewController documentViewController,Boolean firmaVerificada, byte[] firma, byte[] clavePublica,
			X509Certificate certificado) {
    	super(documentViewController);
    	this.firmaVerificada = firmaVerificada;
    	this.firma = firma;
    	this.clavePublica = clavePublica;
    	this.certificado = certificado;
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
    	new FrameVerificacion(firmaVerificada, firma,
				clavePublica, certificado);
    }
}
