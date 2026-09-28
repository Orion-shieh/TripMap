package com.example.tripmap

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File


@OptIn(ExperimentalTextApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(mapTool: MapTool,tripDetails: TripDetails, onClick: (start: LocationDetails?, end: LocationDetails) -> Unit, onDismiss: () -> Unit, onDelete: () -> Unit, onUpdate: (TripDetails) -> Unit) {

    val scaffoldState = rememberBottomSheetScaffoldState(rememberStandardBottomSheetState(skipHiddenState = false))

    var isEditing by remember { mutableStateOf(false) }//编辑按钮是否被按
    var showConfirmDialog by remember { mutableStateOf(false) }//删除按钮是否被按


    var cameraCapture by remember { mutableStateOf(false) }
    var albumCapture by remember { mutableStateOf(false) }


    var imageSelectDetails by remember { mutableStateOf<ImageDetails?>(null) }
    //选中的图片的全部信息

    var viewImagePath by remember { mutableStateOf<String?>(null) }
    //全屏查看图片的地址

    BackHandler {
        onDismiss()
    }


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


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        if (isEditing) {
            //点击编辑按钮
            TripEditorScreen(
                mapTool = mapTool,
                tripDetails = tripDetails,//传入行程详情
                onDismiss = { isEditing = false },
            ){newTripDetails ->
                onUpdate(newTripDetails)
                isEditing = false
            }
        } else if (!imageSelectDetails?.path.isNullOrBlank()) {
            ImageDetailsScreen(
                image = imageSelectDetails!!,
                onDismiss = {
                    imageSelectDetails =
                        ImageDetails("", "", LocationDetails("", 0.0, 0.0))
                }, // 关闭时清空 path
                onRightClick = { it ->
                    imageSelectDetails =
                        ImageDetails("", "", LocationDetails("", 0.0, 0.0))
                    onClick(null, it)
                },
                onDelete = {
                    val newTripDetails = tripDetails.copy(
                        image = tripDetails.image.filter { it -> it != imageSelectDetails }
                    )
                    onUpdate(newTripDetails)
                    imageSelectDetails = null
                }
            ) { newImage ->
                val newTripDetails = tripDetails.copy(
                    image = tripDetails.image.map { it ->
                        if (it == imageSelectDetails) newImage else it
                    }.toMutableList()
                )
                onUpdate(newTripDetails)
                imageSelectDetails = null
            }
        } else if (viewImagePath != null) {
            ViewImage(viewImagePath) {
                viewImagePath = null
            }
        } else if (cameraCapture) {
            TripCameraCapture(tripDetails, { cameraCapture = false }) { newData ->
                cameraCapture = false
                onUpdate(newData)
            }
        } else if (albumCapture) {
            TripAlbumCapture(tripDetails, { albumCapture = false }) { newData ->
                albumCapture = false
                onUpdate(newData)
            }
        } else {
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
                                modifier = Modifier.padding(horizontal = 24.dp).customShadow()
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

                                    Text(//行程名
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        text = tripDetails.title,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(Modifier.height(5.dp))

                                    Text(//行程时间显示
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        text = "${tripDetails.date}   ${tripDetails.startTime} - ${tripDetails.endTime}",
                                        color = Color.Gray,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    if (tripDetails.fromLoc.name.isNotBlank() || tripDetails.toLoc.name.isNotBlank()) {
                                        Spacer(Modifier.height(6.dp))

                                        Card(
                                            modifier = Modifier
                                                .padding(horizontal = 12.dp)
                                                .height(26.dp)
                                                .clip(RoundedCornerShape(12.dp)),
                                            onClick = {
                                                onClick(
                                                    tripDetails.fromLoc,
                                                    tripDetails.toLoc
                                                )
                                            },
                                            colors = CardDefaults.cardColors(
                                                MaterialTheme.colorScheme.surface
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(//从哪到哪显示
                                                    text = " ${tripDetails.fromLoc.name} ",
                                                    fontSize = 15.sp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    maxLines = 1,
                                                    modifier = Modifier
                                                        .widthIn(max = 120.dp)
                                                        .clip(RoundedCornerShape(32.dp))
                                                        .basicMarquee()
                                                )

                                                Icon(//位置图标
                                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                        .padding(horizontal = 2.dp)
                                                )

                                                Text(//从哪到哪显示
                                                    text = " ${tripDetails.toLoc.name} ",
                                                    fontSize = 15.sp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    maxLines = 1,
                                                    modifier = Modifier
                                                        .widthIn(max = 120.dp)
                                                        .clip(RoundedCornerShape(32.dp))
                                                        .basicMarquee()
                                                )
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(12.dp))
                                }
                            }
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp).padding(top = 24.dp)
                            ) {
                                Card (
                                    modifier = Modifier.padding(start = 16.dp).customShadow(
                                        borderRadius = 8.dp,
                                        color = Color(0xFF3EA4D2)
                                    )
                                ){
                                    Text(//备注标题文字
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        text = "备注详情",
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontWeight = FontWeight.Bold
                                    )
                                }


                                Column(
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                        .fillMaxSize()
                                    //.background(MaterialTheme.colorScheme.surface)
                                    //.verticalScroll(rememberScrollState())
                                ) {
                                    LazyColumn {
                                        item {
                                            Card (
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.customShadow(
                                                    borderRadius = 8.dp,
                                                    color = Color(0xFF49C6FF).copy(0.85f),
                                                    offsetX = 2.dp,
                                                    offsetY = 2.dp
                                                )
                                            ){
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 16.dp)
                                                ) {
                                                    Text(//备注正文
                                                        text = tripDetails.note.takeIf { it.isNotBlank() }
                                                            ?: "无备注",
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        lineHeight = 24.sp,//行高
                                                        modifier = Modifier.padding(vertical = 8.dp)
                                                    )
                                                }
                                            }
                                            Spacer(Modifier.height(20.dp))
                                        }
                                        items(
                                            items = tripDetails.image,
                                            key = { it.path }) { imageDetail ->
                                            PictureItem(
                                                image = imageDetail,
                                                onLeftClick = { viewImagePath = it },
                                                onUpClick = { imageSelectDetails = it },
                                                onRightClick = {
                                                    onClick(null, it)
                                                }
                                            )
                                        }
                                        item { Spacer(Modifier.height(120.dp)) }
                                    }
                                }
                            }
                        }
                    }
                }
            ) {
            }
            FloatingActionButton(
                //拍照添加照片日程按钮
                onClick = { cameraCapture = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .navigationBarsPadding(),
                containerColor = MaterialTheme.colorScheme.primary.copy(0.85f)
            ) {
                Icon(
                    Icons.Default.AddAPhoto,
                    null,
                    tint = MaterialTheme.colorScheme.background.copy(0.85f)
                )
            }
            FloatingActionButton(
                //相册添加照片日程按钮
                onClick = { albumCapture = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 70.dp)
                    .padding(24.dp)
                    .navigationBarsPadding(),
                containerColor = MaterialTheme.colorScheme.primary.copy(0.85f)
            ) {
                Icon(
                    Icons.Default.AddPhotoAlternate,
                    null,
                    tint = MaterialTheme.colorScheme.background.copy(0.85f)
                )
            }
        }
    }
}
//日程详情查看

@OptIn(ExperimentalTextApi::class)
@Composable
fun PictureItem(image: ImageDetails, onLeftClick: (data: String) -> Unit, onUpClick: (image: ImageDetails) -> Unit, onRightClick: (location: LocationDetails) -> Unit) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Card(//图片展示
            modifier = Modifier
                .weight(3.2f)
                .padding(bottom = 8.dp),
            onClick = { onLeftClick(image.path) }
        ) {
            if (image.path.isNotBlank()) {
                    AsyncImage(
                        model = image.path,
                        contentDescription = "行程照片",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
            }else{
                Text("暂无照片", color = Color.Gray, modifier = Modifier.padding(8.dp))
            }
        }

        Spacer(Modifier.weight(0.15f))

        Column(//右侧备注和位置
            modifier = Modifier
                .weight(1.8f)
                .align(Alignment.Top)
            //.padding(end = 12.dp, bottom = 12.dp)

        ) {
            Card (
                onClick = { onUpClick(image) },
                modifier = Modifier.customShadow(color = Color(0xFFFFD04E).copy(0.65f), offsetX = 2.dp, offsetY = 2.dp, blurRadius = 50.dp)
            ){
                Text(
                    //右侧备注
                    text = image.note.takeIf { it.isNotBlank() } ?: "无备注" ,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp)
                        .padding(vertical = 2.dp),
                    fontSize = 14.sp,
                    maxLines = 7,
                    minLines = 2
                )
            }
            Spacer(Modifier.height(6.dp))

            if (image.location.name.isNotBlank()){
                Card(
                    //赋予位置可点击操作
                    onClick = { onRightClick(image.location) },
                    modifier = Modifier.height(24.dp).customShadow(color = Color(0xFFFFD04E).copy(0.65f), offsetX = 0.dp, offsetY = 0.dp, blurRadius = 50.dp)

                    ) {
                    Row(
                        //位置图标和文字的打包成行
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(start = 4.dp, end = 8.dp)
                    ) {
                        Icon(//位置图标
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(//处于的位置的文字
                            text = image.location.name,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.basicMarquee()
                        )
                    }
                }
            }
        }
    }
}
//行程详情的图片展示格式

@Composable
fun TripCameraCapture(tripDetails: TripDetails, onDismiss:() -> Unit, onSave: (tripDetails: TripDetails) -> Unit){
    val context = LocalContext.current//当前上下文环境，用于外部调用东西需要

    var ifSuccess by remember { mutableStateOf(false) }


    val file = remember {
        //提前创建一个内部路径文件用于存储图片
        File(context.filesDir,
            "trip_${System.currentTimeMillis()}.jpg")
    }
    val uri = remember {
        //将路径变为外部软件能用的uri
        FileProvider.getUriForFile(
            context,//上下文环境
            "${context.packageName}.fileProvider",
            file//刚创建的文件
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) {success ->
        //当相机关闭后，这部分代码会自动运行
        if (success) {
            ifSuccess = true
        }else {
            onDismiss() // 拍照失败或取消时，也要关掉开关
        }
    }
    if (ifSuccess){

        ImageVerificationScreen(
            imagePath = file.absolutePath,
            note = "",
            location = LocationDetails("",0.0,0.0),
            ifEditCome = false,
            onDismiss = { ifSuccess = false; if (it != null) { File(it).delete() }; onDismiss()}
        ) { imageDetails->
            //file.absolutePath相机拍摄的绝对路径
            ifSuccess = false
            val newTripDetails = tripDetails.copy(image = tripDetails.image + imageDetails)

            onSave(newTripDetails)
            //重新组合并保存
        }
    }

    LaunchedEffect(Unit) {
        cameraLauncher.launch(uri)
    }
}
//行程中拍摄图片备注

@Composable
fun TripAlbumCapture(tripDetails: TripDetails, onDismiss: () -> Unit, onSave: (TripDetails) -> Unit) {
    val context = LocalContext.current

    var ifSuccess by remember { mutableStateOf(false) }

    val file = remember { File(context.filesDir, "trip_album_${System.currentTimeMillis()}.jpg") }
    //使用remember锁定文件对象

    val pickerLauncher = rememberLauncherForActivityResult(
        //定义Launcher
        ActivityResultContracts.PickVisualMedia()
    ) { resultUri ->
        //当相册关闭后，这部分代码会自动运行
        if (resultUri != null) {
            try {
                //把相册选中的图，拷贝进上面remember锁定的file里
                context.contentResolver.openInputStream(resultUri)?.use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
                ifSuccess = true
            } catch (e: Exception) {
                e.printStackTrace()
                onDismiss()
            }
        } else {
            onDismiss()
        }
    }
    if (ifSuccess){

        ImageVerificationScreen(
            imagePath = file.absolutePath,
            note = "",
            location = LocationDetails("",0.0,0.0),
            ifEditCome = false,
            onDismiss = { ifSuccess = false; if (it != null) { File(it).delete() }; onDismiss()}
        ) { imageDetails->
            //file.absolutePath相机拍摄的绝对路径
            ifSuccess = false

            val newTripDetails = tripDetails.copy(image = tripDetails.image + imageDetails)

            onSave(newTripDetails)
            //重新组合并保存
        }
    }

    LaunchedEffect(Unit) {
        //LaunchedEffect确保只触发一次
        pickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}
//行程中相册图片备注
