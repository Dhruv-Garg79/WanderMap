package com.example.dhurv.wandermap

import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, OnStreetViewPanoramaReadyCallback {

    override fun onStreetViewPanoramaReady(streetViewPanorama: StreetViewPanorama?) {
        streetViewPanorama?.setOnStreetViewPanoramaChangeListener{
            if (it?.links == null)
                Toast.makeText(this, "location not available", Toast.LENGTH_SHORT).show();
        }
    }

    private val TAG = "MapsActivity"
    private val REQUEST_LOCATION_PERMISSION = 4
    private lateinit var mMap: GoogleMap
    private val zoom = 15F
    private val home = LatLng(28.0, 78.0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = SupportMapFragment.newInstance()
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, mapFragment).commit()

        mapFragment.getMapAsync(this)
    }

    private fun setInfoWindowClickToPanorama( map : GoogleMap){
        map.setOnInfoWindowClickListener {
            if (it.tag == "poi"){
                val options = StreetViewPanoramaOptions().position(it.position)
                val fragment = SupportStreetViewPanoramaFragment.newInstance(options)
                fragment.getStreetViewPanoramaAsync(this)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment).addToBackStack(null).commit()
            }
        }
    }

    private fun setMapLongClick( map : GoogleMap) {
        map.setOnMapLongClickListener {

            val snippet = String.format(Locale.getDefault(), "Lat: %1$.5f, Long: %2$.5f", it.latitude, it.longitude)

            map.addMarker(MarkerOptions().apply {
                position(it)
                title(getString(R.string.dropped_pin))
                snippet(snippet)
                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            })
        }
    }

    private fun setPoiClickListener( map : GoogleMap) {
        map.setOnPoiClickListener{
            map.addMarker(MarkerOptions().position(it.latLng).title(it.name)).apply {
                tag = "poi"
            }.showInfoWindow()
        }
    }

    private fun enableMyLocation(){
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mMap.isMyLocationEnabled = true
        }
        else ActivityCompat
            .requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_LOCATION_PERMISSION){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                enableMyLocation();
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        try {
            val success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success) Log.e(TAG, "Style Parsing Failed")
        }catch (e : Resources.NotFoundException){
            Log.e(TAG, "${e.message}")
        }

        setMapLongClick(mMap)
        setPoiClickListener(mMap)
        setInfoWindowClickToPanorama(mMap)

        mMap.addMarker(MarkerOptions().position(home).title("Marker in Home"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(home, zoom))

        val homeOverlay = GroundOverlayOptions()
            .image(BitmapDescriptorFactory.fromResource(R.drawable.android))
            .position(home, 100f)
        mMap.addGroundOverlay(homeOverlay)
        enableMyLocation()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_map, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item?.itemId){
            R.id.normal_map -> {
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                true
            }
            R.id.hybrid_map -> {
                mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                true
            }
            R.id.terrain_map -> {
                mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                true
            }
            R.id.satellite_map -> {
                mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                true
            }
            else -> false
        }
    }
}
