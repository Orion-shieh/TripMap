package com.example.tripmap

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.amap.api.services.core.PoiItem
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageDetailsScreen(image: ImageDetails, onRightClick:(location: LocationDetails) -> Unit, onDelete:() -> Unit, onDismiss: () -> Unit, onSave: (ImageDetails) -> Unit){
    //imageInitial = p[7]
    val scaffoldState = rememberBottomSheetScaffoldState(rememberStandardBottomSheetState(skipHiddenState = false))


    var isEditing by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var viewImagePath by remember { mutableStateOf<String?>(null) }

    BackHandler { onDismiss() }

    if (showConfirmDialog) {
        //弹出确认删除对话框
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },//点击对话框外部关闭
            title = { Text("确认删除") },
            text = { Text("确定要移除这段行程吗？") },
            confirmButton = {//删除按钮
                TextButton(onClick = onDelete) {
                    Text("删除", color = Color.Red)
                }
            },
            dismissButton = {//取消按钮
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (isEditing) {
        //点击编辑按钮
        ImageVerificationScreen(
            imagePath = image.path,
            note = image.note,
            location = image.location,
            ifEditCome = true,
            onDismiss = {isEditing = false}
        ){image ->
            onSave(image)
            //重新组合并保存
        }

    } else {
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
                sheetDragHandle = { null },
                sheetSwipeEnabled = false,
                sheetContent = {
                    Surface(
                        //整个页面
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(screenHeights.heigh)
                            .shadow(4.dp, RoundedCornerShape(28.dp))
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f), RoundedCornerShape(28.dp)),
                        color = Color.Transparent
                        ) {
                        Column {
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
                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 24.dp)
                                    .customShadow()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = onDismiss,
                                        ) {
                                            //左上角返回按钮绘制
                                            Icon(
                                                Icons.AutoMirrored.Filled.ArrowBack,
                                                null,
                                                tint = Color.Gray
                                            )
                                        }
                                        Row {
                                            //右上角编辑和删除按钮绘制
                                            IconButton(onClick = { isEditing = true }) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    null,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            IconButton(onClick = { showConfirmDialog = true }) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    null,
                                                    tint = Color.Gray
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(top = 100.dp)
                        ) {
                            Column(
                                Modifier
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp)
                            ) {
                                Card (
                                    modifier = Modifier.customShadow(
                                        borderRadius = 8.dp,
                                        color = Color(0xFF3EA4D2)
                                    )
                                ) {
                                    Text(//所选图片标题
                                        text = "所选图片",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .padding(start = 2.dp),
                                        fontSize = 20.sp
                                    )
                                }

                                Spacer(Modifier.height(18.dp))

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { image.path.let { viewImagePath = it } }
                                ) {
                                    AsyncImage(
                                        model = image.path,
                                        contentDescription = "所选行程照片",
                                        modifier = Modifier
                                            .fillMaxWidth(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                }

                                Spacer(Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Card (
                                        modifier = Modifier.customShadow(
                                            borderRadius = 8.dp,
                                            color = Color(0xFFEBB25F)
                                        ).padding(end = 12.dp)
                                    ) {
                                        Text(//所选图片标题
                                            text = "图片备注",
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                .padding(start = 2.dp),
                                            fontSize = 20.sp
                                        )
                                    }

                                    Spacer(Modifier.weight(1f))

                                    if (image.location.name.isNotBlank()) {
                                        Card(
                                            //赋予位置可点击操作
                                            modifier = Modifier
                                                .height(28.dp)
                                                .padding(end = 22.dp)
                                                .customShadow(
                                                    color = Color(0xFF49C6FF).copy(0.65f),
                                                    offsetX = 2.dp,
                                                    offsetY = 2.dp,
                                                    blurRadius = 50.dp
                                                ),
                                            onClick = { onRightClick(image.location) },
                                            ) {
                                            Row(
                                                //位置图标和文字的打包成行
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .padding(start = 6.dp, end = 6.dp)
                                                    .padding(vertical = 2.dp)
                                                    .clip(RoundedCornerShape(24.dp))
                                            ) {
                                                Icon(//位置图标
                                                    imageVector = Icons.Default.Place,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(//处于的位置的文字
                                                    text = image.location.name,
                                                    fontSize = 16.sp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.basicMarquee()
                                                )
                                            }
                                        }
                                    }

                                }
                                Spacer(Modifier.height(18.dp))


                                Card(
                                    modifier = Modifier.customShadow(color = Color(0xFFFFBB00).copy(0.65f), offsetX = 2.dp, offsetY = 2.dp, blurRadius = 50.dp),
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .padding(horizontal = 8.dp),
                                    ) {
                                        Text(//备注正文
                                            text = image.note.takeIf { it.isNotBlank() } ?: "无备注",
                                            color = MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 24.sp,//行高
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(screenHeights.low))
                            }
                        }
                    }
                    if (viewImagePath != null) {
                        ViewImage(viewImagePath) {
                            viewImagePath = null
                        }
                    }
                }
            ) {

            }
        }
    }
}
//查看图片信息界面

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageVerificationScreen(imagePath: String, note: String, location: LocationDetails, ifEditCome: Boolean, onDismiss: (imageDetails: String?) -> Unit, onSave: (image: ImageDetails) -> Unit){
    val context = LocalContext.current

    val scaffoldState = rememberBottomSheetScaffoldState(rememberStandardBottomSheetState(skipHiddenState = false))

    var poiList by remember { mutableStateOf<List<PoiItem>?>(null) }

    var locationPicker by remember { mutableStateOf(false) }

    var imageNote by remember { mutableStateOf(note) }
    var locationName by remember { mutableStateOf(location.name) }
    var locationLon by remember { mutableDoubleStateOf(location.lng) }
    var locationLat by remember { mutableDoubleStateOf(location.lat) }


    BackHandler {
        if (ifEditCome){
            onDismiss(null)
        }else{
            File(imagePath).delete()
            onDismiss(null)
        }
    }

    Box(//嵌套一个Box为了做上方留空
        modifier = Modifier.fillMaxSize(),
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
                Surface(
                    //整个页面
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeights.heigh)
                        .shadow(4.dp, RoundedCornerShape(28.dp))
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f), RoundedCornerShape(28.dp)),
                    color = Color.Transparent
                ) {
                    Column {
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
                        Card(
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .customShadow()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { onDismiss(imagePath) },

                                    ) {
                                    //左上角返回按钮绘制
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        null,
                                        tint = Color.Gray
                                    )
                                }
                            }
                        }

                        Column (
                            Modifier
                                .padding(horizontal = 32.dp).padding(top = 24.dp)
                                .verticalScroll(rememberScrollState())
                        ){
                            Card (
                                modifier = Modifier.customShadow(
                                    borderRadius = 8.dp,
                                    color = Color(0xFF3EA4D2)
                                )
                            ) {
                                Text(//所选图片标题
                                    text = "所选图片",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .padding(start = 2.dp),
                                    fontSize = 20.sp
                                )
                            }
                            Spacer(Modifier.height(14.dp))

                            AsyncImage(
                                model = imagePath,
                                contentDescription = "所选行程照片",
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .clip(RoundedCornerShape(12.dp))
                            )

                            Spacer(Modifier.height(24.dp))

                            Row (
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Card (
                                    modifier = Modifier.customShadow(
                                        borderRadius = 8.dp,
                                        color = Color(0xFFEBB25F)
                                    ).padding(end = 12.dp)
                                ) {
                                    Text(//所选图片标题
                                        text = "图片备注",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .padding(start = 2.dp),
                                        fontSize = 20.sp
                                    )
                                }
                                Row(
                                    modifier = Modifier.weight(3f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Card (Modifier.weight(3f).customShadow(borderRadius = 12.dp, color = Color(0xFF3AC8FF))){
                                        CustomHeightTextField(
                                            value = locationName,
                                            onValueChange = { locationName = it },
                                            label = "所在位置", // 继续保留你的自定义阴影
                                            icon = Icons.Default.LocationOn,
                                            textColor = MaterialTheme.colorScheme.primary,
                                            surfaceColor = Color(0x0DABE3FF),
                                        )
                                    }

                                    IconButton(
                                        modifier = Modifier.weight(1f),
                                        onClick = { locationPicker = true },
                                    ) {
                                        Icon(
                                            Icons.Default.Search,
                                            null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    DropdownMenu(//时间选择菜单
                                        modifier = Modifier
                                            .width(160.dp)
                                            .height(220.dp),
                                        containerColor = MaterialTheme.colorScheme.background.copy(0.85f),
                                        expanded = locationPicker,//通过exp控制菜单打开与关闭
                                        onDismissRequest = { locationPicker = false },//点击外面退出菜单
                                        offset = DpOffset(x = 0.dp, y = 4.dp)
                                    ) {
                                        MapTool(context).poiSearch(locationName){ poiList = it}
                                        poiList?.forEach { poiItem ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = poiItem.title,
                                                            maxLines = 1,
                                                            modifier = Modifier
                                                                .padding(start = 5.dp)
                                                                .horizontalScroll(
                                                                    rememberScrollState()
                                                                ), // 👈 一行代码实现超长自动滚动,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    },
                                                    onClick = {
                                                        locationName = poiItem.title
                                                        locationLon = poiItem.latLonPoint.longitude
                                                        locationLat = poiItem.latLonPoint.latitude
                                                        locationPicker = false
                                                    })
                                            }
                                    }
                                }
                            }
                            Spacer(Modifier.height(6.dp))

                            OutlinedTextField(//备注详情输入框
                                imageNote,
                                { imageNote = it },
                                Modifier
                                    .fillMaxWidth()
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
            }
        ){

        }
        FloatingActionButton(
            //右下角保存图标
            onClick = {
                onSave(ImageDetails(imagePath, imageNote, LocationDetails(locationName, locationLon, locationLat)))
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
//确认图片信息界面

@Composable
fun ViewImage(imagePath: String?, onDismiss: () -> Unit) {
    var scale by remember { mutableFloatStateOf(1f) }
    //放大倍数
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    //xy轴偏移量
    var rotation by remember { mutableFloatStateOf(0f) }
    //旋转角度

    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    var canOffsetWidth by remember { mutableFloatStateOf(0f) }
    var canOffsetHeight by remember { mutableFloatStateOf(0f) }
    //可偏移像素点

    var backgroundColor by remember { mutableStateOf(Color.Black) }
    val backgroundWhite = MaterialTheme.colorScheme.background

    BackHandler { onDismiss() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .onSizeChanged { containerSize = it }
            //手机尺寸
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                backgroundColor =
                    if (backgroundColor == Color.Black) backgroundWhite else Color.Black
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, offsetXY, zoom, _ ->
                    // 1. 处理缩放
                    scale = (scale * zoom).coerceIn(0.8f, 6f)

                    // 2. 计算边界限制
                    // 图片放大后的尺寸
                    if (rotation == 90f || rotation == 270f) {
                        canOffsetWidth = containerSize.width * scale * 0.3f
                        canOffsetHeight = containerSize.height * 0.6f
                    } else {
                        canOffsetWidth = containerSize.width * scale * 0.6f
                        canOffsetHeight = containerSize.height * 0.6f
                    }

                    offsetX =
                        (offsetX + offsetXY.x * 1f).coerceIn(-canOffsetWidth, canOffsetWidth)
                    offsetY =
                        (offsetY + offsetXY.y * 1f).coerceIn(-canOffsetHeight, canOffsetHeight)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imagePath,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offsetX
                    translationY = offsetY
                    rotationZ = rotation
                },
            contentScale = ContentScale.Fit
        )

        // 旋转按钮
        FloatingActionButton(
            onClick = { rotation = (rotation + 90f) % 360f },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .navigationBarsPadding(),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = "Rotate", tint = backgroundColor)
        }
    }
}
//图片全屏查看
