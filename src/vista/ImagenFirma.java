package vista;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;

public class ImagenFirma {

	private final String dir = System.getProperty("user.dir");
	private File output;

	public ImagenFirma(String nombre, int nivelSeguridad, int anchura, int altura) {
		BufferedImage imagen = new BufferedImage(anchura, altura, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D graphics = imagen.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setBackground(new Color(242,242,242));
		graphics.fillRect(0, 0, anchura, altura);
		Font fuente_texto;
		if(altura > 150) {
			fuente_texto = new Font("Arial", Font.BOLD, altura/10);
		}else {
			fuente_texto = new Font("Arial", Font.BOLD, altura/6);
		}
		graphics.setFont(fuente_texto);
		graphics.setColor(Color.BLACK);
		graphics.drawString("FIRMA DIGITAL CON DILITHIUM", 0, altura/5);
		graphics.drawString("Autor:" + nombre, 0, (altura/5)*2);
		if (nivelSeguridad == 5) {
			graphics.drawString("Algoritmo:" + "Dilitium5", 0, (altura/5)*3);
		} else if (nivelSeguridad == 3) {
			graphics.drawString("Algoritmo:" + "Dilitium3", 0, (altura/5)*3);
		} else {
			graphics.drawString("Algoritmo:" + "Dilitium2", 0, (altura/5)*3);
		}
		graphics.drawString("Ubicación:León", 0, (altura/5)*4);
		graphics.drawString("Fecha:" + new Date(), 0, (altura/5)*5);
		 BufferedImage pngImage = null;
	        try {
	            pngImage = ImageIO.read(new File(dir + "\\recursos\\Dilithium_sign.png")); 
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        if (pngImage != null) {
	            int pngWidth = pngImage.getWidth();
	            int pngHeight = pngImage.getHeight();
	            int pngX = (anchura - pngWidth);

	            graphics.drawImage(pngImage, pngX, 0, null);
	        }
		output = new File(dir + "\\recursos\\firma.png");
		try {
			ImageIO.write(imagen, "png", output);
		} catch (IOException e) {
			e.printStackTrace();
		}

		graphics.dispose();
	}
}
