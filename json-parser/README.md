此代码的主要功能是生成c++和python的json解析代码，生成的代码可以将json解析成对应语言的类实例。

---

##### 文件夹的说明
- cpp文件夹中存放的是生成c++代码的程序
- python文件夹中存放的是生成c++代码的程序
- output文件夹中存放的是生成的代码，其中c++需要的json环境文件已经放在里面
- conf文件夹中存放的是类配置文件样本

---

##### 类配置文件的说明
每个配置文件可以包含多个类，类之间可以相互包含（对应的是json包含）。对每一个类，第一行有三个字段，第一个字段是'#'，第二个字段是目标类名称，第三个字段n表示有多少个属性需要解析。接下来包含多少属性就会有多少行，每一行由6-7个描述子构成，描述子由\t隔开。描述子的形式如下：
```
name    type    necessary    array    default    class    mapped_name
```
每个字段的描述如下：
- name：json中成员名称
- type：变量数据类型，可以是int double string object
- necessary：此变量是否可省略，1表示不可省略，0表示可以省略
- array：此变量是否是数组，1表示是，0表示不是，2表示可能是（处理数组单个元素退化成基本类型的情况）
- default：默认值，当necessary=0时起作用
- class：此变量的复杂类型名称，只有当type=object时起作用
- mapped_name：此变量在类中对应的名称，作名称映射，省略时默认为name值

下面给出一个例子
```java
#	Ingredient	2
quantity	string	0	0	0	none
name	string	1	0	0	none
#	Instruction	3
image	string	0	0	unknown_default_value	none
description	string	1	0	0	none
son	string	0	2	0	0
#	Method	19
id	string	1	0	0	none
name	string	1	0	0	none
url	string	1	0	0	none
totalTime	string	1	0	0	none
image	string	1	0	none	none
difficulty	string	1	0	0	none
cookingMethod	string	1	2	0	none
flavor	string	1	2	0	none
viewCount	string	1	0	0	none
cookCount	string	1	0	0	none
majorIngredients#num#baidu	int	0	0	1	none
majorIngredients	object	1	2	[]	Ingredient
minorIngredients#num#baidu	int	0	0	1	none
minorIngredients	object	0	1	[]	Ingredient
instructions#num#baidu	int	1	0	0	none
instructions	object	1	1	0	Instruction
cookingTips	string	0	2	none	none
mipUrl	string	0	0	0	none
resImg	string	1	0	0	none
```

---

##### c++程序生成代码的说明
程序中有个全局变量**log_source**，负责控制打印日志和错误信息的情况。
现在可以取值为"std"和"xbot"，取值为"std"时，采用cout的方式输出日志和错误信息，可以在标准g++下编译运行；取值为"xbot"时，生成的代码为"xbot"上使用的日志WRITE_APP_DEBUG_LOG、warning_log）、错误信息(fatal_error)输出格式，本地不能运行。输出方法、格式也可以自行修改，只需要修改函数**log_factory**即可。

现在c++实例包含复杂类型的情况，默认存储指针。

c++程序的编译，请在output目录下执行：`g++ parser_generator.cpp jsoncpp.cpp`，然后执行`./a.out`。

---

##### 局限性
- 暂不支持数组嵌套的情况
- 暂不支持第一层就是数组的情况