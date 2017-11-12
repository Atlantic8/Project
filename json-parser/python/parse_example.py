#!/usr/bin/python
# coding: utf-8
import json
from conf_parse import element, conf_tree

# 假设需要转化目标类为 class_name
class Method:
    def __init__(self):
        pass

class Ingredient:
    def __init__(self):
        pass

class Instruction:
    def __init__(self):
        pass

class target:
    def __init__(self):
        pass

class Box:
    def __init__(self):
        pass

global root_map

def name_filter(name):
    return ''.join(map(lambda x: x if x.isdigit() or x.isalpha() or x is '_' '#' else '', list(name)))

# 判断类型实例是否和类型id对应
# type_instance 是可以获取到的类型实例
# type_name 是类型name
# int, string, object, array
# 返回 True or False
def type_check(type_instance, type_name):
    #print (type(type_instance), type_name)
    if type_name.lower() == 'int':
        return True if (type(type_instance) is type(1) or type(type_instance) is type(1.1)) else False
    if type_name.lower() == 'string':
        return type(type_instance) == type('aaa') or type(type_instance) == type(u'aaa')
    # 类类型的匹配在json状态无法进行，只能先看其是不是字典
    if type_name == 'object':
        return type(type_instance) == type({})
    if type_name == 'array':
        return type(type_instance) == type([])
    return False


def load_configuration(filename):
    ctree = conf_tree()
    if ctree.construct_tree(filename) is False:
        print ('fail to construct tree')
    return ctree

# 解析array
# type是array元素类型
# json_instance是一个array
# class_name是元素的类型名，只有当array元素还是object时有用
def list_parse(type, json_instance, class_name=None):    
    # print ('call : ', type, json_instance)
    if len(json_instance) is 0: # 长度为0的list
        return json_instance 
    ret = []
    # list 中的元素是dict
    if type == 'object':
        # 检查list元素类型是否匹配
        if type_check(json_instance[0], type) is False:
            print ('list item type unmatch')
            return None
        for dict_item in json_instance:
            dict_instance = parse(dict_item, class_name)
            if dict_instance is None:
                print ('invalid json data, invalid dict items in list', class_name)
                #print (json.dumps(dict_item))
                return None
            ret.append(dict_instance)
        return ret
    # list 中的元素还是list
    # **嵌套array的暂时无法工作
    elif type == 'array':
        if type_check(json_instance[0], type) is not True:
            print ('list item type unmatch')
            return None
        for list_item in json_instance:
            list_instance = list_parse('array', list_item, )
            if list_instance is None:
                print ('invalid json data, invalid list items in list')
                return None
            ret.append(list_instance)
        return ret
    else: # 简单类型
        if type_check(json_instance[0], type) is not True:
            print ('list item type unmatch')
            return None
        for item in json_instance:
            ret.append(item)
        return ret

def parse(js, class_name):
    if type_check(js, 'object') is True:
    
        global root_map
        if not root_map.has_key(class_name):
            print ('class: ' + class_name + ' is not registered.')
            return None
        root = root_map[class_name]
        return parse_by_element(js, root)
    elif type_check(js, 'array') is True:
        # 这部分的修改涉及到conf文件的修改，第一层就是array不好命名，需要套个壳啥的
        pass

    else:
        print('none-json type')
        return None


def parse_by_element(json_instance, root):
    # create target instance
    target_instance = None
    # print (root.class_name)
    try:
        exec('target_instance = '+root.class_name+'()')
        #print (root.class_name+' instance created')
    except:
        print ('fail to create '+root.class_name+' instance')
        return target_instance
    # 对root的每一个子节点，即每一个属性
    for ele_key in root.sons.keys():
        ele = root.sons[ele_key]
        #  不能省略的属性不存在
        if ele.necessary == '1' and json_instance.has_key(ele_key) is False:
            print ('invalid json data, missing attribute: '+ele_key, root.class_name)
            print (str(json_instance))
            return None
        # 能省略的属性不存在
        elif ele.necessary == '0' and json_instance.has_key(ele_key) is False:
            # target_instance.{{ele_key}} = ele.default_value
            try:
                exec('target_instance.'+ name_filter(ele_key)+'= ele.default_value')
            except:
                exec('target_instance.'+name_filter(ele_key)+'= None')
                #pass
        else :
            # 检查是否是数组
            if ele.array=='1' or (ele.array=='2' and type_check(json_instance[ele_key], 'array') is True):
                if type_check(json_instance[ele_key], 'array') is False:
                    print ('invalid json data, array type unmatched')
                    return None
                # 如果list中的元素不是类型

                tmp_list = list_parse(ele.type, json_instance[ele_key], ele.class_name)
                if tmp_list is None:
                    print ('invalid json data, invalid list')
                    return None
                exec('target_instance.'+name_filter(ele_key)+'= tmp_list')
            # 不是数组，是复杂类型
            elif ele.type == 'object':
                if type_check(json_instance[ele_key], 'object') is False:
                    print ('invalid json data, object type unmatched')
                    #return None
                #print (json_instance[ele_key], ele.sons)
                dict_instance = parse(json_instance[ele_key], ele.class_name)
                if dict_instance is None:
                    print ('invalid json data, invalid sub-dictionary')
                    return None
                exec('target_instance.'+ name_filter(ele_key) +'= dict_instance')
            else: # 基本数据类型，int， string
                if type_check(json_instance[ele_key], ele.type) is False:
                    print ('invalid json data, basic type unmatched', ele.type, ele_key, json_instance[ele_key])
                    return None
                exec('target_instance.'+name_filter(ele_key)+'= json_instance[ele_key]')
    return target_instance

# 功能入口
# class_name表明输出应该是哪个类的实例
def parse_entry(conf_filename, json_str, class_name) :
    # load configure file
    global root_map
    try:
        # 加载conf文件
        ctree = load_configuration(conf_filename) 
        root_map = ctree.map
    except:
        print('fail to load configuration')


    # load json data.
    js = None
    try:
        js = json.loads(json_str)
    except:
        print('fail to load json string')
    obj = parse(js, class_name)
    return obj


if __name__ == "__main__":
    json_str = '{"id": 2135131,"name": "fanrong", "score":[84,92,95,88,76]}'
    json_str = '{"id": 2135131,"name": "jack", "sons":[{"id1": 199306, \
                "name1":"trump"}, {"id1":199507, "name1":"putin"}]}'
    #json_str = '[{"id": 199306, "name":"trump"}, {"id":199507.00, "name":"putin"}]'
    fp = open('test', 'r')
    data = fp.readline()
    jss = json.loads(data)['data'][0]['result']
    for js in jss:
    
        json_str = json.dumps(js)
        '''
        json_str = '{"id": "2135131","name": "jack", "son":{"id": "199306", \
                "name":"trump"}}'
        # print (json_str)
        '''
        target_name = 'Method'

        obj = parse_entry('test.conf', json_str, target_name)
        print (obj.id, obj.url)
    #print (obj.id, obj.name, obj.son)
    