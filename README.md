# Android 10的ScopedStorage(分区存储)的介绍

.

### Scoped Storage的由来


> Android长久以来都支持外置存储空间这个功能，也就是我们常说的SD卡存储。这个功能使用得极其广泛，几乎所以开发者在开发的时都喜欢在SD卡的根目录下建立一个自己应用的专属的目录，用来存放各类文件和数据。导致SD卡的文件管理变的异常混乱。而且用户即使我卸载了一个完全不再使用的程序，它所产生的垃圾文件却可能会一直保留在我的手机上，不会被自动删除，这就使用户的存储空间一直处于十分紧张的状态，还浪费了大量的存储资源。
>
> 为了解决上述问题，Google 在Android 10当中加入了Scoped Storage分区存储机制。

.

.

### 简介

.

**介绍：**

> 为了让用户更好地管理自己的文件并减少混乱，以 Android 10（API 级别 29）及更高版本为目标平台的应用在默认情况下被赋予了对外部存储空间的分区访问权限（即分区存储）。此类应用只能看到本应用专有的目录（通过 `context.getExternalFilesDir()` 访问）以及本应用所创建的特定类型的媒体文件。如果你的应用不符合该条件的会以兼容模式运行，兼容模式跟以前一样，根据路径可以直接存储文件。但很有可能随着SDK的更新而无法使用，所以建议尽早完成Scoped Storage 的适配。

**说明：**
>**分区存储(Scoped Storage)机制，是一种安全机制，用于防止应用读取其他应用的数据；并具有以下特点：**
> 1. 每个应用程序都有自己的存储空间，即 [应用专属目录](https://developer.android.google.cn/training/data-storage/app-specific)
> 
> 2. 应用程序不能翻过自己的目录，去访问公共目录
> 
> 3. 应用程序请求的数据都要通过权限检测，不符合要求不会被放行
> 
> 4. 使用 [MediaStore](https://developer.android.google.cn/reference/android/provider/MediaStore) 相关API可以让你访问共享的存储空间

**注意：**

> 通过`android:requestLegacyExternalStorage="true"`设置兼容模式，在Android 11中以上配置依然有效，但仅限于`targetSdkVersion`小于或等于29的情况。如果你的`targetSdkVersion >=30`，Scoped Storage就会被强制启用，`android:requestLegacyExternalStorage="true"`标记将会被忽略。

.

.

### Scoped Storage权限的介绍

**介绍：**

> 默认情况下，对于`targetSdkVersion >= 29`的应用，其访问权限范围限定为Scoped Storage。此应用无需请求与存储相关的用户权限，即可以查看外部存储中以下类型的文件：
> 
>- **应用外部特定目录中的文件**（使用`getExternalFilesDir()`访问）
>
>- **应用自己创建的照片、视频和音频**（通过`MediaStore`访问）
>
> **注：**
> ScopedStorage将影响在Android10系统首次安装启动、且`targetSdkVersion >=29`的应用。需要访问和共享外部存储文件的应用会受到影响，需要进行兼容性适配。

**影响范围：**

在Android 10及以上设备运行的应用。
>**注：**
>
>- `targetSdkVersion <= 28`，不受影响
>- `targetSdkVersion >= 29`，默认情况应用外部存储可见性将被过滤，应用需要对分区存储进行适配


**下面是关于ScopedStorage权限及访问方式：**

| 存储位置 | 访问应用自己生成的文件 | 访问其他应用生成的的文件 | 访问方式 | 卸载应用是否删除文件
| -- | -- | -- | -- | -- |
| 共享存储空间 | 无需权限 | 需要权限`READ_EXTERNAL_STORAGE` | `MediaStore` Api  | 否
| `Downloads`目录 | 无需权限 | 无需权限 | 通过存储访问框架SAF，加载系统文件选择器 | 否
| 应用外部专属目录 | 无需权限 | 无法直接访问 | `getExternalFilesDir()`获取到属于应用自己专属的文件路径  | 是

.

.

### Scoped Storage 相关API的介绍

.

**1. 应用专属文件介绍 :**

> **应用专属文件存储分为两类，如下所示：**
> 
>- [**内部存储空间目录**](https://developer.android.google.cn/training/data-storage/app-specific#java)
>> **说明：**
>> 这些目录既包括用于存储持久性文件的专属位置，也包括用于存储缓存数据的其他位置。系统会阻止其他应用访问这些位置，并且在 Android 10（API 级别 29）及更高版本中，系统会对这些位置进行加密。
>**使用场景：** 非常适合存储只有应用本身才能访问的敏感数据
>>
>> **API :**
>> 通过 `context.getFilesDir()` 方法可以获取内部存储空间的访问路径。
>- [**外部存储空间目录**](https://developer.android.google.cn/training/data-storage/app-specific#java)
>> **说明：**
>> 这些目录既包括用于存储持久性文件的专属位置，也包括用于存储缓存数据的其他位置。虽然其他应用可以在具有适当权限的情况下访问这些目录，但存储在这些目录中的文件仅供您的应用使用。如果您明确打算创建其他应用能够访问的文件，您的应用应改为将这些文件存储在外部存储空间的[共享存储空间](https://developer.android.google.cn/training/data-storage/shared)部分。
>> 
>> **API :**
>> 通过 `context.getExternalFilesDir()` 方法可以获取外部存储空间的访问路径。
>
> **注意：**
> 当用户卸载应用，系统会移除保存在应用专属存储空间中的文件，如果你希望用户卸载应用后仍保留某些数据，可以使用[共享存储空间](https://developer.android.google.cn/training/data-storage/shared)来存储这部分数据。


.

**2. 共享存储空间介绍**

**介绍：**
> 为了提供更丰富的用户体验，许多应用允许用户提供和访问位于外部存储卷上的媒体。框架提供经过优化的媒体集合索引，称为媒体库，使您可以更轻松地检索和更新这些媒体文件。即使您的应用已卸载，这些文件仍会保留在用户的设备上。
>
>- 可以通过  `ContentResolver` 对象与媒体库抽象互动。

**Android 提供用于存储和访问以下类型的可共享数据的 API：**

>- [**媒体内容:**](https://developer.android.google.cn/training/data-storage/shared/media) 系统提供标准的公共目录来存储这些类型的文件，这样用户就可以将所有照片保存在一个公共位置，将所有音乐和音频文件保存在另一个公共位置，依此类推。您的应用可以使用此平台的 [`MediaStore`](https://developer.android.google.cn/reference/android/provider/MediaStore) API 访问此内容。
>> **MediaStore相关Api**
>>- **图片路径：** `MediaStore.Images.Media.EXTERNAL_CONTENT_URI`
>>- **视频路径：** `MediaStore.Video.Media.EXTERNAL_CONTENT_URI` 
>>- **音频文件路径：** `MediaStore.Audio.Media.EXTERNAL_CONTENT_URI`
>>- **下载的文件路径：** `MediaStore.Downloads.Media.EXTERNAL_CONTENT_URI` 
> 
>- [**文档和其他文件：**](https://developer.android.google.cn/training/data-storage/shared/documents-files) 系统有一个特殊目录，用于包含其他文件类型，例如 `PDF` 文档和采用 `EPUB` 格式的图书。您的应用可以使用此平台的[存储访问框架](https://developer.android.google.cn/guide/topics/providers/create-document-provider)访问这些文件。

.

**3. 管理设备上所有的文件**

**介绍：**
> Android 11中强制启用`Scoped Storage`是为了更好地保护用户的隐私，以及提供更加安全的数据保护。对于绝大部分应用程序来说，使用`MediaStore`提供的API就已经可以满足大家的开发需求了。
>
> 拥有对整个SD卡的读写权限，在Android 11上被认为是一种非常危险的权限，同时也可能会对用户的数据安全造成比较大的影响。对于这类危险程度比较高的权限，Google通常采用的做法是，使用Intent跳转到一个专门的授权页面，引导用户手动授权，比如悬浮窗，无障碍服务等。

**申请权限步骤：**

**1. 在`AndroidManifest.xml`注册权限**

```java
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE"
	        tools:ignore="ScopedStorage" />
```


**2. 在Activity 中跳转到授权页面**

```java
if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R ||Environment.isExternalStorageManager()) {

    Toast.makeText(this, "已获得访问所有文件权限", Toast.LENGTH_SHORT).show()

} else {
    val builder = AlertDialog.Builder(this)
        .setMessage("本程序需要您同意允许访问所有文件权限")
        .setPositiveButton("确定") { _, _ ->
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            startActivity(intent)
        }
    builder.show()
}
```

> **注意：**
> 即使我们获得了管理SD卡的权限，对于Android这个目录下的很多资源仍然是访问受限的，比如说Android/data这个目录在Android 11中使用任何手段都无法访问。



.

.

### 简单案例

**介绍：**
> 本项目实现作用域存储当中获取手机相册里的图片，并以列表的形式展示在界面上，点击图片跳转到图片预览界面；并把将App私有目录的图片添加到相册中。

.

**项目演示：**

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210226164805373.gif#pic_center)



**项目地址**
- [**码云地址**](https://gitee.com/qu-wenbin/scoped-storage-demo)
- [**Github地址**](https://github.com/Ou520/ScopedStorageDemo)

.

.

### 参考资料

- [**Android 存储用例和最佳做法**](https://developer.android.google.cn/training/data-storage/use-cases)
- [**数据和文件存储概览**](https://developer.android.google.cn/training/data-storage)
- [**访问应用专属文件**](https://developer.android.google.cn/training/data-storage/app-specific)
- [**共享存储空间概览**](https://developer.android.google.cn/training/data-storage/shared)
