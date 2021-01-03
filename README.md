# harmony-view-binding
[码云同步仓库](https://gitee.com/jeffer_s/harmony-view-binding)
------

### 是什么？
    - view-binding for harmony
    - 鸿蒙应用开发view-binding插件，消除findComponentById模版代码
    - 无注解、编译期间生成Binding类文件

------

### 怎么用？

1. 在project根目录的build.gradle文件中引入view-binding的maven仓库地址和classpath
``` gradle
buildscript {
    repositories {
        maven {
            url 'https://mirrors.huaweicloud.com/repository/maven/'
        }
        maven {
            url 'https://developer.huawei.com/repo/'
        }

        jcenter()
        maven{
            url 'https://dl.bintray.com/eholee/maven'
        }
    }
    dependencies {
        classpath 'com.huawei.ohos:hap:2.4.0.1'
        // view-binding
        classpath 'com.eholee.plugin:view-binding:1.0.1'
    }
}
```
2. 在feature模块的build.gradle文件中引入view-binding插件
``` gradle   
    apply plugin: 'com.huawei.ohos.hap'
    apply plugin: 'com.eholee.plugin.view-binding'
    ohos {
        ...
    }
    viewBinding{
        enable true
    }

    dependencies {
        ...
    }
```
3. 执行gradle sync 即可自动生成ViewBinding类，生成目录在feature中的build/generated/source/viewBinding中，
       类的命名方法通过获得xml布局文件名后遵循大驼峰法（Upper Camel Case）并追加Binding后缀，如：MainAblityBinding
       
4. 在需要填充布局的地方使用
   主要是两个api：1. binding = AbilityMainBinding.parse(this); 2. binding.getRoot()
``` java     
public class MainAbilitySlice extends AbilitySlice {
    private AbilityMainBinding binding;
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        binding = AbilityMainBinding.parse(this);
        super.setUIContent(binding.getRoot());
        binding.textHelloworld.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                new ToastDialog(MainAbilitySlice.this).setText("click").show();
            }
        });
    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }
}
```

### 可选项
1. 提供设置根布局api 
    ``` java 
    parse(Context context, ComponentContainer parent, boolean attachToRoot) 
    ```
2. 支持feature模块view-binding功能的开启与关闭：
   feature中的build.gradle中设置
  ``` gradle 
        viewBinding{
            enable false 
            // false为关闭，插件将不会解析该feature所有的xml布局文件，
            //true为开启，插件将会解析该feature下所有的xml布局文件
        }
  ```
3. 支持针对单个xml布局文件开启与关闭view-binding功能
     默认是都开启，如需关闭，需在xml根节点中加入如下信息：
``` xml 
   xmlns:eholee="http://schemas.eholee.com/viewbinding"
   eholee:view_binding="false"
   示例：
   <?xml version="1.0" encoding="utf-8"?>
   <DirectionalLayout
        xmlns:ohos="http://schemas.huawei.com/res/ohos"
        xmlns:eholee="http://schemas.eholee.com/viewbinding"
        eholee:view_binding="false"
        ohos:height="match_parent"
        ohos:width="match_parent"
        ohos:background_element="$color:colorAppBackground"
        ohos:orientation="vertical">
        ...
    </DirectionalLayout>
```
------
### 请作者喝杯咖啡
![image](coffee.png)

#### 参考
1. Android ViewBinding
2. com.huawei.ohos:hap:2.4.0.1 插件api

#### LICENSE
[Apache License 2.0](https://github.com/Eholee/harmony-view-binding/blob/master/LICENSE)

