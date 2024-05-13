package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Before;
import org.junit.jupiter.api.Test;

import modelo.FirmaDigital;

class DilithiumTest {
	FirmaDigital firmaDigital2, firmaDigital3, firmaDigital5;
	byte[] mensaje = "¡Test de Dilithium!".getBytes();
	
	@Before
	void setUp() {
		firmaDigital2 = new FirmaDigital(2);
		firmaDigital3 = new FirmaDigital(3);
		firmaDigital5 = new FirmaDigital(5);
	}
	@Test
	void test() {
		fail("Not yet implemented");
	}

}
