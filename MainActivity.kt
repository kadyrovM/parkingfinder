package com.example.parkingfinder

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.google.android.libraries.places.api.model.CircularBounds

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvStatus: TextView
    private lateinit var btnFind: Button

    private val parkingList = mutableListOf<ParkingPlace>()
    private var userLocation: LatLng? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val apiKey = "AIzaSyCbZbiBXi-xeid28KTEcVZw5-I8eYQgPyM"
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }
        placesClient = Places.createClient(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        tvStatus = findViewById(R.id.tvStatus)
        btnFind = findViewById(R.id.btnFindParking)
        recyclerView = findViewById(R.id.recyclerParking)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Инициализация карты
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btnFind.setOnClickListener {
            checkPermissionAndSearch()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true

        val moscow = LatLng(55.7558, 37.6173)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(moscow, 13f))
    }

    private fun checkPermissionAndSearch() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        } else {
            getUserLocationAndSearch()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            getUserLocationAndSearch()
        } else {
            Toast.makeText(this, "Разрешение на геолокацию необходимо", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getUserLocationAndSearch() {
        tvStatus.text = "Определяем местоположение..."
        btnFind.isEnabled = false

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                userLocation = LatLng(location.latitude, location.longitude)
                googleMap.isMyLocationEnabled = true
                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(userLocation!!, 15f)
                )
                searchNearbyParking(location)
            } else {
                tvStatus.text = "Не удалось определить местоположение"
                btnFind.isEnabled = true
            }
        }
    }

    private fun searchNearbyParking(location: Location) {
        tvStatus.text = "Ищем парковки..."

        val center = com.google.android.gms.maps.model.LatLng(
            location.latitude, location.longitude
        )

        // Поля которые запрашиваем у Places API
        val placeFields = listOf(
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG,
            Place.Field.RATING,
            Place.Field.TYPES
        )

        val circle = CircularBounds.newInstance(
            com.google.android.gms.maps.model.LatLng(location.latitude, location.longitude),
            1500.0
        )

        val searchRequest = SearchNearbyRequest.builder(circle, placeFields)
            .setIncludedTypes(listOf("parking"))
            .setMaxResultCount(15)
            .build()

        placesClient.searchNearby(searchRequest)
            .addOnSuccessListener { response ->
                parkingList.clear()
                googleMap.clear()

                // Маркер пользователя
                googleMap.addMarker(
                    MarkerOptions()
                        .position(userLocation!!)
                        .title("Вы здесь")
                        .icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_AZURE))
                )

                for (place in response.places) {
                    val latLng = place.latLng ?: continue
                    val placeLatLng = LatLng(latLng.latitude, latLng.longitude)

                    // Считаем расстояние
                    val results = FloatArray(1)
                    Location.distanceBetween(
                        location.latitude, location.longitude,
                        latLng.latitude, latLng.longitude,
                        results
                    )

                    val parking = ParkingPlace(
                        name = place.name ?: "Парковка",
                        address = place.address ?: "Адрес не указан",
                        lat = latLng.latitude,
                        lng = latLng.longitude,
                        rating = place.rating?.toFloat() ?: 0f,
                        distanceMeters = results[0]
                    )
                    parkingList.add(parking)

                    // Добавляем маркер на карту
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(placeLatLng)
                            .title(parking.name)
                            .snippet("${parking.distanceMeters.toInt()} м • ⭐${parking.rating}")
                            .icon(BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_VIOLET))
                    )
                }

                // Сортируем по расстоянию
                parkingList.sortBy { it.distanceMeters }// Обновляем список
                recyclerView.adapter = ParkingAdapter(parkingList) { parking ->
                    // Клик по карточке — летим на карту
                    googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(parking.lat, parking.lng), 17f
                        )
                    )
                }

                tvStatus.text = "Найдено: ${parkingList.size} парковок"
                btnFind.isEnabled = true
            }
            .addOnFailureListener { e ->
                tvStatus.text = "Ошибка поиска: ${e.message}"
                btnFind.isEnabled = true
            }
    }
}