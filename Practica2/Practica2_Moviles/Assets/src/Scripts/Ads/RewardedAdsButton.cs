using UnityEngine;
using UnityEngine.UI;
using UnityEngine.Advertisements;

namespace Flow
{

    /// <summary>
    /// Clase que representa los ads con recompensas por visualizacion
    /// </summary>
    public class RewardedAdsButton : MonoBehaviour, IUnityAdsLoadListener, IUnityAdsShowListener
    {
        [Tooltip("Boton para inicializar el video")]
        [SerializeField] 
        Button _showAdButton;
        [Tooltip("Levelmanager de la escena")]
        [SerializeField]
        LevelManager levelManager;

        string _androidAdUnitId = "Rewarded_Android";
        string _iOSAdUnitId = "Rewarded_iOS";
        string _adUnitId = "Rewarded_Android";

        bool flag = false;

        // Get the Ad Unit ID for the current platform:

        void Awake()
        {
#if UNITY_IOS
		_adUnitId = _iOsAdUnitId;
#elif UNITY_ANDROID
            _adUnitId = _androidAdUnitId;
#endif
            //Disable button until ad is ready to show
            _showAdButton.interactable = false;
        }
        void Start()
        {
            LoadAd();
        }

        // Load content to the Ad Unit:
        public void LoadAd()
        {
            // IMPORTANT! Only load content AFTER initialization (in this example, initialization is handled in a different script).
            Debug.Log("Loading Ad: " + _adUnitId);
            Advertisement.Load(_adUnitId, this);
        }

        // If the ad successfully loads, add a listener to the button and enable it:
        public void OnUnityAdsAdLoaded(string adUnitId)
        {
            Debug.Log("Ad Loaded: " + adUnitId);

            if (adUnitId.Equals(_adUnitId))
            {
                // Enable the button for users to click:
                _showAdButton.interactable = true;
            }
        }

        // Implement a method to execute when the user clicks the button.
        public void ShowAd()
        {
            // Disable the button: 
            _showAdButton.interactable = false;

            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!WARNING!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            // Esta condicion es debida a que la actualizacion tiene un error mientras se encuentra en el editor
            // debido a que hay "cola de listeners". Esto en build no ocurre. Por ello en UNITY EDITOR la primera
            // vez se le pasa el Listener y luego no.
            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#if UNITY_EDITOR
            // Then show the ad:
            if (!flag)
            {
                Advertisement.Show(_adUnitId, this);
                flag = true;
            }
            else 
                Advertisement.Show(_adUnitId);
#else
            Advertisement.Show(_adUnitId, this);
#endif
        }

        // Implement the Show Listener's OnUnityAdsShowComplete callback method to determine if the user gets a reward:
        public void OnUnityAdsShowComplete(string adUnitId, UnityAdsShowCompletionState showCompletionState)
        {
            if (adUnitId.Equals(_adUnitId) && showCompletionState.Equals(UnityAdsShowCompletionState.COMPLETED))
            {
                // Grant a reward.
                levelManager.AddAHint();

                // Load another ad:
                Advertisement.Load(_adUnitId, this);
            }
        }

        // Implement Load and Show Listener error callbacks:
        public void OnUnityAdsFailedToLoad(string adUnitId, UnityAdsLoadError error, string message)
        {
            Debug.Log($"Error loading Ad Unit {adUnitId}: {error.ToString()} - {message}");
            // Use the error details to determine whether to try to load another ad.
        }

        public void OnUnityAdsShowFailure(string adUnitId, UnityAdsShowError error, string message)
        {
            Debug.Log($"Error showing Ad Unit {adUnitId}: {error.ToString()} - {message}");
            // Use the error details to determine whether to try to load another ad.
        }

        public void OnUnityAdsShowStart(string adUnitId) { }
        public void OnUnityAdsShowClick(string adUnitId) { }

        void OnDestroy()
        {
            // Clean up the button listeners:
            _showAdButton.onClick.RemoveAllListeners();
        }
    }
}