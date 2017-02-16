##### account:
account主要存储的是用户数据，其字段包括：

	user_name -> char
    nick_name -> char
    password -> char
    is_forbidden -> bool
    // JSON字典用来表示当前user的解题情况<problem_id:status> 0为ac，1为正在进行, 2: not start
    problems_status -> char
    email -> Email
    create_time -> DateTime


##### problem
problem对应题目，其字段包括：

	problem_id -> int
    title -> char
    content -> char
    difficulty -> int
    example -> text
    create_time -> DateTime
    submission_number -> int
    accept_number -> int
    acceptance -> float
    accept_user_number -> int
    hint -> RichText
    visible -> bool
    time_limit -> int (ms)
    memory_limit -> int (MB)


##### submission
submission对应每次的提交，其字段包括：

	id -> char
    user_name -> char
    problem_id -> int
    title -> char
    create_time -> DateTime
    time_used -> int (ms)
    language -> char (c, c++, java, python)
	memory_used -> int





