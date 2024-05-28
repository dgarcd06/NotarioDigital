package test;

import static org.junit.Assert.assertEquals;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

import controlador.Main;
import controlador.NotarioDigital;
import vista.FrameVerificacion;

class MainFrameTest {
	private final NotarioDigital n = new NotarioDigital();
	private final static String dir = System.getProperty("user.dir");
	
	@Test
	void valoresIniciales(){
		assertEquals(0,n.getPDFCargado());
		
		n.abrirArchivoArrastrado(new File("RutaInventada"));
		n.abrirArchivoArrastrado(new File(dir + "\\recursos\\pdf_test.pdf"));
		assertEquals(1,n.getPDFCargado());
		n.abrirArchivoArrastrado(new File(dir + "\\recursos\\pdf_test.pdf"));
	}
	@Test
	void testBuscarFirmaDoc() throws IOException {
		n.abrirArchivoArrastrado(new File(dir + "\\recursos\\pdf_test.pdf"));
		assertNull(n.buscarFirmaDocumento(n.getDoc()));
		n.firmaDocumento(3, 100, 100, 200, 150);
		assertNotNull(n.buscarFirmaDocumento(n.getDoc()));
	}
	@Test
	void testGuardado() throws IOException {
		assertEquals(1,n.guardar(0));
		n.abrirArchivoArrastrado(new File(dir + "\\recursos\\pdf_test.pdf"));
		assertEquals(1,n.guardar(0));
		assertEquals(0,n.guardar(1));
		n.setPDFCargado(0);
		assertEquals(1,n.guardar(n.getPDFCargado()));
	}
	@Test
	void testMain() {
		Main.main(null);
	}
	@Test
	void testFrameVerificacion() {
		FrameVerificacion ver = new FrameVerificacion(false, null, null, null);
		ver.setVisible(false);
	}
}
