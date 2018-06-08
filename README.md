# shinny-futures-android
一个开源的 android 平台期货行情交易终端</br>
## 预览
<p><img src="screenshot/主力合约.jpg" width="24%"/> <img src="screenshot/成交记录.jpg" width="24%"/> <img src="screenshot/信息.jpg" width="24%"/> <img src="screenshot/交易.jpg" width="24%"/></p>

## 功能架构
软件的主要功能：查看行情以及进行交易<br>
- 首页
  - 导航栏：完成各个交易所合约列表的切换以及不同页面的跳转
  - 合约列表显示：合约报价单显示，长按添加/删除自选
  - 账户：查看账户资金
  - 持仓：跳转到合约详情页
  - 成交：查看成交记录
  - 反馈：用户意见查看与反馈
  - 搜索：合约搜索
- 合约详情页
  - 当日：显示分时图
  - 日线：显示日Ｋ线
  - 小时：显示小时线
  - 5分钟：显示5分钟线
  - 设置：选择是否显示持仓线、挂单线、均线
  - 合约标题：展示自选合约列表，点击切换合约信息
  - 信息：显示合约盘口信息
  - 持仓：显示账户持仓列表
  - 委托：显示账户下单情况
  - 交易：三键下单板进行交易
## 代码架构
### 数据层面
根据websocket协议进行实时数据更新
- 利用OkHttp从服务器获取合约列表文件进行解析
- 利用java-websocket框架分别与行情和交易服务器进行连接，获取行情数据和期货公司列表数据
- 对服务器发过来的json数据进行解析
- 数据解析完毕后利用android广播机制进行行情数据以及交易数据的刷新
### UI层面
界面由activity、fragment以及adapter三个模块构成，分别负责作为容器、数据展示与交互以及数据绑定刷新
- 利用Support Library库中的RecyclerView实现合约报价列表、Drawlayout实现抽屉导航
- 利用MPAndroidChart框架完成Ｋ线图的绘制
- 利用dataBinding框架部分页面数据的绑定，简化代码
- 自定义下单软键盘
## 主要第三方库
- [Gson](https://github.com/google/gson)
- [EventBus](https://github.com/greenrobot/EventBus)
- [Okhttp](https://github.com/square/okhttp)
- [Java-Websockets](https://github.com/TooTallNate/Java-WebSocket)
- [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)
