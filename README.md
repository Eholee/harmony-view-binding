# harmony-view-binding

##### 是什么？
    - view-binding for harmony
    - 鸿蒙应用开发view-binding插件，消除findComponentById模版代码
    - 无注解、编译期间生成Binding类文件

##### 怎么用？
    step1.在project根目录的build.gradle文件中引入view-binding的maven仓库地址和classpath
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
            classpath 'com.eholee.plugin:view-binding:0.0.2-alpha'
        }
    }
    ```
    step2. 在feature模块的build.gradle文件中引入view-binding插件
