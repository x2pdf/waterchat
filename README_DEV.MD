# README_DEV

## 1 环境
* 运行和构建项目环境 -- JDK8, maven3.5.4, IntelliJ IDEA(CE) 2021.3.3
* 在windows电脑构建可安装 .exe 文件时需要的环境 -- JDK17
* 以下文件需要您自行下载或者生成,因为github限制了文件大于100MB的文件上传（重要！！！）。
  1. src/main/resources/models/Qwen3-0.6B-GGUF/Qwen3-0.6B-Q8_0.gguf，
  需要您到链接 https://huggingface.co/Qwen/Qwen3-0.6B-GGUF/tree/main 下载Qwen3-0.6B-Q8_0.gguf ，
  然后放到src/main/resources/models/Qwen3-0.6B-Q8_0.gguf/  文件夹之下。
  2. intellij idea请不要使用太高的版本，比如如果使用 2025.3 版本打包应用时，会出现：Java FX Packager: Bundled ant not found. 的错误。 实测IntelliJ IDEA 2021.3.3 (Community Edition)也可以正常打包，再高的版本就自行测试是否可以打包了。开发者本人耗费了超过6个小时才通过安装更低版本的IntelliJ IDEA 2021.3.3才知道这个"坑"！"
  

## 2 构建
### intellij idea 构建项目的配置
![avatar](asset/markdown/artifact-structure.png)
![avatar](asset/markdown/add-dependency.png)
![avatar](asset/markdown/artifact-main-class.png)

注：
<br>1. 需要将 output layout 中绿色框框中的依赖全部添加到红色框框当中， 如果有特殊的依赖包或者文件可以点击黄色箭头的+按钮添加你的特殊的文件添加到输出的层中（补充：output layout
的设定是将项目所有依赖的包、文件或者其他的东西在打包输出氏一并输出到打包的目标文件当中，如果不再这里指定那么打包出来的文件在运行程序时将会找不到项目中需要的文件而导致程序运行异常）

<br>2. 如果本项目是在 Mac 上构建build，那么此时在 waterchat/out/artifacts/waterchat/bundles 目录下已经有可以直接在Mac安装使用的 waterchat-24.01.dmg 安装包了。


### 构建项目为 exe 可执行文件(windows)
* Step1:

![avatar](asset/markdown/build-step-1.png)
![avatar](asset/markdown/build-step-2.png)

* Step2:

CMD 窗口执行(命令中的JDK17的路径需要更换为你的具体路径):
set JAVA_HOME="C:/Program Files/Java/jdk-17.0.1

总之要切换到java17的环境下进行后续操作（你也可以直接更改系统JAVA_HOME这些步骤（自行搜索）来切换到JAVA17）。

* Step3:
执行以下命令既可以将exe制作成可安装的exe（需要先安装 wix toolset（自行搜索如何安装，可能需要windos启用某些功能，可能会出现windows缺乏redit..库文件之类的问题，总之自行搜索解决））: <br>
在 waterchat/out/artifacts/waterchat/bundles 目录下开启命令行窗口（要是java17的环境）执行以下命令 <br>
jpackage -n waterchat --app-image ./waterchat --vendor waterchat --app-version 24.01 --icon ./waterchat/waterchat.ico --dest ./target/build-link-package --temp ./target/build-link-package/temp --win-dir-chooser --win-menu --win-menu-group waterchat --win-shortcut

（这里你可也可能会遇到问题，打包错误，打包出来的exe文件安装失败，安装好了运行却报错error invoking method之类的，总之自行搜索解决。）

* Step4:
编译生成的安装包的保存位置为：
  waterchat/out/artifacts/waterchat/bundles/target/build-link-package/
<br>然后如同往常安装 windows 软件一样安装 waterchat-24.01.exe 即可使用！

## 3 注意事项
* 请将用用安装在没有中文名称的路径中。
* 所有的配置只要英文，不要有中文，也不要有空格或者中文的标点符号。
* model_name_list 是都是英文逗号分隔的英文字符串，逗号与英文之间不要留有空格。
* model_path=models/Qwen3-0.6B-GGUF
的 model_path 中 models文件夹下的模型参数文件夹名称要和 model_name_list 保持一致。
* config/resourcefilepath.txt 文件中指定了内置模型需要用到的文件，需要将 models文件夹下的每一个模型参数文件都配置，
否则应用运行时会找不到运行模型需要的文件。

## 4 兼容的AI模型说明
*本应用应当都能够支持阿里巴巴通义千问 Qwen3 GGUF 类型的 AI 文本对话大模型。

*本应用理论上能兼容（加载运行）所有 GGUF 类型的 AI 文本对话大模型。

## 5 快速添加同类型模型（已有安装本应用在电脑）
* 1找到本应用在电脑上的waterchat文件夹
* 2在 waterchat/resoueces/models/ 创建你模型名称的文件夹，例如 Qwen3-1.7B-GGUF
* 3将你的模型的所有参数文件下载并放到waterchat/resoueces/models/Qwen3-1.7B-GGUF 文件夹里面
* 4更改 waterchat/resoueces/config/config.txt 里的 model_name_list 新增你的模型名称（英文逗号拼接，不要有空格）
* 5启动本应用，切换到help页面，选择切换模型为 Qwen3-1.7B-GGUF即可。