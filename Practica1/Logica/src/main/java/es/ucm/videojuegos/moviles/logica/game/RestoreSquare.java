package es.ucm.videojuegos.moviles.logica.game;

import es.ucm.videojuegos.moviles.logica.board.Square;
import es.ucm.videojuegos.moviles.logica.board.Vector2D;

/*Clase que guarda la informacion necesaria para devolver a una casilla
* a su estado anterior. Es usada en Restore Manager con el fin de no guardar
* Casilla*/
public class RestoreSquare {

    RestoreSquare(Vector2D pos, Square.SquareType currentType){
        this._currentType = currentType;
        this._position = pos;
    }

    /*Devuelve la posicion de la casilla de la que estamos almacenando informacion
    * @return Posicion de la casilla en cuestion*/
    public Vector2D get_position() {
        return _position;
    }

    /*Devuelve el estado de la casilla que hemos almacenado
    * @return Estado de la casilla*/
    public Square.SquareType get_currentType() {
        return _currentType;
    }

    //Posicion dentro del tablero
    private Vector2D _position;
    //Tipo de la casilla que se quiere guardar
    private Square.SquareType _currentType;
}
