package logica;

import logica.Casilla.Tipo;

public class Pista1 implements Pista {
	
	@Override
	public boolean EsAplicable(Casilla casilla, Tablero tablero) {
		Vector2D[] dir = {new Vector2D(1,0),new Vector2D(-1,0),new Vector2D(0,1),new Vector2D(0,-1)};
		//Buscamos si existe alg�n vac�o donde poner rojos
		boolean posible = false;
		for(int i = 0; i < 4 ; ++i) {
			Casilla sospechoso = tablero.buscarPrimerVacio(dir[i], casilla.getPos());
			posible = (sospechoso != null) || posible;	//Si es null implica que no existe vac�o para esa direcci�n
		}
		return casilla.getNumero() != 0 &&			//Si es una casilla con numero
			   posible					&&			//Si sus casillas vecinas no son vac�as
			   tablero.mirarAlrededor(casilla.getPos(), 1) == casilla.getNumero();
	}
	
	@Override
	public void AplicarPista(Casilla casilla, Tablero tablero) {
		Vector2D[] dir = {new Vector2D(1,0),new Vector2D(-1,0),new Vector2D(0,1),new Vector2D(0,-1)};
		for(int i = 0; i < 4 ; ++i) {
			Casilla sospechoso = tablero.buscarPrimerVacio(dir[i], casilla.getPos());
			if(sospechoso != null && sospechoso.getTipoActual() == Tipo.VACIO) {
				sospechoso.setTipo(Tipo.ROJO);
				tablero.modificarVacias(-1);
			}
		}
	}

	@Override
	public String GenerarAyuda() {
		return "Si un n�mero tiene ya visibles el n�mero de celdas que dice, entonces se puede cerrar";
	}
	

	

}
