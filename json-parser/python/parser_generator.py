#!/usr/bin/python
# coding: utf-8
# generate python code
from conf_parse import element, conf_tree

global writer
global root_map

def name_filter(name):     
    return "".join(map(lambda x: x if x.isdigit() or x.isalpha() or x not in ["_", "#"] else "", list(name)))

def generate_class_definition():
    global writer
    global root_map
    # 生成相应的类
    for key in root_map.keys():
        writer.write( 'class %s:\n%sdef __init__(self):\
                    \n%spass\n\n' % (key, get_tabs(1), get_tabs(2)) )

def get_tabs(n):
    if (type(n) != type(2) or n < 0):
        print ('non-int parameter for getting tabs', n)
    return "".join(["    " for i in range(n)])

def conf_load(conf_name):
    fp = open(conf_name, 'r')
    global root_map
    while True:
        line = fp.readline().strip()
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
    conf_data = ele_data.split('\t')
    # print (ele_data, len(conf_data))
    if len(conf_data) != 6:
        print ('you may need to check configure file.')
        print ('attention, wrong conf_file format: ', conf_data, len(conf_data))
        return False

    if root_p.sons.has_key(conf_data[0]):
        print ('Warning: duplicate attribute name.')
        return False
    new_ele = element(conf_data[0], conf_data[1], \
            conf_data[2], conf_data[3], conf_data[4], conf_data[5].strip('\n'))
    root_p.sons[conf_data[0]] = new_ele

    return True


def parser_list(ele_key, ele, n, class_name):
    data = ('%sret = []\
    \n%sif len(json_instance["%s"]) == 0:\
    \n%spass\n' % \
            (get_tabs(n), get_tabs(n), ele_key, get_tabs(n+1)) )
    # 检查数组元素类型是否正确
    data += ('%selse:\n%sif type_check(json_instance["%s"][0], "%s") == False:\
    \n%sprint ("array item type unmatch") \
    \n%sreturn None\n' %\
            (get_tabs(n), get_tabs(n+1), ele_key, ele.type, \
            get_tabs(n+2), get_tabs(n+2)) )
    # 循环获得元素
    data += ('%sfor item in json_instance["%s"]:\n' % (get_tabs(n+1), ele_key) )
    if ele.type == 'object':
        data += ('%sinstance = parser_%s(item)\
        \n%sif instance is None:\
        \n%sprint ("invalid object item in array", "%s")\
        \n%sreturn None\
        \n%sret.append(instance)\n' % (get_tabs(n+2), class_name, \
                get_tabs(n+2), get_tabs(n+3), class_name, \
                get_tabs(n+3), get_tabs(n+2) ) )
    elif ele.type == 'array':
        pass
    else:
        data += parser_basic('item', ele_key, ele, n+2, 1)
        ''' data += ('%sinstance = parser_basic(item)\
        \n%sif instance is None:\
        \n%sprint ("invalid basic item in array", item)\
        \n%sreturn None\n' % (get_tabs(n+1), get_tabs(n+1), \
                get_tabs(n+2), get_tabs(n+2) ) ) '''
    data += ('%sexec("target_instance.%s=ret")\n'\
     % (get_tabs(n),name_filter(ele_key) ))
    return data

# js是内容， ele_key是对应的变量名，ele是元素规则
def parser_basic(js, ele_key, ele, n, isList = None):
    data = ('%sif type_check(%s, "%s") == False:\
    \n%sprint ("invalid json data, basic type unmatched","%s", %s)\
    \n%sreturn None\
    \n%sexec(\'target_instance.%s= %s\')\n'\
            % (get_tabs(n), js, ele.type, get_tabs(n+1), ele.type, \
            js, get_tabs(n+1), get_tabs(n),name_filter(ele_key), js )) if isList is None \
    else ('%sif type_check(%s, "%s") == False:\
    \n%sprint ("invalid json data, basic type unmatched","%s", %s)\
    \n%sreturn None\
    \n%sret.append(item)\n'\
            % (get_tabs(n), js, ele.type, get_tabs(n+1), ele.type, \
            js, get_tabs(n+1), get_tabs(n) ))
    return data

# js是解析对象， ele是元素规则，class_name是类名
def parser_help(js, class_name, ele_key, ele, n):
    global root_map
    data = ''
    # 是否是数组
    if ele.array == '1':
        # 不是数组
        data += ('%sif type_check(%s, "array") == False:\
        \n%sprint ("invalid json data, array type unmatched", \'%s\', %s)\
        \n%sreturn None\n' % (get_tabs(n), js, get_tabs(n+1), \
                ele.type, js, get_tabs(n+1) ))
        # 是数组
        data += parser_list(ele_key, ele, n, class_name)
    elif ele.array == '2':
        data += ('%sif type_check(%s, "array") == True:\n'% (get_tabs(n), js ) )
        data += parser_list(ele_key, ele, n+1, class_name)
        data += ('%selse:\n' % (get_tabs(n)) )
        if ele.type == 'object':
            # 不是object
            data += ('%sif type_check(%s, "object") == False:\
            \n%sprint ("invalid json data, object type unmatched")\
            \n%sreturn None\n' % (get_tabs(n+1), js, get_tabs(n+2), 
                    get_tabs(n+2)) )
            # 是object
            data += ('%sobj_instance = parser_%s(%s)\
            \n%sif obj_instance is None:\
            \n%sprint ("invalid json data, invalid sub-dictionary")\
            \n%sreturn None\
            \n%sexec("target_instance.%s=obj_instance")\n'\
                    % (get_tabs(n+1), class_name, js, get_tabs(n+1), get_tabs(n+2), \
                    get_tabs(n+2), get_tabs(n+1),name_filter(ele_key) ) )
        else:
            data += parser_basic('%s'%(js), ele_key, ele, n+1)
        data += ('%sret = [] \
                \n%sif target_instance.%s is not None: \
                \n%sret.append(target_instance.%s)\
                \n%sexec("target_instance.%s=ret")\n' % \
                (get_tabs(n+1), get_tabs(n+1), name_filter(ele_key), get_tabs(n+2),name_filter(ele_key),\
                 get_tabs(n+1), name_filter(ele_key)))
    elif ele.array == '0':
        if ele.type == 'object':
            # 不是object
            data += ('%sif type_check(%s, "object") == False:\
            \n%sprint ("invalid json data, object type unmatched")\
            \n%sreturn None\n' % (get_tabs(n), js, get_tabs(n+1), 
                    get_tabs(n+1)) )
            # 是object
            data += ('%sobj_instance = parser_%s(%s)\
            \n%sif obj_instance is None:\
            \n%sprint ("invalid json data, invalid sub-dictionary")\
            \n%sreturn None\
            \n%sexec("target_instance.%s=obj_instance")'\
                    % (get_tabs(n), class_name, js, \
                    get_tabs(n), get_tabs(n+1), get_tabs(n+1), get_tabs(n),name_filter(ele_key) ) )
        else:
            data += parser_basic('%s'%js, ele_key, ele, n)
    return data


# 产生类class_name的解析代码
def class_parser(class_name):
    global writer, root_map
    data = ('def parser_%s (json_instance):\n' % class_name)

    data += ('%s# create target instance\
    \n%starget_instance = None\
    \n%stry:\
    \n%sexec("target_instance = %s()")\
    \n%sexcept:\
    \n%sprint ("fail to create %s instance")\
    \n%sreturn target_instance\n' % (get_tabs(1), get_tabs(1), \
            get_tabs(1), get_tabs(2), class_name, get_tabs(1), \
            get_tabs(2), class_name, get_tabs(2) ))
    
    root = root_map[class_name]
    for ele_key in root.sons.keys():
        ele = root.sons[ele_key]
        data += ('\n%s# for attribute %s, %s \n' % (get_tabs(1), \
                ele_key, "array" if ele.array=='1' else "probably array" \
                if ele.array=='2' else ele.type) )
        #  不能省略的属性
        if ele.necessary == '1':
            n = 1
            # 不存在
            data += ('%sif json_instance.has_key("%s") == False:\
            \n%sprint ("invalid json data, missing attribute: ","%s", "%s")\
            \n%sreturn None\n%selse:\n' % (get_tabs(n), ele_key, get_tabs(n+1), 
                    ele_key, class_name, get_tabs(n+1), get_tabs(n)) )
            data += parser_help('json_instance["%s"]'%ele_key, ele.class_name, ele_key, ele, n+1)
        else:
            n = 1
            # 可忽略的不存在
            data += ('%sif json_instance.has_key("%s") == False:\n%stry:\
            \n%sexec("target_instance.%s=%s")\
            \n%sexcept:\n%sexec("target_instance.%s=None")\
            \n%selse:\n' % (get_tabs(n), ele_key, get_tabs(n+1), get_tabs(n+2), \
            name_filter(ele_key), ele.default_value ,get_tabs(n+1), get_tabs(n+2),name_filter(ele_key), \
            get_tabs(n)) )
            # 可忽略的存在
            data += parser_help('json_instance["%s"]'%ele_key, ele.class_name, ele_key, ele, n+1)
    data += '\n%sreturn target_instance\n\n' % get_tabs(1)
    writer.write(data)

# 给定配置文件的路径、
def main(conf_file, target_filename):
    global writer, root_map
    root_map = {}
    writer = open('./'+target_filename, 'w')
    writer.write('#!/usr/bin/python\n# coding: utf-8\nimport json\n\n\n')
    # 写入函数name_filter
    writer.write('def name_filter(name): \
    \n%sreturn "".join(map(lambda x: x if x.isdigit() \
or x.isalpha() or x not in ["_", "#"] else "", list(name)))\n\n\n' % (get_tabs(1)))
    
    # 写入函数type_check
    writer.write('# 判断类型实例是否和类型id对应 \
                \n# type_instance 是可以获取到的类型实例 \
                \n# type_name 是类型name \
                \n# int, string, object, array \
                \n# 返回 True or False \
                \ndef type_check(type_instance, type_name): \
                    \n%s#print (type(type_instance), type_name) \
                    \n%sif type_name.lower() == "int": \
                        \n%sreturn True if (type(type_instance) is type(1) or type(type_instance) is type(1.1)) else False \
                    \n%sif type_name.lower() == "string": \
                        \n%sreturn type(type_instance) == type("aaa") or type(type_instance) == type(u"aaa") \
                    \n%s# 类类型的匹配在json状态无法进行，只能先看其是不是字典 \
                    \n%sif type_name == "object": \
                        \n%sreturn type(type_instance) == type({}) \
                    \n%sif type_name == "array": \
                        \n%sreturn type(type_instance) == type([]) \
                    \n%sreturn False\n\n\n' % (get_tabs(1),get_tabs(1),get_tabs(2),get_tabs(1), \
                    get_tabs(2),get_tabs(1),get_tabs(1),get_tabs(2),get_tabs(1),get_tabs(2),get_tabs(1)) )

    conf_load(conf_file)
    generate_class_definition()

    # 写入类解析代码
    for key in root_map.keys():
        class_parser(key)
        #break
    # 写入功能入口
    writer.write('#功能入口，将json_instance解析到class_name类的实例中\
                \ndef parser(json_instance, class_name):\
                \n%sexec("obj = parser_"+class_name+"(json_instance)")\
                \n%sif not obj:\
                \n%sprint ("none object")\
                \n%selse:\
                \n%sprint("transformation succeed")\n\
                \n%sreturn obj\n\n' \
                % (get_tabs(1), get_tabs(1), get_tabs(2), \
                get_tabs(1), get_tabs(2), get_tabs(2) ))
    
    # 写入主函数
    writer.write('#主函数\
                \nif __name__ == "__main__":\
                \n%s# write your code here\
                \n%s# parser(json_instance, class_name)\n' % \
                (get_tabs(1), get_tabs(1) ) )
    writer.close()

if __name__ == "__main__":
    conf_file = './../conf/test.conf'
    target_name = './../output/generated_parser.py'
    # 输出在output文件夹下
    main(conf_file, target_name)