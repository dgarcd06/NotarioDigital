package vista;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;

public class ImagenFirma {

	private String nombre,firma,clave_publica;
	private final String dir = System.getProperty("user.dir");
	private File output;
	
	public ImagenFirma(String nombre, String firma, String clave_publica, int anchura, int altura) {
		 BufferedImage imagen = new BufferedImage(anchura, altura, BufferedImage.TYPE_INT_RGB);
	     Graphics2D graphics = imagen.createGraphics();
	     graphics.setColor(Color.WHITE);
	     graphics.fillRect(0, 0, anchura, altura);
	     Font fuente_texto = new Font("Arial", Font.BOLD, 14);
	     graphics.setFont(fuente_texto);
	     graphics.setColor(Color.BLACK);
	     graphics.drawString("Nombre: " + nombre, 50, 50);
	     graphics.drawString("Fecha: " + new Date(), 50, 70);
	     graphics.drawString("Algoritmo: " + "Dilitium", 50, 90);
	     graphics.drawString("Firma: " + firma, 50, 110);
	     graphics.drawString("Clave Publica: " + clave_publica, 50, 130);
	     output = new File(dir + "\\recursos\\firma.png");
	     try {
	            ImageIO.write(imagen, "png", output);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }

	        graphics.dispose();
	}
}
