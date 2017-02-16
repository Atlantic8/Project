base.html中需要填充的是：

    username

problemset.html继承自base.html
需要填充的是：

	一系列有部分属性的problem实例，以及这些题的状态（从account的字段中查询）
    go_prev, go_next表示上一页和下一页的页码
    page：表示当前页面的页面数值

problem.html继承自base.html
需要填充的是：

	problem：一个代表部分属性的problem实例
    code：表示代码，如果有的话
    judged：表示是否是刚judge完的题目
    result：状态值

submission.html继承自base.html

	一系列有部分属性的submission实例
    go_prev, go_next表示上一页和下一页的页码
    page：表示当前页面的页面数值


