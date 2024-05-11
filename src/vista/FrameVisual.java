package vista;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;

/**
 * Elemento que se carga al seleccionar "Firma Visual"
 * en las opciones del JFrame principal.
 * 
 * Este Frame recogerá las coordenadas que desee el usuario seleccionar
 * para añadir la firma digital en el documento.
 */
@SuppressWarnings("serial")
public class FrameVisual extends JFrame implements MouseListener{

	public FrameVisual(int width,int height,int x,int y){
		this.setSize(width,height);
		this.setUndecorated(true);	//Necesario para la transparencia
		this.setBackground(new Color(224,206,67,30));
		this.setLocation(x,y);
		this.setVisible(true);
		this.setEnabled(false);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	
}
