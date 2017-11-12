#!/usr/bin/python
# coding: utf-8
# generate cpp code
from conf_parse import element, conf_tree

global writer
global root_map

log_source = 'std'

# formatted log data, without spaces
# log_source could be std(cout) or other
# log_type could be error, warning, log
def log_factory(log_content, log_type='log'):
    data = ''
    if log_source == 'std':
        data += 'cout << "%s: " << ' % log_type
        data += '"%s" << endl;' % log_content
    elif log_source == 'xbot':
        if log_type == 'warning':
            data += 'warning_log("warning: %s");' % log_content
        elif log_type == 'error':
            data += 'fatal_error("fatal error: %s");' % log_content
        elif log_type == "log":
            data += 'WRITE_APP_DEBUG_LOG(APP, "log: %s");' % log_content

    return data

# 生成类的定义，测试的时候用
def generate_class_definition(class_name):
    global writer, root_map
    root = root_map[class_name]
    data = 'struct %s{\n' % class_name
    for key in root.sons.keys():
        instance = root.sons[key]
        if instance.array != '0':
            if instance.type == 'object':
                data += '\tstd::vector<%s*> %s;\n' % (instance.class_name, name_filter(instance.mapped_name))
            else:
                data += '\tstd::vector<%s> %s;\n' % (instance.type, name_filter(instance.mapped_name))
        elif instance.type == 'object':
            data += '\t%s* %s = NULL;\n' % (instance.class_name, name_filter(instance.mapped_name))
        else:
            data += '\t%s %s;\n' % (instance.type, name_filter(instance.mapped_name))

    data += '};\n\n'
    writer.write(data)

def get_tabs(n):
    if (type(n) != type(2) or n < 0):
        print ('non-int parameter for getting tabs', n)
    return "".join(["    " for i in range(n)])

def name_filter(name):
    ret = ''
    for c in list(name):
        if c.isdigit() is True:
            ret += c
        elif c.isalpha() is True:
            ret += c
        elif c == '_':
            ret += c
    return ret

def conf_load(conf_name):
    fp = open(conf_name, 'r')
    global root_map
    while True:
        line = fp.readline().strip('\n')
        if not line:
            break
        lines = line.split('\t')
        print (len(lines), lines)
        class_name = lines[1]
        rule_number = int(lines[2])
        #print (rule_number)
        root = element('root', '4', '1', '0', '0', class_name)
        for i in range(rule_number):
            line = fp.readline()
            if construct_tree_by_element(root, line) is False:
                print ('fail to construct element')
        root_map[class_name] = root

def construct_tree_by_element(root_p, ele_data):
    conf_data = ele_data.strip('\n').split('\t')
    # print (ele_data, len(conf_data))
    if len(conf_data) < 6:
        print ('you may need to check configure file.')
        print ('attention, wrong conf_file format: ', conf_data, len(conf_data))
        return False

    if root_p.sons.has_key(conf_data[0]):
        print ('Warning: duplicate attribute name.')
        return False
    new_ele = element(conf_data[0], conf_data[1], \
            conf_data[2], conf_data[3], conf_data[4], conf_data[5])

    new_ele.mapped_name = conf_data[6] if len(conf_data)>6 else new_ele.name

    root_p.sons[conf_data[0]] = new_ele

    return True

def type_tansfer(type):
    if type == 'double' or type == 'float':
        return 'real'
    elif type in ['int', 'string', 'object']:
        return type
    else:
        print ('warning, type error while transfering type name.')
        return None

def parser_list(ele_key, ele, n, class_name):
    data = ''
    if ele.type == 'object':
        data += ('%sstd::vector<%s*> ret;' % (get_tabs(n), class_name))
        data += ('\n%sif (json_instance["%s"].size() == 0) {' % (get_tabs(n), ele_key))
        data += ('\n%sinstance->%s = ret;' % (get_tabs(n+1), ele.mapped_name) )
        data += ('\n%s'%get_tabs(n+1) + log_factory('parse list %s with zero size.'%ele.mapped_name, 'log') + '\n')
    elif ele.type == 'array':
        pass
    else:
        data += ('%sstd::vector<%s> ret;' % (get_tabs(n), ele.type) )
        data += ('\n%sif (json_instance["%s"].size() == 0) {' % (get_tabs(n), ele_key) )
        data += ('\n%sinstance->%s = ret;\n' % (get_tabs(n+1), ele.mapped_name) )
        data += ('\n%s'%get_tabs(n+1) + log_factory('parse list %s with zero size.'%ele.mapped_name, 'log') + '\n')

    # 检查数组元素类型是否正确
    data += ('%s} else {' % (get_tabs(n)) )
    data += ('\n%sif (json_instance["%s"][0].type() != Json::%sValue) {' % (get_tabs(n+1), ele_key, type_tansfer(ele.type)) )
    #data += ('\n%sif (type_check(json_instance["%s"][0], "%s") == false) {' % (get_tabs(n+1), ele_key, ele.type) )

    data += ('\n%s' % (get_tabs(n+2)) + log_factory("array item %s type unmatch"%ele_key, 'error') )
    data += ('\n%sreturn NULL;\n%s}\n' % (get_tabs(n+2), get_tabs(n+1)) )
    
    # 循环获得元素
    data += ('%sfor (Json::Value item : json_instance["%s"]) {\n' % (get_tabs(n+1), ele_key) )
    if ele.type == 'object':
        data += ('%sauto inst = parser_%s(item);' % (get_tabs(n+2), class_name) )
        data += ('\n%sif (inst == NULL) {' % (get_tabs(n+2)) )
        log_data = "invalid object item %s in array, %s." % (ele_key, class_name)
        data += ('\n%s' % get_tabs(n+3) + log_factory(log_data, 'error') )
        data += ('\n%sreturn NULL;\n%s}' % (get_tabs(n+3), get_tabs(n+2)) )
        data += ('\n%sret.push_back(inst);\n' % (get_tabs(n+2)) )
    elif ele.type == 'array':
        pass
    else:
        data += parser_basic('item', ele_key, ele, n+2, 2)
    data += ('%s}\n%sinstance->%s=ret;\n%s}'\
     % (get_tabs(n+1), get_tabs(n+1), name_filter(ele.mapped_name), get_tabs(n) ))
    data += ('\n%s'%get_tabs(n) + log_factory('parse element %s complete.'%ele.mapped_name, 'log') + '\n')
    return data

# 首字母变成大写
def name_transfer(type_name):
    # 可以转化int，float，double，long，string等单个单词构成的type
    return type_name.title()

# js是内容， ele_key是对应的变量名，ele是元素规则
# 基本类型包括：int，float，string（有需要可以加上double）
def parser_basic(js, ele_key, ele, n, isList):
    data = ''
    if isList == 0: # 就是简单的元素
        data += ('\n%sif (%s.type() != Json::%sValue) {' % (get_tabs(n), js, type_tansfer(ele.type)) )
        #data += ('%sif (type_check(%s, "%s") == false) {' % (get_tabs(n), js, ele.type) )
        log_data = "invalid json data, basic type %s unmatched, %s." % (ele_key, ele.type)
        data += ('\n%s' % get_tabs(n+1) + log_factory(log_data, 'error') )

        data += ('\n%sreturn NULL;\n%s}' % (get_tabs(n+1), get_tabs(n)) )
        data += ('\n%sinstance->%s=%s.as%s();' % (get_tabs(n), \
                name_filter(ele.mapped_name), js, name_transfer(ele.type)) )
        data += ('\n%s'%get_tabs(n) + log_factory('parse basic element %s complete.'%ele.mapped_name, 'log') + '\n')
        
    elif isList == 1: # 本来应该是list，但是退化成单个元素了
        data += ('\n%sif (%s.type() != Json::%sValue) {' % (get_tabs(n), js, type_tansfer(ele.type)) )
        #data += ('%sif (type_check(%s, "%s") == false) {' % (get_tabs(n), js, ele.type) )
        log_data = "invalid json data, basic type %s unmatched, %s." % (ele_key, ele.type)
        data += ('\n%s' % get_tabs(n+1) + log_factory(log_data, 'error') )
        
        data += ('\n%sreturn NULL;\n%s}' % (get_tabs(n+1), get_tabs(n)) )
        data += ('\n%sstd::vector<%s> ret;' % (get_tabs(n), ele.type) )
        data += ('\n%sret.push_back(%s.as%s());' % (get_tabs(n), js, name_transfer(ele.type)) )
        data += ('\n%sinstance->%s = ret;' % (get_tabs(n), ele.mapped_name) )
        data += ('\n%s'%get_tabs(n) + log_factory('parse basic element %s complete.'%ele.mapped_name, 'log') + '\n')
        
    elif isList == 2: # list中的元素
        data += ('\n%sif (%s.type() != Json::%sValue) {' % (get_tabs(n), js, type_tansfer(ele.type)) )
        #data += ('%sif (type_check(%s, "%s") == false) {' % (get_tabs(n), js, ele.type) )
        log_data = "invalid json data, basic type %s unmatched, %s." % (ele_key, ele.type)
        data += ('\n%s' % get_tabs(n+1) + log_factory(log_data, 'error') )

        data += ('\n%sreturn NULL;\n%s}' % (get_tabs(n+1), get_tabs(n)) )
        data += ('\n%sret.push_back(item.as%s());\n' % (get_tabs(n), name_transfer(ele.type)) )
        
    return data

# js是解析对象， ele是元素规则，class_name是类名
def parser_help(js, class_name, ele_key, ele, n):
    global root_map
    data = ''
    # 是否是数组
    if ele.array == '1':
        # 不是数组
        data += ('\n%sif (%s.type() != Json::arrayValue) {\n' % (get_tabs(n), js) )
        #data += ('%sif (type_check(%s, "array") == false) {' % (get_tabs(n), js) )
        log_data = "invalid json data, array type %s unmatched, %s" % (ele_key, ele.type)
        data += ('\n%s' % get_tabs(n+1) + log_factory(log_data, 'error') )

        data += ('\n%sreturn NULL;\n%s}\n' % (get_tabs(n+1), get_tabs(n)) )
        # 是数组
        data += parser_list(ele_key, ele, n, ele.class_name)
    elif ele.array == '2':
        data += ('\n%sif (%s.type() == Json::arrayValue) {\n' % (get_tabs(n), js) )
        #data += ('%sif (type_check(%s, "array") == true) {\n'% (get_tabs(n), js ) )
        data += parser_list(ele_key, ele, n+1, class_name)
        data += ('%s} else {\n' % (get_tabs(n)) )
        if ele.type == 'object':
            # 不是object
            data += ('\n%sif (%s.type() != Json::objectValue) {' % (get_tabs(n+1), js) )
            #data += ('%sif (type_check(%s, "object") == false) {' % (get_tabs(n+1), js) )
            log_data = "invalid json data, object type %s unmatched" % ele_key
            data += ('\n%s' % get_tabs(n+2) + log_factory(log_data, 'error') )

            data += ('\n%sreturn NULL;\n%s}\n' % (get_tabs(n+2), get_tabs(n+1)) )
            # 是object
            data += ('%sauto obj_instance = parser_%s(%s);' % (get_tabs(n+1), class_name, js) )
            data += ('\n%sif (obj_instance == NULL) {' % (get_tabs(n+1)) )
            log_data = "invalid json data, invalid sub-dictionary"
            data += ('\n%s' % get_tabs(n+2) + log_factory(log_data, 'error') )

            data += ('\n%sreturn NULL;\n%s}' % (get_tabs(n+2), get_tabs(n+1)) )
            data += ('\n%sstd::vector<%s*> ret;' % (get_tabs(n+1), ele.class_name) )
            data += ('\n%sret.push_back(obj_instance);' % (get_tabs(n+1)) )
            data += ('\n%sinstance->%s = ret;' % (get_tabs(n+1), name_filter(ele.mapped_name)) )
            data += ('\n%s'%get_tabs(n+1) + log_factory('parse object element %s complete.'%ele.mapped_name, 'log') + '\n')
            
        else:
            data += parser_basic('%s'%(js), ele_key, ele, n+1, 1)
        data += ('\n%s}\n' % get_tabs(n))
    else:
        if ele.type == 'object':
            # 不是object
            data += ('\n%sif (%s.type() != Json::objectValue) {' % (get_tabs(n), js) )
            #data += ('%sif (type_check(%s, "object") == false) {' % (get_tabs(n), js) )
            log_data = "invalid json data, object type %s unmatched" % ele_key
            data += ('\n%s' % get_tabs(n+1) + log_factory(log_data, 'error') )

            data += ('\n%sreturn NULL;\n%s}\n' % (get_tabs(n+1), get_tabs(n)) )
            
            # 是object
            data += ('%sauto obj_instance = parser_%s(%s);' % (get_tabs(n), class_name, js) )
            data += ('\n%sif (obj_instance == NULL) {' % (get_tabs(n)) )
            log_data = "invalid json data, sub-dictionary %s unmatched" % ele_key
            data += ('\n%s' % get_tabs(n+1) + log_factory(log_data, 'error') )

            data += ('\n%sreturn NULL;\n%s}\n' % (get_tabs(n+1), get_tabs(n)) )
            data += ('\n%sinstance->%s=obj_instance;\n' % (get_tabs(n), name_filter(ele.mapped_name)) )
            data += ('\n%s'%get_tabs(n) + log_factory('parse object element %s complete.'%ele.mapped_name, 'log') + '\n')
        else:
            data += parser_basic('%s'%js, ele_key, ele, n, 0)
    return data


# 产生类class_name的解析代码
def class_parser(class_name):
    global writer, root_map
    data = ('%s* parser_%s (const Json::Value& json_instance) {\n' % (class_name, class_name) )

    data += ('%s// create target instance' % get_tabs(1))
    data += ('\n%s%s* instance = new %s();' % (get_tabs(1), class_name, class_name ))
    
    root = root_map[class_name]
    for ele_key in root.sons.keys():
        ele = root.sons[ele_key]
        data += ('\n%s// for attribute %s, %s \n' % (get_tabs(1), \
                ele_key, "array" if ele.array=='1' else "probably array" \
                if ele.array=='2' else ele.type) )
        #  不能省略的属性
        if ele.necessary == '1':
            n = 1
            # 不存在
            data += ('%sif (json_instance.isMember("%s") == false) {' % (get_tabs(n), ele_key) )
            log_data = "invalid json data, missing attribute, %s, %s" % (ele_key, class_name)
            data += ('\n%s' % get_tabs(n+1) + log_factory(log_data, 'error') )

            data += ('\n%sreturn NULL;\n%s} else {\n' % (get_tabs(n+1), get_tabs(n)) )
            
            data += parser_help('json_instance["%s"]'%ele_key, ele.class_name, ele_key, ele, n+1)
        else:
            n = 1
            # 可忽略的不存在
            data += ('%sif (json_instance.isMember("%s") == false) {' % (get_tabs(n), ele_key) )
            data += ('\n%s//c++中的默认初始化操作应该在构造函数中完成' % (get_tabs(n+1)) )
            data += ('\n%s;' % (get_tabs(n+1)) )
            data += ('\n%s} else {\n' % (get_tabs(n)) )
            
            # 可忽略的存在
            data += parser_help('json_instance["%s"]'%ele_key, ele.class_name, ele_key, ele, n+1)
        data += ('%s}\n' % get_tabs(n))
    data += '\n%sreturn instance;\n' % get_tabs(1)
    data += '}\n\n'
    writer.write(data)

def main(target_file, conf_file):
    global writer, root_map
    root_map = {}
    writer = open(target_file, 'w')
    data = '#include <iostream>\n'
    data += '#include <vector>\n'
    data += '#include "json/json.h"\n'
    data += '#include <string>\n'
    data += '#include<fstream>\n'
    data += '// 如果需要，请自行添加头文件.\n\n\n'
    data += 'using namespace std;\n\n'
    
    
    # 写入函数type_check
    data += '// 判断类型实例是否和类型id对应'
    data += '\n// type_instance 是可以获取到的类型实例'
    data += '\n// type_name 是类型name'
    data += '\n// int, double, string, object, array'
    data += '\n// 返回 true or false'
    data += '\nbool type_check(Json::Value instance, std::string type_name) {'
    data += ('\n%s// print (type(type_instance), type_name)' % (get_tabs(1)) )
    data += ('\n%sif (type_name == "int")' % (get_tabs(1)) )
    data += ('\n%sreturn instance.isInt()||instance.isInt64()||instance.isUInt()||instance.isUInt64()? true:false;' % (get_tabs(2)) )
    data += ('\n%sif (type_name == "float" || type_name == "double")' % (get_tabs(1)) )
    data += ('\n%sreturn instance.isDouble()? true:false;' % (get_tabs(2)) )
    data += ('\n%sif (type_name == "string")' % (get_tabs(1)) )
    data += ('\n%sreturn instance.isString()?true:false;' % (get_tabs(2)) )
    data += ('\n%s// 类类型的匹配在json状态无法进行，只能先看其是不是字典' % (get_tabs(1)) )
    data += ('\n%sif (type_name == "object")' % (get_tabs(1)) )
    data += ('\n%sreturn instance.isObject()?true:false;' % (get_tabs(2)) )
    data += ('\n%sif (type_name == "array")' % (get_tabs(1)) )
    data += ('\n%sreturn instance.isArray()?true:false;' % (get_tabs(2)) )
    data += ('\n%sreturn false;\n}\n\n\n' % (get_tabs(1)) )

    conf_load(conf_file)

    # 写入类声明
    for class_name in root_map.keys():
        data += ('struct %s;\n'%class_name)
    
    # 写入函数声明
    for class_name in root_map.keys():
        data += ('%s* parser_%s(const Json::Value &json_instance);\n' % (class_name, class_name))

    data += '\n'

    writer.write(data)
    # 写入c++类定义
    for class_name in root_map.keys():
        generate_class_definition(class_name)

    data = ''

    # 写入类解析代码
    for key in root_map.keys():
        class_parser(key)
        #break
    
    # 写入主函数
    data += '// 主函数'
    data += '\nint main() {'
    data += ('\n%s// write your code here' % (get_tabs(1)) )
    data += ('\n%s// -------------------- \n' % (get_tabs(1)) )
    data += ('\n%sifstream is("test");' % (get_tabs(1)) )
    data += ('\n%sstring str("");' % (get_tabs(1)) )
    data += ('\n%sgetline(is, str);' % (get_tabs(1)) )
    data += ('\n%sJson::Reader reader;' % (get_tabs(1)) )
    data += ('\n%sJson::Value root;' % (get_tabs(1)) )
    data += ('\n%sif (reader.parse(str, root)) {' % (get_tabs(1)) )
    data += ('\n%sJson::Value js = root["data"][0]["result"][0];' % (get_tabs(2)) )
    data += ('\n%sMethod* obj = parser_Method(js);' % (get_tabs(2)) )
    data += ('\n%scout << obj->id << endl;' % (get_tabs(2)) )
    data += ('\n%s}' % (get_tabs(1)) )
    data += ('\n%sreturn 0;\n}\n' % (get_tabs(1)) )
    
    writer.write(data)
    writer.close()

if __name__ == "__main__":
    target_file = 'gparser.cpp'
    conf_file = 'conf/test.conf'
    log_source = 'std' # std is executable, xbot is for code migration
    main(target_file, conf_file)