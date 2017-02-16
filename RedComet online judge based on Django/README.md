##### 基于Django的在线评测系统

###### 实现功能

- 用户管理、注册、登入登出
- 题目管理，添加修改题目、测试用例
- 支持c、c++、java、python语言
- 提交记录管理
- 比赛功能（尚未实现）
- 论坛（有待实现）

###### 数据设计
- 用户数据设计如下

```java
class account(models.Model):
    #user_id = models.IntegerField(unique=True, primary_key=True, default=1)

    user_name = models.CharField(max_length=30, primary_key=True, unique=True) # primary_key=True

    nick_name = models.CharField(max_length=30, default='New user')

    password  = models.CharField(max_length=30)

    email = models.EmailField(default='')

    is_forbidden = models.BooleanField(default=False)

    create_time = models.DateTimeField(auto_now_add=True, null=True)

    problems_status = JSONField(default="{}",null=True, blank=True)

    #USERNAME_FIELD = 'user_name'

    #REQUIRED_FIELDS = []

    #objects = UserManager()

    def __unicode__(self):
        return self.user_name

    class Meta:
        db_table = "user"
```

- 题目数据设计如下

```java
class problem(models.Model):
    # require attention when adding new problem
    problem_id = models.IntegerField(primary_key=True, db_index=True)

    title = models.CharField(unique=True, max_length=50)

    content = models.TextField(max_length=10000)

    difficulty = models.IntegerField()

    example = models.TextField(blank=True)
	# 创建时间
    create_time = models.DateTimeField(auto_now_add=True)

    submission_number = models.IntegerField(default=0)

    accept_number = models.IntegerField(default=0)

    acceptance = models.FloatField(default=0)

    accept_user_number = models.IntegerField(default=0)
    # 提示
    hint = models.TextField(blank=True, null=True)
    # 是否可见 false的话相当于删除
    visible = models.BooleanField(default=True)

    # 时间限制 单位是毫秒
    time_limit = models.IntegerField()
    # 内存限制 单位是MB
    memory_limit = models.IntegerField()

    # 标签
    # tags = models.ManyToManyField(problemTag)

    def __unicode__(self):
        return self.title

    class Meta:
        db_table = "problem"
```

- 提交记录数据设计如下

```java
def rand_str(length=32):
    if length > 128:
        raise ValueError('length must <= 128')
    return hashlib.sha512(os.urandom(128)).hexdigest()[0:length]

class submission(models.Model):

    id = models.CharField(max_length=32, default=rand_str, primary_key=True, db_index=True)

    user_name = models.CharField(max_length=30, db_index=True)

    problem_id = models.IntegerField(db_index=True)

    title = models.CharField(max_length=50)

    create_time = models.DateTimeField(auto_now_add=True)

	# 判题所用时间
    time_used = models.IntegerField(blank=True, null=True)
    # c, c++, java, python
    language = models.CharField(max_length=6)
    # waiting, judging...
    res = models.IntegerField(default=result["Waiting"])
    # 存储判题结果，比如cpu时间内存之类的, should be json-type string
    memory_used = models.IntegerField(blank=True, null=True)

    # path = models.CharField(max_length=40, default="")

    class Meta:
        db_table = "submission"
        ordering = ['-create_time', 'problem_id']

    def __unicode__(self):
        return 'submission '+self.id
```

###### 判题
- 每道题的测试用例和正确结果以文件的形式存储
- 浏览器提交的任务使用FIFO队列管理
- 使用Python的进程池高效地处理每一个任务
- 程序编译通过python的Subprocess包的Popen方法调用相应的编译器完成
- 程序执行消耗的时间和空间计算通过基于c语言的python扩展包lorun获取






