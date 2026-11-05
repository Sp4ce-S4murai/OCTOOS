package com.pedro.octoos.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.pedro.octoos.data.ActiveTripState
import com.pedro.octoos.data.LocationTracker
import com.pedro.octoos.data.ResolvedDestination
import com.pedro.octoos.data.TravelMode
import com.pedro.octoos.ui.theme.BebasNeueFamily
import com.pedro.octoos.ui.theme.IbmPlexMonoFamily
import com.pedro.octoos.ui.theme.OctoBackground
import com.pedro.octoos.ui.theme.OctoBorder
import com.pedro.octoos.ui.theme.OctoGray
import com.pedro.octoos.ui.theme.OctoLime
import com.pedro.octoos.ui.theme.OctoOSTheme
import com.pedro.octoos.ui.theme.OctoText
import com.pedro.octoos.ui.theme.OrbitronFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import com.pedro.octoos.data.TelemetryState
import com.pedro.octoos.data.WeatherState
import com.pedro.octoos.ui.components.OctoStatusStrip

@Composable
fun MapScreen(
    activeTrip: ActiveTripState,
    userLocation: LatLng?,
    isOnline: Boolean,
    isGpsEnabled: Boolean,
    hasLocationPermission: Boolean,
    onRequestGpsOrPermission: () -> Unit,
    onStartRealTrip: (ActiveTripState) -> Unit,
    onStopTrip: () -> Unit,
    onOpenGoogleMapsIntent: (String, TravelMode) -> Unit,
    onBackClick: () -> Unit,
    telemetry: TelemetryState = TelemetryState(),
    weather: WeatherState = WeatherState(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var addressQuery by remember { mutableStateOf(activeTrip.destinationAddress) }
    var selectedMode by remember { mutableStateOf(activeTrip.travelMode) }
    var noAddressResults by remember { mutableStateOf(false) }

    val currentOrigin = remember(userLocation, activeTrip.originLat, activeTrip.originLng) {
        userLocation ?: LatLng(activeTrip.originLat, activeTrip.originLng)
    }
    var resolvedDestLatLng by remember(activeTrip.destinationLat, activeTrip.destinationLng) {
        mutableStateOf(LatLng(activeTrip.destinationLat, activeTrip.destinationLng))
    }

    fun performAddressSearch(launchExternalMaps: Boolean) {
        val query = addressQuery.trim()
        if (query.isEmpty()) return
        // Sem GPS não há origem real: nunca traçar rota a partir de (0,0).
        if (userLocation == null) {
            if (launchExternalMaps) onOpenGoogleMapsIntent(query, selectedMode)
            return
        }
        scope.launch {
            val geocoded = LocationTracker.geocodeAddress(context, query)
            if (geocoded == null) {
                noAddressResults = true
                return@launch
            }
            noAddressResults = false
            resolvedDestLatLng = geocoded.latLng
            onStartRealTrip(
                LocationTracker.buildTripWithRoute(
                    origin = currentOrigin,
                    destination = geocoded,
                    travelMode = selectedMode
                )
            )
            if (launchExternalMaps) {
                onOpenGoogleMapsIntent(query, selectedMode)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OctoBackground)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        MapTopBar(
            isOnline = isOnline,
            isGpsEnabled = isGpsEnabled && hasLocationPermission,
            onGpsStatusClick = onRequestGpsOrPermission,
            onBackClick = onBackClick
        )

        Spacer(modifier = Modifier.height(10.dp))

        OctoStatusStrip(
            coordsText = userLocation?.let { String.format(Locale.US, "X %.4f · Y %.4f", it.latitude, it.longitude) }
                ?: "GPS SEM SINAL",
            coordsActive = userLocation != null,
            tempText = "EXT ${weather.temperatureCelsius?.let { "$it°C" } ?: "--°C"}  ·  BAT ${telemetry.tempCelsius?.let { "$it°C" } ?: "--°C"}",
            tempHot = (telemetry.tempCelsius ?: 0) > 60
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (!isOnline) {
            OfflineMapStateBox(modifier = Modifier.weight(1f))
        } else {
            AddressSearchBar(
                query = addressQuery,
                onQueryChange = {
                    addressQuery = it
                    if (noAddressResults) noAddressResults = false
                },
                onSearchSubmit = { performAddressSearch(launchExternalMaps = true) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            TravelModeRow(
                selectedMode = selectedMode,
                distanceLabel = activeTrip.remainingDistanceKmLabel,
                durationLabel = activeTrip.remainingDurationLabel,
                onSelectMode = { newMode ->
                    selectedMode = newMode
                    if (activeTrip.isActive && userLocation != null) {
                        scope.launch {
                            onStartRealTrip(
                                LocationTracker.buildTripWithRoute(
                                    origin = currentOrigin,
                                    destination = ResolvedDestination(
                                        formattedName = addressQuery.ifBlank { activeTrip.destinationAddress },
                                        latLng = resolvedDestLatLng
                                    ),
                                    travelMode = newMode
                                )
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                noAddressResults -> {
                    NoAddressResultsBox(modifier = Modifier.weight(1f))
                }

                !isGpsEnabled || !hasLocationPermission -> {
                    NoGpsStateBox(
                        hasPermission = hasLocationPermission,
                        onActivateClick = onRequestGpsOrPermission,
                        modifier = Modifier.weight(1f)
                    )
                }

                userLocation == null -> {
                    WaitingGpsBox(modifier = Modifier.weight(1f))
                }

                else -> {
                    InteractiveGoogleMapBox(
                        origin = currentOrigin,
                        destination = resolvedDestLatLng,
                        destinationLabel = addressQuery.ifBlank { activeTrip.destinationAddress },
                        routePoints = tripRoutePoints(activeTrip, currentOrigin, resolvedDestLatLng),
                        nextManeuverDistance = activeTrip.nextManeuverDistanceLabel,
                        hasLocationPermission = hasLocationPermission,
                        onMapTapNewDestination = { tappedLatLng ->
                            resolvedDestLatLng = tappedLatLng
                            scope.launch {
                                onStartRealTrip(
                                    LocationTracker.buildTripWithRoute(
                                        origin = currentOrigin,
                                        destination = ResolvedDestination(
                                            formattedName = "Ponto no mapa",
                                            latLng = tappedLatLng
                                        ),
                                        travelMode = selectedMode
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .background(OctoLime)
                        .clickable { performAddressSearch(launchExternalMaps = true) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ABRIR GOOGLE MAPS",
                        fontFamily = IbmPlexMonoFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = OctoBackground,
                        letterSpacing = 0.8.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .height(46.dp)
                        .background(OctoBackground)
                        .border(1.dp, if (activeTrip.isActive) OctoLime else OctoBorder)
                        .clickable {
                            if (activeTrip.isActive) {
                                onStopTrip()
                            } else {
                                performAddressSearch(launchExternalMaps = false)
                            }
                        }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (activeTrip.isActive) "ENCERRAR" else "TRAÇAR ROTA",
                        fontFamily = IbmPlexMonoFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.sp,
                        color = if (activeTrip.isActive) OctoLime else OctoText,
                        letterSpacing = 0.6.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MapTopBar(
    isOnline: Boolean,
    isGpsEnabled: Boolean,
    onGpsStatusClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NAVEGAÇÃO",
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = OctoLime,
                letterSpacing = 0.6.sp,
                modifier = Modifier.clickable(onClick = onBackClick)
            )
            Text(
                text = when {
                    !isOnline -> "○ OFFLINE"
                    !isGpsEnabled -> "○ SEM GPS"
                    else -> "● GPS ATIVO"
                },
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = if (isOnline && isGpsEnabled) OctoLime else OctoGray,
                modifier = Modifier.clickable(onClick = onGpsStatusClick)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(OctoBorder)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = OctoText, fontWeight = FontWeight.Bold)) {
                    append("MAPA")
                }
                withStyle(SpanStyle(color = OctoLime, fontWeight = FontWeight.Bold)) {
                    append("/")
                }
            },
            fontFamily = OrbitronFamily,
            fontSize = 22.sp,
            letterSpacing = 1.5.sp
        )
    }
}

@Composable
private fun AddressSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .background(OctoBackground)
            .border(1.dp, OctoLime)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "> ",
            fontFamily = IbmPlexMonoFamily,
            fontSize = 13.sp,
            color = OctoLime
        )
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (query.isEmpty()) {
                Text(
                    text = "buscar endereço_",
                    fontFamily = IbmPlexMonoFamily,
                    fontSize = 13.sp,
                    color = OctoLime
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                cursorBrush = SolidColor(OctoLime),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearchSubmit() }),
                textStyle = TextStyle(
                    fontFamily = IbmPlexMonoFamily,
                    fontSize = 13.sp,
                    color = OctoLime
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Text(
            text = "IR →",
            fontFamily = IbmPlexMonoFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = OctoLime,
            modifier = Modifier
                .clickable(onClick = onSearchSubmit)
                .padding(start = 8.dp)
        )
    }
}

@Composable
private fun TravelModeRow(
    selectedMode: TravelMode,
    distanceLabel: String,
    durationLabel: String,
    onSelectMode: (TravelMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TravelMode.entries.forEach { mode ->
            val isSelected = mode == selectedMode
            Box(
                modifier = Modifier
                    .background(if (isSelected) OctoLime else OctoBackground)
                    .border(1.dp, if (isSelected) OctoLime else OctoBorder)
                    .clickable { onSelectMode(mode) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode.label,
                    fontFamily = IbmPlexMonoFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    color = if (isSelected) OctoBackground else OctoText,
                    letterSpacing = 0.8.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "${distanceLabel.uppercase()} · ${durationLabel.uppercase()}",
            fontFamily = IbmPlexMonoFamily,
            fontSize = 10.sp,
            color = OctoGray
        )
    }
}

@Composable
private fun InteractiveGoogleMapBox(
    origin: LatLng,
    destination: LatLng,
    destinationLabel: String,
    routePoints: List<LatLng>,
    nextManeuverDistance: String,
    hasLocationPermission: Boolean,
    onMapTapNewDestination: (LatLng) -> Unit,
    modifier: Modifier = Modifier
) {
    val isPreview = LocalInspectionMode.current
    var mapLoaded by remember { mutableStateOf(false) }
    var mapHintVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(8000L)
        mapHintVisible = !mapLoaded
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0E130D))
            .border(1.dp, OctoBorder)
    ) {
        if (!isPreview) {
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(origin, 14f)
            }
            val mapStyle = remember {
                MapStyleOptions(LocationTracker.CYBER_BRUTAL_MAP_STYLE_JSON)
            }

            LaunchedEffect(origin, destination) {
                runCatching {
                    val bounds = LatLngBounds.builder()
                        .include(origin)
                        .include(destination)
                        .build()
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 72))
                }
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isBuildingEnabled = true,
                    isMyLocationEnabled = hasLocationPermission,
                    mapStyleOptions = mapStyle
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    compassEnabled = true,
                    myLocationButtonEnabled = hasLocationPermission,
                    mapToolbarEnabled = true
                ),
                onMapClick = onMapTapNewDestination,
                onMapLoaded = { mapLoaded = true }
            ) {
                Marker(
                    state = MarkerState(position = destination),
                    title = destinationLabel
                )
                Polyline(
                    points = routePoints,
                    color = OctoLime,
                    width = 9f,
                    pattern = listOf(Dash(22f), Gap(14f))
                )
                Circle(
                    center = origin,
                    radius = 50.0,
                    fillColor = Color(0xFF3A86FF),
                    strokeColor = Color.White,
                    strokeWidth = 4f
                )
            }
        }

        if (mapHintVisible) {
            RouteFallbackCanvas(
                origin = origin,
                destination = destination,
                routePoints = routePoints,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (mapHintVisible) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .background(OctoBackground.copy(alpha = 0.92f))
                    .border(1.dp, OctoLime)
                    .padding(16.dp)
            ) {
                Text(
                    text = "MAPA NÃO CARREGOU · ROTA EM MODO LOCAL\nverifique o Maps SDK for Android e a chave",
                    fontFamily = IbmPlexMonoFamily,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = OctoLime,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Top HUD bar over the live GoogleMap
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(OctoBackground.copy(alpha = 0.78f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = nextManeuverDistance,
                fontFamily = BebasNeueFamily,
                fontSize = 22.sp,
                color = OctoLime
            )
            Text(
                text = "DESTINO · ${destinationLabel.uppercase()}",
                fontFamily = IbmPlexMonoFamily,
                fontSize = 10.sp,
                color = OctoText,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun NoGpsStateBox(
    hasPermission: Boolean,
    onActivateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, OctoBorder),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "SEM GPS",
                fontFamily = BebasNeueFamily,
                fontSize = 44.sp,
                lineHeight = 44.sp,
                color = OctoLime,
                textAlign = TextAlign.Center,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (!hasPermission) {
                    "Permissão de localização necessária para\nexibir sua posição no mapa."
                } else {
                    "O GPS está desativado. Ative a localização\npara atualizar coordenadas e rotas."
                },
                fontFamily = IbmPlexMonoFamily,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = OctoGray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .background(OctoLime)
                    .clickable(onClick = onActivateClick)
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (!hasPermission) "PERMITIR LOCALIZAÇÃO" else "ATIVAR GPS",
                    fontFamily = IbmPlexMonoFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = OctoBackground,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}

@Composable
private fun NoAddressResultsBox(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, OctoBorder),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "SEM\nRESULTADOS",
                fontFamily = BebasNeueFamily,
                fontSize = 44.sp,
                lineHeight = 44.sp,
                color = OctoLime,
                textAlign = TextAlign.Center,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Nenhum endereço encontrado.",
                fontFamily = IbmPlexMonoFamily,
                fontSize = 11.sp,
                color = OctoGray
            )
        }
    }
}

@Composable
private fun OfflineMapStateBox(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, OctoBorder),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "OFFLINE",
                fontFamily = BebasNeueFamily,
                fontSize = 44.sp,
                lineHeight = 44.sp,
                color = OctoLime,
                textAlign = TextAlign.Center,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Sem conexão com a internet.\nO mapa offline (MapLibre) faz parte da Fase 2.",
                fontFamily = IbmPlexMonoFamily,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = OctoGray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(
    name = "Mapa Online — OctoOS",
    showBackground = true,
    backgroundColor = 0xFF0A0A0A,
    widthDp = 360,
    heightDp = 740
)
@Composable
private fun MapScreenOnlinePreview() {
    OctoOSTheme {
        MapScreen(
            activeTrip = ActiveTripState(isActive = true),
            userLocation = LatLng(-23.5505, -46.6333),
            isOnline = true,
            isGpsEnabled = true,
            hasLocationPermission = true,
            onRequestGpsOrPermission = {},
            onStartRealTrip = {},
            onStopTrip = {},
            onOpenGoogleMapsIntent = { _, _ -> },
            onBackClick = {}
        )
    }
}

@Preview(
    name = "Mapa Sem GPS — OctoOS",
    showBackground = true,
    backgroundColor = 0xFF0A0A0A,
    widthDp = 360,
    heightDp = 740
)
@Composable
private fun MapScreenNoGpsPreview() {
    OctoOSTheme {
        MapScreen(
            activeTrip = ActiveTripState(isActive = false),
            userLocation = null,
            isOnline = true,
            isGpsEnabled = false,
            hasLocationPermission = true,
            onRequestGpsOrPermission = {},
            onStartRealTrip = {},
            onStopTrip = {},
            onOpenGoogleMapsIntent = { _, _ -> },
            onBackClick = {}
        )
    }
}

@Preview(
    name = "Mapa Offline — OctoOS",
    showBackground = true,
    backgroundColor = 0xFF0A0A0A,
    widthDp = 360,
    heightDp = 740
)
@Composable
private fun MapScreenOfflinePreview() {
    OctoOSTheme {
        MapScreen(
            activeTrip = ActiveTripState(isActive = false),
            userLocation = null,
            isOnline = false,
            isGpsEnabled = true,
            hasLocationPermission = true,
            onRequestGpsOrPermission = {},
            onStartRealTrip = {},
            onStopTrip = {},
            onOpenGoogleMapsIntent = { _, _ -> },
            onBackClick = {}
        )
    }
}

/** Pontos da rota real; sem rota, desenha a linha reta entre origem e destino. */
fun tripRoutePoints(trip: com.pedro.octoos.data.ActiveTripState, origin: com.google.android.gms.maps.model.LatLng, destination: com.google.android.gms.maps.model.LatLng): List<com.google.android.gms.maps.model.LatLng> =
    if (trip.routePolyline.size >= 2) {
        trip.routePolyline.map { com.google.android.gms.maps.model.LatLng(it.first, it.second) }
    } else {
        listOf(origin, destination)
    }

@Composable
private fun WaitingGpsBox(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, OctoBorder),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "AGUARDANDO GPS",
                fontFamily = BebasNeueFamily,
                fontSize = 40.sp,
                lineHeight = 40.sp,
                color = OctoLime,
                textAlign = TextAlign.Center,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Buscando sua posição. A rota é traçada\nassim que o sinal chegar.",
                fontFamily = IbmPlexMonoFamily,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = OctoGray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RouteFallbackCanvas(
    origin: LatLng,
    destination: LatLng,
    routePoints: List<LatLng>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val allPoints = listOf(origin, destination) + routePoints
        val minLat = allPoints.minOf { it.latitude }
        val maxLat = allPoints.maxOf { it.latitude }
        val minLng = allPoints.minOf { it.longitude }
        val maxLng = allPoints.maxOf { it.longitude }
        val cosLat = kotlin.math.cos(Math.toRadians((minLat + maxLat) / 2.0)).coerceAtLeast(0.2)
        val spanX = ((maxLng - minLng) * cosLat).coerceAtLeast(1e-6).toFloat()
        val spanY = (maxLat - minLat).coerceAtLeast(1e-6).toFloat()
        val pad = 48f
        val drawW = size.width - pad * 2
        val drawH = size.height - pad * 2
        val scale = minOf(drawW / spanX, drawH / spanY)
        val offX = pad + (drawW - spanX * scale) / 2f
        val offY = pad + (drawH - spanY * scale) / 2f

        fun project(p: LatLng): Offset = Offset(
            offX + ((p.longitude - minLng) * cosLat).toFloat() * scale,
            offY + (maxLat - p.latitude).toFloat() * scale
        )

        drawRect(OctoBackground)
        val step = 44.dp.toPx()
        var gx = 0f
        while (gx < size.width) {
            drawLine(OctoLime.copy(alpha = 0.07f), Offset(gx, 0f), Offset(gx, size.height), strokeWidth = 1f)
            gx += step
        }
        var gy = 0f
        while (gy < size.height) {
            drawLine(OctoLime.copy(alpha = 0.07f), Offset(0f, gy), Offset(size.width, gy), strokeWidth = 1f)
            gy += step
        }

        val dash = PathEffect.dashPathEffect(floatArrayOf(22f, 14f))
        if (routePoints.size >= 2) {
            val path = Path()
            routePoints.forEachIndexed { i, p ->
                val o = project(p)
                if (i == 0) path.moveTo(o.x, o.y) else path.lineTo(o.x, o.y)
            }
            drawPath(path, OctoLime, style = Stroke(width = 9f, pathEffect = dash))
        } else {
            drawLine(OctoLime, project(origin), project(destination), strokeWidth = 9f, pathEffect = dash)
        }
        drawCircle(Color(0xFF3A86FF), 12f, project(origin))
        drawCircle(OctoLime, 14f, project(destination))
    }
}
