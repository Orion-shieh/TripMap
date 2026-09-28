package com.example.tripmap

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DoneOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ModeOfTravel
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amap.api.services.core.PoiItem

@Composable
fun CustomHeightTextField(value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector, height: Dp = 45.dp, modifier: Modifier = Modifier, textColor: Color, backgroundColor: Color = MaterialTheme.colorScheme.onSecondaryContainer, surfaceColor: Color = MaterialTheme.colorScheme.onSecondaryContainer) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        // 1. 这里硬性规定高度为 45.dp，绝不会变形
        modifier = modifier.height(height).clip(RoundedCornerShape(12.dp)).background(backgroundColor),
        singleLine = true,
        textStyle = TextStyle(
            fontSize = 16.sp,
            color = textColor // 你的输入文字颜色
        ),
        // 2. decorationBox 是自定义输入框外观的核心
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = surfaceColor, // 你的背景色
                        shape = RoundedCornerShape(12.dp) // 配合你的边框圆角
                    )
                    .padding(horizontal = 12.dp), // 左右文字留白
                verticalAlignment = Alignment.CenterVertically // 确保文字永远垂直居中！
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    // 当没有输入内容时，显示提示文字 (相当于原来的 Label/Placeholder)
                    if (value.isEmpty()) {
                        Row {
                            Icon(icon, "", tint = Color.Gray)
                            Text(
                                text = label,
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    color = Color.Gray
                                )
                            )
                        }
                    }
                    // 这里是真正渲染输入文字和光标的地方
                    innerTextField()
                }
            }
        }
    )
}
// 自定义 Web/Figma 风格的阴影

@Composable
fun Modifier.customShadow(
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,   // 阴影颜色
    alpha: Float = 0.16f,          // 阴影透明度 (越小越浅)
    borderRadius: Dp = 12.dp,      // 圆角大小 (需与组件圆角一致)
    blurRadius: Dp = 16.dp,        // 模糊半径 (越大越发散)
    offsetX: Dp = 6.dp,           // X轴偏移 (正数向右，负数向左)
    offsetY: Dp = 6.dp            // Y轴偏移 (正数向下，负数向上)
) = this.drawBehind {
    val shadowColor = color.copy(alpha = alpha).toArgb()
    val transparent = color.copy(alpha = 0f).toArgb()

    this.drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = transparent
        // 调用底层 Paint 的设置阴影层方法
        frameworkPaint.setShadowLayer(
            blurRadius.toPx(),
            offsetX.toPx(),
            offsetY.toPx(),
            shadowColor
        )
        // 绘制出一个带阴影的圆角矩形背景
        canvas.drawRoundRect(
            0f,
            0f,
            this.size.width,
            this.size.height,
            borderRadius.toPx(),
            borderRadius.toPx(),
            paint
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripEditorScreen(mapTool: MapTool, tripDetails: TripDetails, onDismiss: () -> Unit, onSave: (TripDetails) -> Unit){

    val scaffoldState = rememberBottomSheetScaffoldState(rememberStandardBottomSheetState(skipHiddenState = false))

    val context = LocalContext.current
    var poiList by remember { mutableStateOf<List<PoiItem>?>(null) }

    var newTripDetails by remember { mutableStateOf(tripDetails) }


    var weekDatePicker by remember { mutableStateOf(false) }
    var timePicker by remember { mutableStateOf(false) }
    //时间选择开关控制
    var fromPicker by remember { mutableStateOf(false) }
    var toPicker by remember { mutableStateOf(false) }

    var isStart by remember { mutableStateOf(true) }
    //是在选“开始时间”还是“结束时间”
    var isFrom by remember { mutableStateOf(true) }
    val timeState = rememberTimePickerState(is24Hour = true)
    //24小时制

    var fromName by remember { mutableStateOf(tripDetails.fromLoc.name) }
    var fromLon by remember { mutableDoubleStateOf(tripDetails.fromLoc.lng) }
    var fromLat by remember { mutableDoubleStateOf(tripDetails.fromLoc.lat) }
    var toName by remember { mutableStateOf(tripDetails.toLoc.name) }
    var toLon by remember { mutableDoubleStateOf(tripDetails.toLoc.lng) }
    var toLat by remember { mutableDoubleStateOf(tripDetails.toLoc.lat) }
    var toMethod by remember { mutableIntStateOf(tripDetails.toLoc.method) }
    var trafficTime by remember { mutableStateOf(tripDetails.trafficTime) }



    var menuView by remember { mutableStateOf(false) }
    val methodList = remember { listOf("公共交通", "乘车", "步行", "骑行", "未定") }
    val iconList = listOf(
        Icons.Default.DirectionsBus,
        Icons.Default.DirectionsCar,
        Icons.AutoMirrored.Filled.DirectionsWalk,
        Icons.AutoMirrored.Filled.DirectionsBike,
    )

    if (timePicker) {
        //时间选择
        AlertDialog(
            onDismissRequest = { timePicker = false },
            //点击外侧退出
            confirmButton = {
                TextButton(
                    onClick = {
                        val t = timeState.hour.toString().padStart(2, '0') +
                                ":" + timeState.minute.toString().padStart(2, '0')
                        //padStart(2, '0')确保时间格式始终是"06:05"，而不是"6:5"
                        newTripDetails = if (isStart) {
                            newTripDetails.copy(startTime = t)
                        } else {
                            newTripDetails.copy(endTime = t)
                        }

                        timePicker = false
                    }
                ) {
                    Text(
                        text = "确定",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                }
            },
            text = { TimePicker(state = timeState) }
            //唤起时间选择器
        )
    }

    BackHandler {
        onDismiss() // 只执行关闭编辑器的操作
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = screenHeights.heigh,
            sheetShape = RectangleShape,
            sheetContainerColor = Color.Transparent,
            sheetShadowElevation = 0.dp,
            sheetDragHandle = {null},
            sheetSwipeEnabled = false,
            sheetContent = {
                Surface(//整个页面，包括所有组件，除了按键
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeights.heigh)
                        .shadow(4.dp, RoundedCornerShape(28.dp))
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f), RoundedCornerShape(28.dp)),
                    color = Color.Transparent
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
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState())//允许上下滑动
                    ) {
                        OutlinedTextField(
                            //行程名的输入框
                            value = newTripDetails.title,
                            onValueChange = { newTripDetails = newTripDetails.copy(title = it) },
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(top = 20.dp)
                                .customShadow(blurRadius = 16.dp),
                            label = { Text("行程标题") },
                            textStyle = TextStyle(//输入框中的文字样式
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedLabelColor = Color.Gray,
                                //未聚焦时标签的颜色
                                unfocusedContainerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                focusedContainerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                focusedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        )


                        Spacer(Modifier.height(24.dp))

                        Row (
                            //时间输入框和行程方式的左右排列需要
                            modifier = Modifier.padding(horizontal = 16.dp).height(60.dp),
                        ){
                            Card (//日期时间卡片
                                modifier = Modifier
                                    .width(250.dp)
                                    .height(45.dp)
                                    .padding(end = 15.dp)
                                    .customShadow(borderRadius = 12.dp),
                                onClick = {weekDatePicker = true},
                                shape = RoundedCornerShape(12.dp),
                            ){
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.onSecondaryContainer)
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer, // 使用刚才讨论过的 Outline 颜色
                                            shape = RoundedCornerShape(12.dp)          // 设置圆角，需与裁剪一致
                                        ),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                ){
                                    Row(
                                        Modifier
                                            .height(40.dp)
                                            .align(Alignment.CenterVertically)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable{
                                                weekDatePicker = true
                                            }
                                            .padding(top = 8.dp, start = 8.dp, end = 6.dp)
                                    ){
                                        Icon(
                                            Icons.Default.CalendarToday,
                                            null,
                                            tint = Color.Gray,
                                            modifier = Modifier.padding(top = 4.dp).size(16.dp)
                                        )
                                        Text(text = " ${newTripDetails.date}", color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }


                                    Box(//中间竖线
                                        Modifier
                                            .width(1.5.dp)
                                            .height(16.dp)
                                            .fillMaxHeight()
                                            .background(MaterialTheme.colorScheme.onSecondary)
                                    )

                                    Row(
                                        Modifier
                                            .height(40.dp)
                                            .align(Alignment.CenterVertically)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable{
                                                isStart = true; timePicker = true
                                            }
                                            .padding(top = 8.dp, start = 6.dp, end = 6.dp)
                                    ){
                                        Icon(
                                            Icons.Default.AccessTimeFilled,
                                            null,
                                            tint = Color.Gray,
                                            modifier = Modifier.padding(top = 4.dp).size(16.dp)
                                        )
                                            Text(text = " ${newTripDetails.startTime}", color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }


                                    Box(//中间竖线
                                        Modifier
                                            .width(1.5.dp)
                                            .height(16.dp)
                                            .fillMaxHeight()
                                            .background(MaterialTheme.colorScheme.onSecondary)
                                    )

                                    Row(
                                        Modifier
                                            .height(40.dp)
                                            .align(Alignment.CenterVertically)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable{
                                                isStart = false; timePicker = true
                                            }
                                            .padding(top = 8.dp, start = 6.dp, end = 8.dp)
                                    ){
                                        Icon(
                                            Icons.Default.AccessTime,
                                            null,
                                            tint = Color.Gray,
                                            modifier = Modifier.padding(top = 4.dp).size(16.dp)
                                        )
                                            Text(text = " ${newTripDetails.endTime}", color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                }
                            }
                            DropdownMenu(//时间选择菜单
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(180.dp),
                                containerColor = MaterialTheme.colorScheme.background.copy(0.85f),
                                expanded = weekDatePicker,//通过exp控制菜单打开与关闭
                                onDismissRequest = { weekDatePicker = false },//点击外面退出菜单
                                offset = DpOffset(x = 0.dp, y = 4.dp)//微调位置
                            ) {
                                listOf("5号", "6号", "7号", "8号", "9号")
                                    .forEach { date -> DropdownMenuItem(
                                        text = {
                                            Text(text = date,
                                                modifier = Modifier.padding(start = 5.dp),
                                                fontWeight = FontWeight.Bold,
                                                color = if (date == newTripDetails.date) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.secondary
                                            )
                                        },
                                        onClick = { newTripDetails = newTripDetails.copy(date = date); weekDatePicker = false })
                                    }
                            }

                            if (toLat != 0.0){
                                val viewIcon = iconList.getOrNull(toMethod)?:Icons.Default.ModeOfTravel

                                FloatingActionButton (
                                    onClick = { menuView = true },
                                    modifier = Modifier
                                        .padding(start = 6.dp, bottom = 12.dp)
                                        .size(50.dp)
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.background, // 使用刚才讨论过的 Outline 颜色
                                            shape = RoundedCornerShape(12.dp)          // 设置圆角，需与裁剪一致
                                        )
                                        .customShadow(),
                                    elevation = FloatingActionButtonDefaults.elevation(
                                        defaultElevation = 0.dp,
                                    ),
                                    containerColor =  MaterialTheme.colorScheme.onSecondaryContainer,
                                ) {
                                    Icon(
                                        viewIcon,
                                        null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                DropdownMenu(//方式选择菜单
                                    modifier = Modifier
                                        .width(60.dp)
                                        .height(180.dp),
                                    containerColor = MaterialTheme.colorScheme.background.copy(0.85f),
                                    expanded = menuView,//通过exp控制菜单打开与关闭
                                    onDismissRequest = { menuView = false },//点击外面退出菜单
                                    offset = DpOffset(x = 250.dp, y = 4.dp)//微调位置
                                ) {
                                    methodList.forEach { method -> DropdownMenuItem(
                                        text = {
                                            Text(text = method,
                                                modifier = Modifier.padding(start = 5.dp),
                                                fontWeight = FontWeight.Bold,
                                                color = if (methodList.indexOf(method) == toMethod) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.secondary
                                            )
                                        },
                                        onClick = {
                                            toMethod = methodList.indexOf(method)
                                            if (fromLat != 0.0){
                                                mapTool.searchRoute(
                                                    startDetails = LocationDetails(fromName, fromLon, fromLat),
                                                    endDetails = LocationDetails(toName, toLon, toLat, toMethod),
                                                    method = toMethod,
                                                    { trafficTime = it }
                                                ){ }
                                            }
                                            menuView = false
                                        })
                                    }
                                }
                            }
                        }

                        Row(
                            //导航位置输入框的左右排列需要
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Box(
                                modifier = Modifier.weight(3f)
                            ){
                                CustomHeightTextField(
                                    value = fromName,
                                    onValueChange = { fromName = it },
                                    label = "起点", // 继续保留你的自定义阴影
                                    icon = Icons.Default.LocationOn,
                                    textColor = Color(0xFF3AC8FF),
                                    surfaceColor = Color(0x0DABE3FF),
                                    modifier = Modifier.customShadow(borderRadius = 12.dp, color = Color(0xFF3AC8FF))
                                )


                                DropdownMenu(//时间选择菜单
                                    modifier = Modifier
                                        .width(160.dp)
                                        .height(220.dp),
                                    containerColor = MaterialTheme.colorScheme.background.copy(0.85f),
                                    expanded = fromPicker,//通过exp控制菜单打开与关闭
                                    onDismissRequest = { fromPicker = false },//点击外面退出菜单
                                    offset = DpOffset(x = 0.dp, y = 4.dp)
                                ) {
                                    MapTool(context).poiSearch(fromName) { poiList = it }
                                    poiList?.forEach { poiItem ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = poiItem.title,
                                                    maxLines = 1,
                                                    modifier = Modifier
                                                        .padding(start = 5.dp)
                                                        .horizontalScroll(rememberScrollState()), // 👈 一行代码实现超长自动滚动,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            },
                                            onClick = {
                                                fromName = poiItem.title
                                                fromLon = poiItem.latLonPoint.longitude
                                                fromLat = poiItem.latLonPoint.latitude
                                                fromPicker = false
                                            })
                                    }
                                }
                            }

                            Column (
                                modifier = Modifier.weight(1f).customShadow(color = MaterialTheme.colorScheme.tertiary, offsetX = 0.dp, offsetY = 0.dp, blurRadius = 30.dp),
                            ){

                                Icon(//中间的纸飞机箭头图标
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary.copy(0.8f),
                                    modifier = Modifier.padding(start = 12.dp).size(20.dp).customShadow(color = MaterialTheme.colorScheme.tertiary)
                                )
                                Spacer(Modifier.height(16.dp))
                            }

                            Box(
                                modifier = Modifier.weight(3f)
                            ){
                                CustomHeightTextField(
                                    value = toName,
                                    onValueChange = { toName = it },
                                    label = "终点", // 继续保留你的自定义阴影
                                    icon = Icons.Default.LocationOn,
                                    textColor = Color(0xFFFFBB00),
                                    surfaceColor = Color(0x0DFFE5A5),
                                    modifier = Modifier.customShadow(borderRadius = 8.dp, color = Color(0xFFFFBB00)
                                    )
                                )

                                DropdownMenu(//时间选择菜单
                                    modifier = Modifier
                                        .width(160.dp)
                                        .height(220.dp),
                                    containerColor = MaterialTheme.colorScheme.background.copy(0.85f),
                                    expanded = toPicker,//通过exp控制菜单打开与关闭
                                    onDismissRequest = { toPicker = false },//点击外面退出菜单
                                    offset = DpOffset(x = 0.dp, y = 4.dp)
                                ) {
                                    MapTool(context).poiSearch(toName) { poiList = it }
                                    poiList?.forEach { poiItem ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = poiItem.title,
                                                    maxLines = 1,
                                                    modifier = Modifier
                                                        .padding(start = 5.dp)
                                                        .horizontalScroll(rememberScrollState()), // 👈 一行代码实现超长自动滚动,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            },
                                            onClick = {
                                                toName = poiItem.title
                                                toLon = poiItem.latLonPoint.longitude
                                                toLat = poiItem.latLonPoint.latitude
                                                toPicker = false
                                            }
                                        )
                                    }
                                }
                            }

                            IconButton(
                                modifier = Modifier.weight(1f).customShadow(color = MaterialTheme.colorScheme.tertiary, offsetX = 0.dp, offsetY = 0.dp, blurRadius = 30.dp),
                                onClick = {
                                    if (isFrom){
                                        fromPicker = true
                                        isFrom = false
                                    }else{
                                        toPicker = true
                                        isFrom = true
                                    }
                                },
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    null,
                                    tint = MaterialTheme.colorScheme.tertiary.copy(0.8f)
                                )
                            }
                        }

                        Text(
                            text = "* 终点按需填写，起点都可以不写",
                            color = Color.Gray,
                            style = TextStyle(
                                fontSize = 13.sp
                            ),
                            modifier = Modifier.padding(top = 6.dp, start = 28.dp)
                        )


                        Spacer(Modifier.height(18.dp))

                        OutlinedTextField(//备注详情输入框
                            newTripDetails.note,
                            { newTripDetails = newTripDetails.copy(note = it) },
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(240.dp)
                                .customShadow(blurRadius = 16.dp),
                            label = { Text("备注详情") },
                            textStyle = TextStyle(//输入框文字样式
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            //输入框文字颜色
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedLabelColor = Color.Gray,
                                //未聚焦时标签的颜色
                                unfocusedContainerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                focusedContainerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                unfocusedBorderColor = MaterialTheme.colorScheme.background,
                                focusedBorderColor = MaterialTheme.colorScheme.background,
                            )
                        )
                        Spacer(Modifier.height(screenHeights.low))
                    }

                }

            }
        ){

        }
        FloatingActionButton(
            //右下角保存图标
            onClick = {
                newTripDetails = newTripDetails.copy(
                    fromLoc = LocationDetails(fromName, fromLon, fromLat),
                    toLoc = LocationDetails(toName, toLon, toLat, toMethod),
                    trafficTime = trafficTime
                )
                if (newTripDetails.title.isNotBlank()) {
                    onSave(newTripDetails)
                }
                onDismiss()
            },
            //点击保存功能
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .navigationBarsPadding(),
            containerColor = MaterialTheme.colorScheme.primary.copy(0.85f)
        ) {
            Icon(
                Icons.Default.DoneOutline,
                null,
                tint = MaterialTheme.colorScheme.background.copy(0.85f)
            )
        }
    }
}
//日程编辑或添加界面
