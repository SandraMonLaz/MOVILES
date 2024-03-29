package es.ucm.videojuegos.moviles.logica.board;

import java.util.Random;

import es.ucm.videojuegos.moviles.engine.Pair;
import es.ucm.videojuegos.moviles.logica.board.Square.SquareType;
/*Clase que implementa y gestiona el tablero del juego desde su creacion hasta su comprobacion.
 */
public class Board {
    /*Constructora de la clase que crea un tablero de tamxtam*/
    public Board(int tam){
        _dimensions = tam;
        _gameBoard = new Square[_dimensions][_dimensions];
        _solutionBoard = new Square[_dimensions][_dimensions];
        _hintsManager = new HintsManager();
        _currentVoid = tam * tam;
        //variable que guarda si el numero de azules y rojos es distinto de 0 o si el numero de azules
        //que ven los numeros es menor o igual que el tamanio del tablero
        boolean non_zeros;
        do {
        	non_zeros = doBoard();
        } while(!non_zeros || !isValid());  //mientras que no sea valido
        prepareBoard();
    }

    /*Metodo que devuelve si el tablero que se ha resuelto es igual al de la solucion
    * @return Boolean de si la solucion obtenida es correcta*/
    public boolean isCorrect(){
        //Vemos si la solucion dada se corresponde con el tablero de la solcion
        for(int i = 0; i < this._dimensions; i++) {
            for(int j = 0; j < this._dimensions; j++) {
                if(this._solutionBoard[i][j].getCurrentType() != this._gameBoard[i][j].getCurrentType())
                    return false;
            }
        }
        return true;
    }

    /*Metodo que devuelve una pareja que almacena una pista y la posicion de la casilla sobre la que se aplica
    * @return Pareja pista/posicion*/
    public Pair<String,Vector2D> getHint(){
        return this._hintsManager.getHint(this);
    }

    /* Modifica el tipo de la casilla dado su tipo actual
	 * @param casilla que se modifica*/
    public void modifySquare(Square square) {
    	switch(square.getCurrentType()) {
    	case RED:
    		square.setType(SquareType.VOID);
    		break;
    	case BLUE:
    		square.setType(SquareType.RED);
    		break;
    	case VOID:
    		square.setType(SquareType.BLUE);
    		break;
    	}
    }

    /* Suma el valor de vacias al actual valor
	 * @param mod modificador que se suma. Si es negativo se resta*/
    public void modifyVoid(int mod) { _currentVoid +=mod;}

    /*Obtiene el numero actual de casillas vacias en el tablero
    * @return Numero de casillas vacias actuales del tablero*/
    public int getCurrentNumberOfVoid() { return _currentVoid;}

    /*Obtiene el numero total de vacios desde la creacion del tablero
    * @return Numero de casillas totales y modificables del tablero */
    public int getTotalNumberOfVoid() { return  _totalVoid;}

    /*Obtiene la dimension del tablero
    * @return dimensiones del tablero*/
    public int getDimensions() { return _dimensions;}

    /*Obtiene el tablero de juego
    * @return Array que contiene todas las casillas del tablero*/
    public Square[][] getBoard() {return _gameBoard;}

    /*Obtiene el tablero de solucion
     * @return Array que contiene todas las casillas del tablero*/
    public Square[][] getSoltionBoard() {return _solutionBoard;}

    /* Miramos cuantos azules hay alrededor de una casilla hasta encontrar una pared
     * @param pos (actual desde la cual se mira en el tablero)
     * @param tablero 0 indica que miran azules utilizando el tablerode la solucion;
     * 1 indica que se mira en el tablero del juego y se cuentan azules teniendo en cuenta los vacios;
     * y diferente a 0 y 1 que se mira en el juego sin importar los vacios
     * @return numero de casillas que se han encontrado*/
    protected int lookAround(Vector2D pos, int board){
        Vector2D[] dir = {new Vector2D(1,0),new Vector2D(-1,0),new Vector2D(0,1),new Vector2D(0,-1)};
        int numVistos =0;
        for(int i =0 ;i < 4; i++){
            if(board == 0)
                numVistos += lookAroundRecInSolutionBoard(pos,dir[i]);
            else if(board == 1)
                numVistos += lookAroundRecParcial(pos,dir[i]);
            else
                numVistos += lookAroundRecInGame(pos,dir[i]);
        }
        return numVistos;
    }

    /*Mira en el tablero del juego de manera recursiva dada una posiccion y una direccion el numero de azules.
     * Deja de contar al encontrarse con un rojo o con un vacio
     * @param pos inicial desde la cual se busca
     * @param dir en la que se buscan azules
     * @return numero de casillas que se han encontrado*/
    protected int lookAroundRecParcial(Vector2D pos, Vector2D dir){
        Vector2D newPos = new Vector2D(pos.getX()+ dir.getX(),pos.getY()+ dir.getY());
        if( newPos.getX() < 0 || newPos.getX() >= _dimensions || newPos.getY() < 0 || newPos.getY() >= _dimensions ||   	//Si me he salido de cualquier limite
                _gameBoard[newPos.getX()][newPos.getY()].getCurrentType() == SquareType.RED ||							    // Si me he encontrado un muro
                _gameBoard[newPos.getX()][newPos.getY()].getCurrentType() == SquareType.VOID)								// Si me he encontrado con un vac�o
            return 0;
        return 1 + lookAroundRecParcial(newPos,dir);
    }

    /* Dada una direccion, busca el ultimo espacio vacio sin paredes de por medio
     * @param dir direccion en la que buscara un sospechoso
     * @param tablero (del juego)
     * @param pos posicion de la casilla desde la que partir
     * @return Casilla vacia encontrada o null en caso de que no existiera ninguna*/
    protected Square searchFirstVoid(Vector2D dir, Vector2D pos) {
        Vector2D newPos = new Vector2D(pos.getX()+ dir.getX(),pos.getY()+ dir.getY());

        if( newPos.getX() < 0 || newPos.getX() >= _dimensions ||  						    //Si me he salido por las X's
                newPos.getY() < 0 || newPos.getY() >= _dimensions ||   						//Si me he salido por las Y's
                _gameBoard[newPos.getX()][newPos.getY()].getCurrentType() == SquareType.RED)			//La siguiente es roja
            return null;

        else if (_gameBoard[newPos.getX()][newPos.getY()].getCurrentType() == SquareType.BLUE)	//La siguiente es azul
            return searchFirstVoid(dir, newPos);
        else
            return _gameBoard[newPos.getX()][newPos.getY()];
    }
    /*Comprueba si el tablero generado es resoluble dadas las pistas
     * que proporciona el juego. Además comprueba que la solucion es correcta, es deir
     * que todos los numeros no modificables ven los numeros que les corresponden y además que
     * todos los azules vean menos que las dimensiones del tablero
     * @return Boolean que indica si el tablero es valido*/
    private boolean isValid(){
    	if(!this._hintsManager.isValid(this))
    		return false;
        //Vemos si los numeros se corresponden y pueden ser correctos
        for(int i = 0; i < this._dimensions; i++) {
            for(int j = 0; j < this._dimensions; j++) {
                if(this._gameBoard[i][j].getNumber() > 0){
                    int lookAround = lookAround(this._gameBoard[i][j].getPos(),2);
                    if(lookAround != this._gameBoard[i][j].getNumber() && lookAround <= this._dimensions)
                        return false;
                }
            }
        }
        return true;
    }
    
    /*Genera un tablero de manera aleatoria. Siendo un 75% azules y un 25% rojos
    * @return boolean que indica si es posible generar un tablero valido a partir de la configuracion actual*/
    private boolean doBoard(){
        Random rand = new Random();
        int blue = 0, red = 0;
        for(int i = 0; i < _dimensions; i++){
            for(int j = 0; j < _dimensions; j++){
            	//Rellenamos el tablero del juego con vac�os
                _gameBoard[i][j] = new Square(SquareType.VOID,0,true, new Vector2D(i,j),this);
                int caso = rand.nextInt(100);
                //Azul
                if(caso < 60) {
                	_solutionBoard[i][j] = new Square(SquareType.BLUE,0,false, new Vector2D(i,j),this);
            		blue++;
                }

                //Rojo
                else {
                	red++;
                	_solutionBoard[i][j] = new Square(SquareType.RED,0,false, new Vector2D(i,j),this);
                }
            }
        }
        //Escogemos casillas para ponerlas estaticas
        return blue > 0 && red > 0 && chooseSquares(blue, red);
        
    }
    
    /*Mira en el tablero de la solcion de manera recursiva dada una posiccion y una direccion el numero de azules.
     * Usado para calcular el numero en los no modificables a la hora de generar el tablero
     * Deja de contar al encontrarse con un rojo
     * @param pos inicial desde la cual se busca
     * @param dir en la que se buscan azules
     * @return contador de casillas interesantes hasta llegar a una roja o salirnos del tablero*/
    private int lookAroundRecInSolutionBoard(Vector2D pos, Vector2D dir){
        Vector2D newPos = new Vector2D(pos.getX()+ dir.getX(),pos.getY()+ dir.getY());
        if( newPos.getX() < 0 || newPos.getX() >= _dimensions || newPos.getY() < 0 || newPos.getY() >= _dimensions ||   //Si me he salido de cualquier limite
            _solutionBoard[newPos.getX()][newPos.getY()].getCurrentType() == SquareType.RED)                            //Si me he encontrado un muro
            return 0;
        return 1 + lookAroundRecInSolutionBoard(newPos,dir);
    }
    
    /*Mira en el tablero del juego de manera recursiva dada una posiccion y una direccion el numero de azules.
     * Deja de contar al encontrarse con un rojo
     * @param pos inicial desde la cual se busca
     * @param dir en la que se buscan azules
     * @return contador de caillas azules o vacias hasta encontrar una roja o salirnos del tablero*/
    private int lookAroundRecInGame(Vector2D pos, Vector2D dir){
        Vector2D newPos = new Vector2D(pos.getX()+ dir.getX(),pos.getY()+ dir.getY());
        if( newPos.getX() < 0 || newPos.getX() >= _dimensions || newPos.getY() < 0 || newPos.getY() >= _dimensions ||   //Si me he salido de cualquier limite
		_gameBoard[newPos.getX()][newPos.getY()].getCurrentType() == SquareType.RED)									  	// Si me he encontrado un muro
            return 0;
        return 1 + lookAroundRecInGame(newPos,dir);
    }


    /*Escoge de manera aleatoria
    * @param limAzules Numero maximo de azules a escoger
    * @param limRojos Numero maximo de rojos a escoger
    * @return Boolean que indica si ha sido posible escoger las casillas*/
    private boolean chooseSquares(int limAzules, int limRojos){
    	
        int blue =  _dimensions * _dimensions /4;
        int red =  (int)(_dimensions * _dimensions /4.5f);
        
        if(blue > limAzules || red > limRojos) return false;
        
        //Actualizo el valor de las casillas vac�as
        _currentVoid = _dimensions * _dimensions;
        
        Random rand = new Random();
        //Mientras que haya azules que posicionar, busco una posicion aleatoria
        // y aniado al tablero del juego siempre que sea posible
        while(blue>0){
            int posX=rand.nextInt(_dimensions);
            int posY=rand.nextInt(_dimensions);
            if(_solutionBoard[posX][posY].getCurrentType() == SquareType.BLUE &&		//Si en la solucion es azul
            _gameBoard[posX][posY].getCurrentType() != SquareType.BLUE){			//Si no he visitado ya esta casilla
                _gameBoard[posX][posY].setModificable(false);
                _gameBoard[posX][posY].setType(SquareType.BLUE);
                int numAzules = lookAround(new Vector2D(posX,posY),0);
                if(numAzules == 0 || numAzules>this._dimensions) return false;
                _gameBoard[posX][posY].setNumber(numAzules);
                blue--;
            }
        }
        //Mientras que haya rojos que posicionar, busco una posicion aleatoria
        // y aniado al tablero del juego siempre que sea posible
        while(red>0){
            int posX=rand.nextInt(_dimensions);
            int posY=rand.nextInt(_dimensions);
            if(_solutionBoard[posX][posY].getCurrentType() == SquareType.RED && 	//Si en la solucion es azul
    	    _gameBoard[posX][posY].getCurrentType() != SquareType.RED){			//Si no he visitado ya esta casilla
                _gameBoard[posX][posY].setModificable(false);
                _gameBoard[posX][posY].setType(SquareType.RED);
                red--;
            }
        }

        return true;
    }

    /*Vuelve a poner el tablero del juego a su estado original.
     *Este metodo se llama despues de comprobar que el tablero funcione. */
    private void prepareBoard() {
        //Primero guardamos el numero de azules que ven
        for(int i = 0; i < this._dimensions; i++) {
            for(int j = 0; j < this._dimensions; j++) {
                this._solutionBoard[i][j].setType(this._gameBoard[i][j].getCurrentType());
                if(this._solutionBoard[i][j].getCurrentType() == SquareType.BLUE){
                    int num = lookAround(this._solutionBoard[i][j].getPos(),1);
                    this._solutionBoard[i][j].setNumber(num);
                }
            }
        }
        //Despues guardamos el tipo y ponemos a vacio los modificables
    	for(int i = 0; i < this._dimensions; i++) {
			for(int j = 0; j < this._dimensions; j++) {
				if(this._gameBoard[i][j].is_modificable())
					this._gameBoard[i][j].setType(SquareType.VOID);
			}
		}
        this._totalVoid = this._currentVoid;
    }

    /*Pinta el tablero (Solo para debug)*/
    public void debugBoardState() {
    	for(int i = 0; i < this._dimensions; i++) {
			for(int j = 0; j < this._dimensions; j++) {
				switch(this._gameBoard[i][j].getCurrentType()) {
    		    	case RED:
    		    		System.out.print("X ");
    		    		break;
    		    	case BLUE:
    		    		System.out.print(this._gameBoard[i][j].getNumber() + " ");
    		    		break;
    		    	case VOID:
    		    		System.out.print("- ");
    		    		break;
		    	}			
			}
			
			System.out.print("      |      ");
			
			for(int j = 0; j < this._dimensions; j++) {
				switch(this._solutionBoard[i][j].getCurrentType()) {
		    	case RED:
		    		System.out.print("X ");
		    		break;
		    	case BLUE:
		    		System.out.print("O ");
		    		break;
		    	case VOID:
		    		System.out.print("- ");
		    		break;
		    	}
				
			}
			System.out.print("\n");
    	}
    	
    	System.out.print("////////////////////////// \n");
    }
    
    //Dimensiones del tablero 
    private int _dimensions;
    //Numero de casillas que se encuentran vacias
    private int _currentVoid;
    //Numero de casillas que se encuentran vacias
    private int _totalVoid;
    //Array que representa las distintas casillas del tablero
    private Square[][] _gameBoard;
    private Square[][] _solutionBoard;
    private HintsManager _hintsManager;

}