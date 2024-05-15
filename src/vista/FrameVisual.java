package vista;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

@SuppressWarnings("serial")
public class FrameVisual extends JFrame {

    private int startX, startY, ancho, alto;
    private boolean dragging, firmaDeseada;

    public FrameVisual(int width, int height, int posX, int posY) {
        this.setSize(width, height);
        this.setUndecorated(true);    //Necesario para la transparencia
        this.setBackground(new Color(224, 206, 67, 30));
        this.setLocation(posX, posY);
        this.setVisible(true);
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                startX = e.getX();
                startY = e.getY();
                dragging = true;
            }

            public void mouseReleased(MouseEvent e) {
                dragging = false;
                ancho = e.getX() - startX;
                alto = e.getY() - startY;
                repaint();
                validarSeleccion();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (dragging) {
                	ancho = e.getX() - startX;
                	alto = e.getY() - startY;
                    repaint();
                }
            }
        });
        JOptionPane.showMessageDialog(this, "Selecciona el área donde desees crear la firma");
    }
    
    public void cerrarFrame() {
        this.dispose();
    }

    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.BLACK);
        g.drawRect(startX, startY, ancho, alto);
    }
    public boolean getFirmaDeseada() {
    	return this.firmaDeseada;
    }
    
    public int getAncho() {
    	return ancho;
    }
    
    public int getAlto() {
    	return alto;
    }
    public int getX() {
    	return startX;
    }
    
    public int getY() {
    	return startY;
    }
    
    public void validarSeleccion() {
    	int option = JOptionPane.showOptionDialog(null,
				"¿Desea confirmar el área seleccionado para la firma?",
				"Selección de área",JOptionPane.YES_NO_OPTION, 
			      JOptionPane.QUESTION_MESSAGE, 
			      null, null, null);
    	if(option == 0) {
    		firmaDeseada = true;
    		this.setVisible(false);
    		synchronized (this) {
                this.notifyAll();
            }
    	}
    }
}
