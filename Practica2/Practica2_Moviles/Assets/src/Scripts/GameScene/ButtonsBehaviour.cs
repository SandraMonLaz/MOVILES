using System.Collections;
using System.Collections.Generic;
using UnityEngine;

namespace Flow{
    public class ButtonsBehaviour : MonoBehaviour
    {
        [SerializeField]
        LevelManager lvlManager;
        [SerializeField]
        InterstitialAdExample inter;
        public void GoToSelectMenu(string scene)
        {
            GameManager.getInstance().changeScene(scene);
        }

        public void GetHint()
        {
            lvlManager.getAHint();
        }

        public void RewindAction()
        {
            //
        }

        public void NextLvl()
        {
            GameManager.getInstance().prepareLevel(GameManager.getInstance().getCurrentLevel() + 1);
            inter.ShowAd();
        }

        public void PreviousLvl()
        {
            GameManager.getInstance().prepareLevel(GameManager.getInstance().getCurrentLevel() - 1);
            inter.ShowAd();
        }

        public void RestartLvl()
        {
            lvlManager.restartLevel();
        }

        public void ClosePanel(GameObject panel)
        {
            panel.SetActive(false);
        }
    }
}

