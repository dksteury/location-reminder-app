package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    private val REQUEST_LOCATION_PERMISSION = 1

    private var locationPermissionGranted = false

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var mapFragment : SupportMapFragment
    private lateinit var map: GoogleMap

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var selectedPOI : PointOfInterest? = null
    private var selectedLatLong : LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        checkLocationPermission()

//        TODO: add the map setup implementation
        //decided to do this in checkLocationPermission or onRequestPermissionsResult
        //so that onMapReady isn't called until permission is granted or denied
//        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync(this)
//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected


//        TODO: call this function after the user confirms on the selected location
        binding.buttonSaveLocation.setOnClickListener {
            if (selectedLatLong == null) {
                _viewModel.showErrorMessage.postValue(getString(R.string.select_location_or_poi))
            } else {
                onLocationSelected()
            }
        }

        return binding.root
    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
        if (selectedPOI == null) {
            Log.i("onLocationSelected", "selectedPOI == null")
            _viewModel.reminderSelectedLocationStr.value = getString(R.string.dropped_pin)
        } else {
            Log.i("onLocationSelected", "selectedPOI != null")
            _viewModel.selectedPOI.value = selectedPOI
            _viewModel.reminderSelectedLocationStr.value = selectedPOI?.name
        }
        _viewModel.latitude.value = selectedLatLong?.latitude
        _viewModel.longitude.value = selectedLatLong?.longitude
        _viewModel.navigationCommand.postValue(NavigationCommand.Back)
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context,
                    R.raw.map_style
                )
            )
            if (!success) {
                Log.e("setMapStyle", "Style parsing failed.")
            }
        }
        catch (e: Resources.NotFoundException) {
            Log.e("setMapStyle", "Can't find style. Error: ", e)
        }
    }
    private fun setMapClick(map:GoogleMap) {
        map.setOnMapClickListener { latLng ->
            map.clear()
            selectedLatLong = latLng
            selectedPOI = null
            val snippet = String.format(
                    Locale.getDefault(),
                    "Lat: %1$.5f, Long: %2$.5f",
                    latLng.latitude,
                    latLng.longitude
            )
            map.addMarker(
                    MarkerOptions()
                            .position(latLng)
                            .title(getString(R.string.dropped_pin))
                            .snippet(snippet)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
            )
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            map.clear()
            selectedPOI = poi
            selectedLatLong = poi.latLng
            val poiMarker = map.addMarker(
                    MarkerOptions()
                            .position(poi.latLng)
                            .title(poi.name)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
            )
            poiMarker.showInfoWindow()
        }
    }

    @SuppressLint("MissingPermission")
    private fun zoomUser() {
        fusedLocationProviderClient
            .lastLocation.addOnSuccessListener { lastKnownLocation ->
                if (lastKnownLocation != null) {
                    Log.i("zoomUser", "lastKnownLocation is not null!")
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            LatLng(lastKnownLocation.latitude,
                            lastKnownLocation.longitude), 15f))
                } else {
                    Log.i("zoomUser", "lastKnownLocation is null!")
                }
        }
    }

    private fun isPermissionGranted() : Boolean {
        return ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkLocationPermission() {
        if (isPermissionGranted()) {
            locationPermissionGranted = true
            mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
        } else {
            requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
            )
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        setMapStyle(map)

        if (locationPermissionGranted) {
            setMapClick(map)
            setPoiClick(map)
            map.setMyLocationEnabled(true)
            zoomUser()
            _viewModel.showToast.postValue(getString(R.string.select_location_or_poi))
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true

                Log.i("onRequestPermissions", "Blue dot fix called!")
                // Note: the rest of the code in this 'if' block is need to allow blue dot and My
                // location button to work the first time location permission is granted
                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        super.onLocationResult(locationResult)
                    }
                }

                with(LocationRequest()) {
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                    interval = 0
                    fastestInterval = 0
//                    numUpdates = 1
                    fusedLocationProviderClient.requestLocationUpdates(this, locationCallback, Looper.myLooper())
                }

            }else {
                _viewModel.showErrorMessage.postValue(getString(R.string.permission_denied_explanation))
            }
            mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
        }
    }

   override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
