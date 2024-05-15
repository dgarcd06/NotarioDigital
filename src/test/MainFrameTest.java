package test;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import controlador.NotarioDigital;

class MainFrameTest {
	private final NotarioDigital n = new NotarioDigital();

	
	@Test
	void valoresIniciales() {
		assertEquals(0,n.getPDFCargado());
	}

}
