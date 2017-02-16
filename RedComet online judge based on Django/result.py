# coding=utf-8


# 这个映射关系是前后端通用的,判题服务器提供接口,也应该遵守这个,可能需要一些转换
result = {
    "Accepted": 0,
    "Runtime_error": 1,
    "Time_Limit_Exceeded": 2,
    "Memory_Limit_Exceeded": 3,
    "Compile_Error": 4,
    "Format_Error": 5,
    "Wrong_Answer": 6,
    "System_Error": 7,
    "Waiting": 8
}

rev_result = {
	0 : "Accepted",
    1 : "Runtime_error",
    2 : "Time_Limit_Exceeded",
    3 : "Memory_Limit_Exceeded",
    4 : "Compile_Error",
    5 : "Format_Error",
    6 : "Wrong_Answer",
    7 : "System_Error",
    8 : "Waiting"
}
