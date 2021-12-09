using System.Collections;
using System.Collections.Generic;
using UnityEngine;

namespace Flow
{

    public class Pipe
    {
        List<Tile> _currentPipe;
        List<TileInfo> _lastPipe;

        Tile _firstTile;
        Tile _secondTile;
        Tile _firstRemoved;
        bool _finished;
        bool haveBeenReversed;


        public Pipe(Tile firstTile, Tile lastTile)
        {
            _firstTile = firstTile;
            _secondTile = lastTile;
            _finished = false;
            _lastPipe = new List<TileInfo>();
            _currentPipe = new List<Tile>();
            _firstRemoved = null;
        }

        #region Metodos de control

        public Color getColor()
        {
            return _firstTile.getColor();
        }

        /// <summary>
        /// Metodo que limpia la pipe por completo
        /// </summary>
        public void clearPipe()
        {
            //Me deshago de todos los tiles
            removeTilesRange(0, _currentPipe.Count);

            //Los tiles que representan los circulos hay que avisarles de que ya no tienen ninguna dir para que escondan sus renderers
            _firstTile.setDirection(new Vector2(0, 0));
            _secondTile.setDirection(new Vector2(0, 0));

            //La pipe pasa a estar incompleta
            _finished = false;
        }

        /// <summary>
        /// Aniade un nuevo tile a la tuberia, devuelve true si se ha cortado a si misma
        /// </summary>
        /// <param name="newTile"></param>
        public bool addTileToPipe(Tile newTile)
        {
            //La tuberia no debe contener ya al tile ni estar terminada aun para meter nuevos tiles
            if (!_currentPipe.Contains(newTile))
            {
                //Si el nuevo tile es uno de los extremos y ya habia tiles en la tuberia, significa que se ha cerrado
                if ((newTile == _secondTile || newTile == _firstTile) && _currentPipe.Count > 1)
                    _finished = true;

                _currentPipe.Add(newTile);

                return false;
            }
            else if (_currentPipe.Count > 1)     //Volver atras solo si hay mas tiles en la lista que el primer circulo
            {
                removeTilesRange(_currentPipe.IndexOf(newTile) + 1, _currentPipe.Count);
                return true;
            }
            else return false;
        }

        /// <summary>
        /// Elimina los tiles necesarios cuando la tuberia es cortada por otra
        /// </summary>
        /// <param name="cutted"></param>
        public void cut(Tile cutted)
        {
            //Obtener indice del corte
            int where = _currentPipe.IndexOf(cutted);

            //Si la tuberia no esta cerrada, se eliminan los tiles desde el corte hasta el final
            if (!_finished)
                removeTilesRange(where, _currentPipe.Count);
            //Si esta cerrada, se corta el trazo con mas cantidad de tiles respecto al corte
            else
            {
                if (where < _currentPipe.Count - 1 - where)
                {
                    removeTilesRange(0, where + 1);
                    _currentPipe.Reverse();
                    haveBeenReversed = true;
                }
                else
                    removeTilesRange(where, _currentPipe.Count);
            }



            //La tuberia deja de estar cerrada
            _finished = false;
        }

        /// <summary>
        /// Elimina los tiles necesarios cuando la tuberia es cortada por otra
        /// </summary>
        /// <param name="newHead"></param>
        public Tile establishNewHead(Tile newHead)
        {
            //Obtener indice del corte
            int where = _currentPipe.IndexOf(newHead);
            Tile behind;
            //Si la tuberia no esta cerrada, se eliminan los tiles desde el corte hasta el final
            if (!_finished)
            {
                removeTilesRange(where + 1, _currentPipe.Count);
            }
            //Si esta cerrada, se corta el trazo con mas cantidad de tiles respecto al corte
            else
            {
                if (where < _currentPipe.Count - 1 - where)
                {
                    removeTilesRange(0, where);
                    _currentPipe.Reverse();
                    haveBeenReversed = true;
                }
                else
                {
                    removeTilesRange(where + 1, _currentPipe.Count);
                }
            }
            //La tuberia deja de estar cerrada
            _finished = false;
            behind = _currentPipe[_currentPipe.IndexOf(newHead) - 1];
            return behind;
        }


        public void saveFlow()
        {
            haveBeenReversed = false;
            _lastPipe.Clear();
            _firstRemoved = null;
            foreach (Tile t in _currentPipe)
            {
                _lastPipe.Add(new TileInfo(t));
            }
        }

        public void restoreFlow()
        {
            if (_currentPipe.Count == _lastPipe.Count) return;

            //Damos la vuelta a lastPipe en caso de haberse invertido tambien currentPipe
            if (haveBeenReversed)
            {
                _lastPipe.Reverse();
                haveBeenReversed = false;
            }

            for (int i = _currentPipe.Count; i < _lastPipe.Count; ++i)
            {
                //Si el tipo del tile es vacio, lo volvemos a ocupar con la info guardada
                if (_lastPipe[i].associatedTile.getTileType() == Tile.TileType.voidTile)
                {
                    _currentPipe.Add(_lastPipe[i].associatedTile);
                    _currentPipe[i].setTileAttributes(_lastPipe[i]);
                    _currentPipe[i].setTileType(_lastPipe[i].type);

                    _currentPipe[i - 1].setTileAttributes(_lastPipe[i - 1]);
                }
                else
                    break;
            }
        }

        /// <summary>
        /// Devuelve si la tuberia esta completa
        /// </summary>
        public bool isCompleted()
        {
            return _finished;
        }

        /// <summary>
        /// Devuelve el ultimo tile almacenado en la lista si existe
        /// </summary>
        public Tile getLastTile() 
        {
            if (_currentPipe.Count > 0) return _currentPipe[_currentPipe.Count - 1];
            else return null;
        }

        /// <summary>
        /// Devuelve el primer tile a continuacion del ultimo corte realizado
        /// </summary>
        /// <returns></returns>
        public Tile getFirstRemoved()
        {
            return _firstRemoved;
        }

        public bool isEmpty()
        {
            return _currentPipe.Count == 0;
        }

        #endregion

        #region Metodos auxiliares
        /// +--------------------------------------------------------------------------------------+
        /// |                                 METODOS AUXILIARES                                   |
        /// +--------------------------------------------------------------------------------------+

        /// <summary>
        /// Elimina y resetea los tiles de la tuberia desde un indice a otro [inicio, final)
        /// </summary>
        /// <param name="beginning"> Indice inicial </param>
        /// <param name="end"> Indice final </param>
        private void removeTilesRange(int beginning, int end)
        {
            Vector2 reset = new Vector2(0, 0);
            //Reseteamos los tiles no circulos a vacios
            for (int i = beginning; i < end; ++i)
            {
                if (_currentPipe[i].getTileType() != Tile.TileType.circleTile)
                {
                    _currentPipe[i].setTileType(Tile.TileType.voidTile);
                }
                _currentPipe[i].setDirection(reset);
            }

            if (beginning == end) Debug.Log("Beginning y end son iguales en " + _firstTile.getColor());

            //Guardamos el primer tile a continuacion del corte realizado
            _firstRemoved = _currentPipe[beginning];

            //Eliminamos de la lista los tiles dentro del rango
            _currentPipe.RemoveRange(beginning, end - beginning);

            if (_currentPipe.Count > 0 && _currentPipe[_currentPipe.Count - 1].getTileType() != Tile.TileType.circleTile)
                _currentPipe[_currentPipe.Count - 1].setTileType(Tile.TileType.pipeHead);

            if (_currentPipe.Count > 0 && _currentPipe[_currentPipe.Count - 1].getTileType() == Tile.TileType.circleTile)
            {
                _currentPipe[_currentPipe.Count - 1].setDirection(new Vector2(0, 0));
            }
        }

        public Tile getTileBehind(Tile ahead)
        {
            int i = 0;
            while (i<_currentPipe.Count && _currentPipe[i] != ahead) i++;

            if (i == 0 || i == _currentPipe.Count) return null;
            else return _currentPipe[i - 1];
        }

        #endregion
    }
}