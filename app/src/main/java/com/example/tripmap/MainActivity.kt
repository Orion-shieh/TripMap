package com.example.tripmap

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DensityMedium
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.NearMeDisabled
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapsInitializer
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE
import com.amap.api.maps.model.MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER
import com.amap.api.services.core.PoiItem
import com.amap.api.services.route.BusPathV2
import com.amap.api.services.route.DrivePathV2
import com.amap.api.services.route.RidePath
import com.amap.api.services.route.WalkPath
import com.example.tripmap.ui.theme.TripMapTheme
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream


val screenHeights: ScreenHeights @Composable get() {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    return remember(screenHeight) { ScreenHeights(screenHeight) }
}

class ScreenHeights(total: Dp) {
    val low = total * 0.4f
    val peek = total * 0.55f
    val lowPeek = total * 0.55f - 58.dp
    val medium = total * 0.7f
    val mediumHeigh = total * 0.85f
    val heigh = total * 0.92f
    //val full = total * 1.00f
}
//抽屉高度

data class ImageDetails(
    var path: String,
    var note: String,
    var location: LocationDetails,
)
//图片信息

class SearchLocation(start: LocationDetails? = null, end: LocationDetails? = null) {
    // 使用 by mutableStateOf，这样修改 endLoc 时，Compose 会立刻发现并刷新 UI
    var startLoc by mutableStateOf(start)
    var endLoc by mutableStateOf(end)
}

data class LocationDetails(
    var name: String,
    val lng: Double,
    val lat: Double,
    val method: Int = -1
)

data class TripDetails(
    val id: String,
    val date: String,
    val title: String,
    val startTime: String,
    val endTime: String,
    val fromLoc: LocationDetails,
    val toLoc: LocationDetails,
    val trafficTime: String?,
    val note: String,
    val image: List<ImageDetails>
)
//存储格式，到时候需要把日期格式更改为Date

data class AllRouteResult(
    var busPaths: List<BusPathV2> = emptyList(),
    var drivePaths: List<DrivePathV2> = emptyList(),
    var walkPaths: List<WalkPath> = emptyList(),
    var ridePaths: List<RidePath> = emptyList()
)

class StorageManager(context: Context) {
    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd")
        .create()
    //用于将数据转为Json格式，或者Json转回TripDetails类

    private val fileName = "trip_data.json"
    private val file = File(context.filesDir, fileName)
    //定位到存储文件的路径

    fun saveTripData(dataList: List<TripDetails>) {
        //存储数据
        val jsonString = gson.toJson(dataList)
        //将数据转为Json格式，为单行的字符串
        file.writeText(jsonString)
        //写入存储
    }

    fun loadTripData(): List<TripDetails> {
        //读取数据
        // 去掉大括号，使用 listOf() 包裹
        if (!file.exists()) return listOf(
            TripDetails(
                UUID.randomUUID().toString(), "5号", "标题", "09:00", "10:00",
                LocationDetails("上海市", 31.230525, 121.473667), LocationDetails("北京市", 39.904179, 116.407387, 1),
                null, "备注", emptyList())
        )
        //不存在时返回空列表
        return try {
            val jsonString = file.readText()
            val type = object : TypeToken<List<TripDetails>>() {}.type
            //确保恢复数据时的格式正确
            gson.fromJson(jsonString, type)
        } catch (e: Exception) {
            e.printStackTrace()
            listOf(
                TripDetails(
                    UUID.randomUUID().toString(), "5号", "标题", "09:00", "10:00",
                        LocationDetails("上海市", 31.230525, 121.473667), LocationDetails("北京市", 39.904179, 116.407387, 1),
                    null, "备注", emptyList())
            )
        }
    }
}

class BackupManager(private val context: Context) {
    private val gson = Gson()

    // 导出功能
    fun exportData(tripList: List<TripDetails>, outputUri: Uri) {
        context.contentResolver.openOutputStream(outputUri)?.use { os ->
            ZipOutputStream(BufferedOutputStream(os)).use { zos ->

                // 1. 打包 JSON 数据
                val jsonString = gson.toJson(tripList)
                zos.putNextEntry(ZipEntry("trip_data.json"))
                zos.write(jsonString.toByteArray())
                zos.closeEntry()

                // 2. 打包所有图片 (增加防重名机制)
                val packagedImages = mutableSetOf<String>() // 记录已打包的文件名

                tripList.forEach { trip ->
                    trip.image.forEach { img ->
                        val file = File(img.path)
                        val entryName = "images/${file.name}"

                        // 只有文件存在，且之前没被打包过，才放进 ZIP
                        if (file.exists() && !packagedImages.contains(entryName)) {
                            try {
                                zos.putNextEntry(ZipEntry(entryName))
                                file.inputStream().use { it.copyTo(zos) }
                                zos.closeEntry()
                                packagedImages.add(entryName)
                            } catch (e: Exception) {
                                e.printStackTrace() // 防止个别图片异常中断整个导出
                            }
                        }
                    }
                }
            }
        }
    }

    // 导入功能
    fun importData(inputUri: Uri): List<TripDetails>? {
        var tripList: List<TripDetails>? = null

        try {
            context.contentResolver.openInputStream(inputUri)?.use { inputStream ->
                ZipInputStream(BufferedInputStream(inputStream)).use { zis ->
                    var entry: ZipEntry? = zis.nextEntry
                    while (entry != null) {
                        when {
                            // 读取 JSON（修复了 Buffer 吞噬后续流的 Bug）
                            entry.name == "trip_data.json" -> {
                                val jsonString = zis.readBytes().toString(Charsets.UTF_8)
                                val type = object : TypeToken<List<TripDetails>>() {}.type
                                tripList = gson.fromJson(jsonString, type)
                            }

                            // 读取图片
                            entry.name.startsWith("images/") && !entry.isDirectory -> {
                                val fileName = entry.name.substringAfter("images/")
                                val targetFile = File(context.filesDir, fileName)

                                // 只有当本地没有这张图片时，才需要覆盖写入，节省性能
                                if (!targetFile.exists()) {
                                    targetFile.outputStream().use { fos ->
                                        // zis.copyTo 不会关闭流，非常安全
                                        zis.copyTo(fos)
                                    }
                                }
                            }
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
            }

            // 关键步骤：更新图片绝对路径
            tripList?.forEach { trip ->
                trip.image.forEach { img ->
                    val fileName = File(img.path).name
                    val newFile = File(context.filesDir, fileName)
                    // 更新为当前手机上的绝对路径
                    img.path = newFile.absolutePath
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return null // 导入失败时可以根据业务返回 null
        }

        return tripList
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        MapsInitializer.updatePrivacyShow(this, true, true)
        MapsInitializer.updatePrivacyAgree(this, true)

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            TripMapTheme(dynamicColor = false) {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
                    //FloatingButtonDrawerDemo()
                    Home()
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home() {
    val context = LocalContext.current

    //val startLoc = LocationDetails(name="myLocation", lng=113.40971420638306, lat=23.0535472352902)
    //val endLoc = LocationDetails(name="华南理工大学(五山校区)", lng=113.344859, lat=23.155564)

    var method by remember { mutableIntStateOf(0) }

    var map by remember { mutableStateOf<AMap?>(null) }
    var mapTool = remember { MapTool(context, map) }

    var searchLoc by remember { mutableStateOf("") }
    var needSearch by remember { mutableStateOf(false) }

    val storageManager = remember { StorageManager(context) }
    val tripList = remember {
        mutableStateListOf<TripDetails>().apply { addAll(storageManager.loadTripData()) }
    }

    var toAnimateLoc by remember { mutableStateOf(false) }
    var animateLoc by remember { mutableStateOf<LatLng?>(null) }
    val scope = rememberCoroutineScope()
    val screen = screenHeights

    val mapModeList = listOf(
        AMap.MAP_TYPE_NORMAL,
        AMap.MAP_TYPE_NIGHT,
        AMap.MAP_TYPE_BUS,
        AMap.MAP_TYPE_NAVI
    )

    val dateList = listOf("5号", "6号", "7号", "8号", "9号")
    val pagerState = rememberPagerState(pageCount = { dateList.size })
    val selectedDate = dateList[pagerState.currentPage]

    LaunchedEffect(Unit) {
        val todayDate = SimpleDateFormat("d号晴", Locale.getDefault()).format(Date())

        val index = dateList.indexOf(todayDate)

        if (index != -1) {
            pagerState.animateScrollToPage(index)
        }
    }

    var showEditorForAdd by remember { mutableStateOf(false) }
    var sheetHide by remember { mutableStateOf(false) }
    var sheetPartial by remember { mutableStateOf(false) }

    val chooseRoute = remember { SearchLocation(null, null) }


    var showChooseRoute by remember { mutableStateOf(false) }
    var showHome by remember { mutableStateOf(true) }
    var showSearch by remember { mutableStateOf(false) }
    var showGPS by remember { mutableStateOf(false) }
    var showPoiCard by remember { mutableStateOf(false) }
    var showBusCard by remember { mutableStateOf(false) }


    var busPath by remember { mutableStateOf<BusPathV2?>(null) }
    var showPoiCardPoiItem by remember { mutableStateOf<PoiItem?>(null) }

    var searchPioList by remember { mutableStateOf<List<PoiItem>?>(null) }

    var selectedTrip by remember { mutableStateOf<TripDetails?>(null) }


    var ifCanHide by remember { mutableStateOf(true) }
    var ifAllPoiView by remember { mutableStateOf(false) }
    var ifCenter by remember { mutableStateOf(false) }


    val scaffoldState = rememberBottomSheetScaffoldState(rememberStandardBottomSheetState(skipHiddenState = !ifCanHide))
    val isSheetExpanded = scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded
    val isSheetPartial = scaffoldState.bottomSheetState.currentValue == SheetValue.PartiallyExpanded
    val isSheetHidden = scaffoldState.bottomSheetState.currentValue == SheetValue.Hidden
    var isExpanded by remember { mutableStateOf(false) }


    var methodDropdownMenu by remember { mutableStateOf(false) }

    val methods = remember {listOf("公共交通", "乘车", "步行", "骑行") }

    val methodsIcon = remember {listOf(
        Icons.Default.DirectionsBus,
        Icons.Default.DirectionsCar,
        Icons.AutoMirrored.Filled.DirectionsWalk,
        Icons.AutoMirrored.Filled.DirectionsBike)
    }

    fun mapNotToCenter(){
        ifCenter = false
        map?.myLocationStyle = map?.myLocationStyle?.myLocationType(LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)
    }
    fun mapToCenter(){
        ifCenter = true
        map?.myLocationStyle = map?.myLocationStyle?.myLocationType(LOCATION_TYPE_LOCATION_ROTATE)
    }


    BackHandler(enabled = isSheetPartial || isSheetExpanded || showGPS || showSearch || showBusCard) {
        when {
            showBusCard -> { showBusCard = false; showHome = true; showGPS = false}

            showGPS -> { showGPS = false; map?.clear(); animateLoc = null; chooseRoute.startLoc = null; chooseRoute.endLoc = null }

            showSearch -> { showSearch = false }

            isSheetExpanded -> { scope.launch { scaffoldState.bottomSheetState.partialExpand() } }

            isSheetPartial -> { scope.launch { scaffoldState.bottomSheetState.hide() } }

        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = screen.peek,
            sheetDragHandle = {null},
            sheetContainerColor = Color.Transparent,
            sheetShadowElevation = 0.dp,
            sheetContent = {
                Column {
                    //抽屉上方菜单
                    SheetButton(
                        onLeftClick = {
                            if (ifAllPoiView){
                                ifAllPoiView = false
                                map?.clear()
                                mapTool.getCurrentLocation(true){ }
                            } else {
                                ifAllPoiView = true
                            }
                        },
                        onClick = {
                            if (animateLoc != null){
                                chooseRoute.endLoc = LocationDetails("toLoc", animateLoc!!.longitude, animateLoc!!.latitude)
                            }
                        },
                        onSearch = { locName ->
                            if (locName.isNotBlank()) {
                                searchLoc = locName
                                needSearch = !needSearch
                                showSearch = true
                                mapNotToCenter()
                                ifCanHide = false
                            } else {
                                showSearch = false
                            }
                        }
                    )

                    LaunchedEffect(sheetHide) {
                        if (sheetHide) {
                            scope.launch { scaffoldState.bottomSheetState.hide() }
                            sheetHide = false
                        }
                    }

                    LaunchedEffect(sheetPartial) {
                        if (sheetPartial) {
                            scope.launch { scaffoldState.bottomSheetState.partialExpand() }
                            sheetPartial = false
                        }
                    }


                    LaunchedEffect(toAnimateLoc) {
                        map?.animateCamera(CameraUpdateFactory.newLatLng(animateLoc))
                        map?.addMarker(MarkerOptions().position(animateLoc).icon(BitmapDescriptorFactory.fromResource(
                            R.drawable.place)))
                        toAnimateLoc = false
                    }

                    LaunchedEffect(showHome) {
                        if (showHome && isExpanded) {
                            scope.launch { scaffoldState.bottomSheetState.expand() }
                        } else if(showHome){
                            scope.launch { scaffoldState.bottomSheetState.partialExpand() }
                        } else {
                            scope.launch { scaffoldState.bottomSheetState.hide() }
                        }
                    }

                    LaunchedEffect(showGPS) {
                        if (showGPS) {
                            mapToCenter()

                            map?.moveCamera(CameraUpdateFactory.zoomTo(17f))

                            if (method == 0){
                                showHome = false
                                showBusCard = true
                            }

                        } else {
                            mapNotToCenter()

                            showHome = true
                        }
                    }

                    LaunchedEffect(chooseRoute.startLoc, chooseRoute.endLoc, method) {
                        launchSearchRoute(mapTool, chooseRoute, method){ busPath = it }
                    }


                    if (showSearch){//搜索页面
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(screenHeights.lowPeek)
                                .shadow(4.dp, RoundedCornerShape(28.dp))
                                .background(
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
                                    RoundedCornerShape(28.dp)
                                ),
                        ) {
                            Row {
                                Spacer(Modifier.weight(2.5f))
                                Surface(
                                    modifier = Modifier
                                        .padding(vertical = 12.dp)
                                        .weight(1f)
                                        .background(Color.Transparent),
                                    color = Color(0xCC959595),
                                    shape = CircleShape
                                ) {
                                    Box(Modifier.size(width = 40.dp, height = 4.dp))
                                }
                                Spacer(Modifier.weight(2.5f))
                            }
                            Column (
                                modifier = Modifier.verticalScroll(rememberScrollState()),
                            ){
                                LaunchedEffect(needSearch) {
                                    chooseRoute.endLoc = null
                                    animateLoc = null
                                    mapTool.poiAroundSearch(searchLoc,
                                        onClick = {
                                            animateLoc = LatLng(it.latLonPoint.latitude, it.latLonPoint.longitude)
                                            toAnimateLoc = true
                                            ifCanHide = true
                                            sheetHide = true
                                            showPoiCard = true
                                            mapNotToCenter()
                                            showPoiCardPoiItem = it
                                        },
                                        poiList = { searchPioList = it }
                                    )
                                }
                                searchPioList?.forEach {
                                    PoiLineItem(
                                        poi = it,
                                        ifShowButton = true,
                                        onClick = { }
                                    ) { poiLoc ->
                                        map?.animateCamera(
                                            CameraUpdateFactory.newLatLngZoom(
                                                LatLng(poiLoc.latLonPoint.latitude, poiLoc.latLonPoint.longitude), // 这里你原本写了 location.longitude，改回 it 比较规范
                                                12f
                                            )
                                        )
                                        animateLoc = LatLng(poiLoc.latLonPoint.latitude, poiLoc.latLonPoint.longitude)

                                    }
                                }
                                Spacer(Modifier.height(160.dp))
                            }
                        }
                    } else {//主页
                        ifCanHide = true

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                //.shadow(4.dp, shape = RoundedCornerShape(28.dp))
                                .clip(RoundedCornerShape(28.dp))
                                .height(screen.mediumHeigh)
                                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f))
                        ) {
                            Row {
                                Spacer(Modifier.weight(2.5f))
                                Surface(
                                    modifier = Modifier
                                        .padding(vertical = 12.dp)
                                        .weight(1f)
                                        .background(Color.Transparent),
                                    color = Color(0xCC959595),
                                    shape = CircleShape
                                ) {
                                    Box(Modifier.size(width = 40.dp, height = 4.dp))
                                }
                                Spacer(Modifier.weight(2.5f))
                            }
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color = Color.Transparent),
                                verticalAlignment = Alignment.Top,
                                //userScrollEnabled = false
                            ) { pageIndex ->
                                //每一个抽屉页面
                                val dateAtPage = dateList[pageIndex]
                                Column(modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                ) {
                                    val filteredSortedList by remember(dateAtPage) {
                                        //使用derivedStateOf缓存排序结果
                                        derivedStateOf {
                                            tripList
                                                .filter { it.date == dateAtPage }
                                                .sortedBy { it.startTime }
                                        }
                                    }

                                    if (ifAllPoiView){
                                        AllPoiViewScreen(mapTool, filteredSortedList)
                                        ifAllPoiView = false
                                    }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        filteredSortedList.forEach { item ->
                                            TripLineItem(
                                                tripDetails = item,
                                                onClick = { selectedTrip = item },
                                                onDateClick = { toLoc ->
                                                    if (toLoc.lat != 0.0){
                                                        chooseRoute.startLoc = null
                                                        chooseRoute.endLoc = toLoc
                                                        sheetPartial = true
                                                        showGPS = true
                                                        showBusCard = true
                                                    }
                                                },
                                                onLocClick = { toLoc ->
                                                    if (toLoc.lat != 0.0){
                                                        animateLoc = LatLng(toLoc.lat, toLoc.lng)
                                                        toAnimateLoc = true
                                                        sheetPartial = true
                                                        mapNotToCenter()
                                                    }
                                                },
                                                onRightClick = {fromLoc, toLoc ->
                                                    chooseRoute.startLoc = fromLoc
                                                    chooseRoute.endLoc = toLoc
                                                    method = toLoc.method
                                                    sheetPartial = true
                                                    mapNotToCenter()
                                                }
                                            )
                                        }
                                        Spacer(Modifier.height(200.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                //地图组件导入
                val displayMetrics = context.resources.displayMetrics
                val screenWidthPx = displayMetrics.widthPixels
                val screenHeightPx = displayMetrics.heightPixels
                val isDark = isSystemInDarkTheme()

                MapView { map = it }
                mapTool =  MapTool(context, map)

                LaunchedEffect(map) {
                    mapTool.addHomePoiMarkers(tripList)
                }

                LaunchedEffect(isDark) {
                    val targetType = if (isDark) AMap.MAP_TYPE_NIGHT else AMap.MAP_TYPE_NORMAL
                    if (map?.mapType != targetType) {
                        map?.mapType = targetType
                    }
                }

                LaunchedEffect(isSheetHidden) {
                    if( showHome || showGPS || showSearch || showBusCard){
                        if (!isSheetHidden || showBusCard){
                            map?.let {
                                val centerX = screenWidthPx / 2

                                val centerY = screenHeightPx / 3

                                it.setPointToCenter(centerX, centerY)
                            }
                        } else {
                            map?.let {
                                val centerX = screenWidthPx / 2

                                val centerY = screenHeightPx / 2

                                it.setPointToCenter(centerX, centerY)
                            }
                        }
                    }
                }
                LaunchedEffect(map) {
                    map?.let {
                        val centerX = screenWidthPx / 2

                        val centerY = screenHeightPx / 3

                        it.setPointToCenter(centerX, centerY)
                    }
                }
            }
        }

        TopEndButton(
            tripList = tripList,
            storageManager = storageManager,
            weekDate = selectedDate,
            onClick = {
                mapTool.clearMap()
                chooseRoute.startLoc = null
                chooseRoute.endLoc = null
                showSearch = false
                showPoiCard = false
                if (!(showGPS || showBusCard || showChooseRoute || showEditorForAdd || selectedTrip != null)) {
                    mapTool.addHomePoiMarkers(tripList)
                }

            },
            onMapMode = { map?.mapType = mapModeList[it] }
        ) { newDate ->
            //右上角日期选择
            val index = dateList.indexOf(newDate)
            if (index != -1) { scope.launch { pagerState.animateScrollToPage(index) } }
        }

        if(showHome){
            Column (Modifier.align(Alignment.BottomEnd)){
                FloatingActionButton(
                    onClick = { methodDropdownMenu = true },
                    modifier = Modifier
                        .padding(24.dp)
                        .padding(bottom = 70.dp)
                        .navigationBarsPadding(),
                    containerColor = MaterialTheme.colorScheme.background.copy(0.85f)
                ) { Icon(methodsIcon[method], null, tint = MaterialTheme.colorScheme.primary.copy(0.85f)) }

                DropdownMenu(
                    expanded = methodDropdownMenu,
                    modifier = Modifier.width(82.dp),
                    onDismissRequest = { methodDropdownMenu = false },
                    shape = RoundedCornerShape(15.dp),
                    containerColor = MaterialTheme.colorScheme.background.copy(0.85f),
                    offset = DpOffset(x = 0.dp, y = 6.dp)
                ) {
                    methods.forEach {
                        DropdownMenuItem(
                            text = {
                                Text(it,
                                    fontWeight = FontWeight.Bold,
                                    color = if(methods[method] == it) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                )
                            },
                            onClick = {
                                method = methods.indexOf(it)
                                methodDropdownMenu = false
                                showGPS = false
                            }
                        )
                    }
                }

            }
            if (showPoiCard){
                Card (
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(end = 70.dp, bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    )
                ){
                    showPoiCardPoiItem?.let {
                        PoiLineItem(it, false, { showPoiCard = false }) { location -> }
                    }
                }

                FloatingActionButton(
                    onClick = {
                        if (animateLoc != null){
                            chooseRoute.endLoc = LocationDetails("toLoc", animateLoc!!.longitude, animateLoc!!.latitude)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(all = 24.dp)
                        .padding(bottom = 210.dp)
                        .navigationBarsPadding(),
                    containerColor = MaterialTheme.colorScheme.background.copy(0.85f)
                ) { Icon(Icons.AutoMirrored.Filled.Send, null, tint = MaterialTheme.colorScheme.primary.copy(0.85f)) }
            }

            if ( chooseRoute.endLoc != null ){
                FloatingActionButton(
                    onClick = {
                        ifCanHide = true
                        mapNotToCenter()
                        showChooseRoute = true
                    },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp)
                        .navigationBarsPadding(),
                    containerColor = MaterialTheme.colorScheme.background.copy(0.85f)
                ) { Icon(Icons.Default.Route, null, tint = MaterialTheme.colorScheme.primary.copy(0.85f)) }

                if ( showGPS ){
                    FloatingActionButton(
                        onClick = { showGPS = false },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(24.dp)
                            .padding(bottom = 70.dp)
                            .navigationBarsPadding(),
                        containerColor = MaterialTheme.colorScheme.background.copy(0.85f)
                    ) { Icon(Icons.Default.NearMeDisabled, null, tint = MaterialTheme.colorScheme.primary.copy(0.85f)) }

                } else {
                    FloatingActionButton(
                        onClick = { showGPS = true },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(24.dp)
                            .padding(bottom = 70.dp)
                            .navigationBarsPadding(),
                        containerColor = MaterialTheme.colorScheme.background.copy(0.85f)
                    ) { Icon(Icons.Default.NearMe, null, tint = MaterialTheme.colorScheme.primary.copy(0.85f)) }

                }

            }


            if (scaffoldState.bottomSheetState.currentValue != SheetValue.Hidden){
                //抽屉打开之后

                FloatingActionButton(
                    onClick = { ifCanHide = true; sheetHide = true},
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp)
                        .navigationBarsPadding(),
                    containerColor = MaterialTheme.colorScheme.primary.copy(0.85f)
                ) { Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(34.dp), tint = MaterialTheme.colorScheme.background.copy(0.85f)) }

                if (showSearch){
                    /*
                    FloatingActionButton(
                        onClick = { showSearch = false },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(24.dp)
                            .padding(bottom = 70.dp)
                            .navigationBarsPadding(),
                        containerColor = MaterialTheme.colorScheme.background.copy(0.85f)
                    ) { Icon(Icons.Default.CalendarViewDay, null, tint = MaterialTheme.colorScheme.primary.copy(0.85f)) }

                     */
                } else {
                    FloatingActionButton(
                        onClick = { showEditorForAdd = true },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(24.dp)
                            .padding(bottom = 140.dp)
                            .navigationBarsPadding(),
                        containerColor = MaterialTheme.colorScheme.background.copy(0.85f)
                    ) { Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary.copy(0.85f)) }
                }
            }
            //抽屉打开之后的按钮

            else {
                //抽屉隐藏之后
                if ( ifCenter ) {
                    FloatingActionButton(
                        onClick = { mapNotToCenter() },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(24.dp)
                            .padding(bottom = 140.dp)
                            .navigationBarsPadding(),
                        containerColor = MaterialTheme.colorScheme.background.copy(0.85f)
                    ) { Icon(Icons.Default.LocationOff, null, tint = MaterialTheme.colorScheme.primary.copy(0.85f)) }

                } else {
                    FloatingActionButton(
                        onClick = { mapToCenter() },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(24.dp)
                            .padding(bottom = 140.dp)
                            .navigationBarsPadding(),
                        containerColor = MaterialTheme.colorScheme.background.copy(0.85f)
                    ) { Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary.copy(0.85f)) }

                }


                if (showSearch){
                    FloatingActionButton(
                        onClick = { scope.launch { scaffoldState.bottomSheetState.partialExpand(); ifCanHide = false } },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(24.dp)
                            .navigationBarsPadding(),
                        containerColor = MaterialTheme.colorScheme.primary.copy(0.85f)
                    ) { Icon(Icons.Default.KeyboardArrowUp, null, modifier = Modifier.size(34.dp), tint = MaterialTheme.colorScheme.background) }

                } else {
                    FloatingActionButton(
                        onClick = { scope.launch { scaffoldState.bottomSheetState.partialExpand() } },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(24.dp)
                            .navigationBarsPadding(),
                        containerColor = MaterialTheme.colorScheme.primary.copy(0.85f)
                    ) { Icon(Icons.Default.CalendarViewDay, null, Modifier.size(26.dp), tint = MaterialTheme.colorScheme.background) }

                }
            }
        } else {
            if (showGPS) {
                FloatingActionButton(
                    onClick = {
                        ifCanHide = true
                        showGPS = false
                        showChooseRoute = true
                    },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp)
                        .navigationBarsPadding(),
                    containerColor = MaterialTheme.colorScheme.background.copy(0.85f)
                ) { Icon(Icons.Default.Route, null, tint = MaterialTheme.colorScheme.primary.copy(0.85f)) }

                if ( ifCenter ){
                    FloatingActionButton(
                        onClick = { mapNotToCenter() },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(24.dp)
                            .padding(bottom = 70.dp)
                            .navigationBarsPadding(),
                        containerColor = MaterialTheme.colorScheme.background.copy(0.85f)
                    ) { Icon(Icons.Default.NearMeDisabled, null, tint = MaterialTheme.colorScheme.primary.copy(0.85f)) }

                } else {
                    FloatingActionButton(
                        onClick = { mapToCenter() },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(24.dp)
                            .padding(bottom = 70.dp)
                            .navigationBarsPadding(),
                        containerColor = MaterialTheme.colorScheme.background.copy(0.85f)
                    ) { Icon(Icons.Default.NearMe, null, tint = MaterialTheme.colorScheme.primary.copy(0.85f)) }
                }
            }

            if (showBusCard) {
                Card (
                    modifier = Modifier
                        .padding(start = 100.dp)
                        .fillMaxWidth()
                        .height(screenHeights.lowPeek)
                        .align(Alignment.BottomEnd),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(topStart = 36.dp),
                ){
                    busPath?.let { BusRouteCard(it) }
                }
            }
        }


        if (showChooseRoute && chooseRoute.endLoc != null) {
            var routeResultList by remember { mutableStateOf(AllRouteResult()) }

            LaunchedEffect(chooseRoute.endLoc) {
                if (chooseRoute.startLoc == null) {
                    mapTool.getCurrentLocation { location ->
                        location?.let {
                            mapTool.searchAllRoute(
                                LocationDetails(
                                    name = "myLoc",
                                    lng = it.longitude,
                                    lat = it.latitude
                                ), chooseRoute.endLoc!!
                            ) { list ->
                                routeResultList = list
                            }
                        }
                    }

                } else {
                    mapTool.searchAllRoute(chooseRoute.startLoc!!, chooseRoute.endLoc!!) {
                        routeResultList = it
                    }
                }
            }


            if (routeResultList.drivePaths.isNotEmpty() || routeResultList.walkPaths.isNotEmpty() || routeResultList.ridePaths.isNotEmpty() || routeResultList.busPaths.isNotEmpty()){
                LaunchedEffect(chooseRoute.endLoc) {
                    scope.launch { scaffoldState.bottomSheetState.hide() }
                }
                ChooseRoute(
                    map = map!!,
                    allRouteResult = routeResultList,
                    returnBusPath = { busPath = it},
                    onDismiss = { showGPS = it; showChooseRoute = false; showHome = true; if (showGPS) mapNotToCenter()}
                ){route, mode ->
                    method = mode
                    when(mode){
                        0-> mapTool.drawRouteOnMap(chooseRoute.endLoc!!, mapTool.extractPolyline(route.busPaths[0]), true)
                        1-> mapTool.drawRouteOnMap(chooseRoute.endLoc!!, mapTool.extractPolyline(route.drivePaths[0]), true)
                        2-> mapTool.drawRouteOnMap(chooseRoute.endLoc!!, mapTool.extractPolyline(route.walkPaths[0]), true)
                        3-> mapTool.drawRouteOnMap(chooseRoute.endLoc!!, mapTool.extractPolyline(route.ridePaths[0]), true)
                    }
                }
            }

        }

        LaunchedEffect(showEditorForAdd, selectedTrip) {
            if (showEditorForAdd || selectedTrip != null) {
                isExpanded = isSheetExpanded
            }
        }

        if (showEditorForAdd) {
            showHome = false
            TripEditorScreen(
                mapTool = mapTool,
                tripDetails = TripDetails(UUID.randomUUID().toString(), selectedDate, "", "09:00", "10:00",
                    LocationDetails("", 0.0, 0.0), LocationDetails("", 0.0, 0.0), null, "", emptyList()),
                onDismiss = { showEditorForAdd = false; showHome = true },
            ){ newData ->
                showHome = true
                val index = tripList.indexOfFirst { it.id == newData.id }
                if (index != -1) tripList[index] = newData else tripList.add(newData)
                storageManager.saveTripData(tripList)
                showEditorForAdd = false
            }
        }
        //添加日程界面

        if (selectedTrip != null) {
            showHome = false
            TripDetailScreen(
                mapTool = mapTool,
                tripDetails = selectedTrip!!,
                onClick = {start, end ->
                    showHome = true
                    if (start?.lat != 0.0 && start != null){
                        mapTool.searchRoute(start, end, method, { }){ busPath = it }
                        mapNotToCenter()
                    } else if (end.lat != 0.0){
                        mapTool.toLocation(end, method, { }){ busPath = it }
                        mapNotToCenter()
                    } else {
                        println("无效导航")
                    }
                    chooseRoute.startLoc = start
                    chooseRoute.endLoc = end
                    sheetHide = true
                    selectedTrip = null
                },
                onDismiss = { selectedTrip = null; showHome = true },
                onDelete = {
                    showHome = true
                    tripList.remove(selectedTrip)
                    storageManager.saveTripData(tripList)
                    selectedTrip = null
                },
                onUpdate = { newData ->
                    showHome = true
                    val index = tripList.indexOfFirst { it.id == newData.id }
                    if (index != -1) {
                        tripList[index] = newData
                        storageManager.saveTripData(tripList)
                        selectedTrip = newData
                    }
                },
            )
        }
    }
}
//主页

@OptIn(ExperimentalTextApi::class)
@Composable
fun TripLineItem(tripDetails: TripDetails, onClick: () -> Unit, onDateClick: (location: LocationDetails) -> Unit, onLocClick: (location: LocationDetails) -> Unit, onRightClick: (LocationDetails, LocationDetails) -> Unit) {

    val duration = calculateDuration(tripDetails.startTime, tripDetails.endTime)//计算后的行程所需时间
    val methodsIcon = remember {listOf(
        Icons.Default.DirectionsBus,
        Icons.Default.DirectionsCar,
        Icons.AutoMirrored.Filled.DirectionsWalk,
        Icons.AutoMirrored.Filled.DirectionsBike)
    }

    Row(//这是每个日程的样式
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)//限定高度为内部最高的组件，也就是最小有多高
            .background(Color.Transparent),
        verticalAlignment = Alignment.Top//向上对齐
    ) {
        Card (
            modifier = Modifier
                .padding(start = 10.dp)
                .customShadow(borderRadius = 12.dp)
        ){
            Column(//这是左侧时间栏
                modifier = Modifier
                    .width(60.dp)
                    .padding(top = 2.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onDateClick(tripDetails.toLoc) }
                    .background(MaterialTheme.colorScheme.background)
                    .padding(4.dp),
                //.customShadow(borderRadius = 10.dp),
                //限定左侧时间栏的大小

                horizontalAlignment = Alignment.CenterHorizontally,//水平居中
                verticalArrangement = Arrangement.Top//向上对齐

            ) {
                val tightTextStyle = TextStyle(
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                    //去掉文字默认的上下保留空白
                    lineHeight = 10.sp
                    //文字行高
                )

                Text(//开始时间
                    text = tripDetails.startTime,
                    style = tightTextStyle.copy(
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 16.sp
                    )
                )
                Text(//终止时间
                    text = tripDetails.endTime,
                    style = tightTextStyle.copy(
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                if (duration.isNotBlank()) {
                    Text(//所需时间
                        text = duration,
                        style = tightTextStyle.copy(
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        Box(//这是中间分割线
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(//中间竖线
                Modifier
                    .width(1.5.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.onSecondary)
            )
            Box(//中间圆点
                Modifier
                    .padding(top = 10.dp)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.onTertiary,
                        CircleShape
                    )
            )
        }

        Row (//右侧行程卡片栏
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp, bottom = 12.dp)
                .customShadow()
        ) {
            Card(
                modifier = Modifier
                    .weight(4f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onClick() },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            ) {
                Row (
                    modifier = Modifier.fillMaxWidth()
                ){
                    Column(Modifier
                        .weight(3f)
                        .padding(6.dp)
                        .padding(horizontal = 2.dp)
                    ) {
                        Row {
                            Column (modifier = Modifier.weight(1f)){
                                Text(//行程名称
                                    modifier = Modifier.padding(start = 6.dp),
                                    text = tripDetails.title,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1
                                )

                                val toLoc = tripDetails.toLoc.name //目的地名

                                if (toLoc.isNotBlank()) {

                                    Spacer(Modifier.height(2.dp))
                                    //行程和目的地竖直间距

                                    Row {
                                        Column (modifier = Modifier.weight(1f)){
                                            Card(//目的地卡片
                                                modifier = Modifier
                                                    .height(24.dp)
                                                    .widthIn(max = 160.dp),
                                                onClick = { onLocClick(tripDetails.toLoc)},
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surface
                                                )
                                            ){
                                                Row(
                                                    modifier = Modifier.padding(start = 4.dp, end = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    Icon(//位置图标
                                                        imageVector = Icons.Default.Place,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Text(//目的地
                                                        text = " $toLoc",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        maxLines = 1,
                                                        //modifier = Modifier.basicMarquee()
                                                    )
                                                }
                                            }
                                        }
                                        tripDetails.trafficTime?.let {
                                            Text(
                                                modifier = Modifier.padding(start = 4.dp),
                                                text = it,
                                                fontSize = 14.sp,
                                                color = Color.Red,
                                                maxLines = 2,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                }
                            }
                            if (tripDetails.fromLoc.lat != 0.0 && tripDetails.toLoc.lat != 0.0) {
                                val viewIcon = methodsIcon.getOrNull(tripDetails.toLoc.method)?:Icons.Default.Route

                                FloatingActionButton(
                                    shape = CircleShape,
                                    onClick = {
                                        onRightClick(tripDetails.fromLoc, tripDetails.toLoc)
                                        //mapTool.searchAllRoute(tripDetails.fromLoc, tripDetails.toLoc){println(it)}
                                    },
                                    modifier = Modifier
                                        .size(42.dp)
                                        .padding(top = 6.dp, end = 6.dp),
                                    containerColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ) {
                                    Icon(
                                        viewIcon,
                                        null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )

                                }
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        //目的地和备注竖直间距
                        Text(
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .weight(1f),
                            text = tripDetails.note.takeIf { it.isNotBlank() } ?: "无备注",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 2
                        )


                    }
                }
            }
        }
    }
}
//行程列表的每一个行程的格式


fun launchSearchRoute(mapTool: MapTool, searchLoc: SearchLocation, method: Int, returnBusPath: (BusPathV2) -> Unit){
    var startLoc = searchLoc.startLoc
    val endLoc = searchLoc.endLoc
    if(endLoc == null) {
        println("无效")
    }else if (startLoc == null) {
        mapTool.getCurrentLocation{ myLoc ->
            myLoc?.let{
                startLoc = LocationDetails("myLoc", it.longitude, it.latitude)
                mapTool.searchRoute(startLoc, endLoc, method, { }){ busRoute -> returnBusPath(busRoute) }
            }
        }
    } else {
        mapTool.searchRoute(startLoc, endLoc, method, { }){ returnBusPath(it) }
    }
}
//发起单条路线搜索

@Composable
fun PoiLineItem(poi: PoiItem, ifShowButton: Boolean, onClick: () -> Unit, onRightClick: (PoiItem) -> Unit) {
    Card(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(bottom = 16.dp)
            .fillMaxWidth()
            .customShadow(), // 保留你的自定义阴影
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (ifShowButton) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.background.copy(
                        0.85f
                    )
                )
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 1. 标题
                Text(
                    text = poi.title ?: "未知地点",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .basicMarquee(),
                )

                // 2. 距离（如果是周边搜索且距离 > 0）
                if (poi.distance > 0) {
                    val distanceText = if (poi.distance >= 1000) {
                        String.format("%.1fkm", poi.distance / 1000.0)
                    } else {
                        "${poi.distance}m"
                    }
                    Text(
                        text = distanceText,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 3. 评分与人均（💡 核心修复：直接使用 rating 和 cost）
            Row(verticalAlignment = Alignment.CenterVertically) {
                val rating = poi.poiExtension?.getmRating()
                val openTime = poi.poiExtension?.opentime

                if (!rating.isNullOrEmpty()) {
                    Text(text = "评分：$rating", color = Color(0xFFFFA244), fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                }
                if (!openTime.isNullOrEmpty() && openTime != "0.0") {
                    Text(text = "营业时间$openTime", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically // 建议加上垂直居中更好看
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // 4. 地址与商圈
                    val area = if (!poi.businessArea.isNullOrEmpty()) "${poi.businessArea} | " else ""
                    Text(
                        text = "$area${poi.snippet ?: ""}",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis // 有了 weight 的限制，省略号现在能正常工作了！
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // 5. 业态标签 (切割 typeDes)
                    val typeDes = poi.typeDes
                    if (!typeDes.isNullOrEmpty()) {
                        Row {
                            val typeTags = typeDes.split(";")
                            typeTags.take(5).forEach { tag ->
                                Text(
                                    text = tag,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                if (ifShowButton){

                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(60.dp),
                        contentAlignment = Alignment.BottomEnd // 保持你原来的对齐方式
                    ) {
                        FloatingActionButton(
                            onClick = { onRightClick(poi) },
                            modifier = Modifier.size(40.dp),
                            containerColor = MaterialTheme.colorScheme.primary.copy(0.85f),
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationSearching,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.background.copy(0.85f)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(60.dp),
                        contentAlignment = Alignment.BottomEnd // 保持你原来的对齐方式
                    ) {
                        FloatingActionButton(
                            onClick = { onClick() },
                            modifier = Modifier.size(40.dp),
                            containerColor = MaterialTheme.colorScheme.primary.copy(0.85f),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.background.copy(0.85f)
                            )
                        }
                    }
                }
            }
        }
    }
}
//周边搜索每一个样式


@Composable
fun BusRouteCard(busPath: BusPathV2) {
    //val allDistance = busPath.distance
    val allWalkDistance = busPath.walkDistance - busPath.walkDistance % 100
    val allDuration = busPath.duration
    val allCost = busPath.cost
    var ifShowDetails by remember { mutableStateOf(true) }

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())


    val subwayColor = remember { Color(0xCC1EB9FF) }
    val busColor = remember { Color(0xCCFF952B) }


    var viewColor = remember { subwayColor }
    var viewIcon = remember { Icons.Default.DirectionsSubway }

    @Composable
    fun DrawLine(bowColor: Color) {
        Box(//这是中间分割线
            modifier = Modifier
                .width(32.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(//中间竖线
                Modifier
                    .width(1.5.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.onSecondary)
            )
            Box(//中间圆点
                Modifier
                    .padding(top = 6.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(bowColor)
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.onTertiary,
                        CircleShape
                    )
            )
        }
    }

    Box{
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(0.85f))
        ) {
            // --- 1. 头部概览信息 ---
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Row(
                    modifier = Modifier.padding(all = 16.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // 耗时格式化
                    val hour = allDuration / 3600
                    val minute = allDuration / 60 % 60
                    val timeText = if (hour > 0) "${hour}小时${minute}分" else "${minute}分钟"

                    Text(
                        text = timeText,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "步行${allWalkDistance / 1000}千米",
                        Modifier.padding(start = 12.dp, bottom = 2.dp),
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontSize = 14.sp
                        )
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "￥${allCost}",
                        style = TextStyle(
                            color = busColor
                        ),
                        fontWeight = FontWeight.Medium
                    )


                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    for (step in busPath.steps) {
                        if (step.walk != null && step.walk.distance > 0) {
                            Row(
                                modifier = Modifier.height(IntrinsicSize.Min)
                            ) {
                                DrawLine(Color(0xFF12B768))
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF12B768))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )

                                    Text(
                                        text = "步行${step.walk.distance.toInt()}米",
                                        color = Color.White,
                                        modifier = Modifier.padding(start = 4.dp),
                                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    )
                                }
                            }
                        }
                        if (step.busLines.getOrNull(0) != null) {
                            when (step.busLines[0].busLineType) {
                                "地铁线路" -> {
                                    viewColor = subwayColor; viewIcon = Icons.Default.DirectionsSubway
                                }

                                "普通公交线路" -> {
                                    viewColor = busColor; viewIcon = Icons.Default.DirectionsBus
                                }
                            }
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.height(IntrinsicSize.Min)
                                ) {
                                    DrawLine(viewColor)
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                start = 0.dp,
                                                top = 6.dp,
                                                end = 26.dp,
                                                bottom = 8.dp
                                            )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(viewColor)
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = viewIcon,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "${step.busLines[0].busLineName}",
                                                color = Color.White,
                                                modifier = Modifier.padding(start = 4.dp),
                                                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                            )
                                        }

                                        Text(
                                            text = "• ${step.busLines[0].departureBusStation.busStationName}（" +
                                                    (step.busLines[0].firstBusTime?.let { timeFormat.format(it) + "-" }
                                                        ?: "") +
                                                    (step.busLines[0].lastBusTime?.let { timeFormat.format(it) + "）" }
                                                        ?: ""),
                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                            modifier = Modifier
                                                .padding(start = 12.dp)
                                                .padding(top = 4.dp),
                                            style = TextStyle(
                                                fontSize = 15.sp
                                            )
                                        )

                                        if (ifShowDetails){
                                            step.busLines[0].passStations.forEach {
                                                Text(
                                                    text = "• ${it.busStationName}",
                                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                    modifier = Modifier
                                                        .padding(start = 12.dp)
                                                        .padding(top = 4.dp),
                                                    style = TextStyle(
                                                        fontSize = 15.sp
                                                    )
                                                )
                                            }
                                        }

                                        Text(
                                            text = "• ${step.busLines[0].arrivalBusStation.busStationName}" +
                                                    "（乘${step.busLines[0].passStationNum + 1}站）",
                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                            modifier = Modifier
                                                .padding(start = 12.dp)
                                                .padding(top = 4.dp)
                                                .padding(bottom = 8.dp),
                                            style = TextStyle(
                                                fontSize = 15.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.height(IntrinsicSize.Min)
                    ) {
                        Box(//这是中间分割线
                            modifier = Modifier
                                .width(32.dp)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Box(//中间竖线
                                Modifier
                                    .width(1.5.dp)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.onSecondary)
                            )
                            Spacer(Modifier.height(20.dp))
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { ifShowDetails = !ifShowDetails },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .navigationBarsPadding(),
            containerColor = MaterialTheme.colorScheme.background.copy(0.85f)
        ) { Icon(Icons.Default.DensityMedium, null, tint = MaterialTheme.colorScheme.primary.copy(0.85f)) }

    }
}
//公共交通导航样式

@Composable
fun SheetButton(onLeftClick: () -> Unit, onClick: () -> Unit, onSearch: (String) -> Unit) {
    var locName by remember { mutableStateOf("") }


    // 🌟 关键修改 1：把 Column 换成 Box
    Row (Modifier
        .padding(horizontal = 16.dp)
        .padding(bottom = 6.dp)
    ){
        FloatingActionButton(
            onClick = { onLeftClick() },
            modifier = Modifier.size(50.dp),
            containerColor = MaterialTheme.colorScheme.background.copy(0.85f),
        ) {
            Icon(
                imageVector = Icons.Default.Preview,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(0.85f)
            )
        }

        Column (Modifier
            .height(50.dp)
            .weight(1f)
            .padding(horizontal = 8.dp)){
            Spacer(Modifier.weight(1f))
            CustomHeightTextField(
                value = locName,
                onValueChange = { locName = it },
                label = "搜索",
                icon = Icons.Default.Search,
                height = 40.dp,
                textColor = Color.Gray,
                backgroundColor = Color.Transparent,
                surfaceColor = MaterialTheme.colorScheme.background.copy(0.85f),
            )
        }

        Row {
            FloatingActionButton(
                onClick = { onSearch(locName) },
                modifier = Modifier.size(50.dp),
                containerColor = MaterialTheme.colorScheme.background.copy(0.85f),
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(0.85f)
                )
            }

            Spacer(Modifier.width(8.dp))

            FloatingActionButton(
                onClick = { onClick() },
                modifier = Modifier.size(50.dp),
                containerColor = MaterialTheme.colorScheme.background.copy(0.85f),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    null,
                    tint = MaterialTheme.colorScheme.primary.copy(0.85f),
                )
            }
        }
    }
}
//抽屉上方功能按钮

@Composable
fun TopEndButton(tripList: MutableList<TripDetails>, storageManager: StorageManager, weekDate: String, onClick: () -> Unit, onMapMode: (Int) -> Unit, onRightSelect: (String) -> Unit) {
    val context = LocalContext.current

    var exp by remember { mutableStateOf(false) }
    //组件的状态。判断是开还是关
    val backupManager = remember { BackupManager(context) }

    val scope = rememberCoroutineScope()

    var menuDropdownMenu by remember { mutableStateOf(false) }

    // 导出处理程序
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        uri?.let {
            scope.launch(Dispatchers.IO) {
                backupManager.exportData(tripList, it)
            }
        }
    }

    // 导入处理程序
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            scope.launch(Dispatchers.IO) {
                val importedList = backupManager.importData(it)
                if (importedList != null) {
                    launch(Dispatchers.Main) {
                        tripList.clear()
                        tripList.addAll(importedList)
                        storageManager.saveTripData(tripList)
                    }
                }
            }
        }
    }

    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(top = 45.dp, end = 20.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Row(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
            //wrapContentSize限定区块大小仅为我的区块大小

            Box{
                FloatingActionButton(
                    onClick = { menuDropdownMenu = true },
                    modifier = Modifier
                        .padding(top = (1.5).dp)
                        .size(45.dp),
                    containerColor = MaterialTheme.colorScheme.background.copy(0.85f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(0.85f)
                    )
                }

                DropdownMenu(
                    expanded = menuDropdownMenu,
                    modifier = Modifier.width(82.dp),
                    onDismissRequest = { menuDropdownMenu = false },
                    shape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.background.copy(0.85f),
                    offset = DpOffset(x = 0.dp, y = 8.dp)
                ) {
                    val mapModeList = listOf("标准地图", "夜间地图", "公共交通", "车载地图")

                    DropdownMenuItem(
                        text = {
                            Text("重置地图", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        },
                        onClick = {
                            menuDropdownMenu = false
                            onClick()
                        }
                    )

                    mapModeList.forEach {
                        DropdownMenuItem(
                            text = {
                                Text(it, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            },
                            onClick = {
                                menuDropdownMenu = false
                                onMapMode(mapModeList.indexOf(it))
                            }
                        )
                    }

                    DropdownMenuItem(
                        text = {
                            Text("导出日程", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        },
                        onClick = {
                            menuDropdownMenu = false
                            exportLauncher.launch("TripBackup_${System.currentTimeMillis()}.zip")
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text("导入日程", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        },
                        onClick = {
                            menuDropdownMenu = false
                            importLauncher.launch(arrayOf("application/zip"))
                        }
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Surface(
                //可以有点击操作
                onClick = { exp = true },
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 6.dp,
                color = MaterialTheme.colorScheme.background.copy(0.85f)
            ) {
                Row(
                    //日期和箭头显示
                    modifier = Modifier
                        .width(80.dp)
                        .padding(
                            horizontal = 16.dp,//水平间距
                            vertical = 10.dp//竖直间距
                        ),
                    verticalAlignment = Alignment.CenterVertically
                    //竖直居中对齐
                ) {
                    Text(
                        //日期显示
                        text = weekDate,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary.copy(0.85f)
                    )
                    Spacer(Modifier.width(4.dp))
                    //文字与箭头间隔
                    Icon(
                        //箭头图标
                        imageVector = if (exp) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                DropdownMenu(
                    modifier = Modifier.width(80.dp),
                    expanded = exp,//通过exp控制菜单打开与关闭
                    onDismissRequest = { exp = false },//点击外面退出菜单
                    containerColor = MaterialTheme.colorScheme.background.copy(0.85f),
                    offset = DpOffset(x = 0.dp, y = 8.dp)//微调位置
                ) {
                    listOf("5号", "6号", "7号", "8号", "9号")
                        .forEach { d -> DropdownMenuItem(
                            text = {
                                Text(text = d,
                                    modifier = Modifier.padding(start = 5.dp),
                                    fontWeight = FontWeight.Bold,
                                    color = if (d == weekDate) MaterialTheme.colorScheme.primary.copy(0.85f)
                                    else MaterialTheme.colorScheme.secondary
                                )
                            },
                            onClick = { onRightSelect(d); exp = false })
                        }
                }
            }
        }
    }
}
//右上角菜单和日期切换按钮


fun calculateDuration(start: String, end: String): String {
    try {
        val s = start.split(":").map { it.toInt() }
        val e = end.split(":").map { it.toInt() }
        //先分割，再转成数字列表
        val diff = (e[0] * 60 + e[1]) - (s[0] * 60 + s[1])
        //计算差值
        if (diff < 0) return "出错"
        //隔天加一天
        val h = diff / 60
        val m = diff % 60
        return when {
            h > 0 && m > 0 -> "${h}h${m}m"
            h > 0 -> "${h}h"
            else -> "${m}m"
        }//输出
    } catch (_: Exception) { return "出错" } //遇到无法计算的防止报错
}
//计算时间差函数
