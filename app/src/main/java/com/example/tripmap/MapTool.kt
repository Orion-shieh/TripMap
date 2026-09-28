package com.example.tripmap

import android.Manifest
import android.content.Context
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.NearMeDisabled
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.MapsInitializer
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.maps.model.PolylineOptions
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItem
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import com.amap.api.services.route.BusPathV2
import com.amap.api.services.route.BusRouteResultV2
import com.amap.api.services.route.DrivePathV2
import com.amap.api.services.route.DriveRouteResultV2
import com.amap.api.services.route.RidePath
import com.amap.api.services.route.RideRouteResultV2
import com.amap.api.services.route.RouteSearchV2
import com.amap.api.services.route.WalkPath
import com.amap.api.services.route.WalkRouteResultV2
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import android.graphics.Color as AndroidColor

class MapTool(private val context: Context, private val aMap: AMap? = null) {
    // 自动判断路线类型，并提取出能直接画到地图上的坐标集合
    fun extractPolyline(path: Any?): List<LatLng> {
        if (path == null) return emptyList()
        val points = mutableListOf<LatLng>()
        // 路线上所有坐标点的列表

        when (path) {
            is BusPathV2 -> path.steps.forEach { s ->
                s.walk?.steps?.forEach { w -> w.polyline?.forEach { points.add(LatLng(it.latitude, it.longitude)) } }
                s.busLines?.forEach { b -> b.polyline?.forEach { points.add(LatLng(it.latitude, it.longitude)) } }
            }

            is DrivePathV2 -> path.steps.forEach { s ->
                s.polyline?.forEach {
                    points.add(LatLng(it.latitude, it.longitude))
                }
            }

            is WalkPath -> path.steps.forEach { s -> s.polyline?.forEach { points.add(LatLng(it.latitude, it.longitude)) } }
            is RidePath -> path.steps.forEach { s -> s.polyline?.forEach { points.add(LatLng(it.latitude, it.longitude)) } }
        }
        return points
    }
    fun poiSearch(placeName: String, poiList: (List<PoiItem>) -> Unit) {
        val query = PoiSearch.Query(placeName, "", "020")
        query.pageSize = 20
        //搜索条件信息

        val poiSearch = PoiSearch(context, query)
        //实例化搜索类

        poiSearch.setOnPoiSearchListener(object : PoiSearch.OnPoiSearchListener {
            //结果监听，有结果时执行
            override fun onPoiSearched(result: PoiResult?, rCode: Int) {
                if (rCode == 1000 && result != null) {
                    //获取成功
                    poiList(result.pois)
                } else {
                    poiList(emptyList())
                    //获取失败，提供空列表
                }
            }
            override fun onPoiItemSearched(p0: PoiItem?, p1: Int) {}
        })

        poiSearch.searchPOIAsyn()
        //发起搜索，异步执行
    }

    fun poiAroundSearch(keyword: String, onClick: (PoiItem) -> Unit, poiList: (List<PoiItem>) -> Unit) {

        // 1. 第一步：先静默获取当前最新定位（不需要移动地图视角）
        getCurrentLocation(getToMyLocation = false) { location ->

            // 2. 判断定位是否成功
            if (location != null) {
                println("定位成功：${location.latitude}, ${location.longitude}，开始搜索周边：$keyword")

                // 3. 构建搜索条件
                val query = PoiSearch.Query(keyword, "", "")
                query.pageSize = 20
                query.extensions = "all" // 开启深度信息，拿评分和人均

                val poiSearch = PoiSearch(context, query)

                // 4. 将刚刚拿到的最新定位，设置为周边搜索的中心点
                val centerPoint = LatLonPoint(location.latitude, location.longitude)
                poiSearch.bound = PoiSearch.SearchBound(centerPoint, 20000) // 默认搜 20000 米内

                // 5. 设置搜索结果回调
                poiSearch.setOnPoiSearchListener(object : PoiSearch.OnPoiSearchListener {
                    override fun onPoiSearched(result: PoiResult?, rCode: Int) {
                        if (rCode == 1000 && result != null) {
                            poiList(result.pois) // 搜索成功，返回列表
                            addPoiMarkersToMap(result.pois, onClick = { onClick(it) })
                        } else {
                            poiList(emptyList()) // 搜索失败，返回空列表
                        }
                    }
                    override fun onPoiItemSearched(p0: PoiItem?, p1: Int) {}
                })

                // 6. 发起搜索
                poiSearch.searchPOIAsyn()

            } else {
                // 定位失败：直接中断搜索，返回空列表
                println("获取当前位置失败，无法发起周边搜索")
                poiList(emptyList())
            }
        }
    }

    fun addHomePoiMarkers(tripList: List<TripDetails>) {
        if (aMap == null || tripList.isEmpty()) return

        // 如果地图还没加载完，先设置一个监听器
        aMap.addOnMapLoadedListener {
            executeBoundsMove(tripList)
        }

        // 如果已经加载过了，直接执行（建议把逻辑抽离出来）
        executeBoundsMove(tripList)
    }

    fun addPoiMarkersToMap(poiList: List<PoiItem>, onClick: (PoiItem) -> Unit) {
        if (aMap == null || poiList.isEmpty()) return

        // 1. 极其重要：清空地图上之前的旧标点和路线！
        aMap.clear()

        // 🌟 第一步：给地图设置监听器（给接线员留个电话）
        // 这个动作只需要做一次，所以写在 for 循环的外面
        aMap.setOnMarkerClickListener { clickedMarker ->
            // 当用户点击任意一个标点时，这段代码就会被触发！

            // 我们把当初贴在标点上的“小纸条”（PoiItem）取下来
            val data = clickedMarker.`object` as? PoiItem

            if (data != null) {
                onClick(data)

            }

            // 返回 true 表示“这个点击事件我拦截处理了，地图你别管了”
            true
        }


        // 用于计算所有点的边界，方便最后一步让地图自动缩放
        val boundsBuilder = LatLngBounds.Builder()

        // 2. 遍历你的 POI 列表
        for (poi in poiList) {
            val latLng = LatLng(poi.latLonPoint.latitude, poi.latLonPoint.longitude)

            // 组装大头针配置
            val markerOption = MarkerOptions()
                .position(latLng)
                .title(poi.title)
                .snippet(poi.snippet)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.place))

            // 🌟 第二步：添加标点，并把数据绑上去！
            // aMap.addMarker 会返回一个真实的 Marker 对象在地图上
            val marker = aMap.addMarker(markerOption)

            // 把当前这条 poi 数据，像贴纸条一样贴在这个 marker 身上
            // 这样上面监听器被点击的时候，才能取出来
            marker.`object` = poi

            // 把这个坐标加进边界计算器
            boundsBuilder.include(latLng)
        }

        // 3. 终极视觉优化：移动地图视角，把所有标点刚好装进屏幕！
        try {
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 200)
            aMap.animateCamera(cameraUpdate)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun executeBoundsMove(tripList: List<TripDetails>) {
        val boundsBuilder = LatLngBounds.Builder()
        var hasPoints = false

        for (trip in tripList) {
            if (trip.toLoc.lat != 0.0) {
                val latLng = LatLng(trip.toLoc.lat, trip.toLoc.lng)

                if (latLng == LatLng(23.11826, 113.309944)) {
                    val markerOption = MarkerOptions()
                        .position(latLng)
                        .title(trip.toLoc.name)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.hotal))
                    val marker = aMap?.addMarker(markerOption)
                    marker?.let { it.`object` = trip }
                } else {
                    val markerOption = MarkerOptions()
                        .position(latLng)
                        .title(trip.toLoc.name)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.poi))
                    val marker = aMap?.addMarker(markerOption)
                    marker?.let { it.`object` = trip }
                }

                boundsBuilder.include(latLng)
                hasPoints = true
            }
        }

        if (hasPoints) {
            try {
                // 使用 moveCamera 替代 animateCamera 测试是否有反应
                // 100 是 padding，如果数值太大也可能导致计算失败
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100)
                aMap?.animateCamera(cameraUpdate)
            } catch (e: Exception) {
                // 这里通常会抛出：Map size has not been determined
                e.printStackTrace()
            }
        }
    }

    fun addPoiLinesToMap(poiList: List<LocationDetails>) {
        if (aMap == null || poiList.isEmpty()) return

        aMap.clear()
        val boundsBuilder = LatLngBounds.Builder()

        // 💡 1. 定义计数器，记录需要搜索的路线总数
        val totalRoutes = if (poiList.size > 1) poiList.size - 1 else 0
        var completedRoutes = 0

        val poiIconList = listOf(
            R.drawable.poi_1, R.drawable.poi_2, R.drawable.poi_3, R.drawable.poi_4,
            R.drawable.poi_5, R.drawable.poi_6, R.drawable.poi_7, R.drawable.poi_8, R.drawable.poi_9,
        )

        val routeIconList = listOf(
            R.drawable.bus_route,
            R.drawable.drive_route,
            R.drawable.walk_route,
            R.drawable.ride_route,
        )

        // 先把所有 POI 点加进去（这是同步的，立刻生效）
        var poiNumber = 0
        for (poi in poiList) {
            val latLng = LatLng(poi.lat, poi.lng)
            boundsBuilder.include(latLng) // 包含 marker 点

            if (latLng == LatLng(23.11826,113.309944)){
                val markerOption = MarkerOptions()
                    .position(latLng)
                    .title(poi.name)
                    .snippet("酒店")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.hotal))
                aMap.addMarker(markerOption)
            } else {
                poiNumber += 1
                val markerOption = poiIconList.getOrNull(poiNumber - 1)?.let {
                    MarkerOptions()
                        .position(latLng)
                        .title(poi.name)
                        .snippet("第${poiNumber}个地点")
                        .icon(BitmapDescriptorFactory.fromResource(it))
                }?: MarkerOptions()
                    .position(latLng)
                    .title(poi.name)
                    .snippet("第${poiNumber}个地点")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.poi))
                aMap.addMarker(markerOption)
            }
        }


        // 如果只有一个点，直接缩放即可
        if (totalRoutes == 0) {
            try {
                // 这里的 150 是像素 padding
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 200)
                aMap.moveCamera(cameraUpdate)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return
        }

        // 💡 2. 遍历并搜索路线

        val lastPoi = LocationDetails("酒店", 113.309944,23.11826 )
        val currentPoi = poiList[0]

        searchPoiRoute(lastPoi, currentPoi, currentPoi.method) { routePoints ->
            // 这里是异步回调内部
            routePoints.forEach { latLng -> boundsBuilder.include(latLng) }

            val polylineOptions = PolylineOptions().apply {
                routeIconList.getOrNull(currentPoi.method)
                    ?.let {
                        customTexture = BitmapDescriptorFactory.fromResource(it)
                    }?:{
                    customTexture = BitmapDescriptorFactory.fromResource(R.drawable.route)
                }
                width(46f)//路线线条粗细
                isUseTexture = true
                lineJoinType(PolylineOptions.LineJoinType.LineJoinRound)
                lineCapType(PolylineOptions.LineCapType.LineCapRound)
                addAll(routePoints)
            }


            aMap.addPolyline(polylineOptions)

            completedRoutes++
        }




        for (i in 0 until poiList.size - 1) {
            val lastPoi = poiList[i]
            val currentPoi = poiList[i + 1]

            searchPoiRoute(lastPoi, currentPoi, currentPoi.method) { routePoints ->
                // 这里是异步回调内部
                routePoints.forEach { latLng -> boundsBuilder.include(latLng) }


                val polylineOptions = PolylineOptions().apply {
                    routeIconList.getOrNull(currentPoi.method)
                        ?.let {
                            customTexture = BitmapDescriptorFactory.fromResource(it)
                        }?:{
                        customTexture = BitmapDescriptorFactory.fromResource(R.drawable.route)
                    }
                    width(46f)//路线线条粗细
                    isUseTexture = true
                    lineJoinType(PolylineOptions.LineJoinType.LineJoinRound)
                    lineCapType(PolylineOptions.LineCapType.LineCapRound)
                    addAll(routePoints)
                }


                aMap.addPolyline(polylineOptions)

                // 💡 3. 每完成一条线，计数器加1，当全部完成后执行缩放
                completedRoutes++
                if (completedRoutes == totalRoutes) {
                    try {
                        // 这里的 150 是像素 padding
                        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 300)
                        aMap.animateCamera(cameraUpdate)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun searchPoiRoute(startDetails: LocationDetails, endDetails: LocationDetails, method: Int, onResult: (List<LatLng>) ->Unit){
        val routeSearch = RouteSearchV2(context)
        //初始化路线搜索对象RouteSearch

        val fromAndTo = RouteSearchV2.FromAndTo(
            LatLonPoint(startDetails.lat, startDetails.lng),
            LatLonPoint(endDetails.lat, endDetails.lng)
        )//把lat/lon转换为高德需要的LatLonPoint

        val driveQuery = RouteSearchV2.DriveRouteQuery(fromAndTo, RouteSearchV2.DrivingStrategy.DEFAULT, null, null, "")
        val walkQuery = RouteSearchV2.WalkRouteQuery(fromAndTo)
        val rideQuery = RouteSearchV2.RideRouteQuery(fromAndTo)

        driveQuery.showFields = RouteSearchV2.ShowFields.COST or
                RouteSearchV2.ShowFields.POLINE
        walkQuery.showFields = RouteSearchV2.ShowFields.COST or
                RouteSearchV2.ShowFields.POLINE
        rideQuery.showFields = RouteSearchV2.ShowFields.COST or
                RouteSearchV2.ShowFields.POLINE


        val listener = object: RouteSearchV2.OnRouteSearchListener {
            //监听回调
            override fun onBusRouteSearched(result: BusRouteResultV2?, err: Int) {
                if (err == 1000){
                    result?.paths?.getOrNull(0)?.let { onResult(extractPolyline(it)) }

                } else println("公交路线获取失败")

            }
            override fun onDriveRouteSearched(result: DriveRouteResultV2?, err: Int) {
                if (err == 1000){
                    result?.paths?.getOrNull(0)?.let { onResult(extractPolyline(it)) }

                } else println("驾车路线获取失败")
            }
            override fun onWalkRouteSearched(result: WalkRouteResultV2?, err: Int) {
                if (err == 1000){
                    result?.paths?.getOrNull(0)?.let { onResult(extractPolyline(it)) }

                } else println("步行路线获取失败")
            }
            override fun onRideRouteSearched(result: RideRouteResultV2?, err: Int) {
                if (err == 1000){
                    result?.paths?.getOrNull(0)?.let { onResult(extractPolyline(it)) }

                } else println("骑行路线获取失败")
            }
        }
        routeSearch.setRouteSearchListener(listener)

        @Suppress("DEPRECATION")
        when (method) {
            0 -> {
                val busQuery = RouteSearchV2.BusRouteQuery(fromAndTo, RouteSearchV2.BusMode.BUS_DEFAULT, "020", 0)
                busQuery.showFields = RouteSearchV2.ShowFields.COST or
                        RouteSearchV2.ShowFields.POLINE
                routeSearch.calculateBusRouteAsyn(busQuery)

            }//公交路线搜索

            1 -> {
                val driveQuery = RouteSearchV2.DriveRouteQuery(fromAndTo, RouteSearchV2.DrivingStrategy.DEFAULT, null, null, "")
                driveQuery.showFields = RouteSearchV2.ShowFields.COST or
                        RouteSearchV2.ShowFields.POLINE
                routeSearch.calculateDriveRouteAsyn(driveQuery)
            }//驾车路线搜索

            2 -> {
                val walkQuery = RouteSearchV2.WalkRouteQuery(fromAndTo)
                walkQuery.showFields = RouteSearchV2.ShowFields.COST or
                        RouteSearchV2.ShowFields.POLINE
                routeSearch.calculateWalkRouteAsyn(walkQuery)
            }//步行路线搜索

            3 -> {
                val rideQuery = RouteSearchV2.RideRouteQuery(fromAndTo)
                rideQuery.showFields = RouteSearchV2.ShowFields.COST or
                        RouteSearchV2.ShowFields.POLINE
                routeSearch.calculateRideRouteAsyn(rideQuery)
            }//骑车路线搜索

        }
    }

    fun getCurrentLocation(getToMyLocation: Boolean = false, onResult: (AMapLocation?) -> Unit) {
        try {
            val locationClient = AMapLocationClient(context)
            //初始化客户端

            val locationOption = AMapLocationClientOption().apply {
                //定位模式设置
                isOnceLocation = true
                locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                //isSensorEnable = true//方向传感器
            }
            locationClient.setLocationOption(locationOption)
            //提供定位模式给客户端

            locationClient.setLocationListener { location ->
                //监听器
                if (location != null && location.errorCode == 0) {
                    if (getToMyLocation){
                        aMap?.animateCamera(
                            CameraUpdateFactory.newLatLng(
                                LatLng(location.latitude, location.longitude)
                            )
                        )
                    }
                    onResult(location)//返回地址
                } else {
                    println("定位失败，错误码：${location?.errorCode}，原因：${location?.errorInfo}")
                    onResult(null)
                }

                locationClient.stopLocation()
                locationClient.onDestroy()
                //关闭引擎
            }

            locationClient.startLocation()
            //发起定位
        } catch (e: Exception) {
            //异常，打印堆栈信息。
            e.printStackTrace()
            onResult(null)
        }
    }

    fun drawRouteOnMap(endLoc: LocationDetails, points: List<LatLng>, ifView: Boolean = false) {
        if (ifView){
            aMap?.clear()
            aMap?.addMarker(
                MarkerOptions()
                    .position(LatLng(endLoc.lat, endLoc.lng))
                    .title(endLoc.name)
                    .snippet("目的地")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.place))
            )
        }

        //清空地图

        println("准备画线，坐标点数量：${points.size}")

        if (points.isEmpty()) return
        //没有点就不画

        val polylineOptions = PolylineOptions().apply {
            customTexture = BitmapDescriptorFactory.fromResource(R.drawable.route)
            width(46f)//路线线条粗细
            isUseTexture = true//让线条更平滑
            lineJoinType(PolylineOptions.LineJoinType.LineJoinRound)//线条拐弯处圆角
            lineCapType(PolylineOptions.LineCapType.LineCapRound)//线条端点为圆角
            addAll(points) //把所有的点加进去
        }

        aMap?.addPolyline(polylineOptions)//绘制线条
        if (ifView) viewRouteOnMap(points)//是否居中显示
    }

    fun viewRouteOnMap(points: List<LatLng>) {
        if (points.isEmpty()) return
        //空列表返回

        val boundsBuilder = LatLngBounds.Builder()
        //创建一个 LatLngBounds.Builder（经纬度边界建造器）
        points.forEach { boundsBuilder.include(it) }
        //遍历所有点，包含到边界建造器里（它会自动计算出能圈住所有点的最小外接矩形）

        val padding = 200//边界空白大小
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), padding)
        //地图镜头更新动作
        aMap?.animateCamera(cameraUpdate)
        //移动镜头
    }

    fun toLocation(locationDetails: LocationDetails, method: Int, onDismiss: (myLocation: LocationDetails) -> Unit, returnBusPath: (BusPathV2) -> Unit){
        getCurrentLocation { location ->
            //定位当前位置
            if (location != null){
                val startLoc = LocationDetails("myLocation", location.longitude, location.latitude)
                searchRoute(startLoc, locationDetails, method, {}){ returnBusPath(it) }
                //调用searchRoute
                onDismiss(startLoc)
            }
        }
    }

    fun searchRoute(startDetails: LocationDetails, endDetails: LocationDetails, method: Int, returnTime: (String) -> Unit, returnBusPath: (BusPathV2) -> Unit){
        val routeSearch = RouteSearchV2(context)
        //初始化路线搜索对象RouteSearch

        val fromAndTo = RouteSearchV2.FromAndTo(
            LatLonPoint(startDetails.lat, startDetails.lng),
            LatLonPoint(endDetails.lat, endDetails.lng)
        )//把lat/lon转换为高德需要的LatLonPoint

        val driveQuery = RouteSearchV2.DriveRouteQuery(fromAndTo, RouteSearchV2.DrivingStrategy.DEFAULT, null, null, "")
        val walkQuery = RouteSearchV2.WalkRouteQuery(fromAndTo)
        val rideQuery = RouteSearchV2.RideRouteQuery(fromAndTo)

        driveQuery.showFields = RouteSearchV2.ShowFields.COST or
                RouteSearchV2.ShowFields.POLINE
        walkQuery.showFields = RouteSearchV2.ShowFields.COST or
                RouteSearchV2.ShowFields.POLINE
        rideQuery.showFields = RouteSearchV2.ShowFields.COST or
                RouteSearchV2.ShowFields.POLINE


        val listener = object: RouteSearchV2.OnRouteSearchListener {
            //监听回调
            override fun onBusRouteSearched(result: BusRouteResultV2?, err: Int) {
                if (err == 1000){
                    val allDuration =result?.paths?.getOrNull(0)?.duration!!.toInt()
                    val hour = allDuration / 3600
                    val minute = allDuration / 60 % 60

                    returnTime(if (hour > 0) "${hour}h${minute}m" else "${minute}m")
                    returnBusPath(result.paths?.getOrNull(0) as BusPathV2)
                    result.paths?.getOrNull(0)?.let { drawRouteOnMap(endDetails, extractPolyline(it), true) }

                } else println("公交路线获取失败")

            }
            override fun onDriveRouteSearched(result: DriveRouteResultV2?, err: Int) {
                if (err == 1000){
                    val allDuration = result?.paths?.getOrNull(0)?.cost?.duration!!.toInt()
                    val hour = allDuration / 3600
                    val minute = allDuration / 60 % 60

                    returnTime(if (hour > 0) "${hour}h${minute}m" else "${minute}m")
                    result.paths?.getOrNull(0)?.let { drawRouteOnMap(endDetails, extractPolyline(it), true) }

                } else println("驾车路线获取失败")
            }
            override fun onWalkRouteSearched(result: WalkRouteResultV2?, err: Int) {
                if (err == 1000){
                    val allDuration =result?.paths?.getOrNull(0)?.duration!!.toInt()
                    val hour = allDuration / 3600
                    val minute = allDuration / 60 % 60

                    returnTime(if (hour > 0) "${hour}h${minute}m" else "${minute}m")
                    result.paths?.getOrNull(0)?.let { drawRouteOnMap(endDetails, extractPolyline(it), true) }

                } else println("步行路线获取失败")
            }
            override fun onRideRouteSearched(result: RideRouteResultV2?, err: Int) {
                if (err == 1000){
                    val allDuration =result?.paths?.getOrNull(0)?.duration!!.toInt()
                    val hour = allDuration / 3600
                    val minute = allDuration / 60 % 60

                    returnTime(if (hour > 0) "${hour}h${minute}m" else "${minute}m")
                    result.paths?.getOrNull(0)?.let { drawRouteOnMap(endDetails, extractPolyline(it), true) }

                } else println("骑行路线获取失败")
            }
        }
        routeSearch.setRouteSearchListener(listener)

        @Suppress("DEPRECATION")
        when (method) {
            0 -> {
                val busQuery = RouteSearchV2.BusRouteQuery(fromAndTo, RouteSearchV2.BusMode.BUS_DEFAULT, "020", 0)
                busQuery.showFields = RouteSearchV2.ShowFields.COST or
                        RouteSearchV2.ShowFields.POLINE
                routeSearch.calculateBusRouteAsyn(busQuery)

            }//公交路线搜索

            1 -> {
                val driveQuery = RouteSearchV2.DriveRouteQuery(fromAndTo, RouteSearchV2.DrivingStrategy.DEFAULT, null, null, "")
                driveQuery.showFields = RouteSearchV2.ShowFields.COST or
                        RouteSearchV2.ShowFields.POLINE
                routeSearch.calculateDriveRouteAsyn(driveQuery)
            }//驾车路线搜索

            2 -> {
                val walkQuery = RouteSearchV2.WalkRouteQuery(fromAndTo)
                walkQuery.showFields = RouteSearchV2.ShowFields.COST or
                        RouteSearchV2.ShowFields.POLINE
                routeSearch.calculateWalkRouteAsyn(walkQuery)
            }//步行路线搜索

            3 -> {
                val rideQuery = RouteSearchV2.RideRouteQuery(fromAndTo)
                rideQuery.showFields = RouteSearchV2.ShowFields.COST or
                        RouteSearchV2.ShowFields.POLINE
                routeSearch.calculateRideRouteAsyn(rideQuery)
            }//骑车路线搜索

        }
    }

    fun searchAllRoute(startDetails: LocationDetails, endDetails: LocationDetails, onResult: (AllRouteResult) -> Unit) {

        // 1. 准备参数 (V1 给公交用，V2 给其他用)
        val fromAndTo = RouteSearchV2.FromAndTo(LatLonPoint(startDetails.lat, startDetails.lng), LatLonPoint(endDetails.lat, endDetails.lng))

        val finalResult = AllRouteResult()
        var completedCount = 0
        val totalRequests = 4

        val busQuery = RouteSearchV2.BusRouteQuery(fromAndTo, RouteSearchV2.BusMode.BUS_DEFAULT, "020", 0)
        val driveQuery = RouteSearchV2.DriveRouteQuery(fromAndTo, RouteSearchV2.DrivingStrategy.DEFAULT, null, null, "")
        val walkQuery = RouteSearchV2.WalkRouteQuery(fromAndTo)
        val rideQuery = RouteSearchV2.RideRouteQuery(fromAndTo)

        busQuery.showFields = RouteSearchV2.ShowFields.COST or
                RouteSearchV2.ShowFields.POLINE
        driveQuery.showFields = RouteSearchV2.ShowFields.COST or
                RouteSearchV2.ShowFields.POLINE
        walkQuery.showFields = RouteSearchV2.ShowFields.COST or
                RouteSearchV2.ShowFields.POLINE
        rideQuery.showFields = RouteSearchV2.ShowFields.COST or
                RouteSearchV2.ShowFields.POLINE


        // 计数器：集齐 4 个结果就召唤神龙 (回调)
        fun checkDone() {
            completedCount++
            if (completedCount == totalRequests) {
                onResult(finalResult)
            }
        }

        // 2. 引擎实例化 (公交用V1，剩下的全上V2)
        val busSearch = RouteSearchV2(context)
        val driveSearch = RouteSearchV2(context)
        val walkSearch = RouteSearchV2(context)
        val rideSearch = RouteSearchV2(context)

        // 3. 监听器实例化

        // 【监听器 V2】—— 处理驾车、步行、骑行
        val listener = object : RouteSearchV2.OnRouteSearchListener {

            override fun onBusRouteSearched(result: BusRouteResultV2?, err: Int) {
                println("V1 bus 路线数量: " + result?.paths?.size)
                if (err == 1000) result?.paths?.let { finalResult.busPaths = it }
                checkDone()
            }
            override fun onDriveRouteSearched(result: DriveRouteResultV2?, err: Int) {
                println("V2 drive 路线数量: " + result?.paths?.size)
                if (err == 1000) result?.paths?.let { finalResult.drivePaths = it }
                checkDone()
            }
            override fun onWalkRouteSearched(result: WalkRouteResultV2?, err: Int) {
                println("walk 路线数量: " + result?.paths?.size)
                if (err == 1000) result?.paths?.let { finalResult.walkPaths = it }
                checkDone()
            }
            override fun onRideRouteSearched(result: RideRouteResultV2?, err: Int) {
                println("ride 路线数量: " + result?.paths?.size)
                if (err == 1000) result?.paths?.let { finalResult.ridePaths = it }
                checkDone()
            }
        }

        // 4. 绑定监听器 (各找各妈)
        busSearch.setRouteSearchListener(listener)
        driveSearch.setRouteSearchListener(listener)
        walkSearch.setRouteSearchListener(listener)
        rideSearch.setRouteSearchListener(listener)


        busSearch.calculateBusRouteAsyn(busQuery)
        driveSearch.calculateDriveRouteAsyn(driveQuery)
        walkSearch.calculateWalkRouteAsyn(walkQuery)
        rideSearch.calculateRideRouteAsyn(rideQuery)
    }

    fun clearMap(){
        aMap?.clear()
        getCurrentLocation { location ->
            println("检查拿到的位置：$location")
            location?.let {
                // 🎯 关键修改：把 animateCamera 改成 moveCamera ！！！
                aMap?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(it.latitude, it.longitude), // 这里你原本写了 location.longitude，改回 it 比较规范
                        15f
                    )
                )
            }
        }
    }
}
//地图工具包


@Composable
fun MapView(returnMap: (AMap) -> Unit) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapTool = remember { MapTool(context, aMap = mapView.map) }

    // 🗺️ 生命周期管理（保持不变）
    DisposableEffect(lifecycleOwner, mapView) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            mapView.onPause()
            mapView.onDestroy()
        }
    }

    // 👮‍♂️ 1. 老板准备一个“权限申请专员”
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions.getOrDefault(
            Manifest.permission.ACCESS_FINE_LOCATION, // 这里加上 android. 前缀
            false
        )
        if (fineLocationGranted) {
            // 🎉 2. 权限通过了！老板下令：工具箱，去给我拿位置！
            mapTool.getCurrentLocation(true) { location ->
                if (location != null) {
                    println("完美定位并居中！当前位置：${location.address}")
                }
            }
        }
    }

    // 🚀 3. 页面一打开，老板就立刻派人去要权限
    LaunchedEffect(Unit) {
        returnMap(mapView.map)

        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        mapTool.getCurrentLocation { location ->
            println("检查拿到的位置：$location")

            location?.let {
                // 🎯 关键修改：把 animateCamera 改成 moveCamera ！！！
                mapView.map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(it.latitude, it.longitude), // 这里你原本写了 location.longitude，改回 it 比较规范
                        16.5f
                    )
                )
            }
        }
    }


    // 🗺️ 4. 画地图（保持不变）
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            MapsInitializer.updatePrivacyShow(it, true, true)
            MapsInitializer.updatePrivacyAgree(it, true)
            val uiSettings = mapView.map.uiSettings

            uiSettings.logoPosition//高德logo位置AMapOptions.LOGO_POSITION_BOTTOM_LEFT
            uiSettings.zoomPosition//缩放按钮位置ZOOM_POSITION_RIGHT_BOTTOM
            uiSettings.isCompassEnabled//左上角指南针
            uiSettings.isTiltGesturesEnabled//双指倾斜（进入3D视角）
            uiSettings.isZoomControlsEnabled = false//缩放按钮显示
            uiSettings.isZoomGesturesEnabled//双指缩放
            uiSettings.isIndoorSwitchEnabled//室内地图楼层切换控件显示
            uiSettings.isScaleControlsEnabled//比例尺显示
            uiSettings.isScrollGesturesEnabled//单指滑动平移地图
            uiSettings.isRotateGesturesEnabled//双指旋转
            uiSettings.isGestureScaleByMapCenter//双指缩放时的中心锚点为地图中心
            uiSettings.isMyLocationButtonEnabled//右上角定位按钮


            val myLocationStyle = MyLocationStyle()
            myLocationStyle.strokeColor(AndroidColor.TRANSPARENT)
            myLocationStyle.radiusFillColor(AndroidColor.TRANSPARENT)
            myLocationStyle.strokeWidth(0f)
            myLocationStyle.interval(3000)//定位时间间隔，单位ms

            //myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW)//只定位一次。
            //myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE)//定位一次，且将视角移动到地图中心点。
            //myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW)//连续定位、且将视角移动到地图中心点，定位蓝点跟随设备移动。（1秒1次定位）
            //myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE)//连续定位、且将视角移动到地图中心点，地图依照设备方向旋转，定位点会跟随设备移动。（1秒1次定位）
            //myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE)//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）默认执行此种模式。

            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)//连续定位、蓝点不会移动到地图中心点，定位点依照设备方向旋转，并且蓝点会跟随设备移动。
            //myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER)//连续定位、蓝点不会移动到地图中心点，并且蓝点会跟随设备移动。
            //myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE_NO_CENTER)//连续定位、蓝点不会移动到地图中心点，地图依照设备方向旋转，并且蓝点会跟随设备移动。



            myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.my_location))//蓝点图标
            myLocationStyle.anchor(0.5f, 0.5f)//图标定位锚点中心

            mapView.map.myLocationStyle = myLocationStyle
            mapView.map.isMyLocationEnabled = true
            mapView.map.showIndoorMap(true)
            mapView
        },
    )
}
//地图显示函数

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseRoute(map: AMap, allRouteResult: AllRouteResult, onDismiss: (Boolean) -> Unit, returnBusPath: (BusPathV2) -> Unit, onResult: (AllRouteResult, Int) -> Unit){

    var ifCanHide by remember { mutableStateOf(false) }

    val scaffoldState = rememberBottomSheetScaffoldState(rememberStandardBottomSheetState(skipHiddenState = !ifCanHide))

    val isSheetHidden = scaffoldState.bottomSheetState.currentValue == SheetValue.Hidden

    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(pageCount = { 4 })

    var showGPS by remember { mutableStateOf(false) }

    BackHandler {
        onDismiss(showGPS)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = screenHeights.low,
            sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            sheetContainerColor = Color.Transparent,
            sheetShadowElevation = 0.dp,
            sheetDragHandle = {null},
            sheetContent = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ){
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                    ) {
                        RouteButton(pageIndex = pagerState.currentPage) {
                            scope.launch { pagerState.animateScrollToPage(it) }
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(screenHeights.medium)
                            .padding(top = 60.dp)
                            .shadow(4.dp, shape = RoundedCornerShape(28.dp))
                            .background(
                                color = MaterialTheme.colorScheme.background.copy(0.85f),
                                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                            )
                    ) {
                        Row {
                            Spacer(Modifier.weight(2f))
                            Surface(
                                modifier = Modifier
                                    .padding(vertical = 12.dp)
                                    .weight(1f),
                                color = Color.Gray,
                                shape = CircleShape
                            ) {
                                Box(Modifier.size(width = 40.dp, height = 4.dp))
                            }
                            Spacer(Modifier.weight(2f))
                        }

                        LaunchedEffect(ifCanHide) {
                            if (ifCanHide){
                                scope.launch { scaffoldState.bottomSheetState.hide() }
                            }
                        }

                        HorizontalPager(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Transparent),
                            state = pagerState,
                            verticalAlignment = Alignment.Top,
                            //userScrollEnabled = false
                        ) { pageIndex ->
                            //每一个抽屉页面
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .verticalScroll(rememberScrollState())
                            ) {
                                when(pageIndex){
                                    0 -> {
                                        allRouteResult.busPaths.forEach {
                                            BusRouteItem(it){route, mode ->
                                                returnBusPath(route)
                                                onResult(AllRouteResult(busPaths = listOf(route)), 0)
                                            }
                                        }
                                    }
                                    1 -> {
                                        allRouteResult.drivePaths.forEach {
                                            DriveRouteItem(it){route, mode ->
                                                onResult(AllRouteResult(drivePaths = listOf(route)), 1)
                                            }
                                        }
                                    }
                                    2 -> {
                                        allRouteResult.walkPaths.forEach {
                                            WalkRouteItem(it) { route, mode ->
                                                onResult(AllRouteResult(walkPaths = listOf(route)), 2)
                                            }
                                        }
                                    }
                                    3 -> {
                                        allRouteResult.ridePaths.forEach {
                                            RideRouteItem(it) { route, mode ->
                                                onResult(AllRouteResult(ridePaths = listOf(route)), 3)
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(75.dp))
                            }
                        }
                    }
                }
            }
        ) {

        }

        FloatingActionButton(
            onClick = {
                if (isSheetHidden){
                    scope.launch { scaffoldState.bottomSheetState.partialExpand() }
                    ifCanHide = false
                } else {
                    ifCanHide = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
                .navigationBarsPadding(),
            containerColor = MaterialTheme.colorScheme.background.copy(0.85f)
        ) { Icon(Icons.Default.Route, null, tint = MaterialTheme.colorScheme.primary.copy(0.85f)) }


        if (showGPS){
            FloatingActionButton(
                onClick = {
                    showGPS = false

                    val style = map.myLocationStyle ?: MyLocationStyle()

                    style.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)

                    map.myLocationStyle = style

                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
                    .padding(bottom = 70.dp)
                    .navigationBarsPadding(),
                containerColor = MaterialTheme.colorScheme.background.copy(0.85f)
            ) { Icon(Icons.Default.NearMeDisabled, null, tint = MaterialTheme.colorScheme.primary.copy(0.85f)) }

        } else {
            FloatingActionButton(
                onClick = {
                    showGPS = true
                    val style = map.myLocationStyle ?: MyLocationStyle()

                    style.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE)

                    map.myLocationStyle = style

                    map.moveCamera(CameraUpdateFactory.zoomTo(17f))

                    onDismiss(showGPS)
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
                    .padding(bottom = 70.dp)
                    .navigationBarsPadding(),
                containerColor = MaterialTheme.colorScheme.background.copy(0.85f)
            ) { Icon(Icons.Default.NearMe, null, tint = MaterialTheme.colorScheme.primary.copy(0.85f)) }

        }
    }
}


@Composable
fun RouteButton(pageIndex: Int, onClick: (pageIndex: Int) -> Unit) {
    val iconList = listOf(
        Icons.Default.DirectionsBus,
        Icons.Default.DirectionsCar,
        Icons.AutoMirrored.Filled.DirectionsWalk,
        Icons.AutoMirrored.Filled.DirectionsBike
    )

    // 使用 spacedBy 自动处理按钮之间的 13.dp 间距，取代你手动写的 padding
    Row(
        modifier = Modifier.padding(start = 26.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        iconList.forEachIndexed { index, icon ->
            val isSelected = index == pageIndex

            // 加上颜色过渡动画，滑动 Pager 时会有丝滑的颜色渐变效果
            val containerColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(0.85f) else MaterialTheme.colorScheme.background.copy(0.85f),
                label = "containerColor"
            )
            val iconColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.background.copy(0.85f) else MaterialTheme.colorScheme.primary.copy(0.85f),
                label = "iconColor"
            )

            FloatingActionButton(
                onClick = { onClick(index) },
                modifier = Modifier.size(50.dp),
                containerColor = containerColor,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor
                )
            }
        }
    }
}

@Composable
fun BusRouteItem(busPath: BusPathV2, onClick: (BusPathV2, Int) -> Unit) {
    //val allDistance = busPath.distance
    val allWalkDistance = busPath.walkDistance - busPath.walkDistance % 100
    val allDuration = busPath.duration
    val allCost = busPath.cost

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
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
            .fillMaxWidth()
            .background(Color.Transparent)
            .customShadow()
    ) {
        // --- 1. 头部概览信息 ---
        Card(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.background)
                .clickable(
                    onClick = { onClick(busPath, 0) }
                ),
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

                Spacer(modifier = Modifier.weight(1f))

                // 骑行标语
                Text(
                    text = "城市穿梭 🚌",
                    Modifier.padding(bottom = 2.dp),
                    style = TextStyle(
                        color = busColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth()
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
                                        text = "乘${step.busLines[0].passStationNum + 1}站" +
                                                "（${step.busLines[0].arrivalBusStation.busStationName}）  " +
                                                (step.busLines[0].firstBusTime?.let { timeFormat.format(it) + "-" }
                                                    ?: "") +
                                                (step.busLines[0].lastBusTime?.let { timeFormat.format(it) }
                                                    ?: ""),
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
                    }
                    Row (
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .padding(bottom = 8.dp)
                    ) {
                        Spacer(modifier = Modifier.weight(5f))
                        Box(Modifier.weight(1f)){
                            Text(
                                text = "￥${allCost}",
                                style = TextStyle(
                                    color = busColor
                                ),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DriveRouteItem(drivePath: DrivePathV2, onClick: (DrivePathV2, Int) -> Unit) {
    val allDistance = ((drivePath.distance/100).toInt()).toFloat()
    val allDuration = drivePath.cost.duration.toInt()
    val allTolls = drivePath.cost.tolls // 过路费
    val trafficLights = drivePath.cost.trafficLights // 红绿灯总数

    // 驾车专属的主题色（高德蓝）
    val driveColor = remember { Color(0xCC226EF2) }
    val viewIcon = remember { Icons.Default.DirectionsCar }

    @Composable
    fun DrawLine() {
        Box( // 这是中间分割线
            modifier = Modifier
                .width(32.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            Box( // 中间竖线
                Modifier
                    .width(1.5.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.onSecondary)
            )
            Box( // 中间圆点
                Modifier
                    .padding(top = 6.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(driveColor)
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.onTertiary,
                        CircleShape
                    )
            )
        }
    }


    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
            .fillMaxWidth()
            .customShadow()
    ){
        Card (
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.background)
                .clickable(
                    onClick = { onClick(drivePath, 1) }
                )
        ){
            // --- 1. 头部概览信息 ---
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
                    text = if (allTolls > 0) "收费 ￥${allTolls}" else "免费路段",
                    Modifier.padding(start = 12.dp, bottom = 2.dp),
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontSize = 14.sp
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                // 骑行标语
                Text(
                    text = "随心驰骋 🚙",
                    Modifier.padding(bottom = 2.dp),
                    style = TextStyle(
                        color = driveColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }


            // --- 2. 步骤时间轴 ---
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.height(IntrinsicSize.Min)
                ) {
                    DrawLine()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 0.dp, end = 26.dp, bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(driveColor)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Route,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "共${allDistance / 10.0}千米，${trafficLights}个红绿灯",
                                color = Color.White,
                                modifier = Modifier.padding(start = 4.dp),
                                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            )
                        }
                        Text(
                            text = "收费里程：${drivePath.cost.tollDistance.toInt()}米 ${drivePath.cost.tollRoad}",
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
                Row(
                    modifier = Modifier.height(IntrinsicSize.Min)
                ) {
                    DrawLine()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 0.dp, end = 26.dp, bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(driveColor)
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
                                text = drivePath.steps[0].instruction + "出发",
                                color = Color.White,
                                modifier = Modifier.padding(start = 4.dp),
                                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            )
                        }
                        Text(
                            text = drivePath.steps[1].instruction,
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

                // --- 3. 底部收尾虚线 ---
                Row(
                    modifier = Modifier.height(IntrinsicSize.Min)
                ) {
                    Box( // 这是中间分割线
                        modifier = Modifier
                            .width(32.dp)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box( // 中间竖线
                            Modifier
                                .width(1.5.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.onSecondary)
                        )
                    }
                    Column(
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun WalkRouteItem(walkPath: WalkPath, onClick: (WalkPath, Int) -> Unit) {
    val allDistance = walkPath.distance
    val allDuration = walkPath.duration

    // 步行专属的主题色（高德绿）
    val walkColor = remember { Color(0xCC009952) }
    //val viewIcon = remember { Icons.AutoMirrored.Filled.DirectionsWalk }

    @Composable
    fun DrawLine() {
        Box( // 这是中间分割线
            modifier = Modifier
                .width(32.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            Box( // 中间竖线
                Modifier
                    .width(1.5.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.onSecondary)
            )
            Box( // 中间圆点
                Modifier
                    .padding(top = 6.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary)
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.onTertiary,
                        CircleShape
                    )
            )
        }
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
            .fillMaxWidth()
            .customShadow()
    ) {
        Card(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.background)
                .clickable(
                    onClick = { onClick(walkPath, 2) }
                )
        ) {
            // --- 1. 头部概览信息 ---
            Row(
                modifier = Modifier.padding(all = 16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // 耗时格式化优化
                val hour = allDuration / 3600
                val minute = allDuration / 60 % 60
                val timeText = if (hour > 0) "${hour}小时${minute}分" else "${minute}分钟"

                Text(
                    text = timeText,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // 步行展示：总距离（因为步行距离较短，用“米”或“千米”视情况而定，这里统一转为千米保留一位小数更好看，或者直接写米）
                val distanceText = if (allDistance >= 1000) {
                    String.format("共 %.1f 千米", allDistance / 1000.0)
                } else {
                    "共 $allDistance 米"
                }

                Text(
                    text = distanceText,
                    Modifier.padding(start = 12.dp, bottom = 2.dp),
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontSize = 14.sp
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                // 步行通常不需要花费，这里可以用一句鼓励的话代替
                Text(
                    text = "低碳出行 \uD83C\uDF31",
                    Modifier.padding(bottom = 2.dp),
                    style = TextStyle(
                        color = walkColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            // --- 2. 步骤时间轴 ---
            // --- 2. 时间轴步骤 ---
            Column(modifier = Modifier.fillMaxWidth()) {
                // 第一步：展示总览图标
                Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                    DrawLine()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 26.dp, bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(walkColor)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Route,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "全程预计步数：${(allDistance * 1.3).toInt()}步",
                                color = Color.White,
                                modifier = Modifier.padding(start = 4.dp),
                                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            )
                        }
                        Text(
                            text = "绿色出行，低碳环保",
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 8.dp),
                            style = TextStyle(fontSize = 15.sp)
                        )
                    }
                }
                // 第二步：展示出发指令
                if (walkPath.steps.isNotEmpty()) {
                    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                        DrawLine()
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 26.dp, bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(walkColor)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.DirectionsWalk,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${walkPath.steps[0].instruction}出发",
                                    color = Color.White,
                                    modifier = Modifier.padding(start = 4.dp),
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                            Text(
                                text = "${walkPath.steps[1].instruction}",
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(start = 12.dp, top = 4.dp),
                                style = TextStyle(fontSize = 15.sp)
                            )
                        }
                    }
                }
                // 底部线
                Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .fillMaxHeight(), contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            Modifier
                                .width(1.5.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.onSecondary)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun RideRouteItem(ridePath: RidePath, onClick: (RidePath, Int) -> Unit) {
    val allDistance = ridePath.distance
    val allDuration = ridePath.duration

    // 骑行专属的主题色（共享单车青蓝色）
    val rideColor = remember { Color(0xCC01A6BE) }
    //val viewIcon = remember { Icons.AutoMirrored.Filled.DirectionsBike }

    @Composable
    fun DrawLine() {
        Box( // 这是中间分割线
            modifier = Modifier
                .width(32.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            Box( // 中间竖线
                Modifier
                    .width(1.5.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.onSecondary)
            )
            Box( // 中间圆点
                Modifier
                    .padding(top = 6.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(rideColor)
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.onTertiary,
                        CircleShape
                    )
            )
        }
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
            .fillMaxWidth()
            .customShadow()
    ) {
        Card(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.background)
                .clickable(
                    onClick = { onClick(ridePath, 2) }
                )
        ) {
            // --- 1. 头部概览信息 ---
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

                // 骑行距离展示：智能切换米和千米
                val distanceText = if (allDistance >= 1000) {
                    String.format("共 %.1f 千米", allDistance / 1000.0)
                } else {
                    "共 $allDistance 米"
                }

                Text(
                    text = distanceText,
                    Modifier.padding(start = 12.dp, bottom = 2.dp),
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontSize = 14.sp
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                // 骑行标语
                Text(
                    text = "迎风而行 \uD83D\uDEB4",
                    Modifier.padding(bottom = 2.dp),
                    style = TextStyle(
                        color = rideColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            // --- 2. 步骤时间轴 ---
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                    DrawLine()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 26.dp, bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(rideColor)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Route,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "消耗约：${(allDistance / 1000 * 25).toInt()}千卡",
                                color = Color.White,
                                modifier = Modifier.padding(start = 4.dp),
                                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            )
                        }
                        Text(
                            text = "迎着微风，享受骑行",
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 8.dp),
                            style = TextStyle(fontSize = 15.sp)
                        )
                    }
                }
                if (ridePath.steps.isNotEmpty()) {
                    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                        DrawLine()
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 26.dp, bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(rideColor)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.DirectionsBike,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${ridePath.steps[0].instruction}出发",
                                    color = Color.White,
                                    modifier = Modifier.padding(start = 4.dp),
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                            Text(
                                text = ridePath.steps[1].instruction,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(start = 12.dp, top = 4.dp),
                                style = TextStyle(fontSize = 15.sp)
                            )
                        }
                    }
                }
                Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            Modifier
                                .width(1.5.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.onSecondary)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun AllPoiViewScreen(mapTool: MapTool, allList: List<TripDetails>){
    val poiList = remember { mutableListOf<LocationDetails>() }
    allList.forEach {
        if (it.toLoc.lat != 0.0){
            poiList.add(it.toLoc)
        }
    }

    if (poiList.isNotEmpty()){
        mapTool.addPoiLinesToMap(poiList)
    }

}
